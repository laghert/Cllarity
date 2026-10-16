package com.cllarity.app.model

enum class CosmeticSlot {
    HEAD,
    EYES,
    CHEEKS,
    NECK,
    WAIST,
    CLOTHING,
    AURA
}

enum class CosmeticCategory(val title: String) {
    HEADWEAR("Nakrycia głowy"),
    CLOTHING("Ubranka"),
    ACCESSORIES("Dodatki")
}

enum class ClothingColor(val displayName: String, val hexColor: Long) {
    LAVENDER("Lawenda", 0xFFA78BFA),
    SKY_CYAN("Błękit", 0xFF38BDF8),
    EMERALD("Szmaragd", 0xFF10B981),
    ROSE("Pudrowy Róż", 0xFFFB7185),
    AMBER("Ciepły Miód", 0xFFF59E0B),
    CORAL("Czerwień", 0xFFE11D48),
    MIDNIGHT("Grafit", 0xFF1E153A),
    CREAM("Krem", 0xFFF5EBE0)
}

enum class LlumieCosmetic(
    val displayName: String,
    val unlockLevel: Int,
    val description: String,
    val slot: CosmeticSlot,
    val category: CosmeticCategory,
    val requiredStreakDays: Int = 0
) {
    // --- NAKRYCIA GŁOWY ---
    NONE("Brak", 1, "Zdejmij nakrycie głowy", CosmeticSlot.HEAD, CosmeticCategory.HEADWEAR),
    SPROUT_LEAF("Pączek Nadziei", 2, "Zabawny zielony listek kołyszący się na główce", CosmeticSlot.HEAD, CosmeticCategory.HEADWEAR),
    BEANIE_HAT("Zimowa Beanie", 3, "Ciepła czapka z puszystym pomponem", CosmeticSlot.HEAD, CosmeticCategory.HEADWEAR),
    BASEBALL_CAP("Bejsbolówka", 4, "Sportowa czapeczka z daszkiem", CosmeticSlot.HEAD, CosmeticCategory.HEADWEAR),
    WIZARD_HAT("Czapka Maga", 5, "Magiczna fioletowa czapka z gwiazdkami", CosmeticSlot.HEAD, CosmeticCategory.HEADWEAR),
    GLOWING_HALO("Złota Aureola", 6, "Aureola pulsująca w rytmie oddechu", CosmeticSlot.HEAD, CosmeticCategory.HEADWEAR),
    ROYAL_CROWN("Królewska Korona", 7, "Złota korona królewska wysadzana szafirami", CosmeticSlot.HEAD, CosmeticCategory.HEADWEAR),
    BERET_HAT("Beret Artysty", 8, "Stylowy beret dodający artystycznego ducha", CosmeticSlot.HEAD, CosmeticCategory.HEADWEAR),
    OLIVE_WREATH("Wieniec Oliwny", 9, "Grecki wieniec laurowy symbolizujący stoicki pokój i triumf ducha", CosmeticSlot.HEAD, CosmeticCategory.HEADWEAR),

    // --- UBRANKA (KOLOROWALNE) ---
    SWEATER("Sweterek", 2, "Ciepły dzianinowy sweterek dopasowany do sylwetki", CosmeticSlot.CLOTHING, CosmeticCategory.CLOTHING),
    TSHIRT("T-Shirt", 3, "Wygodna bawełniana koszulka z mini logo gwiazdki", CosmeticSlot.CLOTHING, CosmeticCategory.CLOTHING),
    HOODIE("Bluza z Kapturem", 5, "Luźna bluza z kapturem, sznurkami i kieszenią kangurką", CosmeticSlot.CLOTHING, CosmeticCategory.CLOTHING),
    STOIC_CAPE("Peleryna Stoika", 6, "Elegancka peleryna chroniąca przed życiowymi burzami", CosmeticSlot.CLOTHING, CosmeticCategory.CLOTHING),
    ZEN_KIMONO("Kimono Zen", 7, "Tradycyjne minimalistyczne kimono z pasem obi dla głębokiego wyciszenia", CosmeticSlot.CLOTHING, CosmeticCategory.CLOTHING),
    MAGE_ROBE("Szata Mędrca", 8, "Mistyczna szata z gwiezdnymi runami samodyscypliny", CosmeticSlot.CLOTHING, CosmeticCategory.CLOTHING),
    GOLDEN_ARMOR("Zbroja Niezłomności", 10, "Lśniący napierśnik ze złotą gwiazdą niezłomnego ducha", CosmeticSlot.CLOTHING, CosmeticCategory.CLOTHING),

    // --- DODATKI ---
    CHEEK_STICKERS("Gwiezdne Naklejki", 2, "Złote gwiazdki i serduszka na policzkach", CosmeticSlot.CHEEKS, CosmeticCategory.ACCESSORIES),
    COZY_SCARF("Ciepły Szalik", 3, "Stylowy, miękki szalik otulający szyję z frędzelkami", CosmeticSlot.NECK, CosmeticCategory.ACCESSORIES),
    LEATHER_BELT("Skórzany Pasek", 3, "Klasyczny pasek z lśniącą złotą klamrą", CosmeticSlot.WAIST, CosmeticCategory.ACCESSORIES),
    COOL_SHADES("Czarne Okulary", 4, "Modne okularki przeciwsłoneczne dodające pewności siebie", CosmeticSlot.EYES, CosmeticCategory.ACCESSORIES),
    TRAVEL_BACKPACK("Plecak Podróżnika", 4, "Skórzany plecaczek gotowy na każdą nową wyprawę", CosmeticSlot.AURA, CosmeticCategory.ACCESSORIES),
    BOW_TIE("Elegancka Muszka", 5, "Szykowna muszka na wyjątkowe okazje", CosmeticSlot.NECK, CosmeticCategory.ACCESSORIES),
    BELL_COLLAR("Złoty Dzwoneczek", 6, "Złoty dzwoneczek brzęczący przy każdym ruchu", CosmeticSlot.NECK, CosmeticCategory.ACCESSORIES),
    AURA_STARS("Gwiezdny Pył", 8, "Kosmiczne cząsteczki energii krążące wokół Llumie", CosmeticSlot.AURA, CosmeticCategory.ACCESSORIES),

    // --- RELIKTY SERII (TROFEA KAMIEŃ MILOWYCH) ---
    GOLDEN_FLAME_RELIC("Płomyk Trwałości", 1, "Złoty płomyk trzymany w łapkach za 7 dni czystości", CosmeticSlot.AURA, CosmeticCategory.ACCESSORIES, requiredStreakDays = 7),
    REBIRTH_MEDALLION("Medalion Odrodzenia", 1, "Grawerowany medalion na szyi za 30 dni wolności", CosmeticSlot.NECK, CosmeticCategory.ACCESSORIES, requiredStreakDays = 30),
    PHOENIX_WINGS("Skrzydła Feniksa", 1, "Legendarne skrzydła światła za 90 dni nowej ścieżki mózgu", CosmeticSlot.AURA, CosmeticCategory.ACCESSORIES, requiredStreakDays = 90)
}

