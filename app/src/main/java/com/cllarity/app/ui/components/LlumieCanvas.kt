package com.cllarity.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import com.cllarity.app.ui.theme.rememberHaptics
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cllarity.app.model.ClothingColor
import com.cllarity.app.model.LlumieCosmetic
import com.cllarity.app.model.LlumieMood
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Animated visual states for Llumie mascot.
 */
enum class LlumieState {
    IDLE,
    JOYFUL,
    SLEEPING
}

/**
 * Facial expression layer of Llumie (independent from the body pose).
 * NEUTRAL & HAPPY keep the classic look; the rest add new emotions.
 */
enum class LlumieEmotion {
    NEUTRAL,
    HAPPY,
    PROUD,
    LOVING,
    SLEEPY,
    CALM,
    SURPRISED
}

private fun defaultEmotionFor(state: LlumieState) = when (state) {
    LlumieState.JOYFUL -> LlumieEmotion.HAPPY
    LlumieState.SLEEPING -> LlumieEmotion.SLEEPY
    LlumieState.IDLE -> LlumieEmotion.NEUTRAL
}

/**
 * Cache holder for Path objects to guarantee ZERO object allocations
 * in the 60/120 FPS onDraw rendering loop.
 */
private class LlumiePathHolder {
    val bodyPath = Path()
    val leftEarOuterPath = Path()
    val leftEarInnerPath = Path()
    val rightEarOuterPath = Path()
    val rightEarInnerPath = Path()
    val chestCorePath = Path()
    val mouthPath = Path()
    val leftEyeArchPath = Path()
    val rightEyeArchPath = Path()
    val slumberBlanketPath = Path()
    val headHighlightPath = Path()
    val wizardHatPath = Path()
    val crownPath = Path()
    val sproutStemPath = Path()
    val sproutLeafPath = Path()
    val capVisorPath = Path()
    val bowLeftPath = Path()
    val bowRightPath = Path()
    val clothingBodyPath = Path()
    val clothingCollarPath = Path()
    val beltPath = Path()
    val leftWingPath = Path()
    val rightWingPath = Path()
    val wardrobe = WardrobePaths()
    val fxPath = Path()
}

/**
 * Llumie Mascot Component - Fusion of a soft, fluffy bunny and an ethereal flame of light.
 * Supports:
 * - Anatomically fitted clothes tailored to Llumie's organic teardrop figure using matching Bézier curves.
 * - Multimodal Petting Engine with drag gestures, head tilting, purring haptics, and spring bounce.
 * - Multi-equip mixing across Headwear, Colorable Clothing, and Accessories.
 * - Milestone Streak Relics (7, 30, 90 days).
 */
