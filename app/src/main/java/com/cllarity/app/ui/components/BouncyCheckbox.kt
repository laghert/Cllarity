package com.cllarity.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cllarity.app.ui.theme.AccentLumen
import com.cllarity.app.ui.theme.GlassBorderSubtle
import com.cllarity.app.ui.theme.MidnightBackground
import androidx.compose.ui.graphics.graphicsLayer
import com.cllarity.app.ui.theme.rememberHaptics
import com.cllarity.app.ui.theme.Motion

@Composable
fun BouncyCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    activeColor: Color = AccentLumen,
    isHapticsEnabled: Boolean = true
) {
    val haptics = rememberHaptics()
    var isPressed by remember { mutableStateOf(false) }

    // Apple Spring Physics: Squash & Stretch
    val scaleX by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.88f
            checked -> 1.04f
            else -> 1.0f
        },
        animationSpec = spring(
            dampingRatio = 0.68f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "checkboxScaleX"
    )

    val scaleY by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.82f // Squash deeper on press
            checked -> 1.08f // Stretch up on toggle release
            else -> 1.0f
        },
        animationSpec = spring(
            dampingRatio = 0.58f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "checkboxScaleY"
    )

    // Corner Radius Morph: Square -> Organic Squircle Pebble
    val cornerMorphRatio by animateFloatAsState(
        targetValue = if (checked) 0.38f else 0.28f,
        animationSpec = Motion.spatialDefault(),
        label = "cornerMorph"
    )

    // Halo Glow on Completion
    val haloAlpha by animateFloatAsState(
        targetValue = if (checked && !isPressed) 0.35f else 0f,
        animationSpec = Motion.effectsDefault(),
        label = "haloGlow"
    )

    val bgColor by animateColorAsState(
        targetValue = if (checked) activeColor else Color.Transparent,
        animationSpec = Motion.ColorMorph,
        label = "checkboxBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (checked) activeColor else GlassBorderSubtle,
        animationSpec = Motion.ColorMorph,
        label = "checkboxBorder"
    )

    val checkmarkProgress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = Motion.timed(Motion.DurationShort + 20),
        label = "checkmarkProgress"
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                this.scaleX = scaleX
                this.scaleY = scaleY
            }
            .pointerInput(checked) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        if (isHapticsEnabled) {
                            haptics.tick()
                        }
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = {
                        if (isHapticsEnabled) {
                            if (!checked) haptics.success() else haptics.click()
                        }
                        onCheckedChange(!checked)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val canvasW = this.size.width
            val canvasH = this.size.height
            val strokeW = 2.dp.toPx()
            val cornerRadius = CornerRadius(canvasW * cornerMorphRatio, canvasW * cornerMorphRatio)

            // Halo Aura Glow behind checkbox
            if (haloAlpha > 0.01f) {
                drawCircle(
                    color = activeColor.copy(alpha = haloAlpha),
                    radius = canvasW * 0.72f,
                    center = Offset(canvasW / 2f, canvasH / 2f)
                )
            }

            // Background Fill
            drawRoundRect(
                color = bgColor,
                size = Size(canvasW, canvasH),
                cornerRadius = cornerRadius,
                style = Fill
            )

            // Border
            drawRoundRect(
                color = borderColor,
                size = Size(canvasW, canvasH),
                cornerRadius = cornerRadius,
                style = Stroke(width = strokeW)
            )

            // Animated Checkmark
            if (checkmarkProgress > 0f) {
                val checkPath = Path().apply {
                    val p1 = Offset(canvasW * 0.28f, canvasH * 0.52f)
                    val p2 = Offset(canvasW * 0.44f, canvasH * 0.68f)
                    val p3 = Offset(canvasW * 0.74f, canvasH * 0.34f)
                    moveTo(p1.x, p1.y)
                    lineTo(p2.x, p2.y)
                    lineTo(p3.x, p3.y)
                }

                val pathMeasure = PathMeasure()
                pathMeasure.setPath(checkPath, false)
                val totalLength = pathMeasure.length
                val partialPath = Path()
                pathMeasure.getSegment(0f, totalLength * checkmarkProgress, partialPath, true)

                drawPath(
                    path = partialPath,
                    color = MidnightBackground,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }
        }
    }
}
