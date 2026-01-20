package com.example.snorly.feature.alarm.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.snorly.core.common.components.HomeTopBar
import com.example.snorly.feature.alarm.components.AlarmCard
import com.example.snorly.feature.alarm.components.premiumBackground

@Composable
fun AlarmScreen(
    onEditAlarm: (Long) -> Unit,
    viewModel: AlarmScreenViewModel = viewModel()
) {
    val alarms by viewModel.alarms.collectAsState()
    val selection by viewModel.selectionUiState.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .premiumBackground() // <-- Apply the shine/grain effect here
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                HomeTopBar( // transparent bg
                    title = if (selection.selectionMode) "${selection.selectedIds.size} selected" else "Alarm",
                    actions = {
                        if (selection.selectionMode) {
                            IconButton(onClick = { viewModel.deleteSelected() }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Delete selected")
                            }
                        }
                    }
                )
            }
        ) { innerPadding ->
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
                items(alarms) { alarm ->
                    val dayText = viewModel.selectedDayNames(alarm.days)
                    val isSelected = selection.selectedIds.contains(alarm.id)

                    AlarmCard(
                        alarm = alarm,
                        dayText = dayText,
                        selectionMode = selection.selectionMode,
                        selected = isSelected,
                        onClick = { viewModel.onAlarmClick(alarm.id, onEditAlarm) },
                        onLongClick = { viewModel.onAlarmLongPress(alarm.id) },
                        onSelectToggle = { viewModel.toggleSelected(alarm.id) },
                        onToggle = { checked -> viewModel.toggleAlarm(alarm.id, checked) }
                    )
                }
            }
        }
    }
}
