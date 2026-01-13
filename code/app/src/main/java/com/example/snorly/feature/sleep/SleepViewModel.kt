package com.example.snorly.feature.sleep

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snorly.core.health.HealthConnectManager
import kotlinx.coroutines.launch
import java.security.AccessController.checkPermission

class SleepViewModel(
    private val healthConnectManager: HealthConnectManager
): ViewModel() {


    val requiredPermissions = healthConnectManager.permissions

    // state for ui to know what to show
    var hasPermission by mutableStateOf(false)
    private set

    //Check permission immediately when VM starts
    init {
        checkPermissions()
    }

    // We launch a coroutine to check permissions involves disk I/O
    fun checkPermissions() {
        viewModelScope.launch {
            hasPermission = healthConnectManager.hasAllPermissions()
        }
    }

    // Because our ViewModel needs an argument (Manager), we need a custom Factory.
    // This is "boilerplate" you will see often in Android without Hilt.
    class Factory(private val manager: HealthConnectManager) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SleepViewModel(manager) as T
        }
    }
}