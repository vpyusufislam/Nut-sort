package com.example.model

enum class Difficulty(
    val title: String,
    val numColors: Int,
    val capacity: Int,
    val emptyTubes: Int,
    val rows: Int,
    val description: String
) {
    EASY(
        title = "EASY",
        numColors = 4,
        capacity = 4,
        emptyTubes = 2,
        rows = 2,
        description = "4 Colors • 4 Balls per Tube • 2 Empty Tubes"
    ),
    MEDIUM(
        title = "MEDIUM",
        numColors = 6,
        capacity = 4,
        emptyTubes = 2,
        rows = 2,
        description = "6 Colors • 4 Balls per Tube • 2 Empty Tubes"
    ),
    HARD(
        title = "HARD",
        numColors = 8,
        capacity = 4,
        emptyTubes = 2,
        rows = 2,
        description = "8 Colors • 4 Balls per Tube • 2 Empty Tubes"
    );

    val totalTubes: Int
        get() = numColors + emptyTubes

    val emptyRods: Int get() = emptyTubes
    val totalRods: Int get() = totalTubes
}
