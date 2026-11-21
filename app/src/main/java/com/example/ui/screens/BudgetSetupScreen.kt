package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.RestMode
import com.example.ui.components.CurrencyCirclesIcon
import com.example.ui.theme.BuckwheatThemeTokens
import com.example.util.CurrencyUtils
import com.example.util.DateUtils

@Composable
fun BudgetSetupScreen(
    isEditingExisting: Boolean,
    initialName: String,
    initialAmount: Double?,
    initialStartEpochDay: Long = DateUtils.todayEpochDay(),
    initialFinishEpochDay: Long?,
    initialRestMode: RestMode,
    initialCurrencyCode: String,
    initialCurrencySymbol: String,
    startWeekOnMonday: Boolean,
    canDismiss: Boolean,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
    onApplyBudget: (
        name: String,
        amount: Double,
        startEpochDay: Long,
        finishEpochDay: Long,
        restMode: RestMode,
        currencyCode: String,
        currencySymbol: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    if (canDismiss) {
        BackHandler(onBack = onDismiss)
    }

    val colors = BuckwheatThemeTokens.colors
    val todayEpochDay = remember { DateUtils.todayEpochDay() }
    var budgetName by remember(initialName) { mutableStateOf(initialName) }
    var amountText by remember(initialAmount) {
        mutableStateOf(
            if (initialAmount != null && initialAmount > 0.0) {
                if (initialAmount % 1.0 == 0.0) initialAmount.toLong().toString()
                else initialAmount.toString()
            } else ""
        )
    }
    var startEpochDay by remember(initialStartEpochDay) {
        mutableStateOf(initialStartEpochDay)
    }
    var finishEpochDay by remember(initialFinishEpochDay, initialStartEpochDay) {
        mutableStateOf(initialFinishEpochDay?.takeIf { it >= initialStartEpochDay })
    }
    var restMode by remember(initialRestMode) { mutableStateOf(initialRestMode) }
    var currencyCode by remember(initialCurrencyCode) { mutableStateOf(initialCurrencyCode) }
    var currencySymbol by remember(initialCurrencySymbol) { mutableStateOf(initialCurrencySymbol) }

    var activeDatePickerMode by remember { mutableStateOf<DatePickerMode?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showRestDialog by remember { mutableStateOf(false) }
    var showCurrencySheet by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        if (!isEditingExisting) {
            try {
                focusRequester.requestFocus()
            } catch (_: Exception) {
            }
        }
    }

    val parsedAmount = amountText.replace(",", ".").toDoubleOrNull() ?: 0.0
    val daysCount = finishEpochDay?.let { DateUtils.inclusiveDaysBetween(startEpochDay, it) } ?: 0
    val isAmountValid = parsedAmount > 0.0
    val isDateValid = finishEpochDay != null && daysCount >= 1
    val canApply = isAmountValid && isDateValid

    activeDatePickerMode?.let { pickerMode ->
        FinishDatePickerScreen(
            initialSelectedEpochDay = if (pickerMode == DatePickerMode.START) startEpochDay else finishEpochDay,
            startWeekOnMonday = startWeekOnMonday,
            mode = pickerMode,
            rangeStartEpochDay = startEpochDay,
            rangeFinishEpochDay = finishEpochDay,
            onDismiss = { activeDatePickerMode = null },
            onConfirmDate = { chosen ->
                if (pickerMode == DatePickerMode.START) {
                    startEpochDay = chosen
                    val currentFinish = finishEpochDay
                    if (currentFinish != null && currentFinish < chosen) {
                        finishEpochDay = chosen
                    }
                } else {
                    finishEpochDay = chosen
                }
                activeDatePickerMode = null
            }
        )
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.backdropSurface)
            .statusBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 560.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (canDismiss) onDismiss() },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("setup_wallet_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccountBalanceWallet,
                        contentDescription = stringResource(R.string.wallet_cd),
                        tint = colors.onDarkSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("setup_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = stringResource(R.string.settings_cd),
                        tint = colors.onDarkSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(colors.bgDark)
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(
                            if (isEditingExisting) R.string.editing_budget_title
                            else R.string.setting_up_budget_title
                        ),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = colors.onDark,
                        textAlign = TextAlign.Center
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                focusRequester.requestFocus()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        ) {
                            if (currencySymbol.isNotEmpty() && amountText.isNotEmpty()) {
                                Text(
                                    text = currencySymbol,
                                    style = TextStyle(
                                        fontSize = 44.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.onDarkSecondary
                                    ),
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                            }
                            BasicTextField(
                                value = amountText,
                                onValueChange = { raw ->
                                    amountText = sanitizeNumericInput(raw)
                                },
                                singleLine = true,
                                cursorBrush = SolidColor(colors.blueRing),
                                textStyle = TextStyle(
                                    fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
                                    fontSize = 52.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onDark,
                                    textAlign = TextAlign.Center
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { focusManager.clearFocus() }
                                ),
                                modifier = Modifier
                                    .widthIn(min = 24.dp, max = 300.dp)
                                    .focusRequester(focusRequester)
                                    .testTag("budget_amount_input")
                            )
                        }
                    }

                    if (isEditingExisting) {
                        SetupOptionRow(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = null,
                                    tint = colors.onDark,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            title = "Budget name",
                            trailingValue = budgetName,
                            onClick = {
                                focusManager.clearFocus()
                                showRenameDialog = true
                            },
                            modifier = Modifier.testTag("setup_budget_name_row")
                        )

                        SetupOptionRow(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarToday,
                                    contentDescription = null,
                                    tint = colors.onDark,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            title = "Starts ${DateUtils.formatDayMonth(startEpochDay)}",
                            trailingValue = if (startEpochDay == todayEpochDay) "Today" else "Change",
                            onClick = {
                                focusManager.clearFocus()
                                activeDatePickerMode = DatePickerMode.START
                            },
                            modifier = Modifier.testTag("setup_start_date_row")
                        )
                    }

                    SetupOptionRow(
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                tint = colors.onDark,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        title = if (finishEpochDay != null) {
                            "Until ${DateUtils.formatDayMonth(finishEpochDay!!)}"
                        } else {
                            stringResource(R.string.no_finish_date_selected)
                        },
                        trailingValue = if (daysCount > 0) {
                            "$daysCount ${if (daysCount == 1) "day" else "days"}"
                        } else "",
                        onClick = {
                            focusManager.clearFocus()
                            activeDatePickerMode = DatePickerMode.FINISH
                        },
                        modifier = Modifier.testTag("setup_finish_date_row")
                    )

                    SetupOptionRow(
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Directions,
                                contentDescription = null,
                                tint = colors.onDark,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        title = stringResource(R.string.rest_label),
                        trailingValue = restModeLabel(restMode),
                        onClick = {
                            focusManager.clearFocus()
                            showRestDialog = true
                        },
                        modifier = Modifier.testTag("setup_rest_row")
                    )

                    SetupOptionRow(
                        leadingIcon = {
                            CurrencyCirclesIcon(tint = colors.onDark, size = 24.dp)
                        },
                        title = stringResource(R.string.currency_label),
                        trailingValue = if (currencySymbol.isNotEmpty()) {
                            "$currencySymbol  $currencyCode".trim()
                        } else "",
                        onClick = {
                            focusManager.clearFocus()
                            showCurrencySheet = true
                        },
                        modifier = Modifier.testTag("setup_currency_row")
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = colors.divider, thickness = 1.dp)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = if (canApply) colors.greenText else colors.onDark,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(18.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            if (!canApply) {
                                Text(
                                    text = stringResource(R.string.unable_to_budget),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = colors.onDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                if (!isAmountValid) {
                                    Text(
                                        text = stringResource(R.string.error_enter_budget),
                                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                                        color = colors.onDarkSecondary
                                    )
                                }
                                if (!isDateValid) {
                                    Text(
                                        text = stringResource(R.string.error_min_one_day),
                                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                                        color = colors.onDarkSecondary
                                    )
                                }
                            } else {
                                val daily = parsedAmount / daysCount.toDouble()
                                Text(
                                    text = stringResource(R.string.daily_budget_preview_title),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = colors.onDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${CurrencyUtils.formatAmount(daily, currencySymbol)} per day for $daysCount ${if (daysCount == 1) "day" else "days"}",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                                    color = colors.greenText
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .height(62.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (canApply) colors.blue else colors.cardSurface)
                        .clickable(enabled = canApply) {
                            finishEpochDay?.let { finishDay ->
                                onApplyBudget(
                                    budgetName,
                                    parsedAmount,
                                    startEpochDay,
                                    finishDay,
                                    restMode,
                                    currencyCode,
                                    currencySymbol
                                )
                            }
                        }
                        .testTag("setup_apply_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.apply),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = if (canApply) colors.onBlue else colors.onDarkDisabled
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = if (canApply) colors.onBlue else colors.onDarkDisabled,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }

    if (showRenameDialog) {
        RenameBudgetDialog(
            currentName = budgetName,
            onConfirm = { newName ->
                budgetName = newName
                showRenameDialog = false
            },
            onDismiss = { showRenameDialog = false }
        )
    }

    if (showRestDialog) {
        RestModeSelectorSheet(
            selectedMode = restMode,
            onSelect = {
                restMode = it
                showRestDialog = false
            },
            onDismiss = { showRestDialog = false }
        )
    }

    if (showCurrencySheet) {
        CurrencySelectorSheet(
            selectedCode = currencyCode,
            onSelect = { opt ->
                currencyCode = opt.code
                currencySymbol = opt.symbol
                showCurrencySheet = false
            },
            onDismiss = { showCurrencySheet = false }
        )
    }
}

@Composable
fun SetupOptionRow(
    leadingIcon: @Composable () -> Unit,
    title: String,
    trailingValue: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    titleColor: Color = BuckwheatThemeTokens.colors.onDark
) {
    val colors = BuckwheatThemeTokens.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leadingIcon()
        Spacer(modifier = Modifier.width(18.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            ),
            color = titleColor,
            modifier = Modifier.weight(1f)
        )
        if (trailingValue.isNotEmpty()) {
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = trailingValue,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = colors.onDarkSecondary
            )
        }
    }
}

@Composable
fun restModeLabel(mode: RestMode): String {
    return when (mode) {
        RestMode.ALWAYS_ASK -> stringResource(R.string.rest_always_ask)
        RestMode.SPLIT_TO_REST -> stringResource(R.string.rest_split)
        RestMode.ADD_TO_TODAY -> stringResource(R.string.rest_add_today)
    }
}

private fun sanitizeNumericInput(raw: String): String {
    val normalized = raw.replace(",", ".")
    val sb = StringBuilder()
    var hasDot = false
    var decimalDigits = 0
    for (ch in normalized) {
        if (ch.isDigit()) {
            if (hasDot) {
                if (decimalDigits < 2) {
                    sb.append(ch)
                    decimalDigits++
                }
            } else if (sb.length < 9) {
                sb.append(ch)
            }
        } else if (ch == '.' && !hasDot) {
            if (sb.isEmpty()) sb.append('0')
            sb.append('.')
            hasDot = true
        }
    }
    return sb.toString()
}