@Composable
fun LlumieMascot(
    state: LlumieState,
    modifier: Modifier = Modifier,
    cosmetic: LlumieCosmetic = LlumieCosmetic.NONE,
    cosmetics: Set<LlumieCosmetic> = if (cosmetic != LlumieCosmetic.NONE) setOf(cosmetic) else emptySet(),
    clothingColor: ClothingColor = ClothingColor.LAVENDER,
    emotion: LlumieEmotion = defaultEmotionFor(state),
    onTap: () -> Unit = {},
    onPet: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val haptics = rememberHaptics()

    // Tap reaction: a short "surprised" hop
    var surpriseTick by remember { mutableLongStateOf(0L) }
    var isSurprised by remember { mutableStateOf(false) }
    LaunchedEffect(surpriseTick) {
        if (surpriseTick != 0L) {
            isSurprised = true
            delay(850)
            isSurprised = false
        }
    }
    val activeEmotion = if (isSurprised && state != LlumieState.SLEEPING) LlumieEmotion.SURPRISED else emotion

    // Path cache instance retained across recompositions - zero GC overhead during draw
    val paths = remember { LlumiePathHolder() }

    // -------------------------------------------------------------------------
    // MULTIMODAL PETTING ENGINE (Pointer Tracking, Head Tilt, Purring Haptics)
    // -------------------------------------------------------------------------
    var isPetting by remember { mutableStateOf(false) }
    var rawDragX by remember { mutableFloatStateOf(0f) }
    var rawDragY by remember { mutableFloatStateOf(0f) }
    var lastHapticTickTime by remember { mutableLongStateOf(0L) }

    // Spring physics for drag tilt returning to zero with pleasant bounce (Apple spring spec)
    val animatedTiltX by animateFloatAsState(
        targetValue = if (isPetting) rawDragX else 0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow),
        label = "tiltX"
    )
    val animatedTiltY by animateFloatAsState(
        targetValue = if (isPetting) rawDragY else 0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow),
        label = "tiltY"
    )

    // -------------------------------------------------------------------------
    // 1. SPRING PHYSICS FOR STATE TRANSITIONS (Apple Fluid Interface specs)
    // -------------------------------------------------------------------------

    // Vertical jump offset for JOYFUL state & tap response
    val targetJumpOffset = when {
        isPetting -> 10f
        isSurprised && state != LlumieState.SLEEPING -> -30f
        state == LlumieState.JOYFUL -> -24f
        state == LlumieState.SLEEPING -> 14f
        else -> 0f
    }
    val animatedJumpOffsetY by animateFloatAsState(
        targetValue = targetJumpOffset,
        animationSpec = spring(
            dampingRatio = if (state == LlumieState.JOYFUL) 0.62f else Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "jumpOffset"
    )

    // Squash & Stretch physics (scaleX with volume preservation: scaleY = 1 / sqrt(scaleX))
    val targetSquashX = when {
        isPetting -> 1.12f
        state == LlumieState.JOYFUL -> 0.93f
        state == LlumieState.SLEEPING -> 1.16f
        else -> 1.0f
    }
    val animatedSquashX by animateFloatAsState(
        targetValue = targetSquashX,
        animationSpec = spring(
            dampingRatio = 0.58f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "squashX"
    )
    val effectiveScaleX = animatedSquashX
    val effectiveScaleY = 1f / sqrt(effectiveScaleX)

    // Ear Droop factor for SLEEPING transition
    val targetEarDroop = if (state == LlumieState.SLEEPING) 1f else 0f
    val animatedEarDroop by animateFloatAsState(
        targetValue = targetEarDroop,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = Spring.StiffnessLow
        ),
        label = "earDroop"
    )

    // Core light glow intensity
    val targetCoreGlow = when {
        isPetting -> 1.35f // Glows brightly with warmth when petted!
        state == LlumieState.JOYFUL -> 1.25f
        state == LlumieState.SLEEPING -> 0.40f
        else -> 0.85f
    }
    val animatedCoreIntensity by animateFloatAsState(
        targetValue = targetCoreGlow,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessLow),
        label = "coreGlow"
    )

    // -------------------------------------------------------------------------
    // 2. CONTINUOUS AMBIENT PHYSICS (Harmonic infinite loop at 60/120 FPS)
    // -------------------------------------------------------------------------
    val infiniteTransition = rememberInfiniteTransition(label = "LlumieAmbientPhysics")

    // Vertical idle levitation
    val idleLevitationY by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idleLevitation"
    )

    // Subtle rhythmic breathing
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.015f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (state == LlumieState.SLEEPING) 3600 else 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    )

    // Flame Ears: Organic independent harmonic swaying
    val leftEarFlameSway by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leftEarSway"
    )
    val rightEarFlameSway by infiniteTransition.animateFloat(
        initialValue = 5f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2100, delayMillis = 120, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rightEarSway"
    )

    // Sprout leaf swaying
    val sproutSway by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sproutSway"
    )

    // Luminous inner core pulse
    val coreHeartbeatPulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (state == LlumieState.JOYFUL || isPetting) 800 else 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "corePulse"
    )

    // Halo pulse & Orbiting stars
    val haloPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.60f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloAlpha"
    )
    val starsOrbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "starsOrbit"
    )

    // Periodic blinking when awake
    var eyeBlinkScale by remember { mutableFloatStateOf(1f) }
    LaunchedEffect(state) {
        if (state != LlumieState.SLEEPING) {
            while (true) {
                delay((3000..5500).random().toLong())
                eyeBlinkScale = 0.08f
                delay(120)
                eyeBlinkScale = 1.0f
            }
        }
    }

    // Active items set resolution
    val activeSet = if (cosmetics.isNotEmpty()) cosmetics else if (cosmetic != LlumieCosmetic.NONE) setOf(cosmetic) else emptySet()

    // -------------------------------------------------------------------------
    // 3. CANVAS DRAWING IMPLEMENTATION (Clean responsive vector math)
    // -------------------------------------------------------------------------
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    haptics.click()
                    surpriseTick = System.nanoTime()
                    onTap()
                })
            }
            .pointerInput(state) {
                // Multimodal Petting Engine: drag gestures with continuous purring haptics!
                detectDragGestures(
                    onDragStart = {
                        isPetting = true
                        haptics.click()
                        lastHapticTickTime = System.currentTimeMillis()
                        onPet()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        rawDragX = (rawDragX + dragAmount.x * 0.4f).coerceIn(-32f, 32f)
                        rawDragY = (rawDragY + dragAmount.y * 0.4f).coerceIn(-18f, 18f)

                        // Rhythmic purring/heartbeat haptic feedback tick every 160ms while dragging
                        val now = System.currentTimeMillis()
                        if (now - lastHapticTickTime > 160L) {
                            haptics.tick()
                            lastHapticTickTime = now
                        }
                    },
                    onDragEnd = {
                        isPetting = false
                        rawDragX = 0f
                        rawDragY = 0f
                        haptics.tick()
                        onTap()
                    },
                    onDragCancel = {
                        isPetting = false
                        rawDragX = 0f
                        rawDragY = 0f
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height
            val dim = min(canvasW, canvasH)

            val centerX = canvasW / 2f + animatedTiltX * 0.6f

            // Generous headroom (0.58f) so ears, beanies, and wizard hats never clip!
            val effectiveLevitation = if (state == LlumieState.SLEEPING) 0f else idleLevitationY
            val centerY = canvasH * 0.58f + animatedJumpOffsetY + effectiveLevitation + animatedTiltY * 0.5f

            // Ground Contact Shadow
            val shadowBaseY = canvasH * 0.90f
            val shadowElevationFactor = (1f - (centerY - canvasH * 0.58f) / (dim * 0.4f)).coerceIn(0.5f, 1.4f)
            val shadowW = dim * 0.56f * (1f / shadowElevationFactor) * effectiveScaleX
            val shadowH = dim * 0.085f * effectiveScaleY
            val shadowAlpha = when (state) {
                LlumieState.SLEEPING -> 0.38f
                LlumieState.JOYFUL -> 0.16f
                else -> 0.28f
            }
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1E153A).copy(alpha = shadowAlpha),
                        Color(0xFF1E153A).copy(alpha = shadowAlpha * 0.4f),
                        Color.Transparent
                    ),
                    center = Offset(centerX, shadowBaseY),
                    radius = shadowW / 2f
                ),
                topLeft = Offset(centerX - shadowW / 2f, shadowBaseY - shadowH / 2f),
                size = Size(shadowW, shadowH)
            )

            // Dynamic scale container (Squash & Stretch applied from bottom-center pivot)
            scale(
                scaleX = effectiveScaleX,
                scaleY = effectiveScaleY * breathScale,
                pivot = Offset(centerX, centerY + dim * 0.32f)
            ) {
                val bodyW = dim * 0.54f
                val bodyH = dim * 0.56f
                val bodyTop = centerY - bodyH * 0.38f
                val bodyBottom = centerY + bodyH * 0.46f

                val geo = WardrobeGeo(centerX, centerY, dim, bodyTop, bodyBottom, bodyW)
                val isSleeping = state == LlumieState.SLEEPING

                // =============================================================
                // AMBIENT COSMETICS: BEHIND BODY (Wings, Stars, Halo, Capes, Backpack)
                // =============================================================
                drawWardrobeBack(
                    w = paths.wardrobe,
                    g = geo,
                    set = activeSet,
                    clothing = clothingColor,
                    orbitDeg = starsOrbitAngle,
                    pulse = haloPulseAlpha,
                    flap = leftEarFlameSway,
                    sleeping = isSleeping
                )

                // =============================================================
                // LAYER 1: FLAME EARS (Behind the body)
                // =============================================================
                drawFlameEars(
                    paths = paths,
                    centerX = centerX,
                    headTop = bodyTop,
                    bodyW = bodyW,
                    dim = dim,
                    leftSway = leftEarFlameSway + animatedTiltX * 0.4f,
                    rightSway = rightEarFlameSway + animatedTiltX * 0.4f,
                    earDroop = animatedEarDroop
                )

                // =============================================================
                // LAYER 2: ETHEREAL BODY (Teardrop Bunny Body with Glass Glow)
                // =============================================================
                drawEtherealBody(
                    paths = paths,
                    centerX = centerX,
                    centerY = centerY,
                    bodyTop = bodyTop,
                    bodyBottom = bodyBottom,
                    bodyW = bodyW,
                    dim = dim,
                    coreIntensity = animatedCoreIntensity
                )

                // =============================================================
                // LAYER 3: LUMINOUS INNER CORE (Heart of Light on Chest)
                // =============================================================
                drawHeartOfLight(
                    paths = paths,
                    centerX = centerX,
                    chestY = centerY + dim * 0.06f,
                    dim = dim,
                    coreIntensity = animatedCoreIntensity * coreHeartbeatPulse
                )

                // =============================================================
                // LAYER 4: CLOTHING LAYER (ANATOMICALLY FITTED TO LLUMIE'S FIGURE!)
                // =============================================================
                if (!isSleeping) {
                    drawWardrobeClothing(
                        w = paths.wardrobe,
                        bodyPath = paths.bodyPath,
                        g = geo,
                        set = activeSet,
                        clothing = clothingColor
                    )
                }

                // =============================================================
                // LAYER 5: EXPRESSIVE FACE (Eyes & Tiny Bunny Mouth)
                // =============================================================
                drawExpressiveFace(
                    paths = paths,
                    centerX = centerX,
                    faceCenterY = centerY - dim * 0.05f,
                    dim = dim,
                    state = state,
                    blinkScale = eyeBlinkScale,
                    isPetting = isPetting,
                    emotion = activeEmotion,
                    fxPhase = starsOrbitAngle / 360f
                )

                // =============================================================
                // LAYER 6: VISIBLE ACCESSORIES (Hats, Stickers, Glasses, Scarf, Relics)
                // =============================================================
                drawWardrobeAccessories(
                    w = paths.wardrobe,
                    body = paths.bodyPath,
                    g = geo,
                    set = activeSet,
                    sproutSway = sproutSway,
                    flicker = coreHeartbeatPulse,
                    orbitDeg = starsOrbitAngle,
                    sleeping = isSleeping
                )

                // =============================================================
                // LAYER 7: SLEEPING BLANKET OVERLAY (Ears wrapping body in SLEEPING)
                // =============================================================
                if (animatedEarDroop > 0.15f) {
                    drawSleepingBlanket(
                        paths = paths,
                        centerX = centerX,
                        bodyBottom = bodyBottom,
                        bodyW = bodyW,
                        dim = dim,
                        droopProgress = animatedEarDroop
                    )
                }
            }
        }
    }
}

