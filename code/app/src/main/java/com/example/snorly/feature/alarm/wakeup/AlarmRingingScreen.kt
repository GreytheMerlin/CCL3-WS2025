package com.example.snorly.feature.alarm.wakeup

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.snorly.core.database.AppDatabase
import com.example.snorly.core.database.entities.AlarmEntity

@Composable
fun AlarmRingingScreen(
    alarmId: Long,
    onStopRinging: () -> Unit,
    onSnooze: (snoozeMinutes: Int) -> Unit,
    onOpenChallenge: () -> Unit,
) {
    val context = LocalContext.current

    var alarm by remember { mutableStateOf<AlarmEntity?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(alarmId) {
        loading = true
        val dao = AppDatabase.getDatabase(context.applicationContext).alarmDao()
        alarm = try {
            dao.getById(alarmId)
        } catch (_: Exception) {
            null
        }
        loading = false
    }

    if (loading) {
        Surface(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        return
    }

    val a = alarm
    if (a == null) {
        RingingScreen(
            title = "Alarm",
            subtitle = "Not found",
            showSnooze = false,
            snoozeMinutes = 0,
            hasChallenge = false,
            onDismiss = onStopRinging,
            onDismissWithChallenge = {},
            onSnooze = {}
        )
        return
    }

    val hasChallenge = a.challenge.isNotEmpty()
    val snoozeMinutes = a.snoozeMinutes

    RingingScreen(
        title = "Wake up",
        subtitle = if (hasChallenge) "Complete the challenge to dismiss" else "",
        showSnooze = snoozeMinutes > 0,
        snoozeMinutes = snoozeMinutes,
        hasChallenge = hasChallenge,
        onDismiss = onStopRinging,
        onDismissWithChallenge = onOpenChallenge,
        onSnooze = { onSnooze(snoozeMinutes) }
    )
}

@Composable
private fun RingingScreen(
    title: String,
    subtitle: String,
    showSnooze: Boolean,
    snoozeMinutes: Int,
    hasChallenge: Boolean,
    onDismiss: () -> Unit,
    onDismissWithChallenge: () -> Unit,
    onSnooze: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.headlineLarge)
            if (subtitle.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(24.dp))

            if (showSnooze) {
                Button(
                    onClick = onSnooze,
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) { Text("Snooze (${snoozeMinutes} min)") }

                Spacer(Modifier.height(12.dp))
            }

            Button(
                onClick = { if (hasChallenge) onDismissWithChallenge() else onDismiss() },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text(if (hasChallenge) "Dismiss with Challenge" else "Dismiss")
            }
        }
    }
}
