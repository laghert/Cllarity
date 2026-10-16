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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.WbSunny
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cllarity.app.ui.theme.AccentLumen
import com.cllarity.app.ui.theme.AccentLumenSubtle
import com.cllarity.app.ui.theme.GlassBorderLumen
import com.cllarity.app.ui.theme.GlassBorderSubtle
import com.cllarity.app.ui.theme.MidnightBackground
import com.cllarity.app.ui.theme.MidnightSurfaceCard
import com.cllarity.app.ui.theme.MidnightSurfaceElevated
import com.cllarity.app.ui.theme.Radius
import com.cllarity.app.ui.theme.TextPrimary
import com.cllarity.app.ui.theme.TextSecondary
import com.cllarity.app.ui.theme.TextTertiary
import com.cllarity.app.ui.theme.rememberHaptics
import com.cllarity.app.ui.theme.rememberSound
import java.util.Calendar
import com.cllarity.app.ui.theme.Motion

data class StoicQuote(val text: String, val author: String)

private val CuratedQuotes = listOf(
    StoicQuote("Masz władzę nad swoim umysłem, a nie nad zdarzeniami na zewnątrz. Uświadom to sobie, a odnajdziesz siłę.", "Marek Aureliusz"),
    StoicQuote("Prawdziwa wolność to panowanie nad samym sobą. Człowiek, który nie potrafi sobą rządzić, nie jest wolny.", "Epiktet"),
    StoicQuote("Nie ten jest biedny, kto ma mało, ale ten, kto pragnie więcej.", "Seneka"),
    StoicQuote("Najlepszą zemstą na złych nawykach jest nie bycie do nich podobnym.", "Marek Aureliusz"),
    StoicQuote("Spokój umysłu rodzi się z rezygnacji z kontroli nad tym, na co nie mamy wpływu.", "Epiktet"),
    StoicQuote("Trudności ukazują ludzki charakter. Gdy nadejdą próby, pamiętaj, że to zapaśnik, z którym stajesz do walki.", "Epiktet"),
    StoicQuote("Kto zwycięża innych, jest silny; kto zwycięża siebie, jest niezwyciężony.", "Laozi"),
    StoicQuote("Nie trać więcej czasu na dyskusje, jaki powinien być dobry człowiek. Po prostu nim bądź.", "Marek Aureliusz"),
    StoicQuote("Im większy wysiłek wkładasz w opanowanie impulsu, tym większą budujesz wewnętrzną twierdzę.", "Seneka")
)

/**
 * Zbalansowany moduł:
 * - W ciągu dnia: Myśl Dnia (Stoicyzm & Spokój)
 * - Wieczorem (po 20:00): Wieczorne Wyciszenie i Dziennik Wdzięczności (+20 XP)
 */
@Composable
fun DailyStoicAffirmation(
    isEveningDone: Boolean = false,
    onSaveEveningReflection: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptics = rememberHaptics()
    val sound = rememberSound(true)
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val isEveningTime = currentHour >= 20 || currentHour < 5

    val dayOfYear = remember { Calendar.getInstance().get(Calendar.DAY_OF_YEAR) }
    var quoteOffset by remember { mutableIntStateOf(0) }
    val currentQuoteIndex = (dayOfYear + quoteOffset) % CuratedQuotes.size
    val activeQuote = CuratedQuotes[currentQuoteIndex]

    var eveningNote by remember { mutableStateOf("") }
    var isSubmittedToday by remember(isEveningDone) { mutableStateOf(isEveningDone) }

    AppleCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.Card),
        backgroundColor = MidnightSurfaceElevated,
        borderColor = if (isEveningTime && !isSubmittedToday) GlassBorderLumen.copy(alpha = 0.4f) else GlassBorderSubtle
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AccentLumenSubtle)
                            .border(1.dp, GlassBorderLumen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isEveningTime) Icons.Rounded.NightsStay else Icons.Rounded.FormatQuote,
                            contentDescription = null,
                            tint = AccentLumen,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isEveningTime) "WIECZORNE WYCISZENIE" else "MYŚL DNIA • STOICYZM & SPOKÓJ",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentLumen,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (isEveningTime) "Zapieczętuj dzień i wycisz umysł" else "Kotwica uwagi na dzisiejszy dzień",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    }
                }

                if (!isEveningTime) {
                    IconButton(
                        onClick = {
                            haptics.tick()
                            sound.tap()
                            quoteOffset++
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Inna myśl",
                            tint = TextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cytat stoicki
            AnimatedContent(
                targetState = activeQuote,
                transitionSpec = { Motion.crossfade() },
                label = "dailyQuoteAnim"
            ) { q ->
                Column {
                    Text(
                        text = "„${q.text}”",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        fontStyle = FontStyle.Italic,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "— ${q.author}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Opcjonalny wieczorny mikro-dziennik wdzięczności (po 20:00)
            if (isEveningTime) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(GlassBorderSubtle))
                Spacer(modifier = Modifier.height(14.dp))

                if (isSubmittedToday) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radius.Tile))
                            .background(AccentLumenSubtle)
                            .padding(12.dp)
                    ) {
                        Icon(Icons.Rounded.Check, null, tint = AccentLumen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Wieczorna refleksja zapisana (+20 XP). Dobrej nocy i regeneracji!",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentLumen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Text(
                        text = "Za co jesteś dziś wdzięczny? (1 zdanie)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = eveningNote,
                        onValueChange = { eveningNote = it },
                        placeholder = { Text("Np. Za to, że oparłem się pokusie i poszedłem na spacer.", color = TextTertiary, style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
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

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(Radius.Chip))
                            .background(if (eveningNote.isNotBlank()) AccentLumen else MidnightSurfaceCard)
                            .clickable(enabled = eveningNote.isNotBlank()) {
                                haptics.success()
                                sound.chord()
                                onSaveEveningReflection(eveningNote)
                                isSubmittedToday = true
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Zapieczętuj dzień (+20 XP)",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (eveningNote.isNotBlank()) MidnightBackground else TextTertiary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
