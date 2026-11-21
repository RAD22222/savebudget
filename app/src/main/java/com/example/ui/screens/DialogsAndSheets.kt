package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ExpenseEntity
import com.example.data.RestMode
import com.example.ui.theme.BuckwheatThemeTokens
import com.example.util.CurrencyOption
import com.example.util.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestModeSelectorSheet(
    selectedMode: RestMode,
    onSelect: (RestMode) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = BuckwheatThemeTokens.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.bgDark,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(colors.handleColor)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 20.dp)
        ) {
            Text(
                text = stringResource(R.string.rest_dialog_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 21.sp),
                color = colors.onDark,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp)
            )

            RestOptionRow(
                title = stringResource(R.string.rest_always_ask),
                subtitle = stringResource(R.string.rest_always_ask_desc),
                selected = selectedMode == RestMode.ALWAYS_ASK,
                onClick = { onSelect(RestMode.ALWAYS_ASK) }
            )
            RestOptionRow(
                title = stringResource(R.string.rest_split),
                subtitle = stringResource(R.string.rest_split_desc),
                selected = selectedMode == RestMode.SPLIT_TO_REST,
                onClick = { onSelect(RestMode.SPLIT_TO_REST) }
            )
            RestOptionRow(
                title = stringResource(R.string.rest_add_today),
                subtitle = stringResource(R.string.rest_add_today_desc),
                selected = selectedMode == RestMode.ADD_TO_TODAY,
                onClick = { onSelect(RestMode.ADD_TO_TODAY) }
            )
        }
    }
}

@Composable
private fun RestOptionRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = BuckwheatThemeTokens.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = colors.blueRing,
                unselectedColor = colors.onDarkSecondary
            )
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = colors.onDark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onDarkSecondary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySelectorSheet(
    selectedCode: String,
    onSelect: (CurrencyOption) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = BuckwheatThemeTokens.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.bgDark,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(colors.handleColor)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Text(
                text = stringResource(R.string.select_currency_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 21.sp),
                color = colors.onDark,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(CurrencyUtils.supportedCurrencies, key = { it.code.ifEmpty { "NONE" } }) { option ->
                    val isSelected = option.code == selectedCode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                            .padding(horizontal = 24.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.cardSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = option.symbol.ifEmpty { "—" },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = colors.onDark
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = option.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.onDark
                            )
                            if (option.code.isNotEmpty()) {
                                Text(
                                    text = option.code,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.onDarkSecondary
                                )
                            }
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = colors.blueRing,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommentInputDialog(
    initialComment: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = BuckwheatThemeTokens.colors
    var text by remember(initialComment) { mutableStateOf(initialComment) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardSurface,
        title = {
            Text(
                text = stringResource(R.string.add_comment),
                color = colors.onDark
            )
        },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.take(80) },
                placeholder = {
                    Text(
                        text = stringResource(R.string.comment_placeholder),
                        color = colors.onDarkSecondary
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.onDark,
                    unfocusedTextColor = colors.onDark,
                    focusedBorderColor = colors.blueRing,
                    unfocusedBorderColor = colors.divider,
                    cursorColor = colors.blueRing
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("comment_text_field")
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text.trim()) }) {
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
fun EditExpenseDialog(
    expense: ExpenseEntity,
    onSave: (Double, String) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = BuckwheatThemeTokens.colors
    var amountStr by remember(expense.id) {
        mutableStateOf(
            if (expense.amount % 1.0 == 0.0) expense.amount.toLong().toString()
            else expense.amount.toString()
        )
    }
    var commentStr by remember(expense.id) { mutableStateOf(expense.comment) }

    val parsed = amountStr.replace(",", ".").toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardSurface,
        title = {
            Text(
                text = "Edit expense",
                color = colors.onDark
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it.take(12) },
                    label = { Text("Amount", color = colors.onDarkSecondary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.onDark,
                        unfocusedTextColor = colors.onDark,
                        focusedBorderColor = colors.blueRing,
                        unfocusedBorderColor = colors.divider,
                        cursorColor = colors.blueRing
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = commentStr,
                    onValueChange = { commentStr = it.take(80) },
                    label = { Text("Comment", color = colors.onDarkSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.onDark,
                        unfocusedTextColor = colors.onDark,
                        focusedBorderColor = colors.blueRing,
                        unfocusedBorderColor = colors.divider,
                        cursorColor = colors.blueRing
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = parsed > 0.0,
                onClick = { onSave(parsed, commentStr.trim()) }
            ) {
                Text(stringResource(R.string.save), color = colors.blueRing)
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete) {
                    Text(stringResource(R.string.delete), color = colors.dangerText)
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel), color = colors.onDarkSecondary)
                }
            }
        }
    )
}

@Composable
fun FinishEarlyConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = BuckwheatThemeTokens.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardSurface,
        title = {
            Text(
                text = stringResource(R.string.finish_early_confirm_title),
                color = colors.onDark
            )
        },
        text = {
            Text(
                text = stringResource(R.string.finish_early_confirm_body),
                color = colors.onDarkSecondary
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.testTag("confirm_finish_early_button")
            ) {
                Text(stringResource(R.string.finish), color = colors.dangerText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = colors.onDarkSecondary)
            }
        }
    )
}
