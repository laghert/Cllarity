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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cllarity.app.model.Habit
import com.cllarity.app.model.HabitCategory
import com.cllarity.app.ui.theme.AccentAmber
import com.cllarity.app.ui.theme.AccentEmerald
import com.cllarity.app.ui.theme.AccentIndigo
import com.cllarity.app.ui.theme.AccentIndigoLight
import com.cllarity.app.ui.theme.AccentRose
import com.cllarity.app.ui.theme.AccentLumen
import com.cllarity.app.ui.theme.AccentLumenSubtle
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
import com.cllarity.app.ui.theme.rememberHaptics

@Composable
fun HabitDetailSheet(
    habit: Habit,
    onToggleToday: () -> Unit,
    onRemove: () -> Unit,
    onClose: () -> Unit
) {
    val haptics = rememberHaptics()
    val safeCat = habit.safeCategory
    val safeDiff = habit.safeDifficulty
    val safeFreq = habit.safeFrequency
    val safeHistory = habit.safeHistoryDates

    val categoryColor = when (safeCat) {
        HabitCategory.MORNING -> AccentAmber
        HabitCategory.BODY_HEALTH -> AccentRose
        HabitCategory.FOCUS_MIND -> AccentIndigo
        HabitCategory.EVENING -> Color(0xFF818CF8)
    }

    val totalCompleted = safeHistory.size + if (habit.isCompletedToday && safeHistory.none { it.endsWith("today") }) 1 else 0
    val totalXpEarned = totalCompleted * habit.xpReward
    // Activity trend over the past 7 days (synthesized or from history)
    val activityPoints = remember(habit.streak, habit.isCompletedToday) {
        val s = habit.streak.toFloat()
        listOf(
            (s - 6).coerceAtLeast(0f),
            (s - 5).coerceAtLeast(0f),
            (s - 4).coerceAtLeast(0f),
            (s - 3).coerceAtLeast(0f),
            (s - 2).coerceAtLeast(0f),
            (s - 1).coerceAtLeast(0f),
            s
        )
    }
    val chartLabels = listOf("-6d", "-5d", "-4d", "-3d", "-2d", "Wczoraj", "Dziś")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 22.dp, vertical = 12.dp)
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
                        .size(46.dp)
                        .clip(RoundedCornerShape(Radius.Tile))
                        .background(categoryColor.copy(alpha = 0.16f))
                        .border(1.dp, categoryColor.copy(alpha = 0.35f), RoundedCornerShape(Radius.Tile)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getHabitIcon(habit.category),
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = if (habit.isCountBased) {
                            "${safeCat.displayName} • Cel: ${habit.safeTargetCount} ${habit.safeUnit} dziennie"
                        } else {
                            "${safeCat.displayName} • ${safeDiff.label}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Zamknij",
                    tint = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(440.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Description Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.Tile))
                        .background(MidnightSurfaceCard)
                        .border(1.dp, GlassBorderSubtle, RoundedCornerShape(Radius.Tile))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "CEL I KORZYŚĆ NAWYKU",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = habit.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // 2. Metrics (Streak, Total completions, Total XP)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HabitMetricTile(
                        title = "Seria",
                        value = "${habit.streak} d.",
                        icon = Icons.Rounded.LocalFireDepartment,
                        color = AccentLumen,
                        modifier = Modifier.weight(1f)
                    )
                    HabitMetricTile(
                        title = if (habit.isCountBased) "Cel dzisiaj" else "Ukończono",
                        value = if (habit.isCountBased) "${habit.safeCurrentCountToday}/${habit.safeTargetCount}" else "${totalCompleted.coerceAtLeast(habit.streak)}x",
                        icon = Icons.Rounded.Check,
                        color = AccentLumen,
                        modifier = Modifier.weight(1f)
                    )
                    HabitMetricTile(
                        title = "Zdobyty XP",
                        value = "+$totalXpEarned",
                        icon = Icons.Rounded.Bolt,
                        color = AccentLumen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3. Activity Trend Chart
            item {
                AppleCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.Card),
                    backgroundColor = MidnightSurfaceCard
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ShowChart,
                                    contentDescription = null,
                                    tint = AccentEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Trend regularności (7 dni)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Dyscyplina",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentEmerald,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        SmoothLineChart(
                            dataPoints = activityPoints,
                            labels = chartLabels,
                            lineColor = AccentEmerald,
                            height = 120.dp
                        )
                    }
                }
            }
            // 3.5 Heatmap Kalendarza Dyscypliny (Ostatnie 28 dni)
            item {
                val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
                val todayStr = remember { dateFormat.format(Date()) }

                val past28Days = remember(safeHistory, habit.isCompletedToday) {
                    val list = mutableListOf<Triple<String, Int, Boolean>>() // dateStr, dayOfMonth, isDone
                    for (i in 27 downTo 0) {
                        val c = Calendar.getInstance()
                        c.add(Calendar.DAY_OF_YEAR, -i)
                        val dStr = dateFormat.format(c.time)
                        val dayNum = c.get(Calendar.DAY_OF_MONTH)
                        val isDone = if (dStr == todayStr) habit.isCompletedToday || safeHistory.contains(dStr)
                        else safeHistory.contains(dStr) || habit.lastCompletedDate == dStr
                        list.add(Triple(dStr, dayNum, isDone))
                    }
                    list
                }

                val completedIn28 = past28Days.count { it.third }
                val consistencyPercent = ((completedIn28.toFloat() / 28f) * 100).toInt()

                AppleCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.Card),
                    backgroundColor = MidnightSurfaceCard
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.CalendarMonth,
                                    contentDescription = null,
                                    tint = AccentIndigoLight,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Kalendarz nawyku (4 tygodnie)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "$consistencyPercent%",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (consistencyPercent >= 50) AccentEmerald else AccentAmber,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Siatka dni tygodnia
                        val weekChunked = past28Days.chunked(7)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            weekChunked.forEach { week ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    week.forEach { (dateStr, dayNum, isDone) ->
                                        val isToday = dateStr == todayStr
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(32.dp)
                                                .clip(RoundedCornerShape(Radius.Badge))
                                                .background(
                                                    when {
                                                        isDone -> AccentEmerald
                                                        else -> MidnightSurfaceElevated
                                                    }
                                                )
                                                .border(
                                                    width = if (isToday) 1.5.dp else 1.dp,
                                                    color = when {
                                                        isToday -> Color.White
                                                        isDone -> AccentEmerald.copy(alpha = 0.5f)
                                                        else -> GlassBorderSubtle
                                                    },
                                                    shape = RoundedCornerShape(Radius.Badge)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isDone) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            } else {
                                                Text(
                                                    text = "$dayNum",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = TextTertiary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Zaliczono $completedIn28 z 28 ostatnich dni",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(AccentEmerald)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Ukończone",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextTertiary,
                                )
                            }
                        }
                    }
                }
            }

            // 4. Quick Action: Toggle today status
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.Chip))
                        .background(if (habit.isCompletedToday) AccentLumenSubtle else AccentLumen)
                        .border(1.dp, if (habit.isCompletedToday) GlassBorderLumen else Color.Transparent, RoundedCornerShape(Radius.Chip))
                        .clickable {
                            if (!habit.isCompletedToday) haptics.success() else haptics.click()
                            onToggleToday()
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = if (habit.isCompletedToday) AccentLumen else MidnightBackground,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (habit.isCompletedToday) "Ukończony na dziś (kliknij by cofnąć)" else "Zalicz nawyk na dziś (+${habit.xpReward} XP)",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (habit.isCompletedToday) AccentLumen else MidnightBackground,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 5. Remove Action (stonowany alert koralowy, bezpieczny)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.Chip))
                        .background(SemanticAlertBg)
                        .border(1.dp, SemanticAlertBorder, RoundedCornerShape(Radius.Chip))
                        .clickable {
                            onRemove()
                            onClose()
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Delete, null, tint = SemanticAlert, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Przestań śledzić ten nawyk",
                            style = MaterialTheme.typography.labelMedium,
                            color = SemanticAlert,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitMetricTile(
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
            .padding(12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(title, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

private fun getHabitIcon(category: HabitCategory): ImageVector = when (category) {
    HabitCategory.MORNING -> Icons.Rounded.WbSunny
    HabitCategory.BODY_HEALTH -> Icons.Rounded.Favorite
    HabitCategory.FOCUS_MIND -> Icons.Rounded.Psychology
    HabitCategory.EVENING -> Icons.Rounded.NightsStay
}
