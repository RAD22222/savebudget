package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    // Settings
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun observeSettings(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsOnce(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSettings(settings: AppSettingsEntity)

    // Budgets
    @Query("SELECT * FROM budgets WHERE isActive = 1 ORDER BY isSelectedOnHome DESC, id DESC LIMIT 1")
    fun observeSelectedHomeBudget(): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets WHERE isActive = 1 ORDER BY isSelectedOnHome DESC, createdAt DESC")
    fun observeAllActiveBudgets(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE isActive = 0 ORDER BY finishedAt DESC, id DESC")
    fun observeFinishedBudgets(): Flow<List<BudgetEntity>>

    @Query("UPDATE budgets SET isSelectedOnHome = 0 WHERE isActive = 1")
    suspend fun clearHomeSelection()

    @Query("UPDATE budgets SET isSelectedOnHome = 1 WHERE id = :budgetId")
    suspend fun setHomeSelection(budgetId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity): Long

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)

    // Sub-Budgets under a Monthly/Main Budget
    @Query("SELECT * FROM sub_budgets WHERE parentBudgetId = :parentBudgetId ORDER BY createdAt ASC")
    fun observeSubBudgets(parentBudgetId: Long): Flow<List<SubBudgetEntity>>

    @Query("SELECT * FROM sub_budgets WHERE id = :id LIMIT 1")
    suspend fun getSubBudgetById(id: Long): SubBudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubBudget(subBudget: SubBudgetEntity): Long

    @Update
    suspend fun updateSubBudget(subBudget: SubBudgetEntity)

    @Delete
    suspend fun deleteSubBudget(subBudget: SubBudgetEntity)

    // Expenses
    @Query("SELECT * FROM expenses WHERE budgetId = :budgetId ORDER BY timestamp DESC")
    fun observeExpensesForBudget(budgetId: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE budgetId = :budgetId ORDER BY timestamp ASC")
    suspend fun getExpensesForBudgetOnce(budgetId: Long): List<ExpenseEntity>

    @Query("SELECT budgetId, SUM(amount) AS totalSpent FROM expenses GROUP BY budgetId")
    fun observeSpentTotalsByBudget(): Flow<List<BudgetSpentTotal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    // Borrows & Lends
    @Query("SELECT * FROM debts ORDER BY isSettled ASC, epochDay DESC, createdAt DESC")
    fun observeDebts(): Flow<List<DebtEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: DebtEntity): Long

    @Update
    suspend fun updateDebt(debt: DebtEntity)

    @Delete
    suspend fun deleteDebt(debt: DebtEntity)

    // Budget Reminders (connected to main or sub-budget)
    @Query("SELECT * FROM monthly_bills ORDER BY isPaidThisMonth ASC, dueDayOfMonth ASC, id DESC")
    fun observeMonthlyBills(): Flow<List<MonthlyBillEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonthlyBill(bill: MonthlyBillEntity): Long

    @Update
    suspend fun updateMonthlyBill(bill: MonthlyBillEntity)

    @Delete
    suspend fun deleteMonthlyBill(bill: MonthlyBillEntity)

    @Query("UPDATE monthly_bills SET isPaidThisMonth = 0")
    suspend fun resetAllBillsUnpaid()

    // Reset operations
    @Query("DELETE FROM expenses WHERE budgetId = :budgetId")
    suspend fun deleteExpensesForBudget(budgetId: Long)

    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()

    @Query("DELETE FROM sub_budgets")
    suspend fun deleteAllSubBudgets()

    @Query("DELETE FROM budgets")
    suspend fun deleteAllBudgets()

    @Query("DELETE FROM debts")
    suspend fun deleteAllDebts()

    @Query("DELETE FROM monthly_bills")
    suspend fun deleteAllExtras()
}

data class BudgetSpentTotal(
    val budgetId: Long,
    val totalSpent: Double
)
