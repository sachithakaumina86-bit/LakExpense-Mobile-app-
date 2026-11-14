package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.components.LakLogoMark
import com.example.ui.theme.LakTheme
import com.example.util.MoneyMath

@Composable
fun OnboardingScreen(
    onCompleteOnboarding: (
        fullName: String,
        email: String,
        location: String,
        openingBalanceCents: Long,
        includeSampleData: Boolean
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LakTheme.colors
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("Colombo, Sri Lanka") }
    var openingBalanceText by rememberSaveable { mutableStateOf("0.00") }
    var includeDemoData by rememberSaveable { mutableStateOf(false) }
    var nameError by rememberSaveable { mutableStateOf<String?>(null) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colors.primary,
        unfocusedBorderColor = colors.outlineVariant,
        focusedTextColor = colors.onSurface,
        unfocusedTextColor = colors.onSurface,
        focusedLabelColor = colors.primary,
        unfocusedLabelColor = colors.onSurfaceVariant,
        cursorColor = colors.primary
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = colors.surfaceContainerLow,
                    border = BorderStroke(1.dp, colors.cardBorder)
                ) {
                    Box(
                        modifier = Modifier.padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LakLogoMark(modifier = Modifier.size(44.dp))
                    }
                }
                Column {
                    Text(
                        text = "LakExpense",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.onSurface
                    )
                    Text(
                        text = "Every Rupee. Under Control.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.primary
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = colors.surfaceContainerLow,
                border = BorderStroke(1.dp, colors.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = "100% Offline Privacy",
                        tint = colors.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = "Private Sri Lankan LKR Vault",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.onSurface
                        )
                        Text(
                            text = "Zero cloud servers or accounts required. Everything stays locally in your device SQLite ledger.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }

            Text(
                text = "Set Up Your Local Profile",
                style = MaterialTheme.typography.headlineSmall,
                color = colors.onSurface
            )

            OutlinedTextField(
                value = fullName,
                onValueChange = {
                    fullName = it
                    nameError = null
                },
                label = { Text("Your Name") },
                placeholder = { Text("e.g. Kasun Kalhara") },
                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = colors.primary) },
                isError = nameError != null,
                supportingText = nameError?.let { msg -> { Text(msg, color = colors.expense) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                colors = fieldColors,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_name_input")
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email (Optional)") },
                placeholder = { Text("name@email.lk") },
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null, tint = colors.onSurfaceVariant) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = fieldColors,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_email_input")
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("City / Region") },
                placeholder = { Text("Colombo, Sri Lanka") },
                leadingIcon = { Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = colors.onSurfaceVariant) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                colors = fieldColors,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_location_input")
            )

            OutlinedTextField(
                value = openingBalanceText,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() || it == '.' }
                    openingBalanceText = filtered
                },
                label = { Text("Opening Balance (Rs.)") },
                placeholder = { Text("0.00") },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.AccountBalanceWallet,
                        contentDescription = null,
                        tint = colors.primary
                    )
                },
                prefix = { Text("Rs. ", color = colors.primary, fontWeight = FontWeight.Bold) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = fieldColors,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_balance_input")
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = colors.surfaceContainer,
                border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = "Explore with Sample Sri Lankan Data",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Optional Keells, CEB & PickMe sample logs & budgets. Can be cleared anytime with one tap.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = includeDemoData,
                        onCheckedChange = { includeDemoData = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.onPrimary,
                            checkedTrackColor = colors.primary
                        ),
                        modifier = Modifier.testTag("onboarding_demo_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val trimmed = fullName.trim()
                    if (trimmed.isEmpty()) {
                        nameError = "Please enter your name to personalize your vault"
                        return@Button
                    }
                    val openingCents = MoneyMath.parseToCents(openingBalanceText) ?: 0L
                    onCompleteOnboarding(trimmed, email.trim(), location.trim(), openingCents, includeDemoData)
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primaryContainer,
                    contentColor = colors.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("onboarding_submit_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Initialize Local Vault",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
