package com.example.snorly.feature.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.snorly.core.database.PreferenceManager
import com.example.snorly.core.health.HealthConnectManager

class OnboardingViewModelFactory(
    private val context: Context,
    private val healthConnectManager: HealthConnectManager,
    private val preferenceManager: PreferenceManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OnboardingViewModel::class.java)) {
            return OnboardingViewModel(context, healthConnectManager, preferenceManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
