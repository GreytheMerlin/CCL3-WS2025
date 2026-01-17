package com.example.snorly.feature.challenges.shake

import androidx.compose.runtime.Immutable

@Immutable
data class ShakeChallengeUiState(
    val requiredShakes: Int = 0,
    val done: Int = 0,
    val remaining: Int = 0,
    val progress: Float = 0f
)