package com.example.snorly.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snorly.core.database.UserProfileDao
import com.example.snorly.core.database.entities.UserProfileEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val dao: UserProfileDao) : ViewModel() {

    // Now valid because all fields in Entity have default nulls
    private val _state = MutableStateFlow(UserProfileEntity())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            dao.getUserProfile().collect { profile ->
                if (profile != null) {
                    _state.value = profile
                } else {
                    // Initialize DB with an empty row if none exists
                    dao.insertOrUpdate(UserProfileEntity())
                }
            }
        }
    }

    fun saveProfile(
        bedTime: String?,
        wakeTime: String?
    ) {
        val updated = _state.value.copy(
            targetBedTime = bedTime,
            targetWakeTime = wakeTime
        )
        viewModelScope.launch {
            dao.insertOrUpdate(updated)
        }
    }

    class Factory(private val dao: UserProfileDao) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(dao) as T
        }
    }
}