package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.CategoryEntity
import com.example.data.local.PaymentMethodEntity
import com.example.data.model.AppSettings
import com.example.data.model.TransactionFilterState
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionType
import com.example.ui.components.CategoryIconMapper
import com.example.ui.theme.LakTheme
import com.example.util.DateUtils
import com.example.util.MoneyMath
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    items: List<TransactionItem>,
    filterState: TransactionFilterState,
    selectedYearMonth: String,
    categories: List<CategoryEntity>,
    paymentMethods: List<PaymentMethodEntity>,
    settings: AppSettings,
    onUpdateFilter: ((TransactionFilterState) -> TransactionFilterState) -> Unit,
    onResetFilters: () -> Unit,
    onEditTransaction: (TransactionItem) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    onAddTransactionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LakTheme.colors
    var showFilterSheet by remember { mutableStateOf(false) }
    var showDateRangePicker by remember { mutableStateOf(false) }
    var detailItem by remember { mutableStateOf<TransactionItem?>(null) }
    var deleteConfirmId by remember { mutableStateOf<Long?>(null) }

    val totalSpent = remember(items) {
        items.filter { it.entity.type == TransactionType.EXPENSE }.sumOf { it.entity.amountCents }
    }
    val totalEarned = remember(items) {
        items.filter { it.entity.type == TransactionType.INCOME }.sumOf { it.entity.amountCents }
    }
    val netCash = totalEarned - totalSpent

    val groupedByDay = remember(items) {
        items.groupBy { DateUtils.startOfDay(it.entity.timestamp) }
            .entries
            .sortedByDescending { it.key }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .testTag("transactions_list"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 108.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Bar + Filter Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = colors.surfaceContainer,
                        border = BorderStroke(1.dp, colors.cardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search transactions",
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            BasicTextField(
                                value = filterState.query,
                                onValueChange = { q -> onUpdateFilter { it.copy(query = q) } },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.onSurface),
                                cursorBrush = SolidColor(colors.primary),
                                decorationBox = { inner ->
                                    Box {
                                        if (filterState.query.isEmpty()) {
                                            Text(
                                                text = "Search payee, note, or category",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = colors.onSurfaceVariant
                                            )
                                        }
                                        inner()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("transactions_search_input")
                            )
                            if (filterState.query.isNotEmpty()) {
                                IconButton(
                                    onClick = { onUpdateFilter { it.copy(query = "") } },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Clear search",
                                        tint = colors.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    val activeFilterCount = filterState.selectedCategoryIds.size +
                        filterState.selectedPaymentMethodIds.size +
                        if (filterState.customStartMillis != null) 1 else 0
                    Surface(
                        onClick = { showFilterSheet = true },
                        shape = RoundedCornerShape(14.dp),
                        color = if (activeFilterCount > 0) colors.primaryContainer else colors.surfaceContainer,
                        border = BorderStroke(1.dp, colors.cardBorder),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("transactions_filter_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Tune,
                                contentDescription = "Filter by category, method, or date",
                                tint = if (activeFilterCount > 0) colors.onPrimary else colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Horizontal Quick Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterPill(
                        label = "All Types",
                        selected = filterState.typeFilter == null,
                        dotColor = null,
                        showCheck = filterState.typeFilter == null,
                        testTag = "filter_chip_all",
                        onClick = { onUpdateFilter { it.copy(typeFilter = null) } }
                    )
                    FilterPill(
                        label = "Expense Only",
                        selected = filterState.typeFilter == TransactionType.EXPENSE,
                        dotColor = colors.expense,
                        showCheck = false,
                        testTag = "filter_chip_expense",
                        onClick = { onUpdateFilter { it.copy(typeFilter = TransactionType.EXPENSE) } }
                    )
                    FilterPill(
                        label = "Income Only",
                        selected = filterState.typeFilter == TransactionType.INCOME,
                        dotColor = colors.income,
                        showCheck = false,
                        testTag = "filter_chip_income",
                        onClick = { onUpdateFilter { it.copy(typeFilter = TransactionType.INCOME) } }
                    )
                    FilterPill(
                        label = "Transfer",
                        selected = filterState.typeFilter == TransactionType.TRANSFER,
                        dotColor = colors.transfer,
                        showCheck = false,
                        testTag = "filter_chip_transfer",
                        onClick = { onUpdateFilter { it.copy(typeFilter = TransactionType.TRANSFER) } }
                    )
                    FilterPill(
                        label = if (filterState.customStartMillis != null) "Custom Dates" else if (filterState.useSelectedMonth) DateUtils.formatShortMonth(selectedYearMonth) else "All Dates",
                        selected = !filterState.useSelectedMonth || filterState.customStartMillis != null,
                        dotColor = colors.primary,
                        showCheck = false,
                        testTag = "filter_chip_dates",
                        onClick = { showDateRangePicker = true }
                    )
                }
            }

            // Summary Card matching Stitch OCTOBER NET OUTFLOW card
            item {
                val monthHeader = if (filterState.customStartMillis != null && filterState.customEndMillis != null) {
                    DateUtils.formatRangeLabel(filterState.customStartMillis, filterState.customEndMillis).uppercase()
                } else if (filterState.useSelectedMonth) {
                    DateUtils.formatMonthYear(selectedYearMonth).substringBefore(" ").uppercase()
                } else {
                    "ALL-TIME"
                }
                val isNetNegative = netCash < 0L
                val totalFlow = (totalSpent + totalEarned).coerceAtLeast(1L)
                val expenseShare = (totalSpent.toFloat() / totalFlow.toFloat()).coerceIn(0f, 1f)

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = colors.surfaceContainer,
                    border = BorderStroke(1.dp, colors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
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
                                        imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                                        contentDescription = "Net activity",
                                        tint = colors.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = if (isNetNegative) "$monthHeader NET OUTFLOW" else "$monthHeader NET SURPLUS",
                                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                                        color = colors.onSurfaceVariant
                                    )
                                    Text(
                                        text = if (settings.balanceHidden) "••••••••" else MoneyMath.formatAmount(
                                            netCash,
                                            settings.currencyOption,
                                            showSign = true
                                        ),
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isNetNegative) colors.expense else colors.income
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Activity",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.onSurfaceVariant
                                )
                                Text(
                                    text = "${items.size} logs",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = colors.onSurface
                                )
                            }
                        }

                        // Proportional Outflow vs Inflow bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(colors.income)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(if (totalSpent == 0L && totalEarned == 0L) 0f else expenseShare)
                                    .height(6.dp)
                                    .clip(CircleShape)
                                    .background(colors.expense)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(colors.expense)
                                )
                                Text(
                                    text = "Spent: ${if (settings.balanceHidden) "••••" else MoneyMath.formatAmount(totalSpent, settings.currencyOption)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colors.onSurface
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(colors.income)
                                )
                                Text(
                                    text = "Earned: ${if (settings.balanceHidden) "••••" else MoneyMath.formatAmount(totalEarned, settings.currencyOption)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colors.onSurface
                                )
                            }
                        }
                    }
                }
            }

            if (items.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = colors.surfaceContainer,
                        border = BorderStroke(1.dp, colors.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ReceiptLong,
                                contentDescription = "No matching transactions",
                                tint = colors.primary,
                                modifier = Modifier.size(38.dp)
                            )
                            Text(
                                text = "No Matching Transactions",
                                style = MaterialTheme.typography.headlineSmall,
                                color = colors.onSurface
                            )
                            Text(
                                text = "Try adjusting your search or filter selection, or record a new transaction.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(onClick = onResetFilters) {
                                    Text("Reset Filters", color = colors.primary)
                                }
                                Button(
                                    onClick = onAddTransactionClick,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.primaryContainer,
                                        contentColor = colors.onPrimary
                                    )
                                ) {
                                    Text("Add Entry")
                                }
                            }
                        }
                    }
                }
            } else {
                groupedByDay.forEach { (dayStart, dayItems) ->
                    val (dayPrefix, dayDateStr) = DateUtils.formatRelativeDayHeader(dayStart)
                    val dayIncome = dayItems.filter { it.entity.type == TransactionType.INCOME }.sumOf { it.entity.amountCents }
                    val dayExpense = dayItems.filter { it.entity.type == TransactionType.EXPENSE }.sumOf { it.entity.amountCents }
                    val dayNet = dayIncome - dayExpense

                    item(key = "header_$dayStart") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp, bottom = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = dayPrefix,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = colors.onSurface
                                )
                                Text("•", style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
                                Text(
                                    text = dayDateStr,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = colors.onSurfaceVariant
                                )
                            }
                            if (dayNet != 0L) {
                                Text(
                                    text = if (settings.balanceHidden) "••••" else MoneyMath.formatAmount(
                                        dayNet,
                                        settings.currencyOption,
                                        showSign = true
                                    ),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (dayNet > 0L) colors.income else colors.expense
                                )
                            }
                        }
                    }

                    items(dayItems, key = { it.entity.id }) { item ->
                        TransactionHistoryCard(
                            item = item,
                            settings = settings,
                            onClick = { detailItem = item }
                        )
                    }
                }
            }
        }
    }

    // Transaction Detail Bottom Sheet
    detailItem?.let { item ->
        val tx = item.entity
        ModalBottomSheet(
            onDismissRequest = { detailItem = null },
            containerColor = colors.surfaceContainerLow
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = tx.merchant.ifBlank { item.category?.name ?: tx.type.label },
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.onSurface
                        )
                        Text(
                            text = "${DateUtils.formatDate(tx.timestamp, settings.dateFormat)} • ${DateUtils.formatTime(tx.timestamp)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                    Text(
                        text = MoneyMath.formatAmount(
                            tx.amountCents,
                            settings.currencyOption,
                            isPositiveOverride = when (tx.type) {
                                TransactionType.INCOME -> true
                                TransactionType.EXPENSE -> false
                                TransactionType.TRANSFER -> null
                            }
                        ),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = when (tx.type) {
                            TransactionType.INCOME -> colors.income
                            TransactionType.EXPENSE -> colors.expense
                            TransactionType.TRANSFER -> colors.transfer
                        }
                    )
                }

                HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.4f))

                DetailInfoRow("Type", tx.type.label)
                DetailInfoRow("Category", item.category?.name ?: tx.type.label)
                DetailInfoRow(
                    "Payment Account",
                    if (tx.type == TransactionType.TRANSFER && item.toPaymentMethod != null) {
                        "${item.paymentMethod?.name ?: "From"} → ${item.toPaymentMethod.name}"
                    } else {
                        item.paymentMethod?.name ?: "Cash"
                    }
                )
                if (tx.note.isNotBlank()) {
                    DetailInfoRow("Note", tx.note)
                }
                if (!tx.receiptPath.isNullOrBlank()) {
                    Text(
                        text = "Attached Receipt / Bill Slip",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant
                    )
                    AsyncImage(
                        model = File(tx.receiptPath),
                        contentDescription = "Receipt image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            deleteConfirmId = tx.id
                            detailItem = null
                        },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, colors.expense.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("detail_delete_button")
                    ) {
                        Icon(Icons.Outlined.DeleteOutline, contentDescription = null, tint = colors.expense)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete", color = colors.expense)
                    }

                    Button(
                        onClick = {
                            detailItem = null
                            onEditTransaction(item)
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primaryContainer,
                            contentColor = colors.onPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("detail_edit_button")
                    ) {
                        Icon(Icons.Outlined.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit")
                    }
                }
            }
        }
    }

    // Multi-filter Bottom Sheet (Categories, Payment Methods, Month Toggle)
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = colors.surfaceContainerLow
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter Ledger",
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.onSurface
                    )
                    TextButton(onClick = {
                        onResetFilters()
                        showFilterSheet = false
                    }) {
                        Text("Reset All", color = colors.primary)
                    }
                }

                Surface(
                    onClick = {
                        onUpdateFilter {
                            it.copy(
                                useSelectedMonth = !it.useSelectedMonth,
                                customStartMillis = null,
                                customEndMillis = null
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = colors.surfaceContainer,
                    border = BorderStroke(1.dp, colors.cardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (filterState.useSelectedMonth) {
                                "Scope: ${DateUtils.formatMonthYear(selectedYearMonth)}"
                            } else {
                                "Scope: All History"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.onSurface
                        )
                        Text(
                            text = "Tap to toggle",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.primary
                        )
                    }
                }

                Text("Categories", style = MaterialTheme.typography.labelLarge, color = colors.onSurface)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val selected = cat.id in filterState.selectedCategoryIds
                        FilterPill(
                            label = cat.name,
                            selected = selected,
                            dotColor = CategoryIconMapper.parseColor(cat.colorHex, colors.primary),
                            showCheck = selected,
                            testTag = "sheet_filter_cat_${cat.id}",
                            onClick = {
                                onUpdateFilter { state ->
                                    val next = if (selected) {
                                        state.selectedCategoryIds - cat.id
                                    } else {
                                        state.selectedCategoryIds + cat.id
                                    }
                                    state.copy(selectedCategoryIds = next)
                                }
                            }
                        )
                    }
                }

                Text("Payment Methods", style = MaterialTheme.typography.labelLarge, color = colors.onSurface)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    paymentMethods.forEach { method ->
                        val selected = method.id in filterState.selectedPaymentMethodIds
                        FilterPill(
                            label = method.name,
                            selected = selected,
                            dotColor = colors.primary,
                            showCheck = selected,
                            testTag = "sheet_filter_method_${method.id}",
                            onClick = {
                                onUpdateFilter { state ->
                                    val next = if (selected) {
                                        state.selectedPaymentMethodIds - method.id
                                    } else {
                                        state.selectedPaymentMethodIds + method.id
                                    }
                                    state.copy(selectedPaymentMethodIds = next)
                                }
                            }
                        )
                    }
                }

                Button(
                    onClick = { showFilterSheet = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primaryContainer,
                        contentColor = colors.onPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Apply Filters", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showDateRangePicker) {
        val rangeState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = filterState.customStartMillis,
            initialSelectedEndDateMillis = filterState.customEndMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDateRangePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val s = rangeState.selectedStartDateMillis
                    val e = rangeState.selectedEndDateMillis ?: s
                    if (s != null && e != null) {
                        onUpdateFilter {
                            it.copy(
                                customStartMillis = DateUtils.startOfDay(s),
                                customEndMillis = DateUtils.endOfDay(e),
                                useSelectedMonth = false
                            )
                        }
                    }
                    showDateRangePicker = false
                }) {
                    Text("Apply Range", color = colors.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onUpdateFilter {
                        it.copy(
                            customStartMillis = null,
                            customEndMillis = null,
                            useSelectedMonth = !it.useSelectedMonth
                        )
                    }
                    showDateRangePicker = false
                }) {
                    Text(
                        if (filterState.useSelectedMonth) "Show All Time" else "Use Month Cycle",
                        color = colors.onSurfaceVariant
                    )
                }
            }
        ) {
            DateRangePicker(state = rangeState, modifier = Modifier.height(420.dp))
        }
    }

    deleteConfirmId?.let { txId ->
        AlertDialog(
            onDismissRequest = { deleteConfirmId = null },
            containerColor = colors.surfaceContainer,
            title = { Text("Delete Transaction?", color = colors.onSurface) },
            text = { Text("This action removes the record permanently from your offline SQLite vault.", color = colors.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteTransaction(txId)
                    deleteConfirmId = null
                }) {
                    Text("Delete", color = colors.expense, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmId = null }) {
                    Text("Cancel", color = colors.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
private fun FilterPill(
    label: String,
    selected: Boolean,
    dotColor: Color?,
    showCheck: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val colors = LakTheme.colors
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) colors.primaryContainer else colors.surfaceContainer,
        border = BorderStroke(
            1.dp,
            if (selected) colors.primary else colors.outlineVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier
            .height(38.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (showCheck) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = colors.onPrimary,
                    modifier = Modifier.size(15.dp)
                )
            } else if (dotColor != null) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (selected) colors.onPrimary else dotColor)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (selected) colors.onPrimary else colors.onSurface
            )
        }
    }
}

