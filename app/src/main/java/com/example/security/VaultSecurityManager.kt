package com.example.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class VaultSecurityManager(private val context: Context) {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val PIN_KEY_ALIAS = "lakexpense_vault_pin_key"
        private const val AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH_BITS = 128
        private const val PBKDF2_ITERATIONS = 65_536
        private const val PBKDF2_KEY_BITS = 256
    }

    private val random = SecureRandom()

    fun isBiometricAvailable(): Boolean {
        return try {
            val manager = BiometricManager.from(context)
            val result = manager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            result == BiometricManager.BIOMETRIC_SUCCESS
        } catch (_: Exception) {
            false
        }
    }

    fun authenticateBiometric(
        activity: FragmentActivity,
        title: String = "Unlock LakExpense Vault",
        subtitle: String = "Verify your identity to access your offline ledger",
        negativeButtonText: String = "Use Master PIN",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isBiometricAvailable()) {
            onError("Biometric authentication is not enrolled or supported on this device.")
            return
        }
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                        errorCode != BiometricPrompt.ERROR_USER_CANCELED
                    ) {
                        onError(errString.toString())
                    }
                }
            }
        )
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            .build()
        prompt.authenticate(promptInfo)
    }

    fun createPinEncryptedBlob(pin: String): String {
        val salt = ByteArray(16).also { random.nextBytes(it) }
        val hash = hashPin(pin, salt)
        val payload = JSONObject().apply {
            put("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
            put("hash", Base64.encodeToString(hash, Base64.NO_WRAP))
        }.toString()

        val key = getOrCreateKeystoreKey()
        return if (key != null) {
            val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val iv = cipher.iv
            val cipherBytes = cipher.doFinal(payload.toByteArray(StandardCharsets.UTF_8))
            JSONObject().apply {
                put("mode", "keystore_gcm")
                put("iv", Base64.encodeToString(iv, Base64.NO_WRAP))
                put("ct", Base64.encodeToString(cipherBytes, Base64.NO_WRAP))
            }.toString()
        } else {
            // Fallback for test environments without AndroidKeyStore hardware backing
            JSONObject().apply {
                put("mode", "salted_sha256")
                put("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
                put("hash", Base64.encodeToString(hash, Base64.NO_WRAP))
            }.toString()
        }
    }

    fun verifyPin(pin: String, encryptedBlob: String): Boolean {
        return try {
            val root = JSONObject(encryptedBlob)
            val payloadJson = when (root.optString("mode")) {
                "keystore_gcm" -> {
                    val key = getOrCreateKeystoreKey() ?: return false
                    val iv = Base64.decode(root.getString("iv"), Base64.NO_WRAP)
                    val ct = Base64.decode(root.getString("ct"), Base64.NO_WRAP)
                    val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
                    cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
                    val plain = cipher.doFinal(ct)
                    JSONObject(String(plain, StandardCharsets.UTF_8))
                }
                "salted_sha256" -> root
                else -> return false
            }
            val salt = Base64.decode(payloadJson.getString("salt"), Base64.NO_WRAP)
            val expectedHash = Base64.decode(payloadJson.getString("hash"), Base64.NO_WRAP)
            val actualHash = hashPin(pin, salt)
            MessageDigest.isEqual(expectedHash, actualHash)
        } catch (_: Exception) {
            false
        }
    }

    fun encryptBackupPayload(plainJson: String, password: String): String {
        val salt = ByteArray(16).also { random.nextBytes(it) }
        val iv = ByteArray(12).also { random.nextBytes(it) }
        val secretKey = deriveAesKey(password, salt)
        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
        val cipherText = cipher.doFinal(plainJson.toByteArray(StandardCharsets.UTF_8))

        return JSONObject().apply {
            put("format", "LAKEXPENSE_VAULT_V1")
            put("cipher", "AES-256-GCM")
            put("kdf", "PBKDF2WithHmacSHA256")
            put("iterations", PBKDF2_ITERATIONS)
            put("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
            put("iv", Base64.encodeToString(iv, Base64.NO_WRAP))
            put("payload", Base64.encodeToString(cipherText, Base64.NO_WRAP))
            put("exportedAt", System.currentTimeMillis())
        }.toString(2)
    }

    fun decryptBackupPayload(encryptedContent: String, password: String): String {
        val envelope = JSONObject(encryptedContent)
        require(envelope.optString("format") == "LAKEXPENSE_VAULT_V1") {
            "Unrecognized LakExpense vault backup file."
        }
        val salt = Base64.decode(envelope.getString("salt"), Base64.NO_WRAP)
        val iv = Base64.decode(envelope.getString("iv"), Base64.NO_WRAP)
        val cipherText = Base64.decode(envelope.getString("payload"), Base64.NO_WRAP)
        val iterations = envelope.optInt("iterations", PBKDF2_ITERATIONS)

        val secretKey = deriveAesKey(password, salt, iterations)
        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
        val plainBytes = cipher.doFinal(cipherText)
        return String(plainBytes, StandardCharsets.UTF_8)
    }

    private fun deriveAesKey(
        password: String,
        salt: ByteArray,
        iterations: Int = PBKDF2_ITERATIONS
    ): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, PBKDF2_KEY_BITS)
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    private fun hashPin(pin: String, salt: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        digest.update(pin.toByteArray(StandardCharsets.UTF_8))
        return digest.digest()
    }

    private fun getOrCreateKeystoreKey(): SecretKey? {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            val existing = keyStore.getKey(PIN_KEY_ALIAS, null) as? SecretKey
            if (existing != null) return existing

            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val spec = KeyGenParameterSpec.Builder(
                PIN_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            keyGenerator.init(spec)
            keyGenerator.generateKey()
        } catch (_: Exception) {
            null
        }
    }
}
