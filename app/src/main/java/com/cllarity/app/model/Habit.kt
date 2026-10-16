package com.cllarity.app.model

enum class HabitCategory(val displayName: String, val icon: String) {
    MORNING("Poranek", "wb_sunny"),
    BODY_HEALTH("Ciało i Zdrowie", "favorite"),
    FOCUS_MIND("Skupienie i Umysł", "psychology"),
    EVENING("Wieczór i Regeneracja", "nights_stay")
}

enum class HabitDifficulty(val label: String, val weight: Int) {
    EASY("Łatwy", 1),
    MEDIUM("Średni", 2),
    HARD("Wymagający", 3)
}
enum class HabitFrequency(val displayName: String, val shortLabel: String) {
    DAILY("Codziennie", "7x/tydz"),
    WEEKDAYS("Dni robocze (Pn-Pt)", "Pn-Pt"),
    WEEKENDS("Weekend (So-Nd)", "So-Nd")
}


data class Habit(
    val id: String,
    val name: String,
    val description: String,
    val category: HabitCategory = HabitCategory.BODY_HEALTH,
    val difficulty: HabitDifficulty = HabitDifficulty.MEDIUM,
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val streak: Int = 0,
    val isCompletedToday: Boolean = false,
    val lastCompletedDate: String? = null, // "YYYY-MM-DD"
    val historyDates: List<String> = emptyList(),
    val isActive: Boolean = false,
    val isCustom: Boolean = false,
    // --- NOWE ZAAWANSOWANE CECHY NAWYKU ---
    val targetCount: Int = 1,              // Ile razy trzeba zrobić (np. 1 = zwykły checkbox, 5 = 5 szklanek wody)
    val currentCountToday: Int = 0,        // Ile razy zrobiono dzisiaj
    val unit: String = "razy",             // Jednostka: "razy", "szklanki", "stron", "minut", "serii"
    val scheduledDays: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7) // 1=Poniedziałek, 7=Niedziela (ISO-8601)
) {
    val safeFrequency: HabitFrequency
        get() = (frequency as HabitFrequency?) ?: HabitFrequency.DAILY

    val safeDifficulty: HabitDifficulty
        get() = (difficulty as HabitDifficulty?) ?: HabitDifficulty.MEDIUM

    val safeCategory: HabitCategory
        get() = (category as HabitCategory?) ?: HabitCategory.BODY_HEALTH

    val safeHistoryDates: List<String>
        get() = (historyDates as List<String>?) ?: emptyList()

    val safeTargetCount: Int
        get() = (targetCount as Int?).let { if (it == null || it < 1) 1 else it }

    val safeCurrentCountToday: Int
        get() = (currentCountToday as Int?).let { if (it == null || it < 0) 0 else it }

    val safeUnit: String
        get() = (unit as String?).let { if (it.isNullOrBlank()) "razy" else it }

    val safeScheduledDays: Set<Int>
        get() {
            val s = scheduledDays as Set<Int>?
            if (s != null && s.isNotEmpty()) return s
            // Migracja z legacy frequency, jeśli brak scheduledDays
            return when (safeFrequency) {
                HabitFrequency.DAILY -> setOf(1, 2, 3, 4, 5, 6, 7)
                HabitFrequency.WEEKDAYS -> setOf(1, 2, 3, 4, 5)
                HabitFrequency.WEEKENDS -> setOf(6, 7)
            }
        }

    /** Czy nawyk ma cel wielokrotny (stepper z plusikiem) czy pojedyncze odhaczenie */
    val isCountBased: Boolean
        get() = safeTargetCount > 1

    /** Sprawdza, czy nawyk jest zaplanowany na dany dzień tygodnia ISO (1=Pn .. 7=Nd) */
    fun isScheduledForDayOfWeek(dayOfWeekIso: Int): Boolean {
        return safeScheduledDays.contains(dayOfWeekIso)
    }

    /** Postęp dzisiejszy w zakresie 0.0f .. 1.0f */
    val todayProgressRatio: Float
        get() {
            if (!isCountBased) return if (isCompletedToday) 1.0f else 0.0f
            return (safeCurrentCountToday.toFloat() / safeTargetCount.toFloat()).coerceIn(0f, 1f)
        }

    val xpReward: Int
        get() = safeDifficulty.weight * 10
}

