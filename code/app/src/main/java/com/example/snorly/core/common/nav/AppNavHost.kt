package com.example.snorly.core.common.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.snorly.feature.alarm.AlarmScreen
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
        }
    }
}