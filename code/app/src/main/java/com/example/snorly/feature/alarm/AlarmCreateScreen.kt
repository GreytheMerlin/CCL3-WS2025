@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.snorly.feature.alarm

import android.R.attr.checked
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.snorly.feature.alarm.components.SettingRow
import com.example.snorly.feature.alarm.components.SnoozeSlider
import com.example.snorly.feature.alarm.components.TimePickerWheel
import com.example.snorly.feature.alarm.components.ToggleRow


@Composable
fun AlarmCreateScreen(
    onClose: () -> Unit = {},
    onCreateAlarm: () -> Unit = {}
) {
    var hour by remember { mutableIntStateOf(7) }
    var minute by remember { mutableIntStateOf(30) }

    var label by remember { mutableStateOf("Alarm Name") }
    var ringtone by remember { mutableStateOf("Repeater") }
    var vibration by remember { mutableStateOf("Zig Zag") }
    var dismissChallenge by remember { mutableStateOf("Zig Zag") }

    var dynamicWake by remember { mutableStateOf(false) }
    var wakeUpChecker by remember { mutableStateOf(false) }

    var enableSnooze by remember { mutableStateOf(true) }
    var snoozeMinutes by remember { mutableIntStateOf(5) }

    val bg = Color(0xFF000000)
    val card = Color(0xFF1B1B1B)
    val card2 = Color(0xFF222222)
    val muted = Color(0xFFB3B3B3)
    val divider = Color(0xFF2A2A2A)
    val accent = Color(0xFFFFE7A3)
    val primaryBtn = Color(0xFF1677FF)

    Scaffold(
        containerColor = bg,
        topBar = {
            TopAppBar(
                title = { Text("New Alarm", fontWeight = FontWeight.Medium) },
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
                    onClick = onCreateAlarm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryBtn,
                        contentColor = Color.White
                    )
                ) {
                    Text("Create Alarm", fontWeight = FontWeight.SemiBold)
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

            // Time picker-ish (two number columns with a capsule highlight)
            TimePickerWheel(
                hour = hour,
                minute = minute,
                onHourChange = { hour = it },
                onMinuteChange = { minute = it }
            )

            Spacer(Modifier.height(22.dp))

            // Alarm label
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

            // Rows
            SettingRow(
                title = "Alarm Ringtones",
                value = ringtone,
                onClick = { /* open */ },
            )
            SettingRow(
                title = "Vibration Pattern",
                value = vibration,
                onClick = { /* open */ },
            )
            SettingRow(
                title = "Dismiss Challenge",
                subtitle = "Fun games to wake you up",
                value = dismissChallenge,
                onClick = { /* open */ },
            )

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

            // Animated Slider
            SnoozeSlider(
                visible = enableSnooze,
                value = snoozeMinutes,
                onValueChange = { snoozeMinutes = it }
            )

            Spacer(Modifier.height(90.dp)) // room for bottom button
        }
    }
}