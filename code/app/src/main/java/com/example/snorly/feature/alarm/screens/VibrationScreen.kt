package com.example.snorly.feature.alarm.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.snorly.core.common.components.BackTopBar

@Composable
fun VibrationScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            BackTopBar(
                title = "Vibration Pattern",
                onBackClick = onBack
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Vibration Patterns Go Here")
        }
    }
}