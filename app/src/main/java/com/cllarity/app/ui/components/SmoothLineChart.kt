package com.cllarity.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cllarity.app.ui.theme.AccentIndigo
import com.cllarity.app.ui.theme.TextTertiary
import com.cllarity.app.ui.theme.Motion

@Composable
fun SmoothLineChart(
    dataPoints: List<Float>,
    labels: List<String> = emptyList(),
    modifier: Modifier = Modifier,
    height: Dp = 150.dp,
    lineColor: Color = AccentIndigo,
    fillStartAlpha: Float = 0.35f
) {
    if (dataPoints.isEmpty()) return

    var animationTrigger by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(dataPoints) {
        animationTrigger = 1f
    }

    val animProgress by animateFloatAsState(
        targetValue = animationTrigger,
        animationSpec = Motion.timed(Motion.DurationExtraLong, Motion.EaseDecelerate),
        label = "chartLineAnim"
    )

    val maxVal = (dataPoints.maxOrNull() ?: 1f).coerceAtLeast(1f)
    val minVal = (dataPoints.minOrNull() ?: 0f).coerceAtLeast(0f)
    val range = (maxVal - minVal).coerceAtLeast(1f)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            val w = size.width
            val h = size.height
            val stepX = if (dataPoints.size > 1) w / (dataPoints.size - 1) else w

            // 1. Subtle horizontal grid lines (at 25%, 50%, 75%)
            val gridColor = Color(0x18FFFFFF)
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            for (i in 1..3) {
                val y = h * (i * 0.25f)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dashEffect
                )
            }

            // 2. Compute coordinates
            val points = dataPoints.mapIndexed { index, value ->
                val normY = (value - minVal) / range
                val px = index * stepX
                val py = h - (normY * (h * 0.78f)) - (h * 0.11f) // leave padding top and bottom
                Offset(px, py)
            }

            // 3. Construct smooth cubic Bezier curve
            val curvePath = Path()
            val fillPath = Path()

            if (points.isNotEmpty()) {
                curvePath.moveTo(points[0].x, points[0].y)
                fillPath.moveTo(points[0].x, h)
                fillPath.lineTo(points[0].x, points[0].y)

                for (i in 0 until points.size - 1) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    val controlX1 = p0.x + (p1.x - p0.x) * 0.5f
                    val controlY1 = p0.y
                    val controlX2 = p0.x + (p1.x - p0.x) * 0.5f
                    val controlY2 = p1.y

                    curvePath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                    fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                }

                fillPath.lineTo(points.last().x, h)
                fillPath.close()

                // Draw Gradient Area under Curve
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            lineColor.copy(alpha = fillStartAlpha * animProgress),
                            lineColor.copy(alpha = 0.0f)
                        ),
                        startY = 0f,
                        endY = h
                    )
                )

                // Measure and draw progressive animated stroke
                val pathMeasure = PathMeasure()
                pathMeasure.setPath(curvePath, false)
                val totalLength = pathMeasure.length
                val animatedSegment = Path()
                pathMeasure.getSegment(0f, totalLength * animProgress, animatedSegment, true)

                drawPath(
                    path = animatedSegment,
                    color = lineColor,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )

                // Draw point markers
                val revealedCount = (points.size * animProgress).toInt().coerceIn(0, points.size)
                for (i in 0 until revealedCount) {
                    val pt = points[i]
                    // Outer glow
                    drawCircle(
                        color = lineColor.copy(alpha = 0.25f),
                        radius = 7.dp.toPx(),
                        center = pt
                    )
                    // Inner point
                    drawCircle(
                        color = lineColor,
                        radius = 4.dp.toPx(),
                        center = pt
                    )
                    // Center glint
                    drawCircle(
                        color = Color.White,
                        radius = 1.8.dp.toPx(),
                        center = pt
                    )
                }
            }
        }

        // Labels underneath
        if (labels.isNotEmpty() && labels.size == dataPoints.size) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                labels.forEach { label ->
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