// =============================================================================
// GRAPHICS ENGINE: VECTOR PATH DRAWING FUNCTIONS
// =============================================================================

private fun DrawScope.drawFlameEars(
    paths: LlumiePathHolder,
    centerX: Float,
    headTop: Float,
    bodyW: Float,
    dim: Float,
    leftSway: Float,
    rightSway: Float,
    earDroop: Float
) {
    val earBaseSpacing = bodyW * 0.16f
    val earLength = dim * 0.30f

    // LEWE UCHO (PŁOMIEŃ)
    val leftBaseInnerX = centerX - earBaseSpacing * 0.45f
    val leftBaseOuterX = centerX - earBaseSpacing * 1.55f
    val earBaseY = headTop + dim * 0.04f

    val normalLeftTipX = centerX - bodyW * 0.28f + leftSway
    val normalLeftTipY = headTop - earLength
    val droopLeftTipX = centerX - bodyW * 0.46f
    val droopLeftTipY = headTop + dim * 0.32f

    val leftTipX = normalLeftTipX * (1f - earDroop) + droopLeftTipX * earDroop
    val leftTipY = normalLeftTipY * (1f - earDroop) + droopLeftTipY * earDroop

    paths.leftEarOuterPath.reset()
    paths.leftEarOuterPath.moveTo(leftBaseOuterX, earBaseY)
    if (earDroop > 0.4f) {
        paths.leftEarOuterPath.cubicTo(
            leftBaseOuterX - dim * 0.10f, earBaseY + dim * 0.10f,
            leftTipX - dim * 0.08f, leftTipY - dim * 0.08f,
            leftTipX, leftTipY
        )
        paths.leftEarOuterPath.cubicTo(
            leftTipX + dim * 0.06f, leftTipY - dim * 0.08f,
            leftBaseInnerX - dim * 0.04f, earBaseY + dim * 0.10f,
            leftBaseInnerX, earBaseY
        )
    } else {
        paths.leftEarOuterPath.cubicTo(
            leftBaseOuterX - dim * 0.10f, earBaseY - earLength * 0.45f,
            leftTipX - dim * 0.08f, leftTipY + earLength * 0.25f,
            leftTipX, leftTipY
        )
        paths.leftEarOuterPath.cubicTo(
            leftTipX + dim * 0.06f, leftTipY + earLength * 0.35f,
            leftBaseInnerX - dim * 0.02f, earBaseY - earLength * 0.30f,
            leftBaseInnerX, earBaseY
        )
    }
    paths.leftEarOuterPath.close()

    drawPath(
        path = paths.leftEarOuterPath,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF67E8F9), Color(0xFFA78BFA), Color(0xFF7C3AED)),
            startY = leftTipY,
            endY = earBaseY
        )
    )

    // Wewnętrzny cyjanowy rdzeń lewego płomienia
    paths.leftEarInnerPath.reset()
    val innerLeftBaseX = (leftBaseOuterX + leftBaseInnerX) / 2f
    val innerLeftTipX = leftTipX + (centerX - leftTipX) * 0.12f
    val innerLeftTipY = leftTipY + dim * 0.07f
    paths.leftEarInnerPath.moveTo(innerLeftBaseX - dim * 0.022f, earBaseY)
    paths.leftEarInnerPath.quadraticTo(
        innerLeftBaseX - dim * 0.04f, (earBaseY + innerLeftTipY) / 2f,
        innerLeftTipX, innerLeftTipY
    )
    paths.leftEarInnerPath.quadraticTo(
        innerLeftBaseX + dim * 0.03f, (earBaseY + innerLeftTipY) / 2f,
        innerLeftBaseX + dim * 0.022f, earBaseY
    )
    paths.leftEarInnerPath.close()

    drawPath(
        path = paths.leftEarInnerPath,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFE0F2FE), Color(0x6667E8F9), Color.Transparent),
            startY = innerLeftTipY,
            endY = earBaseY
        )
    )

    // PRAWE UCHO (PŁOMIEŃ)
    val rightBaseInnerX = centerX + earBaseSpacing * 0.45f
    val rightBaseOuterX = centerX + earBaseSpacing * 1.55f

    val normalRightTipX = centerX + bodyW * 0.28f + rightSway
    val normalRightTipY = headTop - earLength
    val droopRightTipX = centerX + bodyW * 0.46f
    val droopRightTipY = headTop + dim * 0.32f

    val rightTipX = normalRightTipX * (1f - earDroop) + droopRightTipX * earDroop
    val rightTipY = normalRightTipY * (1f - earDroop) + droopRightTipY * earDroop

    paths.rightEarOuterPath.reset()
    paths.rightEarOuterPath.moveTo(rightBaseInnerX, earBaseY)
    if (earDroop > 0.4f) {
        paths.rightEarOuterPath.cubicTo(
            rightBaseInnerX + dim * 0.04f, earBaseY + dim * 0.10f,
            rightTipX - dim * 0.06f, rightTipY - dim * 0.08f,
            rightTipX, rightTipY
        )
        paths.rightEarOuterPath.cubicTo(
            rightTipX + dim * 0.08f, rightTipY - dim * 0.08f,
            rightBaseOuterX + dim * 0.10f, earBaseY + dim * 0.10f,
            rightBaseOuterX, earBaseY
        )
    } else {
        paths.rightEarOuterPath.cubicTo(
            rightBaseInnerX + dim * 0.02f, earBaseY - earLength * 0.30f,
            rightTipX - dim * 0.06f, rightTipY + earLength * 0.35f,
            rightTipX, rightTipY
        )
        paths.rightEarOuterPath.cubicTo(
            rightTipX + dim * 0.08f, rightTipY + earLength * 0.25f,
            rightBaseOuterX + dim * 0.10f, earBaseY - earLength * 0.45f,
            rightBaseOuterX, earBaseY
        )
    }
    paths.rightEarOuterPath.close()

    drawPath(
        path = paths.rightEarOuterPath,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF67E8F9), Color(0xFFA78BFA), Color(0xFF7C3AED)),
            startY = rightTipY,
            endY = earBaseY
        )
    )

    // Wewnętrzny cyjanowy rdzeń prawego płomienia
    paths.rightEarInnerPath.reset()
    val innerRightBaseX = (rightBaseOuterX + rightBaseInnerX) / 2f
    val innerRightTipX = rightTipX + (centerX - rightTipX) * 0.12f
    val innerRightTipY = rightTipY + dim * 0.07f
    paths.rightEarInnerPath.moveTo(innerRightBaseX - dim * 0.022f, earBaseY)
    paths.rightEarInnerPath.quadraticTo(
        innerRightBaseX - dim * 0.03f, (earBaseY + innerRightTipY) / 2f,
        innerRightTipX, innerRightTipY
    )
    paths.rightEarInnerPath.quadraticTo(
        innerRightBaseX + dim * 0.04f, (earBaseY + innerRightTipY) / 2f,
        innerRightBaseX + dim * 0.022f, earBaseY
    )
    paths.rightEarInnerPath.close()

    drawPath(
        path = paths.rightEarInnerPath,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFE0F2FE), Color(0x6667E8F9), Color.Transparent),
            startY = innerRightTipY,
            endY = earBaseY
        )
    )
}

