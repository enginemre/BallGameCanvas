package com.engin.canvasexamples

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PitchViewModel(
    screenWithPx: Float,
    screenHeightPx: Float,
    pitchPaddingVerticalPx: Float,
    pitchPaddingHorizontalPx: Float
) : ViewModel() {

    private val _state =
        MutableStateFlow(
            PitchState(
                screenHeightPx = screenHeightPx,
                screenWidthPx = screenWithPx,
                pitchVerticalPaddingPx = pitchPaddingVerticalPx,
                pitchHorizontalPaddingPx = pitchPaddingHorizontalPx
            )
        )
    val state: StateFlow<PitchState> = _state.asStateFlow()

    var gameJob: Job? = null

    init {
        initGame()
        startGameLoop()
    }

    private fun initGame(withAnimation: Boolean = false) {
        viewModelScope.launch {
            _state.update { state ->
                val playerOneTarget = Offset(state.screenWidthPx / 2, 300f)
                val playerTwoTarget = Offset(state.screenWidthPx / 2, state.screenHeightPx - 300f)
                val ballTarget = Offset(state.screenWidthPx / 2, state.screenHeightPx / 2)

                state.copy(
                    playerOneOffset = playerOneTarget,
                    playerTwoOffset = playerTwoTarget,
                    ballOffset = ballTarget,
                    ballVelocity = Offset.Zero,
                    playerOneVelocity = Offset.Zero,
                    playerTwoVelocity = Offset.Zero
                )

            }
        }
    }

    private fun startGameLoop() {
        gameJob?.cancel()
        gameJob = viewModelScope.launch {
            while (state.value.showGoalDialog.not()) {
                updateBallSpeed()
                with(state.value) {
                    edgeCheck(pitchVerticalPaddingPx, pitchHorizontalPaddingPx)
                    checkCollisions(playerOneOffset, playerOneVelocity)
                    checkCollisions(playerTwoOffset, playerTwoVelocity)
                    if (checkGoalScored()) {
                        _state.update { it.copy(showGoalDialog = true) }
                        initGame(withAnimation = true)

                    }
                }
                delay(16L)
            }
        }
    }

    private fun checkGoalScored(): Boolean {
        val state = _state.value
        val ballRadius = 20f

        val goalWidth = 370f
        val goalLeftX = state.screenWidthPx / 2 - goalWidth / 2
        val goalRightX = state.screenWidthPx / 2 + goalWidth / 2
        val goalTopY = state.pitchVerticalPaddingPx - 100f
        val goalBottomY = state.screenHeightPx - state.pitchVerticalPaddingPx + 100f

        val ballInsideGoalTop =
            state.ballOffset.x in goalLeftX..goalRightX && state.ballOffset.y - ballRadius <= goalTopY
        if (ballInsideGoalTop) {
            _state.update { it.copy(playerTwoScore = it.playerTwoScore + 1) }
        }
        val ballInsideGoalBottom =
            state.ballOffset.x in goalLeftX..goalRightX && state.ballOffset.y + ballRadius >= goalBottomY
        if (ballInsideGoalBottom) {
            _state.update { it.copy(playerOneScore = it.playerOneScore + 1) }
        }
        return ballInsideGoalTop || ballInsideGoalBottom
    }


    private fun updateBallSpeed() {
        _state.update {
            it.copy(
                ballOffset = it.ballOffset + it.ballVelocity,
                ballVelocity = it.ballVelocity * DAMPING_FACTOR
            )
        }
    }

    private fun edgeCheck(pitchPaddingVertical: Float, pitchPaddingHorizontalPx: Float) {
        _state.update {
            val ballRadius = 20f

            var ballVelocity = it.ballVelocity
            var ballOffset = it.ballOffset

            // **Kale Sınırları**
            val goalWidth = 370f  // Kalelerin toplam genişliği
            val goalLeftX = it.screenWidthPx / 2 - (goalWidth / 2)
            val goalRightX = it.screenWidthPx / 2 + (goalWidth / 2)
            val goalTopY = pitchPaddingVertical - 100f
            val goalBottomY = it.screenHeightPx - pitchPaddingVertical + 100f

            // **X Ekseni Sınır Kontrolü** (Kaleyi hesaba kat)
            if (!((ballOffset.y in goalTopY..pitchPaddingVertical) || (ballOffset.y in it.screenHeightPx - pitchPaddingVertical..goalBottomY))) {
                if (ballOffset.x - ballRadius <= pitchPaddingHorizontalPx || ballOffset.x + ballRadius >= it.screenWidthPx - pitchPaddingHorizontalPx) {
                    ballVelocity = Offset(-ballVelocity.x, ballVelocity.y)
                    ballOffset = ballOffset.copy(
                        x = ballOffset.x.coerceIn(
                            pitchPaddingHorizontalPx + ballRadius,
                            it.screenWidthPx - pitchPaddingHorizontalPx - ballRadius
                        )
                    )
                }
            }

            // **Y Ekseni Sınır Kontrolü (Kaleyi Hesaba Kat)**
            if (ballOffset.y - ballRadius <= pitchPaddingVertical) {
                // Eğer top kalenin dışındaysa normal sekme yap
                if (!(ballOffset.x in goalLeftX..goalRightX)) {
                    ballVelocity = Offset(ballVelocity.x, -ballVelocity.y)
                    ballOffset = ballOffset.copy(
                        y = pitchPaddingVertical + ballRadius
                    )
                }
                // Eğer top kalenin içindeyse ve arka direğe çarptıysa geri sekme yap
                else if (ballOffset.y - ballRadius < goalTopY) {
                    ballVelocity = Offset(ballVelocity.x, -ballVelocity.y)
                    ballOffset = ballOffset.copy(
                        y = goalTopY + ballRadius
                    )
                }
            }

            if (ballOffset.y + ballRadius >= it.screenHeightPx - pitchPaddingVertical) {
                if (!(ballOffset.x in goalLeftX..goalRightX)) {
                    ballVelocity = Offset(ballVelocity.x, -ballVelocity.y)
                    ballOffset = ballOffset.copy(
                        y = it.screenHeightPx - pitchPaddingVertical - ballRadius
                    )
                }
                // Eğer top alt kaleye girdi ve arka direğe çarptıysa geri sekme yap
                else if (ballOffset.y + ballRadius > goalBottomY) {
                    ballVelocity = Offset(ballVelocity.x, -ballVelocity.y)
                    ballOffset = ballOffset.copy(
                        y = goalBottomY - ballRadius
                    )
                }
            }

            // **Player Sınır Kontrolü**
            val playerOneOffset = Offset(it.playerOneOffset.x, it.playerOneOffset.y)
            val playerTwoOffset = Offset(it.playerTwoOffset.x, it.playerTwoOffset.y)

            it.copy(
                ballVelocity = ballVelocity,
                ballOffset = ballOffset,
                playerOneOffset = playerOneOffset,
                playerTwoOffset = playerTwoOffset
            )
        }
    }

    private fun checkCollisions(playerOffset: Offset, playerVelocity: Offset) {
        val distance = (state.value.ballOffset - playerOffset).getDistance()

        if (distance < 85f) {
            val normal = (state.value.ballOffset - playerOffset).normalize()

            val ballSpeed = state.value.ballVelocity.getDistance()
            val playerSpeed = playerVelocity.getDistance()

            val newSpeed = ballSpeed + (playerSpeed * 0.9f) + 5f
            val newVelocity = normal * newSpeed

            _state.update { it.copy(ballVelocity = newVelocity) }
        }
    }


    fun onPointerEvent(change: PointerInputChange) {
        val position = change.position
        val pointerId = change.id

        _state.update {
            val updatedPointers = it.activePointers.toMutableMap()

            if (change.changedToDown()) {
                val playerOneDist = (it.playerOneOffset - position).getDistance()
                val playerTwoDist = (it.playerTwoOffset - position).getDistance()

                if (playerOneDist < 50f) {
                    updatedPointers[pointerId] = 1
                } else if (playerTwoDist < 50f) {
                    updatedPointers[pointerId] = 2
                }
            }

            var newPlayerOneOffset = it.playerOneOffset
            var newPlayerTwoOffset = it.playerTwoOffset
            var newPlayerOneVelocity = it.playerOneVelocity
            var newPlayerTwoVelocity = it.playerTwoVelocity

            if (change.pressed) {
                when (updatedPointers[pointerId]) {
                    1 -> {
                        newPlayerOneVelocity =
                            position - it.previousPlayerOneOffset  // Hız hesaplandı
                        newPlayerOneOffset = position
                    }

                    2 -> {
                        newPlayerTwoVelocity =
                            position - it.previousPlayerTwoOffset  // Hız hesaplandı
                        newPlayerTwoOffset = position
                    }
                }
            }

            if (change.changedToUp()) {
                updatedPointers.remove(pointerId)
            }

            it.copy(
                playerOneOffset = newPlayerOneOffset,
                playerTwoOffset = newPlayerTwoOffset,
                playerOneVelocity = newPlayerOneVelocity,
                playerTwoVelocity = newPlayerTwoVelocity,
                previousPlayerOneOffset = it.playerOneOffset, // Önceki konumu güncelle
                previousPlayerTwoOffset = it.playerTwoOffset, // Önceki konumu güncelle
                activePointers = updatedPointers
            )
        }

        change.consume()
    }

    fun onDismissRequest() {
        _state.update { it.copy(showGoalDialog = false) }
        initGame()
        startGameLoop()
    }


    companion object {
        const val DAMPING_FACTOR = 0.98F
        val StrokeWidth = 4.dp
        val PitchHorizontalPadding = 28.dp
        val PitchVerticalPadding = 50.dp
        fun provideFactory(
            screenWidthPx: Float,
            screenHeightPx: Float,
            pitchPaddingVerticalPx: Float,
            pitchPaddingHorizontalPx: Float
        ) =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(PitchViewModel::class.java)) {
                        return PitchViewModel(
                            screenWidthPx,
                            screenHeightPx,
                            pitchPaddingVerticalPx,
                            pitchPaddingHorizontalPx
                        ) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}

@Stable
data class PitchState(
    val screenWidthPx: Float,
    val screenHeightPx: Float,
    val pitchVerticalPaddingPx: Float,
    val pitchHorizontalPaddingPx: Float,
    val showGoalDialog: Boolean = false,
    val playerOneOffset: Offset = Offset.Zero,
    val playerTwoOffset: Offset = Offset.Zero,
    val playerOneScore: Int = 0,
    val playerTwoScore: Int = 0,
    val previousPlayerOneOffset: Offset = Offset.Zero,
    val previousPlayerTwoOffset: Offset = Offset.Zero,
    val playerOneVelocity: Offset = Offset.Zero,
    val playerTwoVelocity: Offset = Offset.Zero,
    val ballVelocity: Offset = Offset.Zero,
    val activePointers: MutableMap<PointerId, Int> = mutableMapOf(),
    val ballOffset: Offset = Offset.Zero

)