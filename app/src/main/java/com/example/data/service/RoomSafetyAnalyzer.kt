package com.example.data.service

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class RoomSafetyAssessment(val isDanger: Boolean)

fun interface RoomSafetyAnalyzer {
    suspend fun analyze(bitmap: Bitmap): RoomSafetyAssessment
}

/**
 * Replaceable on-device baseline. Production deployments should inject a trained vision model.
 */
class VisualHeuristicRoomSafetyAnalyzer : RoomSafetyAnalyzer {
    override suspend fun analyze(bitmap: Bitmap): RoomSafetyAssessment = withContext(Dispatchers.Default) {
        require(bitmap.width > 0 && bitmap.height > 0) { "The selected image is empty." }

        val sampleWidth = minOf(bitmap.width, 100)
        val sampleHeight = minOf(bitmap.height, 100)
        val sampledPixels = IntArray(sampleWidth * sampleHeight)
        var index = 0

        for (sampleY in 0 until sampleHeight) {
            val y = sampleY * bitmap.height / sampleHeight
            for (sampleX in 0 until sampleWidth) {
                val x = sampleX * bitmap.width / sampleWidth
                sampledPixels[index++] = bitmap.getPixel(x, y)
            }
        }

        RoomSafetyColorClassifier.classify(sampledPixels)
    }
}

object RoomSafetyColorClassifier {
    fun classify(pixels: IntArray): RoomSafetyAssessment {
        require(pixels.isNotEmpty()) { "No image pixels are available for analysis." }
        var flamePixels = 0
        var smokePixels = 0

        for (pixel in pixels) {
            val red = (pixel ushr 16) and 0xff
            val green = (pixel ushr 8) and 0xff
            val blue = pixel and 0xff
            val chroma = maxOf(red, green, blue) - minOf(red, green, blue)

            if (red >= 145 && green >= 45 && red > green * 1.2 && blue < minOf(145f, green * 0.9f)) {
                flamePixels++
            }
            if (chroma < 36 && red in 25..155) {
                smokePixels++
            }
        }

        val flameRatio = flamePixels.toFloat() / pixels.size
        val smokeRatio = smokePixels.toFloat() / pixels.size
        return RoomSafetyAssessment(
            isDanger = flameRatio >= 0.035f ||
                (flameRatio >= 0.012f && smokeRatio >= 0.18f) ||
                smokeRatio >= 0.42f
        )
    }
}
