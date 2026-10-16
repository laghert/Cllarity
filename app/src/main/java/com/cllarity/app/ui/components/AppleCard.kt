package com.cllarity.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cllarity.app.ui.theme.GlassBorderSubtle
import com.cllarity.app.ui.theme.MidnightSurfaceElevated
import com.cllarity.app.ui.theme.Motion
import com.cllarity.app.ui.theme.Radius
import com.cllarity.app.ui.theme.rememberHaptics

/**
 * Podstawowa jednostka powierzchni w stylu Apple (iOS Tonal Surface).
 * Głębia budowana przez subtelne zróżnicowanie tonów (MidnightSurfaceElevated)
 * z ledwie widocznym, dyskretnym światłem na krawędzi (GlassBorderSubtle),
 * bez ciężkich, sztucznych plastikowych obrysów.
 */
@Composable
fun AppleCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(Radius.Card),
    backgroundColor: Color = MidnightSurfaceElevated,
    borderColor: Color = GlassBorderSubtle,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val haptics = rememberHaptics()
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.976f else 1.0f,
        animationSpec = Motion.PressSpec,
        label = "cardScale"
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .then(
                if (onClick != null) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                haptics.tick()
                                tryAwaitRelease()
                                isPressed = false
                            },
                            onTap = {
                                haptics.click()
                                onClick()
                            }
                        )
                    }
                } else Modifier
            ),
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Box(content = content)
    }
}
