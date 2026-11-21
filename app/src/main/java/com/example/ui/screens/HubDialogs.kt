package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.DebtType
import com.example.data.ReminderOperation
import com.example.data.SubBudgetEntity
import com.example.ui.theme.BuckwheatThemeTokens
import com.example.util.DateUtils

@Composable
internal fun AddSubBudgetDialog(
    initialName: String,
    currencySymbol: String,
    onConfirm: (String, Double) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = BuckwheatThemeTokens.colors
    var name by remember(initialName) { mutableStateOf(initialName) }
    var amountText by remember { mutableStateOf("") }
    val parsed = amountText.replace(",", ".").toDoubleOrNull() ?: 0.0
    val presets = listOf("Food", "Transport", "Snacks", "Coffee", "Shopping")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardSurface,
        title = { Text("Add Sub-budget", color = colors.onDark) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { preset ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (name == preset) colors.blue else colors.panelSurface)
                                .clickable { name = preset }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = preset,
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                                color = if (name == preset) colors.onBlue else colors.onDarkSecondary
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(30) },
                    label = { Text("Name (e.g. Food, Transport)", color = colors.onDarkSecondary) },
                    singleLine = true,
                    colors = hubDialogFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.take(12) },
                    label = {
                        Text(
                            if (currencySymbol.isNotEmpty()) "Amount ($currencySymbol)" else "Amount",
                            color = colors.onDarkSecondary
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = hubDialogFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && parsed > 0.0,
                onClick = { onConfirm(name.trim(), parsed) }
            ) {
                Text("Add", color = colors.blueRing)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = colors.onDarkSecondary)
            }
        }
    )
}

@Composable
internal fun RenameBudgetDialog(
    currentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = BuckwheatThemeTokens.colors
    var name by remember(currentName) { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardSurface,
        title = { Text("Main Budget Name", color = colors.onDark) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(36) },
                label = { Text("e.g. November Budget", color = colors.onDarkSecondary) },
                singleLine = true,
                colors = hubDialogFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onConfirm(name.trim()) }
            ) {
                Text(stringResource(R.string.save), color = colors.blueRing)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = colors.onDarkSecondary)
            }
        }
    )
}

@Composable
internal fun AddMonthlyBudgetDialog(
    currencySymbol: String,
    onConfirm: (String, Double) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = BuckwheatThemeTokens.colors
    var name by remember { mutableStateOf(DateUtils.currentMonthBudgetName()) }
    var amountText by remember { mutableStateOf("") }
    val parsed = amountText.replace(",", ".").toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardSurface,
        title = { Text("New Main Budget", color = colors.onDark) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(36) },
                    label = { Text("Name (e.g. November Budget)", color = colors.onDarkSecondary) },
                    singleLine = true,
                    colors = hubDialogFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.take(12) },
                    label = {
                        Text(
                            if (currencySymbol.isNotEmpty()) "Total amount ($currencySymbol)" else "Total amount",
                            color = colors.onDarkSecondary
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = hubDialogFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = parsed > 0.0,
                onClick = { onConfirm(name.trim(), parsed) }
            ) {
                Text("Create", color = colors.blueRing)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = colors.onDarkSecondary)
            }
        }
    )
}

@Composable
internal fun AddDebtDialog(
    mainBudgetName: String,
    onConfirm: (String, Double, DebtType, String, Long, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = BuckwheatThemeTokens.colors
    var person by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(DebtType.BORROWED) }
    var selectedEpochDay by remember { mutableLongStateOf(DateUtils.todayEpochDay()) }
    var countInMain by remember { mutableStateOf(false) }
    val parsed = amountText.replace(",", ".").toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardSurface,
        title = { Text("Borrow / Lend", color = colors.onDark) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        DebtType.BORROWED to "Borrowed",
                        DebtType.LENT to "Lent"
                    ).forEach { (itemType, label) ->
                        val active = type == itemType
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    when {
                                        active && itemType == DebtType.BORROWED -> colors.redFill
                                        active -> colors.greenFill
                                        else -> colors.panelSurface
                                    }
                                )
                                .clickable { type = itemType },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                color = when {
                                    active && itemType == DebtType.BORROWED -> colors.redText
                                    active -> colors.greenText
                                    else -> colors.onDarkSecondary
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = person,
                    onValueChange = { person = it.take(40) },
                    label = { Text("Person name", color = colors.onDarkSecondary) },
                    singleLine = true,
                    colors = hubDialogFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.take(12) },
                    label = { Text("Amount", color = colors.onDarkSecondary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = hubDialogFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                // Date Stepper
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.panelSurface)
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { selectedEpochDay -= 1L },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            contentDescription = "Previous day",
                            tint = colors.onDark
                        )
                    }
                    Text(
                        text = DateUtils.formatWeekdayDate(selectedEpochDay),
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.onDark,
                        modifier = Modifier.clickable { selectedEpochDay = DateUtils.todayEpochDay() }
                    )
                    IconButton(
                        onClick = { selectedEpochDay += 1L },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = "Next day",
                            tint = colors.onDark
                        )
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it.take(60) },
                    label = { Text("Note (optional)", color = colors.onDarkSecondary) },
                    singleLine = true,
                    colors = hubDialogFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                // Count in Main Budget toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.panelSurface)
                        .clickable { countInMain = !countInMain }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Count in $mainBudgetName",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = colors.onDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (type == DebtType.BORROWED) {
                                "Adds borrowed cash to main budget"
                            } else {
                                "Subtracts lent cash from main budget"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                            color = colors.onDarkSecondary
                        )
                    }
                    Switch(
                        checked = countInMain,
                        onCheckedChange = { countInMain = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.onBlue,
                            checkedTrackColor = colors.blue,
                            uncheckedThumbColor = colors.onDarkSecondary,
                            uncheckedTrackColor = colors.cardSurface
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = person.isNotBlank() && parsed > 0.0,
                onClick = {
                    onConfirm(
                        person.trim(),
                        parsed,
                        type,
                        note.trim(),
                        selectedEpochDay,
                        countInMain
                    )
                }
            ) {
                Text(stringResource(R.string.save), color = colors.blueRing)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = colors.onDarkSecondary)
            }
        }
    )
}

