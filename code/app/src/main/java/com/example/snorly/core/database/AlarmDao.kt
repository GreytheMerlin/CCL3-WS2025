package com.example.snorly.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.snorly.core.database.entities.AlarmEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface AlarmDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addAlarm(e: AlarmEntity)

    @Update
    suspend fun updateAlarm(alarmEntity: AlarmEntity)

    @Query("UPDATE alarms SET isActive = :isActive WHERE id = :id")
    suspend fun updateActive(id: Long, isActive: Boolean)

    @Query("SELECT * FROM alarms")
    fun getAll(): Flow<List<AlarmEntity>>


    @Query("DELETE FROM alarms WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM alarms WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): AlarmEntity?

    companion object
}