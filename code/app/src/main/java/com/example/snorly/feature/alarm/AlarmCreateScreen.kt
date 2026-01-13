@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.snorly.feature.alarm

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
import com.example.snorly.feature.alarm.components.TimePickerWheel


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
    var snoozeMinutes by remember { mutableStateOf("9") }

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
                dividerColor = divider
            )
            SettingRow(
                title = "Vibration Pattern",
                value = vibration,
                onClick = { /* open */ },
                dividerColor = divider
            )
            SettingRow(
                title = "Dismiss Challenge",
                subtitle = "Fun games to wake you up",
                value = dismissChallenge,
                onClick = { /* open */ },
                dividerColor = divider
            )

            Spacer(Modifier.height(10.dp))

            ToggleRow(
                title = "Dynamic Wake",
                subtitle = "Wake you at optimal time based on\nsleep cycles. Set latest wake time.",
                checked = dynamicWake,
                onCheckedChange = { dynamicWake = it },
                dividerColor = divider,
                accent = accent
            )
            ToggleRow(
                title = "Wake Up Checker",
                subtitle = "You will receive a notification. If you do not accept this,\nthe alarm will be triggered.",
                checked = wakeUpChecker,
                onCheckedChange = { wakeUpChecker = it },
                dividerColor = divider,
                accent = accent
            )

            Spacer(Modifier.height(10.dp))

            ToggleRow(
                title = "Enable Snooze",
                subtitle = "Allow snoozing alarm",
                checked = enableSnooze,
                onCheckedChange = { enableSnooze = it },
                dividerColor = divider,
                accent = accent
            )

            Spacer(Modifier.height(10.dp))

            Text("Snooze Duration (minutes)", color = muted)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = snoozeMinutes,
                onValueChange = { snoozeMinutes = it.filter { ch -> ch.isDigit() }.take(3) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = enableSnooze,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = card,
                    unfocusedContainerColor = card,
                    disabledContainerColor = card,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    disabledTextColor = Color(0xFF888888),
                    focusedBorderColor = divider,
                    unfocusedBorderColor = divider,
                    disabledBorderColor = divider,
                    cursorColor = accent
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(90.dp)) // room for bottom button
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    value: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    dividerColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, style = MaterialTheme.typography.titleLarge)
                if (subtitle != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(subtitle, color = Color(0xFF9B9B9B), style = MaterialTheme.typography.bodyMedium)
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(value, color = Color(0xFF9B9B9B), style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF9B9B9B)
                )

            }
        }
        Spacer(Modifier.height(14.dp))
        HorizontalDivider(color = dividerColor)
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    dividerColor: Color,
    accent: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(6.dp))
                Text(subtitle, color = Color(0xFF9B9B9B), style = MaterialTheme.typography.bodyMedium)
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = accent,
                    uncheckedThumbColor = accent,
                    checkedTrackColor = Color(0xFF2E3A4A),
                    uncheckedTrackColor = Color(0xFF2A2A2A),
                    checkedBorderColor = Color.Transparent,
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }
        Spacer(Modifier.height(14.dp))
        HorizontalDivider(color = dividerColor)
    }
}
