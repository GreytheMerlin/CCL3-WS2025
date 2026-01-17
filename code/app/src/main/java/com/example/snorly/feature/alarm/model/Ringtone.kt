package com.example.snorly.feature.alarm.model

import android.net.Uri

data class Ringtone(
    val id: String,
    val title: String,
    val uri: String, // For device ringtones or android.resource://... for internal
    val isPlaying: Boolean = false,
    val isSelected: Boolean = false
)