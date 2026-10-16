package com.cllarity.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cllarity.app.model.ClothingColor
import com.cllarity.app.model.LlumieCosmetic
import com.cllarity.app.model.LlumieMood
import com.cllarity.app.ui.theme.AccentLumen
import com.cllarity.app.ui.theme.LabelTiny
import com.cllarity.app.ui.theme.AccentLumenGlow
import com.cllarity.app.ui.theme.AccentLumenSubtle
import com.cllarity.app.ui.theme.GlassBorderLumen
import com.cllarity.app.ui.theme.GlassBorderSubtle
import com.cllarity.app.ui.theme.MidnightBackground
import com.cllarity.app.ui.theme.MidnightSurfaceCard
import com.cllarity.app.ui.theme.MidnightSurfaceElevated
import com.cllarity.app.ui.theme.Radius
import com.cllarity.app.ui.theme.SemanticAlert
import com.cllarity.app.ui.theme.SemanticAlertBg
import com.cllarity.app.ui.theme.SemanticAlertBorder
import com.cllarity.app.ui.theme.TextPrimary
import com.cllarity.app.ui.theme.TextSecondary
import com.cllarity.app.ui.theme.TextTertiary
import com.cllarity.app.ui.theme.rememberHaptics
import com.cllarity.app.ui.theme.rememberSound
import com.cllarity.app.ui.theme.Motion

data class OnboardingStep(
    val title: String,
    val subtitle: String,
    val highlightBadge: String,
    val description: String,
    val tips: List<String>,
    val icon: ImageVector,
    val previewType: OnboardingPreviewType
)

enum class OnboardingPreviewType {
    LLUMIE_HERO,
    HABITS_FLOW,
    ADDICTIONS_MONOLITH,
    SOS_ZEN,
    READY_GO
}

/**
 * Luksusowy, Apple-style ekran powitalny i przewodnik początkowy (Setup Guide).
 * Uruchamia się automatycznie przy pierwszym włączeniu aplikacji lub na życzenie z ekranu Debug.
 */
