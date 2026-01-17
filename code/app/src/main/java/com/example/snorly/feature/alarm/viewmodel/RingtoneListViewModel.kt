package com.example.snorly.feature.alarm.viewmodel

import android.app.Application
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.snorly.feature.alarm.model.Ringtone
import com.example.snorly.feature.alarm.model.RingtoneData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RingtoneListViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private var mediaPlayer: MediaPlayer? = null

    // 1. Get arguments
    private val categoryId: String = checkNotNull(savedStateHandle["categoryId"])

    // 2. State
    private val _uiState = MutableStateFlow<List<Ringtone>>(emptyList())
    val uiState = _uiState.asStateFlow()

    init {
        loadRingtones()
    }

    private fun loadRingtones() {
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO) {
                if (categoryId == "device") {
                    fetchDeviceRingtones()
                } else {
                    fetchCategoryRingtones(categoryId)
                }
            }
            _uiState.value = list
        }
    }

    private fun fetchDeviceRingtones(): List<Ringtone> {
        val ringtoneList = mutableListOf<Ringtone>()
        val manager = RingtoneManager(context)
        manager.setType(RingtoneManager.TYPE_ALARM or RingtoneManager.TYPE_RINGTONE)

        try {
            val cursor = manager.cursor
            while (cursor.moveToNext()) {
                val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                val uriPrefix = cursor.getString(RingtoneManager.URI_COLUMN_INDEX)
                val id = cursor.getString(RingtoneManager.ID_COLUMN_INDEX)
                val uri = "$uriPrefix/$id"

                ringtoneList.add(
                    Ringtone(
                        id = id,
                        title = title,
                        uri = uri
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ringtoneList
    }

    // helper to construct valid resource URIs
    private fun getResourceUri(resourceId: Int): String {
        return "android.resource://${getApplication<Application>().packageName}/$resourceId"
    }

    // Return different sounds based on category
    // MUST have these files in res/raw for this to work
    private fun fetchCategoryRingtones(category: String): List<Ringtone> {
        // Get the raw definitions from our Data Object
        val definitions = RingtoneData.getSoundsForCategory(category)

        // Map them to the UI Model (Ringtone) with full URIs
        return definitions.map { def ->
            Ringtone(
                id = def.id,
                title = def.title,
                uri = getResourceUri(def.resId) // Convert Int -> URI String
            )
        }
    }

    fun onRingtoneClick(selected: Ringtone) {
        // 1. Update UI Selection & Playing State
        _uiState.update { list ->
            list.map { item ->
                item.copy(
                    isSelected = item.uri == selected.uri,
                    isPlaying = item.uri == selected.uri
                )
            }
        }

        // 2. Play Sound
        if (selected.uri.isNotEmpty()) {
            playPreview(Uri.parse(selected.uri))
        }
    }

    private fun playPreview(uri: Uri) {
        stopPreview() // Stop previous
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            try {
                setDataSource(context, uri)
                prepare()
                start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stopPreview() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Reset playing icon in UI
        _uiState.update { list -> list.map { it.copy(isPlaying = false) } }
    }

    override fun onCleared() {
        super.onCleared()
        stopPreview()
    }
}