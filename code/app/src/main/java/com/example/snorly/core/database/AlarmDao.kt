package com.example.snorly.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.snorly.core.database.entities.AlarmEntity


@Dao
interface AlarmDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addAlarm(e: AlarmEntity)

    @Update
    suspend fun updateAlarm(alarmEntity: AlarmEntity)

    @Query("SELECT * FROM alarms")
    fun getAll():List<AlarmEntity>


    @Query("DELETE FROM alarms WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM alarms WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): AlarmEntity?

    companion object
}