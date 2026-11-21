package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.BudgetRepository
import com.example.data.ExpenseEntity
import com.example.data.RestMode
import com.example.ui.BudgetViewModel
import com.example.ui.screens.BudgetOverviewSheet
import com.example.ui.screens.BudgetSetupScreen
import com.example.ui.screens.CommentInputDialog
import com.example.ui.screens.CurrencySelectorSheet
import com.example.ui.screens.EditExpenseDialog
import com.example.ui.screens.FinishEarlyConfirmDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.RestModeSelectorSheet
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.BuckwheatTheme
import com.example.ui.theme.BuckwheatThemeTokens
import com.example.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )

        val database = AppDatabase.getInstance(applicationContext)
        val repository = BudgetRepository(database.budgetDao())

        setContent {
            val vm: BudgetViewModel = viewModel(
                factory = BudgetViewModel.Factory(repository)
            )
            val uiState by vm.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(uiState.themeMode) {
                if (uiState.themeMode.isLight) {
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                        navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    )
                } else {
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
                        navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
                    )
                }
            }

            BuckwheatTheme(themeMode = uiState.themeMode) {
                BuckwheatAppRoot(viewModel = vm)
            }
        }
    }
}

@Composable
private fun BuckwheatAppRoot(
    viewModel: BudgetViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = BuckwheatThemeTokens.colors
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showOnboardingManually by remember { mutableStateOf(false) }
    var showBudgetOverview by remember { mutableStateOf(false) }
    var showEditBudgetSetup by remember { mutableStateOf(false) }
    var showSettingsScreen by remember { mutableStateOf(false) }
    var showRestSheet by remember { mutableStateOf(false) }
    var showCurrencySheet by remember { mutableStateOf(false) }
    var showFinishConfirm by remember { mutableStateOf(false) }
    var showCommentDialog by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }

    val csvExportedText = context.getString(R.string.csv_exported)
    val csvFailedText = context.getString(R.string.csv_export_failed)
    val expenseDeletedText = context.getString(R.string.expense_deleted)
    val undoText = context.getString(R.string.undo)

    val createCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val csv = viewModel.generateCsvContent()
                val ok = if (csv != null) {
                    withContext(Dispatchers.IO) {
                        runCatching {
                            context.contentResolver.openOutputStream(uri)?.use { stream ->
                                stream.write(csv.toByteArray(Charsets.UTF_8))
                            }
                            true
                        }.getOrDefault(false)
                    }
                } else false

                snackbarHostState.showSnackbar(
                    message = if (ok) csvExportedText else csvFailedText,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgDark)
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().background(colors.bgDark))
        } else {
            val activeBudget = uiState.activeBudget
            val metrics = uiState.metrics

            when {
                !uiState.settings.hasCompletedOnboarding || showOnboardingManually -> {
                    OnboardingScreen(
                        onSetUpBudgetClick = {
                            showOnboardingManually = false
                            viewModel.completeOnboarding()
                        }
                    )
                }

                activeBudget == null || metrics == null || showEditBudgetSetup -> {
                    val isEditing = showEditBudgetSetup && activeBudget != null
                    BudgetSetupScreen(
                        isEditingExisting = isEditing,
                        initialName = if (isEditing) activeBudget?.name ?: DateUtils.currentMonthBudgetName()
                        else DateUtils.currentMonthBudgetName(),
                        initialAmount = if (isEditing) activeBudget?.startingBudget else null,
                        initialStartEpochDay = if (isEditing) {
                            activeBudget?.startDateEpochDay ?: DateUtils.todayEpochDay()
                        } else {
                            DateUtils.todayEpochDay()
                        },
                        initialFinishEpochDay = if (isEditing) activeBudget?.finishDateEpochDay else null,
                        initialRestMode = RestMode.fromKey(
                            activeBudget?.restMode ?: uiState.settings.defaultRestMode
                        ),
                        initialCurrencyCode = activeBudget?.currencyCode
                            ?: uiState.settings.defaultCurrencyCode,
                        initialCurrencySymbol = activeBudget?.currencySymbol
                            ?: uiState.settings.defaultCurrencySymbol,
                        startWeekOnMonday = uiState.settings.startWeekOnMonday,
                        canDismiss = isEditing,
                        onDismiss = { showEditBudgetSetup = false },
                        onOpenSettings = { showSettingsScreen = true },
                        onApplyBudget = { name, amount, startDay, finishDay, restMode, currCode, currSymbol ->
                            viewModel.saveBudget(
                                name = name,
                                isMonthly = true,
                                amount = amount,
                                startEpochDay = startDay,
                                finishEpochDay = finishDay,
                                restMode = restMode,
                                currencyCode = currCode,
                                currencySymbol = currSymbol,
                                isEditingExisting = isEditing
                            )
                            showEditBudgetSetup = false
                            showBudgetOverview = false
                        }
                    )
                }

                else -> {
                    HomeScreen(
                        budget = activeBudget,
                        allActiveBudgets = uiState.allActiveBudgets,
                        subBudgets = uiState.subBudgets,
                        spentByBudgetId = uiState.spentByBudgetId,
                        spentBySubBudgetId = uiState.spentBySubBudgetId,
                        extrasAdjustmentBySubId = uiState.extrasAdjustmentBySubId,
                        debtMainBudgetDelta = uiState.debtMainBudgetDelta,
                        extrasMainBudgetDelta = uiState.extrasMainBudgetDelta,
                        effectiveMainBudget = uiState.effectiveMainBudget,
                        homeBudgetChips = uiState.homeBudgetChips,
                        activeHomeSubBudgetId = uiState.activeHomeSubBudgetId,
                        metrics = metrics,
                        groupedExpenses = uiState.groupedExpenses,
                        selectedEpochDay = uiState.selectedEpochDay,
                        selectedDayExpenses = uiState.selectedDayExpenses,
                        canGoPrevDay = uiState.canGoPrevDay,
                        canGoNextDay = uiState.canGoNextDay,
                        debts = uiState.debts,
                        extras = uiState.extras,
                        keypadInput = uiState.keypadInput,
                        expenseComment = uiState.expenseComment,
                        onSelectHomeChip = viewModel::selectActiveHomeChip,
                        onShiftDay = viewModel::shiftSelectedDay,
                        onResetDayToToday = viewModel::resetSelectedDayToToday,
                        onOpenBudgetOverview = { showBudgetOverview = true },
                        onOpenSettings = { showSettingsScreen = true },
                        onDigitPress = viewModel::onDigitPressed,
                        onDecimalPress = viewModel::onDecimalPressed,
                        onBackspacePress = viewModel::onBackspacePressed,
                        onClearKeypad = viewModel::onClearKeypad,
                        onConfirmExpense = { viewModel.confirmExpense() },
                        onOpenCommentDialog = { showCommentDialog = true },
                        onClearComment = { viewModel.onCommentChanged("") },
                        onEditExpense = { editingExpense = it },
                        onDeleteExpense = { expense ->
                            viewModel.deleteExpense(expense)
                            coroutineScope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = expenseDeletedText,
                                    actionLabel = undoText,
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.undoDeleteExpense()
                                }
                            }
                        },
                        onResolveRollover = viewModel::resolveRolloverChoice,
                        onSelectBudgetForHome = viewModel::selectBudgetForHome,
                        onToggleShowMainOnHome = viewModel::toggleShowMainBudgetOnHome,
                        onEditMainBudget = { showEditBudgetSetup = true },
                        onRenameActiveBudget = viewModel::renameActiveBudget,
                        onAddSubBudget = viewModel::addSubBudget,
                        onToggleSubBudgetOnHome = viewModel::toggleSubBudgetOnHome,
                        onDeleteSubBudget = viewModel::deleteSubBudget,
                        onCreateQuickMonthlyBudget = viewModel::createQuickMonthlyBudget,
                        onAddDebt = viewModel::addDebt,
                        onToggleDebtSettled = viewModel::toggleDebtSettled,
                        onToggleDebtCountInBudget = viewModel::toggleDebtCountInBudget,
                        onDeleteDebt = viewModel::deleteDebt,
                        onAddExtra = { title, amount, dueDay, op, targetSubId, countInMain ->
                            val msg = viewModel.addExtra(
                                title = title,
                                amount = amount,
                                dueDay = dueDay,
                                operation = op,
                                targetSubBudgetId = targetSubId,
                                countInMainBudget = countInMain
                            )
                            if (msg.isNotEmpty()) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = msg,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        },
                        onToggleExtraCountInBudget = viewModel::toggleExtraCountInBudget,
                        onDeleteExtra = viewModel::deleteExtra
                    )

                    AnimatedVisibility(
                        visible = showBudgetOverview,
                        enter = fadeIn() + slideInVertically { it / 3 },
                        exit = fadeOut() + slideOutVertically { it / 3 }
                    ) {
                        BudgetOverviewSheet(
                            budget = activeBudget,
                            metrics = metrics,
                            onDismiss = { showBudgetOverview = false },
                            onEditClick = {
                                showBudgetOverview = false
                                showEditBudgetSetup = true
                            },
                            onRestClick = { showRestSheet = true },
                            onCurrencyClick = { showCurrencySheet = true },
                            onExportCsvClick = {
                                val todayStr = DateUtils.formatIsoDate(DateUtils.todayEpochDay())
                                createCsvLauncher.launch("buckwheat_budget_$todayStr.csv")
                            },
                            onFinishEarlyClick = { showFinishConfirm = true }
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showSettingsScreen,
            enter = fadeIn() + slideInHorizontally { it / 3 },
            exit = fadeOut() + slideOutHorizontally { it / 3 }
        ) {
            SettingsScreen(
                settings = uiState.settings,
                currentTheme = uiState.themeMode,
                activeBudget = uiState.activeBudget,
                finishedBudgets = uiState.finishedBudgets,
                onSelectTheme = viewModel::updateThemeMode,
                onSelectRestMode = { showRestSheet = true },
                onSelectCurrency = { showCurrencySheet = true },
                onToggleStartOfWeek = {
                    viewModel.updateStartWeekOnMonday(!uiState.settings.startWeekOnMonday)
                },
                onExportCsv = {
                    val todayStr = DateUtils.formatIsoDate(DateUtils.todayEpochDay())
                    createCsvLauncher.launch("buckwheat_budget_$todayStr.csv")
                },
                onResetCurrentBudget = {
                    viewModel.resetCurrentBudgetExpenses()
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Current budget expenses cleared",
                            duration = SnackbarDuration.Short
                        )
                    }
                },
                onResetAllData = {
                    showBudgetOverview = false
                    showEditBudgetSetup = false
                    viewModel.resetAllAppData()
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = "All local data reset",
                            duration = SnackbarDuration.Short
                        )
                    }
                },
                onOpenOnboarding = {
                    showSettingsScreen = false
                    showOnboardingManually = true
                },
                onDismiss = { showSettingsScreen = false }
            )
        }

        if (showRestSheet) {
            val currentRest = RestMode.fromKey(
                uiState.activeBudget?.restMode ?: uiState.settings.defaultRestMode
            )
            RestModeSelectorSheet(
                selectedMode = currentRest,
                onSelect = { mode ->
                    viewModel.updateRestMode(mode)
                    showRestSheet = false
                },
                onDismiss = { showRestSheet = false }
            )
        }

        if (showCurrencySheet) {
            val currentCode = uiState.activeBudget?.currencyCode
                ?: uiState.settings.defaultCurrencyCode
            CurrencySelectorSheet(
                selectedCode = currentCode,
                onSelect = { option ->
                    viewModel.updateCurrency(option.code, option.symbol)
                    showCurrencySheet = false
                },
                onDismiss = { showCurrencySheet = false }
            )
        }

        if (showFinishConfirm) {
            FinishEarlyConfirmDialog(
                onConfirm = {
                    showFinishConfirm = false
                    showBudgetOverview = false
                    viewModel.finishBudgetEarly()
                },
                onDismiss = { showFinishConfirm = false }
            )
        }

        if (showCommentDialog) {
            CommentInputDialog(
                initialComment = uiState.expenseComment,
                onConfirm = { comment ->
                    viewModel.onCommentChanged(comment)
                    showCommentDialog = false
                },
                onDismiss = { showCommentDialog = false }
            )
        }

        editingExpense?.let { target ->
            EditExpenseDialog(
                expense = target,
                onSave = { newAmount, newComment ->
                    viewModel.updateExpense(target, newAmount, newComment)
                    editingExpense = null
                },
                onDelete = {
                    editingExpense = null
                    viewModel.deleteExpense(target)
                    coroutineScope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = expenseDeletedText,
                            actionLabel = undoText,
                            duration = SnackbarDuration.Short
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.undoDeleteExpense()
                        }
                    }
                },
                onDismiss = { editingExpense = null }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = colors.cardSurface,
                contentColor = colors.onDark,
                actionColor = colors.blueRing
            )
        }
    }
}
