package com.example.snorly.feature.challenges.steps

import androidx.compose.runtime.Immutable

@Immutable
data class StepChallengeUiState(
    val requiredSteps: Int = 0,
    val done: Int = 0,
    val remaining: Int = 0,
    val progress: Float = 0f,
    val sensorMissing: Boolean = false
)
