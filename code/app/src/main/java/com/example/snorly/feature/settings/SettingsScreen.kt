package com.example.snorly.feature.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.snorly.core.common.components.MainTopBar
import com.example.snorly.feature.settings.components.SettingsSectionCard
import com.example.snorly.feature.settings.components.SettingsSwitchTile
import com.example.snorly.feature.settings.components.SettingsTile

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel, onNavigateToProfile: () -> Unit
) {
    // Collect Real Data
    val userProfile by viewModel.userProfile.collectAsState()
    val bg = Color.Black

    Scaffold(
        containerColor = bg,
        topBar = {
            MainTopBar(
                title = "Settings"
            )
        }
        ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
            .padding(innerPadding),
            contentPadding = PaddingValues(
                top = 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 80.dp,
                start = 16.dp,
                end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 2. SLEEP GOALS (Renamed from Optimization)
            item {
                SettingsSectionCard(title = "SLEEP GOALS") {
                    // Check if targets are set
                    val statusText =
                        if (!userProfile.targetBedTime.isNullOrBlank() && !userProfile.targetWakeTime.isNullOrBlank()) {
                            "${userProfile.targetBedTime} - ${userProfile.targetWakeTime}"
                        } else {
                            "Setup"
                        }

                    SettingsTile(
                        icon = Icons.Outlined.Bedtime, // Changed Icon
                        title = "Bedtime Schedule",    // Changed Text
                        value = statusText, onClick = onNavigateToProfile
                    )
                }
            }

            // 3. ALARM SETTINGS
            item {
                SettingsSectionCard(title = "ALARM SETTINGS") {
                    SettingsTile(
                        icon = Icons.Outlined.Notifications,
                        title = "Default Alarm Settings",
                        onClick = { })
                    SettingsTile(
                        icon = Icons.Outlined.RoomService,
                        title = "Service",
                        onClick = { })
                    SettingsSwitchTile(
                        icon = Icons.Outlined.VolumeUp,
                        title = "Gradually Increase Volume",
                        checked = true,
                        onCheckedChange = { })
                    SettingsSwitchTile(
                        icon = Icons.Outlined.Snooze, // Changed Icon
                        title = "Smart Snooze",       // Changed Text
                        checked = false, onCheckedChange = { })
                }
            }

            // 4. ABOUT
            item {
                SettingsSectionCard(title = "ABOUT") {
                    SettingsTile(
                        icon = Icons.Outlined.Info,
                        title = "App Version",
                        value = "1.0.0",
                        onClick = { })
                    SettingsTile(
                        icon = Icons.Outlined.Email, title = "Send Feedback", onClick = { })
                }
            }

            // Bottom Padding
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}