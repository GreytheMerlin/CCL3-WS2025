package com.example.snorly.feature.challenges.shake

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ShakeChallengeViewModel : ViewModel() {

    // Tune difficulty here (or pass from Route if you want)
    private val shakeThresholdG: Float = 2.2f
    private val shakeCooldownMs: Long = 350L

    private var lastShakeTimeMs: Long = 0L

    private val _uiState = MutableStateFlow(ShakeChallengeUiState())
    val uiState: StateFlow<ShakeChallengeUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<Effect>(extraBufferCapacity = 1)
    val effects: SharedFlow<Effect> = _effects.asSharedFlow()

    sealed interface Effect {
        data object Solved : Effect
    }

    fun start(requiredShakes: Int) {
        _uiState.value = computeState(requiredShakes = requiredShakes, done = 0)
        lastShakeTimeMs = 0L
    }

    fun onGForce(gForce: Float) {
        val s = _uiState.value
        if (s.requiredShakes <= 0) return

        if (gForce <= shakeThresholdG) return

        val now = System.currentTimeMillis()
        if (now - lastShakeTimeMs < shakeCooldownMs) return
        lastShakeTimeMs = now

        val newDone = (s.done + 1).coerceAtMost(s.requiredShakes)
        _uiState.value = computeState(requiredShakes = s.requiredShakes, done = newDone)

        if (newDone >= s.requiredShakes) {
            _effects.tryEmit(Effect.Solved)
        }
    }

    private fun computeState(requiredShakes: Int, done: Int): ShakeChallengeUiState {
        val remaining = (requiredShakes - done).coerceAtLeast(0)
        val progress = if (requiredShakes == 0) 0f else done.toFloat() / requiredShakes.toFloat()
        return ShakeChallengeUiState(
            requiredShakes = requiredShakes,
            done = done,
            remaining = remaining,
            progress = progress.coerceIn(0f, 1f)
        )
    }
}
