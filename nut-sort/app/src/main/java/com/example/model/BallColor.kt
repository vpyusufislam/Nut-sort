package com.example.model

import androidx.compose.ui.graphics.Color

/**
 * Vibrant, glossy glass marble ball colors matching the video reference.
 * Note: Brown is strictly omitted as requested.
 */
enum class BallColor(
    val displayName: String,
    val primaryColor: Color,
    val darkShade: Color,
    val lightShade: Color,
    val highlightGlint: Color = Color.White
) {
    RED(
        displayName = "Ruby Red",
        primaryColor = Color(0xFFFF1744),
        darkShade = Color(0xFF99001D),
        lightShade = Color(0xFFFF617F)
    ),
    YELLOW(
        displayName = "Sunny Yellow",
        primaryColor = Color(0xFFFFD600),
        darkShade = Color(0xFFB29500),
        lightShade = Color(0xFFFFF176)
    ),
    GREEN(
        displayName = "Lime Green",
        primaryColor = Color(0xFF00E676),
        darkShade = Color(0xFF008C42),
        lightShade = Color(0xFF69F0AE)
    ),
    CYAN(
        displayName = "Turquoise Cyan",
        primaryColor = Color(0xFF00E5FF),
        darkShade = Color(0xFF008EA0),
        lightShade = Color(0xFF84FFFF)
    ),
    BLUE(
        displayName = "Royal Blue",
        primaryColor = Color(0xFF2979FF),
        darkShade = Color(0xFF0043A8),
        lightShade = Color(0xFF82B1FF)
    ),
    ORANGE(
        displayName = "Citrus Orange",
        primaryColor = Color(0xFFFF6D00),
        darkShade = Color(0xFFA84500),
        lightShade = Color(0xFFFFA040)
    ),
    PURPLE(
        displayName = "Vivid Purple",
        primaryColor = Color(0xFFAA00FF),
        darkShade = Color(0xFF600094),
        lightShade = Color(0xFFE040FB)
    ),
    GREY(
        displayName = "Pearl Silver",
        primaryColor = Color(0xFFB0BEC5),
        darkShade = Color(0xFF546E7A),
        lightShade = Color(0xFFECEFF1)
    ),
    PINK(
        displayName = "Hot Pink",
        primaryColor = Color(0xFFFF4081),
        darkShade = Color(0xFFB00049),
        lightShade = Color(0xFFFF80AB)
    )
}

typealias NutColor = BallColor
