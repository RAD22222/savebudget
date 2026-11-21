package com.example.data

import com.example.util.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class BudgetRepository(private val dao: BudgetDao) {

    val settingsFlow: Flow<AppSettingsEntity> = dao.observeSettings().map {
        it ?: AppSettingsEntity()
    }

    val activeBudgetFlow: Flow<BudgetEntity?> = dao.observeSelectedHomeBudget()

    val allActiveBudgetsFlow: Flow<List<BudgetEntity>> = dao.observeAllActiveBudgets()

    val finishedBudgetsFlow: Flow<List<BudgetEntity>> = dao.observeFinishedBudgets()

    val spentTotalsByBudgetFlow: Flow<Map<Long, Double>> = dao.observeSpentTotalsByBudget().map { list ->
        list.associate { it.budgetId to it.totalSpent }
    }

    val debtsFlow: Flow<List<DebtEntity>> = dao.observeDebts()

    val monthlyBillsFlow: Flow<List<MonthlyBillEntity>> = dao.observeMonthlyBills()

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeExpensesFlow: Flow<List<ExpenseEntity>> = activeBudgetFlow.flatMapLatest { budget ->
        if (budget == null) flowOf(emptyList())
        else dao.observeExpensesForBudget(budget.id)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeSubBudgetsFlow: Flow<List<SubBudgetEntity>> = activeBudgetFlow.flatMapLatest { budget ->
        if (budget == null) flowOf(emptyList())
        else dao.observeSubBudgets(budget.id)
    }

    suspend fun completeOnboarding() {
        val current = dao.getSettingsOnce() ?: AppSettingsEntity()
        dao.upsertSettings(current.copy(hasCompletedOnboarding = true))
    }

    suspend fun updateSettings(transform: (AppSettingsEntity) -> AppSettingsEntity) {
        val current = dao.getSettingsOnce() ?: AppSettingsEntity()
        dao.upsertSettings(transform(current))
    }

    suspend fun createNewBudget(
        name: String,
        isMonthly: Boolean,
        startingBudget: Double,
        startEpochDay: Long,
        finishEpochDay: Long,
        restMode: RestMode,
        currencyCode: String,
        currencySymbol: String,
        selectOnHome: Boolean = true
    ): Long {
        val now = System.currentTimeMillis()
        if (selectOnHome) {
            dao.clearHomeSelection()
        }
        val entity = BudgetEntity(
            name = name.trim().ifEmpty { DateUtils.currentMonthBudgetName() },
            isMonthly = isMonthly,
            startingBudget = startingBudget,
            startDateEpochDay = startEpochDay,
            finishDateEpochDay = finishEpochDay,
            isActive = true,
            isSelectedOnHome = selectOnHome,
            showMainOnHome = true,
            createdAt = now,
            restMode = restMode.storageKey,
            currencyCode = currencyCode,
            currencySymbol = currencySymbol,
            lastCheckedEpochDay = startEpochDay,
            todayExtraCarryover = 0.0
        )
        updateSettings {
            it.copy(
                hasCompletedOnboarding = true,
                defaultRestMode = restMode.storageKey,
                defaultCurrencyCode = currencyCode,
                defaultCurrencySymbol = currencySymbol
            )
        }
        return dao.insertBudget(entity)
    }

    suspend fun selectBudgetForHome(budgetId: Long) {
        dao.clearHomeSelection()
        dao.setHomeSelection(budgetId)
    }

    suspend fun updateBudget(budget: BudgetEntity) {
        dao.updateBudget(budget)
    }

    suspend fun finishBudgetEarly(budget: BudgetEntity) {
        dao.updateBudget(
            budget.copy(
                isActive = false,
                isSelectedOnHome = false,
                finishedAt = System.currentTimeMillis()
            )
        )
    }

    // Sub-Budgets (Transport, Food, Snacks, etc.)
    suspend fun addSubBudget(
        parentBudgetId: Long,
        name: String,
        allocatedAmount: Double,
        showOnHome: Boolean = true
    ): Long {
        return dao.insertSubBudget(
            SubBudgetEntity(
                parentBudgetId = parentBudgetId,
                name = name.trim(),
                allocatedAmount = allocatedAmount,
                isShownOnHome = showOnHome
            )
        )
    }

    suspend fun updateSubBudget(subBudget: SubBudgetEntity) {
        dao.updateSubBudget(subBudget)
    }

    suspend fun deleteSubBudget(subBudget: SubBudgetEntity) {
        dao.deleteSubBudget(subBudget)
    }

    suspend fun getSubBudgetById(id: Long): SubBudgetEntity? = dao.getSubBudgetById(id)

    // Expenses
    suspend fun addExpense(
        budgetId: Long,
        subBudgetId: Long? = null,
        amount: Double,
        comment: String,
        epochDay: Long = DateUtils.todayEpochDay()
    ): Long {
        return dao.insertExpense(
            ExpenseEntity(
                budgetId = budgetId,
                subBudgetId = subBudgetId,
                amount = amount,
                comment = comment.trim(),
                epochDay = epochDay,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun restoreExpense(expense: ExpenseEntity) {
        dao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: ExpenseEntity) {
        dao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        dao.deleteExpense(expense)
    }

    // Borrows & Lends connected to a monthly budget
    suspend fun addDebt(
        budgetId: Long?,
        personName: String,
        amount: Double,
        type: DebtType,
        note: String,
        epochDay: Long,
        countInMainBudget: Boolean
    ) {
        dao.insertDebt(
            DebtEntity(
                budgetId = budgetId,
                personName = personName.trim(),
                amount = amount,
                type = type.storageKey,
                note = note.trim(),
                epochDay = epochDay,
                countInMainBudget = countInMainBudget
            )
        )
    }

    suspend fun toggleDebtSettled(debt: DebtEntity) {
        dao.updateDebt(debt.copy(isSettled = !debt.isSettled))
    }

    suspend fun toggleDebtCountInBudget(debt: DebtEntity) {
        dao.updateDebt(debt.copy(countInMainBudget = !debt.countInMainBudget))
    }

    suspend fun deleteDebt(debt: DebtEntity) {
        dao.deleteDebt(debt)
    }

    // Extras (connected to Main Budget or Sub-Budget — Add or Subtract)
    suspend fun addExtra(
        budgetId: Long?,
        title: String,
        amount: Double,
        dueDayOfMonth: Int,
        operation: ReminderOperation,
        targetSubBudgetId: Long?,
        countInMainBudget: Boolean
    ) {
        dao.insertMonthlyBill(
            MonthlyBillEntity(
                budgetId = budgetId,
                title = title.trim(),
                amount = amount,
                category = if (operation == ReminderOperation.ADD) "Add" else "Subtract",
                dueDayOfMonth = dueDayOfMonth.coerceIn(1, 31),
                operationType = operation.storageKey,
                targetSubBudgetId = targetSubBudgetId,
                countInMainBudget = countInMainBudget,
                isPaidThisMonth = countInMainBudget
            )
        )
    }

    suspend fun toggleExtraCountInBudget(extra: MonthlyBillEntity) {
        val next = !extra.countInMainBudget
        dao.updateMonthlyBill(extra.copy(countInMainBudget = next, isPaidThisMonth = next))
    }

    suspend fun deleteExtra(extra: MonthlyBillEntity) {
        dao.deleteMonthlyBill(extra)
    }

    suspend fun resetAllExtras() {
        dao.resetAllBillsUnpaid()
    }

    suspend fun resetCurrentBudgetExpenses(budget: BudgetEntity) {
        dao.deleteExpensesForBudget(budget.id)
        dao.updateBudget(
            budget.copy(
                lastCheckedEpochDay = DateUtils.todayEpochDay(),
                todayExtraCarryover = 0.0
            )
        )
    }

    suspend fun resetAllData() {
        dao.deleteAllExpenses()
        dao.deleteAllSubBudgets()
        dao.deleteAllDebts()
        dao.deleteAllExtras()
        dao.deleteAllBudgets()
    }

    suspend fun buildCsvForBudget(budget: BudgetEntity): String {
        val expenses = dao.getExpensesForBudgetOnce(budget.id)
        val sb = StringBuilder()
        sb.appendLine("Date,Time,Amount,Currency,Comment")
        for (exp in expenses) {
            val dateStr = DateUtils.formatIsoDate(exp.epochDay)
            val timeStr = DateUtils.formatTime(exp.timestamp)
            val escapedComment = "\"${exp.comment.replace("\"", "\"\"")}\""
            val curr = budget.currencyCode.ifEmpty { budget.currencySymbol }
            sb.appendLine("$dateStr,$timeStr,${exp.amount},$curr,$escapedComment")
        }
        return sb.toString()
    }
}
