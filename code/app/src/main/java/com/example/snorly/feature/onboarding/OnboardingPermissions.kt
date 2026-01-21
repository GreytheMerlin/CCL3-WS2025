package com.example.snorly.feature.onboarding

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher

fun handlePermissionAction(
    type: PermissionType,
    context: Context,
    viewModel: OnboardingViewModel,
    healthLauncher: ManagedActivityResultLauncher<Set<String>, Set<String>>,
    isSkipAction: Boolean = false,
    onNext: () -> Unit
) {

    if (isSkipAction) {
        onNext()
        return
    }

    when (type) {
        PermissionType.WELCOME -> onNext()

        PermissionType.EXACT_ALARM -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
                onNext()
        }

        PermissionType.BATTERY -> {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            context.startActivity(intent)
            onNext() // Move forward
        }

        PermissionType.HEALTH_CONNECT -> {
            // Only launch if supported; otherwise, the UI should have mapped this to WELCOME
            viewModel.requestHealthPermissions(healthLauncher)
            onNext() // Optional: Move to final page after launching request
        }
        else -> onNext()
    }
}