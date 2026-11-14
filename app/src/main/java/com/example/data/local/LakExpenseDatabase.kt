package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.model.TransactionType

class LakTypeConverters {
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String = type.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return TransactionType.entries.firstOrNull { it.name == value } ?: TransactionType.EXPENSE
    }
}

@Database(
    entities = [
        UserProfileEntity::class,
        CategoryEntity::class,
        PaymentMethodEntity::class,
        TransactionEntity::class,
        BudgetEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(LakTypeConverters::class)
abstract class LakExpenseDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun categoryDao(): CategoryDao
    abstract fun paymentMethodDao(): PaymentMethodDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        const val DB_NAME = "lakexpense_vault.db"

        @Volatile
        private var INSTANCE: LakExpenseDatabase? = null

        fun getInstance(context: Context): LakExpenseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LakExpenseDatabase::class.java,
                    DB_NAME
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun createInMemory(context: Context): LakExpenseDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                LakExpenseDatabase::class.java
            ).allowMainThreadQueries().build()
        }
    }
}

object DefaultCatalog {
    fun defaultCategories(): List<CategoryEntity> = listOf(
        // Expense categories tailored for Sri Lanka
        CategoryEntity(name = "Food & Dining", subtitle = "PickMe Food, Cafes, Kottu", iconKey = "restaurant", colorHex = "#10B981", type = TransactionType.EXPENSE, isCustom = false, sortOrder = 1),
        CategoryEntity(name = "Fuel & Ride", subtitle = "Ceypetco, LIOC, PickMe, Uber", iconKey = "local_gas_station", colorHex = "#F59E0B", type = TransactionType.EXPENSE, isCustom = false, sortOrder = 2),
        CategoryEntity(name = "Groceries", subtitle = "Keells, Cargills, Arpico, SPAR", iconKey = "shopping_cart", colorHex = "#4EDEA3", type = TransactionType.EXPENSE, isCustom = false, sortOrder = 3),
        CategoryEntity(name = "CEB / Water", subtitle = "CEB, NWSDB, Dialog, SLT Fibre", iconKey = "bolt", colorHex = "#38BDF8", type = TransactionType.EXPENSE, isCustom = false, sortOrder = 4),
        CategoryEntity(name = "Medical", subtitle = "Asiri, Nawaloka, Union Chemist", iconKey = "medical_services", colorHex = "#F87171", type = TransactionType.EXPENSE, isCustom = false, sortOrder = 5),
        CategoryEntity(name = "Outing", subtitle = "Cinema, Streaming, Weekend Trips", iconKey = "theater_comedy", colorHex = "#A78BFA", type = TransactionType.EXPENSE, isCustom = false, sortOrder = 6),
        CategoryEntity(name = "Education", subtitle = "Tuition, Books, Courses", iconKey = "school", colorHex = "#2DD4BF", type = TransactionType.EXPENSE, isCustom = false, sortOrder = 7),
        CategoryEntity(name = "Shopping", subtitle = "Odel, Fashion, Electronics", iconKey = "shopping_bag", colorHex = "#FB7185", type = TransactionType.EXPENSE, isCustom = false, sortOrder = 8),
        CategoryEntity(name = "Personal Care", subtitle = "Salon, Fitness, Wellness", iconKey = "spa", colorHex = "#F472B6", type = TransactionType.EXPENSE, isCustom = false, sortOrder = 9),
        CategoryEntity(name = "Other", subtitle = "Miscellaneous Expenses", iconKey = "category", colorHex = "#94A3B8", type = TransactionType.EXPENSE, isCustom = false, sortOrder = 10),

        // Income categories
        CategoryEntity(name = "Salary", subtitle = "Monthly Payroll & Allowance", iconKey = "payments", colorHex = "#34D399", type = TransactionType.INCOME, isCustom = false, sortOrder = 11),
        CategoryEntity(name = "Freelance", subtitle = "Consulting & Client Projects", iconKey = "design_services", colorHex = "#10B981", type = TransactionType.INCOME, isCustom = false, sortOrder = 12),
        CategoryEntity(name = "Business", subtitle = "Trade & Venture Revenue", iconKey = "storefront", colorHex = "#38BDF8", type = TransactionType.INCOME, isCustom = false, sortOrder = 13),
        CategoryEntity(name = "Investment", subtitle = "FD Interest, Dividends, Treasury", iconKey = "trending_up", colorHex = "#6EE7B7", type = TransactionType.INCOME, isCustom = false, sortOrder = 14),
        CategoryEntity(name = "Other Income", subtitle = "Gifts, Refunds, Reimbursements", iconKey = "savings", colorHex = "#A7F3D0", type = TransactionType.INCOME, isCustom = false, sortOrder = 15),

        // Transfer category
        CategoryEntity(name = "Account Transfer", subtitle = "Between Local Accounts & Wallets", iconKey = "swap_horiz", colorHex = "#38BDF8", type = TransactionType.TRANSFER, isCustom = false, sortOrder = 16)
    )

    fun defaultPaymentMethods(): List<PaymentMethodEntity> = listOf(
        PaymentMethodEntity(name = "Cash", iconKey = "payments", isCustom = false, sortOrder = 1),
        PaymentMethodEntity(name = "Commercial Bank", iconKey = "account_balance", isCustom = false, sortOrder = 2),
        PaymentMethodEntity(name = "Sampath Bank", iconKey = "account_balance", isCustom = false, sortOrder = 3),
        PaymentMethodEntity(name = "HNB / BOC", iconKey = "account_balance_wallet", isCustom = false, sortOrder = 4),
        PaymentMethodEntity(name = "FriMi / Genie", iconKey = "smartphone", isCustom = false, sortOrder = 5),
        PaymentMethodEntity(name = "Credit Card", iconKey = "credit_card", isCustom = false, sortOrder = 6)
    )
}
