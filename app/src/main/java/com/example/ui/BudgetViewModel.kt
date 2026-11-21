package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppSettingsEntity
import com.example.data.BudgetEntity
import com.example.data.BudgetRepository
import com.example.data.DebtEntity
import com.example.data.DebtType
import com.example.data.ExpenseEntity
import com.example.data.MonthlyBillEntity
import com.example.data.ReminderOperation
import com.example.data.RestMode
import com.example.data.SubBudgetEntity
import com.example.ui.theme.AppThemeMode
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max

enum class BudgetHealth {
    HEALTHY,
    WARNING,
    OVER
}

data class DailyExpenseGroup(
    val epochDay: Long,
    val totalForDay: Double,
    val expenses: List<ExpenseEntity>
)

/** A selectable budget pill shown on the Home screen (either the whole Main budget or a Sub-budget). */
data class HomeBudgetChip(
    val subBudgetId: Long?, // null = Whole Main/Monthly Budget
    val name: String,
    val remaining: Double,
    val allocated: Double
)

data class BudgetMetrics(
    val startingBudget: Double,
    val totalSpent: Double,
    val totalRemaining: Double,
    val remainingPercent: Int,
    val spentPercent: Int,
    val totalDays: Int,
    val daysLeft: Int,
    val daysLeftFraction: Float,
    val dailyAllowanceBase: Double,
    val spentOnSelectedDay: Double,
    val remainingOnSelectedDay: Double,
    val selectedDayProgressFraction: Float,
    val selectedDayHealth: BudgetHealth,
    val overallHealth: BudgetHealth,
    val isExpired: Boolean,
    val pendingRolloverAmount: Double
)

data class BudgetUiState(
    val isLoading: Boolean = true,
    val settings: AppSettingsEntity = AppSettingsEntity(),
    val themeMode: AppThemeMode = AppThemeMode.MIDNIGHT,
    val activeBudget: BudgetEntity? = null,
    val allActiveBudgets: List<BudgetEntity> = emptyList(),
    val subBudgets: List<SubBudgetEntity> = emptyList(),
    val spentBySubBudgetId: Map<Long, Double> = emptyMap(),
    val extrasAdjustmentBySubId: Map<Long, Double> = emptyMap(),
    val debtMainBudgetDelta: Double = 0.0,
    val extrasMainBudgetDelta: Double = 0.0,
    val effectiveMainBudget: Double = 0.0,
    val homeBudgetChips: List<HomeBudgetChip> = emptyList(),
    val activeHomeSubBudgetId: Long? = null, // null = tracking whole main budget on Home
    val spentByBudgetId: Map<Long, Double> = emptyMap(),
    val finishedBudgets: List<BudgetEntity> = emptyList(),
    val expenses: List<ExpenseEntity> = emptyList(),
    val groupedExpenses: List<DailyExpenseGroup> = emptyList(),
    val selectedEpochDay: Long = DateUtils.todayEpochDay(),
    val selectedDayExpenses: List<ExpenseEntity> = emptyList(),
    val canGoPrevDay: Boolean = false,
    val canGoNextDay: Boolean = false,
    val metrics: BudgetMetrics? = null,
    val debts: List<DebtEntity> = emptyList(),
    val extras: List<MonthlyBillEntity> = emptyList(),
    val keypadInput: String = "",
    val expenseComment: String = "",
    val lastDeletedExpense: ExpenseEntity? = null
)

