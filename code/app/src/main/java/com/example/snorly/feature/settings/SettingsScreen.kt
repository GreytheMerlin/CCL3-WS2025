package com.example.snorly.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.snorly.feature.settings.components.SettingsSectionCard
import com.example.snorly.feature.settings.components.SettingsSwitchTile
import com.example.snorly.feature.settings.components.SettingsTile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToProfile: () -> Unit // <--- Added Navigation Callback
) {
    // Collect Real Data
    val userProfile by viewModel.userProfile.collectAsState()

    val bg = Color.Black

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = Color.White, fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bg),
                windowInsets = WindowInsets(0.dp)
            )
        },
        containerColor = bg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // 1. OPTIMIZATION PROFILE
            item {
                SettingsSectionCard(title = "SLEEP OPTIMIZATION") {
                    // Check if profile is set (e.g. check if age is not null)
                    val statusText = if (userProfile.age != null) "Active" else "Setup"

                    SettingsTile(
                        icon = Icons.Outlined.Person,
                        title = "Optimization Profile",
                        value = statusText,
                        onClick = onNavigateToProfile // <--- Use the callback
                    )
                }
            }

            // 2. ALARM SETTINGS
            item {
                SettingsSectionCard(title = "ALARM SETTINGS") {
                    SettingsTile(
                        icon = Icons.Outlined.Notifications,
                        title = "Default Alarm Settings",
                        onClick = { }
                    )
                    SettingsSwitchTile(
                        icon = Icons.Outlined.VolumeUp,
                        title = "Gradually Increase Volume",
                        checked = true,
                        onCheckedChange = { }
                    )
                    SettingsSwitchTile(
                        icon = Icons.Outlined.Bedtime,
                        title = "Bedtime Reminder",
                        checked = false,
                        onCheckedChange = { }
                    )
                }
            }

            // 3. APPEARANCE
            item {
                SettingsSectionCard(title = "APPEARANCE") {
                    SettingsTile(
                        icon = Icons.Outlined.Palette,
                        title = "Theme",
                        value = "Dark",
                        onClick = { }
                    )
                }
            }

            // 4. ABOUT
            item {
                SettingsSectionCard(title = "ABOUT") {
                    SettingsTile(
                        icon = Icons.Outlined.Info,
                        title = "App Version",
                        value = "1.0.0",
                        onClick = { }
                    )
                    SettingsTile(
                        icon = Icons.Outlined.Email,
                        title = "Send Feedback",
                        onClick = { }
                    )
                }
            }

            // Bottom Padding
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}