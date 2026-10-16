package com.cllarity.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.cllarity.app.ui.theme.AccentLumen
import com.cllarity.app.ui.theme.AccentLumenLight
import kotlinx.coroutines.delay
import kotlin.random.Random
import com.cllarity.app.ui.theme.Motion

data class ParticleState(
    val id: Int,
    val startOffset: Offset,
    val targetOffset: Offset,
    val curveOffset: Offset,
    val size: Float,
    val color: Color
)

/**
 * Płynna, kinetyczna animacja świetlistych kuleczek XP w stylu Apple / fluid particles.
 * Gdy użytkownik odhacza nawyk lub nabija cel w nałogach, kuleczki energii
 * wzbijają się łukiem w stronę paska postępu.
 */
@Composable
fun FlyingXpParticles(
    trigger: Long, // timestamp lub unikalny id aktywacji
    startOffset: Offset = Offset.Zero,
    targetOffset: Offset = Offset.Zero,
    particleCount: Int = 5,
    onFinished: () -> Unit = {}
) {
    if (trigger <= 0L) return

    val progressAnim = remember(trigger) { Animatable(0f) }

    val particles = remember(trigger) {
        val list = mutableListOf<ParticleState>()
        for (i in 0 until particleCount) {
            val spreadX = (Random.nextFloat() - 0.5f) * 120f
            val spreadY = -Random.nextFloat() * 160f
            val curve = Offset(spreadX, spreadY)
            val size = 7f + Random.nextFloat() * 6f
            val color = if (i % 2 == 0) AccentLumen else AccentLumenLight
            list.add(
                ParticleState(
                    id = i,
                    startOffset = startOffset,
                    targetOffset = targetOffset,
                    curveOffset = curve,
                    size = size,
                    color = color
                )
            )
        }
        list
    }

    LaunchedEffect(trigger) {
        progressAnim.snapTo(0f)
        progressAnim.animateTo(
            targetValue = 1f,
            animationSpec = Motion.timed(650, Motion.EaseStandard)
        )
        delay(50)
        onFinished()
    }

    val t = progressAnim.value
    if (t in 0.001f..0.999f) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val oneMinusT = 1f - t
            particles.forEach { p ->
                // Kąt i krzywa Beziera drugiego stopnia
                val mid = Offset(
                    (p.startOffset.x + p.targetOffset.x) / 2f + p.curveOffset.x,
                    (p.startOffset.y + p.targetOffset.y) / 2f + p.curveOffset.y
                )
                val currentX = oneMinusT * oneMinusT * p.startOffset.x + 2 * oneMinusT * t * mid.x + t * t * p.targetOffset.x
                val currentY = oneMinusT * oneMinusT * p.startOffset.y + 2 * oneMinusT * t * mid.y + t * t * p.targetOffset.y

                val alpha = when {
                    t < 0.2f -> t / 0.2f
                    t > 0.75f -> (1f - t) / 0.25f
                    else -> 1.0f
                }

                // Zewnętrzny delikatny blask (Glow)
                drawCircle(
                    color = p.color.copy(alpha = alpha * 0.35f),
                    radius = p.size * 2.2f,
                    center = Offset(currentX, currentY)
                )

                // Główna lśniąca kuleczka
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = p.size,
                    center = Offset(currentX, currentY)
                )
            }
        }
    }
}
