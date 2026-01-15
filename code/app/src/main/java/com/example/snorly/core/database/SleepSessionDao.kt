package com.example.snorly.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.snorly.core.database.entities.SleepSessionEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface SleepSessionDao {

    // --- Basic CRUD ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepSession(session: SleepSessionEntity): Long

    @Update
    suspend fun updateSleepSession(session: SleepSessionEntity)

    @Delete
    suspend fun deleteSleepSession(session: SleepSessionEntity)

    @Query("DELETE FROM sleep_sessions WHERE id = :id")
    suspend fun deleteSleepSessionById(id: Long)

    @Query("SELECT * FROM sleep_sessions WHERE id = :id")
    suspend fun getSleepSessionById(id: Long): SleepSessionEntity?

    @Query("SELECT * FROM sleep_sessions ORDER BY startTime DESC")
    fun getAllSleepSessions(): Flow<List<SleepSessionEntity>>

    // --- Sync & Deduping Queries ---

    @Query("SELECT * FROM sleep_sessions WHERE healthConnectId = :hcId LIMIT 1")
    suspend fun getByHealthConnectId(hcId: String): SleepSessionEntity?

    // --- OVERLAP CHECKS ---

    // 1. FOR MANUAL ENTRY:
    // Finds if any session overlaps the requested time.
    // "id != :excludeId" is critical: allows us to edit a session without it blocking itself.
    // Logic: (StartA < EndB) AND (EndA > StartB)
    @Query("""
        SELECT * FROM sleep_sessions 
        WHERE id != :excludeId 
        AND startTime < :end AND endTime > :start
        LIMIT 1
    """)
    suspend fun findOverlap(start: Instant, end: Instant, excludeId: Long): SleepSessionEntity?

    // 2. FOR SYNC:
    // Finds ALL overlapping sessions to resolve conflicts
    @Query("""
        SELECT * FROM sleep_sessions 
        WHERE startTime < :end AND endTime > :start
    """)
    suspend fun getSessionsOverlapping(start: Instant, end: Instant): List<SleepSessionEntity>
}