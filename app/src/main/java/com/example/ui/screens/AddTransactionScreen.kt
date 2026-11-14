package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.CategoryEntity
import com.example.data.local.PaymentMethodEntity
import com.example.data.local.TransactionEntity
import com.example.data.model.AppSettings
import com.example.data.model.TransactionType
import com.example.ui.components.CategoryIconMapper
import com.example.ui.components.LakTopHeader
import com.example.ui.theme.LakTheme
import com.example.util.DateUtils
import com.example.util.MoneyMath
import java.io.File
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.Calendar

private data class SriLankaPreset(
    val title: String,
    val subtitle: String,
    val categoryName: String,
    val type: TransactionType,
    val amountCents: Long,
    val merchant: String
)

private val sriLankanPresets = listOf(
    SriLankaPreset("Keells Super", "Weekly groceries", "Groceries", TransactionType.EXPENSE, 425_000L, "Keells Super"),
    SriLankaPreset("CEB Electricity", "Monthly utility bill", "CEB / Water", TransactionType.EXPENSE, 645_000L, "CEB Electricity Bill"),
    SriLankaPreset("Dialog Axiata", "Mobile & Fibre bill", "CEB / Water", TransactionType.EXPENSE, 289_000L, "Dialog Axiata PLC"),
    SriLankaPreset("PickMe Ride", "Daily commute", "Fuel & Ride", TransactionType.EXPENSE, 85_000L, "PickMe Ride"),
    SriLankaPreset("Ceypetco Fuel", "92 Octane refuel", "Fuel & Ride", TransactionType.EXPENSE, 500_000L, "Ceypetco Fuel Station"),
    SriLankaPreset("Monthly Salary", "Primary payroll", "Salary", TransactionType.INCOME, 18_500_000L, "Monthly Salary Credit")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    existingTransaction: TransactionEntity?,
    initialType: TransactionType,
    categories: List<CategoryEntity>,
    paymentMethods: List<PaymentMethodEntity>,
    settings: AppSettings,
    onClose: () -> Unit,
    onNavigateProfile: () -> Unit,
    onCopyReceiptUri: (android.net.Uri, (String?) -> Unit) -> Unit,
    onSaveCustomCategory: (name: String, subtitle: String, iconKey: String, colorHex: String, type: TransactionType) -> Unit,
    onSavePaymentMethod: (name: String) -> Unit,
    onSaveTransaction: (
        txId: Long,
        type: TransactionType,
        amountCents: Long,
        categoryId: Long,
        paymentMethodId: Long,
        toPaymentMethodId: Long?,
        merchant: String,
        note: String,
        receiptPath: String?,
        timestamp: Long
    ) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onClose() }

    val colors = LakTheme.colors
    val haptics = LocalHapticFeedback.current

    var selectedType by rememberSaveable { mutableStateOf(existingTransaction?.type ?: initialType) }
    var rawAmountInput by rememberSaveable {
        mutableStateOf(
            if (existingTransaction != null) {
                val bd = BigDecimal.valueOf(existingTransaction.amountCents).divide(BigDecimal(100))
                if (existingTransaction.amountCents % 100L == 0L) {
                    bd.toBigInteger().toString()
                } else {
                    bd.stripTrailingZeros().toPlainString()
                }
            } else {
                ""
            }
        )
    }
    var selectedCategoryId by rememberSaveable {
        mutableLongStateOf(existingTransaction?.categoryId ?: 0L)
    }
    var selectedPaymentMethodId by rememberSaveable {
        mutableLongStateOf(existingTransaction?.paymentMethodId ?: 0L)
    }
    var selectedToPaymentMethodId by rememberSaveable {
        mutableLongStateOf(existingTransaction?.toPaymentMethodId ?: 0L)
    }
    var timestampMillis by rememberSaveable {
        mutableLongStateOf(existingTransaction?.timestamp ?: System.currentTimeMillis())
    }
    var merchant by rememberSaveable { mutableStateOf(existingTransaction?.merchant ?: "") }
    var note by rememberSaveable { mutableStateOf(existingTransaction?.note ?: "") }
    var receiptPath by rememberSaveable { mutableStateOf(existingTransaction?.receiptPath) }

    var showKeypad by rememberSaveable { mutableStateOf(false) }
    var showPresetsSheet by remember { mutableStateOf(false) }
    var showMoreCategoriesSheet by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddPaymentDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val filteredCategories = remember(categories, selectedType) {
        categories.filter { it.type == selectedType }
    }

    LaunchedEffect(filteredCategories, selectedType) {
        if (filteredCategories.isNotEmpty() && filteredCategories.none { it.id == selectedCategoryId }) {
            selectedCategoryId = filteredCategories.first().id
        }
    }

    LaunchedEffect(paymentMethods) {
        if (paymentMethods.isNotEmpty()) {
            if (paymentMethods.none { it.id == selectedPaymentMethodId }) {
                selectedPaymentMethodId = paymentMethods.first().id
            }
            if (selectedToPaymentMethodId == 0L || paymentMethods.none { it.id == selectedToPaymentMethodId }) {
                selectedToPaymentMethodId = paymentMethods.getOrNull(1)?.id ?: paymentMethods.first().id
            }
        }
    }

    val receiptPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onCopyReceiptUri(uri) { path ->
                if (path != null) receiptPath = path
            }
        }
    }

    fun triggerHaptic() {
        if (settings.hapticsEnabled) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    fun addQuickIncrement(deltaRupees: Long) {
        triggerHaptic()
        validationError = null
        val currentCents = MoneyMath.parseToCents(rawAmountInput) ?: 0L
        val newCents = currentCents + (deltaRupees * 100L)
        rawAmountInput = if (newCents % 100L == 0L) {
            (newCents / 100L).toString()
        } else {
            BigDecimal.valueOf(newCents).divide(BigDecimal(100)).toPlainString()
        }
    }

    fun appendKeypadChar(ch: String) {
        triggerHaptic()
        validationError = null
        if (ch == ".") {
            if (!rawAmountInput.contains(".")) {
                rawAmountInput = if (rawAmountInput.isEmpty()) "0." else "$rawAmountInput."
            }
            return
        }
        if (rawAmountInput == "0") {
            rawAmountInput = ch
            return
        }
        val dotIndex = rawAmountInput.indexOf('.')
        if (dotIndex >= 0 && rawAmountInput.length - dotIndex > 2) return
        if (rawAmountInput.length >= 11) return
        rawAmountInput += ch
    }

    val formattedAmountPreview = remember(rawAmountInput) {
        if (rawAmountInput.isBlank()) {
            "0"
        } else {
            val parts = rawAmountInput.split('.')
            val intPart = parts.getOrNull(0)?.toLongOrNull() ?: 0L
            val grouped = DecimalFormat("#,##0").format(intPart)
            if (parts.size > 1) "$grouped.${parts[1]}" else grouped
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        LakTopHeader(
            subtitle = if (existingTransaction == null) "Quick Add Transaction" else "Edit Transaction",
            showBackButton = true,
            onBackClick = onClose,
            onProfileClick = onNavigateProfile
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sub-header row matching Stitch: Close X | Quick Add + Encrypted badge | Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            onClick = onClose,
                            shape = CircleShape,
                            color = colors.surfaceContainerHigh,
                            border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("quick_add_close_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Close quick add",
                                    tint = colors.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = if (existingTransaction == null) "Quick Add" else "Update Entry",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = colors.onSurface
                            )
                            Surface(
                                shape = CircleShape,
                                color = colors.primary.copy(alpha = 0.14f),
                                border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.35f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Lock,
                                        contentDescription = null,
                                        tint = colors.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "Offline Vault Encrypted",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.primary
                                    )
                                }
                            }
                        }
                    }

                    Surface(
                        onClick = { showPresetsSheet = true },
                        shape = CircleShape,
                        color = colors.surfaceContainerHigh,
                        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.testTag("quick_add_presets_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Bolt,
                                contentDescription = "Sri Lankan presets",
                                tint = colors.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Presets",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.primary
                            )
                        }
                    }
                }

                // Segmented Type Toggle: Expense / Income / Transfer
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surfaceContainerLow,
                    border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TransactionType.entries.forEach { type ->
                            val isSelected = selectedType == type
                            val pillColor = when {
                                !isSelected -> Color.Transparent
                                type == TransactionType.EXPENSE -> Color(0xFFDC2626)
                                type == TransactionType.INCOME -> colors.primaryContainer
                                else -> colors.transfer
                            }
                            val textColor = when {
                                !isSelected -> colors.onSurfaceVariant
                                type == TransactionType.EXPENSE -> Color.White
                                type == TransactionType.INCOME -> colors.onPrimary
                                else -> Color.White
                            }
                            val icon = when (type) {
                                TransactionType.EXPENSE -> Icons.Outlined.ArrowDownward
                                TransactionType.INCOME -> Icons.Outlined.ArrowUpward
                                TransactionType.TRANSFER -> Icons.Outlined.SwapHoriz
                            }
                            Surface(
                                onClick = {
                                    triggerHaptic()
                                    selectedType = type
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = pillColor,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("tx_type_${type.name.lowercase()}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = type.label,
                                        tint = textColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = type.label,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = textColor,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                // Hero Transaction Amount Card
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = colors.surfaceContainer,
                    border = BorderStroke(1.dp, colors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "TRANSACTION AMOUNT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.2.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = colors.onSurfaceVariant
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showKeypad = !showKeypad }
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = settings.currencyOption.symbol,
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(contentAlignment = Alignment.CenterStart) {
                                Text(
                                    text = formattedAmountPreview,
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        fontSize = 38.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (rawAmountInput.isEmpty()) colors.onSurfaceVariant.copy(alpha = 0.4f) else colors.onSurface
                                )
                                BasicTextField(
                                    value = rawAmountInput,
                                    onValueChange = { input ->
                                        validationError = null
                                        val clean = input.filter { it.isDigit() || it == '.' }
                                        val dotCount = clean.count { it == '.' }
                                        if (dotCount <= 1 && clean.length <= 12) {
                                            rawAmountInput = clean
                                        }
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    cursorBrush = SolidColor(colors.primary),
                                    textStyle = MaterialTheme.typography.displayLarge.copy(
                                        fontSize = 38.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .widthIn(min = 60.dp, max = 260.dp)
                                        .testTag("amount_input_field")
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(34.dp)
                                    .background(colors.primary)
                            )
                        }

                        // Quick Increment Chips: +500, +1,000, +5,000, Clear
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(500L to "+500", 1_000L to "+1,000", 5_000L to "+5,000").forEach { (amt, label) ->
                                Surface(
                                    onClick = { addQuickIncrement(amt) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = colors.surfaceContainerHigh,
                                    border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.35f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .testTag("quick_add_chip_$amt")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = colors.onSurface
                                        )
                                    }
                                }
                            }

                            Surface(
                                onClick = {
                                    triggerHaptic()
                                    rawAmountInput = ""
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = colors.expenseContainer.copy(alpha = if (colors.isDark) 0.35f else 0.9f),
                                border = BorderStroke(1.dp, colors.expense.copy(alpha = 0.45f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("amount_clear_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.Backspace,
                                        contentDescription = "Clear amount",
                                        tint = colors.expense,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Clear",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = colors.expense
                                    )
                                }
                            }
                        }

                        // Optional expandable Numeric Keypad
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            TextButton(
                                onClick = { showKeypad = !showKeypad },
                                modifier = Modifier.testTag("toggle_keypad_button")
                            ) {
                                Text(
                                    text = if (showKeypad) "Hide Numeric Keypad" else "Open Numeric Keypad",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.primary
                                )
                            }
                        }

                        if (showKeypad) {
                            NumericKeypadGrid(
                                onDigit = { appendKeypadChar(it) },
                                onBackspace = {
                                    triggerHaptic()
                                    if (rawAmountInput.isNotEmpty()) {
                                        rawAmountInput = rawAmountInput.dropLast(1)
                                    }
                                }
                            )
                        }
                    }
                }

                // Transfer Accounts OR Category Grid
                if (selectedType == TransactionType.TRANSFER) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = colors.surfaceContainer,
                        border = BorderStroke(1.dp, colors.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "From Account",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.onSurface
                            )
                            PaymentMethodChipRow(
                                methods = paymentMethods,
                                selectedId = selectedPaymentMethodId,
                                onSelect = { selectedPaymentMethodId = it },
                                onAddNew = { showAddPaymentDialog = true }
                            )
                            Text(
                                text = "To Account",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.onSurface
                            )
                            PaymentMethodChipRow(
                                methods = paymentMethods,
                                selectedId = selectedToPaymentMethodId,
                                onSelect = { selectedToPaymentMethodId = it },
                                onAddNew = { showAddPaymentDialog = true }
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Category",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = colors.onSurface
                            )
                            Text(
                                text = "Tap to switch",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.primary,
                                modifier = Modifier.clickable { showMoreCategoriesSheet = true }
                            )
                        }

                        val displayCats = filteredCategories.take(7)
                        val gridItems: List<CategoryEntity?> = displayCats + listOf(null)
                        gridItems.chunked(4).forEach { rowCats ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowCats.forEach { cat ->
                                    if (cat == null) {
                                        Surface(
                                            onClick = { showMoreCategoriesSheet = true },
                                            shape = RoundedCornerShape(14.dp),
                                            color = colors.surfaceContainer,
                                            border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.4f)),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(92.dp)
                                                .testTag("category_more_button")
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(colors.surfaceContainerHigh),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.MoreHoriz,
                                                        contentDescription = "More categories",
                                                        tint = colors.onSurfaceVariant
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = "More +",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = colors.onSurface,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    } else {
                                        val isSelected = cat.id == selectedCategoryId
                                        val catColor = CategoryIconMapper.parseColor(cat.colorHex, colors.primary)
                                        Surface(
                                            onClick = {
                                                triggerHaptic()
                                                selectedCategoryId = cat.id
                                            },
                                            shape = RoundedCornerShape(14.dp),
                                            color = colors.surfaceContainer,
                                            border = BorderStroke(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) colors.primary else colors.outlineVariant.copy(alpha = 0.35f)
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(92.dp)
                                                .testTag("category_tile_${cat.id}")
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(
                                                            if (isSelected) colors.primaryContainer else colors.surfaceContainerHigh
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = CategoryIconMapper.iconForKey(cat.iconKey),
                                                        contentDescription = cat.name,
                                                        tint = if (isSelected) colors.onPrimary else catColor,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = cat.name,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                    ),
                                                    color = if (isSelected) colors.primary else colors.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }
                                repeat(4 - rowCats.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // Date & Time Selector Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surfaceContainer,
                    border = BorderStroke(1.dp, colors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val (relPrefix, dateStr) = DateUtils.formatRelativeDayHeader(timestampMillis)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.primary.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarMonth,
                                    contentDescription = "Date and time",
                                    tint = colors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "$relPrefix, $dateStr",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.onSurface
                                )
                                Text(
                                    text = "Recorded at ${DateUtils.formatTime(timestampMillis)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            onClick = { showDatePicker = true },
                            shape = RoundedCornerShape(10.dp),
                            color = colors.surfaceContainerHigh,
                            border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.testTag("change_datetime_button")
                        ) {
                            Text(
                                text = "Change",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                // Payment Method Section (for Expense / Income)
                if (selectedType != TransactionType.TRANSFER) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Payment Method",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = colors.onSurface
                        )
                        PaymentMethodChipRow(
                            methods = paymentMethods,
                            selectedId = selectedPaymentMethodId,
                            onSelect = {
                                triggerHaptic()
                                selectedPaymentMethodId = it
                            },
                            onAddNew = { showAddPaymentDialog = true }
                        )
                    }
                }

                // Merchant / Payee Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surfaceContainer,
                    border = BorderStroke(1.dp, colors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Merchant / Payee",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant
                        )
                        BasicTextField(
                            value = merchant,
                            onValueChange = { merchant = it },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
                            cursorBrush = SolidColor(colors.primary),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                            decorationBox = { inner ->
                                Box {
                                    if (merchant.isEmpty()) {
                                        Text(
                                            text = "e.g. Keells Super, CEB, PickMe, Salary",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = colors.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                    inner()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("merchant_input")
                        )
                    }
                }

                // Note / Description Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surfaceContainer,
                    border = BorderStroke(1.dp, colors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Note / Description",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant
                        )
                        BasicTextField(
                            value = note,
                            onValueChange = { note = it },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.onSurface),
                            cursorBrush = SolidColor(colors.primary),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                            decorationBox = { inner ->
                                Box {
                                    if (note.isEmpty()) {
                                        Text(
                                            text = "Optional details (e.g. Dinner with family in Colombo)",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = colors.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                    inner()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("note_input")
                        )
                    }
                }

                // Attach Bill / Slip Card (Zero-permission Android Photo Picker)
                Surface(
                    onClick = {
                        receiptPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surfaceContainer,
                    border = BorderStroke(1.dp, colors.cardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("attach_receipt_button")
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.primary.copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ReceiptLong,
                                        contentDescription = "Attach bill or slip",
                                        tint = colors.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = if (receiptPath == null) "Attach Bill / Slip" else "Receipt Slip Attached",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                        color = colors.onSurface
                                    )
                                    Text(
                                        text = "Stored on device locally",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.onSurfaceVariant
                                    )
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (receiptPath != null) {
                                    IconButton(
                                        onClick = { receiptPath = null },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.DeleteOutline,
                                            contentDescription = "Remove receipt",
                                            tint = colors.expense
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Outlined.PhotoCamera,
                                    contentDescription = "Select photo",
                                    tint = colors.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Icon(
                                    imageVector = Icons.Outlined.ChevronRight,
                                    contentDescription = null,
                                    tint = colors.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        if (!receiptPath.isNullOrBlank()) {
                            AsyncImage(
                                model = File(receiptPath!!),
                                contentDescription = "Attached bill slip preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, colors.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            )
                        }
                    }
                }

                if (validationError != null) {
                    Text(
                        text = validationError!!,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = colors.expense
                    )
                }

                // Primary Save CTA
                Button(
                    onClick = {
                        val cents = MoneyMath.parseToCents(rawAmountInput)
                        if (cents == null || cents <= 0L) {
                            validationError = "Please enter an amount greater than Rs. 0"
                            return@Button
                        }
                        if (selectedType == TransactionType.TRANSFER &&
                            selectedPaymentMethodId == selectedToPaymentMethodId
                        ) {
                            validationError = "Choose two different accounts for a transfer"
                            return@Button
                        }
                        val catId = if (selectedCategoryId != 0L) {
                            selectedCategoryId
                        } else {
                            filteredCategories.firstOrNull()?.id ?: categories.firstOrNull()?.id ?: 1L
                        }
                        val payId = if (selectedPaymentMethodId != 0L) {
                            selectedPaymentMethodId
                        } else {
                            paymentMethods.firstOrNull()?.id ?: 1L
                        }
                        onSaveTransaction(
                            existingTransaction?.id ?: 0L,
                            selectedType,
                            cents,
                            catId,
                            payId,
                            if (selectedType == TransactionType.TRANSFER) selectedToPaymentMethodId else null,
                            merchant,
                            note,
                            receiptPath,
                            timestampMillis
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primaryContainer,
                        contentColor = colors.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("save_transaction_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (existingTransaction == null) "Save Transaction" else "Update Transaction",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                if (existingTransaction != null) {
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, colors.expense.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("delete_transaction_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = null,
                            tint = colors.expense
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Transaction", color = colors.expense)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Presets Sheet
    if (showPresetsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPresetsSheet = false },
            containerColor = colors.surfaceContainerLow
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Sri Lankan Quick Presets",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.onSurface
                )
                Text(
                    text = "Tap any preset to auto-fill common local transactions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
                sriLankanPresets.forEach { preset ->
                    Surface(
                        onClick = {
                            selectedType = preset.type
                            rawAmountInput = (preset.amountCents / 100L).toString()
                            merchant = preset.merchant
                            categories.firstOrNull {
                                it.type == preset.type && it.name == preset.categoryName
                            }?.let { selectedCategoryId = it.id }
                            showPresetsSheet = false
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = colors.surfaceContainer,
                        border = BorderStroke(1.dp, colors.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = preset.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = colors.onSurface
                                )
                                Text(
                                    text = "${preset.categoryName} • ${preset.subtitle}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                            Text(
                                text = MoneyMath.formatAmount(preset.amountCents, settings.currencyOption, includeDecimals = false),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (preset.type == TransactionType.INCOME) colors.income else colors.primary
                            )
                        }
                    }
                }
            }
        }
    }

    // Full Category Picker & Custom Category Sheet
    if (showMoreCategoriesSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoreCategoriesSheet = false },
            containerColor = colors.surfaceContainerLow
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedType.label} Categories",
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.onSurface
                    )
                    TextButton(onClick = {
                        showMoreCategoriesSheet = false
                        showAddCategoryDialog = true
                    }) {
                        Icon(Icons.Outlined.Add, contentDescription = null, tint = colors.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Create New", color = colors.primary)
                    }
                }

                filteredCategories.forEach { cat ->
                    val isSel = cat.id == selectedCategoryId
                    val catColor = CategoryIconMapper.parseColor(cat.colorHex, colors.primary)
                    Surface(
                        onClick = {
                            selectedCategoryId = cat.id
                            showMoreCategoriesSheet = false
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSel) colors.primary.copy(alpha = 0.14f) else colors.surfaceContainer,
                        border = BorderStroke(1.dp, if (isSel) colors.primary else colors.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.surfaceContainerHigh),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = CategoryIconMapper.iconForKey(cat.iconKey),
                                    contentDescription = cat.name,
                                    tint = catColor
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(cat.name, style = MaterialTheme.typography.labelLarge, color = colors.onSurface)
                                Text(cat.subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                            }
                            if (cat.isCustom) {
                                Text("Custom", style = MaterialTheme.typography.labelSmall, color = colors.primary)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddCategoryDialog) {
        CreateCategoryDialog(
            type = selectedType,
            onDismiss = { showAddCategoryDialog = false },
            onCreate = { name, subtitle, iconKey, colorHex ->
                onSaveCustomCategory(name, subtitle, iconKey, colorHex, selectedType)
                showAddCategoryDialog = false
            }
        )
    }

    if (showAddPaymentDialog) {
        var methodName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddPaymentDialog = false },
            containerColor = colors.surfaceContainer,
            title = { Text("Add Payment Method", color = colors.onSurface) },
            text = {
                OutlinedTextField(
                    value = methodName,
                    onValueChange = { methodName = it },
                    label = { Text("Account / Wallet Name") },
                    placeholder = { Text("e.g. HNB Savings, LankaQR") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (methodName.isNotBlank()) {
                        onSavePaymentMethod(methodName)
                        showAddPaymentDialog = false
                    }
                }) {
                    Text("Add", color = colors.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPaymentDialog = false }) {
                    Text("Cancel", color = colors.onSurfaceVariant)
                }
            }
        )
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = timestampMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { pickedDate ->
                        val oldCal = Calendar.getInstance().apply { timeInMillis = timestampMillis }
                        val newCal = Calendar.getInstance().apply {
                            timeInMillis = pickedDate
                            set(Calendar.HOUR_OF_DAY, oldCal.get(Calendar.HOUR_OF_DAY))
                            set(Calendar.MINUTE, oldCal.get(Calendar.MINUTE))
                        }
                        timestampMillis = newCal.timeInMillis
                    }
                    showDatePicker = false
                    showTimePicker = true
                }) {
                    Text("Next: Time", color = colors.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = colors.onSurfaceVariant)
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (showTimePicker) {
        val cal = Calendar.getInstance().apply { timeInMillis = timestampMillis }
        val timeState = rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE)
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            containerColor = colors.surfaceContainer,
            title = { Text("Select Time", color = colors.onSurface) },
            text = { TimePicker(state = timeState) },
            confirmButton = {
                TextButton(onClick = {
                    val updated = Calendar.getInstance().apply {
                        timeInMillis = timestampMillis
                        set(Calendar.HOUR_OF_DAY, timeState.hour)
                        set(Calendar.MINUTE, timeState.minute)
                    }
                    timestampMillis = updated.timeInMillis
                    showTimePicker = false
                }) {
                    Text("Apply", color = colors.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel", color = colors.onSurfaceVariant)
                }
            }
        )
    }

    if (showDeleteConfirm && existingTransaction != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = colors.surfaceContainer,
            title = { Text("Delete Transaction?", color = colors.onSurface) },
            text = {
                Text(
                    "This entry will be permanently deleted from your local vault and dashboard balances will recalculate immediately.",
                    color = colors.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDeleteTransaction(existingTransaction.id)
                }) {
                    Text("Delete", color = colors.expense, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = colors.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
private fun PaymentMethodChipRow(
    methods: List<PaymentMethodEntity>,
    selectedId: Long,
    onSelect: (Long) -> Unit,
    onAddNew: () -> Unit
) {
    val colors = LakTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        methods.forEach { method ->
            val selected = method.id == selectedId
            Surface(
                onClick = { onSelect(method.id) },
                shape = CircleShape,
                color = if (selected) colors.primaryContainer else colors.surfaceContainer,
                border = BorderStroke(
                    1.dp,
                    if (selected) colors.primary else colors.outlineVariant.copy(alpha = 0.45f)
                ),
                modifier = Modifier
                    .height(42.dp)
                    .testTag("payment_chip_${method.id}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = CategoryIconMapper.iconForKey(method.iconKey),
                        contentDescription = method.name,
                        tint = if (selected) colors.onPrimary else colors.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = method.name,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (selected) colors.onPrimary else colors.onSurface
                    )
                }
            }
        }
        Surface(
            onClick = onAddNew,
            shape = CircleShape,
            color = colors.surfaceContainerHigh,
            border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.4f)),
            modifier = Modifier
                .height(42.dp)
                .testTag("add_payment_method_chip")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Add method", tint = colors.primary, modifier = Modifier.size(16.dp))
                Text("Add", style = MaterialTheme.typography.labelMedium, color = colors.primary)
            }
        }
    }
}

@Composable
private fun NumericKeypadGrid(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit
) {
    val colors = LakTheme.colors
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "DEL")
    )
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { key ->
                    Surface(
                        onClick = {
                            if (key == "DEL") onBackspace() else onDigit(key)
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = colors.surfaceContainerHigh,
                        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("keypad_$key")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (key == "DEL") {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Backspace,
                                    contentDescription = "Backspace",
                                    tint = colors.onSurface
                                )
                            } else {
                                Text(
                                    text = key,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateCategoryDialog(
    type: TransactionType,
    onDismiss: () -> Unit,
    onCreate: (name: String, subtitle: String, iconKey: String, colorHex: String) -> Unit
) {
    val colors = LakTheme.colors
    var name by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("category") }
    val palette = listOf("#10B981", "#4EDEA3", "#38BDF8", "#F59E0B", "#F87171", "#A78BFA")
    var selectedColor by remember { mutableStateOf(palette.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceContainer,
        title = { Text("New ${type.label} Category", color = colors.onSurface) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    placeholder = { Text("e.g. Tuition, Pet Care") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("Subtitle (Optional)") },
                    placeholder = { Text("e.g. School & Books") },
                    singleLine = true
                )
                Text("Icon", style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryIconMapper.availableIcons.take(12).forEach { (key, icon) ->
                        val active = key == selectedIcon
                        Surface(
                            onClick = { selectedIcon = key },
                            shape = RoundedCornerShape(10.dp),
                            color = if (active) colors.primaryContainer else colors.surfaceContainerHigh,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = key,
                                    tint = if (active) colors.onPrimary else colors.onSurface
                                )
                            }
                        }
                    }
                }
                Text("Accent Color", style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    palette.forEach { hex ->
                        val c = CategoryIconMapper.parseColor(hex, colors.primary)
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(
                                    width = if (selectedColor == hex) 2.5.dp else 0.dp,
                                    color = colors.onSurface,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) onCreate(name, subtitle, selectedIcon, selectedColor)
            }) {
                Text("Create", color = colors.primary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.onSurfaceVariant)
            }
        }
    )
}
