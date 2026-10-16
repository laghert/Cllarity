package com.cllarity.app.ui.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import kotlin.math.sin

/*
 * LLUMIE EMOTIONS – extra facial expressions & floating mood effects.
 * Body, ears and heart of light stay untouched; only eyes/mouth/FX change.
 */

private val EyeInk = Color(0xFF1E153A)

private fun Path.emoHeart(x: Float, y: Float, s: Float) {
    reset()
    moveTo(x, y + s * 0.85f)
    cubicTo(x - s * 1.25f, y + s * 0.15f, x - s * 0.95f, y - s * 0.95f, x, y - s * 0.38f)
    cubicTo(x + s * 0.95f, y - s * 0.95f, x + s * 1.25f, y + s * 0.15f, x, y + s * 0.85f)
    close()
}

internal fun DrawScope.drawHeartEyes(p: Path, cx: Float, y: Float, spacing: Float, d: Float, blink: Float) {
    for (s in intArrayOf(-1, 1)) {
        val x = cx + s * spacing
        scale(1f, blink.coerceAtLeast(0.15f), Offset(x, y)) {
            p.emoHeart(x, y, d * 0.03f)
            drawPath(p, Brush.radialGradient(listOf(Color(0xFFFDA4AF), Color(0xFFF43F5E), Color(0xFFBE123C)), Offset(x - d * 0.008f, y - d * 0.01f), d * 0.04f))
            drawPath(p, Color(0xFF881337), style = Stroke(width = d * 0.004f))
            drawCircle(Color.White, d * 0.007f, Offset(x - d * 0.011f, y - d * 0.008f))
        }
    }
}

internal fun DrawScope.drawRoundEyes(cx: Float, y: Float, spacing: Float, d: Float) {
    for (s in intArrayOf(-1, 1)) {
        val c = Offset(cx + s * spacing, y)
        drawCircle(EyeInk, d * 0.030f, c)
        drawCircle(Color.White, d * 0.011f, c + Offset(-d * 0.009f, -d * 0.010f))
        drawCircle(Color.White.copy(alpha = 0.7f), d * 0.005f, c + Offset(d * 0.010f, d * 0.010f))
    }
}

internal fun DrawScope.drawStarryEyes(p: Path, cx: Float, y: Float, spacing: Float, d: Float, blink: Float) {
    val eyeW = d * 0.026f; val eyeH = d * 0.046f * blink
    for (s in intArrayOf(-1, 1)) {
        val x = cx + s * spacing
        if (blink <= 0.15f) {
            drawLine(EyeInk, Offset(x - eyeW, y), Offset(x + eyeW, y), d * 0.012f, StrokeCap.Round)
            continue
        }
        drawRoundRect(EyeInk, Offset(x - eyeW, y - eyeH / 2f), Size(eyeW * 2f, eyeH), CornerRadius(eyeW, eyeW))
        drawSparkle(p, x - eyeW * 0.15f, y - eyeH * 0.12f, d * 0.016f, Color(0xFFFDE047))
        drawCircle(Color.White.copy(alpha = 0.8f), d * 0.004f, Offset(x + eyeW * 0.45f, y + eyeH * 0.25f))
    }
}

/** Floating mood effects: love hearts, pride sparkles, sleepy Zzz. */
internal fun DrawScope.drawEmotionFx(
    p: Path,
    cx: Float,
    faceY: Float,
    d: Float,
    state: LlumieState,
    emotion: LlumieEmotion,
    isPetting: Boolean,
    phase: Float
) {
    when {
        state == LlumieState.SLEEPING -> {
            for (k in 0..2) {
                val t = (phase * 1.4f + k / 3f) % 1f
                val size = d * (0.032f + 0.022f * t)
                val x = cx + d * (0.17f + 0.10f * t) + sin(t * 6.28f + k) * d * 0.012f
                val y = faceY - d * (0.08f + 0.20f * t)
                val a = (if (t < 0.15f) t / 0.15f else 1f - (t - 0.15f) / 0.85f).coerceIn(0f, 1f)
                p.reset()
                p.moveTo(x - size / 2f, y - size / 2f)
                p.lineTo(x + size / 2f, y - size / 2f)
                p.lineTo(x - size / 2f, y + size / 2f)
                p.lineTo(x + size / 2f, y + size / 2f)
                val zColor = if (emotion == LlumieEmotion.CALM) Color(0xFF67E8F9) else Color(0xFFC4B5FD)
                drawPath(p, zColor.copy(alpha = 0.95f * a), style = Stroke(width = d * 0.011f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }
        isPetting || emotion == LlumieEmotion.LOVING -> {
            for (k in 0..2) {
                val t = (phase * 1.6f + k / 3f) % 1f
                val x = cx + (if (k == 1) -1 else 1) * d * (0.21f + 0.03f * sin(t * 6.28f + k))
                val y = faceY - d * (0.04f + 0.22f * t)
                val a = (if (t < 0.15f) t / 0.15f else 1f - (t - 0.15f) / 0.85f).coerceIn(0f, 1f)
                p.emoHeart(x, y, d * 0.032f * (0.7f + 0.3f * (1f - t)))
                drawPath(p, Color(0xFFFB7185).copy(alpha = a))
                drawCircle(Color.White.copy(alpha = 0.7f * a), d * 0.004f, Offset(x - d * 0.007f, y - d * 0.005f))
            }
        }
        emotion == LlumieEmotion.PROUD -> {
            val pts = floatArrayOf(-0.22f, -0.10f, 0.23f, -0.05f, 0.18f, -0.22f)
            for (k in 0..2) {
                val tw = (0.5f + 0.5f * sin((phase * 3f + k * 0.33f) * 6.28f))
                drawSparkle(p, cx + pts[k * 2] * d, faceY + pts[k * 2 + 1] * d, d * (0.018f + 0.01f * tw), Color(0xFFFDE047), 0.35f + 0.65f * tw)
            }
        }
    }
}
