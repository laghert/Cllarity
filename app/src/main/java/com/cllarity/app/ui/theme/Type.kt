package com.cllarity.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.cllarity.app.R

// ============================================================================
// KROJ: Inter (zmienny, osie wght + opsz) – najbliższy SF Pro krój na licencji OFL.
// Jeden plik res/font/inter_variable.ttf obsługuje wszystkie grubości.
//  • InterText    – opsz 14: szersze odstępy i otwarte formy dla tekstu ciągłego.
//  • InterDisplay – opsz 32: ciaśniejszy, bardziej "rzeźbiony" wariant do nagłówków
//    i wielkich liczb (odpowiednik SF Pro Display / Google Sans Display).
// Licencja: assets/licenses/Inter-OFL.txt
// ============================================================================

private val InterWeights = listOf(
    FontWeight.Light,
    FontWeight.Normal,
    FontWeight.Medium,
    FontWeight.SemiBold,
    FontWeight.Bold,
    FontWeight.ExtraBold,
    FontWeight.Black
)

private fun interFamily(opticalSize: Float) = FontFamily(
    InterWeights.map { weight ->
        Font(
            resId = R.font.inter_variable,
            weight = weight,
            variationSettings = FontVariation.Settings(
                FontVariation.weight(weight.weight),
                FontVariation.Setting("opsz", opticalSize)
            )
        )
    }
)

val InterText: FontFamily = interFamily(opticalSize = 14f)
val InterDisplay: FontFamily = interFamily(opticalSize = 32f)

/** Cyfry o stałej szerokości – liczniki nie "tańczą" przy zmianie wartości. */
private const val TabularNumbers = "tnum"

/**
 * Tracking wg metryk dynamicznych Inter (rsms.me/inter/dynmetrics):
 * tracking = -0.0223 + 0.185 · e^(-0.1745 · size) [em]
 * Duże stopnie są ciaśniejsze, małe lekko rozstrzelone – jak w SF Pro.
 */
private fun interTracking(sizeSp: Float): TextUnit =
    (-0.0223f + 0.185f * kotlin.math.exp(-0.1745f * sizeSp)).em

private fun textStyle(
    family: FontFamily,
    weight: FontWeight,
    size: Float,
    lineHeight: Float,
    tabular: Boolean = false
) = TextStyle(
    fontFamily = family,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = interTracking(size),
    fontFeatureSettings = if (tabular) TabularNumbers else null
)

/**
 * Skala typograficzna (Display / Headline / Title / Body / Label).
 * Najmniejszy stopień to 11 sp (jak caption2 w iOS) – wyjątkiem jest [LabelTiny].
 * Display i Headline mają cyfry tabelaryczne, bo niosą liczniki dni, XP i serii.
 */
val Typography = Typography(
    // Wielkie liczby bohaterów (np. 42 dni trzeźwości, rekordy)
    displayLarge = textStyle(InterDisplay, FontWeight.Bold, 44f, 48f, tabular = true),
    displayMedium = textStyle(InterDisplay, FontWeight.Bold, 34f, 40f, tabular = true),
    displaySmall = textStyle(InterDisplay, FontWeight.Bold, 28f, 34f, tabular = true),
    headlineLarge = textStyle(InterDisplay, FontWeight.SemiBold, 26f, 32f, tabular = true),
    headlineMedium = textStyle(InterDisplay, FontWeight.SemiBold, 22f, 28f, tabular = true),
    headlineSmall = textStyle(InterDisplay, FontWeight.SemiBold, 20f, 25f, tabular = true),
    titleLarge = textStyle(InterText, FontWeight.SemiBold, 18f, 24f),
    titleMedium = textStyle(InterText, FontWeight.SemiBold, 16f, 21f),
    titleSmall = textStyle(InterText, FontWeight.Medium, 14f, 19f),
    bodyLarge = textStyle(InterText, FontWeight.Normal, 16f, 23f),
    bodyMedium = textStyle(InterText, FontWeight.Normal, 14f, 20f),
    bodySmall = textStyle(InterText, FontWeight.Normal, 12f, 16f),
    labelLarge = textStyle(InterText, FontWeight.SemiBold, 14f, 18f),
    labelMedium = textStyle(InterText, FontWeight.Medium, 12f, 16f),
    labelSmall = textStyle(InterText, FontWeight.Medium, 11f, 14f)
)

/** Mikro-etykieta (10 sp) wyłącznie dla ciasnych plakietek w siatkach (np. kafelki garderoby). */
val LabelTiny: TextStyle = textStyle(InterText, FontWeight.SemiBold, 10f, 12f)

/** Wariant dowolnego stylu z cyframi tabelarycznymi – dla liczników w stylach Body/Label. */
val TextStyle.tabular: TextStyle
    get() = copy(fontFeatureSettings = TabularNumbers)
