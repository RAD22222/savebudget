package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.BuckwheatThemeTokens
import com.example.util.DateUtils

enum class DatePickerMode {
    FINISH,
    START
}

@Composable
fun FinishDatePickerScreen(
    initialSelectedEpochDay: Long?,
    startWeekOnMonday: Boolean,
    onDismiss: () -> Unit,
    onConfirmDate: (Long) -> Unit,
    modifier: Modifier = Modifier,
    mode: DatePickerMode = DatePickerMode.FINISH,
    rangeStartEpochDay: Long = DateUtils.todayEpochDay(),
    rangeFinishEpochDay: Long? = null
) {
    BackHandler(onBack = onDismiss)

    val colors = BuckwheatThemeTokens.colors
    val todayEpochDay = remember { DateUtils.todayEpochDay() }
    val minSelectableDay = if (mode == DatePickerMode.FINISH) rangeStartEpochDay else Long.MIN_VALUE

    var selectedEpochDay by remember(initialSelectedEpochDay, mode, minSelectableDay) {
        mutableStateOf(
            if (mode == DatePickerMode.FINISH) {
                initialSelectedEpochDay?.takeIf { it >= minSelectableDay }
            } else {
                initialSelectedEpochDay ?: rangeStartEpochDay
            }
        )
    }

    val months = remember(startWeekOnMonday, mode) {
        DateUtils.buildUpcomingMonths(
            monthsCount = 12,
            monthsBefore = if (mode == DatePickerMode.START) 2 else 0,
            startWeekOnMonday = startWeekOnMonday
        )
    }
    val weekdayLabels = remember(startWeekOnMonday) {
        if (startWeekOnMonday) listOf("M", "T", "W", "T", "F", "S", "S")
        else listOf("S", "M", "T", "W", "T", "F", "S")
    }

    val effectiveStart = if (mode == DatePickerMode.START) {
        selectedEpochDay ?: rangeStartEpochDay
    } else {
        rangeStartEpochDay
    }
    val effectiveEnd = if (mode == DatePickerMode.START) {
        rangeFinishEpochDay?.coerceAtLeast(effectiveStart)
    } else {
        selectedEpochDay
    }

    val isApplyEnabled = selectedEpochDay != null

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("date_picker_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.close_cd),
                        tint = colors.onDark,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .height(48.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (isApplyEnabled) colors.blue else colors.cardSurface)
                        .clickable(enabled = isApplyEnabled) {
                            selectedEpochDay?.let(onConfirmDate)
                        }
                        .padding(horizontal = 20.dp)
                        .testTag("date_picker_apply_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = if (isApplyEnabled) colors.onBlue else colors.onDarkDisabled,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.apply),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = if (isApplyEnabled) colors.onBlue else colors.onDarkDisabled
                    )
                }
            }

            Text(
                text = if (mode == DatePickerMode.START) {
                    "Choose a start date"
                } else {
                    stringResource(R.string.choose_finish_date)
                },
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = colors.onDark,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 6.dp)
            )

            AnimatedVisibility(
                visible = effectiveEnd != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val endDay = effectiveEnd ?: effectiveStart
                val totalDays = DateUtils.inclusiveDaysBetween(effectiveStart, endDay)
                val rangeLabel = if (endDay <= effectiveStart) {
                    "${DateUtils.formatDayMonth(effectiveStart)} · 1 day"
                } else {
                    "${DateUtils.formatDayMonth(effectiveStart)} → ${DateUtils.formatDayMonth(endDay)} · $totalDays days"
                }
                Text(
                    text = rangeLabel,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = if (colors.isLight) colors.blue else colors.blueRing,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(months, key = { "${it.year}_${it.monthZeroBased}" }) { month ->
                    MonthSection(
                        month = month,
                        weekdayLabels = weekdayLabels,
                        minSelectableDay = minSelectableDay,
                        rangeStartEpochDay = effectiveStart,
                        rangeEndEpochDay = effectiveEnd,
                        focusedEpochDay = selectedEpochDay ?: todayEpochDay,
                        onSelectDay = { epochDay ->
                            selectedEpochDay = epochDay
                        }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun MonthSection(
    month: DateUtils.MonthCalendar,
    weekdayLabels: List<String>,
    minSelectableDay: Long,
    rangeStartEpochDay: Long,
    rangeEndEpochDay: Long?,
    focusedEpochDay: Long,
    onSelectDay: (Long) -> Unit
) {
    val colors = BuckwheatThemeTokens.colors
    val hasRange = rangeEndEpochDay != null && rangeEndEpochDay > rangeStartEpochDay

    // Rich, layered colors for the deeply rounded range capsule
    val capsuleTopFill = remember(colors) {
        if (colors.isLight) lerp(colors.blueRangeBg, Color.White, 0.22f)
        else lerp(colors.blueRangeBg, colors.blue, 0.42f)
    }
    val capsuleBottomFill = remember(colors) {
        if (colors.isLight) lerp(colors.blueRangeBg, colors.blue, 0.16f)
        else lerp(colors.blueRangeBg, colors.bgDark, 0.26f)
    }
    val capsuleBorderTop = remember(colors) {
        if (colors.isLight) colors.blue.copy(alpha = 0.46f)
        else colors.blueRing.copy(alpha = 0.72f)
    }
    val capsuleBorderMid = remember(colors) {
        if (colors.isLight) colors.blueBright.copy(alpha = 0.34f)
        else colors.blueBright.copy(alpha = 0.56f)
    }
    val capsuleBorderBottom = remember(colors) {
        if (colors.isLight) colors.blue.copy(alpha = 0.22f)
        else colors.blue.copy(alpha = 0.38f)
    }
    val capsuleGlowColor = remember(colors) {
        if (colors.isLight) colors.blue.copy(alpha = 0.10f)
        else colors.blue.copy(alpha = 0.24f)
    }
    val capsuleInnerHighlight = remember(colors) {
        if (colors.isLight) Color.White.copy(alpha = 0.55f)
        else colors.blueRing.copy(alpha = 0.24f)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        val monthTitle = if (month.showYear) {
            "${month.monthName} ${month.year}"
        } else {
            month.monthName
        }

        Text(
            text = monthTitle,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = colors.onDark,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 14.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            weekdayLabels.forEach { dayLetter ->
                Text(
                    text = dayLetter,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = colors.onDarkSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        val totalSlots = month.leadingEmptySlots + month.days.size
        val rows = (totalSlots + 6) / 7

        for (rowIdx in 0 until rows) {
            // Find the contiguous column segment [firstRangeCol..lastRangeCol] covered by the selected range in this row
            var firstRangeCol = -1
            var lastRangeCol = -1
            if (hasRange && rangeEndEpochDay != null) {
                for (colIdx in 0 until 7) {
                    val slot = rowIdx * 7 + colIdx
                    val dayIdx = slot - month.leadingEmptySlots
                    if (dayIdx in month.days.indices) {
                        val dayEpoch = month.days[dayIdx].epochDay
                        if (dayEpoch in rangeStartEpochDay..rangeEndEpochDay) {
                            if (firstRangeCol == -1) firstRangeCol = colIdx
                            lastRangeCol = colIdx
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 3.dp)
                    .drawBehind {
                        if (firstRangeCol != -1 && lastRangeCol != -1) {
                            val colWidth = size.width / 7f
                            val pillHeight = minOf(46.dp.toPx(), colWidth * 0.96f)
                            val pillRadius = pillHeight / 2f
                            val topY = (size.height - pillHeight) / 2f

                            val startCenterX = (firstRangeCol + 0.5f) * colWidth
                            val endCenterX = (lastRangeCol + 0.5f) * colWidth
                            val edgeMargin = 1.dp.toPx()

                            // Fully rounded pill geometry wrapping seamlessly around start/end cells
                            val leftX: Float
                            val pillWidth: Float
                            if (firstRangeCol == lastRangeCol) {
                                val maxLeft = (size.width - pillHeight - edgeMargin).coerceAtLeast(edgeMargin)
                                leftX = (startCenterX - pillRadius).coerceIn(edgeMargin, maxLeft)
                                pillWidth = pillHeight
                            } else {
                                leftX = (startCenterX - pillRadius).coerceAtLeast(edgeMargin)
                                val rightX = (endCenterX + pillRadius).coerceAtMost(size.width - edgeMargin)
                                pillWidth = (rightX - leftX).coerceAtLeast(pillHeight)
                            }
                            val corner = CornerRadius(pillRadius, pillRadius)

                            // 1. Soft outer ambient depth halo
                            val haloExpand = 2.dp.toPx()
                            val haloCorner = CornerRadius(pillRadius + haloExpand, pillRadius + haloExpand)
                            drawRoundRect(
                                color = capsuleGlowColor,
                                topLeft = Offset(leftX - haloExpand, topY - haloExpand),
                                size = Size(pillWidth + haloExpand * 2f, pillHeight + haloExpand * 2f),
                                cornerRadius = haloCorner
                            )

                            // 2. Deep vertical tonal gradient fill
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(capsuleTopFill, capsuleBottomFill),
                                    startY = topY,
                                    endY = topY + pillHeight
                                ),
                                topLeft = Offset(leftX, topY),
                                size = Size(pillWidth, pillHeight),
                                cornerRadius = corner
                            )

                            // 3. Subtle inner top bevel sheen for dimensional depth
                            val innerInset = 1.6.dp.toPx()
                            val innerRadius = (pillRadius - innerInset).coerceAtLeast(0f)
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(capsuleInnerHighlight, Color.Transparent),
                                    startY = topY + innerInset,
                                    endY = topY + pillHeight * 0.6f
                                ),
                                topLeft = Offset(leftX + innerInset, topY + innerInset),
                                size = Size(
                                    (pillWidth - innerInset * 2f).coerceAtLeast(0f),
                                    (pillHeight - innerInset * 2f).coerceAtLeast(0f)
                                ),
                                cornerRadius = CornerRadius(innerRadius, innerRadius),
                                style = Stroke(width = 1.dp.toPx())
                            )

                            // 4. Crisp, smooth, deeply rounded outer gradient border
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        capsuleBorderTop,
                                        capsuleBorderMid,
                                        capsuleBorderBottom
                                    ),
                                    startY = topY,
                                    endY = topY + pillHeight
                                ),
                                topLeft = Offset(leftX, topY),
                                size = Size(pillWidth, pillHeight),
                                cornerRadius = corner,
                                style = Stroke(width = 1.6.dp.toPx())
                            )
                        }
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (colIdx in 0 until 7) {
                    val slot = rowIdx * 7 + colIdx
                    val dayIdx = slot - month.leadingEmptySlots
                    if (dayIdx in month.days.indices) {
                        val cell = month.days[dayIdx]
                        val isPast = cell.epochDay < minSelectableDay
                        val isAnchorStart = cell.epochDay == rangeStartEpochDay
                        val isAnchorEnd = rangeEndEpochDay != null && cell.epochDay == rangeEndEpochDay
                        val isSelected = cell.epochDay == focusedEpochDay || isAnchorEnd
                        val isTodayOrStart = isAnchorStart && !isSelected
                        val isInRange = hasRange &&
                            rangeEndEpochDay != null &&
                            cell.epochDay > rangeStartEpochDay &&
                            cell.epochDay < rangeEndEpochDay

                        DayGridCell(
                            dayText = cell.dayOfMonth.toString(),
                            isPast = isPast,
                            isToday = isTodayOrStart,
                            isSelected = isSelected,
                            isInRange = isInRange,
                            onClick = { onSelectDay(cell.epochDay) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("calendar_day_${cell.epochDay}")
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f).height(52.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayGridCell(
    dayText: String,
    isPast: Boolean,
    isToday: Boolean,
    isSelected: Boolean,
    isInRange: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = BuckwheatThemeTokens.colors
    val isEndpoint = isSelected || isToday

    val badgeScale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "dayBadgeScale"
    )

    val endpointBrush = remember(isSelected, isToday, colors) {
        when {
            isSelected -> Brush.verticalGradient(
                colors = listOf(
                    lerp(colors.blueBright, colors.blueRing, 0.22f),
                    colors.blueBright,
                    lerp(colors.blue, colors.bgDark, if (colors.isLight) 0.04f else 0.14f)
                )
            )
            isToday -> Brush.verticalGradient(
                colors = listOf(
                    lerp(colors.blue, colors.blueBright, 0.45f),
                    colors.blue,
                    lerp(colors.blue, colors.bgDark, if (colors.isLight) 0.05f else 0.20f)
                )
            )
            else -> null
        }
    }

    val endpointBorderBrush = remember(isSelected, isToday, colors) {
        when {
            isSelected -> Brush.verticalGradient(
                colors = listOf(
                    colors.blueRing.copy(alpha = 0.92f),
                    colors.blueBright.copy(alpha = 0.55f)
                )
            )
            isToday -> Brush.verticalGradient(
                colors = listOf(
                    colors.blueRing.copy(alpha = 0.60f),
                    colors.blue.copy(alpha = 0.35f)
                )
            )
            else -> null
        }
    }

    val textColor = when {
        isSelected || isToday -> colors.onBlue
        isInRange -> if (colors.isLight) colors.blueBright else colors.onDark
        isPast -> colors.onDarkDisabled
        else -> colors.onDark
    }

    Box(
        modifier = modifier
            .height(52.dp)
            .clip(CircleShape)
            .then(
                if (!isPast) Modifier.clickable(onClick = onClick) else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(if (isEndpoint) 40.dp else 42.dp)
                .graphicsLayer {
                    scaleX = badgeScale
                    scaleY = badgeScale
                }
                .clip(CircleShape)
                .then(
                    if (endpointBrush != null) {
                        Modifier
                            .background(endpointBrush)
                            .then(
                                if (endpointBorderBrush != null) {
                                    Modifier.border(
                                        width = if (isSelected) 1.35.dp else 1.dp,
                                        brush = endpointBorderBrush,
                                        shape = CircleShape
                                    )
                                } else Modifier
                            )
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = dayText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = when {
                        isSelected || isToday -> FontWeight.Bold
                        isInRange -> FontWeight.SemiBold
                        else -> FontWeight.Medium
                    }
                ),
                color = textColor
            )
        }
    }
}
