package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

enum class AppThemeMode(
    val storageKey: String,
    val displayName: String,
    val isLight: Boolean
) {
    MIDNIGHT("MIDNIGHT", "Midnight", isLight = false),
    PAPER_LIGHT("PAPER_LIGHT", "Daylight", isLight = true),
    ESPRESSO("ESPRESSO", "Espresso", isLight = false),
    NORDIC("NORDIC", "Nordic", isLight = false);

    companion object {
        fun fromKey(key: String?): AppThemeMode {
            return entries.firstOrNull { it.storageKey == key } ?: MIDNIGHT
        }
    }
}

@Immutable
data class BuckwheatColors(
    val bgDark: Color,
    val panelSurface: Color,
    val backdropSurface: Color,
    val cardSurface: Color,
    val keySurface: Color,
    val backspaceSurface: Color,
    val divider: Color,
    val onDark: Color,
    val onDarkSecondary: Color,
    val onDarkDisabled: Color,
    val handleColor: Color,
    val blue: Color,
    val blueBright: Color,
    val blueRing: Color,
    val blueRangeBg: Color,
    val onBlue: Color,
    val greenTrack: Color,
    val greenFill: Color,
    val greenText: Color,
    val greenSubText: Color,
    val amberTrack: Color,
    val amberFill: Color,
    val amberText: Color,
    val amberSubText: Color,
    val redTrack: Color,
    val redFill: Color,
    val redText: Color,
    val redSubText: Color,
    val dangerText: Color,
    val plumCardBg: Color,
    val plumText: Color,
    val plumSubText: Color,
    val plumPillBg: Color,
    val plumPillText: Color,
    val datePillBg: Color,
    val datePillText: Color,
    val isLight: Boolean
)

// 1. Original Buckwheat Midnight Dark (default — exact match to screenshots)
val MidnightPalette = BuckwheatColors(
    bgDark = Color(0xFF121316),
    panelSurface = Color(0xFF2A2B2F),
    backdropSurface = Color(0xFF1F2024),
    cardSurface = Color(0xFF26272B),
    keySurface = Color(0xFF1E1F23),
    backspaceSurface = Color(0xFF3A4152),
    divider = Color(0xFF2B2C31),
    onDark = Color(0xFFEAEBF0),
    onDarkSecondary = Color(0xFF9A9CA3),
    onDarkDisabled = Color(0xFF5A5C63),
    handleColor = Color(0xFF686A72),
    blue = Color(0xFF2F4B7E),
    blueBright = Color(0xFF3D5E9C),
    blueRing = Color(0xFF9CB5F5),
    blueRangeBg = Color(0xFF1D2C49),
    onBlue = Color(0xFFFFFFFF),
    greenTrack = Color(0xFF07491E),
    greenFill = Color(0xFF0A662B),
    greenText = Color(0xFF6BE688),
    greenSubText = Color(0xFF56B870),
    amberTrack = Color(0xFF4A3608),
    amberFill = Color(0xFF6B4E0C),
    amberText = Color(0xFFF6CE5E),
    amberSubText = Color(0xFFCFA946),
    redTrack = Color(0xFF4C171B),
    redFill = Color(0xFF6E2228),
    redText = Color(0xFFFF9B94),
    redSubText = Color(0xFFD67872),
    dangerText = Color(0xFFF2B8B5),
    plumCardBg = Color(0xFF463244),
    plumText = Color(0xFFF3CFE9),
    plumSubText = Color(0xFFBAA2B5),
    plumPillBg = Color(0xFFF3CFE9),
    plumPillText = Color(0xFF2B1B29),
    datePillBg = Color(0xFF1E2025),
    datePillText = Color(0xFFD2D4DC),
    isLight = false
)