private fun DrawScope.drawEtherealBody(
    paths: LlumiePathHolder,
    centerX: Float,
    centerY: Float,
    bodyTop: Float,
    bodyBottom: Float,
    bodyW: Float,
    dim: Float,
    coreIntensity: Float
) {
    val headRadiusX = bodyW * 0.35f
    val bellyRadiusX = bodyW * 0.50f
    val waistY = centerY - dim * 0.04f

    paths.bodyPath.reset()
    paths.bodyPath.moveTo(centerX - headRadiusX, bodyTop + dim * 0.08f)
    paths.bodyPath.cubicTo(
        centerX - headRadiusX, bodyTop - dim * 0.02f,
        centerX + headRadiusX, bodyTop - dim * 0.02f,
        centerX + headRadiusX, bodyTop + dim * 0.08f
    )
    paths.bodyPath.cubicTo(
        centerX + headRadiusX * 1.15f, waistY - dim * 0.02f,
        centerX + bellyRadiusX * 1.05f, centerY + dim * 0.16f,
        centerX + bellyRadiusX, bodyBottom - dim * 0.06f
    )
    paths.bodyPath.cubicTo(
        centerX + bellyRadiusX * 0.65f, bodyBottom + dim * 0.03f,
        centerX - bellyRadiusX * 0.65f, bodyBottom + dim * 0.03f,
        centerX - bellyRadiusX, bodyBottom - dim * 0.06f
    )
    paths.bodyPath.cubicTo(
        centerX - bellyRadiusX * 1.05f, centerY + dim * 0.16f,
        centerX - headRadiusX * 1.15f, waistY - dim * 0.02f,
        centerX - headRadiusX, bodyTop + dim * 0.08f
    )
    paths.bodyPath.close()

    // Ambient Glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0x558B5CF6).copy(alpha = 0.35f * coreIntensity),
                Color(0x22C4B5FD),
                Color.Transparent
            ),
            center = Offset(centerX, centerY + dim * 0.06f),
            radius = bodyW * 0.85f
        ),
        radius = bodyW * 0.85f,
        center = Offset(centerX, centerY + dim * 0.06f)
    )

    // Body Fill
    drawPath(
        path = paths.bodyPath,
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFEDE9FE),
                Color(0xFFC4B5FD),
                Color(0xFF8B5CF6),
                Color(0xFF4C1D95)
            ),
            center = Offset(centerX, centerY - dim * 0.06f),
            radius = bodyW * 0.90f
        )
    )

    // Specular Highlight
    paths.headHighlightPath.reset()
    val hlW = bodyW * 0.38f
    val hlTop = bodyTop + dim * 0.03f
    paths.headHighlightPath.moveTo(centerX - hlW * 0.5f, hlTop)
    paths.headHighlightPath.cubicTo(
        centerX - hlW * 0.2f, hlTop - dim * 0.015f,
        centerX + hlW * 0.2f, hlTop - dim * 0.015f,
        centerX + hlW * 0.5f, hlTop
    )
    paths.headHighlightPath.cubicTo(
        centerX + hlW * 0.25f, hlTop + dim * 0.05f,
        centerX - hlW * 0.25f, hlTop + dim * 0.05f,
        centerX - hlW * 0.5f, hlTop
    )
    paths.headHighlightPath.close()

    drawPath(
        path = paths.headHighlightPath,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0x77FFFFFF), Color(0x05FFFFFF)),
            startY = hlTop,
            endY = hlTop + dim * 0.05f
        )
    )
}

