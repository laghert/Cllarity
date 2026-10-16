package com.cllarity.app.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
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
import com.cllarity.app.ui.theme.SemanticAlert
import com.cllarity.app.ui.theme.SemanticAlertBg
import com.cllarity.app.ui.theme.SemanticAlertBorder
import com.cllarity.app.ui.theme.TextPrimary
import com.cllarity.app.ui.theme.TextSecondary
import com.cllarity.app.ui.theme.TextTertiary
import com.cllarity.app.ui.theme.rememberHaptics
import com.cllarity.app.ui.theme.rememberSound

const val APP_VERSION_NAME = "1.1.0-zen"

data class HelplineContact(
    val name: String,
    val number: String,
    val description: String,
    val is24h: Boolean = true
)

@Composable
fun SettingsSheet(
    isPrivacyMode: Boolean,
    onTogglePrivacyMode: () -> Unit,
    isHapticsEnabled: Boolean,
    onToggleHaptics: () -> Unit,
    isSoundEnabled: Boolean = true,
    onToggleSound: () -> Unit = {},
    isBiometricLockEnabled: Boolean = false,
    onToggleBiometricLock: () -> Unit = {},
    onExportBackup: () -> String = { "" },
    onImportBackup: (String) -> Boolean = { false },
    onOpenDebugMode: () -> Unit,
    onClose: () -> Unit
) {
    val haptics = rememberHaptics()
    val sound = rememberSound(isSoundEnabled)
    val context = LocalContext.current
    var versionTapCount by remember { mutableIntStateOf(0) }
    val clipboardManager = LocalClipboardManager.current
    var exportCopiedNotice by remember { mutableStateOf(false) }
    var showImportField by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }
    var importStatusMessage by remember { mutableStateOf<String?>(null) }

    val helplines = remember {
        listOf(
            HelplineContact(
                name = "Kryzys emocjonalny i psychiczny",
                number = "116 123",
                description = "Kryzysowy Telefon Zaufania dla dorosłych • Bezpłatny i anonimowy",
                is24h = true
            ),
            HelplineContact(
                name = "Telefon Zaufania Uzależnienia (KCPU)",
                number = "800 199 990",
                description = "Wsparcie w uzależnieniach behawioralnych i chemicznych (16:00–21:00)",
                is24h = false
            ),
            HelplineContact(
                name = "Anonimowi Alkoholicy (Infolinia AA)",
                number = "801 033 242",
                description = "Całodobowe wsparcie osób walczących o trzeźwość",
                is24h = true
            ),
            HelplineContact(
                name = "Centrum Wsparcia w Kryzysie",
                number = "800 70 2222",
                description = "Całodobowa bezpłatna pomoc psychologiczna dla każdego w potrzebie",
                is24h = true
            )
        )
    }

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
                    text = "Ustawienia & Wsparcie",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Dopasuj przestrzeń do swoich zmysłów",
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

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Identity Hero Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.Card))
                        .background(MidnightSurfaceCard)
                        .border(1.dp, GlassBorderSubtle, RoundedCornerShape(Radius.Card))
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(AccentLumenSubtle)
                                .border(1.dp, GlassBorderLumen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "C",
                                style = MaterialTheme.typography.titleLarge,
                                color = AccentLumen,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Cllarity & Llumie",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Lokalna, prywatna przestrzeń regeneracji",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Sekcja: Zmysły i Interfejs
            item {
                Text(
                    text = "Zmysły i prywatność",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.Card))
                        .background(MidnightSurfaceCard)
                        .border(1.dp, GlassBorderSubtle, RoundedCornerShape(Radius.Card))
                ) {
                    // 1. Dźwięki sensoryczne (Zen Audio)
                    SettingsToggleRow(
                        icon = Icons.AutoMirrored.Rounded.VolumeUp,
                        title = "Dźwięki sensoryczne (Zen Audio)",
                        subtitle = "Aksamitny klik i drewniane uderzenie przy akcjach",
                        checked = isSoundEnabled,
                        onCheckedChange = {
                            onToggleSound()
                            if (!isSoundEnabled) sound.woodblock()
                        }
                    )

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(GlassBorderSubtle))

                    // 2. Haptyka Taptic Engine
                    SettingsToggleRow(
                        icon = Icons.Rounded.Vibration,
                        title = "Wibracje i haptyka",
                        subtitle = "Fizyczne sprzężenie dotykowe pod palcem",
                        checked = isHapticsEnabled,
                        onCheckedChange = {
                            onToggleHaptics()
                            haptics.click()
                        }
                    )

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(GlassBorderSubtle))

                    // 3. Tryb Dyskretny
                    SettingsToggleRow(
                        icon = Icons.Rounded.Visibility,
                        title = "Tryb dyskretny",
                        subtitle = "Chroni intymne nazwy nałogów w miejscach publicznych",
                        checked = isPrivacyMode,
                        onCheckedChange = {
                            haptics.click()
                            onTogglePrivacyMode()
                        }
                    )

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(GlassBorderSubtle))

                    // 4. Blokada Biometryczna
                    SettingsToggleRow(
                        icon = Icons.Rounded.Fingerprint,
                        title = "Blokada biometryczna / PIN",
                        subtitle = "Wymagaj autoryzacji przy każdym wejściu do aplikacji",
                        checked = isBiometricLockEnabled,
                        onCheckedChange = {
                            haptics.click()
                            onToggleBiometricLock()
                        }
                    )
                }
            }

            // =================================================================
            // SEKCJA: NUMERY ZAUFANIA I WSPARCIE KRYZYSOWE (Punkt 3 użytkownika)
            // =================================================================
            item {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Phone,
                            contentDescription = null,
                            tint = SemanticAlert,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Telefony zaufania & Pomoc w kryzysie",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Jeśli fala Cię przerasta – nie jesteś sam. Dotknij, aby zadzwonić bezpłatnie i anonimowo:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radius.Card))
                            .background(MidnightSurfaceCard)
                            .border(1.dp, SemanticAlertBorder.copy(alpha = 0.35f), RoundedCornerShape(Radius.Card))
                    ) {
                        helplines.forEachIndexed { index, contact ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        haptics.click()
                                        try {
                                            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                                data = Uri.parse("tel:${contact.number.replace(" ", "")}")
                                            }
                                            context.startActivity(dialIntent)
                                        } catch (_: Exception) {}
                                    }
                                    .padding(horizontal = 16.dp, vertical = 13.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = contact.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = contact.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextTertiary,
                                        lineHeight = 15.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (contact.is24h) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(Radius.Badge))
                                                .background(SemanticAlertBg)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "24/7",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SemanticAlert,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(Radius.Chip))
                                            .background(SemanticAlertBg)
                                            .border(1.dp, SemanticAlertBorder, RoundedCornerShape(Radius.Chip))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Rounded.Call,
                                                contentDescription = "Zadzwoń",
                                                tint = SemanticAlert,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = contact.number,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SemanticAlert,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            if (index < helplines.size - 1) {
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(GlassBorderSubtle))
                            }
                        }
                    }
                }
            }

            // Kopia zapasowa (Backup & Restore)
            item {
                Text(
                    text = "Kopia zapasowa i bezpieczeństwo danych",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.Card))
                        .background(MidnightSurfaceCard)
                        .border(1.dp, GlassBorderSubtle, RoundedCornerShape(Radius.Card))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Eksportuj kopię JSON",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Kopiuje całą bazę do bezpiecznego schowka",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextTertiary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(Radius.Chip))
                                    .background(AccentLumen)
                                    .clickable {
                                        haptics.success()
                                        sound.tap()
                                        val json = onExportBackup()
                                        clipboardManager.setText(AnnotatedString(json))
                                        exportCopiedNotice = true
                                    }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.ContentCopy, null, tint = MidnightBackground, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Kopiuj", style = MaterialTheme.typography.labelSmall, color = MidnightBackground, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (exportCopiedNotice) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "✓ Skopiowano bazę! Zapisz ją w bezpiecznym notesie.",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentLumen
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(GlassBorderSubtle))
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Przywróć dane z kopii",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Wklej kod JSON ze schowka",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextTertiary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(Radius.Chip))
                                    .background(MidnightSurfaceElevated)
                                    .border(1.dp, GlassBorderSubtle, RoundedCornerShape(Radius.Chip))
                                    .clickable { showImportField = !showImportField }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = if (showImportField) "Schowaj" else "Wklej",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        AnimatedVisibility(visible = showImportField) {
                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                OutlinedTextField(
                                    value = importJsonText,
                                    onValueChange = { importJsonText = it },
                                    placeholder = { Text("Wklej tutaj JSON...", color = TextTertiary, style = MaterialTheme.typography.bodyMedium) },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 3,
                                    shape = RoundedCornerShape(Radius.Tile),
                                    colors = OutlinedTextFieldDefaults.colors(
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
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(Radius.Chip))
                                        .background(if (importJsonText.isNotBlank()) AccentLumen else MidnightSurfaceElevated)
                                        .clickable(enabled = importJsonText.isNotBlank()) {
                                            val ok = onImportBackup(importJsonText)
                                            if (ok) {
                                                haptics.success()
                                                sound.chord()
                                                importStatusMessage = "Kopia przywrócona pomyślnie!"
                                                importJsonText = ""
                                            } else {
                                                haptics.reject()
                                                importStatusMessage = "Nieprawidłowy format JSON."
                                            }
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Wczytaj i zastąp dane",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (importJsonText.isNotBlank()) MidnightBackground else TextTertiary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (importStatusMessage != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = importStatusMessage ?: "",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AccentLumen
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Wersja i Easter Egg Debug Mode
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.Card))
                        .background(MidnightSurfaceCard)
                        .border(1.dp, GlassBorderSubtle, RoundedCornerShape(Radius.Card))
                        .clickable {
                            versionTapCount++
                            haptics.tick()
                            sound.tap()
                            if (versionTapCount >= 3) {
                                versionTapCount = 0
                                sound.chord()
                                onOpenDebugMode()
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Info, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Wersja aplikacji", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = if (versionTapCount > 0) "Dotknij jeszcze ${3 - versionTapCount}x do trybu Debug" else "Cllarity for Android",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (versionTapCount > 0) AccentLumen else TextTertiary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(Radius.Badge))
                            .background(MidnightSurfaceElevated)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = APP_VERSION_NAME,
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentLumen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (checked) AccentLumen else TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Switch(
            checked = checked,
            onCheckedChange = { onCheckedChange() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MidnightBackground,
                checkedTrackColor = AccentLumen,
                uncheckedThumbColor = TextTertiary,
                uncheckedTrackColor = MidnightSurfaceElevated,
                uncheckedBorderColor = GlassBorderSubtle
            )
        )
    }
}
