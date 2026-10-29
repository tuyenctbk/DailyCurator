package com.example.ui.components.donation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GalleryBlack
import com.example.ui.theme.GoldAccent
import kotlin.random.Random

@Composable
fun PayPalQRCodeView(
    qrContent: String,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 180.dp
) {
    // Generate deterministic 21x21 QR matrix based on content seed
    val matrixSize = 21
    val qrMatrix = remember(qrContent) {
        val matrix = Array(matrixSize) { BooleanArray(matrixSize) }
        val random = Random(qrContent.hashCode())

        // Fill background pattern
        for (r in 0 until matrixSize) {
            for (c in 0 until matrixSize) {
                matrix[r][c] = random.nextFloat() > 0.45f
            }
        }

        // Apply Finder Patterns (3 corners: Top-Left, Top-Right, Bottom-Left)
        fun applyFinderPattern(startR: Int, startC: Int) {
            for (r in 0..6) {
                for (c in 0..6) {
                    val isBorder = r == 0 || r == 6 || c == 0 || c == 6
                    val isCenter = r in 2..4 && c in 2..4
                    matrix[startR + r][startC + c] = isBorder || isCenter
                }
            }
            // Clear quiet zone around finder
            for (r in -1..7) {
                for (c in -1..7) {
                    val targetR = startR + r
                    val targetC = startC + c
                    if (targetR in 0 until matrixSize && targetC in 0 until matrixSize) {
                        val isInsideFinder = r in 0..6 && c in 0..6
                        if (!isInsideFinder) {
                            matrix[targetR][targetC] = false
                        }
                    }
                }
            }
        }

        applyFinderPattern(0, 0)
        applyFinderPattern(0, matrixSize - 7)
        applyFinderPattern(matrixSize - 7, 0)

        // Clear center area for gold emblem
        val centerStart = 8
        val centerEnd = 12
        for (r in centerStart..centerEnd) {
            for (c in centerStart..centerEnd) {
                matrix[r][c] = false
            }
        }

        matrix
    }

    Box(
        modifier = modifier
            .size(sizeDp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(2.dp, GoldAccent, RoundedCornerShape(12.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().aspectRatio(1f)) {
            val moduleWidth = size.width / matrixSize
            val moduleHeight = size.height / matrixSize

            for (r in 0 until matrixSize) {
                for (c in 0 until matrixSize) {
                    if (qrMatrix[r][c]) {
                        drawRect(
                            color = GalleryBlack,
                            topLeft = Offset(c * moduleWidth, r * moduleHeight),
                            size = Size(moduleWidth, moduleHeight)
                        )
                    }
                }
            }

            // Draw Central Gold Emblem Overlay
            val centerSize = moduleWidth * 5
            val centerOffset = Offset((size.width - centerSize) / 2f, (size.height - centerSize) / 2f)

            drawRoundRect(
                color = Color.White,
                topLeft = centerOffset,
                size = Size(centerSize, centerSize),
                cornerRadius = CornerRadius(8f, 8f)
            )

            drawRoundRect(
                color = GoldAccent,
                topLeft = Offset(centerOffset.x + 4f, centerOffset.y + 4f),
                size = Size(centerSize - 8f, centerSize - 8f),
                cornerRadius = CornerRadius(6f, 6f)
            )

            // Canvas P emblem path
            val path = Path().apply {
                val startX = centerOffset.x + centerSize * 0.35f
                val startY = centerOffset.y + centerSize * 0.25f
                moveTo(startX, startY)
                lineTo(startX, startY + centerSize * 0.5f)
                moveTo(startX, startY)
                lineTo(startX + centerSize * 0.22f, startY)
                cubicTo(
                    startX + centerSize * 0.35f, startY,
                    startX + centerSize * 0.35f, startY + centerSize * 0.25f,
                    startX + centerSize * 0.22f, startY + centerSize * 0.25f
                )
                lineTo(startX, startY + centerSize * 0.25f)
            }
            drawPath(path = path, color = GalleryBlack, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f))
        }
    }
}
