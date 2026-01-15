package com.example.snorly.feature.challenges.memory

import androidx.compose.runtime.Immutable

@Immutable
data class MemoryMatchUiState(
    val cards: List<MemoryCard> = emptyList(),
    val pairsFound: Int = 0,
    val totalPairs: Int = 6,
    val moves: Int = 0,
    val isMemorizePhase: Boolean = true,
    val message: String = "Memorize the cards... Game starts soon!",
    val inputLocked: Boolean = false,
    val isCompleted: Boolean = false
)