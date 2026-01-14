package com.example.snorly.feature.alarm.wakeup

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.snorly.core.database.AppDatabase
import com.example.snorly.core.database.entities.AlarmEntity
import kotlin.random.Random


class AlarmRingingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        val alarmId = intent.getLongExtra("alarm_id", -1L)

        setContent {
            MaterialTheme {
                RingingRoute(
                    alarmId = alarmId,
                    onStopRinging = {
                        stopService(Intent(this, AlarmRingingService::class.java))
                        finishAndRemoveTask()
                    },
                    onSnooze = { snoozeMinutes ->
                        val triggerAt = System.currentTimeMillis() + snoozeMinutes * 60_000L
                        AlarmScheduler(applicationContext).schedule(alarmId, triggerAt)

                        stopService(Intent(this, AlarmRingingService::class.java))
                        finishAndRemoveTask()
                    },
                    onOpenChallenge = {
                        // IMPORTANT: do NOT stop the service here.
                        val i = Intent(this, ChallengeHostActivity::class.java).apply {
                            putExtra(ChallengeHostActivity.EXTRA_ALARM_ID, alarmId)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        }
                        startActivity(i)
                        // Optional: close ringing UI so user can't dismiss without solving
                        finishAndRemoveTask()
                    }
                )
            }
        }
    }
}

@Composable
private fun RingingRoute(
    alarmId: Long,
    onStopRinging: () -> Unit,
    onSnooze: (snoozeMinutes: Int) -> Unit,
    onOpenChallenge: () -> Unit
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

/**
 * Simple built-in challenge (math) as a placeholder.
 * You can replace this with your own DB-driven challenge type later.
 */
@Composable
private fun ChallengeCard(onSolved: () -> Unit) {
    val a = remember { Random.nextInt(10, 30) }
    val b = remember { Random.nextInt(10, 30) }
    val answer = a + b

    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Solve to dismiss:", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text("$a + $b = ?", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = input,
                onValueChange = {
                    input = it.filter { ch -> ch.isDigit() }
                    error = false
                },
                label = { Text("Answer") },
                isError = error,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    val v = input.toIntOrNull()
                    if (v == answer) onSolved() else error = true
                },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Confirm")
            }

            if (error) {
                Spacer(Modifier.height(8.dp))
                Text("Wrong answer. Try again.", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
