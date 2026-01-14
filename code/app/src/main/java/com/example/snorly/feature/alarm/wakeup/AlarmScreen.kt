package com.example.snorly.feature.alarm.wakeup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.snorly.core.common.components.HomeTopBar
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

    Scaffold(
        topBar = {
            HomeTopBar(title = "Alarm")
        }
    ) {innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 2. Feed the data to the list
            items(alarms) { alarm ->
                val dayText = viewModel.selectedDayNames(alarm.days)

                AlarmCard(
                    alarm = alarm,
                    dayText = dayText,
                    onToggle = { checked ->
                        viewModel.toggleAlarm(alarm.id, checked)
                    }
                )
            }
        }
    }
}