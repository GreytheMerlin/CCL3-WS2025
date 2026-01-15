package com.example.snorly.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snorly.core.database.UserProfileDao
import com.example.snorly.core.database.entities.UserProfileEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(private val dao: UserProfileDao) : ViewModel() {

    private val _userProfile = MutableStateFlow(UserProfileEntity())
    val userProfile = _userProfile.asStateFlow()

    init {
        viewModelScope.launch {
            dao.getUserProfile().collect { profile ->
                if (profile != null) {
                    _userProfile.value = profile
                } else {
                    // Create initial empty profile if none exists
                    val default = UserProfileEntity()
                    dao.insertOrUpdate(default)
                    _userProfile.value = default
                }
            }
        }
    }

    // Factory
    class Factory(private val dao: UserProfileDao) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(dao) as T
        }
    }
}