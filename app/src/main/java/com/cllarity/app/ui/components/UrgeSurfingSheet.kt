package com.cllarity.app.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cllarity.app.ui.theme.AccentLumen
import com.cllarity.app.ui.theme.AccentLumenGlow
import com.cllarity.app.ui.theme.AccentLumenSubtle
import com.cllarity.app.ui.theme.GlassBorderLumen
import com.cllarity.app.ui.theme.GlassBorderSubtle
import com.cllarity.app.ui.theme.MidnightBackground
import com.cllarity.app.ui.theme.MidnightSurfaceCard
import com.cllarity.app.ui.theme.MidnightSurfaceElevated
import com.cllarity.app.ui.theme.Motion
import com.cllarity.app.ui.theme.Radius
import com.cllarity.app.ui.theme.SemanticAlert
import com.cllarity.app.ui.theme.SemanticAlertBg
import com.cllarity.app.ui.theme.SemanticAlertBorder
import com.cllarity.app.ui.theme.TextPrimary
import com.cllarity.app.ui.theme.TextSecondary
import com.cllarity.app.ui.theme.TextTertiary
import com.cllarity.app.ui.theme.rememberHaptics
import com.cllarity.app.ui.theme.rememberSound
import kotlinx.coroutines.delay

enum class BreathingPhase(val label: String, val instruction: String, val durationSec: Int) {
    INHALE("WDECH", "Wdychaj powoli nosem, napełnij brzuch", 4),
    HOLD_IN("ZATRZYMAJ", "Poczuj przestrzeń i ciszę w ciele", 4),
    EXHALE("WYDECH", "Uwalniaj powietrze, fala opada", 4),
    HOLD_OUT("SPOKÓJ", "Pozostań w bezruchu, jesteś bezpieczny", 4)
}

/**
 * Zen Oasis SOS (10/10 Apple Design):
 * - Usunięto ścianę tekstu i przytłaczające formularze.
 * - Centralny, hipnotyzujący okrąg oddechu pudełkowego Box Breathing (4-4-4-4).
 * - Delikatne tętno haptyczne i kojący dźwięk misy przy każdej fazie.
 * - Szybkie nazwanie emocji (HALT) i zwięzła stoicka kotwica.
 */
