package com.example.snorly.feature.sleep

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import com.example.snorly.R
import com.example.snorly.feature.sleep.components.SleepHistoryItem
import com.example.snorly.feature.sleep.components.SleepStatCard
import com.example.snorly.feature.sleep.components.SleepTrackingCard

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
        val bg = Color(0xFF000000)
        val cardBg = Color(0xFF1C1C1E)
        if (viewModel.hasPermission) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bg)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Header Title
                item {
                    Text(
                        "Sleep",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // 2. Main Action Card (Blue)
                item {
                    SleepTrackingCard()
                }

                // 3. Stats Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SleepStatCard(
                            title = "Avg Duration",
                            value = viewModel.sleepStats.avgDuration,
                            icon = Icons.Outlined.AccessTime,
                            modifier = Modifier.weight(1f)
                        )
                        SleepStatCard(
                            title = "Avg Quality",
                            value = viewModel.sleepStats.avgQuality,
                            icon = Icons.Filled.WbSunny, // Using Sun icon for quality
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 4. List Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Recent Sleep", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Text("View All", color = Color(0xFF4A90E2), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }

                // 5. The List
                items(viewModel.sleepHistory) { day ->
                    SleepHistoryItem(day)
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

