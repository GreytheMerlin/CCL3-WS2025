package com.example.snorly.feature.challenges.steps

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max

class StepChallengeViewModel : ViewModel() {

    private var baseline: Float? = null

    private val _uiState = MutableStateFlow(StepChallengeUiState())
    val uiState: StateFlow<StepChallengeUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<Effect>(extraBufferCapacity = 1)
    val effects: SharedFlow<Effect> = _effects.asSharedFlow()

    sealed interface Effect {
        data object Solved : Effect
    }

    fun start(requiredSteps: Int, sensorMissing: Boolean) {
        baseline = null
        _uiState.value = computeState(requiredSteps, done = 0, sensorMissing = sensorMissing)
    }

    /** Pass raw TYPE_STEP_COUNTER value (cumulative since boot). */
    fun onTotalStepsSinceBoot(totalSinceBoot: Float) {
        val s = _uiState.value
        if (s.sensorMissing) return
        if (s.requiredSteps <= 0) return

        val base = baseline
        if (base == null) {
            baseline = totalSinceBoot
            return
        }

        val delta = (totalSinceBoot - base).toInt()
        val done = max(0, delta).coerceAtMost(s.requiredSteps)

        _uiState.value = computeState(s.requiredSteps, done, s.sensorMissing)

        if (done >= s.requiredSteps) {
            _effects.tryEmit(Effect.Solved)
        }
    }

    private fun computeState(requiredSteps: Int, done: Int, sensorMissing: Boolean): StepChallengeUiState {
        val safeRequired = requiredSteps.coerceAtLeast(1)
        val safeDone = done.coerceIn(0, safeRequired)
        val remaining = (requiredSteps - safeDone).coerceAtLeast(0)
        val progress = if (requiredSteps <= 0) 0f else safeDone.toFloat() / requiredSteps.toFloat()

        return StepChallengeUiState(
            requiredSteps = requiredSteps,
            done = safeDone,
            remaining = remaining,
            progress = progress.coerceIn(0f, 1f),
            sensorMissing = sensorMissing
        )
    }
}
