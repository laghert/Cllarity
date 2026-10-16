package com.cllarity.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cllarity.app.model.Habit
import com.cllarity.app.model.HabitCategory
import com.cllarity.app.model.HabitDifficulty
import com.cllarity.app.model.HabitFrequency
import com.cllarity.app.ui.theme.AccentLumen
import com.cllarity.app.ui.theme.AccentLumenSubtle
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
import com.cllarity.app.ui.theme.rememberHaptics

private enum class HabitSheetTab(val title: String) {
    TEMPLATES("Baza gotowych"),
    CUSTOM("Własny cel")
}

@Composable
fun AddHabitSheet(
    allHabits: List<Habit>,
    onActivateHabit: (String) -> Unit,
    onDeactivateHabit: (String) -> Unit,
    onCreateCustomHabit: (
        name: String,
        desc: String,
        cat: HabitCategory,
        diff: HabitDifficulty,
        freq: HabitFrequency,
        targetCount: Int,
        unit: String,
        scheduledDays: Set<Int>
    ) -> Unit,
    onClose: () -> Unit
) {
    var currentTab by remember { mutableStateOf(HabitSheetTab.TEMPLATES) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 22.dp, vertical = 8.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Nowy nawyk",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Buduj dyscyplinę w swoim tempie",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Zamknij",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Segmented Switch (Apple pill)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.Pill))
                .background(MidnightSurfaceCard)
                .border(1.dp, GlassBorderSubtle, RoundedCornerShape(Radius.Pill))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            HabitSheetTab.values().forEach { tab ->
                val isSel = tab == currentTab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(Radius.Pill))
                        .background(if (isSel) MidnightSurfaceElevated else Color.Transparent)
                        .border(
                            1.dp,
                            if (isSel) GlassBorderLumen else Color.Transparent,
                            RoundedCornerShape(Radius.Pill)
                        )
                        .clickable { currentTab = tab }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSel) TextPrimary else TextSecondary,
                        fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedContent(
            targetState = currentTab,
            transitionSpec = { Motion.fadeThrough() },
            label = "HabitSheetTabs"
        ) { tab ->
            when (tab) {
                HabitSheetTab.TEMPLATES -> TemplatesTabContent(
                    habits = allHabits,
                    onToggleActive = { habit ->
                        if (habit.isActive) {
                            onDeactivateHabit(habit.id)
                        } else {
                            onActivateHabit(habit.id)
                        }
                    }
                )
                HabitSheetTab.CUSTOM -> AdvancedCustomHabitContent(
                    onCreate = { name, desc, cat, diff, freq, target, unit, days ->
                        onCreateCustomHabit(name, desc, cat, diff, freq, target, unit, days)
                        onClose()
                    }
                )
            }
        }
    }
}

