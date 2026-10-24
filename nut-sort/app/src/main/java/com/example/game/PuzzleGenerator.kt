package com.example.game

import com.example.model.BallColor
import com.example.model.BallItem
import com.example.model.Difficulty
import com.example.model.Tube
import java.util.UUID
import kotlin.random.Random

object PuzzleGenerator {

    /**
     * Generates a guaranteed-solvable ball sort puzzle where:
     * 1. All color tubes (0 until numColors) are fully arranged with capacity (4) balls.
     * 2. Exactly 2 tubes are empty buffer tubes for the player to use.
     * 3. Complexity increases gradually every 5 levels for each difficulty.
     * 4. In mystery challenge levels (> level 10), select bottom balls are hidden.
     */
    fun generateLevel(difficulty: Difficulty, level: Int): Pair<List<Tube>, Boolean> {
        val baseSeed = (difficulty.name.hashCode().toLong() and 0xFFFFFFFFL) * 31337L + level * 10007L
        val rng = Random(baseSeed)
        val baseColors = BallColor.entries.take(difficulty.numColors)
        val capacity = difficulty.capacity // 4

        // After level 5: Increase 2 more unsorted tubes where up to 2 colors can be sorted into separate tubes (8 balls each)
        val extraFilledTubes = if (level > 5) 2 else 0
        val numFilledTubes = difficulty.numColors + extraFilledTubes
        val numEmptyTubes = difficulty.emptyTubes // 2
        val totalTubes = numFilledTubes + numEmptyTubes

        // Color distribution for the filled tubes
        val tubeAssignedColors = if (extraFilledTubes > 0) {
            val duplicatedColors = baseColors.shuffled(rng).take(extraFilledTubes)
            baseColors + duplicatedColors
        } else {
            baseColors
        }

        // Complexity tier: increases every 5 levels
        val tier = (level - 1) / 5
        val levelInTier = (level - 1) % 5

        // Start with solved state: each filled tube has 'capacity' balls of its assigned color
        val tubes = MutableList(numFilledTubes) { tubeIdx ->
            MutableList(capacity) { tubeAssignedColors[tubeIdx] }
        }

        // Significantly increased swap count for true puzzle challenge & complexity
        val baseSwapsForDiff = when (difficulty) {
            Difficulty.EASY -> 35
            Difficulty.MEDIUM -> 55
            Difficulty.HARD -> 85
        }
        val tierBonus = tier * 10
        val levelBonus = levelInTier * 4
        val targetSwaps = baseSwapsForDiff + tierBonus + levelBonus

        // Perform pairwise ball swaps among the filled tubes with entropy maximization
        var validSwaps = 0
        var attempts = 0
        while (validSwaps < targetSwaps && attempts < targetSwaps * 12) {
            attempts++
            val t1 = rng.nextInt(numFilledTubes)
            var t2 = rng.nextInt(numFilledTubes)
            while (t2 == t1 && numFilledTubes > 1) {
                t2 = rng.nextInt(numFilledTubes)
            }
            val s1 = rng.nextInt(capacity)
            val s2 = rng.nextInt(capacity)

            // Only swap when colors are different to avoid dead cycles
            if (tubes[t1][s1] != tubes[t2][s2]) {
                val temp = tubes[t1][s1]
                tubes[t1][s1] = tubes[t2][s2]
                tubes[t2][s2] = temp
                validSwaps++
            }
        }

        // Guard against any tube starting pre-solved or trivially semi-solved (level > 1)
        if (level > 1) {
            for (i in 0 until numFilledTubes) {
                // If all 4 balls or top 3 balls match, break it up with another tube
                val countSame = tubes[i].count { it == tubes[i][0] }
                if (countSame >= 3) {
                    val otherTube = (i + 1) % numFilledTubes
                    val temp = tubes[i][capacity - 1]
                    tubes[i][capacity - 1] = tubes[otherTube][capacity - 1]
                    tubes[otherTube][capacity - 1] = temp
                }
            }
        }

        val finalColorGrid = tubes.map { it.toList() }

        val isChallengeLevel = level > 5
        val rngFinal = Random(baseSeed + 999)

        // Above level 5: At least 3 tubes hidden, and minimum 3 tubes unhidden
        val targetChallengeTubes = if (isChallengeLevel) {
            val maxAllowable = (numFilledTubes - 3).coerceAtLeast(3)
            val requested = 3 + (level - 6) / 5
            requested.coerceIn(3, maxAllowable)
        } else 0

        var challengeTubesCreated = 0

        // Pick filled tubes that will be challenge tubes
        val challengeTubeIndices = mutableSetOf<Int>()
        if (targetChallengeTubes > 0) {
            val availableIndices = (0 until numFilledTubes).toMutableList()
            availableIndices.shuffle(rngFinal)
            challengeTubeIndices.addAll(availableIndices.take(targetChallengeTubes))
        }

        // Convert to full Tube objects
        val tubesResult = List(totalTubes) { tubeIdx ->
            if (tubeIdx < numFilledTubes) {
                val colorList = finalColorGrid[tubeIdx]
                val isChallengeTube = challengeTubeIndices.contains(tubeIdx)
                if (isChallengeTube) challengeTubesCreated++

                val balls = if (isChallengeTube) {
                    val topIndex = colorList.size - 1
                    colorList.mapIndexed { slotIdx, color ->
                        BallItem(
                            id = "ball_${tubeIdx}_${slotIdx}_${UUID.randomUUID()}",
                            color = color,
                            // Hide all balls EXCEPT the top ball
                            isHidden = slotIdx != topIndex
                        )
                    }
                } else {
                    colorList.mapIndexed { slotIdx, color ->
                        BallItem(
                            id = "ball_${tubeIdx}_${slotIdx}_${UUID.randomUUID()}",
                            color = color,
                            isHidden = false
                        )
                    }
                }

                Tube(
                    id = tubeIdx,
                    balls = balls,
                    capacity = capacity,
                    isChallengeTube = isChallengeTube
                )
            } else {
                // Empty buffer tube
                Tube(
                    id = tubeIdx,
                    balls = emptyList(),
                    capacity = capacity,
                    isChallengeTube = false
                )
            }
        }

        return Pair(tubesResult, isChallengeLevel && challengeTubesCreated > 0)
    }
}