private fun DrawScope.drawHeartOfLight(
    paths: LlumiePathHolder,
    centerX: Float,
    chestY: Float,
    dim: Float,
    coreIntensity: Float
) {
    val coreR = dim * 0.075f * coreIntensity

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xCC67E8F9),
                Color(0x5538BDF8),
                Color.Transparent
            ),
            center = Offset(centerX, chestY),
            radius = coreR * 2.8f
        ),
        radius = coreR * 2.8f,
        center = Offset(centerX, chestY)
    )

    paths.chestCorePath.reset()
    val heartW = coreR * 1.3f
    val heartH = coreR * 1.5f
    paths.chestCorePath.moveTo(centerX, chestY + heartH * 0.65f)
    paths.chestCorePath.cubicTo(
        centerX - heartW * 1.1f, chestY + heartH * 0.1f,
        centerX - heartW * 0.9f, chestY - heartH * 0.65f,
        centerX, chestY - heartH * 0.35f
    )
    paths.chestCorePath.cubicTo(
        centerX + heartW * 0.9f, chestY - heartH * 0.65f,
        centerX + heartW * 1.1f, chestY + heartH * 0.1f,
        centerX, chestY + heartH * 0.65f
    )
    paths.chestCorePath.close()

    drawPath(
        path = paths.chestCorePath,
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFFFFF), Color(0xFFA5F3FC), Color(0xFF38BDF8)),
            center = Offset(centerX, chestY - heartH * 0.1f),
            radius = coreR * 1.2f
        )
    )
}

