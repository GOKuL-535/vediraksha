package com.example

import com.example.data.service.RoomSafetyColorClassifier
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun classifierFlagsImageContainingFireColoredPixels() {
    val fire = 0xFFF05512.toInt()
    val room = 0xFFE1EBF0.toInt()
    val pixels = IntArray(100) { index -> if (index < 10) fire else room }

    assertTrue(RoomSafetyColorClassifier.classify(pixels).isDanger)
  }

  @Test
  fun classifierLeavesCleanRoomImageSafe() {
    val pixels = IntArray(10_000) { index ->
      when (index % 3) {
        0 -> 0xFF288C91.toInt()
        1 -> 0xFFDED3C6.toInt()
        else -> 0xFF195CA0.toInt()
      }
    }

    assertFalse(RoomSafetyColorClassifier.classify(pixels).isDanger)
  }

  @Test
  fun classifierDoesNotMistakeRoseColoredRoomFurnishingsForFire() {
    val pixels = IntArray(10_000) { index ->
      if (index < 1_500) 0xFFBE7882.toInt() else 0xFFE1EBF0.toInt()
    }

    assertFalse(RoomSafetyColorClassifier.classify(pixels).isDanger)
  }
}
