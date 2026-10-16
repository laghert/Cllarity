package com.cllarity.app.data

import android.content.Context
import android.content.SharedPreferences
import com.cllarity.app.model.Addiction
import com.cllarity.app.model.DefaultAddictions
import com.cllarity.app.model.DefaultHabits
import com.cllarity.app.model.Habit
import com.cllarity.app.model.HabitCategory
import com.cllarity.app.model.HabitDifficulty
import com.cllarity.app.model.HabitFrequency
import com.cllarity.app.model.LlumieCosmetic
import com.cllarity.app.model.ClothingColor
import com.cllarity.app.model.CosmeticSlot
import com.cllarity.app.model.LlumieMood
import com.cllarity.app.model.LlumieState
import com.cllarity.app.model.RelapseEvent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
data class AppBackupData(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val habits: List<Habit>,
    val addictions: List<Addiction>,
    val llumie: LlumieState,
    val urgesResisted: Int = 0
)

class CllarityRepository(context: Context) {
    private val appContext: Context = context.applicationContext
    private val prefs: SharedPreferences = context.getSharedPreferences("cllarity_prefs_v2", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    private val _addictions = MutableStateFlow<List<Addiction>>(emptyList())
    val addictions: StateFlow<List<Addiction>> = _addictions.asStateFlow()

    private val _llumie = MutableStateFlow(LlumieState())
    val llumie: StateFlow<LlumieState> = _llumie.asStateFlow()

    private val _isPrivacyMode = MutableStateFlow(false)
    val isPrivacyMode: StateFlow<Boolean> = _isPrivacyMode.asStateFlow()
    private val _isHapticsEnabled = MutableStateFlow(true)
    val isHapticsEnabled: StateFlow<Boolean> = _isHapticsEnabled.asStateFlow()
    private val _isSoundEnabled = MutableStateFlow(true)
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()
    private val _urgesResistedCount = MutableStateFlow(0)
    val urgesResistedCount: StateFlow<Int> = _urgesResistedCount.asStateFlow()
    private val _isBiometricLockEnabled = MutableStateFlow(false)
    val isBiometricLockEnabled: StateFlow<Boolean> = _isBiometricLockEnabled.asStateFlow()
    private val _lastPledgeDate = MutableStateFlow<String?>(null)
    val lastPledgeDate: StateFlow<String?> = _lastPledgeDate.asStateFlow()
    private val _isOnboardingCompleted = MutableStateFlow(false)
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()
    private val _crisisLetter = MutableStateFlow("")
    val crisisLetter: StateFlow<String> = _crisisLetter.asStateFlow()
    private val _lastEveningReflectionDate = MutableStateFlow<String?>(null)
    val lastEveningReflectionDate: StateFlow<String?> = _lastEveningReflectionDate.asStateFlow()
    init {
        loadData()
        checkDayRollover()
    }

    private fun getTodayDateString(): String = dateFormat.format(Date())

    private fun loadData() {
        _isPrivacyMode.value = prefs.getBoolean(KEY_PRIVACY_MODE, false)
        _isHapticsEnabled.value = prefs.getBoolean(KEY_HAPTICS_ENABLED, true)
        _isSoundEnabled.value = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        _isBiometricLockEnabled.value = prefs.getBoolean("key_biometric_lock", false)
        _lastPledgeDate.value = prefs.getString(KEY_LAST_PLEDGE_DATE, null)
        _isOnboardingCompleted.value = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        _crisisLetter.value = prefs.getString(KEY_CRISIS_LETTER, "") ?: ""
        _lastEveningReflectionDate.value = prefs.getString(KEY_EVENING_REFLECTION, null)
        val defaultHabits = DefaultHabits.createDefaults()
        val habitsJson = prefs.getString(KEY_HABITS, null)
        val loadedHabits: List<Habit> = if (!habitsJson.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<List<Habit>>() {}.type
                val parsed: List<Habit> = gson.fromJson(habitsJson, type)
                val safeParsed = parsed.map { h ->
                    h.copy(
                        category = h.safeCategory,
                        difficulty = h.safeDifficulty,
                        frequency = h.safeFrequency,
                        historyDates = h.safeHistoryDates,
                        targetCount = h.safeTargetCount,
                        currentCountToday = h.safeCurrentCountToday,
                        unit = h.safeUnit,
                        scheduledDays = h.safeScheduledDays
                    )
                }
                val existingIds = safeParsed.map { it.id }.toSet()
                val missingDefaults = defaultHabits.filter { it.id !in existingIds }
                safeParsed + missingDefaults
            } catch (e: Exception) {
                defaultHabits
            }
        } else {
            defaultHabits
        }
        val today = getTodayDateString()
        val sanitizedHabits = loadedHabits.map { habit ->
            val actuallyDoneToday = habit.lastCompletedDate == today || habit.safeHistoryDates.contains(today)
            if (actuallyDoneToday) {
                habit.copy(
                    isCompletedToday = true,
                    currentCountToday = if (habit.safeCurrentCountToday < habit.safeTargetCount) habit.safeTargetCount else habit.safeCurrentCountToday
                )
            } else if (habit.isCompletedToday) {
                habit.copy(isCompletedToday = false, currentCountToday = 0)
            } else {
                habit
            }
        }
        _habits.value = sanitizedHabits

        // Load Addictions
        val defaultAddictions = DefaultAddictions.createDefaults()
        val addictionsJson = prefs.getString(KEY_ADDICTIONS, null)
        val loadedAddictions: List<Addiction> = if (!addictionsJson.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<List<Addiction>>() {}.type
                val parsed: List<Addiction> = gson.fromJson(addictionsJson, type)
                // Merge any missing default addictions (e.g. self_harm, pornography)
                val existingIds = parsed.map { it.id }.toSet()
                val missingDefaults = defaultAddictions.filter { it.id !in existingIds }
                parsed + missingDefaults
            } catch (e: Exception) {
                defaultAddictions
            }
        } else {
            defaultAddictions
        }
        _addictions.value = loadedAddictions

