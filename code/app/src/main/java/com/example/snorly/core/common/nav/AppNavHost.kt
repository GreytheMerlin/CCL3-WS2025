package com.example.snorly.core.common.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.example.snorly.core.health.HealthConnectManager
import com.example.snorly.feature.alarm.AlarmCreateScreen
import com.example.snorly.feature.alarm.wakeup.AlarmScreen
import com.example.snorly.feature.alarm.screens.RepeatScreen
import com.example.snorly.feature.alarm.screens.RingtoneScreen
import com.example.snorly.feature.alarm.screens.VibrationScreen
import com.example.snorly.feature.alarm.wakeup.AlarmViewModel
import com.example.snorly.feature.challenges.screens.AddChallengeScreen
import com.example.snorly.feature.challenges.screens.ChallengeDetailScreen
import com.example.snorly.feature.challenges.screens.DismissChallengesScreen
import com.example.snorly.feature.challenges.viewmodel.ChallengeViewModel
import com.example.snorly.feature.report.ReportScreen
import com.example.snorly.feature.settings.SettingsScreen
import com.example.snorly.feature.sleep.SleepScreen
import com.example.snorly.feature.sleep.SleepViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier

) {
    val alarmViewModel: AlarmViewModel = viewModel()

    // Initialize the manager using the current context
    val context = androidx.compose.ui.platform.LocalContext.current
    val healthConnectManager = remember { HealthConnectManager(context) }

    NavHost(
        navController = navController,
        startDestination = Destination.ALARM.route,
        modifier = modifier
    ) {
        Destination.entries.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    Destination.ALARM -> AlarmScreen()
                    Destination.SLEEP -> {
                        // We create the ViewModel right here, scoped to this screen
                        val sleepViewModel: SleepViewModel = viewModel(
                            factory = SleepViewModel.Factory(healthConnectManager)
                        )
                        SleepScreen(viewModel = sleepViewModel)
                    }
                    Destination.REPORT -> ReportScreen()
                    Destination.SETTINGS -> SettingsScreen()
                }
            }
        }
        // === Alarm screens ===
        composable("alarm_create") { backStackEntry ->

            val selectedChallenges by backStackEntry.savedStateHandle
                .getStateFlow("selectedChallenges", emptyList<String>())
                .collectAsState()

            AlarmCreateScreen(
                navController = navController,
                onClose = { navController.popBackStack() },
                onCreateAlarm = { entity ->
                    alarmViewModel.insert(entity)
                    navController.popBackStack()
                },// Pass navigation lambdas to the screen
                onNavigateToRingtone = { navController.navigate("alarm_ringtone") },
                onNavigateToVibration = { navController.navigate("alarm_vibration") },
                onNavigateToChallenge = { navController.navigate("challenges_graph") },
                selectedChallenges = selectedChallenges
            )
        }
        composable("alarm_ringtone") {
            RingtoneScreen(
                onBack = { navController.popBackStack() },
                onCategoryClick = { categoryId ->
                    // Route to the list screen, passing the ID
                    // e.g. "ringtone_list/nature" or "ringtone_list/spotify"
                    navController.navigate("ringtone_list/$categoryId")
                })

        }
        composable("alarm_vibration") {
            VibrationScreen(onBack = { navController.popBackStack() })
        }

        // Ringtone screen
        composable(
            route = "ringtone_list/{categoryId}",
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
        ) {
            val id = it.arguments?.getString("categoryId")
            // RingtoneListScreen(categoryId = id) ...
        }
        // === Challenges ===
        navigation(
            startDestination = "challenges_main",
            route = "challenges_graph"
        ) {
            // A. Create the ViewModel scoped to this GRAPH, not the whole app.
            // When you exit "challenges_graph", this ViewModel is cleared.
            composable("challenges_main") { backStackEntry ->
                // We get the ViewModelStoreOwner from the graph entry
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("challenges_graph")
                }
                val challengeViewModel: ChallengeViewModel = viewModel(parentEntry)

                DismissChallengesScreen(
                    onBack = { navController.popBackStack() },
                    onAddClick = { navController.navigate("challenges_add") },
                    viewModel = challengeViewModel,
                    onResult = { selectedNames ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("selectedChallenges", selectedNames)
                    }
                )
            }

            composable("challenges_add") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("challenges_graph")
                }
                val challengeViewModel: ChallengeViewModel = viewModel(parentEntry)

                AddChallengeScreen(
                    onBack = { navController.popBackStack() },
                    onChallengeClick = { id -> navController.navigate("challenges_detail/$id") },
                    viewModel = challengeViewModel
                )
            }

            composable(
                route = "challenges_detail/{challengeId}",
                arguments = listOf(navArgument("challengeId") { type = NavType.StringType })
            ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("challenges_graph")
                }
                val challengeViewModel: ChallengeViewModel = viewModel(parentEntry)

                val id = backStackEntry.arguments?.getString("challengeId")
                val challenge = challengeViewModel.getChallengeById(id ?: "")

                if (challenge != null) {
                    ChallengeDetailScreen(
                        challenge = challenge,
                        onBack = { navController.popBackStack() },
                        onSave = {
                            challengeViewModel.addChallenge(challenge)
                            // Pop back to Main Screen, removing Add and Detail from stack
                            navController.popBackStack("challenges_main", inclusive = false)
                        }
                    )
                }
            }
            composable(
                route = "alarm_repeat/{currentSelection}",
                arguments = listOf(navArgument("currentSelection") {
                    type = NavType.StringType
                    defaultValue = "0,0,0,0,0,0,0" // Default to all 0s
                })
            ) { backStackEntry ->
                val selectionStr = backStackEntry.arguments?.getString("currentSelection") ?: ""

                // Parse "1,0,1..." back to List<Int>
                // Map safe check: must be integer, default to 0
                val parsedList = if (selectionStr.isBlank()) {
                    List(7) { 0 }
                } else {
                    selectionStr.split(",").mapNotNull { it.toIntOrNull() }
                }

                // Final safety: ensure exactly 7 items
                val initialDays = if (parsedList.size == 7) parsedList else List(7) { 0 }

                RepeatScreen(
                    initialDays = initialDays,
                    navController = navController
                )
            }
        }
    }
}