package com.cllarity.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

private data class ConfettiParticle(
    val startXRatio: Float,
    val startYRatio: Float,
    val velocityX: Float,
    val velocityY: Float,
    val color: Color,
    val size: Float,
    val rotationSpeed: Float,
    val isCircle: Boolean
)

@Composable
fun CelebrationConfetti(
    trigger: Boolean,
    modifier: Modifier = Modifier,
    onFinished: () -> Unit = {}
) {
    if (!trigger) return

    val progress = remember { Animatable(0f) }
    val colors = listOf(
        Color(0xFFFBBF24), // Amber Gold
        Color(0xFF10B981), // Emerald
        Color(0xFF6366F1), // Indigo
        Color(0xFFFB7185), // Rose
        Color(0xFF06B6D4)  // Cyan
    )

    val particles = remember {
        List(48) {
            ConfettiParticle(
                startXRatio = Random.nextFloat() * 0.6f + 0.2f, // 20% to 80% width
                startYRatio = 0.85f, // launch from lower screen
                velocityX = (Random.nextFloat() - 0.5f) * 650f,
                velocityY = -(Random.nextFloat() * 550f + 450f), // shoot up
                color = colors.random(),
                size = Random.nextFloat() * 8f + 6f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f,
                isCircle = Random.nextBoolean()
            )
        }
    }

    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2400, easing = LinearEasing)
        )
        onFinished()
    }

    val t = progress.value
    if (t in 0.001f..0.999f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val gravity = 900f

            particles.forEach { p ->
                val timeSec = t * 2.4f
                val x = p.startXRatio * w + p.velocityX * timeSec
                val y = p.startYRatio * h + p.velocityY * timeSec + 0.5f * gravity * timeSec * timeSec
                val alpha = (1f - (t * 1.15f)).coerceIn(0f, 1f)

                if (y in -50f..h + 50f && x in -50f..w + 50f && alpha > 0.01f) {
                    val rot = p.rotationSpeed * t
                    val curVy = p.velocityY + gravity * timeSec
                    // Apple-style motion stretch encoding speed
                    val speedStretch = (kotlin.math.abs(curVy) / 400f).coerceIn(1.0f, 2.5f)

                    rotate(rot, pivot = Offset(x, y)) {
                        // Subtle motion trail / blur aura
                        drawCircle(
                            color = p.color.copy(alpha = alpha * 0.25f),
                            radius = p.size * 0.85f * speedStretch,
                            center = Offset(x, y)
                        )

                        if (p.isCircle) {
                            drawOval(
                                color = p.color.copy(alpha = alpha),
                                topLeft = Offset(x - p.size / 2f, y - (p.size * speedStretch) / 2f),
                                size = Size(p.size, p.size * speedStretch)
                            )
                        } else {
                            drawRect(
                                color = p.color.copy(alpha = alpha),
                                topLeft = Offset(x - p.size / 2f, y - (p.size * 0.7f * speedStretch) / 2f),
                                size = Size(p.size, p.size * 0.7f * speedStretch)
                            )
                        }
                    }
                }
            }
        }
    }
}
