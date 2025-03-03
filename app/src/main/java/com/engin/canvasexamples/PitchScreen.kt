package com.engin.canvasexamples

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.engin.canvasexamples.PitchViewModel.Companion.PitchHorizontalPadding
import com.engin.canvasexamples.PitchViewModel.Companion.PitchVerticalPadding
import com.engin.canvasexamples.PitchViewModel.Companion.StrokeWidth

val PitchColor = Color(0xFF4c8527)

@SuppressLint("ReturnFromAwaitPointerEventScope")
@Composable
fun PitchScreen(
    modifier: Modifier = Modifier,
    state: PitchState,
    onPointerChange: (PointerInputChange) -> Unit
) {

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = PitchColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            event.changes.forEach { change ->
                                onPointerChange(change)
                            }
                        }
                    }

                })
        {
            Row(
                modifier = Modifier.align(Alignment.TopEnd).zIndex(2f)
            ) {
                Text(
                    text = "TUR ${state.playerOneScore} - ${state.playerTwoScore} AZER ",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Pitch(
                playerOneOffset = state.playerOneOffset,
                playerTwoOffset = state.playerTwoOffset,
                ballOffset = state.ballOffset
            )
        }
    }
}

@Composable
fun Pitch(
    modifier: Modifier = Modifier,
    playerOneOffset: Offset,
    playerTwoOffset: Offset,
    ballOffset: Offset
) {
    val context = LocalContext.current
    val ballBitmap =
        remember { getBitmapFromImage(context, R.drawable.ball, 100, 100).asImageBitmap() }
    val azerFlag = remember {
        getBitmapFromImage(
            context,
            R.drawable.azerbaijan_flag,
            150,
            150
        ).asImageBitmap()
    }
    val trFlag =
        remember {
            getBitmapFromImage(context, R.drawable.turkey_flag, 267, 150).asImageBitmap()
        }

    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        drawPitchArea()
        drawPlayerOne(playerOneOffset, trFlag)
        drawPlayerTwo(playerTwoOffset, azerFlag)
        drawBall(ballOffset, ballBitmap)
    }
}


@Preview
@Composable
fun PitchPreview(modifier: Modifier = Modifier) {
    val verticalPadding = with(LocalDensity.current) { PitchVerticalPadding.toPx() }
    val horizontalPadding = with(LocalDensity.current) { PitchHorizontalPadding.toPx() }
    PitchScreen(
        state = PitchState(
            1080f,
            1920f,
            verticalPadding,
            horizontalPadding
        ),
        onPointerChange = {}
    )
}


fun DrawScope.drawPitchArea() {
    drawRect(
        color = PitchColor
    )
    val horizontalPitchPadding = PitchHorizontalPadding.toPx()
    val verticalPitchPadding = PitchVerticalPadding.toPx()
    drawRect(
        topLeft = Offset(horizontalPitchPadding, verticalPitchPadding),
        size = Size(
            size.width - 2 * horizontalPitchPadding,
            size.height - 2 * verticalPitchPadding
        ),
        color = Color.White,
        style = Stroke(StrokeWidth.toPx())
    )
    drawGoals()
    drawCenterArea()
    drawPenaltyAreas()
    drawCornerAreas()
}

fun DrawScope.drawGoals() {
    val goalStroke = 8.dp.toPx()
    drawLine(
        color = Color.Black,
        start = Offset(
            x = size.width / 2 - 175f,
            y = PitchVerticalPadding.toPx() - 100f
        ),
        end = Offset(
            x = size.width / 2 - 175f,
            y = PitchVerticalPadding.toPx()
        ),
        strokeWidth = goalStroke
    )
    drawLine(
        color = Color.Black,
        start = Offset(
            x = size.width / 2 + 175f,
            y = PitchVerticalPadding.toPx() - 100f
        ),
        end = Offset(
            x = size.width / 2 + 175f,
            y = PitchVerticalPadding.toPx()
        ),
        strokeWidth = goalStroke
    )
    drawLine(
        color = Color.Black,
        start = Offset(
            x = size.width / 2 - 175f,
            y = size.height - (PitchVerticalPadding.toPx())
        ),
        end = Offset(
            x = size.width / 2 - 175f,
            y = size.height - (PitchVerticalPadding.toPx()) + 100f
        ),
        strokeWidth = goalStroke,
    )
    drawLine(
        color = Color.Black,
        start = Offset(
            x = size.width / 2 + 175f,
            y = size.height - (PitchVerticalPadding.toPx())
        ),
        end = Offset(
            x = size.width / 2 + 175f,
            y = size.height - (PitchVerticalPadding.toPx()) + 100f
        ),
        strokeWidth = goalStroke
    )
    drawLine(
        color = Color.Black,
        start = Offset(
            x = size.width / 2 - 185f,
            y = size.height - (PitchVerticalPadding.toPx()) + 100f
        ),
        end = Offset(
            x = size.width / 2 + 185f,
            y = size.height - (PitchVerticalPadding.toPx()) + 100f
        ),
        strokeWidth = goalStroke
    )
    drawLine(
        color = Color.Black,
        start = Offset(
            x = size.width / 2 - 185f,
            y = PitchVerticalPadding.toPx() - 100f
        ),
        end = Offset(
            x = size.width / 2 + 185f,
            y = PitchVerticalPadding.toPx() - 100f
        ),
        strokeWidth = goalStroke
    )
}

fun DrawScope.drawCenterArea() {
    halfwayArea()
}

