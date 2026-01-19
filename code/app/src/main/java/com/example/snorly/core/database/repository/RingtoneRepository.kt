package com.example.snorly.core.database.repository

import com.example.snorly.core.database.ComposedRingtoneDao
import com.example.snorly.core.database.entities.ComposedRingtoneEntity
import kotlinx.coroutines.flow.Flow

class RingtoneRepository(private val dao: ComposedRingtoneDao) {

    val allComposedRingtones: Flow<List<ComposedRingtoneEntity>> = dao.getAll()

    suspend fun saveRingtone(name: String, notes: List<Double>) {
        val sequenceString = notes.joinToString(",")
        val ringtone = ComposedRingtoneEntity(
            name = name,
            noteSequence = sequenceString
        )
        dao.insert(ringtone)
    }

    // In RingtoneRepository.kt
    suspend fun saveComplexRingtone(name: String, sequence: String) {
        val ringtone = ComposedRingtoneEntity(name = name, noteSequence = sequence)
        dao.insert(ringtone)
    }

    suspend fun deleteRingtone(ringtone: ComposedRingtoneEntity) {
        dao.delete(ringtone)
    }
}