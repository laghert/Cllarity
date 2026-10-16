package com.cllarity.app.model

data class RelapseEvent(
    val timestamp: Long,
    val trigger: String? = null,
    val note: String? = null
)

data class Addiction(
    val id: String,
    val name: String,
    val tagline: String,
    val iconName: String,
    val accentColorHex: Long = 0xFF6366F1,
    val startedAtTimestamp: Long, // ms
    val costPerDayPln: Double = 25.0,
    val hoursPerDaySpent: Double = 1.5,
    val relapseCount: Int = 0,
    val relapseHistory: List<RelapseEvent> = emptyList(),
    val discreetName: String? = null,
    val isActive: Boolean = false,
    val isCustom: Boolean = false
) {
    fun getDisplayName(isDiscreetMode: Boolean): String {
        return if (isDiscreetMode && !discreetName.isNullOrBlank()) {
            discreetName
        } else {
            name
        }
    }

    fun getElapsedMillis(now: Long = System.currentTimeMillis()): Long {
        return (now - startedAtTimestamp).coerceAtLeast(0L)
    }

    fun getDaysElapsed(now: Long = System.currentTimeMillis()): Long {
        val seconds = getElapsedMillis(now) / 1000
        return seconds / 86400
    }

    fun getHoursPart(now: Long = System.currentTimeMillis()): Long {
        val seconds = getElapsedMillis(now) / 1000
        return (seconds % 86400) / 3600
    }

    fun getMinutesPart(now: Long = System.currentTimeMillis()): Long {
        val seconds = getElapsedMillis(now) / 1000
        return (seconds % 3600) / 60
    }

    fun getSecondsPart(now: Long = System.currentTimeMillis()): Long {
        val seconds = getElapsedMillis(now) / 1000
        return seconds % 60
    }

    fun getMoneySaved(now: Long = System.currentTimeMillis()): Double {
        val days = getElapsedMillis(now).toDouble() / (1000.0 * 86400.0)
        return days * costPerDayPln
    }

    fun getHoursSaved(now: Long = System.currentTimeMillis()): Double {
        val days = getElapsedMillis(now).toDouble() / (1000.0 * 86400.0)
        return days * hoursPerDaySpent
    }
}

object DefaultAddictions {
    fun createDefaults(now: Long = System.currentTimeMillis()): List<Addiction> {
        return listOf(
            // 1. Alkohol
            Addiction(
                id = "addiction_alcohol",
                name = "Alkohol",
                tagline = "Czysty umysł, regeneracja wątroby i głęboki sen.",
                iconName = "wine_bar",
                accentColorHex = 0xFFEC4899,
                startedAtTimestamp = now,
                costPerDayPln = 30.0,
                hoursPerDaySpent = 2.0,
                discreetName = "Trzeźwość (Alko)",
                isActive = true // Domyślnie aktywny
            ),
            // 2. Nikotyna
            Addiction(
                id = "addiction_nicotine",
                name = "Nikotyna / Vaping",
                tagline = "Zdrowe płuca, wolność od głodu nikotynowego.",
                iconName = "smoking_rooms",
                accentColorHex = 0xFFF59E0B,
                startedAtTimestamp = now,
                costPerDayPln = 22.0,
                hoursPerDaySpent = 1.0,
                discreetName = "Czysty Oddech",
                isActive = true // Domyślnie aktywny
            ),
            // 3. Cukier / Fast Food
            Addiction(
                id = "addiction_sugar_fastfood",
                name = "Cukier / Fast Food",
                tagline = "Stabilna glukoza, lekkość i brak napadów głodu.",
                iconName = "fastfood",
                accentColorHex = 0xFFEF4444,
                startedAtTimestamp = now,
                costPerDayPln = 25.0,
                hoursPerDaySpent = 0.5,
                discreetName = "Zdrowe Odżywianie",
                isActive = false
            ),
            // 4. Self Harming
            Addiction(
                id = "addiction_self_harm",
                name = "Self Harming",
                tagline = "Czułość dla swojego ciała, bezpieczeństwo i ochrona siebie.",
                iconName = "healing",
                accentColorHex = 0xFF10B981,
                startedAtTimestamp = now,
                costPerDayPln = 0.0,
                hoursPerDaySpent = 1.0,
                discreetName = "Równowaga Emocjonalna",
                isActive = false
            ),
            // 5. Pornografia
            Addiction(
                id = "addiction_pornography",
                name = "Pornografia",
                tagline = "Reset receptorów dopaminy, wewnętrzny spokój i szacunek.",
                iconName = "visibility_off",
                accentColorHex = 0xFF8B5CF6,
                startedAtTimestamp = now,
                costPerDayPln = 0.0,
                hoursPerDaySpent = 1.5,
                discreetName = "Czysty Umysł / NSFW",
                isActive = false
            )
        )
    }
}
