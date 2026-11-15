package com.example.data.local

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.TransactionType

@Immutable
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val fullName: String,
    val email: String,
    val location: String,
    val openingBalanceCents: Long,
    val avatarUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Immutable
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val subtitle: String,
    val iconKey: String,
    val colorHex: String,
    val type: TransactionType,
    val isCustom: Boolean = false,
    val sortOrder: Int = 0
)

@Immutable
@Entity(tableName = "payment_methods")
data class PaymentMethodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconKey: String,
    val isCustom: Boolean = false,
    val sortOrder: Int = 0
)

@Immutable
@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["categoryId"]),
        Index(value = ["paymentMethodId"]),
        Index(value = ["type"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: TransactionType,
    val amountCents: Long,
    val categoryId: Long,
    val paymentMethodId: Long,
    val toPaymentMethodId: Long? = null,
    val merchant: String,
    val note: String,
    val receiptPath: String? = null,
    val timestamp: Long,
    val isDemo: Boolean = false
)

@Immutable
@Entity(
    tableName = "budgets",
    indices = [Index(value = ["yearMonth", "categoryId"], unique = true)]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val yearMonth: String,
    val categoryId: Long?, // null = Overall Monthly Budget Cap
    val limitCents: Long,
    val alertThresholdPercent: Int = 80,
    val createdAt: Long = System.currentTimeMillis()
)
