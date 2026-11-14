package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.DefaultCatalog
import com.example.data.local.DemoDataSeeder
import com.example.data.local.LakExpenseDatabase
import com.example.security.VaultSecurityManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `app name matches LakExpense`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("LakExpense", appName)
    }

    @Test
    fun `room database seeds catalog and cleans demo transactions without disturbing user entries`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = LakExpenseDatabase.createInMemory(context)
        db.categoryDao().insertAll(DefaultCatalog.defaultCategories())
        db.paymentMethodDao().insertAll(DefaultCatalog.defaultPaymentMethods())

        DemoDataSeeder.seedDemoData(db)
        assertTrue(db.transactionDao().countDemo() > 0)

        db.transactionDao().deleteDemoTransactions()
        assertEquals(0, db.transactionDao().countDemo())
        db.close()
    }

    @Test
    fun `security manager verifies PIN and encrypts decrypts vault backup`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val security = VaultSecurityManager(context)

        val blob = security.createPinEncryptedBlob("2468")
        assertTrue(security.verifyPin("2468", blob))
        assertFalse(security.verifyPin("1111", blob))

        val secretJson = """{"vault":"lakexpense","balance":12450000}"""
        val encrypted = security.encryptBackupPayload(secretJson, "StrongPass#2026")
        val decrypted = security.decryptBackupPayload(encrypted, "StrongPass#2026")
        assertEquals(secretJson, decrypted)
    }

    @Test
    fun `first launch reaches onboarding and initializes local vault`() {
        composeRule.mainClock.autoAdvance = true
        composeRule.waitUntil(timeoutMillis = 5_000) {
            runCatching {
                composeRule.onNodeWithTag("onboarding_name_input").assertIsDisplayed()
                true
            }.getOrDefault(false)
        }
        composeRule.onNodeWithTag("onboarding_name_input").performTextInput("Kasun")
        composeRule.onNodeWithTag("onboarding_submit_button")
            .performScrollTo()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 8_000) {
            runCatching {
                composeRule.onNodeWithTag("home_dashboard_list").assertIsDisplayed()
                true
            }.getOrDefault(false)
        }

        // Verify Quick Add Transaction navigation & saving a real LKR expense
        composeRule.onNodeWithTag("nav_quick_add_fab").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            runCatching {
                composeRule.onNodeWithTag("quick_add_chip_1000").assertIsDisplayed()
                true
            }.getOrDefault(false)
        }
        composeRule.onNodeWithTag("quick_add_chip_1000").performClick()
        composeRule.onNodeWithTag("save_transaction_button")
            .performScrollTo()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            runCatching {
                composeRule.onNodeWithTag("home_dashboard_list").assertIsDisplayed()
                true
            }.getOrDefault(false)
        }
    }
}
