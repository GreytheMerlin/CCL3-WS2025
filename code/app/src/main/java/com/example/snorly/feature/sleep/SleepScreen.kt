package com.example.snorly.feature.sleep

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.snorly.feature.sleep.components.SleepTrackingCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepScreen(
    viewModel: SleepViewModel,
    onAddSleepClick: () -> Unit,
    onSleepItemClick: (String) -> Unit
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

    Scaffold(
        topBar = {
            // 1. PLUS BUTTON IN TOP BAR
            CenterAlignedTopAppBar(
                title = { },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Black
                ),
                actions = {
                    IconButton(onClick = onAddSleepClick) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Sleep",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        if (viewModel.hasPermission) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 80.dp),
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

                // 3. TODAY'S STATS (Replaces old Averages)
                // We show the most recent item from history as "Today/Last Night"
                val todaysSleep = viewModel.sleepHistory.firstOrNull()

                // Stats Row
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
                        // A Specific Card for Today with big Duration
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Duration", color = Color.Gray, fontSize = 12.sp)
                                    Text(
                                        todaysSleep.durationFormatted,
                                        color = Color.White,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Row {
                                        Badge(todaysSleep.qualityLabel, todaysSleep.qualityColor)
                                        Spacer(Modifier.width(8.dp))
                                        // Placeholder for stages if available
                                        Badge("REM: --", Color.Gray)
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
                        Text(
                            "View All",
                            color = Color(0xFF4A90E2),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // 5. The List
                items(viewModel.sleepHistory) { day ->
                    // PASS THE CLICK DIRECTLY
                    SleepHistoryItem(
                        data = day,
                        onClick = {
                            if (day.id.isNotEmpty()) {
                                android.util.Log.d("SleepScreen", "Clicking ID: ${day.id}")
                                onSleepItemClick(day.id)
                            } else {
                                android.util.Log.e("SleepScreen", "Error: Sleep ID is empty")
                            }
                        }
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
                Image(
                    painter = painterResource(id = R.drawable.snorly_healthconnect),
                    contentDescription = "Snorly Healthconnect"
                )

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