// 2. Crisp Daylight Light Theme
val PaperLightPalette = BuckwheatColors(
    bgDark = Color(0xFFF4F5F8),
    panelSurface = Color(0xFFFFFFFF),
    backdropSurface = Color(0xFFE6E9F0),
    cardSurface = Color(0xFFEAECEF),
    keySurface = Color(0xFFE6E8EE),
    backspaceSurface = Color(0xFFD5DCEB),
    divider = Color(0xFFDDE0E6),
    onDark = Color(0xFF14171D),
    onDarkSecondary = Color(0xFF606572),
    onDarkDisabled = Color(0xFFA0A5B2),
    handleColor = Color(0xFFC0C4CE),
    blue = Color(0xFF2D55A4),
    blueBright = Color(0xFF214691),
    blueRing = Color(0xFF3E6BC4),
    blueRangeBg = Color(0xFFD9E4FA),
    onBlue = Color(0xFFFFFFFF),
    greenTrack = Color(0xFFD7F2DF),
    greenFill = Color(0xFFB3E8C4),
    greenText = Color(0xFF0A5C28),
    greenSubText = Color(0xFF1F753D),
    amberTrack = Color(0xFFFAF0D2),
    amberFill = Color(0xFFF4DD9A),
    amberText = Color(0xFF7D5300),
    amberSubText = Color(0xFF996B0F),
    redTrack = Color(0xFFFCE0E2),
    redFill = Color(0xFFF7BCBF),
    redText = Color(0xFF9E1C24),
    redSubText = Color(0xFFB8323A),
    dangerText = Color(0xFFC42D35),
    plumCardBg = Color(0xFFF3E2F0),
    plumText = Color(0xFF4F2248),
    plumSubText = Color(0xFF7A4B73),
    plumPillBg = Color(0xFF5C2C55),
    plumPillText = Color(0xFFFDF5FB),
    datePillBg = Color(0xFFEDF0F5),
    datePillText = Color(0xFF2E333E),
    isLight = true
)

// 3. Warm Espresso Dark Theme
val EspressoPalette = BuckwheatColors(
    bgDark = Color(0xFF14100E),
    panelSurface = Color(0xFF28201C),
    backdropSurface = Color(0xFF1D1714),
    cardSurface = Color(0xFF2E241F),
    keySurface = Color(0xFF221B17),
    backspaceSurface = Color(0xFF47372E),
    divider = Color(0xFF362B25),
    onDark = Color(0xFFF5ECE6),
    onDarkSecondary = Color(0xFFB5A296),
    onDarkDisabled = Color(0xFF6B5B52),
    handleColor = Color(0xFF78665B),
    blue = Color(0xFF8C5A3C),
    blueBright = Color(0xFFA86F4D),
    blueRing = Color(0xFFE5B591),
    blueRangeBg = Color(0xFF3E291D),
    onBlue = Color(0xFFFFF8F3),
    greenTrack = Color(0xFF174021),
    greenFill = Color(0xFF226133),
    greenText = Color(0xFF8BE8A2),
    greenSubText = Color(0xFF6CC482),
    amberTrack = Color(0xFF4F3710),
    amberFill = Color(0xFF755218),
    amberText = Color(0xFFFCD674),
    amberSubText = Color(0xFFD9B152),
    redTrack = Color(0xFF4F1B1A),
    redFill = Color(0xFF752927),
    redText = Color(0xFFFFA39E),
    redSubText = Color(0xFFD97D78),
    dangerText = Color(0xFFF5B8B4),
    plumCardBg = Color(0xFF4A312C),
    plumText = Color(0xFFFADBD2),
    plumSubText = Color(0xFFC9A69C),
    plumPillBg = Color(0xFFFADBD2),
    plumPillText = Color(0xFF331B16),
    datePillBg = Color(0xFF1F1815),
    datePillText = Color(0xFFE6D6CC),
    isLight = false
)

