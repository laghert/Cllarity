package com.cllarity.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.Healing
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.SmokingRooms
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.WineBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cllarity.app.model.Addiction
import com.cllarity.app.ui.components.AddictionDetailSheet
import com.cllarity.app.ui.components.AppleCard
import com.cllarity.app.ui.components.CllarityBottomSheet
import com.cllarity.app.ui.theme.AccentIndigo
import com.cllarity.app.ui.theme.AccentRose
import com.cllarity.app.ui.theme.AccentAmber
import com.cllarity.app.ui.theme.AccentEmerald
import com.cllarity.app.ui.theme.AccentLumen
import com.cllarity.app.ui.theme.AccentLumenSubtle
import com.cllarity.app.ui.theme.GlassBorder
import com.cllarity.app.ui.theme.GlassBorderSubtle
import com.cllarity.app.ui.theme.GlassBorderLumen
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import com.cllarity.app.ui.theme.Motion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddictionsScreen(
    addictions: List<Addiction>,
    isPrivacyMode: Boolean,
    isHapticsEnabled: Boolean = true,
    onTogglePrivacyMode: () -> Unit,
    onResetAddiction: (id: String, trigger: String?, note: String?) -> Unit,
    onAddAddictionClick: () -> Unit,
    onRemoveAddiction: (String) -> Unit,
    onOpenSOS: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var nowTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowTimestamp = System.currentTimeMillis()
            delay(1000L)
        }
    }

    var selectedForResetId by remember { mutableStateOf<String?>(null) }
    var selectedDetailId by remember { mutableStateOf<String?>(null) }

    val activeDetailAddiction = remember(addictions, selectedDetailId) {
        addictions.find { it.id == selectedDetailId }
    }
    val activeResetAddiction = remember(addictions, selectedForResetId) {
        addictions.find { it.id == selectedForResetId }
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    val totalMoney = addictions.sumOf { it.getMoneySaved(nowTimestamp) }
    val totalHours = addictions.sumOf { it.getHoursSaved(nowTimestamp) }
    val maxDays = addictions.maxOfOrNull { it.getDaysElapsed(nowTimestamp) } ?: 0L

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Overview Totals Card (if any tracked)
        if (addictions.isNotEmpty()) {
            item {
                AddictionsOverviewCard(
                    totalMoney = totalMoney,
                    totalHours = totalHours,
                    maxDays = maxDays
                )
            }
        }

        // SOS Emergency Urge Surfing Banner
        item {
            AppleCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0x1EF43F5E),
                borderColor = Color(0x44F43F5E),
                shape = RoundedCornerShape(Radius.Tile),
                onClick = onOpenSOS
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0x33F43F5E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Security,
                                contentDescription = null,
                                tint = Color(0xFFF43F5E),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Masz pokusę? Urge Surfing (SOS)",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "3-minutowy oddech pudełkowy uspokaja mózg",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = Color(0xFFF43F5E),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 2. Section Header + Privacy Toggle + Add Button
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (addictions.isEmpty()) "Zwalczane nałogi" else "Śledzone nałogi (${addictions.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Privacy mode eye icon
                    IconButton(onClick = onTogglePrivacyMode) {
                        Icon(
                            imageVector = if (isPrivacyMode) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = "Tryb dyskretny",
                            tint = if (isPrivacyMode) AccentIndigo else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Apple "+ Dodaj" Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(Radius.Tile))
                            .background(AccentIndigo)
                            .clickable { onAddAddictionClick() }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Dodaj",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 3. Empty State or Cards
        if (addictions.isEmpty()) {
            item {
                EmptyAddictionsCard(onAddClick = onAddAddictionClick)
            }
        } else {
            items(addictions, key = { it.id }) { addiction ->
                AnimatedVisibility(
                    visible = true,
                    enter = Motion.itemEnter()
                ) {
                    AddictionItemCard(
                        addiction = addiction,
                        now = nowTimestamp,
                        isPrivacyMode = isPrivacyMode,
                        onCardClick = {
                            if (isHapticsEnabled) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            selectedDetailId = addiction.id
                        },
                        onResetClick = {
                            if (isHapticsEnabled) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            selectedForResetId = addiction.id
                        },
                        onRemove = { onRemoveAddiction(addiction.id) }
                    )
                }
            }
        }
    }

    // Addiction Detail Sheet (Trend Chart, Relapse History & Reset Action)
    if (activeDetailAddiction != null) {
        CllarityBottomSheet(
            onDismissRequest = { selectedDetailId = null }
        ) {
            AddictionDetailSheet(
                addiction = activeDetailAddiction,
                isPrivacyMode = isPrivacyMode,
                onResetClick = {
                    val toResetId = activeDetailAddiction.id
                    selectedDetailId = null
                    selectedForResetId = toResetId
                },
                onClose = { selectedDetailId = null }
            )
        }
    }

    // Reset Sheet (odpowiedzialne, spokojne potwierdzenie z notatką i wyzwalaczem)
    if (activeResetAddiction != null) {
        CllarityBottomSheet(
            onDismissRequest = { selectedForResetId = null },
            sheetState = sheetState
        ) {
            ResetAddictionContent(
                addiction = activeResetAddiction,
                isPrivacyMode = isPrivacyMode,
                onConfirm = { trigger, note ->
                    val resetId = activeResetAddiction.id
                    coroutineScope.launch {
                        sheetState.hide()
                        selectedForResetId = null
                        onResetAddiction(resetId, trigger, note)
                    }
                },
                onCancel = {
                    coroutineScope.launch {
                        sheetState.hide()
                        selectedForResetId = null
                    }
                }
            )
        }
    }
}

