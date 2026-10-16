package com.cllarity.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import com.cllarity.app.data.CllarityRepository
import com.cllarity.app.model.LlumieCosmetic
import com.cllarity.app.model.LlumieMood
import com.cllarity.app.model.LlumieState
import com.cllarity.app.ui.components.AddAddictionSheet
import com.cllarity.app.ui.components.AddHabitSheet
import com.cllarity.app.ui.components.AppleCard
import com.cllarity.app.ui.components.BiometricLockOverlay
import com.cllarity.app.ui.components.CllarityBottomSheet
import com.cllarity.app.ui.components.DebugSheet
import com.cllarity.app.ui.components.LlumieCanvas
import com.cllarity.app.ui.components.OnboardingGuideOverlay
import com.cllarity.app.ui.components.LlumieCustomizationSheet
import com.cllarity.app.ui.components.CelebrationConfetti
import com.cllarity.app.ui.components.DailyStoicAffirmation
import com.cllarity.app.ui.components.UrgeSurfingSheet
import com.cllarity.app.ui.components.LevelUpDialog
import com.cllarity.app.ui.components.SettingsSheet
import com.cllarity.app.ui.theme.AccentLumen
import com.cllarity.app.ui.theme.AccentLumenGlow
import com.cllarity.app.ui.theme.AccentLumenLight
import com.cllarity.app.ui.theme.AccentLumenSubtle
import com.cllarity.app.ui.theme.GlassBorder
import com.cllarity.app.ui.theme.GlassBorderSubtle
import com.cllarity.app.ui.theme.GlassBorderLumen
import com.cllarity.app.ui.theme.MidnightBackground
import com.cllarity.app.ui.theme.MidnightSurfaceCard
import com.cllarity.app.ui.theme.MidnightSurfaceElevated
import com.cllarity.app.ui.theme.MidnightSurfaceHighlight
import com.cllarity.app.ui.theme.Motion
import com.cllarity.app.ui.theme.Radius
import com.cllarity.app.ui.theme.SemanticAlert
import com.cllarity.app.ui.theme.SemanticAlertBg
import com.cllarity.app.ui.theme.SemanticAlertBorder
import com.cllarity.app.ui.theme.TextPrimary
import com.cllarity.app.ui.theme.TextSecondary
import com.cllarity.app.ui.theme.TextTertiary
import com.cllarity.app.ui.theme.rememberHaptics
import com.cllarity.app.ui.theme.rememberSound
enum class MainTab(val title: String, val icon: ImageVector) {
    LLUMIE("Llumie", Icons.Rounded.Spa),
    HABITS("Nawyki", Icons.AutoMirrored.Rounded.FormatListBulleted),
    ADDICTIONS("Nałogi", Icons.Rounded.Security)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: CllarityRepository,
    modifier: Modifier = Modifier
) {
    val allHabits by repository.habits.collectAsState()
    val allAddictions by repository.addictions.collectAsState()
    val llumie by repository.llumie.collectAsState()
    val isPrivacyMode by repository.isPrivacyMode.collectAsState()
    val isHapticsEnabled by repository.isHapticsEnabled.collectAsState()
    val isSoundEnabled by repository.isSoundEnabled.collectAsState()
    val isBiometricLockEnabled by repository.isBiometricLockEnabled.collectAsState()
    var isAppUnlockedByBiometrics by remember { mutableStateOf(!isBiometricLockEnabled) }
    val isOnboardingCompleted by repository.isOnboardingCompleted.collectAsState()
    val crisisLetter by repository.crisisLetter.collectAsState()
    val lastEveningReflectionDate by repository.lastEveningReflectionDate.collectAsState()
    val isEveningDone = remember(lastEveningReflectionDate) { repository.isEveningReflectionDoneToday() }
    var showManualSetupGuide by remember { mutableStateOf(false) }
    val activeHabits = remember(allHabits) { allHabits.filter { it.isActive } }
    val activeAddictions = remember(allAddictions) { allAddictions.filter { it.isActive } }
    var currentTab by remember { mutableStateOf(MainTab.LLUMIE) }
    var showAddHabitSheet by remember { mutableStateOf(false) }
    var showAddAddictionSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showDebugSheet by remember { mutableStateOf(false) }
    var showCustomizationSheet by remember { mutableStateOf(false) }
    var showUrgeSurfingSheet by remember { mutableStateOf(false) }
    val customizationSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val habitSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val addictionSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = LocalHapticFeedback.current
    val haptics = rememberHaptics()
    val sound = rememberSound(isSoundEnabled)
    val lastPledgeDate by repository.lastPledgeDate.collectAsState()
    val isPledgedToday = remember(lastPledgeDate) { repository.isPledgedToday() }
    var showConfetti by remember { mutableStateOf(false) }
    val allHabitsDone = remember(activeHabits) {
        activeHabits.isNotEmpty() && activeHabits.all { it.isCompletedToday }
    }
    var previousAllDone by remember { mutableStateOf(false) }

    var previousLevel by rememberSaveable { mutableIntStateOf(llumie.level) }
    var levelUpDialogLevel by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(llumie.level) {
        if (llumie.level > previousLevel) {
            levelUpDialogLevel = llumie.level
            showConfetti = true
            if (isHapticsEnabled) {
                haptics.success()
            }
        }
        previousLevel = llumie.level
    }
    LaunchedEffect(allHabitsDone) {
        if (allHabitsDone && !previousAllDone) {
            showConfetti = true
            if (isHapticsEnabled) {
                haptics.success()
            }
        }
        previousAllDone = allHabitsDone
    }

    // Automatyczny sprawdzacz zmiany dnia (gdy aplikacja jest otwarta przez północ)
    LaunchedEffect(Unit) {
        while (isActive) {
            repository.checkDayRollover()
            delay(60_000)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightBackground)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MidnightBackground,
        bottomBar = {
            AppleGlidingTabBar(
                currentTab = currentTab,
                onTabSelect = {
                    repository.checkDayRollover()
                    currentTab = it
                },
                habitsPendingCount = activeHabits.count { !it.isCompletedToday }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            // Top App Bar
            TopBarHeader(
                llumie = llumie,
                onOpenSettings = { showSettingsSheet = true },
                onLevelClick = { showCustomizationSheet = true },
                onOpenSOS = { showUrgeSurfingSheet = true }
            )

            // Animated Tab Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = { Motion.fadeThrough() },
                    label = "TabContentTransition"
                ) { targetState ->
                    when (targetState) {
                        MainTab.LLUMIE -> LlumieHeroScreen(
                            llumie = llumie,
                            habitsCompleted = activeHabits.count { it.isCompletedToday },
                            habitsTotal = activeHabits.size,
                            addictionsTracked = activeAddictions.size,
                            isPledgedToday = isPledgedToday,
                            isEveningDone = isEveningDone,
                            onSaveEveningReflection = { repository.saveEveningReflection(it) },
                            onOpenCustomization = {
                                sound.tap()
                                showCustomizationSheet = true
                            },
                            onPledgeDone = {
                                sound.chord()
                                repository.makeDailyPledge()
                            },
                            onNavigateToHabits = {
                                sound.tap()
                                currentTab = MainTab.HABITS
                            },
                            onNavigateToAddictions = {
                                sound.tap()
                                currentTab = MainTab.ADDICTIONS
                            }
                        )
                        MainTab.HABITS -> HabitsScreen(
                            habits = activeHabits,
                            isHapticsEnabled = isHapticsEnabled,
                            isSoundEnabled = isSoundEnabled,
                            onToggleHabit = { repository.toggleHabit(it) },
                            onIncrementHabit = { repository.incrementHabitProgress(it) },
                            onDecrementHabit = { repository.decrementHabitProgress(it) },
                            onAddHabitClick = { showAddHabitSheet = true },
                            onRemoveHabit = { repository.deactivateHabit(it) }
                        )
                        MainTab.ADDICTIONS -> AddictionsScreen(
                            addictions = activeAddictions,
                            isPrivacyMode = isPrivacyMode,
                            isHapticsEnabled = isHapticsEnabled,
                            onTogglePrivacyMode = { repository.togglePrivacyMode() },
                            onResetAddiction = { id, trigger, note ->
                                repository.resetAddiction(id, trigger, note)
                            },
                            onAddAddictionClick = { showAddAddictionSheet = true },
                            onRemoveAddiction = { repository.deactivateAddiction(it) },
                            onOpenSOS = { showUrgeSurfingSheet = true }
                        )
                    }
                }
            }
        }
    }

        // Full screen celebratory confetti overlay (floats on top without affecting UI layout)
        if (showConfetti) {
            CelebrationConfetti(
                trigger = showConfetti,
                modifier = Modifier.fillMaxSize(),
                onFinished = { showConfetti = false }
            )
        }

        // Ekran pełnej blokady biometrycznej (jeśli włączona w Ustawieniach)
        if (isBiometricLockEnabled && !isAppUnlockedByBiometrics) {
            BiometricLockOverlay(
                isLocked = !isAppUnlockedByBiometrics,
                onUnlockSuccess = { isAppUnlockedByBiometrics = true }
            )
        }

        // Ekran startowego przewodnika Setup Guide (przy 1. uruchomieniu lub z Debug)
        if (!isOnboardingCompleted || showManualSetupGuide) {
            OnboardingGuideOverlay(
                onComplete = {
                    repository.completeOnboarding()
                    showManualSetupGuide = false
                }
            )
        }
    }
    // Dialog awansu na kolejny poziom Llumie
    levelUpDialogLevel?.let { lvl ->
        val unlockedAtLevel = remember(lvl) {
            LlumieCosmetic.values().filter { it.unlockLevel == lvl }
        }
        LevelUpDialog(
            newLevel = lvl,
            unlockedItems = unlockedAtLevel,
            onOpenWardrobe = {
                levelUpDialogLevel = null
                showCustomizationSheet = true
            },
            onDismiss = { levelUpDialogLevel = null }
        )
    }

    // Add Habit Sheet
    if (showAddHabitSheet) {
        CllarityBottomSheet(
            onDismissRequest = { showAddHabitSheet = false },
            sheetState = habitSheetState
        ) {
            AddHabitSheet(
                allHabits = allHabits,
                onActivateHabit = { repository.activateHabit(it) },
                onDeactivateHabit = { repository.deactivateHabit(it) },
                onCreateCustomHabit = { name, desc, cat, diff, freq, target, unit, days ->
                    repository.addCustomHabit(name, desc, cat, diff, freq, target, unit, days)
                },
                onClose = { showAddHabitSheet = false }
            )
        }
    }

    // Add Addiction Sheet
    if (showAddAddictionSheet) {
        CllarityBottomSheet(
            onDismissRequest = { showAddAddictionSheet = false },
            sheetState = addictionSheetState
        ) {
            AddAddictionSheet(
                allAddictions = allAddictions,
                onActivateAddiction = { repository.activateAddiction(it) },
                onDeactivateAddiction = { repository.deactivateAddiction(it) },
                onCreateCustomAddiction = { name, tag, icon, start, cost, hrs, disc ->
                    repository.addCustomAddiction(name, tag, icon, start, cost, hrs, disc)
                },
                onClose = { showAddAddictionSheet = false }
            )
        }
    }

    // Settings Sheet
    if (showSettingsSheet) {
        CllarityBottomSheet(
            onDismissRequest = { showSettingsSheet = false }
        ) {
            SettingsSheet(
                isPrivacyMode = isPrivacyMode,
                onTogglePrivacyMode = { repository.togglePrivacyMode() },
                isHapticsEnabled = isHapticsEnabled,
                onToggleHaptics = { repository.toggleHaptics() },
                isSoundEnabled = isSoundEnabled,
                onToggleSound = { repository.toggleSound() },
                isBiometricLockEnabled = isBiometricLockEnabled,
                onToggleBiometricLock = { repository.toggleBiometricLock() },
                onExportBackup = { repository.exportBackupJson() },
                onImportBackup = { repository.importBackupJson(it) },
                onOpenDebugMode = {
                    showSettingsSheet = false
                    showDebugSheet = true
                },
                onClose = { showSettingsSheet = false }
            )
        }
    }

    // Debug Lab Sheet
    if (showDebugSheet) {
        CllarityBottomSheet(
            onDismissRequest = { showDebugSheet = false }
        ) {
            DebugSheet(
                llumie = llumie,
                activeAddictions = activeAddictions,
                onSetLevel = { repository.debugSetLlumieLevel(it) },
                onSetStreak = { repository.debugSetLlumieStreak(it) },
                onAddXp = { repository.addXp(it) },
                onSetMood = { repository.debugSetLlumieMood(it) },
                onEquipCosmetic = { repository.debugForceCosmetic(it) },
                onFastForwardAddiction = { id, days -> repository.debugAddSoberDays(id, days) },
                onResetAddictionToZero = { id -> repository.resetAddiction(id, "Debug reset", null) },
                onCompleteAllHabits = { repository.debugCompleteAllHabits() },
                onResetTodayHabits = { repository.debugResetTodayHabits() },
                onResetAllData = { repository.debugResetAllData() },
                onLaunchSetup = { showManualSetupGuide = true },
                onClose = { showDebugSheet = false }
            )
        }
    }

    // Llumie Customization Sheet (Level-Gated Wardrobe Menu)
    if (showCustomizationSheet) {
        CllarityBottomSheet(
            onDismissRequest = { showCustomizationSheet = false },
            sheetState = customizationSheetState
        ) {
            LlumieCustomizationSheet(
                llumie = llumie,
                onEquipCosmetic = { repository.equipCosmetic(it) },
                onSetClothingColor = { repository.setClothingColor(it) },
                onClose = { showCustomizationSheet = false }
            )
        }
    }
    // SOS Urge Surfing Sheet (Box Breathing & Cravings Rescue)
    if (showUrgeSurfingSheet) {
        CllarityBottomSheet(
            onDismissRequest = { showUrgeSurfingSheet = false }
        ) {
            UrgeSurfingSheet(
                crisisLetter = crisisLetter,
                onSaveCrisisLetter = { repository.saveCrisisLetter(it) },
                onResistSuccess = {
                    repository.recordUrgeResisted(25)
                    showUrgeSurfingSheet = false
                    showConfetti = true
                },
                onClose = { showUrgeSurfingSheet = false }
            )
        }
    }
}

