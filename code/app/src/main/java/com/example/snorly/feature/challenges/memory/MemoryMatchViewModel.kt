package com.example.snorly.feature.challenges.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MemoryMatchViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MemoryMatchUiState())
    val uiState: StateFlow<MemoryMatchUiState> = _uiState

    private var mismatchJob: Job? = null
    private var memorizeJob: Job? = null

    fun startGame(
        pairCount: Int = 6,
        memorizeMillis: Long = 1800L
    ) {
        mismatchJob?.cancel()
        memorizeJob?.cancel()

        val newCards = createNewCards(pairCount = pairCount)

        _uiState.value = MemoryMatchUiState(
            cards = revealAll(newCards),
            totalPairs = pairCount,
            isMemorizePhase = true,
            message = "Memorize the cards... Game starts soon!"
        )

        memorizeJob = viewModelScope.launch {
            delay(memorizeMillis)
            _uiState.update {
                it.copy(
                    cards = hideAllUnmatched(it.cards),
                    isMemorizePhase = false,
                    message = "Go!"
                )
            }
        }
    }

    fun onCardTap(cardId: Int) {
        val s = _uiState.value
        val res = onCardTapped(
            cards = s.cards,
            totalPairs = s.totalPairs,
            pairsFound = s.pairsFound,
            moves = s.moves,
            isMemorizePhase = s.isMemorizePhase,
            inputLocked = s.inputLocked,
            cardId = cardId
        )

        if (res is FlipResult.Updated) {
            _uiState.update {
                val completed = (res.pairsFound == it.totalPairs)
                it.copy(
                    cards = res.cards,
                    pairsFound = res.pairsFound,
                    moves = res.moves,
                    message = res.message,
                    inputLocked = res.needsMismatchFlipBack,
                    isCompleted = completed
                )
            }

            if (res.needsMismatchFlipBack) {
                mismatchJob?.cancel()
                mismatchJob = viewModelScope.launch {
                    delay(650)
                    _uiState.update { cur ->
                        cur.copy(
                            cards = flipBackMismatched(cur.cards),
                            inputLocked = false
                        )
                    }
                }
            }
        }
    }

    fun reset(pairCount: Int = _uiState.value.totalPairs) {
        startGame(pairCount = pairCount)
    }
}