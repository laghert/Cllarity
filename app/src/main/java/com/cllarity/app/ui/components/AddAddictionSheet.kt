package com.cllarity.app.ui.components

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.Healing
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.SmokingRooms
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.WineBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cllarity.app.model.Addiction
import com.cllarity.app.ui.theme.AccentLumen
import com.cllarity.app.ui.theme.AccentLumenSubtle
import com.cllarity.app.ui.theme.GlassBorderLumen
import com.cllarity.app.ui.theme.GlassBorderSubtle
import com.cllarity.app.ui.theme.MidnightBackground
import com.cllarity.app.ui.theme.MidnightSurfaceCard
import com.cllarity.app.ui.theme.MidnightSurfaceElevated
import com.cllarity.app.ui.theme.Radius
import com.cllarity.app.ui.theme.SemanticAlert
import com.cllarity.app.ui.theme.TextPrimary
import com.cllarity.app.ui.theme.TextSecondary
import com.cllarity.app.ui.theme.TextTertiary
import com.cllarity.app.ui.theme.rememberHaptics
import com.cllarity.app.ui.theme.rememberSound
import java.util.Locale
import com.cllarity.app.ui.theme.Motion

private enum class AddictionSheetTab(val title: String) {
    TEMPLATES("Baza szablonów"),
    CUSTOM("Własny cel wolności")
}

@Composable
fun AddAddictionSheet(
    allAddictions: List<Addiction>,
    onActivateAddiction: (String) -> Unit,
    onDeactivateAddiction: (String) -> Unit,
    onCreateCustomAddiction: (
        name: String,
        tagline: String,
        iconName: String,
        startedAt: Long,
        costPerDay: Double,
        hoursSpent: Double,
        discreetName: String?
    ) -> Unit,
    onClose: () -> Unit
) {
    var currentTab by remember { mutableStateOf(AddictionSheetTab.TEMPLATES) }

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
                    text = "Cel Wolności",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Wybierz nałóg do zwalczania lub stwórz własny",
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

        // Apple Tab Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.Pill))
                .background(MidnightSurfaceCard)
                .border(1.dp, GlassBorderSubtle, RoundedCornerShape(Radius.Pill))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AddictionSheetTab.values().forEach { tab ->
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
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedContent(
            targetState = currentTab,
            transitionSpec = { Motion.fadeThrough() },
            label = "AddictionSheetTabs"
        ) { tab ->
            when (tab) {
                AddictionSheetTab.TEMPLATES -> AddictionTemplatesContent(
                    addictions = allAddictions,
                    onToggleActive = { add ->
                        if (add.isActive) {
                            onDeactivateAddiction(add.id)
                        } else {
                            onActivateAddiction(add.id)
                        }
                    }
                )
                AddictionSheetTab.CUSTOM -> AdvancedCustomAddictionContent(
                    onCreate = { name, tag, icon, start, cost, hrs, disc ->
                        onCreateCustomAddiction(name, tag, icon, start, cost, hrs, disc)
                        onClose()
                    }
                )
            }
        }
    }
}

