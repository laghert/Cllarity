package com.cllarity.app

import com.cllarity.app.model.ClothingColor
import com.cllarity.app.model.CosmeticCategory
import com.cllarity.app.model.CosmeticSlot
import com.cllarity.app.model.DefaultHabits
import com.cllarity.app.model.Habit
import com.cllarity.app.model.HabitCategory
import com.cllarity.app.model.HabitDifficulty
import com.cllarity.app.model.LlumieCosmetic
import com.cllarity.app.model.LlumieState
import org.junit.Assert.assertEquals
import com.cllarity.app.data.AppBackupData
import com.cllarity.app.model.Addiction
import com.cllarity.app.model.HabitFrequency
import com.google.gson.Gson
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LlumieAndHabitsTest {

    @Test
    fun `level 1 user cannot unlock clothing items requiring higher level`() {
        val llumieLevel1 = LlumieState(level = 1, streakDays = 0)

        // SWEATER requires level 2
        assertFalse(llumieLevel1.isUnlocked(LlumieCosmetic.SWEATER))
        assertEquals(2, LlumieCosmetic.SWEATER.unlockLevel)

        // TSHIRT requires level 3
        assertFalse(llumieLevel1.isUnlocked(LlumieCosmetic.TSHIRT))
        assertEquals(3, LlumieCosmetic.TSHIRT.unlockLevel)

        // HOODIE requires level 5
        assertFalse(llumieLevel1.isUnlocked(LlumieCosmetic.HOODIE))
        assertEquals(5, LlumieCosmetic.HOODIE.unlockLevel)

        // NONE is unlocked at level 1
        assertTrue(llumieLevel1.isUnlocked(LlumieCosmetic.NONE))
    }

    @Test
    fun `level 2 user can unlock sweater but not tshirt or hoodie`() {
        val llumieLevel2 = LlumieState(level = 2, streakDays = 0)

        assertTrue(llumieLevel2.isUnlocked(LlumieCosmetic.SWEATER))
        assertFalse(llumieLevel2.isUnlocked(LlumieCosmetic.TSHIRT))
        assertFalse(llumieLevel2.isUnlocked(LlumieCosmetic.HOODIE))
    }
    @Test
    fun `higher level outfits are locked until respective levels are reached`() {
        val llumieLevel5 = LlumieState(level = 5, streakDays = 0)
        assertFalse(llumieLevel5.isUnlocked(LlumieCosmetic.STOIC_CAPE))
        assertFalse(llumieLevel5.isUnlocked(LlumieCosmetic.MAGE_ROBE))
        assertFalse(llumieLevel5.isUnlocked(LlumieCosmetic.GOLDEN_ARMOR))

        val llumieLevel6 = LlumieState(level = 6, streakDays = 0)
        assertTrue(llumieLevel6.isUnlocked(LlumieCosmetic.STOIC_CAPE))
        assertFalse(llumieLevel6.isUnlocked(LlumieCosmetic.MAGE_ROBE))
        assertFalse(llumieLevel6.isUnlocked(LlumieCosmetic.GOLDEN_ARMOR))

        val llumieLevel8 = LlumieState(level = 8, streakDays = 0)
        assertTrue(llumieLevel8.isUnlocked(LlumieCosmetic.STOIC_CAPE))
        assertTrue(llumieLevel8.isUnlocked(LlumieCosmetic.MAGE_ROBE))
        assertFalse(llumieLevel8.isUnlocked(LlumieCosmetic.GOLDEN_ARMOR))

        val llumieLevel10 = LlumieState(level = 10, streakDays = 0)
        assertTrue(llumieLevel10.isUnlocked(LlumieCosmetic.GOLDEN_ARMOR))
    }

    @Test
    fun `streak relics require streak days in addition to level`() {
        val llumieLevel10NoStreak = LlumieState(level = 10, streakDays = 2)

        // Golden Flame requires 7 streak days
        assertFalse(llumieLevel10NoStreak.isUnlocked(LlumieCosmetic.GOLDEN_FLAME_RELIC))

        // Rebirth Medallion requires 30 streak days
        assertFalse(llumieLevel10NoStreak.isUnlocked(LlumieCosmetic.REBIRTH_MEDALLION))

        // Phoenix Wings requires 90 streak days
        assertFalse(llumieLevel10NoStreak.isUnlocked(LlumieCosmetic.PHOENIX_WINGS))

        val llumieLevel10WithStreak = LlumieState(level = 10, streakDays = 35)
        assertTrue(llumieLevel10WithStreak.isUnlocked(LlumieCosmetic.GOLDEN_FLAME_RELIC))
        assertTrue(llumieLevel10WithStreak.isUnlocked(LlumieCosmetic.REBIRTH_MEDALLION))
        assertFalse(llumieLevel10WithStreak.isUnlocked(LlumieCosmetic.PHOENIX_WINGS))
    }

    @Test
    fun `sanitizing equipped cosmetics strips locked items`() {
        // Suppose previous state had illegally equipped sweater at level 1
        val corruptedState = LlumieState(
            level = 1,
            streakDays = 0,
            equippedCosmetics = setOf(LlumieCosmetic.SWEATER, LlumieCosmetic.BEANIE_HAT)
        )

        val validCosmetics = corruptedState.activeCosmetics.filter { corruptedState.isUnlocked(it) }.toSet()
        assertEquals(emptySet<LlumieCosmetic>(), validCosmetics)
    }

    @Test
    fun `habit daily normalization correctly marks previous day completion as not completed today`() {
        val today = "2026-10-08"
        val yesterday = "2026-10-07"

        val habitCompletedYesterday = Habit(
            id = "habit_1",
            name = "Medytacja",
            description = "10 min",
            category = HabitCategory.MORNING,
            streak = 5,
            isCompletedToday = true, // Saved yesterday as true
            lastCompletedDate = yesterday,
            historyDates = listOf(yesterday),
            isActive = true
        )

        val actuallyDoneToday = habitCompletedYesterday.lastCompletedDate == today ||
                habitCompletedYesterday.historyDates.contains(today)
        assertFalse(actuallyDoneToday)

        val normalized = if (habitCompletedYesterday.isCompletedToday && !actuallyDoneToday) {
            habitCompletedYesterday.copy(isCompletedToday = false)
        } else {
            habitCompletedYesterday
        }

        assertFalse(normalized.isCompletedToday)
        assertEquals(yesterday, normalized.lastCompletedDate)
        assertEquals(5, normalized.streak)
    }

    @Test
    fun `habit daily rollover resets completion and retains streak when completed yesterday`() {
        val today = "2026-10-08"
        val yesterday = "2026-10-07"

        val habit = Habit(
            id = "habit_1",
            name = "Spacer",
            description = "30 min",
            category = HabitCategory.BODY_HEALTH,
            streak = 3,
            isCompletedToday = true,
            lastCompletedDate = yesterday,
            historyDates = listOf("2026-10-05", "2026-10-06", yesterday),
            isActive = true
        )

        val wasCompletedYesterday = habit.lastCompletedDate == yesterday || habit.historyDates.contains(yesterday)
        assertTrue(wasCompletedYesterday)

        val newStreak = if (wasCompletedYesterday) {
            habit.streak
        } else if (habit.lastCompletedDate == today) {
            habit.streak
        } else {
            0
        }

        val rolledOver = habit.copy(
            isCompletedToday = habit.lastCompletedDate == today,
            streak = newStreak
        )

        assertFalse(rolledOver.isCompletedToday)
        assertEquals(3, rolledOver.streak)
    }

    @Test
    fun `habit daily rollover resets streak to 0 when yesterday was missed`() {
        val today = "2026-10-08"
        val yesterday = "2026-10-07"
        val twoDaysAgo = "2026-10-06"

        val habitMissed = Habit(
            id = "habit_1",
            name = "Czytanie",
            description = "15 stron",
            category = HabitCategory.FOCUS_MIND,
            streak = 10,
            isCompletedToday = false,
            lastCompletedDate = twoDaysAgo,
            historyDates = listOf(twoDaysAgo),
            isActive = true
        )

        val wasCompletedYesterday = habitMissed.lastCompletedDate == yesterday || habitMissed.historyDates.contains(yesterday)
        assertFalse(wasCompletedYesterday)

        val newStreak = if (wasCompletedYesterday) {
            habitMissed.streak
        } else if (habitMissed.lastCompletedDate == today) {
            habitMissed.streak
        } else {
            0
        }

        val rolledOver = habitMissed.copy(
            isCompletedToday = habitMissed.lastCompletedDate == today,
            streak = newStreak
        )

        assertFalse(rolledOver.isCompletedToday)
        assertEquals(0, rolledOver.streak)
    }

    @Test
    fun `unchecking a habit restores previous completed date from history`() {
        val today = "2026-10-08"
        val yesterday = "2026-10-07"

        // Habit completed yesterday, then checked today
        val habitCheckedToday = Habit(
            id = "habit_1",
            name = "Woda",
            description = "2 litry",
            category = HabitCategory.BODY_HEALTH,
            streak = 4,
            isCompletedToday = true,
            lastCompletedDate = today,
            historyDates = listOf(yesterday, today),
            isActive = true
        )

        // User unchecks today
        val newHistory = habitCheckedToday.historyDates.filter { it != today }
        val uncheckedHabit = habitCheckedToday.copy(
            isCompletedToday = false,
            streak = (habitCheckedToday.streak - 1).coerceAtLeast(0),
            lastCompletedDate = newHistory.lastOrNull(),
            historyDates = newHistory
        )

        assertFalse(uncheckedHabit.isCompletedToday)
        assertEquals(3, uncheckedHabit.streak)
        assertEquals(yesterday, uncheckedHabit.lastCompletedDate)
        assertEquals(listOf(yesterday), uncheckedHabit.historyDates)
    }
    @Test
    fun `app backup data round trips correctly through JSON serialization`() {
        val gson = Gson()
        val habits = listOf(
            Habit(
                id = "h1",
                name = "Sport",
                description = "Bieganie",
                category = HabitCategory.BODY_HEALTH,
                frequency = HabitFrequency.WEEKDAYS,
                streak = 7,
                isCompletedToday = true,
                lastCompletedDate = "2026-10-08",
                historyDates = listOf("2026-10-08"),
                isActive = true
            )
        )
        val llumie = LlumieState(level = 6, streakDays = 14, equippedCosmetic = LlumieCosmetic.STOIC_CAPE)
        val backup = AppBackupData(
            version = 1,
            exportedAt = 123456789L,
            habits = habits,
            addictions = emptyList(),
            llumie = llumie,
            urgesResisted = 3
        )

        val json = gson.toJson(backup)
        val restored = gson.fromJson(json, AppBackupData::class.java)

        assertEquals(1, restored.version)
        assertEquals(1, restored.habits.size)
        assertEquals(HabitFrequency.WEEKDAYS, restored.habits[0].frequency)
        assertEquals(6, restored.llumie.level)
        assertEquals(LlumieCosmetic.STOIC_CAPE, restored.llumie.equippedCosmetic)
        assertEquals(3, restored.urgesResisted)
    }
    @Test
    fun `legacy habit JSON without frequency or history dates deserializes safely without crash`() {
        val legacyJson = """
            {
                "id": "legacy_habit",
                "name": "Picie wody",
                "description": "2 litry",
                "streak": 5,
                "isCompletedToday": true,
                "isActive": true
            }
        """.trimIndent()

        val gson = Gson()
        val habit = gson.fromJson(legacyJson, Habit::class.java)

        assertEquals(HabitFrequency.DAILY, habit.safeFrequency)
        assertEquals(HabitDifficulty.MEDIUM, habit.safeDifficulty)
        assertEquals(HabitCategory.BODY_HEALTH, habit.safeCategory)
        assertEquals(emptyList<String>(), habit.safeHistoryDates)
        assertEquals("Codziennie", habit.safeFrequency.displayName)
        // Nowe pola safe fallbacki dla starych danych JSON
        assertEquals(1, habit.safeTargetCount)
        assertEquals(0, habit.safeCurrentCountToday)
        assertEquals("razy", habit.safeUnit)
        assertEquals(setOf(1, 2, 3, 4, 5, 6, 7), habit.safeScheduledDays)
        assertFalse(habit.isCountBased)
    }

    @Test
    fun `count-based habit calculates progress ratio and completion correctly`() {
        val waterHabit = Habit(
            id = "water_habit",
            name = "Picie wody",
            description = "8 szklanek dziennie",
            targetCount = 8,
            currentCountToday = 4,
            unit = "szklanek"
        )

        assertTrue(waterHabit.isCountBased)
        assertEquals(8, waterHabit.safeTargetCount)
        assertEquals(4, waterHabit.safeCurrentCountToday)
        assertEquals(0.5f, waterHabit.todayProgressRatio, 0.001f)
        assertFalse(waterHabit.isCompletedToday)

        val completedWater = waterHabit.copy(
            currentCountToday = 8,
            isCompletedToday = true
        )
        assertEquals(1.0f, completedWater.todayProgressRatio, 0.001f)
        assertTrue(completedWater.isCompletedToday)
    }

    @Test
    fun `scheduledDays correctly identifies weekday vs weekend habits`() {
        val weekdayHabit = Habit(
            id = "gym",
            name = "Siłownia",
            description = "Dni robocze",
            scheduledDays = setOf(1, 2, 3, 4, 5) // Pn-Pt
        )

        // Poniedziałek (1) .. Piątek (5)
        assertTrue(weekdayHabit.isScheduledForDayOfWeek(1))
        assertTrue(weekdayHabit.isScheduledForDayOfWeek(5))
        // Sobota (6), Niedziela (7)
        assertFalse(weekdayHabit.isScheduledForDayOfWeek(6))
        assertFalse(weekdayHabit.isScheduledForDayOfWeek(7))
    }
}
