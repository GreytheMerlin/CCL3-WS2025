package com.example.snorly.core.common.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.example.snorly.core.database.AppDatabase
import com.example.snorly.core.health.HealthConnectManager
import com.example.snorly.feature.alarm.create.AlarmCreateScreen
import com.example.snorly.feature.alarm.create.AlarmCreateViewModel
import com.example.snorly.feature.alarm.overview.AlarmScreen
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
import com.example.snorly.feature.sleep.SleepViewModel
import com.example.snorly.core.data.SleepRepository
import com.example.snorly.core.database.PreferenceManager
import com.example.snorly.core.database.repository.RingtoneRepository
import com.example.snorly.feature.alarm.ToneGenerator.ComposerListScreen
import com.example.snorly.feature.alarm.ToneGenerator.ComposerListViewModel
import com.example.snorly.feature.alarm.ToneGenerator.ComposerViewModel
import com.example.snorly.feature.alarm.ToneGenerator.ComposerScreen
import com.example.snorly.feature.alarm.screens.RingtoneListScreen
import com.example.snorly.feature.onboarding.OnboardingScreen
import com.example.snorly.feature.onboarding.OnboardingViewModel
import com.example.snorly.feature.onboarding.OnboardingViewModelFactory
import com.example.snorly.feature.settings.LegalScreen
import com.example.snorly.feature.settings.components.LegalTexts

import com.example.snorly.feature.sleep.SleepScreen

