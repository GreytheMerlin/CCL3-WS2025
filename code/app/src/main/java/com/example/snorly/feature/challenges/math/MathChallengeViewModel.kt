package com.example.snorly.feature.challenges.math

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

class MathChallengeViewModel : ViewModel() {

    private val a = Random.nextInt(10, 60)
    private val b = Random.nextInt(10, 60)
    private val answer = a + b

    private val _uiState = MutableStateFlow(
        MathChallengeUiState(a = a, b = b)
    )
    val uiState: StateFlow<MathChallengeUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<Effect>(extraBufferCapacity = 1)
    val effects: SharedFlow<Effect> = _effects.asSharedFlow()

    sealed interface Effect {
        data object Solved : Effect
    }

    fun onDigit(d: String) {
        _uiState.update {
            if (it.input.length >= 6) it
            else it.copy(input = it.input + d, error = false)
        }
    }

    fun onBackspace() {
        _uiState.update {
            if (it.input.isEmpty()) it.copy(error = false)
            else it.copy(input = it.input.dropLast(1), error = false)
        }
    }

    fun onConfirm() {
        val input = _uiState.value.input
        val v = input.toIntOrNull()
        if (v == answer) {
            _effects.tryEmit(Effect.Solved)
        } else {
            _uiState.update { it.copy(error = true) }
        }
    }
}
