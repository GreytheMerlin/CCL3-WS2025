package com.example.snorly.feature.sleep

import android.util.Log
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import com.example.snorly.core.common.components.MainTopBar
import com.example.snorly.feature.sleep.components.ConnectHealthConnectBanner
import com.example.snorly.feature.sleep.components.SleepHistoryItem
import com.example.snorly.feature.sleep.components.UpgradedSleepCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepScreen(
    viewModel: SleepViewModel, onAddSleepClick: () -> Unit, onSleepItemClick: (String) -> Unit, onEditSleepClick: (String) -> Unit
) {
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        // This block runs when the user closes the system dialog.
        // We tell the ViewModel to check again.
        viewModel.checkPermissionsAndSync()
    }

    DisposableEffect(Unit) {
        onDispose {
            if (viewModel.trackingMode == SleepTrackingMode.GOOD_MORNING) {
                viewModel.resetToIdle()
            }
        }
    }

    // Pull Down to Refresh State
    val pullRefreshState = rememberPullToRefreshState()

    Scaffold(
        containerColor = Color.Black, topBar = {
            MainTopBar(
                title = "Sleep",
                actionIcon = Icons.Default.Add,
                actionDescription = "Add Sleep",
                onActionClick = { onAddSleepClick() })
        }) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = viewModel.isSyncing,
            onRefresh = { viewModel.syncSleepData() },
            state = pullRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(
                    top = 16.dp,
                    bottom = innerPadding.calculateBottomPadding() + 24.dp,
                    start = 16.dp,
                    end = 16.dp
                ), verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                if (viewModel.isHealthConnectAvailable && !viewModel.hasPermission) {
                    item {
                        ConnectHealthConnectBanner(
                            onConnectClick = {
                                permissionsLauncher.launch(viewModel.requiredPermissions)
                            })
                    }
                }
//                // Main Action Card
                item {
                    UpgradedSleepCard(
                        mode = viewModel.trackingMode,
                        startTime = viewModel.trackingStartTime,
                        onStart = { viewModel.startTracking() },
                        onWakeUp = { viewModel.stopTracking() },
                        onLogSleep = {
                            viewModel.lastSessionId?.let { id ->
                                Log.e("iddebug", " id is: $id")
                                onEditSleepClick(id)
                                viewModel.resetToIdle()
                            }
                        }
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
                        data = day, onClick = {
                            if (day.id.isNotEmpty()) onSleepItemClick(day.id)
                        })
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