private fun DrawScope.drawExpressiveFace(
    paths: LlumiePathHolder,
    centerX: Float,
    faceCenterY: Float,
    dim: Float,
    state: LlumieState,
    blinkScale: Float,
    isPetting: Boolean,
    emotion: LlumieEmotion = LlumieEmotion.NEUTRAL,
    fxPhase: Float = 0f
) {
    val eyeSpacing = dim * 0.105f
    val eyeW = dim * 0.024f
    val eyeH = dim * 0.042f * blinkScale
    val awake = state != LlumieState.SLEEPING

    // 1. OCZY
    if (awake && !isPetting && emotion == LlumieEmotion.LOVING) {
        drawHeartEyes(paths.fxPath, centerX, faceCenterY, eyeSpacing, dim, blinkScale)
    } else if (awake && !isPetting && emotion == LlumieEmotion.SURPRISED) {
        drawRoundEyes(centerX, faceCenterY, eyeSpacing, dim)
    } else if (awake && !isPetting && emotion == LlumieEmotion.PROUD) {
        drawStarryEyes(paths.fxPath, centerX, faceCenterY, eyeSpacing, dim, blinkScale)
    } else if (state == LlumieState.JOYFUL || isPetting) {
        val archSpan = dim * 0.038f
        paths.leftEyeArchPath.reset()
        val leftEyeX = centerX - eyeSpacing
        paths.leftEyeArchPath.moveTo(leftEyeX - archSpan / 2f, faceCenterY + dim * 0.008f)
        paths.leftEyeArchPath.quadraticTo(
            leftEyeX, faceCenterY - dim * 0.028f,
            leftEyeX + archSpan / 2f, faceCenterY + dim * 0.008f
        )
        drawPath(
            path = paths.leftEyeArchPath,
            color = Color(0xFF1E153A),
            style = Stroke(width = dim * 0.016f, cap = StrokeCap.Round)
        )

        paths.rightEyeArchPath.reset()
        val rightEyeX = centerX + eyeSpacing
        paths.rightEyeArchPath.moveTo(rightEyeX - archSpan / 2f, faceCenterY + dim * 0.008f)
        paths.rightEyeArchPath.quadraticTo(
            rightEyeX, faceCenterY - dim * 0.028f,
            rightEyeX + archSpan / 2f, faceCenterY + dim * 0.008f
        )
        drawPath(
            path = paths.rightEyeArchPath,
            color = Color(0xFF1E153A),
            style = Stroke(width = dim * 0.016f, cap = StrokeCap.Round)
        )
    } else if (state == LlumieState.SLEEPING) {
        val sleepSpan = dim * 0.034f
        val eyeSleepY = faceCenterY + dim * 0.015f
        drawLine(
            color = Color(0xFF312E81),
            start = Offset(centerX - eyeSpacing - sleepSpan / 2f, eyeSleepY),
            end = Offset(centerX - eyeSpacing + sleepSpan / 2f, eyeSleepY),
            strokeWidth = dim * 0.014f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFF312E81),
            start = Offset(centerX + eyeSpacing - sleepSpan / 2f, eyeSleepY),
            end = Offset(centerX + eyeSpacing + sleepSpan / 2f, eyeSleepY),
            strokeWidth = dim * 0.014f,
            cap = StrokeCap.Round
        )
    } else {
        if (blinkScale <= 0.15f) {
            drawLine(
                color = Color(0xFF1E153A),
                start = Offset(centerX - eyeSpacing - eyeW, faceCenterY),
                end = Offset(centerX - eyeSpacing + eyeW, faceCenterY),
                strokeWidth = dim * 0.012f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFF1E153A),
                start = Offset(centerX + eyeSpacing - eyeW, faceCenterY),
                end = Offset(centerX + eyeSpacing + eyeW, faceCenterY),
                strokeWidth = dim * 0.012f,
                cap = StrokeCap.Round
            )
        } else {
            drawRoundRect(
                color = Color(0xFF1E153A),
                topLeft = Offset(centerX - eyeSpacing - eyeW, faceCenterY - eyeH / 2f),
                size = Size(eyeW * 2f, eyeH),
                cornerRadius = CornerRadius(eyeW, eyeW)
            )
            drawCircle(
                color = Color.White,
                radius = eyeW * 0.45f,
                center = Offset(centerX - eyeSpacing - eyeW * 0.25f, faceCenterY - eyeH * 0.22f)
            )

            drawRoundRect(
                color = Color(0xFF1E153A),
                topLeft = Offset(centerX + eyeSpacing - eyeW, faceCenterY - eyeH / 2f),
                size = Size(eyeW * 2f, eyeH),
                cornerRadius = CornerRadius(eyeW, eyeW)
            )
            drawCircle(
                color = Color.White,
                radius = eyeW * 0.45f,
                center = Offset(centerX + eyeSpacing - eyeW * 0.25f, faceCenterY - eyeH * 0.22f)
            )
        }
    }

    // 2. RÓŻOWE POLICZKI (BLUSH)
    val cheekDistance = dim * 0.145f
    val cheekY = faceCenterY + dim * 0.038f
    val cheekRadius = dim * 0.032f
    val cheekAlpha = if (state == LlumieState.JOYFUL || isPetting) 0.70f else 0.40f

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFB7185).copy(alpha = cheekAlpha), Color.Transparent),
            center = Offset(centerX - cheekDistance, cheekY),
            radius = cheekRadius
        ),
        radius = cheekRadius,
        center = Offset(centerX - cheekDistance, cheekY)
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFB7185).copy(alpha = cheekAlpha), Color.Transparent),
            center = Offset(centerX + cheekDistance, cheekY),
            radius = cheekRadius
        ),
        radius = cheekRadius,
        center = Offset(centerX + cheekDistance, cheekY)
    )

    // 3. NOSEK I PYSZCZEK
    val noseY = faceCenterY + dim * 0.016f
    drawCircle(
        color = Color(0xFF6D28D9),
        radius = dim * 0.011f,
        center = Offset(centerX, noseY)
    )

    paths.mouthPath.reset()
    val mouthY = noseY + dim * 0.010f
    val mw = dim * 0.018f
    val mh = dim * 0.009f

    if (awake && !isPetting && emotion == LlumieEmotion.SURPRISED) {
        drawOval(Color(0xFF4C1D95), Offset(centerX - dim * 0.012f, mouthY - dim * 0.002f), Size(dim * 0.024f, dim * 0.028f))
        drawOval(Color(0xFFF472B6), Offset(centerX - dim * 0.007f, mouthY + dim * 0.012f), Size(dim * 0.014f, dim * 0.010f))
        drawEmotionFx(paths.fxPath, centerX, faceCenterY, dim, state, emotion, isPetting, fxPhase)
        return
    }

    paths.mouthPath.moveTo(centerX - mw * 1.5f, mouthY)
    paths.mouthPath.quadraticTo(centerX - mw * 0.75f, mouthY + mh, centerX, mouthY)
    paths.mouthPath.quadraticTo(centerX + mw * 0.75f, mouthY + mh, centerX + mw * 1.5f, mouthY)

    drawPath(
        path = paths.mouthPath,
        color = Color(0xFF6D28D9),
        style = Stroke(width = dim * 0.008f, cap = StrokeCap.Round)
    )
    drawEmotionFx(paths.fxPath, centerX, faceCenterY, dim, state, emotion, isPetting, fxPhase)
}

