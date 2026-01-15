package com.example.snorly.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 0,
    val age: Int? = null,          // Nullable, defaults to null (empty)
    val sex: String? = null,
    val chronotype: String? = null,
    val sleepNeedCategory: String? = null,
    val targetBedTime: String? = null,
    val targetWakeTime: String? = null
)