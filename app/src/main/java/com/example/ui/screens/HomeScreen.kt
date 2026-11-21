package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.BudgetEntity
import com.example.data.DebtEntity
import com.example.data.DebtType
import com.example.data.ExpenseEntity
import com.example.data.MonthlyBillEntity
import com.example.data.ReminderOperation
import com.example.data.SubBudgetEntity
import com.example.ui.BudgetHealth
import com.example.ui.BudgetMetrics
import com.example.ui.DailyExpenseGroup
import com.example.ui.HomeBudgetChip
import com.example.ui.theme.BuckwheatThemeTokens
import com.example.util.CurrencyUtils
import com.example.util.DateUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun HomeScreen(
    budget: BudgetEntity,
    allActiveBudgets: List<BudgetEntity>,
    subBudgets: List<SubBudgetEntity>,
    spentByBudgetId: Map<Long, Double>,
    spentBySubBudgetId: Map<Long, Double>,
    extrasAdjustmentBySubId: Map<Long, Double>,
    debtMainBudgetDelta: Double,
    extrasMainBudgetDelta: Double,
    effectiveMainBudget: Double,
    homeBudgetChips: List<HomeBudgetChip>,
    activeHomeSubBudgetId: Long?,
    metrics: BudgetMetrics,
    groupedExpenses: List<DailyExpenseGroup>,
    selectedEpochDay: Long,
    selectedDayExpenses: List<ExpenseEntity>,
    canGoPrevDay: Boolean,
    canGoNextDay: Boolean,
    debts: List<DebtEntity>,
    extras: List<MonthlyBillEntity>,
    keypadInput: String,
    expenseComment: String,
    onSelectHomeChip: (Long?) -> Unit,
    onShiftDay: (Int) -> Unit,
    onResetDayToToday: () -> Unit,
    onOpenBudgetOverview: () -> Unit,
    onOpenSettings: () -> Unit,
    onDigitPress: (Char) -> Unit,
    onDecimalPress: () -> Unit,
    onBackspacePress: () -> Unit,
    onClearKeypad: () -> Unit,
    onConfirmExpense: () -> Unit,
    onOpenCommentDialog: () -> Unit,
    onClearComment: () -> Unit,
    onEditExpense: (ExpenseEntity) -> Unit,
    onDeleteExpense: (ExpenseEntity) -> Unit,
    onResolveRollover: (Boolean) -> Unit,
    onSelectBudgetForHome: (Long) -> Unit,
    onToggleShowMainOnHome: () -> Unit,
    onEditMainBudget: () -> Unit,
    onRenameActiveBudget: (String) -> Unit,
    onAddSubBudget: (String, Double) -> Unit,
    onToggleSubBudgetOnHome: (SubBudgetEntity) -> Unit,
    onDeleteSubBudget: (SubBudgetEntity) -> Unit,
    onCreateQuickMonthlyBudget: (String, Double, Boolean) -> Unit,
    onAddDebt: (String, Double, DebtType, String, Long, Boolean) -> Unit,
    onToggleDebtSettled: (DebtEntity) -> Unit,
    onToggleDebtCountInBudget: (DebtEntity) -> Unit,
    onDeleteDebt: (DebtEntity) -> Unit,
    onAddExtra: (String, Double, Int, ReminderOperation, Long?, Boolean) -> Unit,
    onToggleExtraCountInBudget: (MonthlyBillEntity) -> Unit,
    onDeleteExtra: (MonthlyBillEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = BuckwheatThemeTokens.colors
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    var panelProgress by remember { mutableFloatStateOf(0f) }
    var dragStartAnchor by remember { mutableFloatStateOf(0f) }
    var settleJob by remember { mutableStateOf<Job?>(null) }

    val smoothSpring = remember {
        spring<Float>(
            dampingRatio = 0.86f,
            stiffness = 380f
        )
    }

    fun settleTo(target: Float, initialVelocityNormalized: Float = 0f) {
        settleJob?.cancel()
        settleJob = scope.launch {
            val anim = Animatable(panelProgress)
            anim.animateTo(
                targetValue = target,
                animationSpec = smoothSpring,
                initialVelocity = initialVelocityNormalized.coerceIn(-8f, 8f)
            ) {
                panelProgress = value
            }
        }
    }

    BackHandler(enabled = abs(panelProgress) > 0.25f) {
        settleTo(0f)
    }

    val symbol = budget.currencySymbol
    val activeChipName = homeBudgetChips.firstOrNull { it.subBudgetId == activeHomeSubBudgetId }?.name
        ?: budget.name

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bgDark),
        contentAlignment = Alignment.TopCenter
    ) {
        val totalHeight = maxHeight
        val collapsedPanelHeight = (totalHeight * 0.54f).coerceAtLeast(290.dp)
        val expandedHistoryHeight = (totalHeight - 28.dp).coerceAtLeast(collapsedPanelHeight)
        val historyTravelPx = with(density) { (expandedHistoryHeight - collapsedPanelHeight).toPx() }
            .coerceAtLeast(160f)
        val hubHeight = (totalHeight * 0.86f).coerceAtLeast(440.dp)
        val hubHeightPx = with(density) { hubHeight.toPx() }.coerceAtLeast(280f)
        val keypadAreaHeight = (totalHeight - collapsedPanelHeight).coerceAtLeast(240.dp)

        fun applyVerticalDelta(deltaPx: Float) {
            settleJob?.cancel()
            val current = panelProgress
            val movingInDownZone = current > 0f || (current == 0f && deltaPx > 0f)
            val travel = if (movingInDownZone) historyTravelPx else hubHeightPx
            panelProgress = (current + deltaPx / travel).coerceIn(-1f, 1f)
        }

        fun finishVerticalDrag(velocityPxPerSec: Float) {
            val v = panelProgress
            val velocityThreshold = 450f
            val target = when {
                dragStartAnchor > 0.5f -> {
                    if (velocityPxPerSec < -velocityThreshold || v < 0.74f) 0f else 1f
                }
                dragStartAnchor < -0.5f -> {
                    if (velocityPxPerSec > velocityThreshold || v > -0.74f) 0f else -1f
                }
                else -> {
                    when {
                        velocityPxPerSec > velocityThreshold && v > 0.02f -> 1f
                        velocityPxPerSec < -velocityThreshold && v < -0.02f -> -1f
                        v > 0.20f -> 1f
                        v < -0.18f -> -1f
                        else -> 0f
                    }
                }
            }

            if (target != dragStartAnchor) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            val normTravel = if (v >= 0f) historyTravelPx else hubHeightPx
            settleTo(target, velocityPxPerSec / normTravel)
        }

        val verticalDraggableState = rememberDraggableState { delta ->
            applyVerticalDelta(delta)
        }

        val verticalDragModifier = Modifier.draggable(
            state = verticalDraggableState,
            orientation = Orientation.Vertical,
            onDragStarted = {
                settleJob?.cancel()
                dragStartAnchor = when {
                    panelProgress > 0.5f -> 1f
                    panelProgress < -0.5f -> -1f
                    else -> 0f
                }
            },
            onDragStopped = { velocity ->
                finishVerticalDrag(velocity)
            }
        )

        val historyNestedScrollConnection = remember(historyTravelPx) {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    if (panelProgress > 0f && panelProgress < 0.99f && source == NestedScrollSource.UserInput) {
                        applyVerticalDelta(available.y)
                        return Offset(0f, available.y)
                    }
                    return Offset.Zero
                }

                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource
                ): Offset {
                    if (available.y < 0f && source == NestedScrollSource.UserInput) {
                        if (panelProgress >= 0.99f) dragStartAnchor = 1f
                        applyVerticalDelta(available.y)
                        return Offset(0f, available.y)
                    }
                    return Offset.Zero
                }

                override suspend fun onPreFling(available: Velocity): Velocity {
                    if (panelProgress > 0f && panelProgress < 0.99f) {
                        finishVerticalDrag(available.y)
                        return available
                    }
                    return Velocity.Zero
                }
            }
        }

        val hubNestedScrollConnection = remember(hubHeightPx) {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    if (panelProgress > -0.99f && panelProgress < 0f && source == NestedScrollSource.UserInput) {
                        applyVerticalDelta(available.y)
                        return Offset(0f, available.y)
                    }
                    return Offset.Zero
                }

                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource
                ): Offset {
                    if (available.y > 0f && source == NestedScrollSource.UserInput) {
                        if (panelProgress <= -0.99f) dragStartAnchor = -1f
                        applyVerticalDelta(available.y)
                        return Offset(0f, available.y)
                    }
                    return Offset.Zero
                }

                override suspend fun onPreFling(available: Velocity): Velocity {
                    if (panelProgress > -0.99f && panelProgress < 0f) {
                        finishVerticalDrag(available.y)
                        return available
                    }
                    return Velocity.Zero
                }
            }
        }

        val downFraction = panelProgress.coerceIn(0f, 1f)
        val upFraction = (-panelProgress).coerceIn(0f, 1f)

        // LAYER 1: Bottom Numeric Keypad
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .height(keypadAreaHeight)
                .graphicsLayer {
                    val hideFactor = maxOf(downFraction, upFraction)
                    alpha = (1f - hideFactor * 0.85f).coerceIn(0.08f, 1f)
                    val s = 1f - hideFactor * 0.04f
                    scaleX = s
                    scaleY = s
                }
                .then(verticalDragModifier)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                BuckwheatKeypad(
                    hasInput = keypadInput.isNotEmpty() && (keypadInput.toDoubleOrNull() ?: 0.0) > 0.0,
                    onDigit = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onDigitPress(it)
                    },
                    onDecimal = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onDecimalPress()
                    },
                    onBackspace = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onBackspacePress()
                    },
                    onClearAll = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClearKeypad()
                    },
                    onConfirm = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onConfirmExpense()
                    }
                )
            }
        }

        // LAYER 2: Persistent Top Rounded Panel (120fps GPU translationY)
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .height(expandedHistoryHeight)
                .graphicsLayer {
                    translationY = -(1f - downFraction) * historyTravelPx
                }
                .then(verticalDragModifier),
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
            color = colors.panelSurface,
            shadowElevation = if (colors.isLight) 8.dp else 6.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // 2A: Collapsed Home View
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(collapsedPanelHeight)
                        .graphicsLayer {
                            translationY = (1f - downFraction) * historyTravelPx
                            alpha = (1f - downFraction * 1.6f).coerceIn(0f, 1f)
                        }
                        .statusBarsPadding()
                        .padding(bottom = 44.dp)
                ) {
                    CollapsedMainPanelContent(
                        activeChipName = activeChipName,
                        isSubBudgetSelected = activeHomeSubBudgetId != null,
                        homeBudgetChips = homeBudgetChips,
                        activeHomeSubBudgetId = activeHomeSubBudgetId,
                        metrics = metrics,
                        currencySymbol = symbol,
                        selectedEpochDay = selectedEpochDay,
                        selectedDayExpenses = selectedDayExpenses,
                        canGoPrevDay = canGoPrevDay,
                        canGoNextDay = canGoNextDay,
                        keypadInput = keypadInput,
                        expenseComment = expenseComment,
                        isInteractive = downFraction < 0.25f,
                        onSelectHomeChip = { subId ->
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSelectHomeChip(subId)
                        },
                        onShiftDay = { delta ->
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onShiftDay(delta)
                        },
                        onResetDayToToday = onResetDayToToday,
                        onOpenBudgetOverview = onOpenBudgetOverview,
                        onOpenSettings = onOpenSettings,
                        onOpenCommentDialog = onOpenCommentDialog,
                        onClearComment = onClearComment,
                        onOpenHistoryForSelectedDay = { settleTo(1f) },
                        onResolveRollover = onResolveRollover
                    )
                }

                // 2B: Expanded History View
                if (downFraction > 0.02f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .padding(bottom = 44.dp)
                            .graphicsLayer {
                                alpha = ((downFraction - 0.10f) / 0.90f).coerceIn(0f, 1f)
                            }
                            .nestedScroll(historyNestedScrollConnection)
                    ) {
                        ExpandedHistoryPanelContent(
                            budget = budget,
                            activeTitle = activeChipName,
                            metrics = metrics,
                            groupedExpenses = groupedExpenses,
                            selectedEpochDay = selectedEpochDay,
                            onEditExpense = onEditExpense,
                            onDeleteExpense = onDeleteExpense
                        )
                    }
                }

                // 2C: Bottom Handle Strip
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(44.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClickLabel = stringResource(R.string.toggle_history_cd)
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            settleTo(if (downFraction > 0.5f) 0f else 1f)
                        }
                        .padding(horizontal = 24.dp)
                        .testTag("home_panel_handle"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 42.dp, height = 4.dp)
                            .clip(RoundedCornerShape(50))
                            .background(colors.handleColor)
                    )

                    if (downFraction < 0.15f && upFraction < 0.15f) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(colors.datePillBg.copy(alpha = 0.85f))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    settleTo(-1f)
                                }
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                                .testTag("home_hub_pill_button"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowUp,
                                contentDescription = "Open Monthly Hub",
                                tint = colors.onDarkSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Hub",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                                color = colors.onDarkSecondary
                            )
                        }
                    }
                }
            }
        }

        // LAYER 3: Swipe-Up Minimal Monthly Hub
        if (upFraction > 0.005f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = upFraction }
                    .background(Color.Black.copy(alpha = 0.50f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { settleTo(0f) }
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .height(hubHeight)
                    .graphicsLayer {
                        translationY = (1f - upFraction) * hubHeightPx
                    }
                    .nestedScroll(hubNestedScrollConnection),
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                color = colors.bgDark,
                shadowElevation = 14.dp
            ) {
                SwipeUpHubContent(
                    activeBudget = budget,
                    allActiveBudgets = allActiveBudgets,
                    subBudgets = subBudgets,
                    spentByBudgetId = spentByBudgetId,
                    spentBySubBudgetId = spentBySubBudgetId,
                    extrasAdjustmentBySubId = extrasAdjustmentBySubId,
                    debtMainBudgetDelta = debtMainBudgetDelta,
                    extrasMainBudgetDelta = extrasMainBudgetDelta,
                    effectiveMainBudget = effectiveMainBudget,
                    debts = debts,
                    extras = extras,
                    currencySymbol = symbol,
                    onSelectBudgetForHome = onSelectBudgetForHome,
                    onToggleShowMainOnHome = onToggleShowMainOnHome,
                    onEditMainBudget = onEditMainBudget,
                    onRenameActiveBudget = onRenameActiveBudget,
                    onAddSubBudget = onAddSubBudget,
                    onToggleSubBudgetOnHome = onToggleSubBudgetOnHome,
                    onDeleteSubBudget = onDeleteSubBudget,
                    onCreateQuickMonthlyBudget = onCreateQuickMonthlyBudget,
                    onAddDebt = onAddDebt,
                    onToggleDebtSettled = onToggleDebtSettled,
                    onToggleDebtCountInBudget = onToggleDebtCountInBudget,
                    onDeleteDebt = onDeleteDebt,
                    onAddExtra = onAddExtra,
                    onToggleExtraCountInBudget = onToggleExtraCountInBudget,
                    onDeleteExtra = onDeleteExtra,
                    onCollapse = { settleTo(0f) },
                    headerDragModifier = verticalDragModifier
                )
            }
        }
    }
}

