package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.DesignServices
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.TheaterComedy
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIconMapper {
    val availableIcons: List<Pair<String, ImageVector>> = listOf(
        "restaurant" to Icons.Outlined.Restaurant,
        "local_gas_station" to Icons.Outlined.LocalGasStation,
        "shopping_cart" to Icons.Outlined.ShoppingCart,
        "bolt" to Icons.Outlined.Bolt,
        "medical_services" to Icons.Outlined.MedicalServices,
        "theater_comedy" to Icons.Outlined.TheaterComedy,
        "school" to Icons.Outlined.School,
        "shopping_bag" to Icons.Outlined.ShoppingBag,
        "spa" to Icons.Outlined.Spa,
        "directions_car" to Icons.Outlined.DirectionsCar,
        "payments" to Icons.Outlined.Payments,
        "design_services" to Icons.Outlined.DesignServices,
        "storefront" to Icons.Outlined.Storefront,
        "trending_up" to Icons.AutoMirrored.Outlined.TrendingUp,
        "savings" to Icons.Outlined.Savings,
        "swap_horiz" to Icons.Outlined.SwapHoriz,
        "account_balance" to Icons.Outlined.AccountBalance,
        "account_balance_wallet" to Icons.Outlined.AccountBalanceWallet,
        "smartphone" to Icons.Outlined.Smartphone,
        "credit_card" to Icons.Outlined.CreditCard,
        "receipt_long" to Icons.Outlined.ReceiptLong,
        "category" to Icons.Outlined.Category
    )

    fun iconForKey(key: String?): ImageVector {
        return availableIcons.firstOrNull { it.first == key }?.second ?: Icons.Outlined.Category
    }

    fun parseColor(hex: String?, fallback: Color): Color {
        if (hex.isNullOrBlank()) return fallback
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (_: Exception) {
            fallback
        }
    }
}
