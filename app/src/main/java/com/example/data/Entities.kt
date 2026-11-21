package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.ui.theme.AppThemeMode
import com.example.util.DateUtils

enum class RestMode(val storageKey: String) {
    ALWAYS_ASK("ALWAYS_ASK"),
    SPLIT_TO_REST("SPLIT_TO_REST"),
    ADD_TO_TODAY("ADD_TO_TODAY");

    companion object {
        fun fromKey(key: String?): RestMode {
            return entries.firstOrNull { it.storageKey == key } ?: ALWAYS_ASK
        }
    }
}

enum class DebtType(val storageKey: String) {
    BORROWED("BORROWED"), // You owe someone
    LENT("LENT");         // Someone owes you

    companion object {
        fun fromKey(key: String?): DebtType {
            return entries.firstOrNull { it.storageKey == key } ?: BORROWED
        }
    }
}

enum class ReminderOperation(val storageKey: String) {
    SUBTRACT("SUBTRACT"),
    ADD("ADD");

    companion object {
        fun fromKey(key: String?): ReminderOperation {
            return entries.firstOrNull { it.storageKey == key } ?: SUBTRACT
        }
    }
}

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String = "Daily Budget",
    val isMonthly: Boolean = false,
    val startingBudget: Double,
    val startDateEpochDay: Long,
    val finishDateEpochDay: Long,
    val isActive: Boolean = true,
    val isSelectedOnHome: Boolean = true,
    val showMainOnHome: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val finishedAt: Long? = null,
    val restMode: String = RestMode.ALWAYS_ASK.storageKey,
    val currencyCode: String = "",
    val currencySymbol: String = "",
    val lastCheckedEpochDay: Long = startDateEpochDay,
    val todayExtraCarryover: Double = 0.0
)

@Entity(
    tableName = "sub_budgets",
    foreignKeys = [
        ForeignKey(
            entity = BudgetEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentBudgetId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["parentBudgetId"])]
)
data class SubBudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val parentBudgetId: Long,
    val name: String,
    val allocatedAmount: Double,
    val isShownOnHome: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = BudgetEntity::class,
            parentColumns = ["id"],
            childColumns = ["budgetId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["budgetId"]), Index(value = ["epochDay"])]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val budgetId: Long,
    val subBudgetId: Long? = null,
    val amount: Double,
    val comment: String = "",
    val epochDay: Long,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val budgetId: Long? = null,
    val personName: String,
    val amount: Double,
    val type: String = DebtType.BORROWED.storageKey,
    val note: String = "",
    val epochDay: Long = DateUtils.todayEpochDay(),
    val countInMainBudget: Boolean = false,
    val isSettled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "monthly_bills")
data class MonthlyBillEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val budgetId: Long? = null,
    val title: String,
    val amount: Double,
    val category: String = "Extra",
    val dueDayOfMonth: Int = 1,
    val operationType: String = ReminderOperation.SUBTRACT.storageKey,
    val targetSubBudgetId: Long? = null,
    val countInMainBudget: Boolean = true,
    val isPaidThisMonth: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val hasCompletedOnboarding: Boolean = false,
    val defaultRestMode: String = RestMode.ALWAYS_ASK.storageKey,
    val defaultCurrencyCode: String = "",
    val defaultCurrencySymbol: String = "",
    val startWeekOnMonday: Boolean = false,
    val themeMode: String = AppThemeMode.MIDNIGHT.storageKey
)