@Composable
private fun EmptyAddictionsCard(onAddClick: () -> Unit) {
    AppleCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.Card),
        backgroundColor = MidnightSurfaceElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(AccentLumenSubtle)
                    .border(1.dp, GlassBorderLumen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Security,
                    contentDescription = null,
                    tint = AccentLumen,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Brak śledzonych nałogów",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Wybierz z gotowych szablonów lub dodaj własny cel. Dotknij kafelka, aby zobaczyć wykres i historię.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.Chip))
                    .background(AccentLumen)
                    .clickable { onAddClick() }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Wybierz cel wolności",
                    style = MaterialTheme.typography.labelMedium,
                    color = MidnightBackground,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * 2-tile layout for perfect balance, preventing text wrapping on any screen size.
 */
@Composable
private fun AddictionsOverviewCard(
    totalMoney: Double,
    totalHours: Double,
    maxDays: Long
) {
    AppleCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.Card),
        backgroundColor = MidnightSurfaceElevated,
        borderColor = GlassBorderLumen.copy(alpha = 0.35f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "MONOLIT WOLNOŚCI",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentLumen,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Twoja przestrzeń regeneracji",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (maxDays > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(Radius.Pill))
                            .background(AccentLumenSubtle)
                            .border(1.dp, GlassBorderLumen, RoundedCornerShape(Radius.Pill))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Rekord: $maxDays d.",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentLumen,
                            fontWeight = FontWeight.Bold,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2 równe, luksusowe segmenty w stylu Apple Health
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricSmallTile(
                    title = "Zaoszczędzone",
                    value = String.format(Locale.getDefault(), "%.0f zł", totalMoney),
                    icon = Icons.Rounded.AttachMoney,
                    color = AccentLumen,
                    modifier = Modifier.weight(1f)
                )

                MetricSmallTile(
                    title = "Odzyskany czas",
                    value = String.format(Locale.getDefault(), "%.1f h", totalHours),
                    icon = Icons.Rounded.AccessTime,
                    color = AccentLumen,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricSmallTile(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.Tile))
            .background(MidnightSurfaceCard)
            .border(1.dp, GlassBorderSubtle, RoundedCornerShape(Radius.Tile))
            .padding(14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    maxLines = 1,
                    softWrap = false
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AddictionItemCard(
    addiction: Addiction,
    now: Long,
    isPrivacyMode: Boolean,
    onCardClick: () -> Unit,
    onResetClick: () -> Unit,
    onRemove: () -> Unit
) {
    val days = addiction.getDaysElapsed(now)
    val hours = addiction.getHoursPart(now)
    val minutes = addiction.getMinutesPart(now)
    val money = addiction.getMoneySaved(now)
    val hoursSaved = addiction.getHoursSaved(now)

    val iconVector = getAddictionIcon(addiction.iconName)
    val displayName = addiction.getDisplayName(isPrivacyMode)

    AppleCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.Card),
        backgroundColor = MidnightSurfaceElevated,
        onClick = onCardClick // Stuknięcie w kartę otwiera szczegółowy arkusz z wykresem i resetem
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = AccentLumen,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = addiction.tagline,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary,
                            maxLines = 1
                        )
                    }
                }

                // Dyskretna strzałka informująca o przejściu do szczegółów
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "Szczegóły",
                    tint = TextTertiary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Spokojny, dostojny licznik wolności: Wielkie Dni jako bohater
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.Tile))
                    .background(MidnightSurfaceCard)
                    .border(1.dp, GlassBorderSubtle, RoundedCornerShape(Radius.Tile))
                    .padding(vertical = 14.dp, horizontal = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$days",
                        style = MaterialTheme.typography.displayMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (days == 1L) "dzień czystości" else "dni czystości",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                // Spokojny segment godzin i minut bez migających co sekundę liczb
                Text(
                    text = String.format(Locale.getDefault(), "%02d h %02d m", hours, minutes),
                    style = MaterialTheme.typography.labelLarge,
                    color = AccentLumen,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub-metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (addiction.costPerDayPln > 0) {
                    Text(
                        text = "Oszczędność: ${String.format(Locale.getDefault(), "%.0f zł", money)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                        text = "Odzyskany czas: ${String.format(Locale.getDefault(), "%.1f h", hoursSaved)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (addiction.relapseCount > 0) {
                    Text(
                        text = "Potknięcia: ${addiction.relapseCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                } else {
                    Text(
                        text = "Czysty rekord",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentLumen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeSegment(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary,
        )
    }
}

@Composable
private fun TimeSeparator() {
    Text(
        text = ":",
        style = MaterialTheme.typography.titleMedium,
        color = TextTertiary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun ResetAddictionContent(
    addiction: Addiction,
    isPrivacyMode: Boolean,
    onConfirm: (trigger: String?, note: String?) -> Unit,
    onCancel: () -> Unit
) {
    val triggers = listOf("Stres i napięcie", "Nuda / Rutyna", "Towarzystwo", "Trudne emocje", "Kompulsywny impuls", "Inne")
    var selectedTrigger by remember { mutableStateOf<String?>(null) }
    var noteText by remember { mutableStateOf("") }
    val displayName = addiction.getDisplayName(isPrivacyMode)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Zresetuj licznik: $displayName",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Zamknij",
                    tint = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.Tile))
                .background(Color(0x156366F1))
                .border(1.dp, AccentIndigo.copy(alpha = 0.25f), RoundedCornerShape(Radius.Tile))
                .padding(14.dp)
        ) {
            Text(
                text = "Potknięcie nie przekreśla Twoich dotychczasowych postępów. Każdy dzień, który był czysty, zmienił Twój mózg na lepsze. Llumie wciąż w Ciebie wierzy – zacznijmy od nowa z nową mądrością.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary.copy(alpha = 0.9f),
                lineHeight = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Co było głównym wyzwalaczem? (opcjonalnie)",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(triggers) { trig ->
                val isSel = selectedTrigger == trig
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.Chip))
                        .background(if (isSel) AccentIndigo else MidnightSurfaceCard)
                        .border(1.dp, if (isSel) AccentIndigo else GlassBorderSubtle, RoundedCornerShape(Radius.Chip))
                        .clickable { selectedTrigger = if (isSel) null else trig }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = trig,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSel) Color.White else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Krótka notatka / wniosek na przyszłość:",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            placeholder = { Text("Np. Następnym razem zamiast sięgać po nałóg, pójdę na spacer...", color = TextTertiary, style = MaterialTheme.typography.bodyMedium) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            shape = RoundedCornerShape(Radius.Tile),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentIndigo,
                unfocusedBorderColor = GlassBorder,
                focusedContainerColor = MidnightSurfaceCard,
                unfocusedContainerColor = MidnightSurfaceCard,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.Tile))
                .background(Color(0xFFDC2626))
                .clickable { onConfirm(selectedTrigger, noteText.ifBlank { null }) }
                .padding(vertical = 15.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.RestartAlt,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Zresetuj licznik i zacznij od nowa",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.Tile))
                .clickable { onCancel() }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Anuluj",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun getAddictionIcon(iconName: String): ImageVector = when (iconName) {
    "wine_bar" -> Icons.Rounded.WineBar
    "smoking_rooms" -> Icons.Rounded.SmokingRooms
    "fastfood" -> Icons.Rounded.Fastfood
    "healing" -> Icons.Rounded.Healing
    "visibility_off" -> Icons.Rounded.VisibilityOff
    else -> Icons.Rounded.Security
}
