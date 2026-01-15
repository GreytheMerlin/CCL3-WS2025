package com.example.snorly.core.common.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.snorly.core.database.AppDatabase
import com.example.snorly.core.health.HealthConnectManager
import com.example.snorly.feature.alarm.AlarmCreateScreen
import com.example.snorly.feature.alarm.AlarmScreen
import com.example.snorly.feature.alarm.screens.RepeatScreen
import com.example.snorly.feature.alarm.screens.RingtoneScreen
import com.example.snorly.feature.alarm.screens.VibrationScreen
import com.example.snorly.feature.challenges.screens.AddChallengeScreen
import com.example.snorly.feature.challenges.screens.ChallengeDetailScreen
import com.example.snorly.feature.challenges.screens.DismissChallengesScreen
import com.example.snorly.feature.challenges.viewmodel.ChallengeViewModel
import com.example.snorly.feature.report.ReportScreen
import com.example.snorly.feature.report.ReportViewModel
import com.example.snorly.feature.settings.ProfileScreen
import com.example.snorly.feature.settings.ProfileViewModel
import com.example.snorly.feature.settings.SettingsScreen
import com.example.snorly.feature.settings.SettingsViewModel
import com.example.snorly.feature.sleep.AddSleepScreen
import com.example.snorly.feature.sleep.AddSleepViewModel
import com.example.snorly.feature.sleep.SleepDetailScreen
import com.example.snorly.feature.sleep.SleepDetailViewModel
import com.example.snorly.feature.sleep.SleepScreen
import com.example.snorly.feature.sleep.SleepViewModel
import com.example.snorly.core.data.SleepRepository
import com.example.snorly.feature.alarm.AlarmViewModel

@Composable
fun AppNavHost(
    navController: NavHostController, modifier: Modifier = Modifier

) {
    val alarmViewModel: AlarmViewModel = viewModel()

    // Initialize the manager using the current context
    val context = androidx.compose.ui.platform.LocalContext.current

    val database = remember { AppDatabase.getDatabase(context) }

    val healthConnectManager = remember { HealthConnectManager(context) }

    val sleepRepository = remember {
        SleepRepository(database.sleepSessionDao(), healthConnectManager)
    }

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
                            factory = SleepViewModel.Factory(sleepRepository, healthConnectManager)
                        )

                        // REFRESH LOGIC
                        // We use collectAsState to react immediately to the Handle changes
                        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
                        val refreshState by savedStateHandle?.getStateFlow("refresh_sleep", false)!!
                            .collectAsState()

                        LaunchedEffect(refreshState) {
                            if (refreshState) {
                                android.util.Log.d("NAV", "Refreshing Sleep List...")
//                                sleepViewModel.checkPermissions() // Force Reload
                                // Reset the flag so we don't reload loop
                                savedStateHandle["refresh_sleep"] = false
                            }
                        }

                        SleepScreen(
                            viewModel = sleepViewModel,
                            onAddSleepClick = { navController.navigate("sleep_add") },
                            onSleepItemClick = { sleepId -> navController.navigate("sleep_detail/$sleepId") })
                    }

                    Destination.REPORT -> {
                        // Reuse the SAME manager
                        val reportViewModel: ReportViewModel = viewModel(
                            factory = ReportViewModel.Factory(healthConnectManager)
                        )

                        // REFRESH LOGIC (Same pattern as Sleep Screen)
                        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
                        val refreshState by savedStateHandle?.getStateFlow("refresh_sleep", false)!!.collectAsState()

                        LaunchedEffect(refreshState) {
                            if (refreshState) {
                                reportViewModel.loadReportData() // Reload!
                            }
                        }

                        ReportScreen(viewModel = reportViewModel)
                    }

                    Destination.SETTINGS -> {
                        val db = AppDatabase.getDatabase(context)
                        val settingsViewModel: SettingsViewModel = viewModel(
                            factory = SettingsViewModel.Factory(db.UserProfileDao())
                        )
                        SettingsScreen(
                            viewModel = settingsViewModel,
                            onNavigateToProfile = {
                                navController.navigate("settings_profile")
                            }
                        )
                    }
                }
            }
        }
        // === Alarm screens ===
        composable("alarm_create") { backStackEntry ->

            val selectedChallenges by backStackEntry.savedStateHandle.getStateFlow(
                "selectedChallenges", emptyList<String>()
            ).collectAsState()

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
            startDestination = "challenges_main", route = "challenges_graph"
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
                        navController.previousBackStackEntry?.savedStateHandle?.set(
                            "selectedChallenges", selectedNames
                        )
                    })
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
                        })
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
                    initialDays = initialDays, navController = navController
                )
            }
        }

        // === Sleep ===

        composable("sleep_add") {
            val viewModel: AddSleepViewModel = viewModel(
                factory = AddSleepViewModel.Factory(sleepRepository, null)
            )
            AddSleepScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSaveSuccess = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh_sleep", true)
                    navController.popBackStack()
                }
            )
        }

        // 2. EDIT ROUTE (With ID)
        composable(
            route = "sleep_edit/{sleepId}",
            arguments = listOf(navArgument("sleepId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sleepIdString = backStackEntry.arguments?.getString("sleepId")
            // CRITICAL FIX: Convert String ID back to Long for the Database
            val sleepId = sleepIdString?.toLongOrNull()

            val viewModel: AddSleepViewModel = viewModel(
                factory = AddSleepViewModel.Factory(sleepRepository, sleepId)
            )

            AddSleepScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSaveSuccess = {
                    // Tell detail screen to refresh
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        "refresh_sleep",
                        true
                    )

                    // Also tell the main list (two steps back) to refresh
                    // (Optional, depends on flow)
                    navController.popBackStack()
                })
        }

        composable(
            route = "sleep_detail/{sleepId}",
            arguments = listOf(navArgument("sleepId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sleepIdString = backStackEntry.arguments?.getString("sleepId")
            // CRITICAL FIX: Convert String ID back to Long for the Database
            val sleepId = sleepIdString?.toLongOrNull() ?: -1L

            // Updated to use SleepDetailViewModel.Factory(repository, id)
            val detailViewModel: SleepDetailViewModel = viewModel(
                factory = SleepDetailViewModel.Factory(sleepRepository, healthConnectManager, sleepId, context)
            )

            SleepDetailScreen(
                viewModel = detailViewModel,
                onBack = { navController.popBackStack() },
                onEdit = { id ->
                    // Navigate to add screen, passing ID would require Edit Mode logic
                    navController.navigate("sleep_edit/$id")
                },
                onDeleteSuccess = {
                    // We need to set this on the Main Sleep Screen's handle
                    // "previousBackStackEntry" refers to the screen BEFORE Detail (which is Main Sleep)
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("refresh_sleep", true)

                    navController.popBackStack()
                }
            )
        }

        composable("settings_profile") {
            val db = AppDatabase.getDatabase(androidx.compose.ui.platform.LocalContext.current)
            val viewModel: ProfileViewModel = viewModel(
                factory = ProfileViewModel.Factory(db.UserProfileDao())
            )
            ProfileScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}