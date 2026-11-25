package com.example.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object QrCodeMatrix {
    /**
     * Generates a deterministic 25x25 QR-style matrix with 3 standard 7x7 finder patterns
     * and data modules derived from the UPI payload so the checkout QR updates cleanly
     * with amount and UPI ID.
     */
    fun generate(payload: String, size: Int = 25): Array<BooleanArray> {
        val matrix = Array(size) { BooleanArray(size) }

        fun drawFinder(top: Int, left: Int) {
            for (r in 0 until 7) {
                for (c in 0 until 7) {
                    val isOuter = r == 0 || r == 6 || c == 0 || c == 6
                    val isInner = r in 2..4 && c in 2..4
                    matrix[top + r][left + c] = isOuter || isInner
                }
            }
        }

        drawFinder(0, 0)
        drawFinder(0, size - 7)
        drawFinder(size - 7, 0)

        // Timing strips
        for (i in 8 until size - 8) {
            matrix[6][i] = i % 2 == 0
            matrix[i][6] = i % 2 == 0
        }

        // Alignment square near bottom-right
        for (r in size - 9 until size - 4) {
            for (c in size - 9 until size - 4) {
                val dr = r - (size - 9)
                val dc = c - (size - 9)
                matrix[r][c] = dr == 0 || dr == 4 || dc == 0 || dc == 4 || (dr == 2 && dc == 2)
            }
        }

        // Deterministic PRNG seeded by payload hash
        var seed = payload.hashCode().toLong() xor 0x5DEECE66DL
        for (r in 0 until size) {
            for (c in 0 until size) {
                val inTopLeft = r < 8 && c < 8
                val inTopRight = r < 8 && c >= size - 8
                val inBottomLeft = r >= size - 8 && c < 8
                val inTiming = r == 6 || c == 6
                val inAlign = r in (size - 9) until (size - 4) && c in (size - 9) until (size - 4)
                if (!inTopLeft && !inTopRight && !inBottomLeft && !inTiming && !inAlign) {
                    seed = (seed * 6364136223846793005L + 1442695040888963407L)
                    matrix[r][c] = ((seed ushr 32) and 1L) == 1L
                }
            }
        }
        return matrix
    }

    /**
     * Copies a user-selected Photo Picker image into app-private storage so the
     * image URI persists reliably across app restarts without storage permissions.
     */
    fun copyUriToInternalFile(context: Context, uri: Uri, fileNamePrefix: String): String? {
        return runCatching {
            val destDir = File(context.filesDir, "wingwing_media").apply { mkdirs() }
            val destFile = File(destDir, "${fileNamePrefix}_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        }.getOrNull()
    }
}
