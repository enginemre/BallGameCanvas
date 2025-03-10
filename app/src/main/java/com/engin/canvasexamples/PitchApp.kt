package com.engin.canvasexamples

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.engin.canvasexamples.ui.theme.CanvasexamplesTheme
import kotlinx.coroutines.delay

@Composable
fun PitchApp(viewModel: PitchViewModel) {
    val state by viewModel.state.collectAsState()
    if (state.showGoalDialog) {
        GoalDialog(
            onDismissRequest = viewModel::onDismissRequest
        )
    }
    PitchScreen(state = state, onPointerChange = viewModel::onPointerEvent)
}

@Composable
fun GoalDialog(modifier: Modifier = Modifier, onDismissRequest: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(3000)
        onDismissRequest()
    }
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.goall))
            val progress by animateLottieCompositionAsState(composition, restartOnPlay = false)
            LottieAnimation(
                composition = composition,
                progress = { progress },
            )
            Text(
                "GOAL!!",
                modifier = Modifier.align(Alignment.Center).padding(top = 150.dp),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

        }

    }
}

@Preview
@Composable
private fun PitchAppPreview() {
    CanvasexamplesTheme {
        val verticalPx = with(LocalDensity.current) { PitchViewModel.PitchVerticalPadding.toPx() }
        val horizontalPx =
            with(LocalDensity.current) { PitchViewModel.PitchHorizontalPadding.toPx() }

        PitchScreen(
            state = PitchState(1080f, 1920f, verticalPx, horizontalPx),
            onPointerChange = {}
        )
    }
}