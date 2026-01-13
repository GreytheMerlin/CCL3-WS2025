package com.example.snorly.feature.sleep

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.health.connect.client.PermissionController
import com.example.snorly.core.health.HealthConnectManager

@Composable
fun SleepScreen(
    viewModel: SleepViewModel
    // In a real app, you wouldn't pass the manager here,
    // but we need access to the 'permissions' set for the launcher contract.
    // Ideally, expose 'permissions' via the ViewModel.
) {
    // 1. The Launcher
    // This "registers" our intent to ask for permissions.
    // It doesn't fire yet. It just waits for the signal.
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        // This block runs when the user closes the system dialog.
        // We tell the ViewModel to check again.
        viewModel.checkPermissions()
    }

    // 2. UI Layout
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

        if (viewModel.hasPermission) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

                // === SHOW DATA ===
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Sleep (Last 24h)",
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                    )
                    Text(
                        // READ THE VALUE FROM VIEWMODEL
                        text = viewModel.totalSleepDuration,
                        style = androidx.compose.material3.MaterialTheme.typography.displayLarge
                    )
                }
            }

        } else {
            // B. Permission Missing
            Button(onClick = {


                permissionsLauncher.launch(viewModel.requiredPermissions)
            }) {
                Text("Connect Health Data")
            }
        }
    }
}
