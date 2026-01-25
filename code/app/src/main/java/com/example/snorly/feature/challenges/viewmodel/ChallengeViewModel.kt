package com.example.snorly.feature.challenges.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snorly.core.database.AppDatabase
import com.example.snorly.feature.challenges.model.Challenge
import com.example.snorly.feature.challenges.model.ChallengeDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Collections
import kotlin.collections.toMutableList



data class ChallengeUiState(
    val isEnabled: Boolean = false,
    val activeChallenges: List<Challenge> = emptyList(),
    // Computed property: All challenges minus the active ones
    val availableChallenges: List<Challenge> = emptyList(),
    val initialized: Boolean = false
)

class ChallengeViewModel(application: Application) : AndroidViewModel(application) {

    private val alarmDao = AppDatabase.getDatabase(application).alarmDao()

    private val _uiState = MutableStateFlow(ChallengeUiState())
    val uiState: StateFlow<ChallengeUiState> = _uiState.asStateFlow()

    /** Call this ONCE when opening the screen */
    fun initFromSelection(enabled: Boolean, activeIds: List<String>) {
        if (_uiState.value.initialized) return

        val active = activeIds
            .mapNotNull { id -> ChallengeDataSource.allChallenges.find { it.id == id } }

        _uiState.update {
            it.copy(
                isEnabled = enabled,
                initialized = true
            )
        }

        refreshLists(active)
    }

    fun toggleFeature(enabled: Boolean) {
        _uiState.update { it.copy(isEnabled = enabled) }

        if (!enabled) {
            // OFF -> clear active list
            refreshLists(emptyList())
            return
        }

        // ON -> if empty, auto add Math
        if (_uiState.value.activeChallenges.isEmpty()) {
            getDefaultMathChallenge()?.let { math ->
                addChallenge(math)
            }
        }
    }

    private fun getDefaultMathChallenge(): Challenge? {


        var x = ChallengeDataSource.allChallenges.find { it.title.equals("Math Problem", ignoreCase = true) }
        Log.d("challenge", "${x}")
        return x
    }


    fun addChallenge(challenge: Challenge) {
        val currentActive = _uiState.value.activeChallenges.toMutableList()
        if (!currentActive.contains(challenge)) {
            currentActive.add(challenge)
            refreshLists(currentActive)
        }
    }

    fun removeChallenge(challenge: Challenge) {
        val currentActive = _uiState.value.activeChallenges.toMutableList()
        currentActive.remove(challenge)
        refreshLists(currentActive)
    }

    fun moveChallenge(fromIndex: Int, toIndex: Int) {
        val currentList = _uiState.value.activeChallenges.toMutableList()
        if (fromIndex in currentList.indices && toIndex in currentList.indices) {
            Collections.swap(currentList, fromIndex, toIndex)
            _uiState.update { it.copy(activeChallenges = currentList) }
        }
    }

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