package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.BudgetEntity
import com.example.data.DebtEntity
import com.example.data.DebtType
import com.example.data.MonthlyBillEntity
import com.example.data.ReminderOperation
import com.example.data.SubBudgetEntity
import com.example.ui.theme.BuckwheatThemeTokens
import com.example.util.CurrencyUtils
import com.example.util.DateUtils
import kotlin.math.abs

@Composable
fun SwipeUpHubContent(
    activeBudget: BudgetEntity,
    allActiveBudgets: List<BudgetEntity>,
    subBudgets: List<SubBudgetEntity>,
    spentByBudgetId: Map<Long, Double>,
    spentBySubBudgetId: Map<Long, Double>,
    extrasAdjustmentBySubId: Map<Long, Double>,
    debtMainBudgetDelta: Double,
    extrasMainBudgetDelta: Double,
    effectiveMainBudget: Double,
    debts: List<DebtEntity>,
    extras: List<MonthlyBillEntity>,
    currencySymbol: String,
    onSelectBudgetForHome: (Long) -> Unit,
    onToggleShowMainOnHome: () -> Unit,
    onEditMainBudget: () -> Unit,
    onRenameActiveBudget: (String) -> Unit,
    onAddSubBudget: (name: String, amount: Double) -> Unit,
    onToggleSubBudgetOnHome: (SubBudgetEntity) -> Unit,
    onDeleteSubBudget: (SubBudgetEntity) -> Unit,
    onCreateQuickMonthlyBudget: (name: String, amount: Double, selectOnHome: Boolean) -> Unit,
    onAddDebt: (personName: String, amount: Double, type: DebtType, note: String, epochDay: Long, countInMain: Boolean) -> Unit,
    onToggleDebtSettled: (DebtEntity) -> Unit,
    onToggleDebtCountInBudget: (DebtEntity) -> Unit,
    onDeleteDebt: (DebtEntity) -> Unit,
    onAddExtra: (title: String, amount: Double, dueDay: Int, op: ReminderOperation, targetSubId: Long?, countInMain: Boolean) -> Unit,
    onToggleExtraCountInBudget: (MonthlyBillEntity) -> Unit,
    onDeleteExtra: (MonthlyBillEntity) -> Unit,
    onCollapse: () -> Unit,
    headerDragModifier: Modifier = Modifier,
    modifier: Modifier = Modifier
) {
    val colors = BuckwheatThemeTokens.colors
    var selectedTab by remember { mutableIntStateOf(0) } // 0=Monthly, 1=Borrow, 2=Extras

    var showAddSubDialog by remember { mutableStateOf(false) }
    var presetSubName by remember { mutableStateOf("") }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showNewMonthlyDialog by remember { mutableStateOf(false) }
    var showAddDebtDialog by remember { mutableStateOf(false) }
    var showAddExtraDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bgDark)
            .navigationBarsPadding()
    ) {
        // Minimal draggable top header (handle + context title + close)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(headerDragModifier)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onCollapse)
                    .padding(top = 12.dp, bottom = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(colors.handleColor)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 12.dp, top = 2.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (selectedTab) {
                            0 -> "Monthly Budget"
                            1 -> "Borrow & Lend"
                            else -> "Extras"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 19.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = colors.onDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (selectedTab != 0) {
                        Text(
                            text = "Connected to ${activeBudget.name}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = colors.onDarkSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = onCollapse,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("hub_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.close_cd),
                        tint = colors.onDarkSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Scrollable Hub Content
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.weight(1f),
            label = "hub_tab_content"
        ) { tab ->
            when (tab) {
                0 -> MinimalMonthlyBudgetTab(
                    activeBudget = activeBudget,
                    allActiveBudgets = allActiveBudgets,
                    subBudgets = subBudgets,
                    mainSpent = spentByBudgetId[activeBudget.id] ?: 0.0,
                    spentBySubBudgetId = spentBySubBudgetId,
                    extrasAdjustmentBySubId = extrasAdjustmentBySubId,
                    debtMainBudgetDelta = debtMainBudgetDelta,
                    extrasMainBudgetDelta = extrasMainBudgetDelta,
                    effectiveMainBudget = effectiveMainBudget,
                    currencySymbol = currencySymbol,
                    onSelectBudget = onSelectBudgetForHome,
                    onToggleShowMain = onToggleShowMainOnHome,
                    onEditMainBudget = {
                        onCollapse()
                        onEditMainBudget()
                    },
                    onRenameClick = { showRenameDialog = true },
                    onNewMonthlyClick = { showNewMonthlyDialog = true },
                    onToggleSubOnHome = onToggleSubBudgetOnHome,
                    onDeleteSub = onDeleteSubBudget,
                    onAddSubClick = { preset ->
                        presetSubName = preset
                        showAddSubDialog = true
                    }
                )

                1 -> MinimalBorrowsLendsTab(
                    mainBudgetName = activeBudget.name,
                    debts = debts,
                    debtMainBudgetDelta = debtMainBudgetDelta,
                    currencySymbol = currencySymbol,
                    onToggleSettled = onToggleDebtSettled,
                    onToggleCountInBudget = onToggleDebtCountInBudget,
                    onDeleteDebt = onDeleteDebt,
                    onAddClick = { showAddDebtDialog = true }
                )

                else -> MinimalExtrasTab(
                    mainBudgetName = activeBudget.name,
                    subBudgets = subBudgets,
                    extras = extras,
                    extrasMainBudgetDelta = extrasMainBudgetDelta,
                    currencySymbol = currencySymbol,
                    onToggleCountInBudget = onToggleExtraCountInBudget,
                    onDeleteExtra = onDeleteExtra,
                    onAddClick = { showAddExtraDialog = true }
                )
            }
        }

        // Bottom 3-Tab Navigation Bar: Monthly · Borrow · Extras
        HorizontalDivider(color = colors.divider.copy(alpha = 0.7f), thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.bgDark)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HubBottomTabItem(
                label = "Monthly",
                icon = Icons.Outlined.AccountBalanceWallet,
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                modifier = Modifier
                    .weight(1f)
                    .testTag("hub_tab_budgets")
            )
            HubBottomTabItem(
                label = "Borrow",
                icon = Icons.Outlined.SwapHoriz,
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                modifier = Modifier
                    .weight(1f)
                    .testTag("hub_tab_debts")
            )
            HubBottomTabItem(
                label = "Extras",
                icon = Icons.Outlined.AutoAwesome,
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                modifier = Modifier
                    .weight(1f)
                    .testTag("hub_tab_extras")
            )
        }
    }

    if (showAddSubDialog) {
        AddSubBudgetDialog(
            initialName = presetSubName,
            currencySymbol = currencySymbol,
            onConfirm = { name, amount ->
                onAddSubBudget(name, amount)
                showAddSubDialog = false
            },
            onDismiss = { showAddSubDialog = false }
        )
    }

    if (showRenameDialog) {
        RenameBudgetDialog(
            currentName = activeBudget.name,
            onConfirm = { newName ->
                onRenameActiveBudget(newName)
                showRenameDialog = false
            },
            onDismiss = { showRenameDialog = false }
        )
    }

    if (showNewMonthlyDialog) {
        AddMonthlyBudgetDialog(
            currencySymbol = currencySymbol,
            onConfirm = { name, amount ->
                onCreateQuickMonthlyBudget(name, amount, true)
                showNewMonthlyDialog = false
            },
            onDismiss = { showNewMonthlyDialog = false }
        )
    }

    if (showAddDebtDialog) {
        AddDebtDialog(
            mainBudgetName = activeBudget.name,
            onConfirm = { person, amount, type, note, epochDay, countInMain ->
                onAddDebt(person, amount, type, note, epochDay, countInMain)
                showAddDebtDialog = false
            },
            onDismiss = { showAddDebtDialog = false }
        )
    }

    if (showAddExtraDialog) {
        AddExtraDialog(
            mainBudgetName = activeBudget.name,
            subBudgets = subBudgets,
            onConfirm = { title, amount, dueDay, op, targetSubId, countInMain ->
                onAddExtra(title, amount, dueDay, op, targetSubId, countInMain)
                showAddExtraDialog = false
            },
            onDismiss = { showAddExtraDialog = false }
        )
    }
}

