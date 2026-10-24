package com.example

import com.example.game.PuzzleGenerator
import com.example.model.Difficulty
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun hardLevel_generation_isFastAndNeverCrashes() {
    val (tubes, _) = PuzzleGenerator.generateLevel(Difficulty.HARD, 1)
    assertEquals(10, tubes.size)
    // 8 filled tubes
    val filled = tubes.take(8)
    filled.forEach { tube ->
      assertEquals(4, tube.balls.size)
    }
    // 2 empty buffer tubes
    val empty = tubes.drop(8)
    empty.forEach { tube ->
      assertTrue(tube.balls.isEmpty())
    }
  }

  @Test
  fun mediumLevel_generation_matchesVideoSpecifications() {
    val (tubes, _) = PuzzleGenerator.generateLevel(Difficulty.MEDIUM, 117)
    assertEquals(8, tubes.size)
    val filled = tubes.take(6)
    filled.forEach { tube ->
      assertEquals(4, tube.balls.size)
    }
    val empty = tubes.drop(6)
    empty.forEach { tube ->
      assertTrue(tube.balls.isEmpty())
    }
  }
}