@Composable
fun OnboardingGuideOverlay(
    onComplete: () -> Unit
) {
    val haptics = rememberHaptics()
    val sound = rememberSound(true)
    var stepIndex by remember { mutableIntStateOf(0) }

    val steps = remember {
        listOf(
            OnboardingStep(
                title = "Witaj w Cllarity",
                subtitle = "Twoja prywatna przestrzeń skupienia i regeneracji",
                highlightBadge = "Światło Llumie",
                description = "Poznaj Llumie – Twojego towarzysza budowania dyscypliny. Llumie rozświetla się wraz z Twoimi sukcesami i gaśnie, gdy zapominasz o swoich wartościach.",
                tips = listOf(
                    "Dotknij lub pogłaszcz Llumie, aby usłyszeć stoicką myśl",
                    "Każdy odhaczony cel daje punkty XP i nowe akcesoria",
                    "Aplikacja działa w 100% lokalnie i bez śledzenia"
                ),
                icon = Icons.Rounded.Spa,
                previewType = OnboardingPreviewType.LLUMIE_HERO
            ),
            OnboardingStep(
                title = "Nawyki i Stepper",
                subtitle = "Buduj nawyki w swoim własnym rytmie",
                highlightBadge = "Dzisiaj vs Pozostałe dni",
                description = "Koniec z chaosem. Nawyki są podzielone na to, co masz zrobić DZIŚ oraz na pozostałe dni tygodnia, by nic Cię nie rozpraszało.",
                tips = listOf(
                    "Nawyki pojedyncze odhaczaj jednym stuknięciem w kółko",
                    "Dla wody lub powtórzeń używaj haptycznego steppera [+]",
                    "W kreatorze wybierz dokładne dni tygodnia (np. Pn-Pt)"
                ),
                icon = Icons.Rounded.Check,
                previewType = OnboardingPreviewType.HABITS_FLOW
            ),
            OnboardingStep(
                title = "Monolit Wolności",
                subtitle = "Pokonaj nałogi i odzyskaj swój czas",
                highlightBadge = "Czas czystości",
                description = "Licznik wolności odmierza każdą sekundę Twojej nowej drogi. Śledź zaoszczędzone pieniądze oraz godziny życia, które odzyskałeś.",
                tips = listOf(
                    "Dotknij karty nałogu, by zobaczyć wielki zegar wolności",
                    "Jeśli ulegniesz pokusie – zresetuj z notatką i wyciągnij lekcję",
                    "Tryb dyskretny ukrywa intymne nazwy nałogów w miejscach publicznych"
                ),
                icon = Icons.Rounded.Security,
                previewType = OnboardingPreviewType.ADDICTIONS_MONOLITH
            ),
            OnboardingStep(
                title = "Urge Surfing (SOS)",
                subtitle = "Przeczekaj falę pokusy z oddechem pudełkowym",
                highlightBadge = "Oaza spokoju 4-4-4-4",
                description = "Gdy pojawi się silna chęć powrotu do nałogu, nie walcz z nią. Fala dopaminy trwa kilka minut. Włącz SOS i oddychaj razem ze złotym okręgiem.",
                tips = listOf(
                    "Wdech (4s) → Pauza (4s) → Wydech (4s) → Spokój (4s)",
                    "W kryzysie masz pod ręką bezpłatne telefony zaufania 24/7",
                    "Każde odparcie pokusy na fali daje +25 XP i wzmacnia Twój mózg"
                ),
                icon = Icons.Rounded.LightMode,
                previewType = OnboardingPreviewType.SOS_ZEN
            ),
            OnboardingStep(
                title = "Jesteś gotowy",
                subtitle = "Dziś zaczyna się Twoja nowa droga",
                highlightBadge = "Zasada 24 godzin",
                description = "Nie obiecuj sobie wieczności. Złóż Codzienną Deklarację i bądź wierny sobie przez najbliższe 24 godziny. Krok po kroku.",
                tips = listOf(
                    "Złóż poranną deklarację na ekranie głównym",
                    "W Ustawieniach włącz blokadę biometryczną i dźwięki Zen",
                    "Llumie czuwa przy Tobie. Powodzenia!"
                ),
                icon = Icons.Rounded.AutoAwesome,
                previewType = OnboardingPreviewType.READY_GO
            )
        )
    }

    val currentStep = steps[stepIndex]
    val isLastStep = stepIndex == steps.size - 1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBackground)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. TOP BAR ONBOARDINGU: Pomiń + Kroki
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Wskaźnik kroków w stylu Apple (Dashes)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    steps.forEachIndexed { i, _ ->
                        val active = i == stepIndex
                        val past = i < stepIndex
                        Box(
                            modifier = Modifier
                                .height(4.dp)
                                .width(if (active) 26.dp else 12.dp)
                                .clip(RoundedCornerShape(Radius.Pill))
                                .background(
                                    when {
                                        active -> AccentLumen
                                        past -> AccentLumen.copy(alpha = 0.5f)
                                        else -> MidnightSurfaceCard
                                    }
                                )
                        )
                    }
                }

                Text(
                    text = "Pomiń",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextTertiary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.Badge))
                        .clickable {
                            sound.tap()
                            onComplete()
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. CENTRALNA SCENA POKAZOWA (Dynamic Step Content)
            AnimatedContent(
                targetState = stepIndex,
                transitionSpec = { Motion.sharedAxisX(forward = targetState > initialState) },
                label = "onboardingStepTransition",
                modifier = Modifier.weight(1f)
            ) { idx ->
                val step = steps[idx]
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Pokaźna, organiczna makieta Apple
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .clip(RoundedCornerShape(Radius.Card))
                            .background(MidnightSurfaceElevated)
                            .border(
                                BorderStroke(
                                    1.dp,
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.White.copy(alpha = 0.20f),
                                            Color.White.copy(alpha = 0.04f)
                                        )
                                    )
                                ),
                                RoundedCornerShape(Radius.Card)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        OnboardingPreviewGraphic(step.previewType)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Etykieta wyróżnika
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(Radius.Pill))
                            .background(AccentLumenSubtle)
                            .border(1.dp, GlassBorderLumen, RoundedCornerShape(Radius.Pill))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = step.highlightBadge,
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentLumen,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = step.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3 kluczowe wskazówki w ramce
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radius.Tile))
                            .background(MidnightSurfaceCard)
                            .border(1.dp, GlassBorderSubtle, RoundedCornerShape(Radius.Tile))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        step.tips.forEach { tip ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(AccentLumen)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = tip,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. DOLNY PRZYCISK DALEJ / START
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.Chip))
                    .background(AccentLumen)
                    .clickable {
                        if (isLastStep) {
                            haptics.success()
                            sound.chord()
                            onComplete()
                        } else {
                            haptics.click()
                            sound.tap()
                            stepIndex++
                        }
                    }
                    .padding(vertical = 15.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isLastStep) "Rozpocznij z Llumie" else "Dalej",
                        style = MaterialTheme.typography.labelLarge,
                        color = MidnightBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (isLastStep) Icons.Rounded.Check else Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = MidnightBackground,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingPreviewGraphic(type: OnboardingPreviewType) {
    when (type) {
        OnboardingPreviewType.LLUMIE_HERO -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(115.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(AccentLumenGlow, Color.Transparent)
                                )
                            )
                    )
                    LlumieCanvas(
                        canvasSize = 110.dp,
                        mood = LlumieMood.HAPPY,
                        cosmetics = emptySet(),
                        clothingColor = ClothingColor.LAVENDER,
                        level = 1,
                        streakDays = 1
                    )
                }
            }
        }
        OnboardingPreviewType.HABITS_FLOW -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Makieta nawyku 1x
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.Chip))
                        .background(MidnightSurfaceCard)
                        .border(1.dp, GlassBorderLumen.copy(alpha = 0.5f), RoundedCornerShape(Radius.Chip))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Szklanka wody", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        Text("+10 XP", style = MaterialTheme.typography.labelSmall, color = AccentLumen)
                    }
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(AccentLumen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Check, null, tint = MidnightBackground, modifier = Modifier.size(15.dp))
                    }
                }

                // Makieta steppera
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.Chip))
                        .background(MidnightSurfaceCard)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("10 000 kroków", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        Text("5/10 tys. • +30 XP", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MidnightSurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("-", color = TextSecondary, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(AccentLumenSubtle)
                                .border(1.dp, GlassBorderLumen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", color = AccentLumen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        OnboardingPreviewType.ADDICTIONS_MONOLITH -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.85f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "CZAS WOLNOŚCI",
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentLumen,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("14", style = MaterialTheme.typography.displayMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("dni czystości", style = MaterialTheme.typography.titleSmall, color = TextSecondary, modifier = Modifier.padding(bottom = 6.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(Radius.Chip))
                            .background(MidnightSurfaceCard)
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("280 zł zaoszczędzone", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(Radius.Chip))
                            .background(MidnightSurfaceCard)
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("18.5 h odzyskane", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }
            }
        }
        OnboardingPreviewType.SOS_ZEN -> {
            Box(
                modifier = Modifier.size(130.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(125.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(AccentLumenGlow.copy(alpha = 0.5f), Color.Transparent)
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(MidnightSurfaceCard)
                        .border(1.5.dp, GlassBorderLumen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("WDECH", style = MaterialTheme.typography.labelMedium, color = AccentLumen, fontWeight = FontWeight.Bold)
                        Text("4-4-4-4", style = LabelTiny, color = TextTertiary)
                    }
                }
            }
        }
        OnboardingPreviewType.READY_GO -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(AccentLumenSubtle)
                        .border(1.dp, GlassBorderLumen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = AccentLumen,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Jedna decyzja na 24h",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Dyscyplina to najwyższa forma troski o siebie",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
        }
    }
}
