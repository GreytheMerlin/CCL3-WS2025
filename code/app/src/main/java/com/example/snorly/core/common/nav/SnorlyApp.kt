package com.example.snorly.core.common.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun SnorlyApp() {
    val navController = rememberNavController()
    // Observe the current screen to decide if we show the FAB
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute != "alarm_create") {
                BottomBar(navController = navController)
            }
        },
        floatingActionButton = {
            // Logic: Only show FAB if we are on the Alarm Tab
            if (currentRoute == Destination.ALARM.route) {
                FloatingActionButton(
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
            modifier = Modifier.padding(innerPadding)
        )
    }
}