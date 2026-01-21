package com.example.snorly.feature.onboarding

import androidx.compose.ui.graphics.vector.ImageVector

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val buttonText: String,
    val permissionType: PermissionType
)

enum class PermissionType {
    WELCOME, NOTIFICATIONS, EXACT_ALARM, BATTERY, HEALTH_CONNECT
}