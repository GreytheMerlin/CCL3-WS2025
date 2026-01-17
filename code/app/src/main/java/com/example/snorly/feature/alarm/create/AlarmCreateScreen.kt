@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.snorly.feature.alarm.create

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.snorly.feature.alarm.components.SettingRow
import com.example.snorly.feature.alarm.components.SnoozeSlider
import com.example.snorly.feature.alarm.components.TimePickerWheel
import com.example.snorly.feature.alarm.components.ToggleRow

@Composable
fun AlarmCreateScreen(
    navController: NavController,
    alarmViewModel: AlarmCreateViewModel = viewModel(),
    alarmId: Long? = null,
    onClose: () -> Unit = {},
    onNavigateToRingtone: () -> Unit = {},
    onNavigateToVibration: () -> Unit = {},
    onNavigateToChallenge: () -> Unit = {},
    selectedChallengesFromNav: List<String> = emptyList()
) {
    val state by alarmViewModel.uiState.collectAsState()

    val bg = Color(0xFF000000)
    val card = Color(0xFF1B1B1B)
    val muted = Color(0xFFB3B3B3)
    val divider = Color(0xFF2A2A2A)
    val accent = Color(0xFFFFE7A3)
    val primaryBtn = Color(0xFF1677FF)

    // Load alarm if editing
    LaunchedEffect(alarmId) {
        alarmViewModel.init(alarmId)
    }

    // Optional: if challenges are provided via navigation args, push into VM
    LaunchedEffect(selectedChallengesFromNav) {
        if (selectedChallengesFromNav.isNotEmpty()) {
            alarmViewModel.setSelectedChallenges(selectedChallengesFromNav)
        }
    }

    // Repeat days result receiver
    LaunchedEffect(Unit) {
        val handle = navController.currentBackStackEntry?.savedStateHandle ?: return@LaunchedEffect
        handle.getStateFlow<List<Int>?>("selected_days_result", null).collect { result ->
            if (result != null) {
                alarmViewModel.setRepeatDays(result)
                handle["selected_days_result"] = null
            }
        }
    }
    LaunchedEffect(Unit) {
        val handle = navController.currentBackStackEntry?.savedStateHandle ?: return@LaunchedEffect

        handle.getStateFlow<List<String>?>("selected_challenges_result", null).collect { result ->
            if (result != null) {
                alarmViewModel.setSelectedChallenges(result)
                handle["selected_challenges_result"] = null
            }
        }
    }

    // Handle Ringtone Selection Result
    LaunchedEffect(Unit) {
        val handle = navController.currentBackStackEntry?.savedStateHandle ?: return@LaunchedEffect

        // Watch for the NAME specifically
        handle.getStateFlow<String?>("selected_ringtone_name", null).collect { name ->
            if (name != null) {
                // Get the URI stored alongside it
                val uri = handle.get<String>("selected_ringtone_uri") ?: ""

                // DEBUG LOG: Ensure Name is a Name ("Classic") and URI is a URI ("content://...")
                println("SnorlyDebug: Setting Ringtone -> Name: $name | Uri: $uri")

                // Update ViewModel: Function signature is setRingtone(name, uri)
                alarmViewModel.setRingtone(name = name, uri = uri)

                // Clear state to prevent loop
                handle["selected_ringtone_name"] = null
                handle["selected_ringtone_uri"] = null
            }
        }
    }


    // Close after save
    LaunchedEffect(state.saved) {
        if (state.saved) onClose()
    }

    val dismissChallengeText =
        when (state.selectedChallenges.size) {
            0 -> "Off"
            1 -> state.selectedChallenges.first()
            else -> "${state.selectedChallenges.first()} +${state.selectedChallenges.size - 1}"
        }


    Scaffold(
        containerColor = bg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (alarmId == null) "New Alarm" else "Edit Alarm",
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, divider, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bg,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bg)
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = { alarmViewModel.save() },
                    enabled = !state.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryBtn,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        if (alarmId == null) "Create Alarm" else "Save Changes",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
        ) {
            Spacer(Modifier.height(10.dp))

            TimePickerWheel(
                hour = state.hour,
                minute = state.minute,
                onHourChange = alarmViewModel::setHour,
                onMinuteChange = alarmViewModel::setMinute
            )

            Spacer(Modifier.height(22.dp))

            Text("Alarm label", color = muted)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.label,
                onValueChange = alarmViewModel::setLabel,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = card,
                    unfocusedContainerColor = card,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = divider,
                    unfocusedBorderColor = divider,
                    cursorColor = accent
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(18.dp))

            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                SettingRow(
                    title = "Repeat",
                    value = alarmViewModel.formatDays(state.repeatDays),
                    onClick = {
                        val arg = state.repeatDays.joinToString(",")
                        navController.navigate("alarm_repeat/$arg")
                    }
                )

                SettingRow(
                    title = "Ringtone",
                    value = state.ringtone,
                    onClick = onNavigateToRingtone
                )

                SettingRow(
                    title = "Vibration",
                    value = state.vibration,
                    onClick = onNavigateToVibration
                )

                SettingRow(
                    title = "Dismiss Challenge",
                    subtitle = "Complete a task to turn off",
                    value = dismissChallengeText,
                    onClick = onNavigateToChallenge
                )
            }




            Spacer(Modifier.height(10.dp))

            ToggleRow(
                title = "Enable Snooze",
                subtitle = "allow snoozing alarm",
                checked = state.enableSnooze,
                onCheckedChange = alarmViewModel::setEnableSnooze,
                showDivider = false
            )

            SnoozeSlider(
                visible = state.enableSnooze,
                value = state.snoozeMinutes,
                onValueChange = alarmViewModel::setSnoozeMinutes
            )

            Spacer(Modifier.height(90.dp))
        }
    }
}
