package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AppSettingsEntity
import com.example.data.BudgetEntity
import com.example.data.RestMode
import com.example.ui.components.CurrencyCirclesIcon
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.BuckwheatThemeTokens
import com.example.ui.theme.paletteForTheme
import com.example.util.CurrencyUtils
import com.example.util.DateUtils

@Composable
fun SettingsScreen(
    settings: AppSettingsEntity,
    currentTheme: AppThemeMode,
    activeBudget: BudgetEntity?,
    finishedBudgets: List<BudgetEntity>,
    onSelectTheme: (AppThemeMode) -> Unit,
    onSelectRestMode: () -> Unit,
    onSelectCurrency: () -> Unit,
    onToggleStartOfWeek: () -> Unit,
    onExportCsv: () -> Unit,
    onResetCurrentBudget: () -> Unit,
    onResetAllData: () -> Unit,
    onOpenOnboarding: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onDismiss)

    val colors = BuckwheatThemeTokens.colors
    var showPastBudgets by remember { mutableStateOf(false) }
    var showResetBudgetDialog by remember { mutableStateOf(false) }
    var showResetAllDialog by remember { mutableStateOf(false) }

    val currentRestMode = RestMode.fromKey(activeBudget?.restMode ?: settings.defaultRestMode)
    val currentSymbol = activeBudget?.currencySymbol ?: settings.defaultCurrencySymbol
    val currentCode = activeBudget?.currencyCode ?: settings.defaultCurrencyCode

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bgDark)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 560.dp)
        ) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.close_cd),
                        tint = colors.onDark,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = colors.onDark
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // SECTION 1: Appearance & Themes (4 curated themes with live preview cards)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Appearance",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = colors.onDarkSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )

                    val themePairs = AppThemeMode.entries.chunked(2)
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        themePairs.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowItems.forEach { mode ->
                                    ThemePreviewCard(
                                        mode = mode,
                                        selected = mode == currentTheme,
                                        onClick = { onSelectTheme(mode) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("theme_card_${mode.storageKey}")
                                    )
                                }
                            }
                        }
                    }
                }

                // SECTION 2: Budget Preferences
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Budget Preferences",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = colors.onDarkSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(colors.cardSurface)
                    ) {
                        SetupOptionRow(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Directions,
                                    contentDescription = null,
                                    tint = colors.onDark,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            title = stringResource(R.string.rest_label),
                            trailingValue = restModeLabel(currentRestMode),
                            onClick = onSelectRestMode
                        )

                        HorizontalDivider(color = colors.divider, modifier = Modifier.padding(horizontal = 20.dp))

                        SetupOptionRow(
                            leadingIcon = {
                                CurrencyCirclesIcon(tint = colors.onDark, size = 22.dp)
                            },
                            title = stringResource(R.string.currency_label),
                            trailingValue = if (currentSymbol.isNotEmpty()) "$currentSymbol $currentCode".trim()
                            else stringResource(R.string.currency_none),
                            onClick = onSelectCurrency
                        )

                        HorizontalDivider(color = colors.divider, modifier = Modifier.padding(horizontal = 20.dp))

                        SetupOptionRow(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarMonth,
                                    contentDescription = null,
                                    tint = colors.onDark,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            title = stringResource(R.string.start_of_week),
                            trailingValue = stringResource(
                                if (settings.startWeekOnMonday) R.string.monday else R.string.sunday
                            ),
                            onClick = onToggleStartOfWeek
                        )
                    }
                }

                // SECTION 3: Offline Data & Guide
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Data & Guide",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = colors.onDarkSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(colors.cardSurface)
                    ) {
                        if (activeBudget != null) {
                            SetupOptionRow(
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Download,
                                        contentDescription = null,
                                        tint = colors.onDark,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                title = stringResource(R.string.export_to_csv),
                                trailingValue = "",
                                onClick = onExportCsv
                            )
                            HorizontalDivider(color = colors.divider, modifier = Modifier.padding(horizontal = 20.dp))
                        }

                        SetupOptionRow(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.History,
                                    contentDescription = null,
                                    tint = colors.onDark,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            title = stringResource(R.string.past_budgets),
                            trailingValue = finishedBudgets.size.toString(),
                            onClick = { showPastBudgets = !showPastBudgets }
                        )

                        if (showPastBudgets) {
                            if (finishedBudgets.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.no_past_budgets),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.onDarkSecondary,
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
                                )
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    finishedBudgets.take(8).forEach { past ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(colors.panelSurface)
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = past.name,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = colors.onDark
                                                )
                                                Text(
                                                    text = CurrencyUtils.formatAmount(
                                                        past.startingBudget,
                                                        past.currencySymbol
                                                    ),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = colors.greenText
                                                )
                                            }
                                            Text(
                                                text = "${DateUtils.formatDayMonth(past.startDateEpochDay)} → ${DateUtils.formatDayMonth(past.finishDateEpochDay)}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = colors.onDarkSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = colors.divider, modifier = Modifier.padding(horizontal = 20.dp))

                        SetupOptionRow(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.HelpOutline,
                                    contentDescription = null,
                                    tint = colors.onDark,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            title = stringResource(R.string.how_it_works),
                            trailingValue = "",
                            onClick = {
                                onDismiss()
                                onOpenOnboarding()
                            }
                        )
                    }
                }

                // SECTION 4: Reset
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Reset",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = colors.onDarkSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(colors.cardSurface)
                    ) {
                        if (activeBudget != null) {
                            SetupOptionRow(
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.RestartAlt,
                                        contentDescription = null,
                                        tint = colors.amberText,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                title = "Reset current budget",
                                trailingValue = "Clear spent",
                                onClick = { showResetBudgetDialog = true },
                                modifier = Modifier.testTag("settings_reset_current_budget")
                            )
                            HorizontalDivider(color = colors.divider, modifier = Modifier.padding(horizontal = 20.dp))
                        }

                        SetupOptionRow(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteForever,
                                    contentDescription = null,
                                    tint = colors.dangerText,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            title = "Reset all data",
                            titleColor = colors.dangerText,
                            trailingValue = "",
                            onClick = { showResetAllDialog = true },
                            modifier = Modifier.testTag("settings_reset_all_data")
                        )
                    }
                }

                // Minimal App Brand Footer with Vector Logo
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_buckwheat_logo),
                        contentDescription = stringResource(R.string.app_name),
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "${stringResource(R.string.app_name)} · Offline",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onDarkSecondary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    if (showResetBudgetDialog && activeBudget != null) {
        AlertDialog(
            onDismissRequest = { showResetBudgetDialog = false },
            containerColor = colors.cardSurface,
            title = { Text("Reset current budget?", color = colors.onDark) },
            text = {
                Text(
                    text = "This clears all logged spending for ${activeBudget.name} and restores the full budget amount. Sub-budgets and settings stay untouched.",
                    color = colors.onDarkSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetBudgetDialog = false
                        onResetCurrentBudget()
                    }
                ) {
                    Text("Reset", color = colors.amberText)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetBudgetDialog = false }) {
                    Text(stringResource(R.string.cancel), color = colors.onDarkSecondary)
                }
            }
        )
    }

    if (showResetAllDialog) {
        AlertDialog(
            onDismissRequest = { showResetAllDialog = false },
            containerColor = colors.cardSurface,
            title = { Text("Reset all data?", color = colors.onDark) },
            text = {
                Text(
                    text = "This permanently deletes all budgets, sub-budgets, expenses, borrows/lends, and extras stored on this device.",
                    color = colors.onDarkSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetAllDialog = false
                        onResetAllData()
                        onDismiss()
                    }
                ) {
                    Text("Reset all", color = colors.dangerText)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetAllDialog = false }) {
                    Text(stringResource(R.string.cancel), color = colors.onDarkSecondary)
                }
            }
        )
    }
}

@Composable
private fun ThemePreviewCard(
    mode: AppThemeMode,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColors = BuckwheatThemeTokens.colors
    val preview = paletteForTheme(mode)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(activeColors.cardSurface)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) activeColors.blueRing else activeColors.divider,
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        // Miniature UI preview matching Buckwheat's layout
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(78.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(preview.bgDark)
                .border(0.5.dp, preview.divider, RoundedCornerShape(14.dp))
        ) {
            // Mini top panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.2f)
                    .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                    .background(preview.panelSurface)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Mini date pill
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .height(7.dp)
                        .clip(RoundedCornerShape(50))
                        .background(preview.datePillBg)
                )
                // Mini green "For today" pill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(50))
                        .background(preview.greenFill)
                )
            }

            // Mini keypad dots + blue confirm
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(50))
                            .background(preview.keySurface)
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(18.dp)
                        .clip(RoundedCornerShape(50))
                        .background(preview.blue)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = mode.displayName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = activeColors.onDark
            )

            if (selected) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(activeColors.blue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = activeColors.onBlue,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
