package com.engin.canvasexamples

import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.dp
import com.engin.canvasexamples.ui.theme.CanvasexamplesTheme

@Composable
fun LineChartApp(modifier: Modifier = Modifier,initialAnimationValue :Float= 0f) {
    CanvasexamplesTheme {
        val animationProgress = remember { androidx.compose.animation.core.Animatable(initialAnimationValue) }
        LaunchedEffect(list) {
            animationProgress.animateTo(1f, tween(3000))
        }
        Scaffold(
            modifier = Modifier.fillMaxSize(), containerColor = MaterialTheme.colorScheme.surface
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                LineChartExample(
                    modifier = Modifier
                        .aspectRatio(3/2f),
                    animateProgress = animationProgress.value
                )
            }

        }
    }
}

@Composable
fun LineChartExample(modifier: Modifier = Modifier, animateProgress: Float) {
    val strokeColor = MaterialTheme.colorScheme.secondary
    Canvas(
        modifier = modifier
            .aspectRatio(3 / 2f)
            .padding(12.dp)
            .fillMaxSize()
    ) {

        val strokeWidthPx = 1.dp.toPx()
        val verticalLine = 4
        val sizeWidth = size.width / 5
        repeat(verticalLine) {
            val startX = sizeWidth * (it + 1)
            drawLine(
                color = strokeColor,
                start = Offset(startX, 0f),
                end = Offset(startX, size.height),
                strokeWidth = strokeWidthPx
            )
        }
        val horizontalLine = 3
        val height = size.height / (horizontalLine + 1)
        repeat(horizontalLine) {
            val startY = height * (it + 1)
            drawLine(
                color = strokeColor,
                start = Offset(0f, startY),
                end = Offset(size.width, startY),
                strokeWidth = strokeWidthPx
            )
        }
        drawRect(
            color = strokeColor,
            style = Stroke(strokeWidthPx)
        )
        clipRect(right = size.width * animateProgress){

            val path = generatePath(list, size)
            val filledPath = Path()
            filledPath.addPath(path)
            filledPath.lineTo(size.width, size.height)
            filledPath.lineTo(0f, size.height)
            filledPath.close()

            val brush =
                Brush.verticalGradient(listOf(Color.Green.copy(alpha = 0.5f), Color.Transparent))

            drawPath(
                path = path,
                color = Color.Green,
                style = Stroke(4.dp.toPx())
            )

            drawPath(
                filledPath,
                brush = brush,
                style = Fill
            )

            // Min ve Max fiyatı bul
            val minPrice = list.minOf { it.price }
            val maxPrice = list.maxOf { it.price }

            val priceRange = maxPrice - minPrice
            val normalize: (Float) -> Float = if (priceRange == 0f) {
                { size.height / 2 }
            } else {
                { price -> size.height - ((price - minPrice) / priceRange) * size.height }
            }

            // Noktaları Çiz
            list.forEachIndexed { i, d ->
                val x = (i.toFloat() / (list.size - 1)) * size.width
                val y = normalize(d.price)

                drawCircle(
                    color = Color.Green,
                    radius = 6.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }
    }
}


data class TestData(
    val price: Float,
)

val list = listOf<TestData>(
    TestData(203.2f),
    TestData(233.2f),
    TestData(238.2f),
    TestData(293.2f),
    TestData(314.2f),
)

fun generatePath(data: List<TestData>, size: Size): Path {
    val path = Path()
    val maxWidth = size.width
    val maxHeight = size.height

    if (data.size < 2) return path // Eğer yeterli veri yoksa hata almamak için

    // Min ve Max fiyatı bul
    val minPrice = data.minOf { it.price }
    val maxPrice = data.maxOf { it.price }

    val priceRange = maxPrice - minPrice
    val normalize: (Float) -> Float = if (priceRange == 0f) {
        { maxHeight / 2 } // Eğer tüm fiyatlar aynıysa, ortada göster
    } else {
        { price -> maxHeight - ((price - minPrice) / priceRange) * maxHeight } // Y ekseni ters çevrildi
    }

    val firstX = 0f
    val firstY = normalize(data.first().price)
    path.moveTo(firstX, firstY)

    for (i in 1 until data.size) {
        val currentX = (i.toFloat() / (data.size - 1)) * maxWidth
        val currentY = normalize(data[i].price)

        val prevX = ((i - 1).toFloat() / (data.size - 1)) * maxWidth
        val prevY = normalize(data[i - 1].price)

        val nextX = if (i < data.size - 1) {
            ((i + 1).toFloat() / (data.size - 1)) * maxWidth
        } else {
            currentX
        }

        val nextY = if (i < data.size - 1) {
            normalize(data[i + 1].price)
        } else {
            currentY
        }

        val controlX1 = prevX + (currentX - prevX) / 3
        val controlY1 = prevY + (currentY - prevY) / 3
        val controlX2 = currentX - (nextX - prevX) / 3
        val controlY2 = currentY - (nextY - prevY) / 3

        path.cubicTo(
            controlX1, controlY1, // İlk kontrol noktası
            controlX2, controlY2, // İkinci kontrol noktası
            currentX, currentY // Hedef nokta
        )
    }

    return path
}