@Composable
private fun TopBarHeader(
    llumie: LlumieState,
    onOpenSettings: () -> Unit,
    onLevelClick: () -> Unit,
    onOpenSOS: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Cllarity",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.6).sp,
                    maxLines = 1,
                    softWrap = false
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(AccentLumen)
                )
            }
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = "Świadome życie & regeneracja",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.width(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // SOS Emergency Button – jedyny wyrazisty, stonowany akcent ratunkowy
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.Chip))
                    .background(SemanticAlertBg)
                    .border(1.dp, SemanticAlertBorder, RoundedCornerShape(Radius.Chip))
                    .clickable { onOpenSOS() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Security,
                    contentDescription = "SOS Pokusa",
                    tint = SemanticAlert,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "SOS",
                    style = MaterialTheme.typography.labelSmall,
                    color = SemanticAlert,
                    fontWeight = FontWeight.Bold
                )
            }

            // Level Chip – spokojny, tonalny, klikalny (otwiera garderobę)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.Chip))
                    .background(MidnightSurfaceElevated)
                    .border(1.dp, GlassBorderSubtle, RoundedCornerShape(Radius.Chip))
                    .clickable { onLevelClick() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = AccentLumen,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Lvl ${llumie.level}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false
                )
            }

            // Settings icon
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Ustawienia",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun LlumieHeroScreen(
    llumie: LlumieState,
    habitsCompleted: Int,
    habitsTotal: Int,
    addictionsTracked: Int,
    isPledgedToday: Boolean,
    isEveningDone: Boolean,
    onSaveEveningReflection: (String) -> Unit,
    onOpenCustomization: () -> Unit,
    onPledgeDone: () -> Unit,
    onNavigateToHabits: () -> Unit,
    onNavigateToAddictions: () -> Unit
) {
    var bubbleTapCount by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 140.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Companion Hero Stage ze świetlistym ambient glow w tle
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                // Delikatny, uspokajający promień światła (Lumen Ambient Glow) za Llumie
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .offset(y = 30.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    AccentLumenGlow,
                                    Color.Transparent
                                )
                            )
                        )
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    // Speech Bubble with Mood (Płynna Apple spring animacja tekstu)
                    val heroMessage = llumie.getDynamicMessage(petCount = bubbleTapCount)
                    AnimatedContent(
                        targetState = heroMessage,
                        transitionSpec = { Motion.crossfade() },
                        label = "llumieSpeechAnim"
                    ) { msg ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(Radius.Tile))
                                .background(MidnightSurfaceElevated)
                                .border(1.dp, GlassBorderLumen.copy(alpha = 0.25f), RoundedCornerShape(Radius.Tile))
                                .clickable { bubbleTapCount++ }
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                textAlign = TextAlign.Center,
                                lineHeight = 19.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Interactive Canvas Creature
                    LlumieCanvas(
                        canvasSize = 205.dp,
                        mood = llumie.mood,
                        cosmetics = llumie.activeCosmetics,
                        clothingColor = llumie.clothingColor,
                        level = llumie.level,
                        streakDays = llumie.streakDays,
                        onTap = {
                            bubbleTapCount++
                        },
                        onPet = {
                            bubbleTapCount++
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Główna metryka: Dni dyscypliny / Streak
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocalFireDepartment,
                            contentDescription = null,
                            tint = AccentLumen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (llumie.streakDays == 1) "1 dzień dyscypliny" else "${llumie.streakDays} dni dyscypliny",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${llumie.currentXp} / ${llumie.xpForNextLevel} XP do poziomu ${llumie.level + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // XP Progress Bar – czysty, subtelny pasek w kolorze AccentLumen
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(Radius.Pill))
                            .background(MidnightSurfaceCard)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = llumie.progressToNextLevel.coerceIn(0.04f, 1f))
                                .height(4.dp)
                                .clip(RoundedCornerShape(Radius.Pill))
                                .background(AccentLumen)
                        )
                    }
                }
            }
        }

        // 2. 24-Hour Pledge Card
        item {
            PledgeCard(
                pledged = isPledgedToday,
                onPledge = onPledgeDone
            )
        }

        // 2.5 Daily Stoic Affirmation & Evening Reflection
        item {
            DailyStoicAffirmation(
                isEveningDone = isEveningDone,
                onSaveEveningReflection = onSaveEveningReflection
            )
        }
        item {
            CustomizationEntryCard(
                llumie = llumie,
                onOpen = onOpenCustomization
            )
        }

        // 4. Quick Highlights (2 columns)
        // 4. Quick Highlights (2 spokojne kolumny w stylu Apple)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Habits summary card
                AppleCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(Radius.Card),
                    backgroundColor = MidnightSurfaceElevated,
                    onClick = onNavigateToHabits
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.FormatListBulleted,
                                contentDescription = null,
                                tint = AccentLumen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Nawyki",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (habitsTotal > 0) "$habitsCompleted / $habitsTotal" else "Brak",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (habitsTotal > 0) "Dzisiaj zrobione" else "Dodaj nawyki",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    }
                }

                // Addictions summary card
                AppleCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(Radius.Card),
                    backgroundColor = MidnightSurfaceElevated,
                    onClick = onNavigateToAddictions
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Security,
                                contentDescription = null,
                                tint = AccentLumen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Nałogi",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$addictionsTracked aktywne",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Trzeźwa ścieżka",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PledgeCard(
    pledged: Boolean,
    onPledge: () -> Unit
) {
    val haptics = rememberHaptics()

    AppleCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.Card),
        backgroundColor = MidnightSurfaceElevated,
        borderColor = if (pledged) GlassBorderLumen.copy(alpha = 0.5f) else GlassBorderSubtle
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (pledged) AccentLumenSubtle else MidnightSurfaceCard)
                            .border(
                                1.dp,
                                if (pledged) GlassBorderLumen else GlassBorderSubtle,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (pledged) Icons.Rounded.Check else Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = AccentLumen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (pledged) "Deklaracja 24h" else "Codzienna Deklaracja 24h",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (pledged) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(Radius.Badge))
                                        .background(AccentLumenSubtle)
                                        .border(1.dp, GlassBorderLumen.copy(alpha = 0.6f), RoundedCornerShape(Radius.Badge))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "Aktywna",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AccentLumen,
                                        fontWeight = FontWeight.Bold,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (pledged)
                                "Wspaniale! Jesteś wierny sobie do końca dnia. Odpoczywaj w spokoju (+30 XP)."
                            else
                                "Świadomie zobowiązuję się do dyscypliny i czystości na kolejne 24 godziny.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                if (!pledged) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(Radius.Chip))
                            .background(AccentLumen)
                            .clickable {
                                haptics.success()
                                onPledge()
                            }
                            .padding(horizontal = 18.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Składam",
                            style = MaterialTheme.typography.labelLarge,
                            color = MidnightBackground,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun CustomizationEntryCard(
    llumie: LlumieState,
    onOpen: () -> Unit
) {
    val unlockedCount = LlumieCosmetic.values().count { it.unlockLevel <= llumie.level }
    val totalCount = LlumieCosmetic.values().size

    AppleCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.Card),
        backgroundColor = MidnightSurfaceElevated,
        onClick = onOpen
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = AccentLumen,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Garderoba Llumie",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                val activeItems = llumie.activeCosmetics
                val subtitle = if (activeItems.isEmpty()) {
                    "$unlockedCount z $totalCount elementów dostępnych"
                } else {
                    activeItems.joinToString(", ") { it.displayName }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "Otwórz garderobę",
                tint = TextTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Apple-style Gliding Pill Tab Bar with physical spring movement.
 */
@Composable
private fun AppleGlidingTabBar(
    currentTab: MainTab,
    onTabSelect: (MainTab) -> Unit,
    habitsPendingCount: Int
) {
    val haptics = rememberHaptics()
    val tabs = MainTab.values()
    val activeIndex = tabs.indexOf(currentTab).coerceAtLeast(0)

    // Warstwa nadrzędna: Gradientowy Scrim rozmywający treść z dołu + pływający szklany dock
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        MidnightBackground.copy(alpha = 0.75f),
                        MidnightBackground.copy(alpha = 0.98f)
                    )
                )
            )
            .navigationBarsPadding()
            .padding(horizontal = 22.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        // Główny dock Apple Glass: półprzezroczysty, z podwójną krawędzią światła padającego z góry
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.Pill))
                .background(MidnightSurfaceElevated.copy(alpha = 0.88f))
                .border(
                    BorderStroke(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.22f), // Specular top highlight
                                Color.White.copy(alpha = 0.05f)  // Subtelny dół
                            )
                        )
                    ),
                    shape = RoundedCornerShape(Radius.Pill)
                )
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .padding(horizontal = 5.dp, vertical = 5.dp)
                    .fillMaxWidth()
            ) {
                val totalWidth = maxWidth
                val segmentWidth = totalWidth / tabs.size

                val animatedOffset by animateDpAsState(
                    targetValue = segmentWidth * activeIndex,
                    animationSpec = Motion.GlidingPillSpec,
                    label = "glidingPillOffset"
                )

                // Haptyczny wskaźnik szybowania z wewnętrznym miękkim podświetleniem (Tonal Glass Core)
                Box(
                    modifier = Modifier
                        .offset(x = animatedOffset)
                        .width(segmentWidth)
                        .height(44.dp)
                        .clip(RoundedCornerShape(Radius.Pill))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MidnightSurfaceCard.copy(alpha = 0.95f),
                                    MidnightSurfaceElevated.copy(alpha = 0.98f)
                                )
                            )
                        )
                        .border(
                            BorderStroke(
                                1.dp,
                                Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.25f),
                                        Color.White.copy(alpha = 0.08f)
                                    )
                                )
                            ),
                            RoundedCornerShape(Radius.Pill)
                        )
                )

                // Etykiety i ikony zakładek z fizyczną reakcją skali
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEach { tab ->
                        val isSelected = tab == currentTab

                        val tabScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.0f else 0.96f,
                            animationSpec = Motion.PressSpec,
                            label = "tabScale"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(Radius.Pill))
                                .clickable {
                                    if (!isSelected) {
                                        haptics.tick()
                                        onTabSelect(tab)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.scale(tabScale)
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) AccentLumen else TextSecondary,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1
                                )

                                // Dyskretna pigułka z licznikiem nawyków
                                if (tab == MainTab.HABITS && habitsPendingCount > 0) {
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) AccentLumen else MidnightSurfaceHighlight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = habitsPendingCount.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) MidnightBackground else TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