object DefaultHabits {
    fun createDefaults(): List<Habit> = listOf(
        // Poranne (4)
        Habit(
            id = "habit_water_morning",
            name = "Szklanka wody po przebudzeniu",
            description = "Nawodnienie organizmu i pobudzenie metabolizmu z rana.",
            category = HabitCategory.MORNING,
            difficulty = HabitDifficulty.EASY,
            isActive = true // Domyślnie aktywny na start
        ),
        Habit(
            id = "habit_bed_making",
            name = "Ścielenie łóżka",
            description = "Pierwsze małe zwycięstwo budujące dyscyplinę na cały dzień.",
            category = HabitCategory.MORNING,
            difficulty = HabitDifficulty.EASY,
            isActive = false
        ),
        Habit(
            id = "habit_morning_stretch",
            name = "Poranna mobilność / Joga",
            description = "10 minut rozciągania na uwolnienie napięć po nocy.",
            category = HabitCategory.MORNING,
            difficulty = HabitDifficulty.MEDIUM,
            isActive = false
        ),
        Habit(
            id = "habit_protein_breakfast",
            name = "Odżywcze śniadanie białkowe",
            description = "Stabilny poziom cukru i sytość bez zjazdów energetycznych.",
            category = HabitCategory.MORNING,
            difficulty = HabitDifficulty.MEDIUM,
            isActive = false
        ),

        // Ciało i Zdrowie (4)
        Habit(
            id = "habit_10k_steps",
            name = "10 000 kroków / Spacer",
            description = "Ruch na świeżym powietrzu, dotlenienie i regeneracja wzroku.",
            category = HabitCategory.BODY_HEALTH,
            difficulty = HabitDifficulty.HARD,
            isActive = true // Domyślnie aktywny na start
        ),
        Habit(
            id = "habit_workout",
            name = "Trening siłowy lub biegowy",
            description = "Minimum 30 minut intensywnej pracy mięśni i układu krążenia.",
            category = HabitCategory.BODY_HEALTH,
            difficulty = HabitDifficulty.HARD,
            isActive = false
        ),
        Habit(
            id = "habit_clean_meal",
            name = "Czysty posiłek bez ultra-przetworzeń",
            description = "Świeże warzywa, pełne ziarna i zero fast-foodów.",
            category = HabitCategory.BODY_HEALTH,
            difficulty = HabitDifficulty.MEDIUM,
            isActive = false
        ),
        Habit(
            id = "habit_water_2l",
            name = "2 litry wody w ciągu dnia",
            description = "Stałe nawodnienie komórek i lepsza koncentracja.",
            category = HabitCategory.BODY_HEALTH,
            difficulty = HabitDifficulty.EASY,
            isActive = false,
            targetCount = 8,
            unit = "szklanek"
        ),

        // Skupienie i Umysł (4)
        Habit(
            id = "habit_deep_work",
            name = "Sesja Deep Work (min. 45 min)",
            description = "Głębokie skupienie bez powiadomień, telefonu i multitaskingu.",
            category = HabitCategory.FOCUS_MIND,
            difficulty = HabitDifficulty.HARD,
            isActive = true // Domyślnie aktywny na start
        ),
        Habit(
            id = "habit_reading",
            name = "15 minut czytania książki",
            description = "Rozwój wiedzy, koncentracji i poszerzanie słownictwa.",
            category = HabitCategory.FOCUS_MIND,
            difficulty = HabitDifficulty.MEDIUM,
            isActive = false
        ),
        Habit(
            id = "habit_mindfulness",
            name = "Medytacja / Praktyka oddechu",
            description = "10 minut uważności na wyciszenie układu współczulnego.",
            category = HabitCategory.FOCUS_MIND,
            difficulty = HabitDifficulty.MEDIUM,
            isActive = false
        ),
        Habit(
            id = "habit_desk_clean",
            name = "Uporządkowanie przestrzeni pracy",
            description = "Czyste biurko to czysty i spokojny umysł.",
            category = HabitCategory.FOCUS_MIND,
            difficulty = HabitDifficulty.EASY,
            isActive = false
        ),

        // Wieczór i Regeneracja (3) -> razem 15 nawyków
        Habit(
            id = "habit_digital_detox",
            name = "Cyfrowy detoks przed snem",
            description = "Odłożenie telefonu i ekranów na min. 45 minut przed pójściem spać.",
            category = HabitCategory.EVENING,
            difficulty = HabitDifficulty.HARD,
            isActive = false
        ),
        Habit(
            id = "habit_evening_journal",
            name = "Podsumowanie dnia / Wdzięczność",
            description = "Zapisanie 3 dobrych rzeczy i refleksja nad minionym dniem.",
            category = HabitCategory.EVENING,
            difficulty = HabitDifficulty.EASY,
            isActive = false
        ),
        Habit(
            id = "habit_sleep_8h",
            name = "7–8 godzin regenerującego snu",
            description = "Fundament odporności, neurogenezy i trwałego samopoczucia.",
            category = HabitCategory.EVENING,
            difficulty = HabitDifficulty.MEDIUM,
            isActive = true // Domyślnie aktywny na start
        )
    )
}
