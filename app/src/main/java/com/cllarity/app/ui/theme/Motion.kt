package com.cllarity.app.ui.theme

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset

/**
 * Jeden system ruchu dla całej aplikacji – hybryda iOS (sprężyny bez sztywnych czasów)
 * i Material 3 Expressive (podział na ruch przestrzenny i efekty).
 *
 * • SPATIAL – pozycja, rozmiar, skala. Lekki overshoot daje "fizyczność".
 * • EFFECTS – alpha i kolor. Zawsze krytycznie tłumione (bez odbicia – kolor nie może "przestrzelić").
 * • TIMED   – tylko tam, gdzie liczy się przewidywalny czas (rysowanie wykresu, oddech SOS).
 *
 * Zasada: w kodzie UI nie wpisujemy już surowych `spring(...)` ani `tween(...)`.
 * Wyjątek: proceduralna animacja postaci Llumie (LlumieCanvas) ma własną "osobowość".
 */
object Motion {

    // ------------------------------------------------------------------------
    // SPATIAL (stiffness ≈ (2π / response)²; response iOS 0.3s ≈ 440, 0.5s ≈ 160)
    // ------------------------------------------------------------------------

    /** Mikrointerakcje: wciśnięcie, przełącznik, odznaka. ~150 ms. */
    fun <T> spatialFast(): SpringSpec<T> = spring(dampingRatio = 0.85f, stiffness = 800f)

    /** Domyślny ruch elementów: pigułki, przesunięcia treści, wejścia. ~300 ms. */
    fun <T> spatialDefault(): SpringSpec<T> = spring(dampingRatio = 0.85f, stiffness = 380f)

    /** Duże powierzchnie i pełnoekranowe nakładki. ~450 ms. */
    fun <T> spatialSlow(): SpringSpec<T> = spring(dampingRatio = 0.9f, stiffness = 200f)

    /** Momenty radości: ukończenie, awans, nagroda. Wyraźne, ale krótkie odbicie. */
    fun <T> spatialBouncy(): SpringSpec<T> = spring(dampingRatio = 0.62f, stiffness = 380f)

    // ------------------------------------------------------------------------
    // EFFECTS (dampingRatio = 1 → bez odbicia)
    // ------------------------------------------------------------------------

    fun <T> effectsFast(): SpringSpec<T> = spring(dampingRatio = 1f, stiffness = 3800f)
    fun <T> effectsDefault(): SpringSpec<T> = spring(dampingRatio = 1f, stiffness = 1600f)
    fun <T> effectsSlow(): SpringSpec<T> = spring(dampingRatio = 1f, stiffness = 800f)

    // ------------------------------------------------------------------------
    // TIMED
    // ------------------------------------------------------------------------

    /** Krzywa standardowa (M3 standard ≈ iOS easeOut) – większość animacji czasowych. */
    val EaseStandard: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    /** Wejścia: szybki start, bardzo długie, miękkie wyhamowanie (M3 emphasized decelerate). */
    val EaseDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

    /** Wyjścia: element "odpływa" z rosnącą prędkością. */
    val EaseAccelerate: Easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    /** Symetryczna sinusoida – oddech, pulsowanie, pętle ambientowe. */
    val EaseBreath: Easing = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)

    const val DurationShort = 200
    const val DurationMedium = 350
    const val DurationLong = 500
    const val DurationExtraLong = 800

    fun <T> timed(
        durationMillis: Int = DurationMedium,
        easing: Easing = EaseStandard,
        delayMillis: Int = 0
    ): TweenSpec<T> = tween(durationMillis = durationMillis, delayMillis = delayMillis, easing = easing)

    // ------------------------------------------------------------------------
    // SEMANTYCZNE ALIASY (czytelność w miejscu użycia)
    // ------------------------------------------------------------------------

    /** Reakcja kafelka/przycisku na dotyk. */
    val PressSpec: SpringSpec<Float> = spatialFast()

    /** Szybujące wskaźniki (pigułka tab baru, zakładki kategorii). */
    val GlidingPillSpec: SpringSpec<Dp> = spatialDefault()
    val GlidingPillFractionSpec: SpringSpec<Float> = spatialDefault()

    /** Przenikanie treści (zmiana tekstu, przełączenie zakładek). */
    val ContentTransitionSpec: SpringSpec<Float> = effectsDefault()

    /** Ukończenie checkboxa i inne mikro-nagrody. */
    val CheckboxSpec: SpringSpec<Float> = spatialBouncy()

    /** Zmiany kolorów i tonów powierzchni. */
    val ColorMorph: SpringSpec<Color> = effectsDefault()

    /** Postęp (paski, pierścienie) – płynnie, bez przestrzelenia ponad 100%. */
    val ProgressSpec: SpringSpec<Float> = spring(dampingRatio = 1f, stiffness = 200f)

    // ------------------------------------------------------------------------
    // GOTOWE PRZEJŚCIA
    // ------------------------------------------------------------------------

    /**
     * Fade through (Material) – dla równorzędnych miejsc docelowych bez relacji
     * przestrzennej: zakładki dolnej nawigacji, segmenty w arkuszu, zmiana cytatu.
     * Nowa treść lekko "wyrasta" (0.96 → 1), stara szybko gaśnie.
     */
    fun fadeThrough(): ContentTransform =
        (fadeIn(effectsDefault()) + scaleIn(spatialDefault(), initialScale = 0.96f)) togetherWith
            fadeOut(effectsFast())

    /** Przenikanie bez ruchu – dla zmieniającego się tekstu w miejscu. */
    fun crossfade(): ContentTransform = fadeIn(effectsDefault()) togetherWith fadeOut(effectsFast())

    /**
     * Shared axis X – kroki i kategorie ułożone w kolejności (onboarding, garderoba).
     * [forward] = true, gdy przechodzimy do elementu po prawej.
     */
    fun sharedAxisX(forward: Boolean): ContentTransform {
        val direction = if (forward) 1 else -1
        val spec = spatialDefault<IntOffset>()
        return (slideInHorizontally(spec) { width -> direction * width / 4 } + fadeIn(effectsDefault())) togetherWith
            (slideOutHorizontally(spec) { width -> -direction * width / 4 } + fadeOut(effectsFast()))
    }

    /** Wejście elementu listy/karty: delikatny podjazd od dołu z rozjaśnieniem. */
    fun itemEnter(): EnterTransition =
        fadeIn(effectsDefault()) + slideInVertically(spatialDefault()) { height -> height / 6 }

    /** Wejście nakładki pełnoekranowej. */
    fun overlayEnter(): EnterTransition = fadeIn(effectsSlow())

    /** Wyjście nakładki pełnoekranowej. */
    fun overlayExit(): ExitTransition = fadeOut(effectsDefault())
}
