package com.example.snorly.core.common.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.snorly.feature.Home

@Composable
fun NavController(modifier: Modifier = Modifier) {
    val navController = rememberNavController()



    NavHost(navController = navController, startDestination = Routes.main, modifier = modifier) {
        composable(route = Routes.main) {
            Home()
        }

    }
}