@Composable
private fun HubBottomTabItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = BuckwheatThemeTokens.colors
    Row(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) colors.blue else colors.cardSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) colors.onBlue else colors.onDarkSecondary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            ),
            color = if (selected) colors.onBlue else colors.onDarkSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MinimalMonthlyBudgetTab(
    activeBudget: BudgetEntity,
    allActiveBudgets: List<BudgetEntity>,
    subBudgets: List<SubBudgetEntity>,
    mainSpent: Double,
    spentBySubBudgetId: Map<Long, Double>,
    extrasAdjustmentBySubId: Map<Long, Double>,
    debtMainBudgetDelta: Double,
    extrasMainBudgetDelta: Double,
    effectiveMainBudget: Double,
    currencySymbol: String,
    onSelectBudget: (Long) -> Unit,
    onToggleShowMain: () -> Unit,
    onEditMainBudget: () -> Unit,
    onRenameClick: () -> Unit,
    onNewMonthlyClick: () -> Unit,
    onToggleSubOnHome: (SubBudgetEntity) -> Unit,
    onDeleteSub: (SubBudgetEntity) -> Unit,
    onAddSubClick: (String) -> Unit
) {
    val colors = BuckwheatThemeTokens.colors
    val mainLeft = effectiveMainBudget - mainSpent
    val allocatedTotal = subBudgets.sumOf { it.allocatedAmount + (extrasAdjustmentBySubId[it.id] ?: 0.0) }
    val existingNames = remember(subBudgets) { subBudgets.map { it.name.lowercase() }.toSet() }
    val remainingPresets = remember(existingNames) {
        listOf("Food", "Transport", "Snacks", "Coffee", "Shopping")
            .filterNot { it.lowercase() in existingNames }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Main Budgets Switcher + Add More Main Budgets pill
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    allActiveBudgets.forEach { b ->
                        val selected = b.id == activeBudget.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (selected) colors.blue else colors.cardSurface)
                                .clickable { onSelectBudget(b.id) }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = b.name,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                                ),
                                color = if (selected) colors.onBlue else colors.onDarkSecondary
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(colors.cardSurface)
                            .border(1.dp, colors.divider, RoundedCornerShape(50))
                            .clickable(onClick = onNewMonthlyClick)
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                            .testTag("hub_add_monthly_budget_btn"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Add main budget",
                            tint = colors.blueRing,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add budget",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.blueRing
                        )
                    }
                }
            }

            // Main Budget Minimal Card (shows live effective total + Borrow/Extras connections)
            item {
                val mainSelected = activeBudget.showMainOnHome || subBudgets.none { it.isShownOnHome }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(colors.panelSurface)
                        .border(
                            width = if (mainSelected) 1.dp else 0.dp,
                            color = if (mainSelected) colors.blueRing.copy(alpha = 0.65f) else colors.panelSurface,
                            shape = RoundedCornerShape(22.dp)
                        )
                        .clickable(onClick = onToggleShowMain)
                        .padding(horizontal = 14.dp, vertical = 15.dp)
                        .testTag("hub_main_budget_row")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (mainSelected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                            contentDescription = "Show main budget on Home",
                            tint = if (mainSelected) colors.blueRing else colors.onDarkSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activeBudget.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 17.sp
                                ),
                                color = colors.onDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.clickable(onClick = onRenameClick)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${DateUtils.formatDayMonth(activeBudget.startDateEpochDay)} – ${DateUtils.formatDayMonth(activeBudget.finishDateEpochDay)}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = colors.onDarkSecondary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = CurrencyUtils.formatAmount(mainLeft, currencySymbol),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (mainLeft < 0) colors.redText else colors.greenText
                            )
                            Text(
                                text = "/ ${CurrencyUtils.formatAmount(effectiveMainBudget, currencySymbol)}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = colors.onDarkSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onEditMainBudget,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("hub_edit_main_budget_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Edit main budget",
                                tint = colors.blueRing,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }

                    // Show connected Borrow/Lend & Extras adjustments live when active
                    if (abs(debtMainBudgetDelta) >= 0.005 || abs(extrasMainBudgetDelta) >= 0.005) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ConnectionTagPill(
                                text = "Base ${CurrencyUtils.formatAmount(activeBudget.startingBudget, currencySymbol)}",
                                bg = colors.cardSurface,
                                fg = colors.onDarkSecondary
                            )
                            if (abs(debtMainBudgetDelta) >= 0.005) {
                                val positive = debtMainBudgetDelta > 0
                                ConnectionTagPill(
                                    text = "Borrow/Lend ${formatSigned(debtMainBudgetDelta, currencySymbol)}",
                                    bg = if (positive) colors.greenTrack else colors.redTrack,
                                    fg = if (positive) colors.greenText else colors.redText
                                )
                            }
                            if (abs(extrasMainBudgetDelta) >= 0.005) {
                                val positive = extrasMainBudgetDelta > 0
                                ConnectionTagPill(
                                    text = "Extras ${formatSigned(extrasMainBudgetDelta, currencySymbol)}",
                                    bg = if (positive) colors.greenTrack else colors.redTrack,
                                    fg = if (positive) colors.greenText else colors.redText
                                )
                            }
                        }
                    }
                }
            }

            // Split Sub-Budgets Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Split sub-budgets · Tap to show on Home",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onDarkSecondary
                    )
                    if (allocatedTotal > 0.0) {
                        Text(
                            text = "${CurrencyUtils.formatAmount(allocatedTotal, currencySymbol)} split",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onDarkSecondary
                        )
                    }
                }
            }

            // Sub-Budget List
            items(subBudgets, key = { it.id }) { sub ->
                val spent = spentBySubBudgetId[sub.id] ?: 0.0
                val extraAdj = extrasAdjustmentBySubId[sub.id] ?: 0.0
                val effectiveAlloc = sub.allocatedAmount + extraAdj
                val left = effectiveAlloc - spent
                val ratio = if (effectiveAlloc > 0.0) {
                    (left / effectiveAlloc).toFloat().coerceIn(0f, 1f)
                } else 0f

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.cardSurface)
                        .border(
                            width = if (sub.isShownOnHome) 1.dp else 0.dp,
                            color = if (sub.isShownOnHome) colors.blueRing.copy(alpha = 0.55f) else colors.cardSurface,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clickable { onToggleSubOnHome(sub) }
                        .padding(start = 14.dp, end = 6.dp, top = 12.dp, bottom = 12.dp)
                        .testTag("hub_sub_budget_${sub.id}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (sub.isShownOnHome) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                        contentDescription = "Show on Home",
                        tint = if (sub.isShownOnHome) colors.blueRing else colors.onDarkSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sub.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp
                                ),
                                color = colors.onDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${CurrencyUtils.formatAmount(left, currencySymbol)} / ${CurrencyUtils.formatAmount(effectiveAlloc, currencySymbol)}",
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                                color = if (left < 0) colors.redText else colors.greenText
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(colors.panelSurface)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(ratio)
                                    .clip(RoundedCornerShape(50))
                                    .background(if (left < 0) colors.redText else colors.greenText)
                            )
                        }
                    }

                    IconButton(
                        onClick = { onDeleteSub(sub) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = stringResource(R.string.delete),
                            tint = colors.onDarkSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Quick split preset chips + Add custom sub-budget
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    remainingPresets.forEach { preset ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(colors.cardSurface)
                                .clickable { onAddSubClick(preset) }
                                .padding(horizontal = 13.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "+ $preset",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.onDark
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(colors.blue)
                            .clickable { onAddSubClick("") }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("hub_add_sub_budget_btn")
                    ) {
                        Text(
                            text = "+ Add sub-budget",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.onBlue
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MinimalBorrowsLendsTab(
    mainBudgetName: String,
    debts: List<DebtEntity>,
    debtMainBudgetDelta: Double,
    currencySymbol: String,
    onToggleSettled: (DebtEntity) -> Unit,
    onToggleCountInBudget: (DebtEntity) -> Unit,
    onDeleteDebt: (DebtEntity) -> Unit,
    onAddClick: () -> Unit
) {
    val colors = BuckwheatThemeTokens.colors
    val totalBorrowed = debts
        .filter { !it.isSettled && DebtType.fromKey(it.type) == DebtType.BORROWED }
        .sumOf { it.amount }
    val totalLent = debts
        .filter { !it.isSettled && DebtType.fromKey(it.type) == DebtType.LENT }
        .sumOf { it.amount }

    Column(modifier = Modifier.fillMaxSize()) {
        // Inline Borrowed / Lent summary
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.redTrack)
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Borrowed", style = MaterialTheme.typography.labelMedium, color = colors.redSubText)
                Text(
                    text = CurrencyUtils.formatAmount(totalBorrowed, currencySymbol),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.redText
                )
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.greenTrack)
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Lent", style = MaterialTheme.typography.labelMedium, color = colors.greenSubText)
                Text(
                    text = CurrencyUtils.formatAmount(totalLent, currencySymbol),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.greenText
                )
            }
        }

        if (abs(debtMainBudgetDelta) >= 0.005) {
            val positive = debtMainBudgetDelta > 0
            Text(
                text = "Counted in $mainBudgetName: ${formatSigned(debtMainBudgetDelta, currencySymbol)}",
                style = MaterialTheme.typography.labelMedium,
                color = if (positive) colors.greenText else colors.redText,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp)
            )
        }

        if (debts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No borrows or lends for $mainBudgetName",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onDarkSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(debts, key = { it.id }) { debt ->
                    val isBorrowed = DebtType.fromKey(debt.type) == DebtType.BORROWED
                    val accent = if (isBorrowed) colors.redText else colors.greenText
                    val displayEpochDay = if (debt.epochDay > 0L) debt.epochDay
                    else DateUtils.timestampToEpochDay(debt.createdAt)
                    val impactSign = if (isBorrowed) "+" else "−"

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(colors.cardSurface)
                            .padding(start = 10.dp, end = 6.dp, top = 10.dp, bottom = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { onToggleSettled(debt) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (debt.isSettled) Icons.Rounded.CheckCircle
                                    else Icons.Rounded.RadioButtonUnchecked,
                                    contentDescription = "Toggle settled",
                                    tint = if (debt.isSettled) colors.greenText else colors.onDarkSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = debt.personName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textDecoration = if (debt.isSettled) TextDecoration.LineThrough else null
                                    ),
                                    color = if (debt.isSettled) colors.onDarkSecondary else colors.onDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val meta = buildString {
                                    append(if (isBorrowed) "Borrowed" else "Lent")
                                    append(" · ")
                                    append(DateUtils.formatWeekdayDate(displayEpochDay))
                                    if (debt.note.isNotEmpty()) {
                                        append(" · ")
                                        append(debt.note)
                                    }
                                }
                                Text(
                                    text = meta,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                    color = colors.onDarkSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Text(
                                text = CurrencyUtils.formatAmount(debt.amount, currencySymbol),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = if (debt.isSettled) TextDecoration.LineThrough else null
                                ),
                                color = if (debt.isSettled) colors.onDarkSecondary else accent
                            )

                            IconButton(
                                onClick = { onDeleteDebt(debt) },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = stringResource(R.string.delete),
                                    tint = colors.onDarkSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Selectable "Count in main budget" toggle pill (user's choice)
                        if (!debt.isSettled) {
                            val active = debt.countInMainBudget
                            Row(
                                modifier = Modifier
                                    .padding(start = 36.dp, top = 4.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(if (active) colors.blue.copy(alpha = 0.22f) else colors.panelSurface)
                                    .border(
                                        width = 1.dp,
                                        color = if (active) colors.blueRing.copy(alpha = 0.6f) else colors.divider,
                                        shape = RoundedCornerShape(50)
                                    )
                                    .clickable { onToggleCountInBudget(debt) }
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = if (active) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                    contentDescription = "Count in main budget",
                                    tint = if (active) colors.blueRing else colors.onDarkSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (active) {
                                        "In main budget ($impactSign${CurrencyUtils.formatAmount(debt.amount, currencySymbol)})"
                                    } else {
                                        "Count in main budget"
                                    },
                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                                    color = if (active) colors.blueRing else colors.onDarkSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(colors.blue)
                .clickable(onClick = onAddClick)
                .testTag("hub_add_debt_btn"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null,
                tint = colors.onBlue,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Add Borrow / Lend",
                style = MaterialTheme.typography.labelLarge,
                color = colors.onBlue
            )
        }
    }
}

@Composable
private fun MinimalExtrasTab(
    mainBudgetName: String,
    subBudgets: List<SubBudgetEntity>,
    extras: List<MonthlyBillEntity>,
    extrasMainBudgetDelta: Double,
    currencySymbol: String,
    onToggleCountInBudget: (MonthlyBillEntity) -> Unit,
    onDeleteExtra: (MonthlyBillEntity) -> Unit,
    onAddClick: () -> Unit
) {
    val colors = BuckwheatThemeTokens.colors
    val subNamesById = remember(subBudgets) { subBudgets.associate { it.id to it.name } }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Add or subtract with main budget",
                style = MaterialTheme.typography.labelMedium,
                color = colors.onDarkSecondary
            )
            if (abs(extrasMainBudgetDelta) >= 0.005) {
                val positive = extrasMainBudgetDelta > 0
                Text(
                    text = formatSigned(extrasMainBudgetDelta, currencySymbol),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (positive) colors.greenText else colors.redText
                )
            }
        }

        if (extras.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Add extra income (+), bills (−), or notes",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onDarkSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(extras, key = { it.id }) { extra ->
                    val op = ReminderOperation.fromKey(extra.operationType)
                    val isAdd = op == ReminderOperation.ADD
                    val targetName = extra.targetSubBudgetId?.let { subNamesById[it] } ?: mainBudgetName
                    val opColor = if (isAdd) colors.greenText else colors.redText
                    val opBg = if (isAdd) colors.greenTrack else colors.redTrack
                    val sign = if (isAdd) "+" else "−"
                    val counted = extra.countInMainBudget

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(colors.cardSurface)
                            .border(
                                width = if (counted) 1.dp else 0.dp,
                                color = if (counted) opColor.copy(alpha = 0.40f) else colors.cardSurface,
                                shape = RoundedCornerShape(18.dp)
                            )
                            .clickable { onToggleCountInBudget(extra) }
                            .padding(start = 10.dp, end = 6.dp, top = 11.dp, bottom = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onToggleCountInBudget(extra) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (counted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                contentDescription = "Toggle count in budget",
                                tint = if (counted) opColor else colors.onDarkSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = extra.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = if (counted) colors.onDark else colors.onDarkSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = buildString {
                                    append(if (counted) "Counted · " else "Not counted · ")
                                    append(targetName)
                                    append(" · Day ${extra.dueDayOfMonth}")
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = colors.onDarkSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (counted) opBg else colors.panelSurface)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$sign${CurrencyUtils.formatAmount(extra.amount, currencySymbol)}",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (counted) opColor else colors.onDarkSecondary
                            )
                        }

                        IconButton(
                            onClick = { onDeleteExtra(extra) },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = stringResource(R.string.delete),
                                tint = colors.onDarkSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(colors.blue)
                .clickable(onClick = onAddClick)
                .testTag("hub_add_extra_btn"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null,
                tint = colors.onBlue,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Add Extra",
                style = MaterialTheme.typography.labelLarge,
                color = colors.onBlue
            )
        }
    }
}

@Composable
private fun ConnectionTagPill(
    text: String,
    bg: androidx.compose.ui.graphics.Color,
    fg: androidx.compose.ui.graphics.Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
            color = fg
        )
    }
}

private fun formatSigned(value: Double, currencySymbol: String): String {
    val sign = if (value >= 0) "+" else "−"
    return "$sign${CurrencyUtils.formatAmount(abs(value), currencySymbol)}"
}
