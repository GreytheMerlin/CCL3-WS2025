package com.example.snorly.feature.alarm.ToneGenerator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snorly.core.database.entities.ComposedRingtoneEntity
import com.example.snorly.core.database.repository.RingtoneRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine // Correct Import
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Wrapper to track UI state for each item
data class ComposedRingtoneUi(
    val data: ComposedRingtoneEntity,
    val isPlaying: Boolean = false
)

class ComposerListViewModel(
    private val repository: RingtoneRepository
) : ViewModel() {

    // Helper to track which ID is currently playing
    private val _playingId = MutableStateFlow<Long?>(null)

    // Merge Database Data + Playing State
    val uiState: StateFlow<List<ComposedRingtoneUi>> =
        combine(
            repository.allComposedRingtones,
            _playingId
        ) { list: List<ComposedRingtoneEntity>, playingId: Long? -> // FIX: Explicit types
            list.map { ringtone ->
                ComposedRingtoneUi(
                    data = ringtone,
                    isPlaying = ringtone.id == playingId
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var playbackJob: Job? = null

    fun togglePlay(ringtone: ComposedRingtoneEntity) {
        if (_playingId.value == ringtone.id) {
            stopPlayback()
        } else {
            stopPlayback() // Stop current before starting new
            startPlayback(ringtone)
        }
    }

    private fun startPlayback(ringtone: ComposedRingtoneEntity) {
        playbackJob = viewModelScope.launch {
            _playingId.value = ringtone.id

            // 1. PARSE THE SONG STRING ("SINE|261.6|0;...")
            val notes = try {
                // FIX: Used mapNotNull to simplify chain
                ringtone.noteSequence.split(";").mapNotNull { part ->
                    val segments = part.split("|")
                    // Safety check for malformed data
                    if (segments.size == 3) {
                        Triple(
                            Instrument.valueOf(segments[0]), // Instrument
                            segments[1].toDouble(),          // Frequency
                            segments[2].toLong()             // Time Offset
                        )
                    } else null
                }.sortedBy { it.third } // Sort by time
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }

            if (notes.isEmpty()) {
                stopPlayback()
                return@launch
            }

            // 2. PLAY SEQUENCE
            val startTime = System.currentTimeMillis()
            val maxDuration = 10000L // 10s cap

            for (note in notes) {
                val (instrument, freq, offset) = note

                // Calculate delay needed
                val targetTime = startTime + offset
                val waitTime = targetTime - System.currentTimeMillis()

                if (waitTime > 0) delay(waitTime)

                // Play Note
                launch { ToneGenerator.playNote(freq, instrument) }
            }

            // Wait for end of song (approx) before resetting UI
            val remaining = (startTime + maxDuration) - System.currentTimeMillis()
            if (remaining > 0) delay(remaining)

            stopPlayback()
        }
    }

    private fun stopPlayback() {
        playbackJob?.cancel()
        playbackJob = null
        _playingId.value = null
    }

    fun deleteRingtone(ringtone: ComposedRingtoneEntity) {
        if (_playingId.value == ringtone.id) stopPlayback()
        viewModelScope.launch {
            repository.deleteRingtone(ringtone)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPlayback()
    }

    // Factory
    class Factory(private val repository: RingtoneRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ComposerListViewModel(repository) as T
        }
    }
}