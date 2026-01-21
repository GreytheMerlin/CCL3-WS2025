package com.example.snorly.core.common.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun SnorlyApp(onDataLoaded: () -> Unit) {
    val navController = rememberNavController()
    // Observe the current screen to decide if we show the FAB
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Define which screens are "Main Tabs" automatically
    // This creates a list: ["alarm", "sleep", "report", "settings"]
    val mainTabs = remember { Destination.entries.map { it.route } }

    // Determine visibility
    val showBottomBar = currentRoute in mainTabs
    val showFab = currentRoute == Destination.ALARM.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                BottomBar(navController = navController)
            }
        },
        floatingActionButton = {
            // Logic: Only show FAB if we are on the Alarm Tab
            if (showFab) {
                FloatingActionButton(
                    containerColor = MaterialTheme.colorScheme.primary,
                    onClick = { navController.navigate("alarm_create") }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add, // Or use painterResource(R.drawable.ic_add)
                        contentDescription = "Create Alarm"
                    )
                }
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            onDataLoaded = onDataLoaded,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        )
    }
}

