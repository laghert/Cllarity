package com.cllarity.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.graphics.graphicsLayer
import com.cllarity.app.ui.theme.rememberHaptics
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import java.util.Calendar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cllarity.app.model.Habit
import com.cllarity.app.model.HabitCategory
import com.cllarity.app.ui.components.AppleCard
import com.cllarity.app.ui.components.BouncyCheckbox
import com.cllarity.app.ui.components.CllarityBottomSheet
import com.cllarity.app.ui.components.FlyingXpParticles
import com.cllarity.app.ui.components.HabitDetailSheet
import androidx.compose.ui.geometry.Offset
import com.cllarity.app.ui.theme.AccentLumen
import com.cllarity.app.ui.theme.AccentLumenSubtle
import com.cllarity.app.ui.theme.GlassBorder
import com.cllarity.app.ui.theme.GlassBorderSubtle
import com.cllarity.app.ui.theme.GlassBorderLumen
import com.cllarity.app.ui.theme.MidnightBackground
import com.cllarity.app.ui.theme.MidnightSurfaceCard
import com.cllarity.app.ui.theme.MidnightSurfaceElevated
import com.cllarity.app.ui.theme.Motion
import com.cllarity.app.ui.theme.Radius
import com.cllarity.app.ui.theme.TextPrimary
import com.cllarity.app.ui.theme.TextSecondary
import com.cllarity.app.ui.theme.TextTertiary
import com.cllarity.app.ui.theme.rememberSound
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    habits: List<Habit>,
    isHapticsEnabled: Boolean = true,
    isSoundEnabled: Boolean = true,
    onToggleHabit: (String) -> Unit,
    onIncrementHabit: (String) -> Unit = {},
    onDecrementHabit: (String) -> Unit = {},
    onAddHabitClick: () -> Unit,
    onRemoveHabit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sound = rememberSound(isSoundEnabled)
    var selectedCategory by remember { mutableStateOf<HabitCategory?>(null) }
    var selectedHabitIdForDetails by remember { mutableStateOf<String?>(null) }
    val activeDetailHabit = remember(habits, selectedHabitIdForDetails) {
        habits.find { it.id == selectedHabitIdForDetails }
    }
    val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    val todayIsoDay = remember {
        val c = Calendar.getInstance()
        val d = c.get(Calendar.DAY_OF_WEEK)
        if (d == Calendar.SUNDAY) 7 else d - 1
    }

    // Liczymy ukończenie tylko dla nawyków zaplanowanych na dzisiaj
    val scheduledTodayHabits = remember(habits, todayIsoDay) {
        habits.filter { it.isScheduledForDayOfWeek(todayIsoDay) }
    }
    val completedCount = scheduledTodayHabits.count { it.isCompletedToday }
    val totalCount = scheduledTodayHabits.size
    val progressRatio = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progressRatio,
        animationSpec = Motion.ProgressSpec,
        label = "progressRatio"
    )

    var particleTrigger by remember { mutableStateOf(0L) }
    var particleStartOffset by remember { mutableStateOf(Offset(500f, 1000f)) }

    val filteredHabits = if (selectedCategory == null) {
        habits
    } else {
        habits.filter { it.category == selectedCategory }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Progress Banner (only shown if habits exist)
        if (habits.isNotEmpty()) {
            item {
                HabitsProgressCard(
                    completedCount = completedCount,
                    totalCount = totalCount,
                    progress = animatedProgress
                )
            }

            // 2. Category Filter Pills
            item {
                CategoryFilterPills(
                    selectedCategory = selectedCategory,
                    onSelectCategory = { selectedCategory = it },
                    habits = habits
                )
            }
        }

        // 3. Section Header + Add Button
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (habits.isEmpty()) "Moje nawyki" else (selectedCategory?.displayName ?: "Aktywne nawyki (${filteredHabits.size})"),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (habits.isNotEmpty()) {
                        Text(
                            text = "${completedCount}/$totalCount ukończone • Dotknij dla menu",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentLumen
                        )
                    }
                }

                // Apple-style "+ Dodaj" Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.Chip))
                        .background(AccentLumen)
                        .clickable { onAddHabitClick() }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            tint = MidnightBackground,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Dodaj nawyk",
                            style = MaterialTheme.typography.labelMedium,
                            color = MidnightBackground,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 4. Empty State or Habit List
        if (habits.isEmpty()) {
            item {
                EmptyHabitsStateCard(onAddClick = onAddHabitClick)
            }
        } else {
            val todayHabits = filteredHabits.filter { it.isScheduledForDayOfWeek(todayIsoDay) }
            val otherHabits = filteredHabits.filter { !it.isScheduledForDayOfWeek(todayIsoDay) }

            // Sekcja A: Dzisiejsze nawyki
            if (todayHabits.isNotEmpty()) {
                items(todayHabits, key = { it.id }) { habit ->
                    HabitItemCard(
                        habit = habit,
                        isScheduledToday = true,
                        isHapticsEnabled = isHapticsEnabled,
                        onToggle = {
                            if (!habit.isCompletedToday) {
                                sound.woodblock()
                                particleStartOffset = Offset(500f, 800f)
                                particleTrigger = System.currentTimeMillis()
                            } else {
                                sound.tap()
                            }
                            onToggleHabit(habit.id)
                        },
                        onIncrement = {
                            if (habit.safeCurrentCountToday + 1 >= habit.safeTargetCount) {
                                sound.woodblock()
                                particleStartOffset = Offset(500f, 800f)
                                particleTrigger = System.currentTimeMillis()
                            } else {
                                sound.tap()
                            }
                            onIncrementHabit(habit.id)
                        },
                        onDecrement = {
                            sound.tap()
                            onDecrementHabit(habit.id)
                        },
                        onLongPress = {
                            sound.tap()
                            selectedHabitIdForDetails = habit.id
                        },
                        onRemove = { onRemoveHabit(habit.id) }
                    )
                }
            }

            // Sekcja B: Pozostałe dni (zwijana, stonowana, niezaśmiecająca wzroku)
            if (otherHabits.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    ) {
                        Text(
                            text = "Pozostałe dni (${otherHabits.size})",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextTertiary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                items(otherHabits, key = { it.id }) { habit ->
                    HabitItemCard(
                        habit = habit,
                        isScheduledToday = false,
                        isHapticsEnabled = isHapticsEnabled,
                        onToggle = { onToggleHabit(habit.id) },
                        onIncrement = { onIncrementHabit(habit.id) },
                        onDecrement = { onDecrementHabit(habit.id) },
                        onLongPress = {
                            sound.tap()
                            selectedHabitIdForDetails = habit.id
                        },
                        onRemove = { onRemoveHabit(habit.id) }
                    )
                }
            }
        }
    }

    if (particleTrigger > 0L) {
        FlyingXpParticles(
            trigger = particleTrigger,
            startOffset = particleStartOffset,
            targetOffset = Offset(500f, 120f),
            onFinished = { particleTrigger = 0L }
        )
    }
}
    // Habit Detail & Quick Actions Sheet (Triggered by Long-Press / Tap)
    if (activeDetailHabit != null) {
        CllarityBottomSheet(
            onDismissRequest = { selectedHabitIdForDetails = null },
            sheetState = detailSheetState
        ) {
            HabitDetailSheet(
                habit = activeDetailHabit,
                onToggleToday = {
                    onToggleHabit(activeDetailHabit.id)
                },
                onRemove = {
                    val removeId = activeDetailHabit.id
                    coroutineScope.launch {
                        detailSheetState.hide()
                        selectedHabitIdForDetails = null
                        onRemoveHabit(removeId)
                    }
                },
                onClose = {
                    coroutineScope.launch {
                        detailSheetState.hide()
                        selectedHabitIdForDetails = null
                    }
                }
            )
        }
    }
}

