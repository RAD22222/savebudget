package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.BudgetEntity
import com.example.data.RestMode
import com.example.ui.BudgetHealth
import com.example.ui.BudgetMetrics
import com.example.ui.components.CurrencyCirclesIcon
import com.example.ui.theme.BuckwheatThemeTokens
import com.example.util.CurrencyUtils
import com.example.util.DateUtils

@Composable
fun BudgetOverviewSheet(
    budget: BudgetEntity,
    metrics: BudgetMetrics,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onRestClick: () -> Unit,
    onCurrencyClick: () -> Unit,
    onExportCsvClick: () -> Unit,
    onFinishEarlyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onDismiss)

    val colors = BuckwheatThemeTokens.colors
    var cardPage by remember { mutableIntStateOf(0) }

    val restMode = RestMode.fromKey(budget.restMode)
    val symbol = budget.currencySymbol

    val (trackColor, fillColor, primaryTextColor, subTextColor) = when (metrics.overallHealth) {
        BudgetHealth.HEALTHY -> QuadColor(
            colors.greenTrack, colors.greenFill, colors.greenText, colors.greenSubText
        )
        BudgetHealth.WARNING -> QuadColor(
            colors.amberTrack, colors.amberFill, colors.amberText, colors.amberSubText
        )
        BudgetHealth.OVER -> QuadColor(
            colors.redTrack, colors.redFill, colors.redText, colors.redSubText
        )
    }

    val heroFillFraction by animateFloatAsState(
        targetValue = if (cardPage == 0) {
            (metrics.remainingPercent / 100f).coerceIn(0f, 1f)
        } else {
            (metrics.spentPercent / 100f).coerceIn(0f, 1f)
        },
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 350f),
        label = "hero_fill"
    )

    val daysSweepFraction by animateFloatAsState(
        targetValue = metrics.daysLeftFraction.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 300f),
        label = "days_ring"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onDismiss
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(colors.bgDark)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {}
                )
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 36.dp, height = 4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(colors.handleColor.copy(alpha = 0.65f))
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = budget.name.ifBlank { stringResource(R.string.budget_title) },
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 23.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = colors.onDark,
                    modifier = Modifier.align(Alignment.Center)
                )
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(48.dp)
                        .testTag("overview_edit_budget_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = stringResource(R.string.edit_budget_cd),
                        tint = colors.onDark,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(186.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(trackColor)
                    .clickable { cardPage = if (cardPage == 0) 1 else 0 }
                    .testTag("overview_hero_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(heroFillFraction)
                        .background(fillColor)
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 18.dp, end = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (cardPage == 0) 6.dp else 5.dp)
                            .clip(CircleShape)
                            .background(
                                if (cardPage == 0) primaryTextColor
                                else primaryTextColor.copy(alpha = 0.35f)
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(if (cardPage == 1) 6.dp else 5.dp)
                            .clip(CircleShape)
                            .background(
                                if (cardPage == 1) primaryTextColor
                                else primaryTextColor.copy(alpha = 0.35f)
                            )
                    )
                }

                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val mainNumber = if (cardPage == 0) {
                        CurrencyUtils.formatAmount(metrics.totalRemaining, symbol)
                    } else {
                        CurrencyUtils.formatAmount(metrics.totalSpent, symbol)
                    }
                    Text(
                        text = mainNumber,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = primaryTextColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(
                            if (cardPage == 0) R.string.left_label else R.string.spent_label
                        ),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = subTextColor
                    )
                }

                Text(
                    text = if (cardPage == 0) {
                        "${metrics.remainingPercent}% of budget"
                    } else {
                        "${metrics.spentPercent}% spent"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = primaryTextColor,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 20.dp, bottom = 18.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(132.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(colors.cardSurface)
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = CurrencyUtils.formatAmount(metrics.startingBudget, symbol),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 25.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = colors.onDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.starting_budget),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = colors.onDarkSecondary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = DateUtils.formatDayMonth(budget.startDateEpochDay),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = colors.onDark
                        )
                        Canvas(
                            modifier = Modifier
                                .weight(1f)
                                .height(16.dp)
                                .padding(horizontal = 10.dp)
                        ) {
                            val cy = size.height / 2f
                            val strokeW = 2.dp.toPx()
                            val arrowSize = 6.dp.toPx()
                            drawLine(
                                color = colors.onDark,
                                start = Offset(0f, cy),
                                end = Offset(size.width - strokeW, cy),
                                strokeWidth = strokeW,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = colors.onDark,
                                start = Offset(size.width - arrowSize, cy - arrowSize),
                                end = Offset(size.width, cy),
                                strokeWidth = strokeW,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = colors.onDark,
                                start = Offset(size.width - arrowSize, cy + arrowSize),
                                end = Offset(size.width, cy),
                                strokeWidth = strokeW,
                                cap = StrokeCap.Round
                            )
                        }
                        Text(
                            text = DateUtils.formatDayMonth(budget.finishDateEpochDay),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = colors.onDark
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(132.dp)
                        .clip(CircleShape)
                        .background(colors.cardSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                        val strokePx = 6.dp.toPx()
                        val inset = strokePx / 2f
                        val arcSize = Size(size.width - strokePx, size.height - strokePx)
                        drawArc(
                            color = colors.divider,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = colors.blueRing,
                            startAngle = -90f,
                            sweepAngle = 360f * daysSweepFraction,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = metrics.daysLeft.toString(),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 27.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = colors.onDark
                        )
                        Text(
                            text = stringResource(R.string.days_left),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = colors.onDarkSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

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
                onClick = onRestClick,
                modifier = Modifier.testTag("overview_rest_row")
            )

            SetupOptionRow(
                leadingIcon = {
                    CurrencyCirclesIcon(tint = colors.onDark, size = 24.dp)
                },
                title = stringResource(R.string.currency_label),
                trailingValue = if (symbol.isNotEmpty()) "$symbol  ${budget.currencyCode}".trim() else "",
                onClick = onCurrencyClick,
                modifier = Modifier.testTag("overview_currency_row")
            )

            SetupOptionRow(
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Download,
                        contentDescription = null,
                        tint = colors.onDark,
                        modifier = Modifier.size(24.dp)
                    )
                },
                title = stringResource(R.string.export_to_csv),
                trailingValue = "",
                onClick = onExportCsvClick,
                modifier = Modifier.testTag("overview_export_csv_row")
            )

            SetupOptionRow(
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = null,
                        tint = colors.dangerText,
                        modifier = Modifier.size(24.dp)
                    )
                },
                title = stringResource(R.string.finish_early),
                trailingValue = "",
                titleColor = colors.dangerText,
                onClick = onFinishEarlyClick,
                modifier = Modifier.testTag("overview_finish_early_row")
            )
        }
    }
}

private data class QuadColor(
    val track: Color,
    val fill: Color,
    val primaryText: Color,
    val subText: Color
)
