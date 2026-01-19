package com.example.snorly.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.snorly.core.database.entities.ComposedRingtoneEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ComposedRingtoneDao {
    @Query("SELECT * FROM composed_ringtones ORDER BY createdAt DESC")
    fun getAll(): Flow<List<ComposedRingtoneEntity>>

    @Query("SELECT * FROM composed_ringtones WHERE id = :id")
    suspend fun getById(id: Long): ComposedRingtoneEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ringtone: ComposedRingtoneEntity): Long

    @Delete
    suspend fun delete(ringtone: ComposedRingtoneEntity)
}