@Composable
private fun AddictionTemplatesContent(
    addictions: List<Addiction>,
    onToggleActive: (Addiction) -> Unit
) {
    val haptics = rememberHaptics()
    val sound = rememberSound(true)

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(480.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(addictions, key = { it.id }) { addiction ->
            val iconVector = when (addiction.iconName) {
                "wine_bar" -> Icons.Rounded.WineBar
                "smoking_rooms" -> Icons.Rounded.SmokingRooms
                "fastfood" -> Icons.Rounded.Fastfood
                "healing" -> Icons.Rounded.Healing
                "visibility_off" -> Icons.Rounded.VisibilityOff
                "smartphone" -> Icons.Rounded.Smartphone
                "sports_esports" -> Icons.Rounded.SportsEsports
                else -> Icons.Rounded.Security
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.Tile))
                    .background(MidnightSurfaceCard)
                    .border(
                        1.dp,
                        if (addiction.isActive) GlassBorderLumen else GlassBorderSubtle,
                        RoundedCornerShape(Radius.Tile)
                    )
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(Radius.Chip))
                                .background(AccentLumenSubtle)
                                .border(1.dp, GlassBorderLumen, RoundedCornerShape(Radius.Chip)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = null,
                                tint = AccentLumen,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = addiction.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (!addiction.discreetName.isNullOrBlank()) {
                                Text(
                                    text = "Dyskretna: \"${addiction.discreetName}\"",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextTertiary,
                                )
                            }
                            Text(
                                text = addiction.tagline,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(Radius.Chip))
                            .background(if (addiction.isActive) AccentLumenSubtle else AccentLumen)
                            .border(
                                1.dp,
                                if (addiction.isActive) GlassBorderLumen else Color.Transparent,
                                RoundedCornerShape(Radius.Chip)
                            )
                            .clickable {
                                haptics.click()
                                sound.tap()
                                onToggleActive(addiction)
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (addiction.isActive) Icons.Rounded.Check else Icons.Rounded.Add,
                                contentDescription = null,
                                tint = if (addiction.isActive) AccentLumen else MidnightBackground,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (addiction.isActive) "Aktywny" else "Śledź",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (addiction.isActive) AccentLumen else MidnightBackground,
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
 * Rozbudowany, luksusowy kreator nałogów w stylu Apple:
 * - Kategoria behawioralna (Substancje, Dopamina cyfrowa, Impulsy)
 * - Interaktywny suwak kosztów i czasu z kalkulatorem zysków rocznych
 * - Wybór daty startu wstecz (dni czystości już osiągnięte)
 * - Wybór wektorowej ikony
 */
@Composable
private fun AdvancedCustomAddictionContent(
    onCreate: (
        name: String,
        tagline: String,
        iconName: String,
        startedAt: Long,
        costPerDay: Double,
        hoursSpent: Double,
        discreetName: String?
    ) -> Unit
) {
    val haptics = rememberHaptics()
    val sound = rememberSound(true)

    var name by remember { mutableStateOf("") }
    var tagline by remember { mutableStateOf("") }
    var discreetName by remember { mutableStateOf("") }

    var selectedCategory by remember { mutableStateOf("Substancje") }
    val categories = listOf("Substancje", "Dopamina cyfrowa", "Finanse & Impulsy")

    // Ikona
    val iconOptions = listOf(
        "wine_bar" to Icons.Rounded.WineBar,
        "smoking_rooms" to Icons.Rounded.SmokingRooms,
        "fastfood" to Icons.Rounded.Fastfood,
        "smartphone" to Icons.Rounded.Smartphone,
        "sports_esports" to Icons.Rounded.SportsEsports,
        "security" to Icons.Rounded.Security,
        "psychology" to Icons.Rounded.Psychology,
        "healing" to Icons.Rounded.Healing
    )
    var selectedIconName by remember { mutableStateOf("security") }

    // Suwaki zysków
    var costPerDaySlider by remember { mutableDoubleStateOf(20.0) }
    var hoursSpentSlider by remember { mutableDoubleStateOf(1.5) }

    // Start od teraz vs osiągnięty już rekord
    var daysCleanAlready by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Nazwa
        item {
            Column {
                Text(text = "Nazwa nałogu / zachowania:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Np. Alkohol, Nikotyna, Gry, Zakupy", color = TextTertiary, style = MaterialTheme.typography.bodyMedium) },
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
        }

        // 2. Kategoria
        item {
            Column {
                Text(text = "Obszar behawioralny:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSel = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(Radius.Tile))
                                .background(if (isSel) MidnightSurfaceCard else MidnightBackground)
                                .border(1.dp, if (isSel) GlassBorderLumen else GlassBorderSubtle, RoundedCornerShape(Radius.Tile))
                                .clickable {
                                    haptics.tick()
                                    selectedCategory = cat
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSel) AccentLumen else TextSecondary,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // 3. Wybór ikony
        item {
            Column {
                Text(text = "Piktogram celu:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(iconOptions) { (key, vector) ->
                        val isSel = selectedIconName == key
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(Radius.Chip))
                                .background(if (isSel) AccentLumenSubtle else MidnightSurfaceCard)
                                .border(1.dp, if (isSel) GlassBorderLumen else GlassBorderSubtle, RoundedCornerShape(Radius.Chip))
                                .clickable {
                                    haptics.tick()
                                    selectedIconName = key
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = vector,
                                contentDescription = null,
                                tint = if (isSel) AccentLumen else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // 4. Kalkulator Zysków Finansowych i Czasowych (Apple Sliders)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.Card))
                    .background(MidnightSurfaceCard)
                    .border(1.dp, GlassBorderSubtle, RoundedCornerShape(Radius.Card))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Szacowany dzienny koszt:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Text(
                        text = "%.0f zł / dzień".format(costPerDaySlider),
                        style = MaterialTheme.typography.titleMedium,
                        color = AccentLumen,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = costPerDaySlider.toFloat(),
                    onValueChange = {
                        costPerDaySlider = it.toDouble()
                        haptics.tick()
                    },
                    valueRange = 0f..150f,
                    steps = 29, // co 5 zł
                    colors = SliderDefaults.colors(
                        thumbColor = AccentLumen,
                        activeTrackColor = AccentLumen,
                        inactiveTrackColor = MidnightSurfaceElevated
                    )
                )

                // Prognoza roczna
                val yearlySavings = costPerDaySlider * 365
                Text(
                    text = "Odzyskasz aż %.0f zł rocznie do zainwestowania w siebie".format(yearlySavings),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )

                Spacer(modifier = Modifier.height(14.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(GlassBorderSubtle))
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Dzienny tracony czas:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Text(
                        text = "%.1f h / dzień".format(hoursSpentSlider),
                        style = MaterialTheme.typography.titleMedium,
                        color = AccentLumen,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = hoursSpentSlider.toFloat(),
                    onValueChange = {
                        hoursSpentSlider = it.toDouble()
                        haptics.tick()
                    },
                    valueRange = 0f..8f,
                    steps = 15,
                    colors = SliderDefaults.colors(
                        thumbColor = AccentLumen,
                        activeTrackColor = AccentLumen,
                        inactiveTrackColor = MidnightSurfaceElevated
                    )
                )

                val monthlyHours = hoursSpentSlider * 30
                Text(
                    text = "To %.0f godzin w miesiącu – równowartość pełnego tygodnia urlopu".format(monthlyHours),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
        }

        // 5. Data startu (Czy jesteś już czysty od paru dni?)
        item {
            Column {
                Text(text = "Stan początkowy:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.Tile))
                        .background(MidnightSurfaceCard)
                        .border(1.dp, GlassBorderSubtle, RoundedCornerShape(Radius.Tile))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (daysCleanAlready == 0) "Zaczynam czystą drogę od teraz" else "Jestem czysty już od $daysCleanAlready dni",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(text = "Nie trać osiągniętych już dni wolności", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(0, 3, 7, 14, 30).forEach { d ->
                            val isSel = daysCleanAlready == d
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(Radius.Badge))
                                    .background(if (isSel) AccentLumen else MidnightSurfaceElevated)
                                    .clickable {
                                        haptics.tick()
                                        daysCleanAlready = d
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (d == 0) "Dziś" else "${d}d",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSel) MidnightBackground else TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Dyskretna nazwa
        item {
            Column {
                Text(text = "Dyskretna nazwa w miejscach publicznych:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = discreetName,
                    onValueChange = { discreetName = it },
                    placeholder = { Text("Np. Wolny czas / Projekt X", color = TextTertiary, style = MaterialTheme.typography.bodyMedium) },
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
        }

        // Submit Button
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.Chip))
                    .background(if (name.isNotBlank()) AccentLumen else MidnightSurfaceCard)
                    .clickable(enabled = name.isNotBlank()) {
                        haptics.success()
                        sound.chord()
                        val startTimestamp = System.currentTimeMillis() - (daysCleanAlready * 86400 * 1000L)
                        val finalTagline = if (tagline.isNotBlank()) tagline else "Czysta ścieżka bez $name"
                        onCreate(
                            name,
                            finalTagline,
                            selectedIconName,
                            startTimestamp,
                            costPerDaySlider,
                            hoursSpentSlider,
                            discreetName.ifBlank { null }
                        )
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Rozpocznij drogę wolności",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (name.isNotBlank()) MidnightBackground else TextTertiary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