class BudgetViewModel(
    private val repository: BudgetRepository
) : ViewModel() {

    private data class TransientState(
        val selectedEpochDay: Long = DateUtils.todayEpochDay(),
        val activeHomeSubBudgetId: Long? = null,
        val keypadInput: String = "",
        val expenseComment: String = "",
        val lastDeletedExpense: ExpenseEntity? = null
    )

    private data class CoreBudgetBundle(
        val settings: AppSettingsEntity,
        val activeBudget: BudgetEntity?,
        val allActiveBudgets: List<BudgetEntity>,
        val subBudgets: List<SubBudgetEntity>,
        val expenses: List<ExpenseEntity>
    )

    private data class HubBundle(
        val finishedBudgets: List<BudgetEntity>,
        val spentByBudgetId: Map<Long, Double>,
        val debts: List<DebtEntity>,
        val reminders: List<MonthlyBillEntity>,
        val transient: TransientState
    )

    private val transientState = MutableStateFlow(TransientState())

    private val coreBundleFlow = combine(
        repository.settingsFlow,
        repository.activeBudgetFlow,
        repository.allActiveBudgetsFlow,
        repository.activeSubBudgetsFlow,
        repository.activeExpensesFlow
    ) { settings, activeBudget, allActive, subBudgets, expenses ->
        CoreBudgetBundle(settings, activeBudget, allActive, subBudgets, expenses)
    }

    private val hubBundleFlow = combine(
        repository.finishedBudgetsFlow,
        repository.spentTotalsByBudgetFlow,
        repository.debtsFlow,
        repository.monthlyBillsFlow,
        transientState
    ) { finished, spentMap, debts, reminders, transient ->
        HubBundle(finished, spentMap, debts, reminders, transient)
    }

    val uiState: StateFlow<BudgetUiState> = combine(
        coreBundleFlow,
        hubBundleFlow
    ) { core, hub ->
        val today = DateUtils.todayEpochDay()
        val activeBudget = core.activeBudget

        val minNavDay = activeBudget?.let { minOf(it.startDateEpochDay, today) - 30L } ?: today
        val maxNavDay = activeBudget?.let { max(it.finishDateEpochDay, today) } ?: today
        val clampedDay = if (activeBudget != null) {
            hub.transient.selectedEpochDay.coerceIn(minNavDay, maxNavDay)
        } else {
            today
        }

        val canPrev = activeBudget != null && clampedDay > minNavDay
        val canNext = activeBudget != null && clampedDay < maxNavDay

        // Scope Borrow/Lend and Extras to the active Monthly Budget (including legacy null-budget rows)
        val scopedDebts = if (activeBudget != null) {
            hub.debts.filter { it.budgetId == null || it.budgetId == activeBudget.id }
        } else {
            emptyList()
        }
        val scopedExtras = if (activeBudget != null) {
            hub.reminders.filter { it.budgetId == null || it.budgetId == activeBudget.id }
        } else {
            emptyList()
        }

        // Live connected adjustments for Main Budget & Sub-Budgets
        val debtMainDelta = scopedDebts
            .filter { it.countInMainBudget && !it.isSettled }
            .sumOf { debt ->
                if (DebtType.fromKey(debt.type) == DebtType.BORROWED) debt.amount else -debt.amount
            }

        val extrasMainDelta = scopedExtras
            .filter { it.countInMainBudget }
            .sumOf { extra ->
                if (ReminderOperation.fromKey(extra.operationType) == ReminderOperation.ADD) extra.amount
                else -extra.amount
            }

        val extrasSubDeltaMap = scopedExtras
            .filter { it.countInMainBudget && it.targetSubBudgetId != null }
            .groupBy { it.targetSubBudgetId!! }
            .mapValues { (_, list) ->
                list.sumOf { extra ->
                    if (ReminderOperation.fromKey(extra.operationType) == ReminderOperation.ADD) extra.amount
                    else -extra.amount
                }
            }

        val effectiveMainTotal = if (activeBudget != null) {
            activeBudget.startingBudget + debtMainDelta + extrasMainDelta
        } else {
            0.0
        }

        // Compute spent per sub-budget
        val spentBySub = core.expenses
            .filter { it.subBudgetId != null }
            .groupBy { it.subBudgetId!! }
            .mapValues { (_, list) -> list.sumOf { it.amount } }

        // Build Home selectable budget pills based on user selection in Monthly Hub
        val homeChips = mutableListOf<HomeBudgetChip>()
        if (activeBudget != null) {
            val totalSpentMain = core.expenses.sumOf { it.amount }
            val visibleSubs = core.subBudgets.filter { it.isShownOnHome }

            // Always ensure at least one budget is visible on Home
            if (activeBudget.showMainOnHome || visibleSubs.isEmpty()) {
                homeChips.add(
                    HomeBudgetChip(
                        subBudgetId = null,
                        name = activeBudget.name,
                        remaining = effectiveMainTotal - totalSpentMain,
                        allocated = effectiveMainTotal
                    )
                )
            }
            visibleSubs.forEach { sub ->
                val subSpent = spentBySub[sub.id] ?: 0.0
                val effectiveSubAllocated = sub.allocatedAmount + (extrasSubDeltaMap[sub.id] ?: 0.0)
                homeChips.add(
                    HomeBudgetChip(
                        subBudgetId = sub.id,
                        name = sub.name,
                        remaining = effectiveSubAllocated - subSpent,
                        allocated = effectiveSubAllocated
                    )
                )
            }
        }

        // Resolve currently selected Home budget (defaulting to the first available chip)
        val requestedSubId = hub.transient.activeHomeSubBudgetId
        val validActiveSubId = if (homeChips.any { it.subBudgetId == requestedSubId }) {
            requestedSubId
        } else {
            homeChips.firstOrNull()?.subBudgetId
        }

        val activeSubEntity = validActiveSubId?.let { id -> core.subBudgets.firstOrNull { it.id == id } }

        // Filter expenses for metrics/history according to selected Home sub-budget (or all if Main)
        val scopedExpenses = if (activeSubEntity != null) {
            core.expenses.filter { it.subBudgetId == activeSubEntity.id }
        } else {
            core.expenses
        }

        val effectiveStartingForMetrics = if (activeSubEntity != null) {
            activeSubEntity.allocatedAmount + (extrasSubDeltaMap[activeSubEntity.id] ?: 0.0)
        } else {
            effectiveMainTotal
        }

        val metrics = activeBudget?.let {
            computeMetrics(
                budget = it,
                effectiveStartingBudget = effectiveStartingForMetrics,
                expenses = scopedExpenses,
                todayEpochDay = today,
                selectedEpochDay = clampedDay,
                isSubBudget = activeSubEntity != null
            )
        }

        val grouped = scopedExpenses
            .groupBy { it.epochDay }
            .entries
            .sortedByDescending { it.key }
            .map { (day, list) ->
                DailyExpenseGroup(
                    epochDay = day,
                    totalForDay = list.sumOf { it.amount },
                    expenses = list.sortedByDescending { it.timestamp }
                )
            }

        val selectedDayList = scopedExpenses
            .filter { it.epochDay == clampedDay }
            .sortedByDescending { it.timestamp }

        BudgetUiState(
            isLoading = false,
            settings = core.settings,
            themeMode = AppThemeMode.fromKey(core.settings.themeMode),
            activeBudget = activeBudget,
            allActiveBudgets = core.allActiveBudgets,
            subBudgets = core.subBudgets,
            spentBySubBudgetId = spentBySub,
            extrasAdjustmentBySubId = extrasSubDeltaMap,
            debtMainBudgetDelta = debtMainDelta,
            extrasMainBudgetDelta = extrasMainDelta,
            effectiveMainBudget = effectiveMainTotal,
            homeBudgetChips = homeChips,
            activeHomeSubBudgetId = validActiveSubId,
            spentByBudgetId = hub.spentByBudgetId,
            finishedBudgets = hub.finishedBudgets,
            expenses = scopedExpenses,
            groupedExpenses = grouped,
            selectedEpochDay = clampedDay,
            selectedDayExpenses = selectedDayList,
            canGoPrevDay = canPrev,
            canGoNextDay = canNext,
            metrics = metrics,
            debts = scopedDebts,
            extras = scopedExtras,
            keypadInput = hub.transient.keypadInput,
            expenseComment = hub.transient.expenseComment,
            lastDeletedExpense = hub.transient.lastDeletedExpense
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BudgetUiState(isLoading = true)
    )

    private fun computeMetrics(
        budget: BudgetEntity,
        effectiveStartingBudget: Double,
        expenses: List<ExpenseEntity>,
        todayEpochDay: Long,
        selectedEpochDay: Long,
        isSubBudget: Boolean
    ): BudgetMetrics {
        val totalDays = DateUtils.inclusiveDaysBetween(
            budget.startDateEpochDay,
            budget.finishDateEpochDay
        ).coerceAtLeast(1)

        val isExpired = todayEpochDay > budget.finishDateEpochDay
        val rawDaysLeft = DateUtils.inclusiveDaysBetween(
            max(todayEpochDay, budget.startDateEpochDay),
            budget.finishDateEpochDay
        )
        val daysLeft = if (isExpired) 0 else rawDaysLeft.coerceIn(1, totalDays)
        val daysLeftFraction = if (totalDays > 0) daysLeft.toFloat() / totalDays.toFloat() else 0f

        val totalSpent = expenses.sumOf { it.amount }
        val totalRemaining = effectiveStartingBudget - totalSpent

        val remainingRatio = if (effectiveStartingBudget > 0.0) {
            (totalRemaining / effectiveStartingBudget).toFloat()
        } else 0f
        val remainingPercent = (remainingRatio * 100f).toInt().coerceIn(0, 100)
        val spentPercent = if (effectiveStartingBudget > 0.0) {
            ((totalSpent / effectiveStartingBudget) * 100.0).toInt().coerceAtLeast(0)
        } else 0

        val spentOnSelectedDay = expenses
            .filter { it.epochDay == selectedEpochDay }
            .sumOf { it.amount }
        val spentBeforeSelectedDay = expenses
            .filter { it.epochDay < selectedEpochDay }
            .sumOf { it.amount }

        val restMode = RestMode.fromKey(budget.restMode)
        val basePerDay = effectiveStartingBudget / totalDays.toDouble()

        val spentBeforeToday = expenses.filter { it.epochDay < todayEpochDay }.sumOf { it.amount }
        val elapsedDaysBeforeToday = (todayEpochDay - budget.startDateEpochDay).toInt().coerceAtLeast(0)
        val expectedSpentBeforeToday = basePerDay * elapsedDaysBeforeToday
        val unspentCarryoverFromPast = (expectedSpentBeforeToday - spentBeforeToday).coerceAtLeast(0.0)

        val hasNewDayRollover = !isSubBudget &&
            todayEpochDay > budget.lastCheckedEpochDay &&
            elapsedDaysBeforeToday > 0 &&
            unspentCarryoverFromPast >= 0.01 &&
            !isExpired

        val pendingRollover = if (restMode == RestMode.ALWAYS_ASK && hasNewDayRollover) {
            unspentCarryoverFromPast
        } else {
            0.0
        }

        val daysFromSelectedToEnd = DateUtils.inclusiveDaysBetween(
            max(selectedEpochDay, budget.startDateEpochDay),
            budget.finishDateEpochDay
        ).coerceAtLeast(1)
        val elapsedBeforeSelected = (selectedEpochDay - budget.startDateEpochDay).toInt().coerceAtLeast(0)
        val remainingBeforeSelected = effectiveStartingBudget - spentBeforeSelectedDay
        val carryoverBeforeSelected = ((basePerDay * elapsedBeforeSelected) - spentBeforeSelectedDay)
            .coerceAtLeast(0.0)

        val dailyAllowanceBase = when {
            selectedEpochDay > budget.finishDateEpochDay -> totalRemaining.coerceAtLeast(0.0)
            restMode == RestMode.ADD_TO_TODAY -> {
                (basePerDay + carryoverBeforeSelected)
                    .coerceAtMost(remainingBeforeSelected.coerceAtLeast(0.0))
            }
            restMode == RestMode.SPLIT_TO_REST -> {
                (remainingBeforeSelected / daysFromSelectedToEnd.toDouble()).coerceAtLeast(0.0)
            }
            else -> {
                val extra = if (!isSubBudget && selectedEpochDay == todayEpochDay) budget.todayExtraCarryover else 0.0
                val splitBase = ((remainingBeforeSelected - extra) / daysFromSelectedToEnd.toDouble())
                    .coerceAtLeast(0.0)
                (splitBase + extra).coerceAtLeast(0.0)
            }
        }

        val remainingOnSelectedDay = dailyAllowanceBase - spentOnSelectedDay
        val selectedProgressFraction = when {
            dailyAllowanceBase <= 0.0 -> 0f
            remainingOnSelectedDay <= 0.0 -> 0f
            else -> (remainingOnSelectedDay / dailyAllowanceBase).toFloat().coerceIn(0f, 1f)
        }

        val selectedDayHealth = when {
            remainingOnSelectedDay < -0.004 || (dailyAllowanceBase <= 0.0 && totalRemaining <= 0.0) -> BudgetHealth.OVER
            selectedProgressFraction < 0.25f -> BudgetHealth.WARNING
            else -> BudgetHealth.HEALTHY
        }

        val overallHealth = when {
            totalRemaining < -0.004 -> BudgetHealth.OVER
            remainingRatio < 0.20f -> BudgetHealth.WARNING
            else -> BudgetHealth.HEALTHY
        }

        return BudgetMetrics(
            startingBudget = effectiveStartingBudget,
            totalSpent = totalSpent,
            totalRemaining = totalRemaining,
            remainingPercent = remainingPercent,
            spentPercent = spentPercent,
            totalDays = totalDays,
            daysLeft = daysLeft,
            daysLeftFraction = daysLeftFraction,
            dailyAllowanceBase = dailyAllowanceBase,
            spentOnSelectedDay = spentOnSelectedDay,
            remainingOnSelectedDay = remainingOnSelectedDay,
            selectedDayProgressFraction = selectedProgressFraction,
            selectedDayHealth = selectedDayHealth,
            overallHealth = overallHealth,
            isExpired = isExpired,
            pendingRolloverAmount = pendingRollover
        )
    }

    // Select active chip on Home screen (null = whole main/monthly budget, or specific subBudgetId)
    fun selectActiveHomeChip(subBudgetId: Long?) {
        transientState.update { it.copy(activeHomeSubBudgetId = subBudgetId) }
    }

    // Toggle whether Main Budget itself is shown on Home screen
    fun toggleShowMainBudgetOnHome() {
        val active = uiState.value.activeBudget ?: return
        val anySubShown = uiState.value.subBudgets.any { it.isShownOnHome }
        // Don't allow turning off Main if no sub-budgets are selected on Home
        if (active.showMainOnHome && !anySubShown) return
        viewModelScope.launch {
            repository.updateBudget(active.copy(showMainOnHome = !active.showMainOnHome))
        }
    }

    // Sub-Budget CRUD under current Monthly/Main Budget
    fun addSubBudget(name: String, amount: Double) {
        val active = uiState.value.activeBudget ?: return
        if (name.isBlank() || amount <= 0.0) return
        viewModelScope.launch {
            repository.addSubBudget(
                parentBudgetId = active.id,
                name = name.trim(),
                allocatedAmount = amount,
                showOnHome = true
            )
        }
    }

    fun toggleSubBudgetOnHome(subBudget: SubBudgetEntity) {
        val active = uiState.value.activeBudget ?: return
        val otherShownCount = uiState.value.subBudgets.count { it.id != subBudget.id && it.isShownOnHome }
        // Keep at least one budget visible on Home (turn Main back on if unchecking last sub)
        viewModelScope.launch {
            val nextShown = !subBudget.isShownOnHome
            repository.updateSubBudget(subBudget.copy(isShownOnHome = nextShown))
            if (!nextShown && otherShownCount == 0 && !active.showMainOnHome) {
                repository.updateBudget(active.copy(showMainOnHome = true))
            }
        }
    }

    fun deleteSubBudget(subBudget: SubBudgetEntity) {
        viewModelScope.launch {
            repository.deleteSubBudget(subBudget)
            if (transientState.value.activeHomeSubBudgetId == subBudget.id) {
                transientState.update { it.copy(activeHomeSubBudgetId = null) }
            }
        }
    }

    fun renameActiveBudget(newName: String) {
        val active = uiState.value.activeBudget ?: return
        if (newName.isBlank()) return
        viewModelScope.launch {
            repository.updateBudget(active.copy(name = newName.trim()))
        }
    }

    // Day switcher (Swipe Left / Right on minimal date pill)
    fun shiftSelectedDay(deltaDays: Int) {
        val active = uiState.value.activeBudget ?: return
        val today = DateUtils.todayEpochDay()
        val minNavDay = minOf(active.startDateEpochDay, today) - 30L
        val maxNavDay = max(active.finishDateEpochDay, today)
        val current = uiState.value.selectedEpochDay
        val target = (current + deltaDays).coerceIn(minNavDay, maxNavDay)
        if (target != current) {
            transientState.update { it.copy(selectedEpochDay = target) }
        }
    }

    fun resetSelectedDayToToday() {
        val today = DateUtils.todayEpochDay()
        transientState.update { it.copy(selectedEpochDay = today) }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            repository.completeOnboarding()
        }
    }

    fun saveBudget(
        name: String = DateUtils.currentMonthBudgetName(),
        isMonthly: Boolean = true,
        amount: Double,
        startEpochDay: Long = DateUtils.todayEpochDay(),
        finishEpochDay: Long,
        restMode: RestMode,
        currencyCode: String,
        currencySymbol: String,
        isEditingExisting: Boolean
    ) {
        viewModelScope.launch {
            val today = DateUtils.todayEpochDay()
            val safeStart = startEpochDay
            val safeFinish = finishEpochDay.coerceAtLeast(safeStart)
            val existing = uiState.value.activeBudget
            if (isEditingExisting && existing != null) {
                repository.updateBudget(
                    existing.copy(
                        name = name.trim().ifEmpty { existing.name },
                        startingBudget = amount,
                        startDateEpochDay = safeStart,
                        finishDateEpochDay = safeFinish,
                        restMode = restMode.storageKey,
                        currencyCode = currencyCode,
                        currencySymbol = currencySymbol,
                        lastCheckedEpochDay = today
                    )
                )
                repository.updateSettings {
                    it.copy(
                        defaultRestMode = restMode.storageKey,
                        defaultCurrencyCode = currencyCode,
                        defaultCurrencySymbol = currencySymbol
                    )
                }
            } else {
                repository.createNewBudget(
                    name = name.ifBlank { DateUtils.currentMonthBudgetName() },
                    isMonthly = isMonthly,
                    startingBudget = amount,
                    startEpochDay = safeStart,
                    finishEpochDay = safeFinish,
                    restMode = restMode,
                    currencyCode = currencyCode,
                    currencySymbol = currencySymbol,
                    selectOnHome = true
                )
            }
            transientState.update {
                it.copy(
                    selectedEpochDay = today.coerceIn(safeStart, safeFinish),
                    activeHomeSubBudgetId = null,
                    keypadInput = "",
                    expenseComment = ""
                )
            }
        }
    }

    fun selectBudgetForHome(budgetId: Long) {
        viewModelScope.launch {
            repository.selectBudgetForHome(budgetId)
            transientState.update {
                it.copy(
                    selectedEpochDay = DateUtils.todayEpochDay(),
                    activeHomeSubBudgetId = null,
                    keypadInput = "",
                    expenseComment = ""
                )
            }
        }
    }

    fun createQuickMonthlyBudget(
        name: String,
        amount: Double,
        selectOnHome: Boolean
    ) {
        if (amount <= 0.0) return
        viewModelScope.launch {
            val settings = uiState.value.settings
            val today = DateUtils.todayEpochDay()
            val endOfMonth = DateUtils.endOfCurrentMonthEpochDay().coerceAtLeast(today)
            repository.createNewBudget(
                name = name.ifBlank { DateUtils.currentMonthBudgetName() },
                isMonthly = true,
                startingBudget = amount,
                startEpochDay = today,
                finishEpochDay = endOfMonth,
                restMode = RestMode.fromKey(settings.defaultRestMode),
                currencyCode = settings.defaultCurrencyCode,
                currencySymbol = settings.defaultCurrencySymbol,
                selectOnHome = selectOnHome
            )
            if (selectOnHome) {
                transientState.update {
                    it.copy(
                        selectedEpochDay = today,
                        activeHomeSubBudgetId = null,
                        keypadInput = "",
                        expenseComment = ""
                    )
                }
            }
        }
    }

    fun updateThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(themeMode = mode.storageKey) }
        }
    }

    fun updateRestMode(restMode: RestMode) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(defaultRestMode = restMode.storageKey) }
            val active = uiState.value.activeBudget
            if (active != null) {
                repository.updateBudget(
                    active.copy(
                        restMode = restMode.storageKey,
                        lastCheckedEpochDay = DateUtils.todayEpochDay(),
                        todayExtraCarryover = 0.0
                    )
                )
            }
        }
    }

    fun resolveRolloverChoice(addToToday: Boolean) {
        viewModelScope.launch {
            val active = uiState.value.activeBudget ?: return@launch
            val pending = uiState.value.metrics?.pendingRolloverAmount ?: 0.0
            repository.updateBudget(
                active.copy(
                    lastCheckedEpochDay = DateUtils.todayEpochDay(),
                    todayExtraCarryover = if (addToToday) pending else 0.0
                )
            )
        }
    }

    fun updateCurrency(code: String, symbol: String) {
        viewModelScope.launch {
            repository.updateSettings {
                it.copy(defaultCurrencyCode = code, defaultCurrencySymbol = symbol)
            }
            val active = uiState.value.activeBudget
            if (active != null) {
                repository.updateBudget(
                    active.copy(currencyCode = code, currencySymbol = symbol)
                )
            }
        }
    }

    fun updateStartWeekOnMonday(startOnMonday: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(startWeekOnMonday = startOnMonday) }
        }
    }

    fun finishBudgetEarly() {
        viewModelScope.launch {
            val active = uiState.value.activeBudget ?: return@launch
            repository.finishBudgetEarly(active)
            transientState.update {
                it.copy(activeHomeSubBudgetId = null, keypadInput = "", expenseComment = "")
            }
        }
    }

    // Keypad operations
    fun onDigitPressed(digit: Char) {
        transientState.update { state ->
            val current = state.keypadInput
            val parts = current.split(".")
            if (parts.size == 2 && parts[1].length >= 2) return@update state
            if (parts.size == 1 && current.length >= 9) return@update state

            val next = when {
                current == "0" && digit == '0' -> "0"
                current == "0" && digit != '0' -> digit.toString()
                else -> current + digit
            }
            state.copy(keypadInput = next)
        }
    }

    fun onDecimalPressed() {
        transientState.update { state ->
            val current = state.keypadInput
            if (current.contains(".")) return@update state
            val next = if (current.isEmpty()) "0." else "$current."
            state.copy(keypadInput = next)
        }
    }

    fun onBackspacePressed() {
        transientState.update { state ->
            if (state.keypadInput.isEmpty()) return@update state
            state.copy(keypadInput = state.keypadInput.dropLast(1))
        }
    }

    fun onClearKeypad() {
        transientState.update { it.copy(keypadInput = "", expenseComment = "") }
    }

    fun onCommentChanged(comment: String) {
        transientState.update { it.copy(expenseComment = comment.take(80)) }
    }

    fun confirmExpense(): Boolean {
        val active = uiState.value.activeBudget ?: return false
        val amount = transientState.value.keypadInput.toDoubleOrNull() ?: return false
        if (amount <= 0.0) return false
        val comment = transientState.value.expenseComment
        val targetEpochDay = uiState.value.selectedEpochDay
        val targetSubId = uiState.value.activeHomeSubBudgetId

        viewModelScope.launch {
            repository.addExpense(
                budgetId = active.id,
                subBudgetId = targetSubId,
                amount = amount,
                comment = comment,
                epochDay = targetEpochDay
            )
            transientState.update { it.copy(keypadInput = "", expenseComment = "") }
        }
        return true
    }

    fun updateExpense(expense: ExpenseEntity, newAmount: Double, newComment: String) {
        if (newAmount <= 0.0) return
        viewModelScope.launch {
            repository.updateExpense(
                expense.copy(amount = newAmount, comment = newComment.trim())
            )
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            transientState.update { it.copy(lastDeletedExpense = expense) }
        }
    }

    fun undoDeleteExpense() {
        val toRestore = transientState.value.lastDeletedExpense ?: return
        viewModelScope.launch {
            repository.restoreExpense(toRestore)
            transientState.update { it.copy(lastDeletedExpense = null) }
        }
    }

    // Borrows & Lends connected to active Monthly Budget
    fun addDebt(
        personName: String,
        amount: Double,
        type: DebtType,
        note: String,
        epochDay: Long,
        countInMainBudget: Boolean
    ) {
        if (personName.isBlank() || amount <= 0.0) return
        val budgetId = uiState.value.activeBudget?.id
        viewModelScope.launch {
            repository.addDebt(
                budgetId = budgetId,
                personName = personName,
                amount = amount,
                type = type,
                note = note,
                epochDay = epochDay,
                countInMainBudget = countInMainBudget
            )
        }
    }

    fun toggleDebtSettled(debt: DebtEntity) {
        viewModelScope.launch {
            repository.toggleDebtSettled(debt)
        }
    }

    fun toggleDebtCountInBudget(debt: DebtEntity) {
        viewModelScope.launch {
            repository.toggleDebtCountInBudget(debt)
        }
    }

    fun deleteDebt(debt: DebtEntity) {
        viewModelScope.launch {
            repository.deleteDebt(debt)
        }
    }

    // Extras connected to Main Budget or Sub-Budget (Add or Subtract)
    fun addExtra(
        title: String,
        amount: Double,
        dueDay: Int,
        operation: ReminderOperation,
        targetSubBudgetId: Long?,
        countInMainBudget: Boolean
    ): String {
        if (title.isBlank() || amount <= 0.0) return ""
        val active = uiState.value.activeBudget
        val targetSub = targetSubBudgetId?.let { id ->
            uiState.value.subBudgets.firstOrNull { it.id == id }
        }
        val targetLabel = targetSub?.name ?: active?.name ?: "Main Budget"

        viewModelScope.launch {
            repository.addExtra(
                budgetId = active?.id,
                title = title,
                amount = amount,
                dueDayOfMonth = dueDay,
                operation = operation,
                targetSubBudgetId = targetSubBudgetId,
                countInMainBudget = countInMainBudget
            )
        }
        return if (!countInMainBudget) {
            "Saved to Extras"
        } else if (operation == ReminderOperation.ADD) {
            "Added to $targetLabel"
        } else {
            "Subtracted from $targetLabel"
        }
    }

    fun toggleExtraCountInBudget(extra: MonthlyBillEntity) {
        viewModelScope.launch {
            repository.toggleExtraCountInBudget(extra)
        }
    }

    fun deleteExtra(extra: MonthlyBillEntity) {
        viewModelScope.launch {
            repository.deleteExtra(extra)
        }
    }

    fun resetCurrentBudgetExpenses() {
        val active = uiState.value.activeBudget ?: return
        viewModelScope.launch {
            repository.resetCurrentBudgetExpenses(active)
            transientState.update {
                it.copy(
                    keypadInput = "",
                    expenseComment = "",
                    lastDeletedExpense = null
                )
            }
        }
    }

    fun resetAllAppData() {
        viewModelScope.launch {
            repository.resetAllData()
            transientState.update {
                TransientState(selectedEpochDay = DateUtils.todayEpochDay())
            }
        }
    }

    suspend fun generateCsvContent(): String? {
        val active = uiState.value.activeBudget ?: return null
        return repository.buildCsvForBudget(active)
    }

    class Factory(private val repository: BudgetRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BudgetViewModel(repository) as T
        }
    }
}
