package com.engin.canvasexamples

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.engin.canvasexamples.ui.theme.CanvasexamplesTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                scrim = Color.WHITE
            ),
        )
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)

        setContent {
            val screenWidth = LocalConfiguration.current.screenWidthDp.dp
            val screenHeight = LocalConfiguration.current.screenHeightDp.dp
            val density = LocalDensity.current
            val screenWidthPx = with(density) { screenWidth.toPx() }
            val screenHeightPx = with(density) { screenHeight.toPx() }
            val pitchVerticalPaddingPx = with(density) { PitchViewModel.PitchVerticalPadding.toPx() }
            val pitchHorizontalPaddingPx = with(density) { PitchViewModel.PitchHorizontalPadding.toPx() }
            PitchApp(
                viewModel = viewModel<PitchViewModel>(
                    factory = PitchViewModel.provideFactory(
                        screenWidthPx,
                        screenHeightPx,
                        pitchPaddingVerticalPx = pitchVerticalPaddingPx,
                        pitchPaddingHorizontalPx = pitchHorizontalPaddingPx
                    )
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LineChartPreview() {
    CanvasexamplesTheme {
        LineChartApp(initialAnimationValue = 1f)
    }
}