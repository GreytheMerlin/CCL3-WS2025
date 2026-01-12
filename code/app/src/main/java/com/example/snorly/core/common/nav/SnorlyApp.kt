package com.example.snorly.core.common.nav

import android.R.attr.onClick
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.material3.BadgeDefaults.containerColor
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

@Composable
fun SnorlyApp() {
    val navController = rememberNavController()
    // Observe the current screen to decide if we show the FAB
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val hazeState = remember { HazeState() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (currentRoute != "alarm_create") {
//                BottomBar(navController = navController)
                GlassBottomBar(
                    navController = navController,
                    hazeState = hazeState
                )
            }
        },
        floatingActionButton = {
            // Logic: Only show FAB if we are on the Alarm Tab
            if (currentRoute == Destination.ALARM.route) {
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
            modifier = Modifier.haze(
                state = hazeState,
                style = HazeDefaults.style(backgroundColor = MaterialTheme.colorScheme.background)
            )
                // 3. IGNORE bottom padding so content scrolls behind
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    start = innerPadding.calculateLeftPadding(LayoutDirection.Ltr),
                    end = innerPadding.calculateRightPadding(LayoutDirection.Ltr)
                )
                .fillMaxSize()
        )
    }
}