@Composable
private fun TransactionHistoryCard(
    item: TransactionItem,
    settings: AppSettings,
    onClick: () -> Unit
) {
    val colors = LakTheme.colors
    val tx = item.entity
    val accent = when (tx.type) {
        TransactionType.INCOME -> colors.income
        TransactionType.TRANSFER -> colors.transfer
        TransactionType.EXPENSE -> CategoryIconMapper.parseColor(item.category?.colorHex, colors.expense)
    }
    val title = tx.merchant.ifBlank { item.category?.name ?: tx.type.label }
    val methodText = if (tx.type == TransactionType.TRANSFER && item.toPaymentMethod != null) {
        "${item.paymentMethod?.name ?: "From"} → ${item.toPaymentMethod.name}"
    } else {
        item.paymentMethod?.name ?: "Cash"
    }
    val amountText = when (tx.type) {
        TransactionType.INCOME -> MoneyMath.formatAmount(tx.amountCents, settings.currencyOption, isPositiveOverride = true)
        TransactionType.EXPENSE -> MoneyMath.formatAmount(tx.amountCents, settings.currencyOption, isPositiveOverride = false)
        TransactionType.TRANSFER -> MoneyMath.formatAmount(tx.amountCents, settings.currencyOption)
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceContainer,
        border = BorderStroke(1.dp, colors.cardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_tx_card_${tx.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f).padding(end = 10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = CategoryIconMapper.iconForKey(item.category?.iconKey),
                        contentDescription = title,
                        tint = accent,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = item.category?.name ?: tx.type.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (tx.type == TransactionType.INCOME) colors.income else colors.onSurfaceVariant,
                            maxLines = 1
                        )
                        Text("•", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = colors.surfaceContainerHigh
                        ) {
                            Text(
                                text = methodText,
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = DateUtils.formatTime(tx.timestamp) + if (!tx.receiptPath.isNullOrBlank()) " • Receipt" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (settings.balanceHidden) "••••••" else amountText,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = when (tx.type) {
                        TransactionType.INCOME -> colors.income
                        TransactionType.EXPENSE -> colors.expense
                        TransactionType.TRANSFER -> colors.transfer
                    }
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (tx.isDemo) "Demo" else "LKR",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DetailInfoRow(label: String, value: String) {
    val colors = LakTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = colors.onSurface
        )
    }
}
