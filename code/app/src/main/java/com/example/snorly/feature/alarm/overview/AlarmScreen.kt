package com.example.snorly.feature.alarm.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AlarmOff
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.snorly.core.common.components.HomeTopBar
import com.example.snorly.feature.alarm.components.AlarmCard
import com.example.snorly.feature.alarm.components.premiumBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(
    onEditAlarm: (Long) -> Unit,
    viewModel: AlarmScreenViewModel = viewModel()
) {
    val alarms by viewModel.alarms.collectAsState()
    val selection by viewModel.selectionUiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .premiumBackground() // <-- Apply the shine/grain effect here
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                HomeTopBar( // transparent bg
                    title = if (selection.selectionMode) "${selection.selectedIds.size} selected" else "Alarm",
                    scrollBehavior = scrollBehavior,
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

            Box(modifier = Modifier.padding(innerPadding)) {
                if (alarms.isEmpty()) {
                    AlarmEmptyState()
                } else {
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
    }
}

@Composable
fun AlarmEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // You can use Icons.Outlined.NotificationsOff or a custom painterResource image
        Icon(
            imageVector = Icons.Outlined.AlarmOff,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = Color.Gray.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No alarms set",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
        )

        Text(
            text = "Tap the + button to start your morning right.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
        )
    }
}
