package com.cllarity.app.ui.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import com.cllarity.app.model.ClothingColor
import com.cllarity.app.model.LlumieCosmetic
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/*
 * LLUMIE WARDROBE ENGINE
 * Every garment & gadget is fitted to Llumie's exact silhouette (the very same Bézier
 * body path), so clothes hug the bunny instead of floating over it. All sizes are
 * relative to the canvas dimension `d` – items look identical at 110dp and 205dp.
 * The bunny itself (ears, body, heart of light) is NOT modified here.
 */

private val Ink = Color(0xFF1E153A)
private val GoldLight = Color(0xFFFEF3C7)
private val Gold = Color(0xFFFBBF24)
private val GoldDeep = Color(0xFFD97706)
private val GoldShadow = Color(0xFF92400E)

/** Geometry mirror of drawEtherealBody – lets items sit exactly on the body contour. */
internal class WardrobeGeo(
    val cx: Float,
    val cy: Float,
    val d: Float,
    val bodyTop: Float,
    val bodyBottom: Float,
    val bodyW: Float
) {
    val headRX = bodyW * 0.35f
    val bellyRX = bodyW * 0.50f
    val faceY = cy - d * 0.05f
    val eyeSpacing = d * 0.105f
    val heartY = cy + d * 0.06f
    private val domeBaseY = bodyTop + d * 0.08f
    private val sideEndY = bodyBottom - d * 0.06f

    /** Half-width of Llumie's silhouette at a given Y (analytic, allocation free). */
    fun halfWidthAt(y: Float): Float {
        if (y <= domeBaseY) {
            val k = ((domeBaseY - y) / (d * 0.075f)).coerceIn(0f, 1f)
            return headRX * sqrt(1f - k * k)
        }
        if (y >= sideEndY) {
            val k = ((y - sideEndY) / (d * 0.0675f)).coerceIn(0f, 1f)
            return bellyRX * sqrt(1f - k * k)
        }
        val y0 = domeBaseY; val y1 = cy - d * 0.06f; val y2 = cy + d * 0.16f; val y3 = sideEndY
        val x0 = headRX; val x1 = headRX * 1.15f; val x2 = bellyRX * 1.05f; val x3 = bellyRX
        var lo = 0f; var hi = 1f
        repeat(16) {
            val t = (lo + hi) / 2f
            if (bez(y0, y1, y2, y3, t) < y) lo = t else hi = t
        }
        return bez(x0, x1, x2, x3, (lo + hi) / 2f)
    }

    private fun bez(a: Float, b: Float, c: Float, e: Float, t: Float): Float {
        val u = 1f - t
        return u * u * u * a + 3f * u * u * t * b + 3f * u * t * t * c + t * t * t * e
    }
}

/** Reusable path cache – zero Path allocations per frame. */
internal class WardrobePaths {
    val a = Path()
    val b = Path()
    val c = Path()
    val e = Path()
    val f = Path()
}

private fun shade(c: Color, f: Float) = lerp(c, Ink, f)
private fun tint(c: Color, f: Float) = lerp(c, Color.White, f)

/** Luminance helper – picks readable trims for very dark / very light fabrics. */
private fun Color.luma() = 0.2126f * red + 0.7152f * green + 0.0722f * blue

/** Builds a region from a curved neckline down to a rounded hem (later clipped to the body). */
private fun Path.garmentRegion(g: WardrobeGeo, neckY: Float, neckDip: Float, neckHalfW: Float, hemY: Float, hemLift: Float) {
    reset()
    val wide = g.d * 0.6f
    moveTo(g.cx - wide, neckY)
    lineTo(g.cx - neckHalfW, neckY)
    cubicTo(g.cx - neckHalfW * 0.55f, neckY + neckDip, g.cx + neckHalfW * 0.55f, neckY + neckDip, g.cx + neckHalfW, neckY)
    lineTo(g.cx + wide, neckY)
    lineTo(g.cx + wide, hemY - hemLift)
    quadraticTo(g.cx, hemY + hemLift, g.cx - wide, hemY - hemLift)
    close()
}

/** Fill a region clipped to Llumie's body + a soft fabric rim that hugs the silhouette. */
private fun DrawScope.fillFitted(bodyPath: Path, region: Path, brush: Brush, rim: Color, d: Float) {
    clipPath(bodyPath) {
        // Soft contact shadow just above the garment edge – separates fabric from fur
        translate(0f, -d * 0.010f) { drawPath(region, Ink.copy(alpha = 0.22f)) }
        translate(0f, -d * 0.005f) { drawPath(region, Ink.copy(alpha = 0.18f)) }
        drawPath(region, brush)
    }
    clipPath(region) {
        drawPath(bodyPath, rim, style = Stroke(width = d * 0.014f))
    }
}

