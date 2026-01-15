@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.snorly.feature.alarm.create

import android.util.Log
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    selectedChallenges: List<String> = emptyList()
) {
    // ViewModel state (needed for saved + edit prefill sync)
    val uiState by alarmViewModel.uiState.collectAsState()

    val dismissChallengeText =
        if (selectedChallenges.isEmpty()) "Off"
        else selectedChallenges.joinToString(", ")

    // Local UI state (you currently use remember-vars; we keep that style)
    var hour by remember { mutableIntStateOf(7) }
    var minute by remember { mutableIntStateOf(30) }

    var label by remember { mutableStateOf("Alarm Name") }
    var ringtone by remember { mutableStateOf("Repeater") }
    var vibration by remember { mutableStateOf("Zig Zag") }
    var repeatDays by remember { mutableStateOf(List(7) { 0 }) }

    var dynamicWake by remember { mutableStateOf(false) }
    var wakeUpChecker by remember { mutableStateOf(false) }

    var enableSnooze by remember { mutableStateOf(true) }
    var snoozeMinutes by remember { mutableIntStateOf(5) }

    val bg = Color(0xFF000000)
    val card = Color(0xFF1B1B1B)
    val muted = Color(0xFFB3B3B3)
    val divider = Color(0xFF2A2A2A)
    val accent = Color(0xFFFFE7A3)
    val primaryBtn = Color(0xFF1677FF)

    // Receive selected days result (no observeForever leak)
    val handle = navController.currentBackStackEntry?.savedStateHandle
    LaunchedEffect(handle) {
        if (handle == null) return@LaunchedEffect

        handle.getStateFlow<List<Int>?>("selected_days_result", null)
            .collect { result ->
                if (result != null) {
                    repeatDays = result
                    handle["selected_days_result"] = null
                }
            }
    }


    // If editing, load the alarm into the VM once
    LaunchedEffect(alarmId) {
        if (alarmId != null) {
            alarmViewModel.loadForEdit(alarmId)
        }
    }


    LaunchedEffect(uiState.id) {
        if (uiState.id != null) {
            hour = uiState.hour
            minute = uiState.minute

            label = uiState.label
            ringtone = uiState.ringtone
            vibration = uiState.vibration
            repeatDays = uiState.repeatDays

            dynamicWake = uiState.dynamicWake
            wakeUpChecker = uiState.wakeUpChecker

            enableSnooze = uiState.enableSnooze
            snoozeMinutes = uiState.snoozeMinutes
            // selectedChallenges comes from navigation args in your current setup
            Log.d("states", "label=$label, hour=$hour, minute=$minute, repeat=$repeatDays, vibrate=$vibration")

        }
    }

    // Close when saved
    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onClose()
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
                    onClick = {
                        // Push local values into VM then save().
                        // VM decides create vs update based on uiState.id (set by loadForEdit()).
                        alarmViewModel.setHour(hour)
                        alarmViewModel.setMinute(minute)
                        alarmViewModel.setLabel(label)
                        alarmViewModel.setRingtone(ringtone)
                        alarmViewModel.setVibration(vibration)
                        alarmViewModel.setRepeatDays(repeatDays)
                        alarmViewModel.setDynamicWake(dynamicWake)
                        alarmViewModel.setWakeUpChecker(wakeUpChecker)
                        alarmViewModel.setEnableSnooze(enableSnooze)
                        alarmViewModel.setSnoozeMinutes(snoozeMinutes)
                        alarmViewModel.setSelectedChallenges(selectedChallenges)

                        alarmViewModel.save()
                    },
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
                hour = hour,
                minute = minute,
                onHourChange = { hour = it },
                onMinuteChange = { minute = it }
            )

            Spacer(Modifier.height(22.dp))

            Text("Alarm label", color = muted)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
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
                    value = alarmViewModel.formatDays(repeatDays),
                    onClick = {
                        val arg = repeatDays.joinToString(",")
                        navController.navigate("alarm_repeat/$arg")
                    }
                )
                SettingRow(
                    title = "Ringtone",
                    value = ringtone,
                    onClick = onNavigateToRingtone
                )
                SettingRow(
                    title = "Vibration",
                    value = vibration,
                    onClick = onNavigateToVibration
                )
                SettingRow(
                    title = "Dismiss Challenge",
                    subtitle = "Complete a task to turn off alarm",
                    value = dismissChallengeText,
                    onClick = onNavigateToChallenge
                )
            }

            Spacer(Modifier.height(10.dp))

            ToggleRow(
                title = "Dynamic Wake",
                subtitle = "Wake you at optimal time based on\nsleep cycles. Set latest wake time.",
                checked = dynamicWake,
                onCheckedChange = { dynamicWake = it },
            )
            ToggleRow(
                title = "Wake Up Checker",
                subtitle = "You will receive a notification. If you do not accept this,\nthe alarm will be triggered.",
                checked = wakeUpChecker,
                onCheckedChange = { wakeUpChecker = it },
            )

            Spacer(Modifier.height(10.dp))

            ToggleRow(
                title = "Enable Snooze",
                subtitle = "allow snoozing alarm",
                checked = enableSnooze,
                onCheckedChange = { enableSnooze = it },
                showDivider = false
            )

            SnoozeSlider(
                visible = enableSnooze,
                value = snoozeMinutes,
                onValueChange = { snoozeMinutes = it }
            )

            Spacer(Modifier.height(90.dp))
        }
    }
}
