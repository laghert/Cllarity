package com.cllarity.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Flare
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.DryCleaning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cllarity.app.model.ClothingColor
import com.cllarity.app.model.CosmeticCategory
import com.cllarity.app.model.CosmeticSlot
import com.cllarity.app.model.LlumieCosmetic
import com.cllarity.app.model.LlumieState
import com.cllarity.app.ui.theme.AccentAmber
import com.cllarity.app.ui.theme.LabelTiny
import com.cllarity.app.ui.theme.AccentEmerald
import com.cllarity.app.ui.theme.AccentIndigo
import com.cllarity.app.ui.theme.AccentIndigoLight
import com.cllarity.app.ui.theme.AccentLumen
import com.cllarity.app.ui.theme.AccentLumenSubtle
import com.cllarity.app.ui.theme.GlassBorder
import com.cllarity.app.ui.theme.GlassBorderSubtle
import com.cllarity.app.ui.theme.GlassBorderLumen
import com.cllarity.app.ui.theme.MidnightSurfaceCard
import com.cllarity.app.ui.theme.MidnightSurfaceElevated
import com.cllarity.app.ui.theme.Motion
import com.cllarity.app.ui.theme.Radius
import com.cllarity.app.ui.theme.TextPrimary
import com.cllarity.app.ui.theme.TextSecondary
import com.cllarity.app.ui.theme.TextTertiary
import com.cllarity.app.ui.theme.rememberHaptics
import com.cllarity.app.ui.theme.rememberSound
/**
 * Ekran Personalizacji Llumie podzielony na 3 kategorie:
 * 1. Nakrycia głowy (czapki, kapelusze, korony)
 * 2. Ubranka (sweterek, t-shirt, bluza - z wyborem koloru!)
 * 3. Dodatki (szalik, pasek, okulary, naklejki, muszka, dzwoneczek, aura)
 *
 * Układ: 3 kolumny kafelków z ikoną w środku, mały kafelek Lvl w lewym dolnym rogu
 * oraz pełne mieszanie rzeczy (mix & match)!
 */