/** Side shading that turns a flat fill into a rounded, 3D garment. */
private fun DrawScope.roundShading(bodyPath: Path, region: Path, g: WardrobeGeo, top: Float, bottom: Float) {
    clipPath(bodyPath) {
        clipPath(region) {
            drawRect(
                brush = Brush.horizontalGradient(
                    0f to Color.Black.copy(alpha = 0.32f),
                    0.22f to Color.Transparent,
                    0.62f to Color.White.copy(alpha = 0.07f),
                    0.80f to Color.Transparent,
                    1f to Color.Black.copy(alpha = 0.36f),
                    startX = g.cx - g.bellyRX * 1.05f,
                    endX = g.cx + g.bellyRX * 1.05f
                ),
                topLeft = Offset(g.cx - g.d * 0.5f, top),
                size = Size(g.d, bottom - top)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// PRIMITIVES
// ---------------------------------------------------------------------------

/** Classic 5-point star. */
private fun Path.star5(x: Float, y: Float, r: Float, inner: Float = 0.45f, rotDeg: Float = 0f) {
    reset()
    for (i in 0 until 10) {
        val rr = if (i % 2 == 0) r else r * inner
        val ang = Math.toRadians((-90.0 + rotDeg + i * 36.0))
        val px = x + (rr * cos(ang)).toFloat()
        val py = y + (rr * sin(ang)).toFloat()
        if (i == 0) moveTo(px, py) else lineTo(px, py)
    }
    close()
}

/** Soft 4-point sparkle. */
private fun Path.sparkle(x: Float, y: Float, r: Float) {
    reset()
    moveTo(x, y - r)
    quadraticTo(x + r * 0.12f, y - r * 0.12f, x + r, y)
    quadraticTo(x + r * 0.12f, y + r * 0.12f, x, y + r)
    quadraticTo(x - r * 0.12f, y + r * 0.12f, x - r, y)
    quadraticTo(x - r * 0.12f, y - r * 0.12f, x, y - r)
    close()
}

private fun Path.heart(x: Float, y: Float, s: Float) {
    reset()
    moveTo(x, y + s * 0.85f)
    cubicTo(x - s * 1.25f, y + s * 0.15f, x - s * 0.95f, y - s * 0.95f, x, y - s * 0.38f)
    cubicTo(x + s * 0.95f, y - s * 0.95f, x + s * 1.25f, y + s * 0.15f, x, y + s * 0.85f)
    close()
}

internal fun DrawScope.drawSparkle(p: Path, x: Float, y: Float, r: Float, color: Color, alphaIn: Float = 1f) {
    val alpha = alphaIn.coerceIn(0f, 1f)
    drawCircle(
        Brush.radialGradient(listOf(color.copy(alpha = 0.45f * alpha), Color.Transparent), Offset(x, y), r * 1.6f),
        r * 1.6f, Offset(x, y)
    )
    p.sparkle(x, y, r)
    drawPath(p, color.copy(alpha = alpha))
    drawCircle(Color.White.copy(alpha = alpha), r * 0.18f, Offset(x, y))
}

/** Llumie's heart of light glowing THROUGH the fabric – keeps the mascot's soul visible. */
private fun DrawScope.heartGlowThrough(p: Path, g: WardrobeGeo, size: Float = 0.032f, strength: Float = 1f) {
    val r = g.d * size
    drawCircle(
        Brush.radialGradient(
            listOf(Color(0xAA67E8F9).copy(alpha = 0.65f * strength), Color(0x3338BDF8), Color.Transparent),
            Offset(g.cx, g.heartY), r * 3.2f
        ), r * 3.2f, Offset(g.cx, g.heartY)
    )
    p.heart(g.cx, g.heartY, r)
    drawPath(p, Brush.radialGradient(listOf(Color.White, Color(0xFFA5F3FC), Color(0xFF38BDF8)), Offset(g.cx, g.heartY - r * 0.2f), r * 1.3f))
    drawPath(p, Color.White.copy(alpha = 0.6f), style = Stroke(width = g.d * 0.004f))
}

private fun Path.neckline(g: WardrobeGeo, neckY: Float, dip: Float, hw: Float) {
    reset()
    moveTo(g.cx - hw, neckY)
    cubicTo(g.cx - hw * 0.55f, neckY + dip, g.cx + hw * 0.55f, neckY + dip, g.cx + hw, neckY)
}

private fun Path.hemline(g: WardrobeGeo, hemY: Float, lift: Float) {
    reset()
    val wide = g.bellyRX * 1.2f
    moveTo(g.cx - wide, hemY - lift)
    quadraticTo(g.cx, hemY + lift, g.cx + wide, hemY - lift)
}

/** Dashed stitching that follows a path. */
private fun DrawScope.stitch(path: Path, color: Color, d: Float) {
    drawPath(
        path, color,
        style = Stroke(
            width = d * 0.0045f, cap = StrokeCap.Round,
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(d * 0.012f, d * 0.009f))
        )
    )
}

/** Ribbed knit band (collar/hem): thick stroke + tiny vertical ribs. */
private fun DrawScope.ribBand(bodyPath: Path, path: Path, g: WardrobeGeo, base: Color, width: Float, ribTop: Float, ribBottom: Float) {
    clipPath(bodyPath) {
        drawPath(path, base, style = Stroke(width = width, cap = StrokeCap.Round))
        drawPath(path, Color.White.copy(alpha = 0.18f), style = Stroke(width = width * 0.25f, cap = StrokeCap.Round))
    }
    val step = g.d * 0.016f
    var x = g.cx - g.bellyRX * 1.1f
    clipPath(bodyPath) {
        while (x < g.cx + g.bellyRX * 1.1f) {
            drawLine(Color.Black.copy(alpha = 0.14f), Offset(x, ribTop), Offset(x, ribBottom), g.d * 0.0035f)
            x += step
        }
    }
}

// ===========================================================================
// BACK LAYER (drawn BEFORE the body): wings, aura, halo, capes, sleeves, backpack
// ===========================================================================
internal fun DrawScope.drawWardrobeBack(
    w: WardrobePaths,
    g: WardrobeGeo,
    set: Set<LlumieCosmetic>,
    clothing: ClothingColor,
    orbitDeg: Float,
    pulse: Float,
    flap: Float,
    sleeping: Boolean
) {
    val c = Color(clothing.hexColor)
    if (LlumieCosmetic.PHOENIX_WINGS in set) drawPhoenixWingsV2(w, g, flap, pulse, orbitDeg)
    if (LlumieCosmetic.AURA_STARS in set) drawAuraStarsV2(w, g, orbitDeg, back = true)
    if (LlumieCosmetic.GLOWING_HALO in set && !sleeping) drawHaloV2(w, g, pulse, orbitDeg)
    if (sleeping) return
    if (LlumieCosmetic.TRAVEL_BACKPACK in set) drawBackpackBack(w, g)
    if (LlumieCosmetic.STOIC_CAPE in set) drawCapeBack(w, g, c)
    if (LlumieCosmetic.ZEN_KIMONO in set) drawHangingSleeves(w, g, c, flare = 0.06f, cuff = tint(c, 0.55f))
    if (LlumieCosmetic.MAGE_ROBE in set) {
        drawMageCollarBack(w, g, c)
        drawHangingSleeves(w, g, shade(c, 0.35f), flare = 0.09f, cuff = Gold)
    }
}

private val wingRowColors = arrayOf(
    listOf(Color(0xFFFDE047), Color(0xFFFB923C), Color(0xFFE11D48)),
    listOf(Color(0xFFFEF08A), Color(0xFFFBBF24), Color(0xFFF97316)),
    listOf(Color(0xFFFFFBEB), Color(0xFFFEF08A), Color(0xFFFBBF24))
)

/** Point on the wing "arm" arc (quadratic Bézier from shoulder to wingtip). */
private fun wingArm(s: Float, t: Float, sx: Float, sy: Float, d: Float): Offset {
    val cxp = sx + s * d * 0.16f; val cyp = sy - d * 0.24f
    val tx = sx + s * d * 0.32f; val ty = sy - d * 0.17f
    val u = 1f - t
    return Offset(u * u * sx + 2f * u * t * cxp + t * t * tx, u * u * sy + 2f * u * t * cyp + t * t * ty)
}

private fun DrawScope.drawPhoenixWingsV2(w: WardrobePaths, g: WardrobeGeo, flap: Float, pulse: Float, orbitDeg: Float) {
    val d = g.d
    val pivotY = g.cy - d * 0.02f
    // Solar aura
    drawCircle(
        Brush.radialGradient(
            listOf(Color(0x77FDE047).copy(alpha = 0.40f * pulse), Color(0x33F97316), Color(0x11E11D48), Color.Transparent),
            Offset(g.cx, pivotY), d * 0.55f
        ), d * 0.55f, Offset(g.cx, pivotY)
    )
    for (si in intArrayOf(-1, 1)) {
        val s = si.toFloat()
        val sx = g.cx + s * d * 0.12f
        rotate(s * flap * 1.4f, Offset(sx, pivotY)) {
            // Feather rows: back (long, crimson) -> front (short, golden)
            for (row in 0..2) {
                val n = 7 - row
                for (i in n - 1 downTo 0) {
                    val t = 0.12f + 0.88f * i / (n - 1).toFloat()
                    val b = wingArm(s, t, sx, pivotY, d)
                    val theta = Math.toRadians((100f - 78f * t).toDouble())
                    val ux = s * cos(theta).toFloat(); val uy = sin(theta).toFloat()
                    val nx = -uy; val ny = ux
                    val len = d * (0.10f + 0.17f * t) * (1f - row * 0.30f)
                    val wid = d * (0.026f + 0.010f * t)
                    val tip = Offset(b.x + ux * len, b.y + uy * len)
                    w.a.reset()
                    w.a.moveTo(b.x - nx * wid * 0.6f, b.y - ny * wid * 0.6f)
                    w.a.cubicTo(b.x + ux * len * 0.5f - nx * wid, b.y + uy * len * 0.5f - ny * wid,
                        tip.x - nx * wid * 0.25f, tip.y - ny * wid * 0.25f, tip.x, tip.y)
                    w.a.cubicTo(tip.x + nx * wid * 0.35f, tip.y + ny * wid * 0.35f,
                        b.x + ux * len * 0.5f + nx * wid, b.y + uy * len * 0.5f + ny * wid,
                        b.x + nx * wid * 0.6f, b.y + ny * wid * 0.6f)
                    w.a.close()
                    drawPath(w.a, Brush.linearGradient(wingRowColors[row], b, tip))
                    drawPath(w.a, Color(0xFFFFFBEB).copy(alpha = 0.45f), style = Stroke(width = d * 0.003f))
                    drawLine(Color(0xFFFFFBEB).copy(alpha = 0.55f), b, Offset(b.x + ux * len * 0.8f, b.y + uy * len * 0.8f), d * 0.0025f, StrokeCap.Round)
                }
            }
            // Wing arm / coverts
            w.b.reset()
            val a0 = wingArm(s, 0f, sx, pivotY, d)
            w.b.moveTo(a0.x, a0.y + d * 0.04f)
            var k = 0
            while (k <= 10) {
                val p = wingArm(s, k / 10f, sx, pivotY, d)
                w.b.lineTo(p.x, p.y)
                k++
            }
            k = 10
            while (k >= 0) {
                val p = wingArm(s, k / 10f, sx, pivotY, d)
                w.b.lineTo(p.x - s * d * 0.01f * (k / 10f), p.y + d * (0.045f - 0.03f * k / 10f))
                k--
            }
            w.b.close()
            drawPath(w.b, Brush.linearGradient(listOf(Color(0xFFFFFBEB), Color(0xFFFDE047), Color(0xFFFBBF24)), a0, wingArm(s, 1f, sx, pivotY, d)))
            drawPath(w.b, Color(0xFFFFFBEB).copy(alpha = 0.7f), style = Stroke(width = d * 0.004f))
        }
        // Rising embers
        for (k in 0..2) {
            val t = ((orbitDeg / 360f * 3f + k * 0.37f) % 1f)
            val ex = sx + s * d * (0.14f + k * 0.08f)
            val ey = pivotY - d * (0.16f + t * 0.16f)
            drawSparkle(w.c, ex, ey, d * (0.012f - k * 0.002f), if (k == 1) Color(0xFFF43F5E) else Color(0xFFFDE047), 1f - t * 0.6f)
        }
    }
}

private fun DrawScope.drawAuraStarsV2(w: WardrobePaths, g: WardrobeGeo, orbitDeg: Float, back: Boolean) {
    val d = g.d
    for (i in 0 until 9) {
        val ang = Math.toRadians((orbitDeg + i * 40f).toDouble())
        val depth = sin(ang).toFloat()
        if (back != (depth < 0f)) continue
        val x = g.cx + (d * 0.40f * cos(ang)).toFloat()
        val y = g.cy + d * 0.03f + d * 0.12f * depth - d * 0.06f * cos(ang * 2).toFloat() * 0.3f
        val scale = 0.70f + 0.30f * (depth + 1f) / 2f
        val twinkle = (0.55f + 0.45f * sin(ang * 3 + i)).toFloat().coerceIn(0.3f, 1f)
        if (i % 3 == 2) {
            drawCircle(Color(0xFF67E8F9).copy(alpha = 0.35f * twinkle), d * 0.014f * scale, Offset(x, y))
            drawCircle(Color(0xFFE0F2FE).copy(alpha = twinkle), d * 0.006f * scale, Offset(x, y))
        } else {
            drawSparkle(w.b, x, y, d * (if (i % 2 == 0) 0.024f else 0.017f) * scale, Color(0xFFFDE047), twinkle)
        }
    }
}

private fun DrawScope.drawHaloV2(w: WardrobePaths, g: WardrobeGeo, pulse: Float, orbitDeg: Float) {
    val d = g.d
    val hy = g.bodyTop - d * 0.10f + (pulse - 0.775f) * d * 0.05f
    val rx = d * 0.12f; val ry = d * 0.032f
    val tl = Offset(g.cx - rx, hy - ry); val sz = Size(rx * 2, ry * 2)
    drawOval(Color(0xFFFDE047).copy(alpha = 0.22f * pulse), tl, sz, style = Stroke(width = d * 0.045f))
    drawOval(Color(0xFFFFD54F).copy(alpha = 0.35f * pulse), tl, sz, style = Stroke(width = d * 0.024f))
    drawOval(
        Brush.horizontalGradient(listOf(GoldDeep, Gold, GoldLight, Gold, GoldDeep), startX = g.cx - rx, endX = g.cx + rx),
        tl, sz, style = Stroke(width = d * 0.013f)
    )
    drawArc(Color.White.copy(alpha = 0.8f * pulse), 200f, 140f, false, tl, sz, style = Stroke(width = d * 0.004f, cap = StrokeCap.Round))
    for (k in 0..2) {
        val a = Math.toRadians((orbitDeg * 1.5f + k * 120f).toDouble())
        drawSparkle(w.b, g.cx + (rx * cos(a)).toFloat(), hy + (ry * sin(a)).toFloat(), d * 0.014f, Color(0xFFFFF7D6), 0.6f + 0.4f * pulse)
    }
}

private val Leather = Color(0xFF8B4513)
private val LeatherDark = Color(0xFF5C2E0E)
private val LeatherLight = Color(0xFFB7703A)

private fun DrawScope.drawBackpackBack(w: WardrobePaths, g: WardrobeGeo) {
    val d = g.d
    // Rolled blanket on top (peeks out beside the head)
    val rollY = g.cy - d * 0.085f
    drawRoundRect(
        Brush.verticalGradient(listOf(Color(0xFF5EEAD4), Color(0xFF0F766E)), rollY, rollY + d * 0.06f),
        Offset(g.cx - d * 0.26f, rollY), Size(d * 0.52f, d * 0.06f), CornerRadius(d * 0.03f)
    )
    for (sx in floatArrayOf(-0.20f, 0.20f)) {
        drawRect(LeatherDark, Offset(g.cx + sx * d - d * 0.008f, rollY - d * 0.004f), Size(d * 0.016f, d * 0.068f))
    }
    // Main pack body
    val top = g.cy - d * 0.04f; val h = d * 0.23f; val half = d * 0.285f
    drawRoundRect(
        Brush.verticalGradient(listOf(LeatherLight, Leather, LeatherDark), top, top + h),
        Offset(g.cx - half, top), Size(half * 2, h), CornerRadius(d * 0.06f)
    )
    // Side pockets with flaps & brass studs
    for (s in intArrayOf(-1, 1)) {
        val px = g.cx + s * (half - d * 0.012f)
        drawRoundRect(Leather, Offset(px - d * 0.035f, top + h * 0.40f), Size(d * 0.07f, h * 0.52f), CornerRadius(d * 0.022f))
        drawRoundRect(LeatherDark, Offset(px - d * 0.037f, top + h * 0.40f), Size(d * 0.074f, h * 0.17f), CornerRadius(d * 0.02f))
        drawCircle(Gold, d * 0.008f, Offset(px + s * d * 0.012f, top + h * 0.52f))
    }
}

private fun DrawScope.drawCapeBack(w: WardrobePaths, g: WardrobeGeo, c: Color) {
    val d = g.d
    val p = w.a
    val hemY = g.bodyBottom + d * 0.012f
    p.reset()
    p.moveTo(g.cx - d * 0.18f, g.cy - d * 0.07f)
    p.cubicTo(g.cx - d * 0.30f, g.cy + d * 0.02f, g.cx - d * 0.37f, g.cy + d * 0.16f, g.cx - d * 0.35f, hemY - d * 0.01f)
    p.quadraticTo(g.cx - d * 0.29f, hemY + d * 0.025f, g.cx - d * 0.22f, hemY)
    p.quadraticTo(g.cx - d * 0.11f, hemY + d * 0.03f, g.cx, hemY + d * 0.005f)
    p.quadraticTo(g.cx + d * 0.11f, hemY + d * 0.03f, g.cx + d * 0.22f, hemY)
    p.quadraticTo(g.cx + d * 0.29f, hemY + d * 0.025f, g.cx + d * 0.35f, hemY - d * 0.01f)
    p.cubicTo(g.cx + d * 0.37f, g.cy + d * 0.16f, g.cx + d * 0.30f, g.cy + d * 0.02f, g.cx + d * 0.18f, g.cy - d * 0.07f)
    p.close()
    drawPath(p, Brush.verticalGradient(listOf(shade(c, 0.15f), shade(c, 0.35f), shade(c, 0.60f)), g.cy - d * 0.07f, hemY + d * 0.03f))
    // Drapery folds on the visible flanks
    for (s in intArrayOf(-1, 1)) {
        w.b.reset()
        w.b.moveTo(g.cx + s * d * 0.26f, g.cy + d * 0.02f)
        w.b.quadraticTo(g.cx + s * d * 0.33f, g.cy + d * 0.12f, g.cx + s * d * 0.29f, hemY + d * 0.01f)
        drawPath(w.b, Color.Black.copy(alpha = 0.22f), style = Stroke(width = d * 0.008f, cap = StrokeCap.Round))
        w.b.reset()
        w.b.moveTo(g.cx + s * d * 0.23f, g.cy + d * 0.0f)
        w.b.quadraticTo(g.cx + s * d * 0.30f, g.cy + d * 0.10f, g.cx + s * d * 0.27f, g.cy + d * 0.19f)
        drawPath(w.b, tint(c, 0.4f).copy(alpha = 0.35f), style = Stroke(width = d * 0.004f, cap = StrokeCap.Round))
    }
    drawPath(p, Gold.copy(alpha = 0.75f), style = Stroke(width = d * 0.005f))
}

private fun DrawScope.drawHangingSleeves(w: WardrobePaths, g: WardrobeGeo, c: Color, flare: Float, cuff: Color) {
    val d = g.d
    val topY = g.cy - d * 0.01f
    val botY = g.cy + d * 0.20f
    for (s in intArrayOf(-1, 1)) {
        val edge = g.halfWidthAt(topY)
        val p = w.a
        p.reset()
        p.moveTo(g.cx + s * (edge - d * 0.03f), topY - d * 0.01f)
        p.quadraticTo(g.cx + s * (edge + d * 0.04f), topY, g.cx + s * (g.bellyRX + d * flare), botY - d * 0.02f)
        p.quadraticTo(g.cx + s * (g.bellyRX + d * flare * 0.6f), botY + d * 0.012f, g.cx + s * (g.bellyRX - d * 0.04f), botY)
        p.close()
        drawPath(p, Brush.verticalGradient(listOf(c, shade(c, 0.40f)), topY, botY))
        w.b.reset()
        w.b.moveTo(g.cx + s * (g.bellyRX + d * flare), botY - d * 0.02f)
        w.b.quadraticTo(g.cx + s * (g.bellyRX + d * flare * 0.6f), botY + d * 0.012f, g.cx + s * (g.bellyRX - d * 0.04f), botY)
        drawPath(w.b, cuff, style = Stroke(width = d * 0.012f, cap = StrokeCap.Round))
    }
}

private fun DrawScope.drawMageCollarBack(w: WardrobePaths, g: WardrobeGeo, c: Color) {
    val d = g.d
    for (s in intArrayOf(-1, 1)) {
        val p = w.a
        p.reset()
        p.moveTo(g.cx + s * d * 0.10f, g.cy + d * 0.03f)
        p.cubicTo(g.cx + s * d * 0.30f, g.cy + d * 0.02f, g.cx + s * d * 0.31f, g.cy - d * 0.08f, g.cx + s * d * 0.27f, g.cy - d * 0.10f)
        p.quadraticTo(g.cx + s * d * 0.22f, g.cy - d * 0.08f, g.cx + s * d * 0.15f, g.cy - d * 0.06f)
        p.close()
        drawPath(p, Brush.verticalGradient(listOf(shade(c, 0.25f), shade(c, 0.65f)), g.cy - d * 0.10f, g.cy + d * 0.03f))
        drawPath(p, Gold, style = Stroke(width = d * 0.006f))
    }
}

// ===========================================================================
// CLOTHING LAYER (drawn AFTER the body, BEFORE the face)
// ===========================================================================
internal fun DrawScope.drawWardrobeClothing(
    w: WardrobePaths,
    bodyPath: Path,
    g: WardrobeGeo,
    set: Set<LlumieCosmetic>,
    clothing: ClothingColor
) {
    val c = Color(clothing.hexColor)
    if (LlumieCosmetic.SWEATER in set) drawSweater(w, bodyPath, g, c)
    if (LlumieCosmetic.TSHIRT in set) drawTShirt(w, bodyPath, g, c)
    if (LlumieCosmetic.HOODIE in set) drawHoodie(w, bodyPath, g, c)
    if (LlumieCosmetic.STOIC_CAPE in set) drawCapeFront(w, bodyPath, g, c)
    if (LlumieCosmetic.ZEN_KIMONO in set) drawKimono(w, bodyPath, g, c)
    if (LlumieCosmetic.MAGE_ROBE in set) drawMageRobe(w, bodyPath, g, c)
    if (LlumieCosmetic.GOLDEN_ARMOR in set) drawArmor(w, bodyPath, g, c)
}

private fun fabricBrush(c: Color, top: Float, bottom: Float) =
    Brush.verticalGradient(listOf(tint(c, 0.14f), c, shade(c, 0.28f)), top, bottom)

private fun DrawScope.drawSweater(w: WardrobePaths, body: Path, g: WardrobeGeo, c: Color) {
    val d = g.d
    val neckY = g.cy + d * 0.022f; val dip = d * 0.03f; val hw = g.headRX * 0.62f
    val hemY = g.cy + d * 0.205f; val lift = d * 0.018f
    w.a.garmentRegion(g, neckY, dip, hw, hemY, lift)
    fillFitted(body, w.a, fabricBrush(c, neckY, hemY), shade(c, 0.35f), d)
    roundShading(body, w.a, g, neckY, hemY + lift)
    clipPath(body) {
        clipPath(w.a) {
            // Knit "V" stitch texture
            var ky = neckY + d * 0.03f
            var row = 0
            while (ky < hemY - d * 0.035f) {
                var kx = g.cx - g.bellyRX * 1.05f + (row % 2) * d * 0.011f
                while (kx < g.cx + g.bellyRX * 1.05f) {
                    drawLine(Color.White.copy(alpha = 0.13f), Offset(kx - d * 0.006f, ky), Offset(kx, ky + d * 0.007f), d * 0.003f, StrokeCap.Round)
                    drawLine(Color.White.copy(alpha = 0.13f), Offset(kx + d * 0.006f, ky), Offset(kx, ky + d * 0.007f), d * 0.003f, StrokeCap.Round)
                    kx += d * 0.022f
                }
                ky += d * 0.018f; row++
            }
            // Raglan seams suggesting soft sleeves
            for (s in intArrayOf(-1, 1)) {
                w.b.reset()
                w.b.moveTo(g.cx + s * hw * 1.05f, neckY + d * 0.004f)
                w.b.quadraticTo(g.cx + s * d * 0.17f, g.cy + d * 0.07f, g.cx + s * (g.halfWidthAt(g.cy + d * 0.13f) - d * 0.01f), g.cy + d * 0.13f)
                drawPath(w.b, shade(c, 0.32f).copy(alpha = 0.7f), style = Stroke(width = d * 0.005f, cap = StrokeCap.Round))
            }
            // Cable-knit braids
            for (s in intArrayOf(-1, 1)) {
                val x = g.cx + s * d * 0.085f
                var y = neckY + d * 0.035f
                val step = d * 0.026f
                while (y < hemY - d * 0.04f) {
                    drawLine(tint(c, 0.35f).copy(alpha = 0.55f), Offset(x - d * 0.012f, y), Offset(x + d * 0.012f, y + step), d * 0.006f, StrokeCap.Round)
                    drawLine(shade(c, 0.25f).copy(alpha = 0.55f), Offset(x + d * 0.012f, y), Offset(x - d * 0.012f, y + step), d * 0.006f, StrokeCap.Round)
                    y += step
                }
                drawLine(shade(c, 0.3f).copy(alpha = 0.35f), Offset(x - d * 0.022f, neckY + d * 0.03f), Offset(x - d * 0.022f, hemY - d * 0.035f), d * 0.003f)
                drawLine(shade(c, 0.3f).copy(alpha = 0.35f), Offset(x + d * 0.022f, neckY + d * 0.03f), Offset(x + d * 0.022f, hemY - d * 0.035f), d * 0.003f)
            }
        }
    }
    // Ribbed collar & hem (light contrasting yarn so the sweater reads on any fur tone)
    w.b.neckline(g, neckY + d * 0.004f, dip, hw)
    clipPath(body) { drawPath(w.b, Ink.copy(alpha = 0.35f), style = Stroke(width = d * 0.03f, cap = StrokeCap.Round)) }
    ribBand(body, w.b, g, tint(c, 0.55f), d * 0.022f, neckY - d * 0.004f, neckY + dip * 0.8f + d * 0.012f)
    w.b.hemline(g, hemY - d * 0.016f, lift)
    ribBand(body, w.b, g, tint(c, 0.45f), d * 0.03f, hemY - d * 0.05f, hemY + lift)
    heartGlowThrough(w.c, g, 0.028f)
}

private fun DrawScope.drawTShirt(w: WardrobePaths, body: Path, g: WardrobeGeo, c: Color) {
    val d = g.d
    val neckY = g.cy + d * 0.024f; val dip = d * 0.026f; val hw = g.headRX * 0.55f
    val hemY = g.cy + d * 0.158f; val lift = d * 0.014f
    w.a.garmentRegion(g, neckY, dip, hw, hemY, lift)
    fillFitted(body, w.a, fabricBrush(c, neckY, hemY), shade(c, 0.30f), d)
    roundShading(body, w.a, g, neckY, hemY + lift)
    val sleeveY = g.cy + d * 0.105f
    clipPath(body) {
        clipPath(w.a) {
            for (s in intArrayOf(-1, 1)) {
                val edge = g.halfWidthAt(sleeveY)
                val sx = g.cx + s * (hw + d * 0.03f)
                val ex = g.cx + s * (edge - d * 0.05f)
                w.b.reset()
                w.b.moveTo(sx, neckY - d * 0.01f)
                w.b.quadraticTo(g.cx + s * (edge - d * 0.085f), g.cy + d * 0.06f, ex, sleeveY)
                w.b.lineTo(g.cx + s * (edge + d * 0.05f), sleeveY - d * 0.012f)
                w.b.lineTo(g.cx + s * (edge + d * 0.05f), neckY - d * 0.02f)
                w.b.close()
                drawPath(w.b, Color.Black.copy(alpha = 0.10f))
                drawPath(w.b, shade(c, 0.32f).copy(alpha = 0.6f), style = Stroke(width = d * 0.004f))
                drawLine(tint(c, 0.45f).copy(alpha = 0.7f), Offset(ex, sleeveY - d * 0.006f), Offset(g.cx + s * (edge + d * 0.02f), sleeveY - d * 0.016f), d * 0.006f, StrokeCap.Round)
            }
        }
    }
    w.b.neckline(g, neckY + d * 0.003f, dip, hw)
    clipPath(body) { drawPath(w.b, tint(c, 0.38f), style = Stroke(width = d * 0.014f, cap = StrokeCap.Round)) }
    w.b.hemline(g, hemY - d * 0.012f, lift)
    clipPath(body) { stitch(w.b, tint(c, 0.45f).copy(alpha = 0.8f), d) }
    // Printed star logo + heart of light shining through
    heartGlowThrough(w.c, g, 0.026f)
    w.c.star5(g.cx + d * 0.085f, neckY + d * 0.05f, d * 0.02f)
    drawPath(w.c, Color.White.copy(alpha = 0.92f))
    drawPath(w.c, shade(c, 0.3f).copy(alpha = 0.5f), style = Stroke(width = d * 0.003f))
}

private fun DrawScope.drawHoodie(w: WardrobePaths, body: Path, g: WardrobeGeo, c: Color) {
    val d = g.d
    val neckY = g.cy + d * 0.018f; val dip = d * 0.028f; val hw = g.headRX * 0.70f
    val hemY = g.cy + d * 0.222f; val lift = d * 0.02f
    w.a.garmentRegion(g, neckY, dip, hw, hemY, lift)
    fillFitted(body, w.a, fabricBrush(c, neckY, hemY), shade(c, 0.35f), d)
    roundShading(body, w.a, g, neckY, hemY + lift)
    // Soft light of the heart diffusing through the thick fabric
    clipPath(body) {
        clipPath(w.a) {
            drawCircle(
                Brush.radialGradient(listOf(Color(0x5567E8F9), Color.Transparent), Offset(g.cx, g.heartY + d * 0.01f), d * 0.09f),
                d * 0.09f, Offset(g.cx, g.heartY + d * 0.01f)
            )
        }
    }
    // Hood resting around the neck (crescent with dark lining)
    val p = w.b
    p.reset()
    p.moveTo(g.cx - hw - d * 0.045f, neckY - d * 0.012f)
    p.cubicTo(g.cx - hw * 0.8f, neckY + dip + d * 0.03f, g.cx + hw * 0.8f, neckY + dip + d * 0.03f, g.cx + hw + d * 0.045f, neckY - d * 0.012f)
    p.lineTo(g.cx + hw, neckY)
    p.cubicTo(g.cx + hw * 0.55f, neckY + dip, g.cx - hw * 0.55f, neckY + dip, g.cx - hw, neckY)
    p.close()
    clipPath(body) {
        drawPath(p, Brush.verticalGradient(listOf(tint(c, 0.18f), shade(c, 0.12f)), neckY - d * 0.01f, neckY + dip + d * 0.03f))
        drawPath(p, shade(c, 0.4f).copy(alpha = 0.6f), style = Stroke(width = d * 0.004f))
    }
    w.c.neckline(g, neckY + d * 0.002f, dip, hw)
    clipPath(body) { drawPath(w.c, shade(c, 0.55f), style = Stroke(width = d * 0.008f, cap = StrokeCap.Round)) }
    // Drawstrings with metal aglets
    for (s in intArrayOf(-1, 1)) {
        val sx = g.cx + s * d * 0.04f
        val sy = neckY + dip + d * 0.016f
        val ex = sx + s * d * 0.006f
        val ey = sy + d * 0.07f
        w.c.reset(); w.c.moveTo(sx, sy); w.c.quadraticTo(sx - s * d * 0.008f, (sy + ey) / 2f, ex, ey)
        drawPath(w.c, Color.Black.copy(alpha = 0.18f), style = Stroke(width = d * 0.011f, cap = StrokeCap.Round))
        drawPath(w.c, Color.White.copy(alpha = 0.95f), style = Stroke(width = d * 0.0075f, cap = StrokeCap.Round))
        drawRoundRect(
            Brush.horizontalGradient(listOf(Color(0xFF94A3B8), Color(0xFFF1F5F9), Color(0xFF64748B)), ex - d * 0.006f, ex + d * 0.006f),
            Offset(ex - d * 0.006f, ey - d * 0.004f), Size(d * 0.012f, d * 0.022f), CornerRadius(d * 0.005f)
        )
        drawCircle(Color(0xFFCBD5E1), d * 0.006f, Offset(sx, sy))
    }
    // Kangaroo pocket
    val pTop = hemY - d * 0.088f; val pBot = hemY - d * 0.018f
    val topHalf = d * 0.095f; val botHalf = d * 0.15f
    p.reset()
    p.moveTo(g.cx - topHalf, pTop)
    p.lineTo(g.cx + topHalf, pTop)
    p.quadraticTo(g.cx + botHalf * 0.85f, pTop + d * 0.01f, g.cx + botHalf, pBot)
    p.quadraticTo(g.cx, pBot + d * 0.014f, g.cx - botHalf, pBot)
    p.quadraticTo(g.cx - botHalf * 0.85f, pTop + d * 0.01f, g.cx - topHalf, pTop)
    p.close()
    clipPath(body) {
        drawPath(p, Brush.verticalGradient(listOf(tint(c, 0.08f), shade(c, 0.22f)), pTop, pBot))
        stitch(p, tint(c, 0.4f).copy(alpha = 0.6f), d)
        for (s in intArrayOf(-1, 1)) {
            w.c.reset()
            w.c.moveTo(g.cx + s * topHalf, pTop)
            w.c.quadraticTo(g.cx + s * botHalf * 0.85f, pTop + d * 0.01f, g.cx + s * botHalf, pBot)
            drawPath(w.c, shade(c, 0.6f).copy(alpha = 0.7f), style = Stroke(width = d * 0.007f, cap = StrokeCap.Round))
        }
    }
    w.c.hemline(g, hemY - d * 0.016f, lift)
    ribBand(body, w.c, g, shade(c, 0.15f), d * 0.03f, hemY - d * 0.05f, hemY + lift)
}

private fun DrawScope.drawCapeFront(w: WardrobePaths, body: Path, g: WardrobeGeo, c: Color) {
    val d = g.d
    val neckY = g.cy + d * 0.02f
    val wide = d * 0.5f
    for (s in intArrayOf(-1, 1)) {
        val p = w.a
        p.reset()
        p.moveTo(g.cx + s * wide, g.cy - d * 0.01f)
        p.lineTo(g.cx + s * d * 0.05f, neckY)
        p.quadraticTo(g.cx + s * d * 0.045f, g.cy + d * 0.08f, g.cx + s * d * 0.085f, g.cy + d * 0.135f)
        p.quadraticTo(g.cx + s * d * 0.13f, g.cy + d * 0.15f, g.cx + s * d * 0.17f, g.cy + d * 0.135f)
        p.quadraticTo(g.cx + s * d * 0.21f, g.cy + d * 0.17f, g.cx + s * d * 0.26f, g.cy + d * 0.16f)
        p.lineTo(g.cx + s * wide, g.cy + d * 0.16f)
        p.close()
        fillFitted(body, p, Brush.verticalGradient(listOf(tint(c, 0.10f), c, shade(c, 0.30f)), neckY, g.cy + d * 0.17f), shade(c, 0.4f), d)
        roundShading(body, p, g, neckY - d * 0.02f, g.cy + d * 0.17f)
        clipPath(body) {
            clipPath(p) {
                for (k in 0..1) {
                    w.b.reset()
                    val fx = g.cx + s * d * (0.11f + k * 0.065f)
                    w.b.moveTo(fx - s * d * 0.02f, neckY + d * 0.01f)
                    w.b.quadraticTo(fx + s * d * 0.01f, g.cy + d * 0.08f, fx, g.cy + d * 0.15f)
                    drawPath(w.b, Color.Black.copy(alpha = 0.18f), style = Stroke(width = d * 0.007f, cap = StrokeCap.Round))
                }
            }
        }
        // Gold-edged inner border
        w.b.reset()
        w.b.moveTo(g.cx + s * d * 0.05f, neckY)
        w.b.quadraticTo(g.cx + s * d * 0.045f, g.cy + d * 0.08f, g.cx + s * d * 0.085f, g.cy + d * 0.135f)
        drawPath(w.b, Gold, style = Stroke(width = d * 0.007f, cap = StrokeCap.Round))
    }
    // Fibula: two golden discs linked by a chain
    val fy = neckY + d * 0.015f
    w.b.reset()
    w.b.moveTo(g.cx - d * 0.055f, fy)
    w.b.quadraticTo(g.cx, fy + d * 0.03f, g.cx + d * 0.055f, fy)
    drawPath(w.b, GoldDeep, style = Stroke(width = d * 0.006f, cap = StrokeCap.Round,
        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(d * 0.008f, d * 0.004f))))
    for (s in intArrayOf(-1, 1)) {
        val o = Offset(g.cx + s * d * 0.055f, fy)
        drawCircle(Color.Black.copy(alpha = 0.25f), d * 0.026f, o + Offset(0f, d * 0.004f))
        drawCircle(Brush.radialGradient(listOf(GoldLight, Gold, GoldDeep), o - Offset(d * 0.006f, d * 0.006f), d * 0.028f), d * 0.024f, o)
        drawCircle(GoldShadow, d * 0.016f, o, style = Stroke(width = d * 0.003f))
        w.c.star5(o.x, o.y, d * 0.01f)
        drawPath(w.c, GoldShadow.copy(alpha = 0.8f))
    }
}

private fun DrawScope.drawKimono(w: WardrobePaths, body: Path, g: WardrobeGeo, c: Color) {
    val d = g.d
    val topY = g.cy + d * 0.022f
    val hemY = g.cy + d * 0.245f
    val obiTop = g.cy + d * 0.118f; val obiBot = g.cy + d * 0.170f
    val vTop = d * 0.062f
    val juban = Color(0xFFFFF7ED)
    // Robe with V opening (the glowing heart peeks through)
    val p = w.a
    p.reset()
    p.moveTo(g.cx - d * 0.5f, topY)
    p.lineTo(g.cx - vTop, topY)
    p.lineTo(g.cx, obiTop)
    p.lineTo(g.cx + vTop, topY)
    p.lineTo(g.cx + d * 0.5f, topY)
    p.lineTo(g.cx + d * 0.5f, hemY)
    p.lineTo(g.cx - d * 0.5f, hemY)
    p.close()
    fillFitted(body, p, fabricBrush(c, topY, hemY), shade(c, 0.35f), d)
    roundShading(body, p, g, topY, hemY)
    // Sakura blossoms pattern
    clipPath(body) {
        clipPath(p) {
            val petal = if (c.luma() > 0.6f) shade(c, 0.35f) else tint(c, 0.6f)
            val spots = floatArrayOf(-0.17f, 0.05f, 0.15f, 0.04f, -0.12f, 0.20f, 0.10f, 0.215f, 0.19f, 0.19f, -0.20f, 0.12f)
            var i = 0
            while (i < spots.size) {
                val bx = g.cx + spots[i] * d; val by = g.cy + spots[i + 1] * d
                for (k in 0 until 5) {
                    val a = Math.toRadians(k * 72.0 - 90.0)
                    drawCircle(petal.copy(alpha = 0.55f), d * 0.008f, Offset(bx + (d * 0.009f * cos(a)).toFloat(), by + (d * 0.009f * sin(a)).toFloat()))
                }
                drawCircle(Gold.copy(alpha = 0.8f), d * 0.004f, Offset(bx, by))
                i += 2
            }
        }
    }
    // Crossed collars (eri): right under, left over
    for (s in intArrayOf(1, -1)) {
        w.b.reset()
        w.b.moveTo(g.cx + s * (vTop + d * 0.012f), topY - d * 0.004f)
        w.b.lineTo(g.cx - s * d * 0.01f, obiTop + d * 0.004f)
        clipPath(body) {
            drawPath(w.b, shade(c, 0.4f), style = Stroke(width = d * 0.03f, cap = StrokeCap.Butt))
            drawPath(w.b, juban, style = Stroke(width = d * 0.020f, cap = StrokeCap.Butt))
        }
    }
    // Obi sash with obijime cord and knot
    w.b.reset()
    w.b.moveTo(g.cx - d * 0.5f, obiTop); w.b.quadraticTo(g.cx, obiTop + d * 0.012f, g.cx + d * 0.5f, obiTop)
    w.b.lineTo(g.cx + d * 0.5f, obiBot); w.b.quadraticTo(g.cx, obiBot + d * 0.012f, g.cx - d * 0.5f, obiBot)
    w.b.close()
    val obi = if (c.luma() < 0.25f) Color(0xFFB45309) else shade(c, 0.62f)
    fillFitted(body, w.b, Brush.verticalGradient(listOf(tint(obi, 0.15f), obi, shade(obi, 0.3f)), obiTop, obiBot), shade(obi, 0.4f), d)
    val cordY = (obiTop + obiBot) / 2f + d * 0.006f
    w.c.reset(); w.c.moveTo(g.cx - d * 0.3f, cordY - d * 0.004f); w.c.quadraticTo(g.cx, cordY + d * 0.008f, g.cx + d * 0.3f, cordY - d * 0.004f)
    clipPath(body) { drawPath(w.c, Gold, style = Stroke(width = d * 0.007f)) }
    drawOval(Gold, Offset(g.cx - d * 0.022f, cordY - d * 0.009f), Size(d * 0.044f, d * 0.02f))
    drawOval(GoldShadow, Offset(g.cx - d * 0.022f, cordY - d * 0.009f), Size(d * 0.044f, d * 0.02f), style = Stroke(width = d * 0.003f))
    drawLine(Gold, Offset(g.cx - d * 0.006f, cordY + d * 0.008f), Offset(g.cx - d * 0.014f, cordY + d * 0.04f), d * 0.005f, StrokeCap.Round)
    drawLine(Gold, Offset(g.cx + d * 0.006f, cordY + d * 0.008f), Offset(g.cx + d * 0.014f, cordY + d * 0.04f), d * 0.005f, StrokeCap.Round)
}

private fun DrawScope.drawMageRobe(w: WardrobePaths, body: Path, g: WardrobeGeo, c: Color) {
    val d = g.d
    val topY = g.cy + d * 0.022f
    val hemY = g.cy + d * 0.248f
    val vTop = d * 0.085f; val vBot = g.cy + d * 0.125f
    val p = w.a
    p.reset()
    p.moveTo(g.cx - d * 0.5f, topY)
    p.lineTo(g.cx - vTop, topY)
    p.quadraticTo(g.cx - d * 0.03f, g.cy + d * 0.08f, g.cx, vBot)
    p.quadraticTo(g.cx + d * 0.03f, g.cy + d * 0.08f, g.cx + vTop, topY)
    p.lineTo(g.cx + d * 0.5f, topY)
    p.lineTo(g.cx + d * 0.5f, hemY)
    p.lineTo(g.cx - d * 0.5f, hemY)
    p.close()
    fillFitted(body, p, Brush.verticalGradient(listOf(c, shade(c, 0.45f), shade(c, 0.70f)), topY, hemY), Ink, d)
    roundShading(body, p, g, topY, hemY)
    clipPath(body) {
        clipPath(p) {
            // Celestial runes: stars & crescent moons
            val pts = floatArrayOf(-0.16f, 0.07f, 0.17f, 0.10f, -0.11f, 0.19f, 0.12f, 0.205f, -0.21f, 0.15f, 0.21f, 0.17f)
            var i = 0
            while (i < pts.size) {
                val x = g.cx + pts[i] * d; val y = g.cy + pts[i + 1] * d
                if ((i / 2) % 2 == 0) {
                    w.c.star5(x, y, d * 0.013f); drawPath(w.c, Gold.copy(alpha = 0.85f))
                } else {
                    drawCircle(GoldLight.copy(alpha = 0.85f), d * 0.012f, Offset(x, y))
                    drawCircle(shade(c, 0.5f), d * 0.010f, Offset(x + d * 0.006f, y - d * 0.003f))
                }
                i += 2
            }
            // Front opening trim
            drawLine(Gold, Offset(g.cx, vBot), Offset(g.cx, hemY), d * 0.012f)
            drawLine(GoldShadow, Offset(g.cx, vBot), Offset(g.cx, hemY), d * 0.003f)
            // Hem band with rune dots
            w.b.hemline(g, hemY - d * 0.022f, d * 0.012f)
            drawPath(w.b, Gold, style = Stroke(width = d * 0.016f))
            drawPath(w.b, GoldShadow, style = Stroke(width = d * 0.004f,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(d * 0.004f, d * 0.012f))))
        }
    }
    // Golden V-collar trim
    w.b.reset()
    w.b.moveTo(g.cx - vTop, topY)
    w.b.quadraticTo(g.cx - d * 0.03f, g.cy + d * 0.08f, g.cx, vBot)
    w.b.quadraticTo(g.cx + d * 0.03f, g.cy + d * 0.08f, g.cx + vTop, topY)
    clipPath(body) {
        drawPath(w.b, GoldShadow, style = Stroke(width = d * 0.016f, cap = StrokeCap.Round))
        drawPath(w.b, Gold, style = Stroke(width = d * 0.010f, cap = StrokeCap.Round))
    }
    // Floating crystal amulet in front of the heart
    val ay = g.heartY + d * 0.012f
    drawLine(GoldDeep, Offset(g.cx - d * 0.05f, topY + d * 0.01f), Offset(g.cx, ay - d * 0.03f), d * 0.003f)
    drawLine(GoldDeep, Offset(g.cx + d * 0.05f, topY + d * 0.01f), Offset(g.cx, ay - d * 0.03f), d * 0.003f)
    drawCircle(Brush.radialGradient(listOf(Color(0x9967E8F9), Color.Transparent), Offset(g.cx, ay), d * 0.07f), d * 0.07f, Offset(g.cx, ay))
    w.c.reset()
    w.c.moveTo(g.cx, ay - d * 0.032f)
    w.c.lineTo(g.cx + d * 0.02f, ay - d * 0.004f)
    w.c.lineTo(g.cx, ay + d * 0.034f)
    w.c.lineTo(g.cx - d * 0.02f, ay - d * 0.004f)
    w.c.close()
    drawPath(w.c, Brush.linearGradient(listOf(Color(0xFFE0F2FE), Color(0xFF67E8F9), Color(0xFF0891B2)), Offset(g.cx - d * 0.02f, ay - d * 0.03f), Offset(g.cx + d * 0.02f, ay + d * 0.03f)))
    drawLine(Color.White.copy(alpha = 0.8f), Offset(g.cx - d * 0.02f, ay - d * 0.004f), Offset(g.cx + d * 0.02f, ay - d * 0.004f), d * 0.002f)
    drawPath(w.c, Color.White.copy(alpha = 0.7f), style = Stroke(width = d * 0.003f))
    drawCircle(Gold, d * 0.006f, Offset(g.cx, ay - d * 0.034f))
}

private fun DrawScope.drawArmor(w: WardrobePaths, body: Path, g: WardrobeGeo, c: Color) {
    val d = g.d
    val neckY = g.cy + d * 0.022f; val dip = d * 0.022f; val hw = g.headRX * 0.58f
    val plateHem = g.cy + d * 0.19f
    // Colored tabard skirt under the plate
    w.a.garmentRegion(g, plateHem - d * 0.03f, 0f, 0f, g.cy + d * 0.248f, d * 0.014f)
    fillFitted(body, w.a, fabricBrush(c, plateHem, g.cy + d * 0.25f), shade(c, 0.4f), d)
    clipPath(body) { clipPath(w.a) {
        var x = g.cx - d * 0.24f
        while (x < g.cx + d * 0.25f) { drawLine(shade(c, 0.35f).copy(alpha = 0.6f), Offset(x, plateHem), Offset(x, g.cy + d * 0.26f), d * 0.004f); x += d * 0.05f }
    } }
    // Breastplate
    w.a.garmentRegion(g, neckY, dip, hw, plateHem, -d * 0.02f)
    val metal = Brush.linearGradient(
        listOf(GoldLight, Gold, GoldDeep, Gold, GoldShadow),
        Offset(g.cx - d * 0.25f, neckY), Offset(g.cx + d * 0.25f, plateHem)
    )
    fillFitted(body, w.a, metal, GoldShadow, d)
    roundShading(body, w.a, g, neckY, plateHem + d * 0.03f)
    clipPath(body) { clipPath(w.a) {
        // Pectoral sculpting & center ridge
        for (s in intArrayOf(-1, 1)) {
            w.b.reset()
            w.b.moveTo(g.cx, g.cy + d * 0.115f)
            w.b.quadraticTo(g.cx + s * d * 0.12f, g.cy + d * 0.125f, g.cx + s * d * 0.23f, g.cy + d * 0.07f)
            drawPath(w.b, GoldShadow.copy(alpha = 0.55f), style = Stroke(width = d * 0.006f, cap = StrokeCap.Round))
            drawPath(w.b, GoldLight.copy(alpha = 0.5f), style = Stroke(width = d * 0.002f))
        }
        drawLine(GoldLight.copy(alpha = 0.85f), Offset(g.cx - d * 0.003f, neckY + dip), Offset(g.cx - d * 0.003f, plateHem + d * 0.02f), d * 0.004f)
        // Faulds (lower lames) with rivets
        for (k in 0..1) {
            val ly = g.cy + d * (0.145f + k * 0.024f)
            w.b.reset(); w.b.moveTo(g.cx - d * 0.3f, ly - d * 0.012f); w.b.quadraticTo(g.cx, ly + d * 0.014f, g.cx + d * 0.3f, ly - d * 0.012f)
            drawPath(w.b, GoldShadow.copy(alpha = 0.75f), style = Stroke(width = d * 0.005f))
            for (s in intArrayOf(-1, 1)) drawCircle(GoldLight, d * 0.0055f, Offset(g.cx + s * d * 0.13f, ly - d * 0.004f))
        }
    } }
    // Gorget collar
    w.b.neckline(g, neckY + d * 0.003f, dip, hw)
    clipPath(body) {
        drawPath(w.b, GoldShadow, style = Stroke(width = d * 0.02f, cap = StrokeCap.Round))
        drawPath(w.b, Gold, style = Stroke(width = d * 0.012f, cap = StrokeCap.Round))
        drawPath(w.b, GoldLight, style = Stroke(width = d * 0.003f, cap = StrokeCap.Round))
    }
    // Pauldrons (layered shoulder plates)
    for (s in intArrayOf(-1, 1)) {
        val py = g.cy + d * 0.035f
        val px = g.cx + s * (g.halfWidthAt(py) - d * 0.03f)
        for (k in 1 downTo 0) {
            val oy = py + k * d * 0.022f
            val rx = d * (0.058f - k * 0.004f); val ry = d * 0.036f
            drawOval(Color.Black.copy(alpha = 0.22f), Offset(px - rx, oy - ry + d * 0.006f), Size(rx * 2, ry * 2))
            drawOval(Brush.radialGradient(listOf(GoldLight, Gold, GoldDeep), Offset(px - s * rx * 0.3f, oy - ry * 0.4f), rx * 1.4f), Offset(px - rx, oy - ry), Size(rx * 2, ry * 2))
            drawOval(GoldShadow, Offset(px - rx, oy - ry), Size(rx * 2, ry * 2), style = Stroke(width = d * 0.004f))
        }
        drawCircle(GoldLight, d * 0.007f, Offset(px - s * d * 0.025f, py))
        drawCircle(GoldLight, d * 0.007f, Offset(px + s * d * 0.022f, py - d * 0.004f))
    }
    // Radiant star of the unbreakable will (the heart of light shines from it)
    val sy = g.heartY - d * 0.005f
    drawCircle(Brush.radialGradient(listOf(Color(0xAAFFFFFF), Color(0x6667E8F9), Color.Transparent), Offset(g.cx, sy), d * 0.08f), d * 0.08f, Offset(g.cx, sy))
    for (k in 0 until 8) {
        rotate(k * 45f + 22.5f, Offset(g.cx, sy)) {
            drawLine(Color.White.copy(alpha = 0.55f), Offset(g.cx, sy - d * 0.035f), Offset(g.cx, sy - d * 0.06f), d * 0.004f, StrokeCap.Round)
        }
    }
    w.c.star5(g.cx, sy, d * 0.036f, 0.48f)
    drawPath(w.c, Brush.radialGradient(listOf(Color.White, GoldLight, Gold), Offset(g.cx, sy), d * 0.04f))
    drawPath(w.c, GoldShadow, style = Stroke(width = d * 0.004f))
    drawCircle(Color(0xFF67E8F9), d * 0.009f, Offset(g.cx, sy))
    drawCircle(Color.White, d * 0.004f, Offset(g.cx - d * 0.003f, sy - d * 0.003f))
}

// ===========================================================================
// ACCESSORIES LAYER (drawn AFTER the face)
// ===========================================================================
internal fun DrawScope.drawWardrobeAccessories(
    w: WardrobePaths,
    body: Path,
    g: WardrobeGeo,
    set: Set<LlumieCosmetic>,
    sproutSway: Float,
    flicker: Float,
    orbitDeg: Float,
    sleeping: Boolean
) {
    if (!sleeping) {
        if (LlumieCosmetic.LEATHER_BELT in set) drawBelt(w, body, g)
        if (LlumieCosmetic.TRAVEL_BACKPACK in set) drawBackpackStraps(w, body, g)
        if (LlumieCosmetic.COZY_SCARF in set) drawScarf(w, g)
        if (LlumieCosmetic.BELL_COLLAR in set) { drawBellCollar(w, body, g); drawBell(w, g, flicker) }
        if (LlumieCosmetic.BOW_TIE in set) drawBowTie(w, g)
        if (LlumieCosmetic.REBIRTH_MEDALLION in set) drawMedallion(w, g, flicker)
        if (LlumieCosmetic.GOLDEN_FLAME_RELIC in set) drawFlameRelic(w, g, flicker)
    }
    if (LlumieCosmetic.CHEEK_STICKERS in set) drawCheekStickers(w, g)
    if (!sleeping) {
        if (LlumieCosmetic.COOL_SHADES in set) drawShades(w, g)
        if (LlumieCosmetic.BERET_HAT in set) drawBeret(w, g)
        if (LlumieCosmetic.BEANIE_HAT in set) drawBeanie(w, body, g)
        if (LlumieCosmetic.BASEBALL_CAP in set) drawCap(w, g)
        if (LlumieCosmetic.OLIVE_WREATH in set) drawWreath(w, g)
        if (LlumieCosmetic.ROYAL_CROWN in set) drawCrown(w, g, flicker)
        if (LlumieCosmetic.WIZARD_HAT in set) drawWizardHat(w, g, sproutSway)
        if (LlumieCosmetic.SPROUT_LEAF in set) drawSprout(w, g, sproutSway)
    }
    if (LlumieCosmetic.AURA_STARS in set) drawAuraStarsV2(w, g, orbitDeg, back = false)
}

private fun DrawScope.drawBelt(w: WardrobePaths, body: Path, g: WardrobeGeo) {
    val d = g.d
    val y = g.cy + d * 0.175f; val h = d * 0.038f; val sag = d * 0.016f
    val p = w.a
    p.reset()
    p.moveTo(g.cx - d * 0.5f, y - sag); p.quadraticTo(g.cx, y + sag, g.cx + d * 0.5f, y - sag)
    p.lineTo(g.cx + d * 0.5f, y - sag + h); p.quadraticTo(g.cx, y + sag + h, g.cx - d * 0.5f, y - sag + h)
    p.close()
    fillFitted(body, p, Brush.verticalGradient(listOf(LeatherLight, Leather, LeatherDark), y - sag, y + sag + h), LeatherDark, d)
    w.b.reset(); w.b.moveTo(g.cx - d * 0.5f, y - sag + h * 0.22f); w.b.quadraticTo(g.cx, y + sag + h * 0.22f, g.cx + d * 0.5f, y - sag + h * 0.22f)
    clipPath(body) { stitch(w.b, Color(0xFFFDE68A).copy(alpha = 0.7f), d) }
    w.b.reset(); w.b.moveTo(g.cx - d * 0.5f, y - sag + h * 0.80f); w.b.quadraticTo(g.cx, y + sag + h * 0.80f, g.cx + d * 0.5f, y - sag + h * 0.80f)
    clipPath(body) { stitch(w.b, Color(0xFFFDE68A).copy(alpha = 0.7f), d) }
    // Brass buckle with prong
    val by = y + sag * 0.5f + h * 0.5f
    val bw = d * 0.06f; val bh = d * 0.052f
    drawRoundRect(Color.Black.copy(alpha = 0.25f), Offset(g.cx - bw / 2f, by - bh / 2f + d * 0.005f), Size(bw, bh), CornerRadius(d * 0.01f))
    drawRoundRect(Brush.linearGradient(listOf(GoldLight, Gold, GoldDeep), Offset(g.cx - bw / 2f, by - bh / 2f), Offset(g.cx + bw / 2f, by + bh / 2f)),
        Offset(g.cx - bw / 2f, by - bh / 2f), Size(bw, bh), CornerRadius(d * 0.01f), style = Stroke(width = d * 0.011f))
    drawLine(GoldDeep, Offset(g.cx - bw * 0.15f, by), Offset(g.cx + bw * 0.35f, by), d * 0.007f, StrokeCap.Round)
    // Belt loop
    drawRoundRect(LeatherDark, Offset(g.cx + d * 0.10f, by - h * 0.65f), Size(d * 0.016f, h * 1.3f), CornerRadius(d * 0.004f))
}

private fun DrawScope.drawBackpackStraps(w: WardrobePaths, body: Path, g: WardrobeGeo) {
    val d = g.d
    clipPath(body) {
        for (s in intArrayOf(-1, 1)) {
            w.a.reset()
            w.a.moveTo(g.cx + s * d * 0.205f, g.cy - d * 0.07f)
            w.a.quadraticTo(g.cx + s * d * 0.135f, g.cy + d * 0.05f, g.cx + s * d * 0.165f, g.cy + d * 0.23f)
            drawPath(w.a, LeatherDark, style = Stroke(width = d * 0.042f, cap = StrokeCap.Butt))
            drawPath(w.a, Leather, style = Stroke(width = d * 0.032f, cap = StrokeCap.Butt))
            stitch(w.a, Color(0xFFFDE68A).copy(alpha = 0.6f), d)
            // Buckle
            val bx = g.cx + s * d * 0.148f; val by = g.cy + d * 0.105f
            drawRoundRect(Gold, Offset(bx - d * 0.022f, by - d * 0.013f), Size(d * 0.044f, d * 0.026f), CornerRadius(d * 0.006f), style = Stroke(width = d * 0.006f))
        }
    }
    // Chest strap with clip (below the heart)
    val cy2 = g.cy + d * 0.13f
    drawLine(LeatherDark, Offset(g.cx - d * 0.145f, cy2), Offset(g.cx + d * 0.145f, cy2), d * 0.016f)
    drawRoundRect(Color(0xFF334155), Offset(g.cx - d * 0.022f, cy2 - d * 0.014f), Size(d * 0.044f, d * 0.028f), CornerRadius(d * 0.008f))
    drawLine(Color(0xFF94A3B8), Offset(g.cx, cy2 - d * 0.01f), Offset(g.cx, cy2 + d * 0.01f), d * 0.003f)
}

private val ScarfRed = Color(0xFFE11D48)
private val ScarfRedDark = Color(0xFF9F1239)
private val ScarfCream = Color(0xFFFFF1F2)

private fun DrawScope.drawScarf(w: WardrobePaths, g: WardrobeGeo) {
    val d = g.d
    val top = g.cy + d * 0.010f; val bot = g.cy + d * 0.064f
    val W = g.halfWidthAt(g.cy + d * 0.03f) + d * 0.012f
    // Hanging tail with stripes & fringe (behind the wrap)
    val tx = g.cx + d * 0.075f; val tTop = bot - d * 0.02f; val tBot = g.cy + d * 0.205f; val tw = d * 0.072f
    rotate(-6f, Offset(tx + tw / 2f, tTop)) {
        drawRoundRect(Brush.horizontalGradient(listOf(ScarfRed, ScarfRedDark), tx, tx + tw), Offset(tx, tTop), Size(tw, tBot - tTop), CornerRadius(d * 0.014f))
        for (k in 1..2) {
            val sy = tTop + (tBot - tTop) * (0.35f + k * 0.2f)
            drawRect(ScarfCream, Offset(tx, sy), Size(tw, d * 0.014f))
        }
        var fx = tx + d * 0.008f
        while (fx < tx + tw - d * 0.004f) {
            drawLine(ScarfCream, Offset(fx, tBot - d * 0.004f), Offset(fx, tBot + d * 0.028f), d * 0.006f, StrokeCap.Round)
            fx += d * 0.013f
        }
    }
    // Soft puffy wrap around the neck
    val p = w.a
    p.reset()
    p.moveTo(g.cx - W, top + d * 0.004f)
    p.quadraticTo(g.cx, top + d * 0.032f, g.cx + W, top + d * 0.004f)
    p.quadraticTo(g.cx + W + d * 0.024f, (top + bot) / 2f + d * 0.01f, g.cx + W - d * 0.006f, bot)
    p.quadraticTo(g.cx, bot + d * 0.034f, g.cx - W + d * 0.006f, bot)
    p.quadraticTo(g.cx - W - d * 0.024f, (top + bot) / 2f + d * 0.01f, g.cx - W, top + d * 0.004f)
    p.close()
    drawPath(p, Color.Black.copy(alpha = 0.20f), style = Stroke(width = d * 0.012f))
    drawPath(p, Brush.verticalGradient(listOf(Color(0xFFFB7185), ScarfRed, ScarfRedDark), top, bot + d * 0.02f))
    clipPath(p) {
        for (k in 0..1) {
            val sy = top + (bot - top) * (0.30f + k * 0.38f)
            w.b.reset(); w.b.moveTo(g.cx - W - d * 0.03f, sy); w.b.quadraticTo(g.cx, sy + d * 0.03f, g.cx + W + d * 0.03f, sy)
            drawPath(w.b, ScarfCream, style = Stroke(width = d * 0.010f))
        }
        // Knit texture
        var x = g.cx - W
        while (x < g.cx + W) {
            drawLine(Color.Black.copy(alpha = 0.10f), Offset(x, top), Offset(x + d * 0.01f, bot + d * 0.03f), d * 0.004f)
            x += d * 0.02f
        }
    }
    drawPath(p, Color.White.copy(alpha = 0.18f), style = Stroke(width = d * 0.004f))
    // Knot where the tail leaves the wrap
    drawOval(Brush.radialGradient(listOf(Color(0xFFFB7185), ScarfRedDark), Offset(tx + tw * 0.4f, bot), d * 0.04f), Offset(tx - d * 0.004f, bot - d * 0.024f), Size(tw + d * 0.008f, d * 0.04f))
}

private fun DrawScope.drawBellCollar(w: WardrobePaths, body: Path, g: WardrobeGeo) {
    val d = g.d
    val y = g.cy + d * 0.016f; val h = d * 0.026f; val sag = d * 0.016f
    val p = w.a
    p.reset()
    p.moveTo(g.cx - d * 0.5f, y - sag); p.quadraticTo(g.cx, y + sag, g.cx + d * 0.5f, y - sag)
    p.lineTo(g.cx + d * 0.5f, y - sag + h); p.quadraticTo(g.cx, y + sag + h, g.cx - d * 0.5f, y - sag + h)
    p.close()
    fillFitted(body, p, Brush.verticalGradient(listOf(Color(0xFFF87171), Color(0xFFDC2626), Color(0xFF991B1B)), y - sag, y + sag + h), Color(0xFF7F1D1D), d)
    for (s in intArrayOf(-2, -1, 1, 2)) {
        val sx = g.cx + s * d * 0.07f
        val t = (sx - g.cx) / (d * 0.5f)
        val sy = y - sag * t * t + h * 0.5f
        drawCircle(Brush.radialGradient(listOf(GoldLight, Gold, GoldDeep), Offset(sx - d * 0.002f, sy - d * 0.002f), d * 0.008f), d * 0.0065f, Offset(sx, sy))
    }
}

private fun DrawScope.drawBell(w: WardrobePaths, g: WardrobeGeo, flicker: Float) {
    val d = g.d
    val ring = Offset(g.cx, g.cy + d * 0.040f)
    rotate((flicker - 1f) * 60f, ring) {
        val c = Offset(g.cx, ring.y + d * 0.03f)
        val r = d * 0.03f
        drawCircle(Gold, d * 0.008f, ring, style = Stroke(width = d * 0.004f))
        drawCircle(Color.Black.copy(alpha = 0.25f), r, c + Offset(d * 0.003f, d * 0.006f))
        drawCircle(Brush.radialGradient(listOf(Color(0xFFFFFBEB), GoldLight, Gold, GoldDeep, GoldShadow), c - Offset(r * 0.35f, r * 0.4f), r * 1.5f), r, c)
        drawLine(GoldShadow, Offset(c.x - r * 0.75f, c.y - r * 0.05f), Offset(c.x + r * 0.75f, c.y - r * 0.05f), d * 0.004f, StrokeCap.Round)
        drawCircle(GoldShadow, r * 0.22f, Offset(c.x, c.y + r * 0.42f))
        drawLine(GoldShadow, Offset(c.x, c.y + r * 0.42f), Offset(c.x, c.y + r * 0.92f), d * 0.004f, StrokeCap.Round)
        drawCircle(Color.White.copy(alpha = 0.85f), r * 0.18f, Offset(c.x - r * 0.38f, c.y - r * 0.45f))
    }
}

private fun DrawScope.drawBowTie(w: WardrobePaths, g: WardrobeGeo) {
    val d = g.d
    val y = g.cy + d * 0.030f
    val red = Color(0xFFE11D48); val deep = Color(0xFF881337)
    for (s in intArrayOf(-1, 1)) {
        val p = w.a
        p.reset()
        p.moveTo(g.cx + s * d * 0.012f, y - d * 0.010f)
        p.cubicTo(g.cx + s * d * 0.035f, y - d * 0.045f, g.cx + s * d * 0.085f, y - d * 0.042f, g.cx + s * d * 0.088f, y - d * 0.004f)
        p.cubicTo(g.cx + s * d * 0.090f, y + d * 0.038f, g.cx + s * d * 0.040f, y + d * 0.045f, g.cx + s * d * 0.012f, y + d * 0.010f)
        p.close()
        drawPath(p, Color.Black.copy(alpha = 0.25f), style = Stroke(width = d * 0.008f))
        drawPath(p, Brush.radialGradient(listOf(Color(0xFFFB7185), red, deep), Offset(g.cx + s * d * 0.05f, y - d * 0.012f), d * 0.07f))
        // Fabric creases
        for (k in -1..1) {
            w.b.reset()
            w.b.moveTo(g.cx + s * d * 0.018f, y + k * d * 0.004f)
            w.b.quadraticTo(g.cx + s * d * 0.045f, y + k * d * 0.02f, g.cx + s * d * 0.07f, y + k * d * 0.026f)
            drawPath(w.b, deep.copy(alpha = 0.55f), style = Stroke(width = d * 0.003f, cap = StrokeCap.Round))
        }
        // Polka dots
        drawCircle(Color.White.copy(alpha = 0.85f), d * 0.005f, Offset(g.cx + s * d * 0.06f, y - d * 0.018f))
        drawCircle(Color.White.copy(alpha = 0.85f), d * 0.005f, Offset(g.cx + s * d * 0.07f, y + d * 0.016f))
        drawCircle(Color.White.copy(alpha = 0.85f), d * 0.005f, Offset(g.cx + s * d * 0.038f, y))
    }
    drawRoundRect(Brush.verticalGradient(listOf(red, deep), y - d * 0.017f, y + d * 0.017f), Offset(g.cx - d * 0.016f, y - d * 0.017f), Size(d * 0.032f, d * 0.034f), CornerRadius(d * 0.009f))
    drawLine(Color.White.copy(alpha = 0.35f), Offset(g.cx - d * 0.008f, y - d * 0.01f), Offset(g.cx - d * 0.008f, y + d * 0.008f), d * 0.003f, StrokeCap.Round)
}

private fun DrawScope.drawMedallion(w: WardrobePaths, g: WardrobeGeo, flicker: Float) {
    val d = g.d
    val m = Offset(g.cx, g.cy + d * 0.138f)
    val violet = Color(0xFF7C3AED)
    for (s in intArrayOf(-1, 1)) {
        w.a.reset()
        w.a.moveTo(g.cx + s * d * 0.115f, g.cy + d * 0.012f)
        w.a.quadraticTo(g.cx + s * d * 0.085f, g.cy + d * 0.09f, m.x + s * d * 0.012f, m.y - d * 0.03f)
        drawPath(w.a, Color(0xFF4C1D95), style = Stroke(width = d * 0.026f, cap = StrokeCap.Butt))
        drawPath(w.a, violet, style = Stroke(width = d * 0.020f, cap = StrokeCap.Butt))
        drawPath(w.a, Gold.copy(alpha = 0.8f), style = Stroke(width = d * 0.003f))
    }
    val r = d * 0.042f
    drawCircle(Brush.radialGradient(listOf(Color(0x66FDE047), Color.Transparent), m, r * 2.2f), r * 2.2f, m)
    drawCircle(Color.Black.copy(alpha = 0.25f), r, m + Offset(0f, d * 0.006f))
    drawCircle(Brush.linearGradient(listOf(GoldLight, Gold, GoldDeep, GoldShadow), m - Offset(r, r), m + Offset(r, r)), r, m)
    drawCircle(GoldShadow, r * 0.78f, m, style = Stroke(width = d * 0.004f))
    drawCircle(Brush.radialGradient(listOf(GoldLight, Gold), m - Offset(r * 0.2f, r * 0.2f), r), r * 0.70f, m)
    // Engraved rebirth flame
    w.b.reset()
    w.b.moveTo(m.x, m.y - r * 0.5f)
    w.b.cubicTo(m.x + r * 0.42f, m.y - r * 0.1f, m.x + r * 0.35f, m.y + r * 0.45f, m.x, m.y + r * 0.45f)
    w.b.cubicTo(m.x - r * 0.35f, m.y + r * 0.45f, m.x - r * 0.42f, m.y - r * 0.1f, m.x, m.y - r * 0.5f)
    w.b.close()
    drawPath(w.b, GoldShadow.copy(alpha = 0.8f))
    drawCircle(Color(0xFF38BDF8), r * 0.18f, Offset(m.x, m.y + r * 0.12f))
    drawCircle(Color.White, r * 0.07f, Offset(m.x - r * 0.05f, m.y + r * 0.07f))
    // Rotating glint
    drawSparkle(w.c, m.x + r * 0.55f, m.y - r * 0.55f, d * 0.014f * flicker, Color.White, (flicker - 0.9f) * 5f)
}

private fun Path.flame(x: Float, baseY: Float, w: Float, h: Float, lean: Float) {
    reset()
    moveTo(x + lean, baseY - h)
    cubicTo(x + w * 0.35f + lean * 0.5f, baseY - h * 0.6f, x + w * 0.62f, baseY - h * 0.35f, x + w * 0.5f, baseY - h * 0.12f)
    cubicTo(x + w * 0.42f, baseY + h * 0.06f, x - w * 0.42f, baseY + h * 0.06f, x - w * 0.5f, baseY - h * 0.12f)
    cubicTo(x - w * 0.62f, baseY - h * 0.35f, x - w * 0.2f + lean * 0.5f, baseY - h * 0.55f, x + lean, baseY - h)
    close()
}

private fun DrawScope.drawFlameRelic(w: WardrobePaths, g: WardrobeGeo, flicker: Float) {
    val d = g.d
    val baseY = g.cy + d * 0.185f
    val lean = (flicker - 1f) * d * 0.15f
    val glowC = Offset(g.cx, baseY - d * 0.045f)
    drawCircle(Brush.radialGradient(listOf(Color(0x99FDE047), Color(0x44F59E0B), Color.Transparent), glowC, d * 0.13f * flicker), d * 0.13f * flicker, glowC)
    w.a.flame(g.cx, baseY, d * 0.075f, d * 0.11f * flicker, lean)
    drawPath(w.a, Brush.verticalGradient(listOf(Color(0xFFFDE047), Color(0xFFF59E0B), Color(0xFFEA580C)), baseY - d * 0.11f, baseY))
    w.a.flame(g.cx, baseY - d * 0.004f, d * 0.048f, d * 0.075f * flicker, lean * 0.7f)
    drawPath(w.a, Brush.verticalGradient(listOf(Color(0xFFFFFBEB), Color(0xFFFDE047)), baseY - d * 0.08f, baseY))
    w.a.flame(g.cx, baseY - d * 0.006f, d * 0.022f, d * 0.038f, lean * 0.4f)
    drawPath(w.a, Color.White)
    // Golden chalice
    val bowlTop = baseY - d * 0.006f
    w.b.reset()
    w.b.moveTo(g.cx - d * 0.05f, bowlTop)
    w.b.quadraticTo(g.cx, bowlTop + d * 0.06f, g.cx + d * 0.05f, bowlTop)
    w.b.close()
    drawPath(w.b, Brush.horizontalGradient(listOf(GoldDeep, GoldLight, Gold, GoldShadow), g.cx - d * 0.05f, g.cx + d * 0.05f))
    drawLine(GoldLight, Offset(g.cx - d * 0.05f, bowlTop), Offset(g.cx + d * 0.05f, bowlTop), d * 0.006f, StrokeCap.Round)
    // Little paws holding the relic (only appear with it)
    for (s in intArrayOf(-1, 1)) {
        val pc = Offset(g.cx + s * d * 0.052f, bowlTop + d * 0.022f)
        drawOval(Color.Black.copy(alpha = 0.18f), Offset(pc.x - d * 0.03f, pc.y - d * 0.018f), Size(d * 0.06f, d * 0.042f))
        drawOval(Brush.radialGradient(listOf(Color(0xFFEDE9FE), Color(0xFFC4B5FD), Color(0xFF8B5CF6)), pc - Offset(0f, d * 0.01f), d * 0.04f),
            Offset(pc.x - d * 0.028f, pc.y - d * 0.02f), Size(d * 0.056f, d * 0.038f))
        for (k in -1..1) drawCircle(Color(0xFFF9A8D4), d * 0.0045f, Offset(pc.x + k * d * 0.012f - s * d * 0.004f, pc.y - d * 0.012f))
    }
    // Rising sparks
    drawSparkle(w.c, g.cx + d * 0.045f, baseY - d * 0.12f - (flicker - 0.92f) * d * 0.3f, d * 0.01f, Color(0xFFFDE047), 0.8f)
    drawSparkle(w.c, g.cx - d * 0.05f, baseY - d * 0.09f - (1.08f - flicker) * d * 0.3f, d * 0.008f, Color(0xFFFFFBEB), 0.7f)
}

private fun DrawScope.drawCheekStickers(w: WardrobePaths, g: WardrobeGeo) {
    val d = g.d
    val y = g.cy - d * 0.012f
    val lx = g.cx - d * 0.145f; val rx = g.cx + d * 0.145f
    // Gold star sticker with white die-cut border
    w.a.star5(lx, y + d * 0.002f, d * 0.034f, 0.5f, -8f)
    drawPath(w.a, Color.Black.copy(alpha = 0.18f), style = Stroke(width = d * 0.016f, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
    w.a.star5(lx, y, d * 0.034f, 0.5f, -8f)
    drawPath(w.a, Color.White, style = Stroke(width = d * 0.012f, join = androidx.compose.ui.graphics.StrokeJoin.Round))
    drawPath(w.a, Brush.linearGradient(listOf(Color(0xFFFEF08A), Gold, GoldDeep), Offset(lx - d * 0.03f, y - d * 0.03f), Offset(lx + d * 0.03f, y + d * 0.03f)))
    drawCircle(Color.White.copy(alpha = 0.8f), d * 0.005f, Offset(lx - d * 0.008f, y - d * 0.01f))
    w.a.heart(lx - d * 0.03f, y + d * 0.03f, d * 0.011f)
    drawPath(w.a, Color.White, style = Stroke(width = d * 0.006f))
    drawPath(w.a, Color(0xFFFB7185))
    // Pink heart sticker
    w.a.heart(rx, y, d * 0.027f)
    drawPath(w.a, Color.Black.copy(alpha = 0.18f), style = Stroke(width = d * 0.016f))
    drawPath(w.a, Color.White, style = Stroke(width = d * 0.012f))
    drawPath(w.a, Brush.linearGradient(listOf(Color(0xFFFDA4AF), Color(0xFFF43F5E), Color(0xFFBE123C)), Offset(rx - d * 0.03f, y - d * 0.03f), Offset(rx + d * 0.03f, y + d * 0.03f)))
    drawCircle(Color.White.copy(alpha = 0.8f), d * 0.005f, Offset(rx - d * 0.012f, y - d * 0.008f))
    w.a.star5(rx + d * 0.032f, y + d * 0.03f, d * 0.014f)
    drawPath(w.a, Color.White, style = Stroke(width = d * 0.006f))
    drawPath(w.a, Gold)
}

private fun DrawScope.drawShades(w: WardrobePaths, g: WardrobeGeo) {
    val d = g.d
    val y = g.faceY - d * 0.002f
    for (s in intArrayOf(-1, 1)) {
        val x = g.cx + s * g.eyeSpacing
        val p = w.a
        p.reset()
        p.moveTo(x - s * d * 0.048f, y - d * 0.028f)
        p.lineTo(x + s * d * 0.052f, y - d * 0.032f)
        p.quadraticTo(x + s * d * 0.056f, y + d * 0.018f, x + s * d * 0.022f, y + d * 0.030f)
        p.lineTo(x - s * d * 0.020f, y + d * 0.030f)
        p.quadraticTo(x - s * d * 0.052f, y + d * 0.022f, x - s * d * 0.048f, y - d * 0.028f)
        p.close()
        drawPath(p, Color.Black.copy(alpha = 0.25f), style = Stroke(width = d * 0.014f, join = androidx.compose.ui.graphics.StrokeJoin.Round))
        drawPath(p, Brush.verticalGradient(listOf(Color(0xFF0F0A1E), Color(0xFF2E1065), Color(0xFF7C3AED)), y - d * 0.03f, y + d * 0.03f))
        clipPath(p) {
            drawLine(Color.White.copy(alpha = 0.55f), Offset(x - d * 0.03f, y + d * 0.03f), Offset(x + d * 0.01f, y - d * 0.035f), d * 0.012f)
            drawLine(Color.White.copy(alpha = 0.35f), Offset(x - d * 0.006f, y + d * 0.03f), Offset(x + d * 0.028f, y - d * 0.03f), d * 0.005f)
        }
        drawPath(p, Ink, style = Stroke(width = d * 0.009f, join = androidx.compose.ui.graphics.StrokeJoin.Round))
        // Temple arm toward the side of the head
        val outer = x + s * d * 0.052f
        drawLine(Ink, Offset(outer, y - d * 0.028f), Offset(g.cx + s * (g.halfWidthAt(y) - d * 0.004f), y - d * 0.02f), d * 0.008f, StrokeCap.Round)
        drawCircle(Color(0xFFCBD5E1), d * 0.005f, Offset(outer, y - d * 0.026f))
    }
    w.b.reset()
    w.b.moveTo(g.cx - g.eyeSpacing + d * 0.048f, y - d * 0.022f)
    w.b.quadraticTo(g.cx, y - d * 0.04f, g.cx + g.eyeSpacing - d * 0.048f, y - d * 0.022f)
    drawPath(w.b, Ink, style = Stroke(width = d * 0.009f, cap = StrokeCap.Round))
}

// ---------------------------------------------------------------------------
// HEADWEAR – all sized to Llumie's head dome (top ≈ bodyTop, half-width ≈ 0.19d)
// ---------------------------------------------------------------------------

private fun DrawScope.drawBeanie(w: WardrobePaths, body: Path, g: WardrobeGeo) {
    val d = g.d; val t = g.bodyTop
    val main = Color(0xFF0EA5E9); val dark = Color(0xFF075985)
    val p = w.a
    p.reset()
    p.moveTo(g.cx - d * 0.198f, t + d * 0.07f)
    p.cubicTo(g.cx - d * 0.215f, t - d * 0.10f, g.cx + d * 0.215f, t - d * 0.10f, g.cx + d * 0.198f, t + d * 0.07f)
    p.close()
    drawPath(p, Color.Black.copy(alpha = 0.2f), style = Stroke(width = d * 0.012f))
    drawPath(p, Brush.radialGradient(listOf(Color(0xFF7DD3FC), main, dark), Offset(g.cx - d * 0.05f, t - d * 0.04f), d * 0.25f))
    clipPath(p) {
        // Knit ribs
        for (k in -6..6) {
            w.b.reset()
            w.b.moveTo(g.cx + k * d * 0.03f, t + d * 0.08f)
            w.b.quadraticTo(g.cx + k * d * 0.034f, t - d * 0.01f, g.cx + k * d * 0.012f, t - d * 0.07f)
            drawPath(w.b, dark.copy(alpha = 0.35f), style = Stroke(width = d * 0.005f))
        }
        // Nordic stripe with tiny hearts
        drawRect(Color(0xFFF0F9FF), Offset(g.cx - d * 0.3f, t - d * 0.008f), Size(d * 0.6f, d * 0.018f))
        var x = g.cx - d * 0.18f
        while (x <= g.cx + d * 0.18f) {
            w.c.heart(x, t + d * 0.001f, d * 0.006f); drawPath(w.c, Color(0xFFF43F5E)); x += d * 0.045f
        }
    }
    // Folded ribbed cuff
    val cuffTop = t + d * 0.045f; val cuffH = d * 0.055f
    drawRoundRect(Color.Black.copy(alpha = 0.22f), Offset(g.cx - d * 0.214f, cuffTop + d * 0.006f), Size(d * 0.428f, cuffH), CornerRadius(d * 0.027f))
    drawRoundRect(Brush.verticalGradient(listOf(Color(0xFF38BDF8), Color(0xFF0369A1)), cuffTop, cuffTop + cuffH), Offset(g.cx - d * 0.214f, cuffTop), Size(d * 0.428f, cuffH), CornerRadius(d * 0.027f))
    var rx = g.cx - d * 0.195f
    while (rx < g.cx + d * 0.2f) { drawLine(dark.copy(alpha = 0.45f), Offset(rx, cuffTop + d * 0.008f), Offset(rx, cuffTop + cuffH - d * 0.008f), d * 0.006f, StrokeCap.Round); rx += d * 0.018f }
    drawLine(Color.White.copy(alpha = 0.35f), Offset(g.cx - d * 0.19f, cuffTop + d * 0.006f), Offset(g.cx + d * 0.19f, cuffTop + d * 0.006f), d * 0.004f, StrokeCap.Round)
    // Fluffy pompom
    val pc = Offset(g.cx, t - d * 0.068f)
    drawCircle(Color.Black.copy(alpha = 0.18f), d * 0.05f, pc + Offset(0f, d * 0.008f))
    for (k in 0 until 8) {
        val a = Math.toRadians(k * 45.0)
        drawCircle(Color(0xFFE0F2FE), d * 0.022f, Offset(pc.x + (d * 0.03f * cos(a)).toFloat(), pc.y + (d * 0.03f * sin(a)).toFloat()))
    }
    drawCircle(Brush.radialGradient(listOf(Color.White, Color(0xFFE0F2FE), Color(0xFFBAE6FD)), pc - Offset(d * 0.012f, d * 0.014f), d * 0.05f), d * 0.038f, pc)
}

private fun DrawScope.drawCap(w: WardrobePaths, g: WardrobeGeo) {
    val d = g.d; val t = g.bodyTop
    val red = Color(0xFFE11D48); val deep = Color(0xFF881337)
    val p = w.a
    p.reset()
    p.moveTo(g.cx - d * 0.198f, t + d * 0.068f)
    p.cubicTo(g.cx - d * 0.21f, t - d * 0.075f, g.cx + d * 0.21f, t - d * 0.075f, g.cx + d * 0.198f, t + d * 0.068f)
    p.close()
    drawPath(p, Color.Black.copy(alpha = 0.2f), style = Stroke(width = d * 0.012f))
    drawPath(p, Brush.radialGradient(listOf(Color(0xFFFB7185), red, deep), Offset(g.cx - d * 0.05f, t - d * 0.03f), d * 0.24f))
    clipPath(p) {
        for (s in intArrayOf(-1, 1)) {
            w.b.reset(); w.b.moveTo(g.cx, t - d * 0.04f); w.b.quadraticTo(g.cx + s * d * 0.09f, t - d * 0.01f, g.cx + s * d * 0.11f, t + d * 0.07f)
            drawPath(w.b, deep.copy(alpha = 0.6f), style = Stroke(width = d * 0.005f))
            stitch(w.b, Color.White.copy(alpha = 0.3f), d)
        }
        // Front panel badge: little white star in a circle
        drawCircle(Color.White, d * 0.03f, Offset(g.cx, t + d * 0.015f))
        w.c.star5(g.cx, t + d * 0.016f, d * 0.021f); drawPath(w.c, red)
    }
    drawCircle(deep, d * 0.014f, Offset(g.cx, t - d * 0.04f))
    drawCircle(Color(0xFFFB7185), d * 0.007f, Offset(g.cx - d * 0.003f, t - d * 0.044f))
    // Curved visor seen from the front
    val vy = t + d * 0.058f
    w.b.reset()
    w.b.moveTo(g.cx - d * 0.225f, vy)
    w.b.quadraticTo(g.cx, vy + d * 0.105f, g.cx + d * 0.225f, vy)
    w.b.quadraticTo(g.cx, vy + d * 0.03f, g.cx - d * 0.225f, vy)
    w.b.close()
    drawPath(w.b, Color.Black.copy(alpha = 0.22f), style = Stroke(width = d * 0.012f))
    drawPath(w.b, Brush.verticalGradient(listOf(red, deep), vy, vy + d * 0.055f))
    w.c.reset(); w.c.moveTo(g.cx - d * 0.19f, vy + d * 0.012f); w.c.quadraticTo(g.cx, vy + d * 0.075f, g.cx + d * 0.19f, vy + d * 0.012f)
    stitch(w.c, Color.White.copy(alpha = 0.45f), d)
}

private fun DrawScope.drawBeret(w: WardrobePaths, g: WardrobeGeo) {
    val d = g.d; val t = g.bodyTop
    val wine = Color(0xFFBE123C); val deep = Color(0xFF4C0519)
    rotate(-10f, Offset(g.cx, t + d * 0.03f)) {
        // Head band
        drawRoundRect(deep, Offset(g.cx - d * 0.17f, t + d * 0.025f), Size(d * 0.34f, d * 0.04f), CornerRadius(d * 0.02f))
        // Puffy crown
        val tl = Offset(g.cx - d * 0.25f, t - d * 0.07f); val sz = Size(d * 0.44f, d * 0.115f)
        drawOval(Color.Black.copy(alpha = 0.22f), tl + Offset(0f, d * 0.008f), sz)
        drawOval(Brush.radialGradient(listOf(Color(0xFFFB7185), wine, deep), Offset(g.cx - d * 0.08f, t - d * 0.04f), d * 0.26f), tl, sz)
        drawArc(Color.White.copy(alpha = 0.25f), 200f, 100f, false, tl + Offset(d * 0.03f, d * 0.012f), Size(sz.width - d * 0.06f, sz.height - d * 0.024f), style = Stroke(width = d * 0.006f, cap = StrokeCap.Round))
        // Stalk (tippet)
        drawLine(deep, Offset(g.cx - d * 0.03f, t - d * 0.068f), Offset(g.cx - d * 0.022f, t - d * 0.098f), d * 0.012f, StrokeCap.Round)
        // Artist pin: tiny palette with paint dots
        val pc = Offset(g.cx + d * 0.12f, t - d * 0.005f)
        drawOval(Color(0xFFFDE68A), Offset(pc.x - d * 0.032f, pc.y - d * 0.022f), Size(d * 0.064f, d * 0.044f))
        drawOval(GoldDeep, Offset(pc.x - d * 0.032f, pc.y - d * 0.022f), Size(d * 0.064f, d * 0.044f), style = Stroke(width = d * 0.003f))
        drawCircle(Color(0xFF38BDF8), d * 0.0065f, pc + Offset(-d * 0.014f, -d * 0.006f))
        drawCircle(Color(0xFF10B981), d * 0.0065f, pc + Offset(d * 0.002f, -d * 0.011f))
        drawCircle(Color(0xFFE11D48), d * 0.0065f, pc + Offset(d * 0.016f, -d * 0.002f))
        drawCircle(Color(0xFFA78BFA), d * 0.0065f, pc + Offset(-d * 0.004f, d * 0.009f))
    }
}

private fun DrawScope.drawWreath(w: WardrobePaths, g: WardrobeGeo) {
    val d = g.d
    val c = Offset(g.cx, g.bodyTop + d * 0.045f)
    val rx = d * 0.20f; val ry = d * 0.05f
    // back half first (darker), then front half
    for (pass in 0..1) {
        var deg = if (pass == 0) 190f else 10f
        val end = if (pass == 0) 350f else 170f
        while (deg <= end) {
            val a = Math.toRadians(deg.toDouble())
            val px = c.x + (rx * cos(a)).toFloat(); val py = c.y + (ry * sin(a)).toFloat()
            val tangent = Math.toDegrees(kotlin.math.atan2(ry * cos(a), -rx * sin(a))).toFloat()
            for (side in intArrayOf(-1, 1)) {
                rotate(tangent + side * 38f + 180f, Offset(px, py)) {
                    val L = d * 0.045f; val W = d * 0.016f
                    w.a.reset()
                    w.a.moveTo(px, py)
                    w.a.quadraticTo(px + L * 0.5f, py - W, px + L, py)
                    w.a.quadraticTo(px + L * 0.5f, py + W, px, py)
                    w.a.close()
                    val leaf = if (pass == 0) listOf(Color(0xFF4D7C0F), Color(0xFF365314)) else listOf(Color(0xFFA3E635), Color(0xFF4D7C0F))
                    drawPath(w.a, Brush.horizontalGradient(leaf, px, px + L))
                    drawLine(GoldLight.copy(alpha = 0.7f), Offset(px + L * 0.1f, py), Offset(px + L * 0.8f, py), d * 0.0025f)
                }
            }
            if (pass == 1 && (deg.toInt() / 20) % 2 == 0) drawCircle(Brush.radialGradient(listOf(GoldLight, Gold), Offset(px - d * 0.003f, py - d * 0.003f), d * 0.01f), d * 0.0075f, Offset(px, py + d * 0.012f))
            deg += 20f
        }
        if (pass == 0) drawOval(Color(0xFF3F6212), Offset(c.x - rx, c.y - ry), Size(rx * 2, ry * 2), style = Stroke(width = d * 0.006f))
    }
    drawArc(Color(0xFF4D7C0F), 10f, 160f, false, Offset(c.x - rx, c.y - ry), Size(rx * 2, ry * 2), style = Stroke(width = d * 0.007f))
}

private fun DrawScope.drawGem(center: Offset, r: Float, light: Color, mid: Color, dark: Color) {
    drawCircle(dark, r * 1.25f, center)
    drawCircle(Brush.radialGradient(listOf(light, mid, dark), center - Offset(r * 0.35f, r * 0.35f), r * 1.4f), r, center)
    drawCircle(Color.White.copy(alpha = 0.9f), r * 0.3f, center - Offset(r * 0.35f, r * 0.35f))
}

private fun DrawScope.drawCrown(w: WardrobePaths, g: WardrobeGeo, flicker: Float) {
    val d = g.d; val t = g.bodyTop
    val bandTop = t + d * 0.0f; val bandBot = t + d * 0.05f
    // Velvet cap
    drawOval(Brush.radialGradient(listOf(Color(0xFFC084FC), Color(0xFF7E22CE), Color(0xFF3B0764)), Offset(g.cx - d * 0.03f, t - d * 0.05f), d * 0.14f),
        Offset(g.cx - d * 0.12f, t - d * 0.075f), Size(d * 0.24f, d * 0.11f))
    val p = w.a
    p.reset()
    p.moveTo(g.cx - d * 0.135f, bandBot)
    p.lineTo(g.cx - d * 0.145f, t - d * 0.062f)
    p.lineTo(g.cx - d * 0.09f, t - d * 0.012f)
    p.lineTo(g.cx - d * 0.065f, t - d * 0.082f)
    p.lineTo(g.cx - d * 0.03f, t - d * 0.016f)
    p.lineTo(g.cx, t - d * 0.105f)
    p.lineTo(g.cx + d * 0.03f, t - d * 0.016f)
    p.lineTo(g.cx + d * 0.065f, t - d * 0.082f)
    p.lineTo(g.cx + d * 0.09f, t - d * 0.012f)
    p.lineTo(g.cx + d * 0.145f, t - d * 0.062f)
    p.lineTo(g.cx + d * 0.135f, bandBot)
    p.quadraticTo(g.cx, bandBot + d * 0.022f, g.cx - d * 0.135f, bandBot)
    p.close()
    drawPath(p, Color.Black.copy(alpha = 0.25f), style = Stroke(width = d * 0.012f, join = androidx.compose.ui.graphics.StrokeJoin.Round))
    drawPath(p, Brush.linearGradient(listOf(GoldLight, Gold, GoldDeep, Gold, GoldShadow), Offset(g.cx - d * 0.15f, t - d * 0.1f), Offset(g.cx + d * 0.15f, bandBot)))
    drawPath(p, GoldShadow, style = Stroke(width = d * 0.004f, join = androidx.compose.ui.graphics.StrokeJoin.Round))
    // Band rim lines
    w.b.reset(); w.b.moveTo(g.cx - d * 0.138f, bandTop + d * 0.006f); w.b.quadraticTo(g.cx, bandTop + d * 0.028f, g.cx + d * 0.138f, bandTop + d * 0.006f)
    drawPath(w.b, GoldLight, style = Stroke(width = d * 0.005f))
    // Pearls on tips
    for ((x, y) in listOf(-0.145f to -0.062f, -0.065f to -0.082f, 0f to -0.105f, 0.065f to -0.082f, 0.145f to -0.062f)) {
        val o = Offset(g.cx + x * d, t + y * d - d * 0.008f)
        drawCircle(Brush.radialGradient(listOf(Color.White, Color(0xFFF1F5F9), Color(0xFFCBD5E1)), o - Offset(d * 0.004f, d * 0.004f), d * 0.014f), d * 0.011f, o)
    }
    // Gems on the band
    val gy = bandTop + d * 0.03f
    drawGem(Offset(g.cx, gy), d * 0.016f, Color(0xFFBAE6FD), Color(0xFF0EA5E9), Color(0xFF0C4A6E))
    drawGem(Offset(g.cx - d * 0.075f, gy - d * 0.004f), d * 0.011f, Color(0xFFFECDD3), Color(0xFFE11D48), Color(0xFF881337))
    drawGem(Offset(g.cx + d * 0.075f, gy - d * 0.004f), d * 0.011f, Color(0xFFFECDD3), Color(0xFFE11D48), Color(0xFF881337))
    drawGem(Offset(g.cx - d * 0.115f, gy - d * 0.008f), d * 0.007f, Color(0xFFBBF7D0), Color(0xFF10B981), Color(0xFF064E3B))
    drawGem(Offset(g.cx + d * 0.115f, gy - d * 0.008f), d * 0.007f, Color(0xFFBBF7D0), Color(0xFF10B981), Color(0xFF064E3B))
    drawSparkle(w.c, g.cx + d * 0.012f, t - d * 0.125f, d * 0.016f * flicker, Color.White, ((flicker - 0.92f) * 6f))
}

private fun DrawScope.drawWizardHat(w: WardrobePaths, g: WardrobeGeo, sway: Float) {
    val d = g.d; val t = g.bodyTop
    val baseY = t + d * 0.05f
    val brimTL = Offset(g.cx - d * 0.27f, baseY - d * 0.045f); val brimSz = Size(d * 0.54f, d * 0.09f)
    val violet = Color(0xFF6D28D9); val deep = Color(0xFF2E1065)
    drawOval(Color.Black.copy(alpha = 0.25f), brimTL + Offset(0f, d * 0.008f), brimSz)
    drawOval(Brush.verticalGradient(listOf(Color(0xFF4C1D95), deep), brimTL.y, brimTL.y + brimSz.height), brimTL, brimSz)
    val tipX = g.cx + d * 0.13f + sway * d * 0.003f; val tipY = t - d * 0.29f
    val p = w.a
    p.reset()
    p.moveTo(g.cx - d * 0.15f, baseY)
    p.cubicTo(g.cx - d * 0.11f, t - d * 0.08f, g.cx - d * 0.03f, t - d * 0.21f, tipX, tipY)
    p.cubicTo(g.cx + d * 0.05f, t - d * 0.17f, g.cx + d * 0.12f, t - d * 0.07f, g.cx + d * 0.15f, baseY)
    p.quadraticTo(g.cx, baseY + d * 0.035f, g.cx - d * 0.15f, baseY)
    p.close()
    drawPath(p, Brush.linearGradient(listOf(Color(0xFF8B5CF6), violet, deep), Offset(g.cx - d * 0.15f, t - d * 0.1f), Offset(g.cx + d * 0.15f, baseY)))
    clipPath(p) {
        w.c.star5(g.cx - d * 0.05f, t - d * 0.07f, d * 0.02f); drawPath(w.c, Color(0xFFFDE047))
        w.c.star5(g.cx + d * 0.06f, t - d * 0.13f, d * 0.013f); drawPath(w.c, Color(0xFFFDE047).copy(alpha = 0.85f))
        drawCircle(Color(0xFFFEF9C3), d * 0.016f, Offset(g.cx + d * 0.065f, t - d * 0.04f))
        drawCircle(violet, d * 0.014f, Offset(g.cx + d * 0.074f, t - d * 0.046f))
        drawCircle(Color.White.copy(alpha = 0.8f), d * 0.004f, Offset(g.cx - d * 0.01f, t - d * 0.17f))
        drawCircle(Color.White.copy(alpha = 0.8f), d * 0.003f, Offset(g.cx - d * 0.09f, t - d * 0.01f))
        // Golden hat band with buckle
        w.b.reset(); w.b.moveTo(g.cx - d * 0.2f, baseY - d * 0.022f); w.b.quadraticTo(g.cx, baseY + d * 0.012f, g.cx + d * 0.2f, baseY - d * 0.022f)
        drawPath(w.b, Gold, style = Stroke(width = d * 0.03f))
        drawPath(w.b, GoldShadow.copy(alpha = 0.6f), style = Stroke(width = d * 0.003f))
    }
    drawRoundRect(GoldLight, Offset(g.cx - d * 0.024f, baseY - d * 0.026f), Size(d * 0.048f, d * 0.03f), CornerRadius(d * 0.005f), style = Stroke(width = d * 0.006f))
    // Front lip of the brim overlapping the cone
    drawArc(Brush.verticalGradient(listOf(Color(0xFF5B21B6), deep), baseY, baseY + d * 0.045f), 0f, 180f, true, brimTL, brimSz)
    drawArc(Color.White.copy(alpha = 0.2f), 20f, 140f, false, brimTL, brimSz, style = Stroke(width = d * 0.004f))
    // Twinkling star charm at the tip
    drawSparkle(w.c, tipX + d * 0.012f, tipY + d * 0.01f, d * 0.022f, Color(0xFFFDE047))
}

private fun DrawScope.drawSprout(w: WardrobePaths, g: WardrobeGeo, sway: Float) {
    val d = g.d; val base = Offset(g.cx, g.bodyTop + d * 0.008f)
    rotate(sway, base) {
        val top = Offset(g.cx + d * 0.006f, g.bodyTop - d * 0.11f)
        w.a.reset(); w.a.moveTo(base.x, base.y); w.a.quadraticTo(g.cx + d * 0.018f, g.bodyTop - d * 0.05f, top.x, top.y)
        drawPath(w.a, Color(0xFF047857), style = Stroke(width = d * 0.016f, cap = StrokeCap.Round))
        drawPath(w.a, Color(0xFF34D399), style = Stroke(width = d * 0.008f, cap = StrokeCap.Round))
        for (s in intArrayOf(-1, 1)) {
            val tip = Offset(top.x + s * d * 0.11f, top.y - d * 0.05f)
            w.b.reset()
            w.b.moveTo(top.x, top.y)
            w.b.cubicTo(top.x + s * d * 0.02f, top.y - d * 0.07f, tip.x - s * d * 0.01f, tip.y - d * 0.03f, tip.x, tip.y)
            w.b.cubicTo(tip.x - s * d * 0.02f, tip.y + d * 0.05f, top.x + s * d * 0.04f, top.y + d * 0.02f, top.x, top.y)
            w.b.close()
            drawPath(w.b, Brush.linearGradient(if (s > 0) listOf(Color(0xFF6EE7B7), Color(0xFF059669)) else listOf(Color(0xFF34D399), Color(0xFF047857)), top, tip))
            drawPath(w.b, Color(0xFF065F46).copy(alpha = 0.5f), style = Stroke(width = d * 0.003f))
            w.c.reset(); w.c.moveTo(top.x, top.y); w.c.quadraticTo(top.x + s * d * 0.05f, top.y - d * 0.04f, tip.x - s * d * 0.01f, tip.y + d * 0.004f)
            drawPath(w.c, Color(0xFFD1FAE5).copy(alpha = 0.8f), style = Stroke(width = d * 0.004f, cap = StrokeCap.Round))
        }
        // Dew drop
        drawCircle(Color(0xCCE0F2FE), d * 0.011f, Offset(top.x + d * 0.07f, top.y - d * 0.025f))
        drawCircle(Color.White, d * 0.004f, Offset(top.x + d * 0.067f, top.y - d * 0.029f))
    }
}
