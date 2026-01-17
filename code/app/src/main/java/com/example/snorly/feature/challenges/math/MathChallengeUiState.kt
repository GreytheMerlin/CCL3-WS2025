package com.example.snorly.feature.challenges.math

import androidx.compose.runtime.Immutable

@Immutable
data class MathChallengeUiState(
    val a: Int = 0,
    val b: Int = 0,
    val input: String = "",
    val error: Boolean = false
)
