package com.example.snorly.feature.alarm.ToneGenerator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snorly.core.database.repository.RingtoneRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Data model for a single recorded event
data class RecordedNote(
    val note: String,
    val frequency: Double,
    val timeOffset: Long, // Milliseconds from start (0-10000)
    val instrument: Instrument
)

data class ComposerUiState(
    val recordingState: RecordingState = RecordingState.IDLE,
    val progress: Float = 0f, // 0.0 to 1.0 (for progress bar)
    val selectedInstrument: Instrument = Instrument.SINE,
    val recordedNotes: List<RecordedNote> = emptyList(), // The song
    val recordingStartTime: Long = 0L
)

enum class RecordingState { IDLE, RECORDING, PLAYING }

class ComposerViewModel(private val repository: RingtoneRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ComposerUiState())
    val uiState = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private val MAX_DURATION = 10000L // 10 Seconds

    // --- INSTRUMENT CONTROLS ---
    fun selectInstrument(instrument: Instrument) {
        _uiState.update { it.copy(selectedInstrument = instrument) }
    }

    // --- RECORDING CONTROLS ---
    fun startRecording() {
        _uiState.update {
            it.copy(
                recordingState = RecordingState.RECORDING,
                recordedNotes = emptyList(), // Clear old song
                recordingStartTime = System.currentTimeMillis(),
                progress = 0f
            )
        }
        startTimer()
    }

    fun stopRecording() {
        timerJob?.cancel()
        _uiState.update { it.copy(recordingState = RecordingState.IDLE, progress = 1f) }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val start = System.currentTimeMillis()
            while (true) {
                val elapsed = System.currentTimeMillis() - start
                if (elapsed >= MAX_DURATION) {
                    stopRecording()
                    break
                }
                _uiState.update { it.copy(progress = elapsed / MAX_DURATION.toFloat()) }
                delay(50) // Update UI every 50ms
            }
        }
    }

    // --- PLAYING NOTES ---
    fun onNoteClick(noteName: String) {
        val freq = ToneGenerator.NOTES[noteName] ?: return
        val currentInstrument = _uiState.value.selectedInstrument

        // 1. Play Sound
        viewModelScope.launch {
            ToneGenerator.playNote(freq, currentInstrument)
        }

        // 2. Record it (IF recording)
        if (_uiState.value.recordingState == RecordingState.RECORDING) {
            val offset = System.currentTimeMillis() - _uiState.value.recordingStartTime
            if (offset in 0..MAX_DURATION) {
                val newNote = RecordedNote(noteName, freq, offset, currentInstrument)
                _uiState.update { it.copy(recordedNotes = it.recordedNotes + newNote) }
            }
        }
    }

    // --- PREVIEW SONG ---
    fun playFullSequence() {
        val notes = _uiState.value.recordedNotes.sortedBy { it.timeOffset }
        if (notes.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(recordingState = RecordingState.PLAYING, progress = 0f) }

            val playbackStart = System.currentTimeMillis()

            // Launch a visual timer for the playback bar
            val visualJob = launch {
                while(true) {
                    val elapsed = System.currentTimeMillis() - playbackStart
                    if (elapsed > MAX_DURATION) break
                    _uiState.update { it.copy(progress = elapsed / MAX_DURATION.toFloat()) }
                    delay(50)
                }
            }

            // Play notes at correct times
            for (note in notes) {
                val targetTime = playbackStart + note.timeOffset
                val delayTime = targetTime - System.currentTimeMillis()
                if (delayTime > 0) delay(delayTime)

                // Fire and forget play so chords work (non-blocking)
                launch { ToneGenerator.playNote(note.frequency, note.instrument) }
            }

            // Wait for end of track
            val remaining = (playbackStart + MAX_DURATION) - System.currentTimeMillis()
            if (remaining > 0) delay(remaining)

            visualJob.cancel()
            _uiState.update { it.copy(recordingState = RecordingState.IDLE, progress = 1f) }
        }
    }

    // --- SAVE TO DB ---
    fun saveRingtone(name: String) {
        val notes = _uiState.value.recordedNotes
        if (notes.isEmpty()) return

        // Serialization: "SINE|C4|0;SQUARE|E4|500"
        val sequenceString = notes.joinToString(";") {
            "${it.instrument.name}|${it.frequency}|${it.timeOffset}"
        }

        viewModelScope.launch {
            repository.saveRingtone(name, notes.map { it.frequency }) // Legacy param, ignored now?
            // WAIT! We need to update the Repository to save THIS string format.
            // For now, let's piggyback on the existing function but pass our formatted string
            // inside the repository directly.

            repository.saveComplexRingtone(name, sequenceString) // **NEW METHOD NEEDED**
        }
    }

    // Factory boilerplate...
    class Factory(private val repository: RingtoneRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ComposerViewModel(repository) as T
        }
    }
}