package com.cllarity.app.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cllarity.app.model.Addiction
import com.cllarity.app.model.LlumieCosmetic
import com.cllarity.app.model.LlumieMood
import com.cllarity.app.model.LlumieState
import com.cllarity.app.ui.theme.AccentAmber
import com.cllarity.app.ui.theme.AccentEmerald
import com.cllarity.app.ui.theme.AccentIndigo
import com.cllarity.app.ui.theme.AccentRose
import com.cllarity.app.ui.theme.GlassBorder
import com.cllarity.app.ui.theme.GlassBorderSubtle
import com.cllarity.app.ui.theme.MidnightSurfaceCard
import com.cllarity.app.ui.theme.TextPrimary
import com.cllarity.app.ui.theme.TextSecondary
import com.cllarity.app.ui.theme.TextTertiary
import com.cllarity.app.ui.theme.Radius

@Composable
fun DebugSheet(
    llumie: LlumieState,
    activeAddictions: List<Addiction>,
    onSetLevel: (Int) -> Unit,
    onSetStreak: (Int) -> Unit,
    onAddXp: (Int) -> Unit,
    onSetMood: (LlumieMood) -> Unit,
    onEquipCosmetic: (LlumieCosmetic) -> Unit,
    onFastForwardAddiction: (String, Long) -> Unit,
    onResetAddictionToZero: (String) -> Unit,
    onCompleteAllHabits: () -> Unit,
    onResetTodayHabits: () -> Unit,
    onResetAllData: () -> Unit,
    onLaunchSetup: () -> Unit = {},
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.Badge))
                        .background(Color(0xFFDC2626))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "DEV",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Laboratorium Debug",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Zamknij",
                    tint = TextSecondary
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. LLUMIE CONTROLS
            item {
                DebugSectionCard(title = "Llumie (Poziom, XP i Seria)") {
                    // Level
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Poziom: ${llumie.level}",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DebugActionButton(
                                label = "-1",
                                icon = Icons.Rounded.Remove,
                                onClick = { onSetLevel(llumie.level - 1) }
                            )
                            DebugActionButton(
                                label = "+1",
                                icon = Icons.Rounded.Add,
                                onClick = { onSetLevel(llumie.level + 1) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Streak
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Seria: ${llumie.streakDays} dni",
                            style = MaterialTheme.typography.titleMedium,
                            color = AccentAmber,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            DebugActionButton(label = "-1d", onClick = { onSetStreak(llumie.streakDays - 1) })
                            DebugActionButton(label = "+1d", onClick = { onSetStreak(llumie.streakDays + 1) })
                            DebugActionButton(label = "+7d", onClick = { onSetStreak(llumie.streakDays + 7) })
                            DebugActionButton(label = "+30d", onClick = { onSetStreak(llumie.streakDays + 30) })
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick XP Boost
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "XP: ${llumie.currentXp}/${llumie.xpForNextLevel}",
                            style = MaterialTheme.typography.titleMedium,
                            color = AccentIndigo,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DebugActionButton(label = "+50 XP", onClick = { onAddXp(50) })
                            DebugActionButton(label = "+150 XP", onClick = { onAddXp(150) })
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Wymuś nastrój:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(LlumieMood.values()) { mood ->
                            val isSel = llumie.mood == mood
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(Radius.Chip))
                                    .background(if (isSel) AccentIndigo else MidnightSurfaceCard)
                                    .border(1.dp, if (isSel) AccentIndigo else GlassBorderSubtle, RoundedCornerShape(Radius.Chip))
                                    .clickable { onSetMood(mood) }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = mood.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSel) Color.White else TextSecondary,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Wymuś akcesorium (bez względu na poziom):",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(LlumieCosmetic.values()) { cos ->
                            val isSel = llumie.equippedCosmetic == cos
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(Radius.Chip))
                                    .background(if (isSel) AccentEmerald else MidnightSurfaceCard)
                                    .border(1.dp, if (isSel) AccentEmerald else GlassBorderSubtle, RoundedCornerShape(Radius.Chip))
                                    .clickable { onEquipCosmetic(cos) }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = cos.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSel) Color.White else TextSecondary,
                                )
                            }
                        }
                    }
                }
            }

            // 2. ADDICTIONS FAST-FORWARD
            item {
                DebugSectionCard(title = "Symulator Czasu Trzeźwości (Nałogi)") {
                    if (activeAddictions.isEmpty()) {
                        Text(
                            text = "Brak aktywnych nałogów do symulacji.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextTertiary
                        )
                    } else {
                        activeAddictions.forEach { addiction ->
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Text(
                                    text = "${addiction.name} (Obecnie: ${addiction.getDaysElapsed()} dni)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    item {
                                        DebugActionButton(label = "+1 d", onClick = { onFastForwardAddiction(addiction.id, 1) })
                                    }
                                    item {
                                        DebugActionButton(label = "+7 d", onClick = { onFastForwardAddiction(addiction.id, 7) })
                                    }
                                    item {
                                        DebugActionButton(label = "+30 d", onClick = { onFastForwardAddiction(addiction.id, 30) })
                                    }
                                    item {
                                        DebugActionButton(label = "+365 d (1 rok)", onClick = { onFastForwardAddiction(addiction.id, 365) })
                                    }
                                    item {
                                        DebugActionButton(
                                            label = "Zeruj",
                                            color = AccentRose,
                                            onClick = { onResetAddictionToZero(addiction.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. HABITS SIMULATOR
            item {
                DebugSectionCard(title = "Symulator Dziennych Nawyków") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(Radius.Chip))
                                .background(AccentEmerald)
                                .clickable { onCompleteAllHabits() }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Zalicz wszystkie",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(Radius.Chip))
                                .background(Color(0xFF2D3748))
                                .clickable { onResetTodayHabits() }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Odznacz wszystkie",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // 4. FACTORY RESET
            item {
                DebugSectionCard(title = "Przewodnik początkowy (Setup Guide)") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radius.Chip))
                            .background(Color(0x22F2CF8B))
                            .border(1.dp, Color(0x66F2CF8B), RoundedCornerShape(Radius.Chip))
                            .clickable {
                                onClose()
                                onLaunchSetup()
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFF2CF8B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Uruchom Setup Guide (Przewodnik)",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFF2CF8B),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 5. FACTORY RESET
            item {
                DebugSectionCard(title = "Stan danych") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radius.Chip))
                            .background(Color(0x22EF4444))
                            .border(1.dp, Color(0x55EF4444), RoundedCornerShape(Radius.Chip))
                            .clickable { onResetAllData() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = null,
                                tint = AccentRose,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Przywróć dane początkowe (Factory Reset)",
                                style = MaterialTheme.typography.labelMedium,
                                color = AccentRose,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DebugSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.Tile))
            .background(MidnightSurfaceCard)
            .border(1.dp, GlassBorderSubtle, RoundedCornerShape(Radius.Tile))
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = AccentIndigo,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun DebugActionButton(
    label: String,
    icon: ImageVector? = null,
    color: Color = AccentIndigo,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.Chip))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(Radius.Chip))
            .clickable { onClick() }
            .padding(horizontal = 9.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