@Composable
fun UrgeSurfingSheet(
    crisisLetter: String = "",
    onSaveCrisisLetter: (String) -> Unit = {},
    onResistSuccess: () -> Unit,
    onClose: () -> Unit
) {
    val haptics = rememberHaptics()
    val sound = rememberSound(true)
    val context = LocalContext.current

    var isBreathing by remember { mutableStateOf(true) }
    var currentPhase by remember { mutableStateOf(BreathingPhase.INHALE) }
    var secondsElapsed by remember { mutableIntStateOf(0) }
    var selectedTrigger by remember { mutableStateOf<String?>(null) }

    val triggers = listOf("Zmęczenie", "Stres", "Samotność", "Nuda", "Złość")

    // Pętla oddechowa Box Breathing (4-4-4-4) z haptycznym pulsem
    LaunchedEffect(isBreathing) {
        while (isBreathing) {
            for (phase in BreathingPhase.values()) {
                currentPhase = phase
                haptics.tick()
                sound.zenBreath()
                for (sec in 0 until phase.durationSec) {
                    delay(1000)
                    secondsElapsed++
                }
            }
        }
    }

    val breathScale = remember { Animatable(0.85f) }
    LaunchedEffect(currentPhase) {
        when (currentPhase) {
            BreathingPhase.INHALE -> breathScale.animateTo(1.22f, Motion.timed(3900, Motion.EaseBreath))
            BreathingPhase.HOLD_IN -> {}
            BreathingPhase.EXHALE -> breathScale.animateTo(0.85f, Motion.timed(3900, Motion.EaseBreath))
            BreathingPhase.HOLD_OUT -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 22.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Górny pasek: Tytuł + Dyskretna pigułka SOS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.Pill))
                    .background(SemanticAlertBg)
                    .border(1.dp, SemanticAlertBorder, RoundedCornerShape(Radius.Pill))
                    .clickable {
                        try {
                            val dial = Intent(Intent.ACTION_DIAL, Uri.parse("tel:116123"))
                            context.startActivity(dial)
                        } catch (_: Exception) {}
                    }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Icon(Icons.Rounded.Call, null, tint = SemanticAlert, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "116 123 (24/7)",
                    style = MaterialTheme.typography.labelSmall,
                    color = SemanticAlert,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = onClose) {
                Icon(Icons.Rounded.Close, "Zamknij", tint = TextSecondary, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // CENTRALNY OKRĄG ODDECHU (Apple Zen Breathing Core)
        Box(
            modifier = Modifier
                .size(230.dp),
            contentAlignment = Alignment.Center
        ) {
            // Zewnętrzna poświata Lumen
            Box(
                modifier = Modifier
                    .size(210.dp)
                    .scale(breathScale.value * 1.08f)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                AccentLumenGlow.copy(alpha = 0.40f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Główny organiczny dysk
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .scale(breathScale.value)
                    .clip(CircleShape)
                    .background(MidnightSurfaceElevated)
                    .border(
                        1.5.dp,
                        Brush.verticalGradient(
                            listOf(
                                AccentLumen.copy(alpha = 0.8f),
                                GlassBorderSubtle
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = currentPhase.label,
                        style = MaterialTheme.typography.titleLarge,
                        color = AccentLumen,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${secondsElapsed / 60}:%02d".format(secondsElapsed % 60),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Kojąca instrukcja pod oddechem
        Text(
            text = currentPhase.instruction,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Krok 2: Nazwij emocję (HALT) jednym dotknięciem
        Text(
            text = "Co wywołało tę falę?",
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            triggers.forEach { trig ->
                val isSel = selectedTrigger == trig
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(Radius.Badge))
                        .background(if (isSel) AccentLumenSubtle else MidnightSurfaceCard)
                        .border(
                            1.dp,
                            if (isSel) GlassBorderLumen else GlassBorderSubtle,
                            RoundedCornerShape(Radius.Badge)
                        )
                        .clickable {
                            haptics.tick()
                            selectedTrigger = trig
                        }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = trig,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSel) AccentLumen else TextSecondary,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        // KROK 3: Kapsuła Czasu / List do siebie w kryzysie
        var isLetterExpanded by remember { mutableStateOf(false) }
        var letterDraft by remember { mutableStateOf(crisisLetter) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.Tile))
                .background(MidnightSurfaceCard)
                .border(1.dp, GlassBorderSubtle, RoundedCornerShape(Radius.Tile))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isLetterExpanded = !isLetterExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Spa,
                        contentDescription = null,
                        tint = AccentLumen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (crisisLetter.isNotBlank()) "Kapsuła Czasu: List do siebie" else "Stwórz list motywacyjny na kryzys",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = if (isLetterExpanded) "Zwiń" else "Rozwiń",
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentLumen,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isLetterExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
                if (crisisLetter.isNotBlank()) {
                    Text(
                        text = "„$crisisLetter”",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        lineHeight = 18.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                } else {
                    Text(
                        text = "Zapisz powód, dla którego walczysz o wolność. Zobaczysz te słowa w najtrudniejszej chwili.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                androidx.compose.material3.OutlinedTextField(
                    value = letterDraft,
                    onValueChange = { letterDraft = it },
                    placeholder = { Text("Np. Pamiętaj jak źle czujesz się rano po ulegnięciu. Robisz to dla swojej rodziny i spokoju.", color = TextTertiary, style = MaterialTheme.typography.bodyMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(Radius.Tile),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentLumen,
                        unfocusedBorderColor = GlassBorderSubtle,
                        focusedContainerColor = MidnightSurfaceElevated,
                        unfocusedContainerColor = MidnightSurfaceElevated,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.Badge))
                        .background(AccentLumenSubtle)
                        .clickable {
                            haptics.success()
                            onSaveCrisisLetter(letterDraft)
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Zapisz w Kapsule",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentLumen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        // Przycisk Zwycięstwa
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.Chip))
                .background(AccentLumen)
                .clickable {
                    haptics.success()
                    sound.chord()
                    onResistSuccess()
                }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Check, null, tint = MidnightBackground, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Przetrwałem pokusę (+25 XP)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MidnightBackground,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
