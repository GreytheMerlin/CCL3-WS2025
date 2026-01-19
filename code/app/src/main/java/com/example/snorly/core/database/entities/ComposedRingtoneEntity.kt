package com.example.snorly.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "composed_ringtones")
data class ComposedRingtoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val noteSequence: String, // Stored as "261.6,440.0,..."
    val createdAt: Long = System.currentTimeMillis()
)