@Composable
fun AppNavHost(
    navController: NavHostController, modifier: Modifier = Modifier

) {

    val alarmCreateViewModel: AlarmCreateViewModel = viewModel()

    // Initialize the manager using the current context
    val context = androidx.compose.ui.platform.LocalContext.current

    // 1. Initialize PreferenceManager
    val preferenceManager = remember { PreferenceManager(context) }

    // 2. Collect the onboarding status as state
    val isOnboardingCompleted by preferenceManager.isOnboardingCompleted
        .collectAsState(initial = null) // null indicates "still loading from disk"

    val database = remember { AppDatabase.getDatabase(context) }

    val ringtoneRepository = remember { RingtoneRepository(database.composedRingtoneDao()) }

    val healthConnectManager = remember { HealthConnectManager(context) }

    val sleepRepository = remember {
        SleepRepository(database.sleepSessionDao(), healthConnectManager)
    }

    // 3. Handle the loading state
    if (isOnboardingCompleted == null) {
        // Optional: Return a Splash Screen or empty Box while reading from disk
        return
    }

    // 4. Determine start destination
    val startRoute = if (isOnboardingCompleted == true) {
        Destination.ALARM.route
    } else {
        "onboarding_route"
    }

    NavHost(
        navController = navController,
        startDestination = startRoute, // Destination.ALARM.route
        modifier = modifier
    ) {

        // --- Onboarding Route (Standalone) ---
        composable("onboarding_route") {
            val onboardingViewModel: OnboardingViewModel = viewModel(
                factory = OnboardingViewModelFactory(context, healthConnectManager, preferenceManager)
            )

            OnboardingScreen(
                viewModel = onboardingViewModel,
                onFinish = {
                    // Navigate to Main and clear the onboarding from the backstack
                    navController.navigate(Destination.ALARM.route) {
                        popUpTo("onboarding_route") { inclusive = true }
                    }
                }
            )
        }

        Destination.entries.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    Destination.ALARM -> AlarmScreen(onEditAlarm = { alarmId ->
                        navController.navigate("alarm_create?alarmId=$alarmId")
                    })

                    Destination.SLEEP -> {
                        // We create the ViewModel right here, scoped to this screen
                        val sleepViewModel: SleepViewModel = viewModel(
                            factory = SleepViewModel.Factory(
                                sleepRepository, healthConnectManager, context
                            )
                        )

                        // REFRESH LOGIC
                        // We use collectAsState to react immediately to the Handle changes
                        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
                        val refreshState by savedStateHandle?.getStateFlow("refresh_sleep", false)!!
                            .collectAsState()

                        LaunchedEffect(refreshState) {
                            if (refreshState) {
                                // CALL THE SYNC FUNCTION
                                sleepViewModel.syncSleepData()
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
                        val userProfileDao = database.UserProfileDao()
                        val reportViewModel: ReportViewModel = viewModel(
                            factory = ReportViewModel.Factory(
                                manager = healthConnectManager, userProfileDao = userProfileDao
                            )
                        )

                        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
                        // Use collectAsStateWithLifecycle for better resource management
                        val refreshNeeded by savedStateHandle?.getStateFlow(
                            "refresh_sleep", false
                        )!!.collectAsStateWithLifecycle()

                        LaunchedEffect(refreshNeeded) {
                            if (refreshNeeded) {
                                reportViewModel.refresh() // Trigger the reactive refresh
                                savedStateHandle.set("refresh_sleep", false) // Reset trigger
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
                            },
                            onNavigateToLegal = { type -> navController.navigate("settings_legal/$type") })
                    }
                }
            }
        }
        // === Alarm screens ===
        composable(
            route = "alarm_create?alarmId={alarmId}", arguments = listOf(
                navArgument("alarmId") {
                    type = NavType.LongType
                    defaultValue = -1L
                })
        ) { backStackEntry ->

            val selectedChallenges by backStackEntry.savedStateHandle.getStateFlow(
                "selectedChallenges", emptyList<String>()
            ).collectAsState()

            val alarmIdArg = backStackEntry.arguments?.getLong("alarmId") ?: -1L
            val alarmId = if (alarmIdArg == -1L) null else alarmIdArg

            AlarmCreateScreen(
                navController = navController,
                alarmId = alarmId, // ✅ this is the important part
                onClose = { navController.popBackStack() },
                onNavigateToRingtone = { navController.navigate("alarm_ringtone") },
                onNavigateToVibration = { navController.navigate("alarm_vibration") },
                onNavigateToChallenge = { navController.navigate("challenges_graph") },

                )
        }

        composable("alarm_ringtone") {
            RingtoneScreen(
                onBack = { navController.popBackStack() },
                onCategoryClick = { categoryId ->
                    // Route to the list screen, passing the ID
                    // e.g. "ringtone_list/nature" or "ringtone_list/spotify"
                    if (categoryId == "composer") {
                        navController.navigate("composer")
                    } else {
                        // Otherwise, go to the standard list (Nature, Spotify, etc.)
                        navController.navigate("ringtone_list/$categoryId")
                    }
                })

        }

        composable("composer") {
            val viewModel: ComposerViewModel = viewModel(
                factory = ComposerViewModel.Factory(ringtoneRepository)
            )

            ComposerScreen(
                onBack = { navController.popBackStack() },
                onListClick = { navController.navigate("composer_list") },
                viewModel = viewModel
            )
        }

        composable("composer_list") {
            val viewModel: ComposerListViewModel = viewModel(
                factory = ComposerListViewModel.Factory(ringtoneRepository)
            )
            ComposerListScreen(
                onBack = { navController.popBackStack() }, onSelect = { ringtone ->
                    // Pass result back to AlarmCreateScreen
                    val previousBackStack = navController.getBackStackEntry("alarm_create")

                    // Set the Name to display in the UI
                    previousBackStack.savedStateHandle["selected_ringtone_name"] = ringtone.name

                    // Set the URI with a special prefix "composed:" so the Service recognizes it
                    previousBackStack.savedStateHandle["selected_ringtone_uri"] =
                        "composed:${ringtone.id}"

                    //Return to Alarm Create (pop everything above it)
                    navController.popBackStack("alarm_create", inclusive = false)
                }, viewModel = viewModel
            )
        }

        composable("alarm_vibration") {
            VibrationScreen(onBack = { navController.popBackStack() })
        }

        // Ringtone screen
        composable(
            route = "ringtone_list/{categoryId}",
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
        ) {
            // We don't need to extract args manually if using SavedStateHandle in ViewModel
            // But we pass callbacks to handle the selection result
            RingtoneListScreen(
                categoryId = it.arguments?.getString("categoryId") ?: "device",
                onBack = { navController.popBackStack() },
                onRingtoneSelected = { name, uri ->
                    // Pass result back to AlarmCreateScreen
                    // We assume 'alarm_create' is in the backstack.
                    // We set the result in the 'savedStateHandle' of the previous entry.
                    navController.getBackStackEntry("alarm_create").savedStateHandle["selected_ringtone_name"] =
                        name
                    navController.getBackStackEntry("alarm_create").savedStateHandle["selected_ringtone_uri"] =
                        uri

                    // Pop back to Create Screen (skipping category selection)
                    // Or pop once to go back to categories. Usually, selecting a sound returns to the form.
                    navController.popBackStack("alarm_create", inclusive = false)
                })
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
                            "selected_challenges_result", selectedNames
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
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        "refresh_sleep", true
                    )
                    navController.popBackStack()
                })
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
                        "refresh_detail",
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
                factory = SleepDetailViewModel.Factory(
                    sleepRepository, healthConnectManager, sleepId, context
                )
            )

            val refreshDetail by backStackEntry.savedStateHandle.getStateFlow(
                "refresh_detail",
                false
            ).collectAsState()

            LaunchedEffect(refreshDetail) {
                if (refreshDetail) {
                    // FIX: Call the method on the INSTANCE, not the class
                    detailViewModel.loadRecord()

                    // Update Previous Screen
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        "refresh_sleep",
                        true
                    )

                    // Reset Flag
                    backStackEntry.savedStateHandle["refresh_detail"] = false
                }
            }

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
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        "refresh_sleep",
                        true
                    )

                    navController.popBackStack()
                })
        }

        composable("settings_profile") {
            val db = AppDatabase.getDatabase(androidx.compose.ui.platform.LocalContext.current)
            val viewModel: ProfileViewModel = viewModel(
                factory = ProfileViewModel.Factory(db.UserProfileDao())
            )
            ProfileScreen(
                viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(
            route = "settings_legal/{legalType}",
            arguments = listOf(navArgument("legalType") { type = NavType.StringType })
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("legalType")
            val title = if (type == "privacy") "Privacy Policy" else "Terms of Service"
            val content =
                if (type == "privacy") LegalTexts.PRIVACY_POLICY else LegalTexts.TERMS_OF_SERVICE

            LegalScreen(
                title = title, content = content, onBack = { navController.popBackStack() })
        }

// Update the SETTINGS composable call:
        composable(Destination.SETTINGS.route) {
            val db = AppDatabase.getDatabase(context)
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.Factory(db.UserProfileDao())
            )
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateToProfile = { navController.navigate("settings_profile") },
                onNavigateToLegal = { type -> navController.navigate("settings_legal/$type") })
        }
    }
}