// 4. Nordic Ocean Dark Theme
val NordicPalette = BuckwheatColors(
    bgDark = Color(0xFF0D141F),
    panelSurface = Color(0xFF1B2638),
    backdropSurface = Color(0xFF131D2B),
    cardSurface = Color(0xFF1E2B40),
    keySurface = Color(0xFF152030),
    backspaceSurface = Color(0xFF2C3E59),
    divider = Color(0xFF24344C),
    onDark = Color(0xFFEBF2FA),
    onDarkSecondary = Color(0xFF8FA4BF),
    onDarkDisabled = Color(0xFF52647D),
    handleColor = Color(0xFF5D7391),
    blue = Color(0xFF22688A),
    blueBright = Color(0xFF2C83AD),
    blueRing = Color(0xFF7AD4F5),
    blueRangeBg = Color(0xFF16384D),
    onBlue = Color(0xFFFFFFFF),
    greenTrack = Color(0xFF0A4340),
    greenFill = Color(0xFF0F6660),
    greenText = Color(0xFF6EF2DE),
    greenSubText = Color(0xFF54C4B4),
    amberTrack = Color(0xFF4A3912),
    amberFill = Color(0xFF6E551A),
    amberText = Color(0xFFFAD570),
    amberSubText = Color(0xFFD4B050),
    redTrack = Color(0xFF4D1A26),
    redFill = Color(0xFF732638),
    redText = Color(0xFFFF9EB2),
    redSubText = Color(0xFFD9798D),
    dangerText = Color(0xFFF5B8C4),
    plumCardBg = Color(0xFF273352),
    plumText = Color(0xFFD2E2FF),
    plumSubText = Color(0xFF9AB0D6),
    plumPillBg = Color(0xFFD2E2FF),
    plumPillText = Color(0xFF152038),
    datePillBg = Color(0xFF131D2D),
    datePillText = Color(0xFFD2E0F2),
    isLight = false
)

fun paletteForTheme(mode: AppThemeMode): BuckwheatColors = when (mode) {
    AppThemeMode.MIDNIGHT -> MidnightPalette
    AppThemeMode.PAPER_LIGHT -> PaperLightPalette
    AppThemeMode.ESPRESSO -> EspressoPalette
    AppThemeMode.NORDIC -> NordicPalette
}

// Legacy aliases preserved for compatibility
val BuckwheatBgDark = MidnightPalette.bgDark
val BuckwheatPanelSurface = MidnightPalette.panelSurface
val BuckwheatBackdropSurface = MidnightPalette.backdropSurface
val BuckwheatCardSurface = MidnightPalette.cardSurface
val BuckwheatKeySurface = MidnightPalette.keySurface
val BuckwheatBackspaceSurface = MidnightPalette.backspaceSurface
val BuckwheatDivider = MidnightPalette.divider
val BuckwheatOnDark = MidnightPalette.onDark
val BuckwheatOnDarkSecondary = MidnightPalette.onDarkSecondary
val BuckwheatOnDarkDisabled = MidnightPalette.onDarkDisabled
val BuckwheatHandleColor = MidnightPalette.handleColor
val BuckwheatBlue = MidnightPalette.blue
val BuckwheatBlueBright = MidnightPalette.blueBright
val BuckwheatBlueRing = MidnightPalette.blueRing
val BuckwheatBlueRangeBg = MidnightPalette.blueRangeBg
val BuckwheatGreenTrack = MidnightPalette.greenTrack
val BuckwheatGreenFill = MidnightPalette.greenFill
val BuckwheatGreenText = MidnightPalette.greenText
val BuckwheatGreenSubText = MidnightPalette.greenSubText
val BuckwheatAmberTrack = MidnightPalette.amberTrack
val BuckwheatAmberFill = MidnightPalette.amberFill
val BuckwheatAmberText = MidnightPalette.amberText
val BuckwheatAmberSubText = MidnightPalette.amberSubText
val BuckwheatRedTrack = MidnightPalette.redTrack
val BuckwheatRedFill = MidnightPalette.redFill
val BuckwheatRedText = MidnightPalette.redText
val BuckwheatRedSubText = MidnightPalette.redSubText
val BuckwheatDangerText = MidnightPalette.dangerText
val BuckwheatPlumCardBg = MidnightPalette.plumCardBg
val BuckwheatPlumText = MidnightPalette.plumText
val BuckwheatPlumSubText = MidnightPalette.plumSubText
val BuckwheatPlumPillBg = MidnightPalette.plumPillBg
val BuckwheatPlumPillText = MidnightPalette.plumPillText
