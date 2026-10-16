package com.cllarity.app.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// NOWA ARCHITEKTURA BARW: "Ciche tło, jedno światło"
// ============================================================================

// 1. GŁĘBIA NEUTRALNA (stonowany grafit bez chłodnego, plastikowego zasinienia)
val MidnightBackground = Color(0xFF0C0D11)       // Ciche, głębokie tło aplikacji
val MidnightSurface = Color(0xFF13151A)          // Pierwszy poziom wyniesienia
val MidnightSurfaceElevated = Color(0xFF191C23)  // Karty i interaktywne kafelki
val MidnightSurfaceCard = Color(0xFF20242D)      // Wewnętrzne segmenty (tło liczników, wskaźniki)
val MidnightSurfaceHighlight = Color(0xFF282D38) // Aktywny hover / wciśnięcie

// 2. SUBTELNA PRZEJRZYSTOŚĆ (optyczna głębia zamiast grubych, plastikowych kresek)
val GlassBorder = Color(0x18FFFFFF)              // Dyskretna linia światła na krawędzi (10% bieli)
val GlassBorderSubtle = Color(0x0EFFFFFF)        // Ledwo widoczny obrys wewnątrz kart (5% bieli)
val GlassBorderLumen = Color(0x33F2CF8B)         // Ciepła krawędź akcentu

// 3. JEDYNY CIEPŁY AKCENT ("Lumen" – światło Llumie, postęp, wierność wartościom)
// Zamiast pstrokatej tęczy (indygo + szmaragd + bursztyn) – jedna świetlista barwa
val AccentLumen = Color(0xFFF2CF8B)              // Ciepłe złocisto-słoneczne światło nadziei
val AccentLumenLight = Color(0xFFFBE4B5)         // Rozbłysk światła
val AccentLumenGlow = Color(0x28F2CF8B)          // Ambient glow za Llumie i podświetlenie postępu
val AccentLumenSubtle = Color(0x14F2CF8B)        // Ciche tło pigułek i badgy

// 4. SEMANTYKA RATUNKOWA (SOS, ostrzeżenie – stonowany koral, nie agresywna czerwień)
val SemanticAlert = Color(0xFFE57373)            // Tylko dla przycisku SOS i resetu nałogu
val SemanticAlertBg = Color(0x1CE57373)          // Dyskretne tło ostrzeżenia
val SemanticAlertBorder = Color(0x40E57373)

// 5. CZYSTA, CIEPŁA TYPOGRAFIA
val TextPrimary = Color(0xFFF5F4F0)              // Ciepła, czytelna biel o wysokim kontraście
val TextSecondary = Color(0xFF9E9FA6)            // Zbalansowana szarość o optymalnej czytelności
val TextTertiary = Color(0xFF6B6C75)             // Stonowane etykiety pomocnicze, jednostki

// ============================================================================
// ALIASY DLA WSTECZNEJ KOMPATYBILNOŚCI (stopniowa migracja bez łamania kodu)
// ============================================================================
val AccentIndigo = AccentLumen
val AccentIndigoLight = AccentLumenLight
val AccentEmerald = AccentLumen
val AccentEmeraldLight = AccentLumenLight
val AccentAmber = AccentLumen
val AccentAmberLight = AccentLumenLight
val AccentRose = SemanticAlert
val AccentCyan = AccentLumen
val AccentViolet = AccentLumen

// Llumie Palette (maskotka zachowuje swoją unikalną tożsamość i urok)
val LlumieBodyLight = Color(0xFF818CF8)
val LlumieBodyBase = Color(0xFF4F46E5)
val LlumieBodyShadow = Color(0xFF3730A3)
val LlumieGlow = Color(0x40F2CF8B)               // Ciepły glow harmonizujący z akcentem
val LlumieCheeks = Color(0x99FB7185)
val LlumieHalo = Color(0xFFFFE082)
val LlumieLeaf = Color(0xFF34D399)
val LlumieScarf = Color(0xFFF43F5E)

// Capybara Llumie Palette
val CapybaraFurLight = Color(0xFFA67449)
val CapybaraFurBase = Color(0xFF85542E)
val CapybaraFurShadow = Color(0xFF5E381A)
val CapybaraMuzzleLight = Color(0xFFEADBCE)
val CapybaraMuzzleBase = Color(0xFFCCAFA0)
val CapybaraSnoutDark = Color(0xFF2B1910)
val CapybaraEarInner = Color(0xFFB57D71)
val CapybaraYuzuOrange = Color(0xFFF59E0B)
val CapybaraYuzuLeaf = Color(0xFF10B981)

// Duszek Światła (Spirit of Hope) Palette
val LlumieSpiritCore = Color(0xFFFFFBEB)
val LlumieSpiritCoreNeon = Color(0xFFFDE047)
val LlumieSpiritCyan = Color(0xFF67E8F9)
val LlumieSpiritLavender = Color(0xFFC4B5FD)
val LlumieSpiritLilac = Color(0xFFA78BFA)
val LlumieSpiritViolet = Color(0xFF7C3AED)
val LlumieSpiritNight = Color(0xFF1E153A)
val LlumieSpiritMist = Color(0xFF4C1D95)
val LlumieSpiritAura = Color(0x40F2CF8B)