@Composable
private fun CollapsedMainPanelContent(
    activeChipName: String,
    isSubBudgetSelected: Boolean,
    homeBudgetChips: List<HomeBudgetChip>,
    activeHomeSubBudgetId: Long?,
    metrics: BudgetMetrics,
    currencySymbol: String,
    selectedEpochDay: Long,
    selectedDayExpenses: List<ExpenseEntity>,
    canGoPrevDay: Boolean,
    canGoNextDay: Boolean,
    keypadInput: String,
    expenseComment: String,
    isInteractive: Boolean,
    onSelectHomeChip: (Long?) -> Unit,
    onShiftDay: (Int) -> Unit,
    onResetDayToToday: () -> Unit,
    onOpenBudgetOverview: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCommentDialog: () -> Unit,
    onClearComment: () -> Unit,
    onOpenHistoryForSelectedDay: () -> Unit,
    onResolveRollover: (Boolean) -> Unit
) {
    val colors = BuckwheatThemeTokens.colors
    val todayEpochDay = remember { DateUtils.todayEpochDay() }
    val isViewingToday = selectedEpochDay == todayEpochDay
    val scope = rememberCoroutineScope()

    var slideDirection by remember { mutableIntStateOf(1) }
    val dateSwipeOffset = remember { Animatable(0f) }

    val trackColor: Color
    val fillColor: Color
    val amountTextColor: Color
    val labelTextColor: Color

    when (metrics.selectedDayHealth) {
        BudgetHealth.HEALTHY -> {
            trackColor = colors.greenTrack
            fillColor = colors.greenFill
            amountTextColor = colors.greenText
            labelTextColor = colors.greenSubText
        }
        BudgetHealth.WARNING -> {
            trackColor = colors.amberTrack
            fillColor = colors.amberFill
            amountTextColor = colors.amberText
            labelTextColor = colors.amberSubText
        }
        BudgetHealth.OVER -> {
            trackColor = colors.redTrack
            fillColor = colors.redFill
            amountTextColor = colors.redText
            labelTextColor = colors.redSubText
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = metrics.selectedDayProgressFraction.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 350f),
        label = "today_pill_progress"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // TOP HEADER AREA: Minimal Swipeable Date Pill + Money Pill + Optional Selected Budget Selector
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 12.dp, top = 8.dp, bottom = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Minimal Swipeable Date & Weekday Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val dateDraggableState = rememberDraggableState { delta ->
                    scope.launch {
                        val next = (dateSwipeOffset.value + delta * 0.45f).coerceIn(-48f, 48f)
                        dateSwipeOffset.snapTo(next)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .clip(RoundedCornerShape(50))
                        .background(colors.datePillBg)
                        .draggable(
                            state = dateDraggableState,
                            orientation = Orientation.Horizontal,
                            enabled = isInteractive,
                            onDragStopped = { velocity ->
                                val offset = dateSwipeOffset.value
                                when {
                                    (offset > 16f || velocity > 250f) && canGoPrevDay -> {
                                        slideDirection = -1
                                        onShiftDay(-1)
                                    }
                                    (offset < -16f || velocity < -250f) && canGoNextDay -> {
                                        slideDirection = 1
                                        onShiftDay(1)
                                    }
                                }
                                scope.launch {
                                    dateSwipeOffset.animateTo(
                                        0f,
                                        spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMedium)
                                    )
                                }
                            }
                        )
                        .padding(horizontal = 6.dp)
                        .testTag("swipeable_date_pill"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable(enabled = isInteractive && canGoPrevDay) {
                                slideDirection = -1
                                onShiftDay(-1)
                            }
                            .testTag("date_pill_prev"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            contentDescription = "Previous day",
                            tint = if (canGoPrevDay) colors.datePillText else colors.onDarkDisabled.copy(alpha = 0.35f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    AnimatedContent(
                        targetState = selectedEpochDay,
                        transitionSpec = {
                            val dir = slideDirection
                            (slideInHorizontally { it / 2 * dir } + fadeIn()) togetherWith
                                (slideOutHorizontally { -it / 2 * dir } + fadeOut())
                        },
                        modifier = Modifier
                            .weight(1f)
                            .graphicsLayer { translationX = dateSwipeOffset.value }
                            .clickable(enabled = isInteractive && !isViewingToday, onClick = onResetDayToToday),
                        label = "date_pill_text"
                    ) { day ->
                        val dateLabel = DateUtils.formatWeekdayDate(day)
                        val text = if (day == todayEpochDay) dateLabel else "$dateLabel · Today"
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = text,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = if (day == todayEpochDay) colors.datePillText else colors.blueRing,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable(enabled = isInteractive && canGoNextDay) {
                                slideDirection = 1
                                onShiftDay(1)
                            }
                            .testTag("date_pill_next"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = "Next day",
                            tint = if (canGoNextDay) colors.datePillText else colors.onDarkDisabled.copy(alpha = 0.35f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 2. Main "For today 100" Pill + Settings Gear
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(50))
                        .background(trackColor)
                        .clickable(enabled = isInteractive, onClick = onOpenBudgetOverview)
                        .testTag("for_today_pill")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedProgress)
                            .background(fillColor)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val pillLabel = when {
                            metrics.isExpired && isViewingToday -> stringResource(R.string.budget_ended)
                            metrics.remainingOnSelectedDay < -0.004 -> stringResource(R.string.over_budget_today)
                            isSubBudgetSelected && isViewingToday -> "$activeChipName · today"
                            isSubBudgetSelected -> "$activeChipName · ${DateUtils.formatDayMonth(selectedEpochDay)}"
                            isViewingToday -> stringResource(R.string.for_today)
                            else -> "For ${DateUtils.formatDayMonth(selectedEpochDay)}"
                        }

                        Text(
                            text = pillLabel,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = labelTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp)
                        )

                        Text(
                            text = CurrencyUtils.formatAmount(metrics.remainingOnSelectedDay, currencySymbol),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = amountTextColor
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onOpenSettings,
                    enabled = isInteractive,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("home_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = stringResource(R.string.settings_cd),
                        tint = colors.onDark.copy(alpha = 0.85f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // 3. Minimal Selectable Home Budget / Sub-Budget Chips (appears whenever user has >1 visible budget on Home)
            if (homeBudgetChips.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(end = 8.dp)
                        .testTag("home_budget_chips_row"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    homeBudgetChips.forEach { chip ->
                        val isSelected = chip.subBudgetId == activeHomeSubBudgetId
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isSelected) colors.blue else colors.datePillBg)
                                .clickable(enabled = isInteractive) {
                                    onSelectHomeChip(chip.subBudgetId)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("home_chip_${chip.subBudgetId ?: "main"}"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = chip.name,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                                ),
                                color = if (isSelected) colors.onBlue else colors.onDarkSecondary
                            )
                            Text(
                                text = CurrencyUtils.formatAmount(chip.remaining, currencySymbol),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (isSelected) colors.onBlue.copy(alpha = 0.85f)
                                else if (chip.remaining < 0) colors.redText else colors.greenText
                            )
                        }
                    }
                }
            }
        }

        // LARGE EMPTY SWIPEABLE CANVAS
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp)
                .testTag("home_empty_swipe_canvas"),
            contentAlignment = Alignment.Center
        ) {
            if (keypadInput.isNotEmpty()) {
                val enteredValue = keypadInput.toDoubleOrNull() ?: 0.0
                val afterRemaining = metrics.remainingOnSelectedDay - enteredValue
                val afterColor = when {
                    afterRemaining < -0.004 -> colors.redText
                    metrics.dailyAllowanceBase > 0 && afterRemaining / metrics.dailyAllowanceBase < 0.25 -> colors.amberText
                    else -> colors.greenText
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = CurrencyUtils.formatInputDisplay(keypadInput, currencySymbol),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 50.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = colors.onDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("keypad_display_amount")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val dayTargetText = if (isViewingToday) "left today" else "left on ${DateUtils.formatDayMonth(selectedEpochDay)}"
                    Text(
                        text = "After: ${CurrencyUtils.formatAmount(afterRemaining, currencySymbol)} $dayTargetText",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = afterColor
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(colors.cardSurface)
                            .clickable(enabled = isInteractive, onClick = onOpenCommentDialog)
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .testTag("add_comment_chip"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.EditNote,
                            contentDescription = null,
                            tint = colors.onDarkSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = expenseComment.ifEmpty { stringResource(R.string.add_comment) },
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = if (expenseComment.isEmpty()) colors.onDarkSecondary else colors.onDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (expenseComment.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.close_cd),
                                tint = colors.onDarkSecondary,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable(onClick = onClearComment)
                            )
                        }
                    }
                }
            } else if (isViewingToday && metrics.pendingRolloverAmount >= 0.01) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(colors.bgDark.copy(alpha = 0.72f))
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.rest_rollover_prompt_title),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onDarkSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = CurrencyUtils.formatAmount(metrics.pendingRolloverAmount, currencySymbol),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.greenText
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(50))
                                .background(colors.cardSurface)
                                .clickable { onResolveRollover(false) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.rest_split),
                                style = MaterialTheme.typography.labelLarge,
                                color = colors.onDark
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(50))
                                .background(colors.blue)
                                .clickable { onResolveRollover(true) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.rest_add_today),
                                style = MaterialTheme.typography.labelLarge,
                                color = colors.onBlue
                            )
                        }
                    }
                }
            } else if (!isViewingToday || selectedDayExpenses.isNotEmpty()) {
                val count = selectedDayExpenses.size
                val daySpent = metrics.spentOnSelectedDay
                if (count > 0) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(50))
                            .background(colors.datePillBg.copy(alpha = 0.88f))
                            .clickable(enabled = isInteractive, onClick = onOpenHistoryForSelectedDay)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$count ${if (count == 1) "entry" else "entries"} · ${CurrencyUtils.formatAmount(daySpent, currencySymbol)} spent",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onDarkSecondary
                        )
                    }
                } else if (!isViewingToday) {
                    Text(
                        text = "Enter amount to add for ${DateUtils.formatDayMonth(selectedEpochDay)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onDarkSecondary.copy(alpha = 0.72f),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandedHistoryPanelContent(
    budget: BudgetEntity,
    activeTitle: String,
    metrics: BudgetMetrics,
    groupedExpenses: List<DailyExpenseGroup>,
    selectedEpochDay: Long,
    onEditExpense: (ExpenseEntity) -> Unit,
    onDeleteExpense: (ExpenseEntity) -> Unit
) {
    val colors = BuckwheatThemeTokens.colors
    val todayEpochDay = remember { DateUtils.todayEpochDay() }
    val symbol = budget.currencySymbol

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(colors.plumCardBg)
                .padding(horizontal = 24.dp, vertical = 22.dp)
                .testTag("history_starting_budget_card")
        ) {
            Text(
                text = CurrencyUtils.formatAmount(metrics.startingBudget, symbol),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = colors.plumText
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$activeTitle · ${stringResource(R.string.starting_budget)}",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = colors.plumSubText
            )

            Spacer(modifier = Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = DateUtils.formatDayMonth(budget.startDateEpochDay),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = colors.plumText
                )

                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .height(14.dp)
                        .padding(start = 12.dp)
                ) {
                    drawLine(
                        color = colors.plumText,
                        start = Offset(0f, size.height / 2f),
                        end = Offset(size.width, size.height / 2f),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(colors.plumPillBg)
                        .padding(horizontal = 16.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${metrics.totalDays} ${if (metrics.totalDays == 1) "day" else "days"}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = colors.plumPillText
                    )
                }

                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .height(18.dp)
                        .padding(end = 12.dp)
                ) {
                    val cy = size.height / 2f
                    val strokeW = 2.dp.toPx()
                    val arrow = 7.dp.toPx()
                    drawLine(
                        color = colors.plumText,
                        start = Offset(0f, cy),
                        end = Offset(size.width - strokeW, cy),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = colors.plumText,
                        start = Offset(size.width - arrow, cy - arrow),
                        end = Offset(size.width, cy),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = colors.plumText,
                        start = Offset(size.width - arrow, cy + arrow),
                        end = Offset(size.width, cy),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                }

                Text(
                    text = DateUtils.formatDayMonth(budget.finishDateEpochDay),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = colors.plumText
                )
            }
        }

        if (groupedExpenses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_spending_yet),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = colors.onDarkSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupedExpenses.forEach { group ->
                    item(key = "header_${group.epochDay}") {
                        val dayLabel = when (group.epochDay) {
                            todayEpochDay -> stringResource(R.string.today)
                            todayEpochDay - 1L -> stringResource(R.string.yesterday)
                            else -> DateUtils.formatWeekdayDate(group.epochDay)
                        }
                        val isHighlightedDay = group.epochDay == selectedEpochDay && selectedEpochDay != todayEpochDay
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dayLabel,
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                                color = if (isHighlightedDay) colors.blueRing else colors.onDarkSecondary
                            )
                            Text(
                                text = CurrencyUtils.formatAmount(group.totalForDay, symbol),
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                                color = if (isHighlightedDay) colors.blueRing else colors.onDarkSecondary
                            )
                        }
                    }

                    items(group.expenses, key = { it.id }) { expense ->
                        ExpenseHistoryRow(
                            expense = expense,
                            currencySymbol = symbol,
                            onEdit = { onEditExpense(expense) },
                            onDelete = { onDeleteExpense(expense) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseHistoryRow(
    expense: ExpenseEntity,
    currencySymbol: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = BuckwheatThemeTokens.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.bgDark.copy(alpha = if (colors.isLight) 0.72f else 0.55f))
            .clickable(onClick = onEdit)
            .padding(start = 18.dp, end = 6.dp, top = 12.dp, bottom = 12.dp)
            .testTag("expense_row_${expense.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = CurrencyUtils.formatAmount(expense.amount, currencySymbol),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = colors.onDark
            )
            if (expense.comment.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = expense.comment,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = colors.onDarkSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Text(
            text = DateUtils.formatTime(expense.timestamp),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color = colors.onDarkSecondary
        )

        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .size(48.dp)
                .testTag("delete_expense_${expense.id}")
        ) {
            Icon(
                imageVector = Icons.Outlined.DeleteOutline,
                contentDescription = stringResource(R.string.delete_expense_cd),
                tint = colors.onDarkSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BuckwheatKeypad(
    hasInput: Boolean,
    onDigit: (Char) -> Unit,
    onDecimal: () -> Unit,
    onBackspace: () -> Unit,
    onClearAll: () -> Unit,
    onConfirm: () -> Unit
) {
    val colors = BuckwheatThemeTokens.colors
    val gap: Dp = 10.dp

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(gap)
    ) {
        Column(
            modifier = Modifier
                .weight(3f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(gap)
        ) {
            KeypadDigitRow(
                digits = listOf('7', '8', '9'),
                gap = gap,
                onDigit = onDigit,
                modifier = Modifier.weight(1f)
            )
            KeypadDigitRow(
                digits = listOf('4', '5', '6'),
                gap = gap,
                onDigit = onDigit,
                modifier = Modifier.weight(1f)
            )
            KeypadDigitRow(
                digits = listOf('1', '2', '3'),
                gap = gap,
                onDigit = onDigit,
                modifier = Modifier.weight(1f)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(gap)
            ) {
                Box(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(colors.keySurface)
                        .clickable { onDigit('0') }
                        .testTag("keypad_0"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "0",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = colors.onDark
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(colors.keySurface)
                        .clickable(onClick = onDecimal)
                        .testTag("keypad_dot"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ".",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = colors.onDark
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(gap)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(colors.backspaceSurface)
                    .combinedClickable(
                        onClick = onBackspace,
                        onLongClick = onClearAll
                    )
                    .testTag("keypad_backspace"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Backspace,
                    contentDescription = stringResource(R.string.backspace_cd),
                    tint = colors.onDark,
                    modifier = Modifier.size(26.dp)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(3f)
                    .clip(RoundedCornerShape(50))
                    .background(if (hasInput) colors.blue else colors.blue.copy(alpha = 0.82f))
                    .clickable(onClick = onConfirm)
                    .testTag("keypad_confirm"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = stringResource(R.string.confirm_expense_cd),
                    tint = colors.onBlue,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
private fun KeypadDigitRow(
    digits: List<Char>,
    gap: Dp,
    onDigit: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = BuckwheatThemeTokens.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(gap)
    ) {
        digits.forEach { d ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(colors.keySurface)
                    .clickable { onDigit(d) }
                    .testTag("keypad_$d"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = d.toString(),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = colors.onDark
                )
            }
        }
    }
}