@Composable
fun LlumieCustomizationSheet(
    llumie: LlumieState,
    onEquipCosmetic: (LlumieCosmetic) -> Unit,
    onSetClothingColor: (ClothingColor) -> Unit = {},
    onClose: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val haptics = rememberHaptics()
    val sound = rememberSound(true)
    val scrollState = rememberScrollState()
    // Aktywna kategoria zakładek (Nakrycia głowy, Ubranka, Dodatki)
    var selectedCategory by remember { mutableStateOf(CosmeticCategory.HEADWEAR) }

    // Aktywny zestaw założonych dodatków (mieszanie rzeczy!)
    var activeSet by remember(llumie.activeCosmetics) {
        mutableStateOf(llumie.activeCosmetics)
    }

    // Wybrany kolor ubranek
    var currentClothingColor by remember(llumie.clothingColor) {
        mutableStateOf(llumie.clothingColor)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        // =====================================================================
        // 1. NAGŁÓWEK EKRANU
        // =====================================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Palette,
                        contentDescription = null,
                        tint = AccentIndigoLight,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Garderoba Llumie",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Mieszaj czapki, ubranka i dodatki",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
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

        Spacer(modifier = Modifier.height(12.dp))

        // =====================================================================
        // 2. SCENA PODGLĄDU NA ŻYWO (LIVE STAGE - POKAZUJE ZMIKSOWANE ELEMENTY)
        // =====================================================================
        AppleCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Radius.Card),
            backgroundColor = MidnightSurfaceElevated
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Podgląd maskotki ze wszystkimi zmiksowanymi dodatkami i fizycznym squash & stretch na zmianę stroju
                var costumeSwitchCount by remember { mutableIntStateOf(0) }
                val bounceScale by animateFloatAsState(
                    targetValue = if (costumeSwitchCount % 2 == 1) 1.05f else 1.0f,
                    animationSpec = Motion.PressSpec,
                    label = "wardrobeBounce"
                )

                Box(
                    modifier = Modifier.scale(bounceScale),
                    contentAlignment = Alignment.Center
                ) {
                    LlumieCanvas(
                        canvasSize = 180.dp,
                        mood = llumie.mood,
                        cosmetics = activeSet,
                        clothingColor = currentClothingColor,
                        level = llumie.level,
                        streakDays = llumie.streakDays,
                        onTap = {
                            costumeSwitchCount++
                            haptics.tick()
                            sound.tap()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Podsumowanie założonych dodatków z ciepłą, świetlistą aureolą AccentLumen
                val activeCount = activeSet.size
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.Pill))
                        .background(if (activeCount > 0) AccentLumenSubtle else MidnightSurfaceCard)
                        .border(
                            1.dp,
                            if (activeCount > 0) GlassBorderLumen else GlassBorderSubtle,
                            RoundedCornerShape(Radius.Pill)
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (activeCount == 0) "Czysty duszek (brak dodatków)"
                        else "Założone ($activeCount): " + activeSet.joinToString(", ") { it.displayName },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (activeCount > 0) AccentLumen else TextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (activeCount > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(Radius.Badge))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                activeSet = emptySet()
                                onEquipCosmetic(LlumieCosmetic.NONE)
                            }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Zdejmij wszystko",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // =====================================================================
        // 3. ZAKŁADKI KATEGORII (Apple Gliding Pill Style)
        // =====================================================================
        val categories = CosmeticCategory.values()
        val selectedIndex = categories.indexOf(selectedCategory).coerceAtLeast(0)
        val animatedTabFraction by animateFloatAsState(
            targetValue = selectedIndex.toFloat(),
            animationSpec = Motion.GlidingPillFractionSpec,
            label = "glidingPill"
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(Radius.Tile))
                .background(MidnightSurfaceCard)
                .padding(4.dp)
        ) {
            val tabWidth = maxWidth / categories.size.toFloat()

            // Apple Gliding Spring Pill Indicator
            Box(
                modifier = Modifier
                    .offset(x = tabWidth * animatedTabFraction)
                    .width(tabWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(Radius.Chip))
                    .background(
                        Brush.horizontalGradient(
                            listOf(AccentIndigo, AccentIndigoLight)
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(Radius.Chip))
            )

            Row(modifier = Modifier.fillMaxSize()) {
                categories.forEachIndexed { index, category ->
                    val isSelected = category == selectedCategory
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(Radius.Chip))
                            .clickable {
                                haptics.tick()
                                selectedCategory = category
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category.title,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) Color.White else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // =====================================================================
        // 4. PALETA KOLORÓW DLA UBRANEK (Color Picker)
        // =====================================================================
        val hasClothingActive = activeSet.any { it.category == CosmeticCategory.CLOTHING }
        val hasClothingUnlocked = LlumieCosmetic.values().any { it.category == CosmeticCategory.CLOTHING && llumie.isUnlocked(it) }
        if (hasClothingActive || (selectedCategory == CosmeticCategory.CLOTHING && hasClothingUnlocked)) {
            Spacer(modifier = Modifier.height(12.dp))
            AppleCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.Tile),
                backgroundColor = MidnightSurfaceElevated
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kolor Ubranka:",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentClothingColor.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentIndigoLight,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Rząd kółek z kolorami do wyboru
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ClothingColor.values().forEach { color ->
                            val isColorSelected = color == currentClothingColor
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(color.hexColor))
                                    .border(
                                        width = if (isColorSelected) 2.5.dp else 1.dp,
                                        color = if (isColorSelected) Color.White else Color(0x33FFFFFF),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        haptics.click()
                                        currentClothingColor = color
                                        onSetClothingColor(color)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isColorSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = if (color == ClothingColor.CREAM) Color.Black else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // =====================================================================
        // 5. SIATKA 3 KOLUMNY Z KAFELKAMI DLA WYBRANEJ KATEGORII
        AnimatedContent(
            targetState = selectedCategory,
            transitionSpec = { Motion.sharedAxisX(forward = targetState.ordinal > initialState.ordinal) },
            label = "CategoryTransition"
        ) { currentCat ->
            val categoryItems = remember(currentCat) {
                LlumieCosmetic.values().filter { it.category == currentCat }
            }
            val rows = remember(categoryItems) {
                categoryItems.chunked(3)
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rows.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { cosmetic ->
                            val isUnlocked = llumie.isUnlocked(cosmetic)
                            val isEquipped = if (cosmetic == LlumieCosmetic.NONE) {
                                activeSet.none { it.slot == CosmeticSlot.HEAD }
                            } else {
                                activeSet.contains(cosmetic)
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                CosmeticGridTile(
                                    cosmetic = cosmetic,
                                    isUnlocked = isUnlocked,
                                    isEquipped = isEquipped,
                                    onClick = {
                                        if (cosmetic == LlumieCosmetic.NONE) {
                                            haptics.tick()
                                            activeSet = activeSet.filter { it.slot != CosmeticSlot.HEAD }.toSet()
                                            onEquipCosmetic(LlumieCosmetic.NONE)
                                        } else if (activeSet.contains(cosmetic)) {
                                            haptics.tick()
                                            val newSet = activeSet - cosmetic
                                            activeSet = newSet
                                            onEquipCosmetic(cosmetic)
                                        } else if (isUnlocked) {
                                            haptics.click()
                                            val conflicting = activeSet.filter { it.slot == cosmetic.slot }
                                            val newSet = (activeSet - conflicting.toSet()) + cosmetic
                                            activeSet = newSet
                                            onEquipCosmetic(cosmetic)
                                        } else {
                                            // Przedmiot zablokowany: odrzucenie haptyczne w stylu Apple
                                            haptics.reject()
                                        }
                                    }
                                )
                            }
                        }

                        if (rowItems.size < 3) {
                            repeat(3 - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

/**
 * Pojedynczy kafelek w 3-kolumnowej siatce:
 * - Ikona w środku
 * - Mały kafelek z wymaganym poziomem w lewym dolnym rogu
 * - Checkmark w prawym górnym rogu
 */
@Composable
private fun CosmeticGridTile(
    cosmetic: LlumieCosmetic,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    onClick: () -> Unit
) {
    val tileBg = when {
        isEquipped -> MidnightSurfaceCard
        isUnlocked -> MidnightSurfaceElevated
        else -> MidnightSurfaceElevated.copy(alpha = 0.40f)
    }

    val tileBorderColor = when {
        isEquipped -> AccentLumen
        isUnlocked -> GlassBorderSubtle
        else -> Color.Transparent
    }
    val icon: ImageVector = when (cosmetic) {
        LlumieCosmetic.NONE -> Icons.Rounded.Flare
        LlumieCosmetic.SPROUT_LEAF -> Icons.Rounded.Spa
        LlumieCosmetic.BEANIE_HAT -> Icons.Rounded.Checkroom
        LlumieCosmetic.BASEBALL_CAP -> Icons.Rounded.AutoAwesome
        LlumieCosmetic.WIZARD_HAT -> Icons.Rounded.AutoAwesome
        LlumieCosmetic.GLOWING_HALO -> Icons.Rounded.LightMode
        LlumieCosmetic.ROYAL_CROWN -> Icons.Rounded.WorkspacePremium
        LlumieCosmetic.BERET_HAT -> Icons.Rounded.Palette
        LlumieCosmetic.OLIVE_WREATH -> Icons.Rounded.Spa
        LlumieCosmetic.SWEATER -> Icons.Rounded.Checkroom
        LlumieCosmetic.TSHIRT -> Icons.Rounded.Checkroom
        LlumieCosmetic.HOODIE -> Icons.Rounded.Checkroom
        LlumieCosmetic.STOIC_CAPE -> Icons.Rounded.DryCleaning
        LlumieCosmetic.ZEN_KIMONO -> Icons.Rounded.DryCleaning
        LlumieCosmetic.MAGE_ROBE -> Icons.Rounded.AutoAwesome
        LlumieCosmetic.GOLDEN_ARMOR -> Icons.Rounded.Shield
        LlumieCosmetic.CHEEK_STICKERS -> Icons.Rounded.Favorite
        LlumieCosmetic.COZY_SCARF -> Icons.Rounded.Checkroom
        LlumieCosmetic.LEATHER_BELT -> Icons.Rounded.AutoAwesome
        LlumieCosmetic.COOL_SHADES -> Icons.Rounded.Visibility
        LlumieCosmetic.TRAVEL_BACKPACK -> Icons.Rounded.Shield
        LlumieCosmetic.BOW_TIE -> Icons.Rounded.AutoAwesome
        LlumieCosmetic.BELL_COLLAR -> Icons.Rounded.Notifications
        LlumieCosmetic.AURA_STARS -> Icons.Rounded.Flare
        LlumieCosmetic.GOLDEN_FLAME_RELIC -> Icons.Rounded.LocalFireDepartment
        LlumieCosmetic.REBIRTH_MEDALLION -> Icons.Rounded.WorkspacePremium
        LlumieCosmetic.PHOENIX_WINGS -> Icons.Rounded.AutoAwesome
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.92f)
            .clip(RoundedCornerShape(Radius.Tile))
            .background(tileBg)
            .border(
                width = if (isEquipped) 1.8.dp else 1.dp,
                color = tileBorderColor,
                shape = RoundedCornerShape(Radius.Tile)
            )
            .clickable { onClick() }
    ) {
        // =====================================================================
        // ŚRODEK KAFELKA: IKONA I NAZWA PRZEDMIOTU
        // =====================================================================
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 10.dp, bottom = 22.dp, start = 6.dp, end = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isEquipped) AccentLumenSubtle
                        else if (isUnlocked) MidnightSurfaceCard
                        else Color(0x0AFFFFFF)
                    )
                    .border(
                        1.dp,
                        if (isEquipped) GlassBorderLumen
                        else if (isUnlocked) GlassBorderSubtle
                        else Color.Transparent,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isEquipped) AccentLumen else if (isUnlocked) TextPrimary else TextTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = cosmetic.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = if (isEquipped) Color.White else if (isUnlocked) TextPrimary else TextSecondary,
                fontWeight = if (isEquipped) FontWeight.Bold else FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // =====================================================================
        // LEWY DOLNY RÓG: MAŁY KAFELEK Z WYMAGANYM LEVELIEM!
        // =====================================================================
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 7.dp, bottom = 7.dp)
                .clip(RoundedCornerShape(Radius.Badge))
                .background(
                    if (!isUnlocked) Color(0x2AF59E0B)
                    else Color(0x1CFFFFFF)
                )
                .border(
                    width = 0.8.dp,
                    color = if (!isUnlocked) AccentAmber.copy(alpha = 0.45f) else Color(0x22FFFFFF),
                    shape = RoundedCornerShape(Radius.Badge)
                )
                .padding(horizontal = 5.dp, vertical = 2.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val isStreakRelic = cosmetic.requiredStreakDays > 0
                val reqText = if (isStreakRelic) "🔥 ${cosmetic.requiredStreakDays}d" else "Lvl ${cosmetic.unlockLevel}"
                if (!isUnlocked) {
                    Icon(
                        imageVector = if (isStreakRelic) Icons.Rounded.LocalFireDepartment else Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = AccentAmber,
                        modifier = Modifier.size(8.5.dp)
                    )
                    Spacer(modifier = Modifier.width(2.5.dp))
                    Text(
                        text = reqText,
                        style = LabelTiny,
                        color = AccentAmber,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = reqText,
                        style = LabelTiny,
                        color = TextTertiary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // =====================================================================
        // PRAWY GÓRNY RÓG: WSKAŹNIK ZAŁOŻENIA (CHECK BADGE)
        // =====================================================================
        if (isEquipped) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 7.dp, end = 7.dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(AccentEmerald)
                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Założone",
                    tint = Color.White,
                    modifier = Modifier.size(11.dp)
                )
            }
        }
    }
}
