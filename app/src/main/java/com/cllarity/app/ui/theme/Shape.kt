package com.cllarity.app.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Jedyna skala zaokrągleń w aplikacji. W kodzie UI używamy wyłącznie tych tokenów –
 * żadnych surowych `RoundedCornerShape(N.dp)`.
 *
 *  Badge 8 · Chip 12 · Tile 16 · Card 22 · Sheet 28 · Pill (pełne zaokrąglenie)
 *
 * Paski postępu i uchwyty zawsze używają [Radius.Pill], dzięki czemu końce
 * pozostają okrągłe niezależnie od wysokości paska.
 */
object Radius {
    val Badge = 8.dp
    val Chip = 12.dp
    val Tile = 16.dp
    val Card = 22.dp
    val Sheet = 28.dp
    val Pill = 999.dp
    val Circle = CircleShape
}

/** Mapowanie tokenów na role Material 3 (pola tekstowe, chipy, dialogi, arkusze). */
val CllarityShapes = Shapes(
    extraSmall = RoundedCornerShape(Radius.Badge),
    small = RoundedCornerShape(Radius.Chip),
    medium = RoundedCornerShape(Radius.Tile),
    large = RoundedCornerShape(Radius.Card),
    extraLarge = RoundedCornerShape(Radius.Sheet)
)
