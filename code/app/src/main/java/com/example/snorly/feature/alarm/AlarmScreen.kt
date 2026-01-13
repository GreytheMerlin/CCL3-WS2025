package com.example.snorly.feature.alarm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.snorly.feature.alarm.components.AlarmCard



@Composable
fun AlarmScreen(
    // We inject the ViewModel here.
    // This allows you to easily swap it out for testing later if needed.
    viewModel: AlarmViewModel = viewModel()
) {


    // 1. Observe the state
    // Whenever the list changes in the ViewModel, 'alarms' will update here
    val alarms by viewModel.alarms.collectAsState()




    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 2. Feed the data to the list
            items(
                items = alarms,
                key = { it.id } // Unique key helps Compose animate efficiently
            ) { alarm ->
                AlarmCard(
                    alarm = alarm,
                    onToggle = { isChecked ->
                        // 3. Pass the event back to the ViewModel
                        viewModel.toggleAlarm(alarm.id, isChecked)
                    }
                )
            }
        }
    }
}