private fun DrawScope.drawSleepingBlanket(
    paths: LlumiePathHolder,
    centerX: Float,
    bodyBottom: Float,
    bodyW: Float,
    dim: Float,
    droopProgress: Float
) {
    paths.slumberBlanketPath.reset()
    val blanketTopY = bodyBottom - dim * 0.16f * droopProgress
    val bw = bodyW * 0.52f * droopProgress

    paths.slumberBlanketPath.moveTo(centerX - bw, blanketTopY)
    paths.slumberBlanketPath.cubicTo(
        centerX - bw * 0.5f, blanketTopY - dim * 0.04f,
        centerX + bw * 0.5f, blanketTopY + dim * 0.02f,
        centerX + bw, blanketTopY
    )
    paths.slumberBlanketPath.cubicTo(
        centerX + bw * 0.8f, bodyBottom + dim * 0.02f,
        centerX - bw * 0.8f, bodyBottom + dim * 0.02f,
        centerX - bw, blanketTopY
    )
    paths.slumberBlanketPath.close()

    drawPath(
        path = paths.slumberBlanketPath,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xCC4C1D95), Color(0xEE1E153A)),
            startY = blanketTopY,
            endY = bodyBottom
        )
    )
}

// =============================================================================
// COMPATIBILITY OVERLOAD FOR EXISTING REPOSITORY CALLERS
// =============================================================================

