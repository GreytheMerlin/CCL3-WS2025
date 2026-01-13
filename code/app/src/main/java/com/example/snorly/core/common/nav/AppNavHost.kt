package com.example.snorly.core.common.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.snorly.feature.alarm.AlarmCreateScreen
import com.example.snorly.feature.alarm.AlarmScreen
import com.example.snorly.feature.alarm.screens.DismissChallengeScreen
import com.example.snorly.feature.alarm.screens.RepeatScreen
import com.example.snorly.feature.alarm.screens.RingtoneScreen
import com.example.snorly.feature.alarm.screens.VibrationScreen
import com.example.snorly.feature.report.ReportScreen
import com.example.snorly.feature.settings.SettingsScreen
import com.example.snorly.feature.sleep.SleepScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Destination.ALARM.route,
        modifier = modifier
    ) {
        Destination.entries.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    Destination.ALARM -> AlarmScreen()
                    Destination.SLEEP -> SleepScreen()
                    Destination.REPORT -> ReportScreen()
                    Destination.SETTINGS -> SettingsScreen()
                }
            }
            // === Alarm screens ===
            composable("alarm_create") {
                AlarmCreateScreen(
                    onClose = { navController.popBackStack() },
                    // Pass navigation lambdas to the screen
                    onNavigateToRingtone = { navController.navigate("alarm_ringtone") },
                    onNavigateToVibration = { navController.navigate("alarm_vibration") },
                    onNavigateToChallenge = { navController.navigate("alarm_challenge") }
                )
            }
            composable("alarm_ringtone") {
                RingtoneScreen(onBack = { navController.popBackStack() })
            }
            composable("alarm_vibration") {
                VibrationScreen(onBack = { navController.popBackStack() })
            }
            composable("alarm_challenge") {
                DismissChallengeScreen(onBack = { navController.popBackStack() })
            }
            composable("alarm_repeat") {
                RepeatScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}