fun DrawScope.drawCornerAreas() {
    drawArc(
        color = Color.White,
        startAngle = 0f,
        sweepAngle = 90f,
        topLeft = Offset(PitchHorizontalPadding.toPx() - 40f, PitchVerticalPadding.toPx() - 40f),
        size = Size(80f, 80f),
        useCenter = false,
        style = Stroke(StrokeWidth.toPx())
    )

    drawArc(
        color = Color.White,
        startAngle = 180f,
        sweepAngle = -90f,
        topLeft = Offset(
            size.width - PitchHorizontalPadding.toPx() - 40f,
            PitchVerticalPadding.toPx() - 40f
        ),
        size = Size(80f, 80f),
        useCenter = false,
        style = Stroke(StrokeWidth.toPx())
    )

    drawArc(
        color = Color.White,
        startAngle = 0f,
        sweepAngle = -90f,
        topLeft = Offset(
            PitchHorizontalPadding.toPx() - 40f,
            size.height - PitchVerticalPadding.toPx() - 40f
        ),
        size = Size(80f, 80f),
        useCenter = false,
        style = Stroke(StrokeWidth.toPx())
    )

    drawArc(
        color = Color.White, startAngle = 180f, sweepAngle = 90f, topLeft = Offset(
            size.width - PitchHorizontalPadding.toPx() - 40f,
            size.height - PitchVerticalPadding.toPx() - 40f
        ), size = Size(80f, 80f), useCenter = false, style = Stroke(StrokeWidth.toPx())
    )
}

fun DrawScope.drawPenaltyAreas() {
    drawRect(
        color = Color.White,
        size = Size(250f, 100f),
        topLeft = Offset(size.width / 2 - 125f, PitchVerticalPadding.toPx()),
        style = Stroke(StrokeWidth.toPx())
    )

    drawRect(
        color = Color.White,
        size = Size(250f, 100f),
        topLeft = Offset(size.width / 2 - 125f, size.height - 100f - PitchVerticalPadding.toPx()),
        style = Stroke(StrokeWidth.toPx())
    )

    drawRect(
        color = Color.White,
        size = Size(500f, 250f),
        topLeft = Offset(size.width / 2 - 250f, size.height - 250f - PitchVerticalPadding.toPx()),
        style = Stroke(StrokeWidth.toPx())
    )

    drawCircle(
        color = Color.White, radius = 15f, center = Offset(
            x = size.width / 2, y = size.height - (((2 * 250f) / 3) + PitchVerticalPadding.toPx())
        )
    )

    drawArc(
        color = Color.White,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        style = Stroke(StrokeWidth.toPx()),
        topLeft = Offset(
            x = (size.width / 2) - 100f, size.height - 250f - 100f - PitchVerticalPadding.toPx()
        ),
        size = Size(200f, 200f)
    )

    drawRect(
        color = Color.White,
        size = Size(500f, 250f),
        topLeft = Offset(size.width / 2 - 250f, PitchVerticalPadding.toPx()),
        style = Stroke(StrokeWidth.toPx())
    )


    drawArc(
        color = Color.White,
        startAngle = -180f,
        sweepAngle = -180f,
        useCenter = false,
        style = Stroke(StrokeWidth.toPx()),
        topLeft = Offset(x = (size.width / 2) - 100f, 150f + PitchVerticalPadding.toPx()),
        size = Size(200f, 200f)
    )

    drawCircle(
        color = Color.White, radius = 15f, center = Offset(
            x = size.width / 2, y = (((2 * 250f) / 3) + PitchVerticalPadding.toPx())
        )
    )

}

fun DrawScope.halfwayArea() {
    drawCircle(
        color = Color.White,
        style = Stroke(StrokeWidth.toPx()),
        radius = size.width / 5,
        center = Offset(size.width / 2, size.height / 2)

    )
    drawCircle(
        color = Color.White, center = Offset(size.width / 2, size.height / 2), radius = 15f
    )
    drawLine(
        color = Color.White,
        start = Offset(x = PitchHorizontalPadding.toPx(), size.height / 2),
        end = Offset(x = size.width - (PitchHorizontalPadding.toPx()), size.height / 2),
        strokeWidth = StrokeWidth.toPx()
    )
}

fun DrawScope.drawBall(ballOffset: Offset, image: ImageBitmap) {
    drawImage(
        image = image,
        topLeft = Offset(ballOffset.x - 50, ballOffset.y - 50)
    )
}

fun DrawScope.drawPlayerOne(center: Offset, image: ImageBitmap) {
    drawImage(
        image = image,
        topLeft = Offset(center.x - 100, center.y - 100),
    )
}

fun DrawScope.drawPlayerTwo(center: Offset, image: ImageBitmap) {
    drawImage(
        image = image,
        topLeft = Offset(center.x - 100, center.y - 100),
    )
}

fun Offset.normalize(): Offset {
    val length = getDistance()
    return if (length != 0f) Offset(x / length, y / length) else Offset.Zero
}

private fun getBitmapFromImage(
    context: Context, drawable: Int,
    width: Int, height: Int
): Bitmap {

    // on below line we are getting drawable
    val db = ContextCompat.getDrawable(context, drawable)

    // in below line we are creating our bitmap and initializing it.
    val bit = Bitmap.createBitmap(
        width, height, Bitmap.Config.ARGB_8888
    )

    // on below line we are
    // creating a variable for canvas.
    val canvas = android.graphics.Canvas(bit)

    // on below line we are setting bounds for our bitmap.
    db!!.setBounds(0, 0, canvas.width, canvas.height)

    // on below line we are simply
    // calling draw to draw our canvas.
    db.draw(canvas)

    // on below line we are
    // returning our bitmap.
    return bit
}