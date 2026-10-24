package com.example.game

import com.example.model.BallColor
import com.example.model.HintInfo
import com.example.model.Tube
import java.util.ArrayDeque

object PuzzleSolver {

    /**
     * Finds the best next move hint using heuristic scoring and bounded search.
     */
    fun findHint(tubes: List<Tube>): HintInfo? {
        // Collect all valid moves
        val validMoves = mutableListOf<Pair<Int, Int>>()
        for (i in tubes.indices) {
            val fromTube = tubes[i]
            if (fromTube.balls.isEmpty()) continue
            // If fromTube is already completed and pure, skip moving from it
            if (fromTube.isCompleted) continue

            val movingBall = fromTube.balls.last()

            for (j in tubes.indices) {
                if (i == j) continue
                val toTube = tubes[j]
                if (toTube.isFull) continue

                if (toTube.isEmpty) {
                    // Only move to empty tube if fromTube has mixed colors or hidden balls
                    val hasMixedOrHidden = fromTube.balls.any { it.color != movingBall.color || it.isHidden }
                    if (hasMixedOrHidden) {
                        validMoves.add(Pair(i, j))
                    }
                } else {
                    val targetTop = toTube.balls.last()
                    // If target top ball is visible and colors match
                    if (!targetTop.isHidden && targetTop.color == movingBall.color) {
                        validMoves.add(Pair(i, j))
                    }
                }
            }
        }

        if (validMoves.isEmpty()) return null

        // Score moves:
        // 1. Moving onto a matching non-empty tube gets highest priority (+50)
        // 2. Uncovering a hidden ball underneath gets high priority (+40)
        // 3. Completing a tube gets priority (+60)
        // 4. Moving to an empty tube gets moderate priority (+10)
        val bestMove = validMoves.maxByOrNull { (fromIdx, toIdx) ->
            val fromTube = tubes[fromIdx]
            val toTube = tubes[toIdx]
            val ball = fromTube.balls.last()
            var score = 0

            if (!toTube.isEmpty && toTube.balls.last().color == ball.color) {
                score += 50
                // Check if this move completes the tube
                if (toTube.balls.size + 1 == toTube.capacity && toTube.balls.all { it.color == ball.color && !it.isHidden }) {
                    score += 60
                }
            }

            // Check if uncovered ball is hidden
            if (fromTube.balls.size >= 2 && fromTube.balls[fromTube.balls.size - 2].isHidden) {
                score += 40
            }

            if (toTube.isEmpty) {
                score += 10
            }

            score
        } ?: validMoves.first()

        val fromTube = tubes[bestMove.first]
        val toTube = tubes[bestMove.second]
        val ball = fromTube.balls.last()

        val reason = if (!toTube.isEmpty) {
            "Match ${ball.color.displayName} to Tube ${bestMove.second + 1}"
        } else {
            "Move ${ball.color.displayName} to empty Tube ${bestMove.second + 1}"
        }

        return HintInfo(
            fromTubeIndex = bestMove.first,
            toTubeIndex = bestMove.second,
            reason = reason
        )
    }

    /**
     * Checks if a board state is solvable to completion using fast bounded BFS.
     */
    fun isSolvable(
        initialTubes: List<List<BallColor>>,
        capacity: Int,
        maxStates: Int = 3500
    ): Boolean {
        fun canonical(state: List<List<BallColor>>): String {
            return state.map { tube -> tube.joinToString("") { it.ordinal.toString() } }
                .sorted()
                .joinToString(";")
        }

        fun isGoal(state: List<List<BallColor>>): Boolean {
            for (tube in state) {
                if (tube.isEmpty()) continue
                if (tube.size != capacity) return false
                val first = tube[0]
                if (tube.any { it != first }) return false
            }
            return true
        }

        if (isGoal(initialTubes)) return true

        val queue = ArrayDeque<List<List<BallColor>>>()
        val visited = HashSet<String>()

        val initialCanonical = canonical(initialTubes)
        visited.add(initialCanonical)
        queue.add(initialTubes)

        var statesChecked = 0
        while (queue.isNotEmpty() && statesChecked < maxStates) {
            val curr = queue.poll() ?: break
            statesChecked++

            if (isGoal(curr)) return true

            for (i in curr.indices) {
                val fromTube = curr[i]
                if (fromTube.isEmpty()) continue
                if (fromTube.size == capacity && fromTube.all { it == fromTube[0] }) continue

                val ball = fromTube.last()
                var movedToEmpty = false

                for (j in curr.indices) {
                    if (i == j) continue
                    val toTube = curr[j]
                    if (toTube.size >= capacity) continue

                    val canMove = if (toTube.isEmpty()) {
                        if (movedToEmpty) false else {
                            movedToEmpty = true
                            fromTube.any { it != ball }
                        }
                    } else {
                        toTube.last() == ball
                    }

                    if (canMove) {
                        val nextState = curr.mapIndexed { idx, t ->
                            when (idx) {
                                i -> t.dropLast(1)
                                j -> t + ball
                                else -> t
                            }
                        }
                        val cano = canonical(nextState)
                        if (visited.add(cano)) {
                            if (isGoal(nextState)) return true
                            queue.add(nextState)
                        }
                    }
                }
            }
        }
        return false
    }
}
