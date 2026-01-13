package com.example.snorly.feature.alarm

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
                        // schedule the same alarm id again after snooze
                        val triggerAt = System.currentTimeMillis() + snoozeMinutes * 60_000L
                        AlarmScheduler(applicationContext).schedule(alarmId, triggerAt)

                        stopService(Intent(this, AlarmRingingService::class.java))
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
    onSnooze: (snoozeMinutes: Int) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    var alarm by remember { mutableStateOf<AlarmEntity?>(null) }
    var loading by remember { mutableStateOf(true) }

    // Load from Room
    LaunchedEffect(alarmId) {
        loading = true
        val dao = AppDatabase.getDatabase(context.applicationContext).alarmDao()
        alarm = dao.getById(alarmId) // if your DAO returns AlarmEntity? it's fine
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
        // Alarm not found -> let user exit safely
        RingingScreen(
            title = "Alarm",
            subtitle = "Not found",
            showSnooze = false,
            snoozeMinutes = 0,
            hasChallenge = false,
            onDismiss = onStopRinging,
            onSnooze = { }
        )
        return
    }

    // ---- customize these two lines to match YOUR DB values ----

    // "challenge in db" -> consider it enabled if not blank and not "None"
    val hasChallenge = a.challenge.isNotBlank() && a.challenge.lowercase() != "none"

    // Snooze time from DB (CHANGE FIELD NAME if yours is different)
    // Example expected field: a.snoozeMinutes
    val snoozeMinutes = try {
        // Replace this with your real field, e.g.: a.snoozeMinutes
        val field = AlarmEntity::class.java.getDeclaredField("snoozeMinutes")
        field.isAccessible = true
        field.getInt(a).coerceAtLeast(0)
    } catch (_: Exception) {
        0 // if field doesn't exist yet
    }

    RingingScreen(
        title = "Wake up",
        subtitle = if (hasChallenge) "Complete the challenge to dismiss" else "",
        showSnooze = snoozeMinutes > 0,
        snoozeMinutes = snoozeMinutes,
        hasChallenge = hasChallenge,
        onDismiss = onStopRinging,
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
    onSnooze: () -> Unit
) {
    var showChallenge by remember { mutableStateOf(false) }

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
                ) {
                    Text("Snooze (${snoozeMinutes} min)")
                }
                Spacer(Modifier.height(12.dp))
            }

            if (!hasChallenge) {
                // Normal dismiss
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Dismiss")
                }
            } else {
                // Challenge flow: no normal dismiss
                if (!showChallenge) {
                    Button(
                        onClick = { showChallenge = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("Dismiss with Challenge")
                    }
                } else {
                    ChallengeCard(
                        onSolved = onDismiss
                    )
                }
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