@Composable
private fun TemplatesTabContent(
    habits: List<Habit>,
    onToggleActive: (Habit) -> Unit
) {
    val haptics = rememberHaptics()
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(460.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        items(habits, key = { it.id }) { habit ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.Tile))
                    .background(MidnightSurfaceCard)
                    .border(
                        1.dp,
                        if (habit.isActive) GlassBorderLumen else GlassBorderSubtle,
                        RoundedCornerShape(Radius.Tile)
                    )
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = habit.category.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary
                            )
                            Text(text = "•", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                            if (habit.isCountBased) {
                                Text(
                                    text = "Cel: ${habit.safeTargetCount} ${habit.safeUnit}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AccentLumen,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(text = "•", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                            }
                            Text(
                                text = "+${habit.xpReward} XP",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentLumen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Apple-style Pill Action Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(Radius.Chip))
                            .background(if (habit.isActive) AccentLumenSubtle else AccentLumen)
                            .border(
                                1.dp,
                                if (habit.isActive) GlassBorderLumen else Color.Transparent,
                                RoundedCornerShape(Radius.Chip)
                            )
                            .clickable {
                                haptics.click()
                                onToggleActive(habit)
                            }
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (habit.isActive) Icons.Rounded.Check else Icons.Rounded.Add,
                                contentDescription = null,
                                tint = if (habit.isActive) AccentLumen else MidnightBackground,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (habit.isActive) "Aktywny" else "Dodaj",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (habit.isActive) AccentLumen else MidnightBackground,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Zaawansowany kreator nawyku w czystym stylu Apple:
 * - Wybór typu celu: 1x (zwykłe odhaczenie) vs Nx (stepper z plusem + jednostka)
 * - Interaktywny wybierak dni tygodnia (Pn-Nd) z szybkimi presetami (Codziennie, Robocze, Weekend)
 */
@Composable
private fun AdvancedCustomHabitContent(
    onCreate: (
        name: String,
        desc: String,
        cat: HabitCategory,
        diff: HabitDifficulty,
        freq: HabitFrequency,
        targetCount: Int,
        unit: String,
        scheduledDays: Set<Int>
    ) -> Unit
) {
    val haptics = rememberHaptics()
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(HabitCategory.BODY_HEALTH) }
    var selectedDifficulty by remember { mutableStateOf(HabitDifficulty.MEDIUM) }

    // Cel (Target)
    var isMultipleTarget by remember { mutableStateOf(false) }
    var targetCount by remember { mutableIntStateOf(3) }
    var unit by remember { mutableStateOf("szklanek") }

    // Dni tygodnia (1=Pn .. 7=Nd)
    var selectedDays by remember { mutableStateOf(setOf(1, 2, 3, 4, 5, 6, 7)) }

    val dayNames = listOf(
        1 to "Pn",
        2 to "Wt",
        3 to "Śr",
        4 to "Cz",
        5 to "Pt",
        6 to "So",
        7 to "Nd"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(480.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Nazwa nawyku:",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Np. Wypicie wody, Nauka języka, Trening", color = TextTertiary, style = MaterialTheme.typography.bodyMedium) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.Tile),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentLumen,
                    unfocusedBorderColor = GlassBorderSubtle,
                    focusedContainerColor = MidnightSurfaceCard,
                    unfocusedContainerColor = MidnightSurfaceCard,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
        }

        item {
            Text(
                text = "Krótki opis lub intencja (opcjonalnie):",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Np. Na czczo ze szczyptą soli kłodawskiej", color = TextTertiary, style = MaterialTheme.typography.bodyMedium) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.Tile),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentLumen,
                    unfocusedBorderColor = GlassBorderSubtle,
                    focusedContainerColor = MidnightSurfaceCard,
                    unfocusedContainerColor = MidnightSurfaceCard,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
        }

        // --- TYP CELU: 1x ODHACZENIE vs NX WIELOKROTNY Z PLUSIKIEM ---
        item {
            Column {
                Text(
                    text = "Sposób realizacji:",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Opcja 1: Zwykłe odhaczenie (1x)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(Radius.Tile))
                            .background(if (!isMultipleTarget) MidnightSurfaceCard else MidnightBackground)
                            .border(
                                1.dp,
                                if (!isMultipleTarget) GlassBorderLumen else GlassBorderSubtle,
                                RoundedCornerShape(Radius.Tile)
                            )
                            .clickable {
                                haptics.click()
                                isMultipleTarget = false
                            }
                            .padding(vertical = 12.dp, horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Pojedynczy",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (!isMultipleTarget) TextPrimary else TextSecondary,
                                fontWeight = if (!isMultipleTarget) FontWeight.Bold else FontWeight.Medium
                            )
                            Text(
                                text = "1 raz dziennie (checkbox)",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary
                            )
                        }
                    }

                    // Opcja 2: Wielokrotny (stepper z plusem)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(Radius.Tile))
                            .background(if (isMultipleTarget) MidnightSurfaceCard else MidnightBackground)
                            .border(
                                1.dp,
                                if (isMultipleTarget) GlassBorderLumen else GlassBorderSubtle,
                                RoundedCornerShape(Radius.Tile)
                            )
                            .clickable {
                                haptics.click()
                                isMultipleTarget = true
                            }
                            .padding(vertical = 12.dp, horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Wielokrotny (+)",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isMultipleTarget) TextPrimary else TextSecondary,
                                fontWeight = if (isMultipleTarget) FontWeight.Bold else FontWeight.Medium
                            )
                            Text(
                                text = "Kilka powtórzeń (stepper)",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary
                            )
                        }
                    }
                }

                // Dodatkowe parametry dla nawyku wielokrotnego
                AnimatedVisibility(visible = isMultipleTarget) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(Radius.Tile))
                            .background(MidnightSurfaceCard)
                            .border(1.dp, GlassBorderSubtle, RoundedCornerShape(Radius.Tile))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Dzienny cel:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "$targetCount $unit",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AccentLumen,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Stepper +/-
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(MidnightSurfaceElevated)
                                        .border(1.dp, GlassBorderSubtle, CircleShape)
                                        .clickable(enabled = targetCount > 2) {
                                            haptics.tick()
                                            targetCount = (targetCount - 1).coerceAtLeast(2)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Remove,
                                        contentDescription = "Mniej",
                                        tint = if (targetCount > 2) TextPrimary else TextTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Text(
                                    text = "$targetCount",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )

                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(MidnightSurfaceElevated)
                                        .border(1.dp, GlassBorderSubtle, CircleShape)
                                        .clickable(enabled = targetCount < 20) {
                                            haptics.tick()
                                            targetCount = (targetCount + 1).coerceAtMost(20)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = "Więcej",
                                        tint = if (targetCount < 20) TextPrimary else TextTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Szybkie presety jednostki
                        Text(
                            text = "Jednostka:",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val quickUnits = listOf("szklanek", "stron", "minut", "razy", "serii", "porcji")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(quickUnits) { u ->
                                val isSel = unit == u
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(Radius.Badge))
                                        .background(if (isSel) AccentLumenSubtle else MidnightSurfaceElevated)
                                        .border(1.dp, if (isSel) GlassBorderLumen else GlassBorderSubtle, RoundedCornerShape(Radius.Badge))
                                        .clickable {
                                            haptics.click()
                                            unit = u
                                        }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = u,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSel) AccentLumen else TextSecondary,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- HARMONOGRAM DNI TYGODNIA ---
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dni realizacji:",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )

                    // Presety szybkiego wyboru
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Codz.",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selectedDays.size == 7) AccentLumen else TextTertiary,
                            fontWeight = if (selectedDays.size == 7) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                                .clip(RoundedCornerShape(Radius.Badge))
                                .clickable {
                                    haptics.click()
                                    selectedDays = setOf(1, 2, 3, 4, 5, 6, 7)
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        Text(
                            text = "Pn-Pt",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selectedDays == setOf(1, 2, 3, 4, 5)) AccentLumen else TextTertiary,
                            fontWeight = if (selectedDays == setOf(1, 2, 3, 4, 5)) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                                .clip(RoundedCornerShape(Radius.Badge))
                                .clickable {
                                    haptics.click()
                                    selectedDays = setOf(1, 2, 3, 4, 5)
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        Text(
                            text = "Weekend",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selectedDays == setOf(6, 7)) AccentLumen else TextTertiary,
                            fontWeight = if (selectedDays == setOf(6, 7)) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                                .clip(RoundedCornerShape(Radius.Badge))
                                .clickable {
                                    haptics.click()
                                    selectedDays = setOf(6, 7)
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Wybierak 7 dni tygodnia w stylu Apple
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dayNames.forEach { (isoDay, shortName) ->
                        val isSelected = selectedDays.contains(isoDay)
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) AccentLumen else MidnightSurfaceCard)
                                .border(
                                    1.dp,
                                    if (isSelected) AccentLumen else GlassBorderSubtle,
                                    CircleShape
                                )
                                .clickable {
                                    haptics.tick()
                                    selectedDays = if (isSelected) {
                                        if (selectedDays.size > 1) selectedDays - isoDay else selectedDays
                                    } else {
                                        selectedDays + isoDay
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = shortName,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) MidnightBackground else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Kategoria
        item {
            Column {
                Text(
                    text = "Obszar życia:",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(HabitCategory.values()) { cat ->
                        val isSel = cat == selectedCategory
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(Radius.Chip))
                                .background(if (isSel) MidnightSurfaceCard else MidnightBackground)
                                .border(
                                    1.dp,
                                    if (isSel) GlassBorderLumen else GlassBorderSubtle,
                                    RoundedCornerShape(Radius.Chip)
                                )
                                .clickable {
                                    haptics.click()
                                    selectedCategory = cat
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = cat.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSel) TextPrimary else TextSecondary,
                                fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Trudność & XP
        item {
            Column {
                Text(
                    text = "Waga nawyku:",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HabitDifficulty.values().forEach { diff ->
                        val isSel = diff == selectedDifficulty
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(Radius.Tile))
                                .background(if (isSel) MidnightSurfaceCard else MidnightBackground)
                                .border(
                                    1.dp,
                                    if (isSel) GlassBorderLumen else GlassBorderSubtle,
                                    RoundedCornerShape(Radius.Tile)
                                )
                                .clickable {
                                    haptics.click()
                                    selectedDifficulty = diff
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = diff.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) TextPrimary else TextSecondary
                                )
                                Text(
                                    text = "+${diff.weight * 10} XP",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AccentLumen
                                )
                            }
                        }
                    }
                }
            }
        }

        // Submit Button
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.Chip))
                    .background(if (name.isNotBlank()) AccentLumen else MidnightSurfaceCard)
                    .clickable(enabled = name.isNotBlank()) {
                        haptics.success()
                        val freq = when (selectedDays) {
                            setOf(1, 2, 3, 4, 5) -> HabitFrequency.WEEKDAYS
                            setOf(6, 7) -> HabitFrequency.WEEKENDS
                            else -> HabitFrequency.DAILY
                        }
                        onCreate(
                            name,
                            description,
                            selectedCategory,
                            selectedDifficulty,
                            freq,
                            if (isMultipleTarget) targetCount else 1,
                            if (isMultipleTarget) unit else "razy",
                            selectedDays
                        )
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Stwórz nawyk",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (name.isNotBlank()) MidnightBackground else TextTertiary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
