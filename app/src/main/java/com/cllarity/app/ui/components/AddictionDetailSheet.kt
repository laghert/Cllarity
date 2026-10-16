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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.Healing
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.SmokingRooms
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.WineBar
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cllarity.app.model.Addiction
import com.cllarity.app.ui.theme.AccentAmber
import com.cllarity.app.ui.theme.AccentEmerald
import com.cllarity.app.ui.theme.AccentIndigo
import com.cllarity.app.ui.theme.AccentIndigoLight
import com.cllarity.app.ui.theme.AccentRose
import com.cllarity.app.ui.theme.AccentLumen
import com.cllarity.app.ui.theme.GlassBorder
import com.cllarity.app.ui.theme.GlassBorderSubtle
import com.cllarity.app.ui.theme.GlassBorderLumen
import com.cllarity.app.ui.theme.Radius
import com.cllarity.app.ui.theme.MidnightSurfaceCard
import com.cllarity.app.ui.theme.MidnightSurfaceElevated
import com.cllarity.app.ui.theme.TextPrimary
import com.cllarity.app.ui.theme.TextSecondary
import com.cllarity.app.ui.theme.TextTertiary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddictionDetailSheet(
    addiction: Addiction,
    isPrivacyMode: Boolean,
    onResetClick: () -> Unit,
    onClose: () -> Unit
) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val days = addiction.getDaysElapsed(now)
    val hours = addiction.getHoursPart(now)
    val minutes = addiction.getMinutesPart(now)
    val seconds = addiction.getSecondsPart(now)
    val money = addiction.getMoneySaved(now)
    val hoursSaved = addiction.getHoursSaved(now)
    val displayName = addiction.getDisplayName(isPrivacyMode)
    val accentColor = Color(addiction.accentColorHex)

    var selectedHorizonIndex by remember { mutableIntStateOf(1) } // 0: 30d, 1: 1 rok, 2: 5 lat
    val horizons = remember {
        listOf(
            Triple(30L, "30 DNI", "Weekendowy wyjazd lub 3 nowe książki"),
            Triple(365L, "1 ROK", "Bilety lotnicze i wymarzone wakacje"),
            Triple(1825L, "5 LAT", "Wkład własny na mieszkanie lub samochód")
        )
    }

    val triggersAnalysis = remember(addiction.relapseHistory) {
        addiction.relapseHistory
            .mapNotNull { it.trigger }
            .filter { it.isNotBlank() }
            .groupingBy { it }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
    }
    // Synthesize 7-day progress points for visualization
    val chartPoints = remember(days) {
        val base = days.toFloat()
        listOf(
            (base - 6).coerceAtLeast(0f),
            (base - 5).coerceAtLeast(0f),
            (base - 4).coerceAtLeast(0f),
            (base - 3).coerceAtLeast(0f),
            (base - 2).coerceAtLeast(0f),
            (base - 1).coerceAtLeast(0f),
            base
        )
    }
    val chartLabels = listOf("-6d", "-5d", "-4d", "-3d", "-2d", "Wczoraj", "Dziś")

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

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
                        .background(accentColor.copy(alpha = 0.16f))
                        .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(Radius.Tile)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getDetailIcon(addiction.iconName),
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = addiction.tagline,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary,
                        maxLines = 1
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
                .height(480.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Live Ticking Counter
            // 1. Apple-style Hero Abstinence Clock Dashboard (Zastępuje ciasny klocek z [Image #2])
            item {
                AppleCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.Card),
                    backgroundColor = MidnightSurfaceElevated,
                    borderColor = GlassBorderLumen.copy(alpha = 0.35f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "CZAS WOLNOŚCI",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentLumen,
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Wielkie Dni jako główny bohater
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$days",
                                style = MaterialTheme.typography.displayLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-1.2).sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (days == 1L) "dzień czystości" else "dni czystości",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Płynne pod-zegary w 3 minimalistycznych kafelkach
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(Radius.Tile))
                                    .background(MidnightSurfaceCard)
                                    .border(1.dp, GlassBorderSubtle, RoundedCornerShape(Radius.Tile))
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = String.format(Locale.getDefault(), "%02d", hours),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("Godzin", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(Radius.Tile))
                                    .background(MidnightSurfaceCard)
                                    .border(1.dp, GlassBorderSubtle, RoundedCornerShape(Radius.Tile))
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = String.format(Locale.getDefault(), "%02d", minutes),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("Minut", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(Radius.Tile))
                                    .background(MidnightSurfaceCard)
                                    .border(1.dp, GlassBorderSubtle, RoundedCornerShape(Radius.Tile))
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = String.format(Locale.getDefault(), "%02d", seconds),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = AccentLumen,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("Sekund", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                                }
                            }
                        }
                    }
                }
            }

            // 2. Metrics (Money & Hours)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(Radius.Tile))
                            .background(MidnightSurfaceCard)
                            .border(1.dp, GlassBorderSubtle, RoundedCornerShape(Radius.Tile))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.AttachMoney, null, tint = AccentEmerald, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Zaoszczędzone", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f zł", money),
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(Radius.Tile))
                            .background(MidnightSurfaceCard)
                            .border(1.dp, GlassBorderSubtle, RoundedCornerShape(Radius.Tile))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.AccessTime, null, tint = AccentIndigo, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Odzyskany czas", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f h", hoursSaved),
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            // 2.3 Interactive Savings & Time Projection Calculator
            item {
                val currentHorizon = horizons[selectedHorizonIndex]
                val projectedDays = currentHorizon.first
                val projectedMoney = projectedDays * addiction.costPerDayPln
                val projectedHours = projectedDays * addiction.hoursPerDaySpent

                AppleCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.Card),
                    backgroundColor = MidnightSurfaceElevated
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Savings,
                                    contentDescription = null,
                                    tint = AccentEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Kalkulator Przyszłych Zysków",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Horizon selector pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            horizons.forEachIndexed { idx, h ->
                                val isSelected = idx == selectedHorizonIndex
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(Radius.Chip))
                                        .background(if (isSelected) AccentIndigo else MidnightSurfaceCard)
                                        .border(
                                            1.dp,
                                            if (isSelected) AccentIndigoLight else GlassBorderSubtle,
                                            RoundedCornerShape(Radius.Chip)
                                        )
                                        .clickable { selectedHorizonIndex = idx }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = h.second,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) Color.White else TextSecondary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Money projection
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(Radius.Tile))
                                    .background(MidnightSurfaceCard)
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Zaoszczędzisz",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextTertiary,
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = String.format(Locale.getDefault(), "+%.0f zł", projectedMoney),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = AccentEmerald,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Time projection
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(Radius.Tile))
                                    .background(MidnightSurfaceCard)
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Odzyskasz",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextTertiary,
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = String.format(Locale.getDefault(), "+%.0f godz.", projectedHours),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = AccentIndigoLight,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Real-life reward comparison
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(Radius.Chip))
                                .background(Color(0x1810B981))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "🎁 ${currentHorizon.third}",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentEmerald,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // 2.5 Motivational Milestone Projection
            item {
                val milestone = getNextMilestone(days, addiction.costPerDayPln)
                AppleCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.Card),
                    backgroundColor = MidnightSurfaceCard
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "NAJBLIŻSZY CEL",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(Radius.Badge))
                                    .background(Color(0x1EF59E0B))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = milestone.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AccentAmber,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress Bar to next milestone
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(Radius.Pill))
                                .background(Color(0xFF1E2840))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = milestone.progress.coerceIn(0.04f, 1f))
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(Radius.Pill))
                                    .background(
                                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                                            listOf(AccentIndigo, AccentEmerald)
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (milestone.daysRemaining > 0)
                                "Jeszcze ${milestone.daysRemaining} d. do celu! Zaoszczędzisz dodatkowe +${String.format(Locale.getDefault(), "%.0f zł", milestone.projectedSavingsPln)}."
                            else
                                "Kamień milowy osiągnięty! Jesteś na wspaniałej drodze.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            // 3. Smooth Line Chart of Sobriety Days Trend
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
                                    tint = accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Trend trzeźwości (ostatnie 7 dni)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "$days d. ciąg",
                                style = MaterialTheme.typography.labelSmall,
                                color = accentColor,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        SmoothLineChart(
                            dataPoints = chartPoints,
                            labels = chartLabels,
                            lineColor = accentColor,
                            height = 120.dp
                        )
                    }
                }
            }

            // 4. Relapse History Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = null,
                            tint = AccentRose,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Historia potknięć (${addiction.relapseHistory.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            // 4.1 Triggers Analytics Card
            if (triggersAnalysis.isNotEmpty()) {
                item {
                    AppleCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Radius.Tile),
                        backgroundColor = MidnightSurfaceCard
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Psychology,
                                    contentDescription = null,
                                    tint = AccentRose,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Główne wyzwalacze pokus",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                triggersAnalysis.take(4).forEach { (trigger, count) ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(Radius.Badge))
                                            .background(Color(0x22F43F5E))
                                            .border(1.dp, Color(0x44F43F5E), RoundedCornerShape(Radius.Badge))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "$trigger ($count×)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFFDA4AF),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (addiction.relapseHistory.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radius.Tile))
                            .background(Color(0x1810B981))
                            .border(1.dp, AccentEmerald.copy(alpha = 0.3f), RoundedCornerShape(Radius.Tile))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🌟 Czyste konto! Brak zarejestrowanych potknięć. Twój umysł i ciało Ci dziękują.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AccentEmerald,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(addiction.relapseHistory.reversed()) { event ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radius.Tile))
                            .background(MidnightSurfaceCard)
                            .border(1.dp, GlassBorderSubtle, RoundedCornerShape(Radius.Tile))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dateFormat.format(Date(event.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )

                                if (!event.trigger.isNullOrBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(Radius.Badge))
                                            .background(Color(0x18F43F5E))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = event.trigger,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AccentRose,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            if (!event.note.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "\"${event.note}\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            // 5. Reset Action Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.Tile))
                        .background(Color(0x18EF4444))
                        .border(1.dp, Color(0x44EF4444), RoundedCornerShape(Radius.Tile))
                        .clickable { onResetClick() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.RestartAlt, null, tint = AccentRose, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Zgłoś potknięcie i zresetuj licznik",
                            style = MaterialTheme.typography.labelLarge,
                            color = AccentRose,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailTimeTile(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
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

private fun getDetailIcon(iconName: String): ImageVector = when (iconName) {
    "wine_bar" -> Icons.Rounded.WineBar
    "smoking_rooms" -> Icons.Rounded.SmokingRooms
    "fastfood" -> Icons.Rounded.Fastfood
    "healing" -> Icons.Rounded.Healing
    "visibility_off" -> Icons.Rounded.VisibilityOff
    else -> Icons.Rounded.Security
}

private data class MilestoneInfo(
    val milestoneDays: Long,
    val title: String,
    val daysRemaining: Long,
    val progress: Float,
    val projectedSavingsPln: Double
)

private fun getNextMilestone(currentDays: Long, costPerDay: Double): MilestoneInfo {
    val milestones = listOf(
        1L to "1 dzień (24h)",
        3L to "3 dni siły",
        7L to "1 tydzień",
        14L to "2 tygodnie",
        30L to "1 miesiąc",
        60L to "2 miesiące",
        90L to "Reset 90 dni",
        180L to "Pół roku",
        365L to "1 rok wolności"
    )
    val next = milestones.firstOrNull { it.first > currentDays } ?: (currentDays + 30L to "Kolejny miesiąc")
    val daysRem = (next.first - currentDays).coerceAtLeast(0L)
    val prog = if (next.first > 0) (currentDays.toFloat() / next.first.toFloat()).coerceIn(0f, 1f) else 1f
    val addSavings = daysRem * costPerDay

    return MilestoneInfo(
        milestoneDays = next.first,
        title = next.second,
        daysRemaining = daysRem,
        progress = prog,
        projectedSavingsPln = addSavings
    )
}
