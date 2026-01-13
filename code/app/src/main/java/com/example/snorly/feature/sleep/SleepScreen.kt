package com.example.snorly.feature.sleep

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import com.example.snorly.R

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
            // B. Permission Missing - Styled "Welcome" State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                     Image(painter = painterResource(id = R.drawable.snorly_healthconnect), contentDescription = "Snorly Healthconnect")

                Spacer(modifier = Modifier.height(32.dp))

                // 2. Title
                Text(
                    text = "Track Your Sleep",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color.White // Or your theme color
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Subtitle
                Text(
                    text = "Connect Snorly with Google Health Connect to analyze your sleep quality and patterns automatically.",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                    color = androidx.compose.ui.graphics.Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 4. Connect Button
                Button(
                    onClick = {
                        permissionsLauncher.launch(viewModel.requiredPermissions)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xFF1677FF) // Your primary blue
                    )
                ) {
                    Text(
                        text = "Connect Health Data",
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    )
                }
            }
        }
        }
    }