@Composable
private fun EmptyHabitsStateCard(onAddClick: () -> Unit) {
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
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = AccentLumen,
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Brak aktywnych nawyków",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Wybierz z gotowej bazy 15 nawyków lub stwórz własny. Przytrzymaj kafelek, aby zobaczyć statystyki i wykres.",
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
                    text = "Wybierz z katalogu",
                    style = MaterialTheme.typography.labelMedium,
                    color = MidnightBackground,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun HabitsProgressCard(
    completedCount: Int,
    totalCount: Int,
    progress: Float
) {
    AppleCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.Card),
        backgroundColor = MidnightSurfaceElevated
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Dzisiejsze nawyki",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$completedCount z $totalCount zrobionych",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    color = AccentLumen,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Czysty, spokojny pasek postępu w kolorze AccentLumen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(Radius.Pill))
                    .background(MidnightSurfaceCard)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progress.coerceIn(0.01f, 1f))
                        .height(6.dp)
                        .clip(RoundedCornerShape(Radius.Pill))
                        .background(AccentLumen)
                )
            }
        }
    }
}

@Composable
private fun CategoryFilterPills(
    selectedCategory: HabitCategory?,
    onSelectCategory: (HabitCategory?) -> Unit,
    habits: List<Habit>
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        item {
            FilterPill(
                label = "Wszystkie (${habits.size})",
                isSelected = selectedCategory == null,
                onClick = { onSelectCategory(null) }
            )
        }
        items(HabitCategory.values()) { category ->
            val count = habits.count { it.category == category }
            if (count > 0) {
                FilterPill(
                    label = "${category.displayName} ($count)",
                    icon = getCategoryIcon(category),
                    isSelected = selectedCategory == category,
                    onClick = { onSelectCategory(category) }
                )
            }
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    icon: ImageVector? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) MidnightSurfaceCard else MidnightSurfaceElevated
    val textColor = if (isSelected) TextPrimary else TextSecondary
    val borderColor = if (isSelected) GlassBorderLumen else GlassBorderSubtle

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.Chip))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(Radius.Chip))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) AccentLumen else TextSecondary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}


