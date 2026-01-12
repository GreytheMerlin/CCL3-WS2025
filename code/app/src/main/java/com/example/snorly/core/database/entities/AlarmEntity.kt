package com.example.snorly.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity (
    @PrimaryKey(autoGenerate = true) val id: Int =0,
    val time:String? = null,
    val challenge: String? = null,
    val ringtone: String? = null,
    val vibration: String? = null,
    val days: List<Int> = emptyList()

)