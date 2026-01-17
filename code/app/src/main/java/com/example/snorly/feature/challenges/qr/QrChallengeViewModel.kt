package com.example.snorly.feature.challenges.qr

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class QrChallengeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(QrChallengeUiState())
    val uiState: StateFlow<QrChallengeUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<Effect>(extraBufferCapacity = 1)
    val effects: SharedFlow<Effect> = _effects.asSharedFlow()

    sealed interface Effect {
        data object Solved : Effect
    }

    fun setExpectedValue(expected: String?) {
        _uiState.update { it.copy(expectedValue = expected) }
    }

    fun onPermissionResult(granted: Boolean) {
        _uiState.update {
            it.copy(
                hasPermission = granted,
                denied = !granted
            )
        }
    }

    fun requestPermissionAgain() {
        // purely UI-triggered (launcher lives in UI), but we clear denied flag
        _uiState.update { it.copy(denied = false) }
    }

    fun onQrScanned(value: String) {
        val s = _uiState.value
        val expected = s.expectedValue

        val ok = (expected == null || value == expected)
        _uiState.update {
            it.copy(
                scannedValue = value,
                success = ok,
                showWrongQr = !ok && expected != null
            )
        }

        if (ok) _effects.tryEmit(Effect.Solved)
    }

    fun simulateSuccessScan() {
        // For dev/testing button
        val expected = _uiState.value.expectedValue
        val value = expected ?: "TEST_OK"
        onQrScanned(value)
    }
}