@Composable
private fun HabitItemCard(
    habit: Habit,
    isScheduledToday: Boolean,
    isHapticsEnabled: Boolean,
    onToggle: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onLongPress: () -> Unit,
    onRemove: () -> Unit
) {
    val haptics = rememberHaptics()
    var isPressed by remember { mutableStateOf(false) }

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.978f else 1.0f,
        animationSpec = Motion.PressSpec,
        label = "habitCardScale"
    )

    // Płynne, ciche przejście tła bez krzykliwych kolorów
    val cardBgColor by animateColorAsState(
        targetValue = when {
            !isScheduledToday -> MidnightSurfaceElevated.copy(alpha = 0.5f)
            habit.isCompletedToday -> MidnightSurfaceCard.copy(alpha = 0.5f)
            else -> MidnightSurfaceElevated
        },
        animationSpec = Motion.ColorMorph,
        label = "cardBgColor"
    )

    val cardBorderColor by animateColorAsState(
        targetValue = when {
            !isScheduledToday -> GlassBorderSubtle.copy(alpha = 0.3f)
            habit.isCompletedToday -> GlassBorderLumen.copy(alpha = 0.3f)
            else -> GlassBorderSubtle
        },
        animationSpec = Motion.ColorMorph,
        label = "cardBorderColor"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .pointerInput(habit.id, habit.isCompletedToday, isScheduledToday) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        if (isHapticsEnabled) {
                            haptics.tick()
                        }
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = {
                        // Stuknięcie w treść karty otwiera szczegóły
                        onLongPress()
                    },
                    onLongPress = {
                        if (isHapticsEnabled) {
                            haptics.heavyClick()
                        }
                        onLongPress()
                    }
                )
            },
        shape = RoundedCornerShape(Radius.Card),
        color = cardBgColor,
        border = BorderStroke(1.dp, cardBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Spokojna, monochromatyczna ikona kategorii
                Icon(
                    imageVector = getCategoryIcon(habit.category),
                    contentDescription = null,
                    tint = if (!isScheduledToday) TextTertiary.copy(alpha = 0.5f)
                    else if (habit.isCompletedToday) TextTertiary
                    else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(14.dp))

                // Habit Details
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = when {
                                !isScheduledToday -> TextSecondary.copy(alpha = 0.7f)
                                habit.isCompletedToday -> TextSecondary
                                else -> TextPrimary
                            },
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textDecoration = if (habit.isCompletedToday) TextDecoration.LineThrough else TextDecoration.None,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (!isScheduledToday) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(Radius.Badge))
                                    .background(MidnightSurfaceCard)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Wolne dziś",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextTertiary,
                                )
                            }
                        }
                    }


                    // Czysta, zrównoważona linia celu i nagrody XP (bez zaśmiecającej passy na liście)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (habit.isCountBased) {
                            Text(
                                text = "${habit.safeCurrentCountToday}/${habit.safeTargetCount} ${habit.safeUnit}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (habit.isCompletedToday) AccentLumen else TextPrimary,
                                fontWeight = if (habit.isCompletedToday) FontWeight.Bold else FontWeight.SemiBold
                            )
                            Text(text = "•", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                        }

                        Text(
                            text = "+${habit.xpReward} XP",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentLumen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // --- PRAWA STRONA KARTY: STEPPER Z PLUSIKIEM vs APPLE BOUNCY CHECKBOX ---
                if (habit.isCountBased) {
                    // Apple Tactile Stepper (+ / -)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (habit.safeCurrentCountToday > 0 && !habit.isCompletedToday) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MidnightSurfaceCard)
                                    .border(1.dp, GlassBorderSubtle, CircleShape)
                                    .clickable {
                                        if (isHapticsEnabled) haptics.tick()
                                        onDecrement()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Remove,
                                    contentDescription = "Odejmij postęp",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        // Przycisk Plusika (lub Checkmark jeśli osiągnięto cel)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (habit.isCompletedToday) AccentLumen else AccentLumenSubtle)
                                .border(
                                    1.dp,
                                    if (habit.isCompletedToday) AccentLumen else GlassBorderLumen,
                                    CircleShape
                                )
                                .clickable {
                                    if (isHapticsEnabled) {
                                        if (!habit.isCompletedToday) haptics.success() else haptics.click()
                                    }
                                    if (habit.isCompletedToday) {
                                        onToggle() // reset / cofnij
                                    } else {
                                        onIncrement() // +1
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (habit.isCompletedToday) Icons.Rounded.Check else Icons.Rounded.Add,
                                contentDescription = if (habit.isCompletedToday) "Ukończono" else "Dodaj postęp",
                                tint = if (habit.isCompletedToday) MidnightBackground else AccentLumen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    // Standardowy Apple Tactile Bouncy Checkbox dla nawyków 1x
                    BouncyCheckbox(
                        checked = habit.isCompletedToday,
                        onCheckedChange = { onToggle() },
                        activeColor = AccentLumen,
                        isHapticsEnabled = isHapticsEnabled
                    )
                }
            }

            // Pasek miniaturowego postępu pod spodem dla nawyków wielokrotnych
            if (habit.isCountBased && habit.safeCurrentCountToday > 0 && !habit.isCompletedToday) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(Radius.Pill))
                        .background(MidnightSurfaceCard)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = habit.todayProgressRatio)
                            .height(3.dp)
                            .clip(RoundedCornerShape(Radius.Pill))
                            .background(AccentLumen)
                    )
                }
            }
        }
    }
}
private fun getCategoryIcon(category: HabitCategory): ImageVector = when (category) {
    HabitCategory.MORNING -> Icons.Rounded.WbSunny
    HabitCategory.BODY_HEALTH -> Icons.Rounded.Favorite
    HabitCategory.FOCUS_MIND -> Icons.Rounded.Psychology
    HabitCategory.EVENING -> Icons.Rounded.NightsStay
}
