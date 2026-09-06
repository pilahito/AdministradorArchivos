package com.cloudterm.ui.theme

/**
 * Tokens de color multiplataforma (Android Compose / desktop / web).
 * Valores ARGB 0xAARRGGBB.
 */
data class ThemePalette(
    val id: String,
    val displayName: String,
    val background: Long,
    val surface: Long,
    val surfaceVariant: Long,
    val primary: Long,
    val secondary: Long,
    val accent: Long,
    val onBackground: Long,
    val onSurface: Long,
    val muted: Long,
    val error: Long,
    val success: Long,
    val border: Long
)

object CloudTermThemes {
    /** Oscuro cian / neÃ³n (identidad CloudTerm Pro). */
    val DarkCyanNeon = ThemePalette(
        id = "dark_cyan_neon",
        displayName = "Oscuro cian/verde neon",
        background = 0xFF0F172A,
        surface = 0xFF1E293B,
        surfaceVariant = 0xFF334155,
        primary = 0xFF00F0FF,
        secondary = 0xFF00E676,
        accent = 0xFF7C6CF0,
        onBackground = 0xFFE2E8F0,
        onSurface = 0xFFE2E8F0,
        muted = 0xFF94A3B8,
        error = 0xFFEF4444,
        success = 0xFF00E676,
        border = 0xFF334155
    )

    val Dracula = ThemePalette(
        id = "dracula",
        displayName = "Dracula",
        background = 0xFF282A36,
        surface = 0xFF44475A,
        surfaceVariant = 0xFF6272A4,
        primary = 0xFFBD93F9,
        secondary = 0xFF50FA7B,
        accent = 0xFFFF79C6,
        onBackground = 0xFFF8F8F2,
        onSurface = 0xFFF8F8F2,
        muted = 0xFF6272A4,
        error = 0xFFFF5555,
        success = 0xFF50FA7B,
        border = 0xFF6272A4
    )

    val SolarizedLight = ThemePalette(
        id = "solarized_light",
        displayName = "Solarized Light",
        background = 0xFFFDF6E3,
        surface = 0xFFEEE8D5,
        surfaceVariant = 0xFF93A1A1,
        primary = 0xFF268BD2,
        secondary = 0xFF2AA198,
        accent = 0xFFD33682,
        onBackground = 0xFF657B83,
        onSurface = 0xFF586E75,
        muted = 0xFF93A1A1,
        error = 0xFFDC322F,
        success = 0xFF859900,
        border = 0xFF93A1A1
    )

    val Cyberpunk = ThemePalette(
        id = "cyberpunk",
        displayName = "Cyberpunk neon",
        background = 0xFF0D0221,
        surface = 0xFF1A0A2E,
        surfaceVariant = 0xFF2E1A47,
        primary = 0xFFFCEE0A,
        secondary = 0xFFFF2A6D,
        accent = 0xFF05D9E8,
        onBackground = 0xFFD1F7FF,
        onSurface = 0xFFD1F7FF,
        muted = 0xFF7A6B8A,
        error = 0xFFFF2A6D,
        success = 0xFF39FF14,
        border = 0xFF05D9E8
    )

    val all: List<ThemePalette> = listOf(DarkCyanNeon, Dracula, SolarizedLight, Cyberpunk)

    fun byId(id: String): ThemePalette =
        all.firstOrNull { it.id == id } ?: DarkCyanNeon
}
