package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Overlapping coins/circles icon matching Buckwheat's Currency row icon.
 */
@Composable
fun CurrencyCirclesIcon(
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = this.size.minDimension * 0.095f
        val radius = this.size.minDimension * 0.30f
        val cy = this.size.height / 2f
        val leftCx = this.size.width * 0.37f
        val rightCx = this.size.width * 0.63f

        // Left crescent/arc circle
        drawArc(
            color = tint,
            startAngle = 115f,
            sweepAngle = 130f,
            useCenter = false,
            topLeft = Offset(leftCx - radius, cy - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        // Right full circle
        drawCircle(
            color = tint,
            radius = radius,
            center = Offset(rightCx, cy),
            style = Stroke(width = strokeWidth)
        )
    }
}