        // Load Llumie State
        val llumieJson = prefs.getString(KEY_LLUMIE, null)
        val loadedLlumie: LlumieState = if (!llumieJson.isNullOrEmpty()) {
            try {
                gson.fromJson(llumieJson, LlumieState::class.java)
            } catch (e: Exception) {
                LlumieState()
            }
        } else {
            LlumieState()
        }
        // Sanitize equipped cosmetics so only unlocked items for current level & streak are worn
        val validCosmetics = loadedLlumie.activeCosmetics.filter { loadedLlumie.isUnlocked(it) }.toSet()
        val sanitizedLlumie = if (validCosmetics != loadedLlumie.activeCosmetics) {
            loadedLlumie.copy(
                equippedCosmetics = validCosmetics,
                equippedCosmetic = validCosmetics.firstOrNull() ?: LlumieCosmetic.NONE
            )
        } else {
            loadedLlumie
        }
        _llumie.value = sanitizedLlumie
        if (sanitizedLlumie != loadedLlumie) {
            saveLlumie(sanitizedLlumie)
        }
    }

    fun checkDayRollover() {
        val today = getTodayDateString()
        val lastSavedDate = prefs.getString(KEY_LAST_DATE, null)

        if (lastSavedDate == null) {
            // Pierwsze uruchomienie lub brak klucza daty: zapisujemy dzisiejszą datę
            prefs.edit().putString(KEY_LAST_DATE, today).apply()
            normalizeHabitsForDate(today)
            return
        }

        if (today != lastSavedDate) {
            // Nowy dzień wykryty! Obliczamy upływ dni i resetujemy stan zaliczenia nawyków
            val daysDiff = calculateDaysBetween(lastSavedDate, today)
            val yesterday = getYesterdayDateString()

            val updated = _habits.value.map { habit ->
                val prevScheduledDate = getPreviousScheduledDate(habit, today)
                val wasCompletedOnPrevScheduled = prevScheduledDate != null &&
                        (habit.lastCompletedDate == prevScheduledDate || habit.safeHistoryDates.contains(prevScheduledDate))

                val newStreak = if (wasCompletedOnPrevScheduled) {
                    habit.streak
                } else if (habit.lastCompletedDate == today) {
                    habit.streak
                } else {
                    0
                }
                habit.copy(
                    isCompletedToday = habit.lastCompletedDate == today,
                    currentCountToday = if (habit.lastCompletedDate == today) habit.safeTargetCount else 0,
                    streak = newStreak
                )
            }
            _habits.value = updated
            saveHabits(updated)

            // Aktualizacja nadrzędnej Passy Dyscypliny Llumie:
            // Passa rośnie tylko wtedy, gdy poprzedni dzień zakończył się sukcesem (złożona deklaracja LUB nawyki)
            val currentLlumie = _llumie.value
            val lastPledge = prefs.getString(KEY_LAST_PLEDGE_DATE, null)
            val wasPledgedYesterday = lastPledge == yesterday

            val activeYesterdayHabits = _habits.value.filter { it.isActive }
            val completedYesterdayCount = activeYesterdayHabits.count {
                it.lastCompletedDate == yesterday || it.safeHistoryDates.contains(yesterday)
            }
            val hadHabitsSuccessYesterday = activeYesterdayHabits.isNotEmpty() &&
                    (completedYesterdayCount.toFloat() / activeYesterdayHabits.size.toFloat()) >= 0.75f

            val wasYesterdaySuccessful = wasPledgedYesterday || hadHabitsSuccessYesterday

            val newLlumieStreak = if (wasYesterdaySuccessful) {
                currentLlumie.streakDays + 1
            } else {
                0
            }

            val newLlumie = currentLlumie.copy(
                streakDays = newLlumieStreak,
                mood = if (wasYesterdaySuccessful) LlumieMood.HAPPY else LlumieMood.REFLECTIVE
            )
            _llumie.value = newLlumie
            saveLlumie(newLlumie)

            prefs.edit().putString(KEY_LAST_DATE, today).apply()
        } else {
            normalizeHabitsForDate(today)
        }
    }

    private fun normalizeHabitsForDate(today: String) {
        var changed = false
        val updated = _habits.value.map { habit ->
            val actuallyDoneToday = habit.lastCompletedDate == today || habit.safeHistoryDates.contains(today)
            if (habit.isCompletedToday && !actuallyDoneToday) {
                changed = true
                habit.copy(isCompletedToday = false, currentCountToday = 0)
            } else if (!habit.isCompletedToday && actuallyDoneToday) {
                changed = true
                habit.copy(isCompletedToday = true, currentCountToday = habit.safeTargetCount)
            } else {
                habit
            }
        }
        if (changed) {
            _habits.value = updated
            saveHabits(updated)
        }
    }

    private fun getYesterdayDateString(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return dateFormat.format(cal.time)
    }

    /**
     * Wyszukuje ostatnią datę (przed beforeDateStr), w którą nawyk był zaplanowany w harmonogramie.
     * Sprawdza do 14 dni wstecz.
     */
    fun getPreviousScheduledDate(habit: Habit, beforeDateStr: String): String? {
        val cal = Calendar.getInstance()
        try {
            val baseDate = dateFormat.parse(beforeDateStr) ?: return null
            cal.time = baseDate
        } catch (e: Exception) {
            return null
        }
        for (i in 1..14) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val dayOfWeekCalendar = cal.get(Calendar.DAY_OF_WEEK)
            val dayOfWeekIso = if (dayOfWeekCalendar == Calendar.SUNDAY) 7 else dayOfWeekCalendar - 1
            if (habit.isScheduledForDayOfWeek(dayOfWeekIso)) {
                return dateFormat.format(cal.time)
            }
        }
        return null
    }

    private fun calculateDaysBetween(startDateStr: String, endDateStr: String): Int {
        return try {
            val startDate = dateFormat.parse(startDateStr)
            val endDate = dateFormat.parse(endDateStr)
            if (startDate != null && endDate != null) {
                val diffMillis = endDate.time - startDate.time
                (diffMillis / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
            } else {
                1
            }
        } catch (e: Exception) {
            1
        }
    }

    fun togglePrivacyMode() {
        val newVal = !_isPrivacyMode.value
        _isPrivacyMode.value = newVal
        prefs.edit().putBoolean(KEY_PRIVACY_MODE, newVal).apply()
    }

    fun toggleHaptics() {
        val newVal = !_isHapticsEnabled.value
        _isHapticsEnabled.value = newVal
        prefs.edit().putBoolean(KEY_HAPTICS_ENABLED, newVal).apply()
    }

    fun toggleSound() {
        val newVal = !_isSoundEnabled.value
        _isSoundEnabled.value = newVal
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, newVal).apply()
    }

    fun toggleBiometricLock() {
        val newVal = !_isBiometricLockEnabled.value
        _isBiometricLockEnabled.value = newVal
        prefs.edit().putBoolean("key_biometric_lock", newVal).apply()
    }
    fun isPledgedToday(): Boolean {
        return _lastPledgeDate.value == getTodayDateString()
    }

    fun makeDailyPledge(): Boolean {
        val today = getTodayDateString()
        if (_lastPledgeDate.value == today) return false
        _lastPledgeDate.value = today
        prefs.edit().putString(KEY_LAST_PLEDGE_DATE, today).apply()
        addXp(30)
        return true
    }

    fun completeOnboarding() {
        _isOnboardingCompleted.value = true
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, true).apply()
    }

    fun resetOnboardingForDebug() {
        _isOnboardingCompleted.value = false
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, false).apply()
    }

    fun saveCrisisLetter(text: String) {
        val trimmed = text.trim()
        _crisisLetter.value = trimmed
        prefs.edit().putString(KEY_CRISIS_LETTER, trimmed).apply()
    }

    fun saveEveningReflection(note: String) {
        val today = getTodayDateString()
        _lastEveningReflectionDate.value = today
        prefs.edit().putString(KEY_EVENING_REFLECTION, today).apply()
        addXp(20)
    }

    fun isEveningReflectionDoneToday(): Boolean {
        return _lastEveningReflectionDate.value == getTodayDateString()
    }
    fun exportBackupJson(): String {
        val backup = AppBackupData(
            exportedAt = System.currentTimeMillis(),
            habits = _habits.value,
            addictions = _addictions.value,
            llumie = _llumie.value,
            urgesResisted = _urgesResistedCount.value
        )
        return gson.toJson(backup)
    }

    fun importBackupJson(jsonString: String): Boolean {
        return try {
            val backup = gson.fromJson(jsonString.trim(), AppBackupData::class.java) ?: return false
            if (backup.habits.isEmpty() && backup.addictions.isEmpty()) return false

            _habits.value = backup.habits
            saveHabits(backup.habits)

            _addictions.value = backup.addictions
            saveAddictions(backup.addictions)

            val validCosmetics = backup.llumie.activeCosmetics.filter { backup.llumie.isUnlocked(it) }.toSet()
            val sanitizedLlumie = backup.llumie.copy(
                equippedCosmetics = validCosmetics,
                equippedCosmetic = validCosmetics.firstOrNull() ?: LlumieCosmetic.NONE
            )
            _llumie.value = sanitizedLlumie
            saveLlumie(sanitizedLlumie)

            _urgesResistedCount.value = backup.urgesResisted
            prefs.edit().putInt("key_urges_resisted", backup.urgesResisted).apply()

            checkDayRollover()
            true
        } catch (e: Exception) {
            false
        }
    }
    fun recordUrgeResisted(xpBonus: Int = 25) {
        val newCount = _urgesResistedCount.value + 1
        _urgesResistedCount.value = newCount
        prefs.edit().putInt("key_urges_resisted", newCount).apply()
        addXp(xpBonus)
    }

    // --- HABITS MANAGEMENT ---

    fun toggleHabit(habitId: String) {
        checkDayRollover()
        val habit = _habits.value.find { it.id == habitId } ?: return
        if (habit.isCountBased) {
            // Dla nawyku wielokrotnego: jeśli nie skończony -> zalicz całość, jeśli skończony -> reset do 0
            if (!habit.isCompletedToday) {
                setHabitProgress(habitId, habit.safeTargetCount)
            } else {
                setHabitProgress(habitId, 0)
            }
        } else {
            val today = getTodayDateString()
            val newCompleted = !habit.isCompletedToday
            val newStreak = if (newCompleted) habit.streak + 1 else (habit.streak - 1).coerceAtLeast(0)
            val newHistory = if (newCompleted) {
                (habit.safeHistoryDates + today).distinct()
            } else {
                habit.safeHistoryDates.filter { it != today }
            }
            val xpDelta = if (newCompleted) habit.xpReward else -habit.xpReward

            val updatedHabits = _habits.value.map {
                if (it.id == habitId) {
                    it.copy(
                        isCompletedToday = newCompleted,
                        currentCountToday = if (newCompleted) 1 else 0,
                        streak = newStreak,
                        lastCompletedDate = if (newCompleted) today else newHistory.lastOrNull(),
                        historyDates = newHistory
                    )
                } else it
            }
            _habits.value = updatedHabits
            saveHabits(updatedHabits)
            if (xpDelta != 0) addXp(xpDelta)
        }
    }

    /**
     * Zwiększa postęp nawyku wielokrotnego o +1 (np. kolejna szklanka wody).
     */
    fun incrementHabitProgress(habitId: String) {
        checkDayRollover()
        val habit = _habits.value.find { it.id == habitId } ?: return
        val current = habit.safeCurrentCountToday
        val target = habit.safeTargetCount
        if (current >= target) return // Już ukończone

        setHabitProgress(habitId, current + 1)
    }

    /**
     * Zmniejsza postęp nawyku wielokrotnego o -1 (cofnięcie pomyłki).
     */
    fun decrementHabitProgress(habitId: String) {
        checkDayRollover()
        val habit = _habits.value.find { it.id == habitId } ?: return
        val current = habit.safeCurrentCountToday
        if (current <= 0) return

        setHabitProgress(habitId, current - 1)
    }

    private fun setHabitProgress(habitId: String, newCount: Int) {
        val today = getTodayDateString()
        var xpDelta = 0

        val updated = _habits.value.map { habit ->
            if (habit.id == habitId) {
                val target = habit.safeTargetCount
                val clamped = newCount.coerceIn(0, target)
                val wasCompleted = habit.isCompletedToday
                val isNowCompleted = clamped >= target

                val newStreak = when {
                    !wasCompleted && isNowCompleted -> habit.streak + 1
                    wasCompleted && !isNowCompleted -> (habit.streak - 1).coerceAtLeast(0)
                    else -> habit.streak
                }

                val newHistory = when {
                    !wasCompleted && isNowCompleted -> (habit.safeHistoryDates + today).distinct()
                    wasCompleted && !isNowCompleted -> habit.safeHistoryDates.filter { it != today }
                    else -> habit.safeHistoryDates
                }

                if (!wasCompleted && isNowCompleted) {
                    xpDelta = habit.xpReward
                } else if (wasCompleted && !isNowCompleted) {
                    xpDelta = -habit.xpReward
                }

                habit.copy(
                    currentCountToday = clamped,
                    isCompletedToday = isNowCompleted,
                    streak = newStreak,
                    lastCompletedDate = if (isNowCompleted) today else newHistory.lastOrNull(),
                    historyDates = newHistory
                )
            } else habit
        }

        _habits.value = updated
        saveHabits(updated)
        if (xpDelta != 0) {
            addXp(xpDelta)
        }
    }

    fun activateHabit(habitId: String) {
        val updated = _habits.value.map {
            if (it.id == habitId) it.copy(isActive = true) else it
        }
        _habits.value = updated
        saveHabits(updated)
    }

    fun deactivateHabit(habitId: String) {
        val updated = _habits.value.map {
            if (it.id == habitId) it.copy(isActive = false, isCompletedToday = false) else it
        }
        _habits.value = updated
        saveHabits(updated)
    }

    fun addCustomHabit(
        name: String,
        description: String,
        category: HabitCategory,
        difficulty: HabitDifficulty,
        frequency: HabitFrequency = HabitFrequency.DAILY,
        targetCount: Int = 1,
        unit: String = "razy",
        scheduledDays: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7)
    ) {
        val newHabit = Habit(
            id = "custom_habit_${UUID.randomUUID()}",
            name = name.trim(),
            description = description.trim(),
            category = category,
            difficulty = difficulty,
            frequency = frequency,
            isActive = true,
            isCustom = true,
            targetCount = targetCount.coerceAtLeast(1),
            currentCountToday = 0,
            unit = unit.trim().ifBlank { "razy" },
            scheduledDays = if (scheduledDays.isEmpty()) setOf(1, 2, 3, 4, 5, 6, 7) else scheduledDays
        )
        val updated = _habits.value + newHabit
        _habits.value = updated
        saveHabits(updated)
        addXp(15) // XP bonus for creating a new healthy habit
    }

    fun removeHabit(habitId: String) {
        val updated = _habits.value.filter { it.id != habitId }
        _habits.value = updated
        saveHabits(updated)
    }

    // --- ADDICTIONS MANAGEMENT ---

    fun activateAddiction(addictionId: String) {
        val now = System.currentTimeMillis()
        val updated = _addictions.value.map {
            if (it.id == addictionId) {
                it.copy(
                    isActive = true,
                    startedAtTimestamp = if (!it.isActive && it.relapseCount == 0) now else it.startedAtTimestamp
                )
            } else it
        }
        _addictions.value = updated
        saveAddictions(updated)
    }

    fun deactivateAddiction(addictionId: String) {
        val updated = _addictions.value.map {
            if (it.id == addictionId) it.copy(isActive = false) else it
        }
        _addictions.value = updated
        saveAddictions(updated)
    }

    fun addCustomAddiction(
        name: String,
        tagline: String,
        iconName: String,
        startedAtTimestamp: Long,
        costPerDayPln: Double,
        hoursPerDaySpent: Double,
        discreetName: String?
    ) {
        val newAddiction = Addiction(
            id = "custom_addiction_${UUID.randomUUID()}",
            name = name.trim(),
            tagline = tagline.trim(),
            iconName = iconName,
            startedAtTimestamp = startedAtTimestamp,
            costPerDayPln = costPerDayPln,
            hoursPerDaySpent = hoursPerDaySpent,
            discreetName = discreetName?.trim()?.ifBlank { null },
            isActive = true,
            isCustom = true
        )
        val updated = _addictions.value + newAddiction
        _addictions.value = updated
        saveAddictions(updated)
        addXp(20) // Bonus for taking control of life
    }

    fun removeAddiction(addictionId: String) {
        val updated = _addictions.value.filter { it.id != addictionId }
        _addictions.value = updated
        saveAddictions(updated)
    }

    fun resetAddiction(addictionId: String, trigger: String?, note: String?) {
        val now = System.currentTimeMillis()
        val updatedAddictions = _addictions.value.map { addiction ->
            if (addiction.id == addictionId) {
                val newEvent = RelapseEvent(timestamp = now, trigger = trigger, note = note)
                addiction.copy(
                    startedAtTimestamp = now,
                    relapseCount = addiction.relapseCount + 1,
                    relapseHistory = addiction.relapseHistory + newEvent
                )
            } else {
                addiction
            }
        }
        _addictions.value = updatedAddictions
        saveAddictions(updatedAddictions)

        // Set Llumie to supportive mood
        val currentLlumie = _llumie.value
        val newLlumie = currentLlumie.copy(mood = LlumieMood.REFLECTIVE)
        _llumie.value = newLlumie
        saveLlumie(newLlumie)
    }

    fun updateAddictionStart(addictionId: String, newTimestamp: Long) {
        val updatedAddictions = _addictions.value.map { addiction ->
            if (addiction.id == addictionId) {
                addiction.copy(startedAtTimestamp = newTimestamp)
            } else {
                addiction
            }
        }
        _addictions.value = updatedAddictions
        saveAddictions(updatedAddictions)
    }

    // --- LLUMIE MANAGEMENT ---

    fun equipCosmetic(cosmetic: LlumieCosmetic) {
        toggleCosmetic(cosmetic)
    }
    fun setClothingColor(color: ClothingColor) {
        val current = _llumie.value
        val updated = current.copy(clothingColor = color)
        _llumie.value = updated
        saveLlumie(updated)
    }


    fun toggleCosmetic(cosmetic: LlumieCosmetic) {
        val current = _llumie.value
        if (cosmetic == LlumieCosmetic.NONE) {
            val updated = current.copy(
                equippedCosmetic = LlumieCosmetic.NONE,
                equippedCosmetics = emptySet()
            )
            _llumie.value = updated
            saveLlumie(updated)
            return
        }
        val currentSet = current.activeCosmetics
        if (currentSet.contains(cosmetic)) {
            // Zdejmowanie założonego przedmiotu
            val newSet = currentSet - cosmetic
            val primary = newSet.firstOrNull() ?: LlumieCosmetic.NONE
            val updated = current.copy(
                equippedCosmetic = primary,
                equippedCosmetics = newSet
            )
            _llumie.value = updated
            saveLlumie(updated)
            return
        }
        // Zakładanie przedmiotu wymaga spełnienia wymagań poziomu i serii
        if (current.isUnlocked(cosmetic)) {
            val conflicting = currentSet.filter { it.slot == cosmetic.slot }
            val newSet = (currentSet - conflicting.toSet()) + cosmetic
            val primary = newSet.firstOrNull() ?: LlumieCosmetic.NONE
            val updated = current.copy(
                equippedCosmetic = primary,
                equippedCosmetics = newSet
            )
            _llumie.value = updated
            saveLlumie(updated)
        }
    }

    fun debugForceCosmetic(cosmetic: LlumieCosmetic) {
        val current = _llumie.value
        if (cosmetic == LlumieCosmetic.NONE) {
            val updated = current.copy(
                equippedCosmetic = LlumieCosmetic.NONE,
                equippedCosmetics = emptySet()
            )
            _llumie.value = updated
            saveLlumie(updated)
            return
        }
        val currentSet = current.activeCosmetics
        val conflicting = currentSet.filter { it.slot == cosmetic.slot }
        val newSet = (currentSet - conflicting.toSet()) + cosmetic
        val primary = newSet.firstOrNull() ?: LlumieCosmetic.NONE
        val updated = current.copy(
            equippedCosmetic = primary,
            equippedCosmetics = newSet
        )
        _llumie.value = updated
        saveLlumie(updated)
    }

    fun addXp(amount: Int) {
        val current = _llumie.value
        var newXp = (current.currentXp + amount).coerceAtLeast(0)
        var newLevel = current.level
        val newTotal = (current.totalXp + amount.coerceAtLeast(0))
        var newMood = current.mood

        while (newXp >= current.xpRequiredForLevel(newLevel)) {
            val req = current.xpRequiredForLevel(newLevel)
            newXp -= req
            newLevel += 1
            newMood = LlumieMood.PROUD
        }

        // Auto-equip newest cosmetic if currently NONE and unlocked
        var cosmetic = current.equippedCosmetic
        if (cosmetic == LlumieCosmetic.NONE) {
            val highestUnlocked = LlumieCosmetic.values()
                .filter { current.copy(level = newLevel).isUnlocked(it) }
                .maxByOrNull { it.unlockLevel } ?: LlumieCosmetic.NONE
            if (highestUnlocked != LlumieCosmetic.NONE) {
                cosmetic = highestUnlocked
            }
        }

        val updated = current.copy(
            level = newLevel,
            currentXp = newXp,
            totalXp = newTotal,
            mood = newMood,
            equippedCosmetic = cosmetic
        )
        _llumie.value = updated
        saveLlumie(updated)
    }

    // --- DEBUG MUTATION METHODS ---

    fun debugSetLlumieLevel(newLevel: Int) {
        val levelClamped = newLevel.coerceAtLeast(1)
        val current = _llumie.value
        val updated = current.copy(
            level = levelClamped,
            currentXp = 0
        )
        _llumie.value = updated
        saveLlumie(updated)
    }

    fun debugSetLlumieStreak(newStreak: Int) {
        val current = _llumie.value
        val updated = current.copy(streakDays = newStreak.coerceAtLeast(0))
        _llumie.value = updated
        saveLlumie(updated)
    }

    fun debugSetLlumieMood(newMood: LlumieMood) {
        val current = _llumie.value
        val updated = current.copy(mood = newMood)
        _llumie.value = updated
        saveLlumie(updated)
    }

    fun debugAddSoberDays(addictionId: String, daysToAdd: Long) {
        val updated = _addictions.value.map { addiction ->
            if (addiction.id == addictionId) {
                val offsetMs = daysToAdd * 86400 * 1000L
                addiction.copy(startedAtTimestamp = addiction.startedAtTimestamp - offsetMs)
            } else {
                addiction
            }
        }
        _addictions.value = updated
        saveAddictions(updated)
    }

    fun debugCompleteAllHabits() {
        val today = getTodayDateString()
        val updated = _habits.value.map {
            if (it.isActive) it.copy(
                isCompletedToday = true,
                lastCompletedDate = today,
                historyDates = (it.historyDates + today).distinct()
            ) else it
        }
        _habits.value = updated
        saveHabits(updated)
        addXp(60)
    }

    fun debugResetTodayHabits() {
        val today = getTodayDateString()
        val updated = _habits.value.map {
            if (it.isActive) {
                val newHistory = it.historyDates.filter { d -> d != today }
                it.copy(
                    isCompletedToday = false,
                    lastCompletedDate = newHistory.lastOrNull(),
                    historyDates = newHistory
                )
            } else it
        }
        _habits.value = updated
        saveHabits(updated)
    }

    fun debugResetAllData() {
        val defHabits = DefaultHabits.createDefaults()
        val defAddictions = DefaultAddictions.createDefaults()
        val defLlumie = LlumieState()

        _habits.value = defHabits
        _addictions.value = defAddictions
        _llumie.value = defLlumie

        saveHabits(defHabits)
        saveAddictions(defAddictions)
        saveLlumie(defLlumie)
    }

    private fun saveHabits(list: List<Habit>) {
        prefs.edit().putString(KEY_HABITS, gson.toJson(list)).apply()
        try {
            com.cllarity.app.ui.widget.CllarityZenWidgetProvider.updateAllWidgets(appContext)
        } catch (_: Exception) {}
    }

    private fun saveAddictions(list: List<Addiction>) {
        prefs.edit().putString(KEY_ADDICTIONS, gson.toJson(list)).apply()
        try {
            com.cllarity.app.ui.widget.CllarityZenWidgetProvider.updateAllWidgets(appContext)
        } catch (_: Exception) {}
    }

    private fun saveLlumie(state: LlumieState) {
        prefs.edit().putString(KEY_LLUMIE, gson.toJson(state)).apply()
        try {
            com.cllarity.app.ui.widget.CllarityZenWidgetProvider.updateAllWidgets(appContext)
        } catch (_: Exception) {}
    }

    companion object {
        private const val KEY_HABITS = "key_habits_v2"
        private const val KEY_ADDICTIONS = "key_addictions_v2"
        private const val KEY_LLUMIE = "key_llumie_v2"
        private const val KEY_LAST_DATE = "key_last_date_v2"
        private const val KEY_PRIVACY_MODE = "key_privacy_mode_v2"
        private const val KEY_HAPTICS_ENABLED = "key_haptics_enabled_v2"
        private const val KEY_SOUND_ENABLED = "key_sound_enabled_v2"
        private const val KEY_LAST_PLEDGE_DATE = "key_last_pledge_date_v2"
        private const val KEY_ONBOARDING_COMPLETED = "key_onboarding_completed_v2"
        private const val KEY_CRISIS_LETTER = "key_crisis_letter_v2"
        private const val KEY_EVENING_REFLECTION = "key_evening_reflection_v2"
    }
}