/**
 * Backward compatibility wrapper for existing app screens.
 */
@Composable
fun LlumieCanvas(
    modifier: Modifier = Modifier,
    canvasSize: Dp = 190.dp,
    mood: LlumieMood = LlumieMood.HAPPY,
    cosmetic: LlumieCosmetic = LlumieCosmetic.NONE,
    cosmetics: Set<LlumieCosmetic> = if (cosmetic != LlumieCosmetic.NONE) setOf(cosmetic) else emptySet(),
    clothingColor: ClothingColor = ClothingColor.LAVENDER,
    level: Int = 1,
    streakDays: Int = 0,
    onTap: () -> Unit = {},
    onPet: () -> Unit = {}
) {
    val mappedState = when (mood) {
        LlumieMood.HAPPY, LlumieMood.PROUD -> LlumieState.JOYFUL
        LlumieMood.SLEEPY, LlumieMood.REFLECTIVE -> LlumieState.SLEEPING
        else -> LlumieState.IDLE
    }
    val mappedEmotion = when (mood) {
        LlumieMood.HAPPY -> LlumieEmotion.HAPPY
        LlumieMood.PROUD -> LlumieEmotion.PROUD
        LlumieMood.LOVING -> LlumieEmotion.LOVING
        LlumieMood.SLEEPY -> LlumieEmotion.SLEEPY
        LlumieMood.REFLECTIVE -> LlumieEmotion.CALM
    }

    LlumieMascot(
        state = mappedState,
        emotion = mappedEmotion,
        cosmetic = cosmetic,
        cosmetics = cosmetics,
        clothingColor = clothingColor,
        modifier = modifier.size(canvasSize),
        onTap = onTap,
        onPet = onPet
    )
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(name = "Llumie Mascot - All States", showBackground = true, backgroundColor = 0xFF090D16)
@Composable
fun LlumieMascotPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "IDLE (Sweter)", color = Color.White)
                    LlumieMascot(
                        state = LlumieState.IDLE,
                        cosmetics = setOf(LlumieCosmetic.WIZARD_HAT, LlumieCosmetic.SWEATER, LlumieCosmetic.COOL_SHADES),
                        clothingColor = ClothingColor.SKY_CYAN,
                        modifier = Modifier.size(110.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "JOYFUL", color = Color.White)
                    LlumieMascot(
                        state = LlumieState.JOYFUL,
                        cosmetics = setOf(LlumieCosmetic.BASEBALL_CAP, LlumieCosmetic.TSHIRT),
                        clothingColor = ClothingColor.ROSE,
                        modifier = Modifier.size(110.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "SLEEPING", color = Color.White)
                    LlumieMascot(
                        state = LlumieState.SLEEPING,
                        modifier = Modifier.size(110.dp)
                    )
                }
            }
        }
    }
}
