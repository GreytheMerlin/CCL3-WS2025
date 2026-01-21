package com.example.snorly.feature.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.snorly.core.common.components.MainTopBar
import com.example.snorly.feature.settings.components.SettingsSectionCard
import com.example.snorly.feature.settings.components.SettingsTile

private const val PRIVACY_POLICY_URL =
    "https://gist.githubusercontent.com/laurenstuerk/99b65e2b12bfd6a0964c5c4a3a3f9c11/raw"
private const val TERMS_OF_SERVICE_URL =
    "https://gist.githubusercontent.com/laurenstuerk/90f675a4361774ddf14e8ca0bb2740d3/raw"
private const val SUPPORT_EMAIL = "your.email@example.com"

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel, onNavigateToProfile: () -> Unit
) {
    // Collect Real Data
    val userProfile by viewModel.userProfile.collectAsState()
    val bg = Color.Black
    val context = LocalContext.current

    Scaffold(
        containerColor = bg, topBar = {
            MainTopBar(
                title = "Settings"
            )
        }) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding), contentPadding = PaddingValues(
                top = 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 80.dp,
                start = 16.dp,
                end = 16.dp
            ), verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        onClick = {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null)
                            )
                            context.startActivity(intent)
                        })/*
                    SettingsTile(
                        icon = Icons.Outlined.RoomService,
                        title = "Service",
                        onClick = { })

                    SettingsSwitchTile(
                        icon = Icons.AutoMirrored.Outlined.VolumeUp,
                        title = "Gradually Increase Volume",
                        checked = false,
                        onCheckedChange = {viewModel.setMaxVolume(context) })
                    SettingsSwitchTile(
                        icon = Icons.Outlined.Snooze, // Changed Icon
                        title = "Smart Snooze",       // Changed Text
                        checked = false, onCheckedChange = { })*/
                }
            }

            // 4. ABOUT & LEGAL
            item {
                SettingsSectionCard(title = "ABOUT & LEGAL") {
                    // Version Info
                    SettingsTile(
                        icon = Icons.Outlined.Info,
                        title = "App Version",
                        value = "1.0.0"
                    )

                    // Privacy Policy
                    SettingsTile(
                        icon = Icons.Outlined.Shield,
                        title = "Privacy Policy",
                        onClick = { openUrl(context, PRIVACY_POLICY_URL) }
                    )

                    // Terms of Service
                    SettingsTile(
                        icon = Icons.Outlined.Description,
                        title = "Terms of Service",
                        onClick = { openUrl(context, TERMS_OF_SERVICE_URL) }
                    )

                    // Contact Support (Great for Play Store Trust)
                    SettingsTile(
                        icon = Icons.Outlined.Email,
                        title = "Contact Support",
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:$SUPPORT_EMAIL")
                                putExtra(Intent.EXTRA_SUBJECT, "Snorly Support - v1.0.0")
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }


            // 5. CLEAN BRANDING FOOTER
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp), // Increased padding for a better "end" feel
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Bedtime,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )

                    Text(
                        text = "Snorly",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Made with ❤️ for better mornings",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}


// Helper function to keep code clean
fun openUrl(context: android.content.Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}