enum class LlumieMood(val title: String, val message: String) {
    HAPPY("Jasny i spokojny", "Czuję Twój wewnętrzny spokój. Nasze światło rośnie!"),
    PROUD("Promienny blask", "Twoja konsekwencja rozświetla całą przestrzeń!"),
    LOVING("Ciepłe zaufanie", "Dziękuję, że dbasz o siebie i o naszą nadzieję."),
    SLEEPY("Spokojny sen", "Czas na regenerację. Głęboki sen pozwala znowu lśnić."),
    REFLECTIVE("Wspierający kocyk", "Potknięcie to nie koniec. Śpię bezpiecznie pod mgiełką i czekam na nasz restart.")
}

data class LlumieState(
    val level: Int = 1,
    val currentXp: Int = 0,
    val totalXp: Int = 0,
    val streakDays: Int = 0,
    val mood: LlumieMood = LlumieMood.HAPPY,
    val equippedCosmetic: LlumieCosmetic = LlumieCosmetic.NONE,
    val equippedCosmetics: Set<LlumieCosmetic> = if (equippedCosmetic != LlumieCosmetic.NONE) setOf(equippedCosmetic) else emptySet(),
    val clothingColor: ClothingColor = ClothingColor.LAVENDER
) {
    fun isUnlocked(cosmetic: LlumieCosmetic): Boolean {
        val levelOk = cosmetic.unlockLevel <= level
        val streakOk = cosmetic.requiredStreakDays == 0 || streakDays >= cosmetic.requiredStreakDays
        return levelOk && streakOk
    }

    fun isEquipped(cosmetic: LlumieCosmetic): Boolean {
        if (cosmetic == LlumieCosmetic.NONE) return activeCosmetics.none { it.slot == CosmeticSlot.HEAD }
        return activeCosmetics.contains(cosmetic)
    }

    val activeCosmetics: Set<LlumieCosmetic>
        get() = if (equippedCosmetics.isNotEmpty()) equippedCosmetics
                else if (equippedCosmetic != LlumieCosmetic.NONE) setOf(equippedCosmetic)
                else emptySet()

    fun xpRequiredForLevel(lvl: Int): Int = 80 + (lvl - 1) * 40

    val xpForNextLevel: Int
        get() = xpRequiredForLevel(level)

    val progressToNextLevel: Float
        get() = (currentXp.toFloat() / xpForNextLevel.toFloat()).coerceIn(0f, 1f)

    fun availableCosmetics(): List<LlumieCosmetic> {
        return LlumieCosmetic.values().filter { isUnlocked(it) }
    }

    /**
     * Generuje bogate, kontekstowe i stoickie wiadomości od Llumie w stylu Apple / Material You.
     */
    fun getDynamicMessage(
        petCount: Int = 0,
        hourOfDay: Int = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    ): String {
        // 1. Reakcje na głaskanie / mruczenie (Multimodal Petting Engine)
        if (petCount > 0) {
            val petQuotes = listOf(
                "Mmm... uwielbiam, gdy mnie głaszczesz! Czuję Twój spokój i skupienie.",
                "Twoja dłoń jest taka ciepła. Oddychajmy powoli razem.",
                "Każde pogłaskanie rozświetla mój wewnętrzny płomyk siły.",
                "Czujesz to ciche mruczenie? To wibracja Twojej rosnącej dyscypliny.",
                "Jestem tu przy Tobie – każda nagła pokusa odpłynie jak morska fala.",
                "Dziękuję za czułość! Razem jesteśmy silniejsi niż jakikolwiek dawny nawyk.",
                "Czysty umysł, ciche serce. Znakomicie sobie dziś radzisz.",
                "Twój dotyk przypomina mi: najważniejsza chwila to właśnie 'teraz'."
            )
            return petQuotes[petCount % petQuotes.size]
        }

        // 2. Komentarz do założonych ubrań i trofeów
        if (activeCosmetics.contains(LlumieCosmetic.PHOENIX_WINGS)) {
            return "Te Skrzydła Feniksa lśnią dzięki Twoim 90 dniom wolności! Narodziłeś się na nowo."
        }
        if (activeCosmetics.contains(LlumieCosmetic.REBIRTH_MEDALLION)) {
            return "Medalion Odrodzenia na mojej szyi przypomina o Twoich 30 dniach czystego mózgu."
        }
        if (activeCosmetics.contains(LlumieCosmetic.GOLDEN_FLAME_RELIC)) {
            return "Trzymam ten Płomyk Trwałości mocno w łapkach. Wytrzymałeś ponad tydzień bez potknięcia!"
        }
        if (activeCosmetics.contains(LlumieCosmetic.COOL_SHADES)) {
            return "W tych okularach żadna nagła pokusa ani zły nastrój nas nie zaskoczą."
        }
        if (activeCosmetics.contains(LlumieCosmetic.SWEATER)) {
            return "Ten sweterek leży idealnie! Jest mi tak przytulnie, ciepło i bezpiecznie."
        }
        if (activeCosmetics.contains(LlumieCosmetic.HOODIE)) {
            return "W tej bluzie z kapturem czuję pełen spokój i gotowość na każde dzisiejsze wyzwanie."
        }
        if (activeCosmetics.contains(LlumieCosmetic.GOLDEN_ARMOR)) {
            return "Ta Złota Zbroja lśni siłą Twojego charakteru. Twoja wola jest twardsza niż stal."
        }
        if (activeCosmetics.contains(LlumieCosmetic.MAGE_ROBE)) {
            return "W tej Szacie Mędrca każda pokusa traci moc wobec Twojej stoickiej mądrości."
        }
        if (activeCosmetics.contains(LlumieCosmetic.STOIC_CAPE)) {
            return "Peleryna Stoika przypomina: burze wokół nas nie mają władzy nad pokojem wewnątrz."
        }
        if (activeCosmetics.contains(LlumieCosmetic.WIZARD_HAT)) {
            return "Czary nie istnieją, ale Twoja codzienna konsekwencja tworzy prawdziwe cuda."
        }
        if (activeCosmetics.contains(LlumieCosmetic.ROYAL_CROWN)) {
            return "Rządzisz swoimi wyborami jak prawdziwy, mądry władca własnego umysłu."
        }

        // 3. Kamienie milowe serii dni (Streak Milestones)
        if (streakDays >= 120) return "Aż $streakDays dni czystości! Twój dawny nawyk to już tylko odległe wspomnienie."
        if (streakDays >= 90) return "Trzy pełne miesiące wolności! Twoje receptory dopaminy są w pełni zregenerowane."
        if (streakDays >= 60) return "Dwa miesiące dyscypliny! Zbudowałeś nową tożsamość człowieka wolnego."
        if (streakDays >= 30) return "Cały miesiąc! Każdy kolejny dzień utrwala Twoją neurologiczną ścieżkę siły."
        if (streakDays >= 21) return "Trzy tygodnie! Nowe nawyki stały się już Twoją naturalną drugą naturą."
        if (streakDays >= 14) return "Dwa tygodnie za Tobą. Najostrzejszy szczyt fali pokus masz już za plecami."
        if (streakDays >= 7) return "Pełny tydzień konsekwencji! Udowodniłeś sobie, że masz kontrolę nad swoim życiem."
        if (streakDays >= 3) return "Trzeci dzień serii! Twój organizm zaczyna łapać głęboki, uspokajający rytm."

        // 4. Cykl dobowy i mądrości stoickie (Circadian rhythm & Daily Wisdom)
        return when (hourOfDay) {
            in 5..8 -> "Nowy świt, czysta karta. Zróbmy dziś jeden mały, spokojny krok w stronę wolności."
            in 9..11 -> "Pamiętaj o poranku: masz władzę nad swoim umysłem, a nie nad zdarzeniami wokół."
            in 12..14 -> "Południe. Zatrzymaj się na trzy głębokie oddechy i poczuj, jak wiele już osiągnąłeś."
            in 15..17 -> "Popołudniowy spadek energii? Wypij szklankę wody i wyprostuj plecy – zaraz odzyskasz jasność."
            in 18..20 -> "Wieczorny spokój. Dziękuję Ci za kolejny dzień, w którym pozostałeś wierny swoim wartościom."
            in 21..23 -> "Dzień dobiega końca. Czas na wyciszenie – jutro będziemy lśnić jeszcze jaśniej."
            else -> "Cisza nocy. Odłóżmy ekrany i pozwólmy układowi nerwowemu na pełną regenerację."
        }
    }
}
