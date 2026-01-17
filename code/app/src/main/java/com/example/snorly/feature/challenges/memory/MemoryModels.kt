package com.example.snorly.feature.challenges.memory

import androidx.compose.runtime.Immutable

@Immutable
data class MemoryCard(
    val id: Int,
    val content: String,
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false
)

sealed interface FlipResult {
    data object Ignored : FlipResult
    data class Updated(
        val cards: List<MemoryCard>,
        val pairsFound: Int,
        val moves: Int,
        val message: String,
        val needsMismatchFlipBack: Boolean
    ) : FlipResult
}