@Composable
internal fun AddExtraDialog(
    mainBudgetName: String,
    subBudgets: List<SubBudgetEntity>,
    onConfirm: (String, Double, Int, ReminderOperation, Long?, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = BuckwheatThemeTokens.colors
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var dueDayText by remember { mutableStateOf("1") }
    var operation by remember { mutableStateOf(ReminderOperation.ADD) }
    var targetSubId by remember { mutableStateOf<Long?>(null) } // null = Main Budget
    var countInMain by remember { mutableStateOf(true) }

    val parsedAmount = amountText.replace(",", ".").toDoubleOrNull() ?: 0.0
    val parsedDay = dueDayText.toIntOrNull()?.coerceIn(1, 31) ?: 1
    val targetName = targetSubId?.let { id -> subBudgets.firstOrNull { it.id == id }?.name } ?: mainBudgetName

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardSurface,
        title = { Text("Add Extra", color = colors.onDark) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Add (+) vs Subtract (-) toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        ReminderOperation.ADD to "+ Add money",
                        ReminderOperation.SUBTRACT to "− Subtract"
                    ).forEach { (op, label) ->
                        val active = operation == op
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    when {
                                        active && op == ReminderOperation.ADD -> colors.greenFill
                                        active -> colors.redFill
                                        else -> colors.panelSurface
                                    }
                                )
                                .clickable { operation = op },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                color = when {
                                    active && op == ReminderOperation.ADD -> colors.greenText
                                    active -> colors.redText
                                    else -> colors.onDarkSecondary
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it.take(40) },
                    label = { Text("Title (e.g. Bonus, Wi-Fi bill)", color = colors.onDarkSecondary) },
                    singleLine = true,
                    colors = hubDialogFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.take(12) },
                        label = { Text("Amount", color = colors.onDarkSecondary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = hubDialogFieldColors(),
                        modifier = Modifier.weight(1.4f)
                    )
                    OutlinedTextField(
                        value = dueDayText,
                        onValueChange = { dueDayText = it.filter(Char::isDigit).take(2) },
                        label = { Text("Day", color = colors.onDarkSecondary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = hubDialogFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Connected budget selector (Main Budget OR any Sub-Budget)
                Text(
                    text = "Connected to",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onDarkSecondary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val mainSelected = targetSubId == null
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (mainSelected) colors.blue else colors.panelSurface)
                            .clickable { targetSubId = null }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = mainBudgetName,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (mainSelected) colors.onBlue else colors.onDarkSecondary
                        )
                    }
                    subBudgets.forEach { sub ->
                        val active = targetSubId == sub.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (active) colors.blue else colors.panelSurface)
                                .clickable { targetSubId = sub.id }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = sub.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (active) colors.onBlue else colors.onDarkSecondary
                            )
                        }
                    }
                }

                // Count in Main Budget toggle (user's choice)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.panelSurface)
                        .clickable { countInMain = !countInMain }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Count in $targetName",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = colors.onDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (operation == ReminderOperation.ADD) {
                                "Adds to budget immediately"
                            } else {
                                "Subtracts from budget immediately"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                            color = colors.onDarkSecondary
                        )
                    }
                    Switch(
                        checked = countInMain,
                        onCheckedChange = { countInMain = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.onBlue,
                            checkedTrackColor = colors.blue,
                            uncheckedThumbColor = colors.onDarkSecondary,
                            uncheckedTrackColor = colors.cardSurface
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank() && parsedAmount > 0.0,
                onClick = {
                    onConfirm(
                        title.trim(),
                        parsedAmount,
                        parsedDay,
                        operation,
                        targetSubId,
                        countInMain
                    )
                }
            ) {
                Text(stringResource(R.string.save), color = colors.blueRing)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = colors.onDarkSecondary)
            }
        }
    )
}

@Composable
private fun hubDialogFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = BuckwheatThemeTokens.colors.onDark,
    unfocusedTextColor = BuckwheatThemeTokens.colors.onDark,
    focusedBorderColor = BuckwheatThemeTokens.colors.blueRing,
    unfocusedBorderColor = BuckwheatThemeTokens.colors.divider,
    cursorColor = BuckwheatThemeTokens.colors.blueRing
)
