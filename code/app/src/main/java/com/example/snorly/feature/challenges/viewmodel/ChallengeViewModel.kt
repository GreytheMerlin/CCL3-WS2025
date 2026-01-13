package com.example.snorly.feature.challenges.viewmodel

import androidx.lifecycle.ViewModel
import com.example.snorly.feature.challenges.model.Challenge
import com.example.snorly.feature.challenges.model.ChallengeDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Collections
import kotlin.collections.toMutableList

data class ChallengeUiState(
    val isEnabled: Boolean = false,
    val activeChallenges: List<Challenge> = emptyList(),
    // Computed property: All challenges minus the active ones
    val availableChallenges: List<Challenge> = emptyList()
)

class ChallengeViewModel : ViewModel() {



    private val _uiState = MutableStateFlow(ChallengeUiState())
    val uiState: StateFlow<ChallengeUiState> = _uiState.asStateFlow()

    init {
        // Init with some default data
        val defaults = ChallengeDataSource.allChallenges.take(2) // First 2 active by default
        refreshLists(defaults)
    }

    // Toggle the main switch
    fun toggleFeature(enabled: Boolean) {
        _uiState.update { it.copy(isEnabled = enabled) }
    }

    // Add a challenge from "Available" to "Active"
    fun addChallenge(challenge: Challenge) {
        val currentActive = _uiState.value.activeChallenges.toMutableList()
        if (!currentActive.contains(challenge)) {
            currentActive.add(challenge)
            refreshLists(currentActive)
        }
    }

    // Remove from "Active"
    fun removeChallenge(challenge: Challenge) {
        val currentActive = _uiState.value.activeChallenges.toMutableList()
        currentActive.remove(challenge)
        refreshLists(currentActive)
    }

    // Handle Drag & Drop Reordering
    fun moveChallenge(fromIndex: Int, toIndex: Int) {
        val currentList = _uiState.value.activeChallenges.toMutableList()
        if (fromIndex in currentList.indices && toIndex in currentList.indices) {
            Collections.swap(currentList, fromIndex, toIndex)
            // We just update the list order, no need to recalculate available
            _uiState.update { it.copy(activeChallenges = currentList) }
        }
    }

    // Helper to keep the "Available" list in sync
    private fun refreshLists(active: List<Challenge>) {
        val all = ChallengeDataSource.allChallenges
        val available = all.filter { !active.contains(it) }

        _uiState.update {
            it.copy(
                activeChallenges = active,
                availableChallenges = available
            )
        }
    }

    fun getChallengeById(id: String): Challenge? {
        return ChallengeDataSource.allChallenges.find { it.id == id }
    }
}