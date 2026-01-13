
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
            TimePickerMock(
                hour = hour,
                minute = minute,
                onHourChange = { hour = it },
                onMinuteChange = { minute = it },
                highlightColor = card2
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
private fun TimePickerMock(
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    highlightColor: Color
) {
    val hours = (0..23).toList()
    val minutes = (0..59).toList()

    // Display-only mimic with 5 visible rows; center row highlighted
    val visible = 5
    val center = 2

    fun wrapIndex(listSize: Int, index: Int): Int {
        var i = index % listSize
        if (i < 0) i += listSize
        return i
    }

    val hourIndex = hours.indexOf(hour).coerceAtLeast(0)
    val minuteIndex = minutes.indexOf(minute).coerceAtLeast(0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
    ) {
        // Highlight capsule
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(highlightColor.copy(alpha = 0.65f))
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 36.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TimeColumn(
                values = (0 until visible).map { offset ->
                    hours[wrapIndex(hours.size, hourIndex + offset - center)]
                },
                selected = hour,
                onIncrement = { onHourChange(hours[wrapIndex(hours.size, hourIndex + 1)]) },
                onDecrement = { onHourChange(hours[wrapIndex(hours.size, hourIndex - 1)]) }
            )

            Text(":", color = Color.White, style = MaterialTheme.typography.headlineLarge)

            TimeColumn(
                values = (0 until visible).map { offset ->
                    minutes[wrapIndex(minutes.size, minuteIndex + offset - center)]
                },
                selected = minute,
                onIncrement = { onMinuteChange(minutes[wrapIndex(minutes.size, minuteIndex + 1)]) },
                onDecrement = { onMinuteChange(minutes[wrapIndex(minutes.size, minuteIndex - 1)]) }
            )
        }
    }
}

@Composable
private fun TimeColumn(
    values: List<Int>,
    selected: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Tap zones: top half decrements, bottom half increments (simple)
        Box(
            modifier = Modifier
                .width(92.dp)
                .height(160.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                values.forEach { v ->
                    val isSel = v == selected
                    Text(
                        text = v.toString().padStart(2, '0'),
                        color = if (isSel) Color.White else Color(0xFF5A5A5A),
                        style = if (isSel) MaterialTheme.typography.displaySmall else MaterialTheme.typography.displaySmall,
                        fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }

            // Invisible click layers
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        onDecrement()
                    }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        onIncrement()
                    }
            )

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
