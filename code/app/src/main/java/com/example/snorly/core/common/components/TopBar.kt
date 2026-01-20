package com.example.snorly.core.common.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBarDefaults.windowInsets
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

// 1. For Main Screens (Alarm, Sleep, Report)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    title: String,
    onSettingsClick: () -> Unit = {},
    actions: @Composable () -> Unit = {}   // ✅ NEW, optional
) {
    TopAppBar(
        title = {
            Text(
                text = title, style = MaterialTheme.typography.headlineMedium
            )
        }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ), actions = {
            actions()

            // OPTIONAL: keep settings icon if you want
            // IconButton(onClick = onSettingsClick) {
            //     Icon(Icons.Default.Settings, contentDescription = "Settings")
            // }
        }
    )

}

// 2. For Detail Screens (Create Alarm, Edit Sleep)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackTopBar(
    title: String, onBackClick: () -> Unit
) {
    CenterAlignedTopAppBar( // Center aligned looks better for sub-pages
        title = {
            Text(
                text = title, style = MaterialTheme.typography.titleLarge
            )
        }, navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back"
                )
            }
        }, colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ), windowInsets = WindowInsets(0)

    )
}