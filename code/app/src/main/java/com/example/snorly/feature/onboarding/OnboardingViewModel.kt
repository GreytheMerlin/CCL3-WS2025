package com.example.snorly.feature.onboarding

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import android.os.PowerManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snorly.core.health.HealthConnectManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.activity.compose.ManagedActivityResultLauncher
import com.example.snorly.core.database.PreferenceManager

class OnboardingViewModel(
    private val context: Context,
    val healthConnectManager: HealthConnectManager,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun updatePermissionStatus() {
        viewModelScope.launch {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

            val isExactAlarmGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else true

            val isBatteryIgnoringOptimizations = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                powerManager.isIgnoringBatteryOptimizations(context.packageName)
            } else true

            val isHealthGranted = healthConnectManager.hasAllPermissions()

            _uiState.value = _uiState.value.copy(
                isExactAlarmGranted = isExactAlarmGranted,
                isBatteryOptimized = !isBatteryIgnoringOptimizations,
                isHealthConnectGranted = isHealthGranted
            )
        }
    }

    fun requestHealthPermissions(launcher: ManagedActivityResultLauncher<Set<String>, Set<String>>) {
        viewModelScope.launch {
            launcher.launch(healthConnectManager.permissions)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            // Persist to disk
            preferenceManager.setOnboardingCompleted(true)
            _uiState.value = _uiState.value.copy(onboardingCompleted = true)
        }
    }
}

data class OnboardingUiState(
    val isExactAlarmGranted: Boolean = false,
    val isBatteryOptimized: Boolean = true,
    val isHealthConnectGranted: Boolean = false,
    val onboardingCompleted: Boolean = false
)