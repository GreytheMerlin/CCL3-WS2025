package com.example.snorly.feature.sleep

import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import com.example.snorly.feature.sleep.components.ConnectHealthConnectBanner
import com.example.snorly.feature.sleep.components.SleepHistoryItem
import com.example.snorly.feature.sleep.components.SleepTrackingCard
import com.example.snorly.feature.sleep.components.UltimateSleepCard
import com.example.snorly.feature.sleep.components.UpgradedSleepCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepScreen(
    viewModel: SleepViewModel, onAddSleepClick: () -> Unit, onSleepItemClick: (String) -> Unit
) {
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        // This block runs when the user closes the system dialog.
        // We tell the ViewModel to check again.
        viewModel.checkPermissionsAndSync()
    }

    // Pull Down to Refresh State
    val pullRefreshState = rememberPullToRefreshState()

    Scaffold(
        containerColor = Color.Black
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = viewModel.isSyncing,
            onRefresh = { viewModel.syncSleepData() },
            state = pullRefreshState,
            modifier = Modifier
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black), // Set back to Black
                contentPadding = PaddingValues(
                    top = 16.dp,
                    bottom = innerPadding.calculateBottomPadding() + 80.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Title
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sleep",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = onAddSleepClick,
                            modifier = Modifier
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add Sleep",
                                tint = Color.White
                            )
                        }
                    }
                }
                if (viewModel.isHealthConnectAvailable && !viewModel.hasPermission) {
                    item {
                        ConnectHealthConnectBanner(
                            onConnectClick = {
                                permissionsLauncher.launch(viewModel.requiredPermissions)
                            }
                        )
                    }
                }
                // Main Action Card (Blue)
                item {
                    SleepTrackingCard(
                        isTracking = viewModel.isTracking,
                        onToggleTracking = { viewModel.toggleTracking() }
                    )
                }
                item {
                    UpgradedSleepCard(
                        isTracking = viewModel.isTracking,
                        onToggleTracking = { viewModel.toggleTracking() }
                    )
                }
                item {
                    UltimateSleepCard(
                        isTracking = viewModel.isTracking,
                        onToggleTracking = { viewModel.toggleTracking() }
                    )
                }

                // TODAY'S STATS (Replaces old Averages)
                // We show the most recent item from history as "Today/Last Night"
                if (viewModel.sleepHistory.isNotEmpty()) {
                    val todaysSleep = viewModel.sleepHistory.firstOrNull()
                    if (todaysSleep != null) {
                        item {
                            Text(
                                "Last Night",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp
                            )
                        }
                        item {
                            //stats Card Logic
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Duration", color = Color.Gray, fontSize = 12.sp)
                                            Text(
                                                viewModel.latestSleepDuration,
                                                color = Color.White,
                                                fontSize = 32.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                "Snorly Sleep Score",
                                                color = Color.Gray,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                viewModel.latestSleepScore,
                                                color = if (viewModel.latestSleepScore != "--") Color(
                                                    0xFF4CAF50
                                                ) else Color.Gray,
                                                fontSize = 32.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(16.dp))
                                    Row {
                                        Badge(todaysSleep.qualityLabel, todaysSleep.qualityColor)
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. List Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Recent Sleep",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // 5. Empty State or List
                if (viewModel.sleepHistory.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No sleep logs yet.\nTap + to add one manually.",
                                color = Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(viewModel.sleepHistory) { day ->
                        SleepHistoryItem(
                            data = day,
                            onClick = {
                                if (day.id.isNotEmpty()) onSleepItemClick(day.id)
                            }
                        )
                    }
                }
            }
        }
    }
}


// Helper for badges
@Composable
fun Badge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}