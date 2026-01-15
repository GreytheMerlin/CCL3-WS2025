package com.example.snorly.feature.alarm.wakeup


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.LocalContext
import com.example.snorly.core.database.AppDatabase

import com.example.snorly.feature.challenges.math.MathChallengeRoute
import com.example.snorly.feature.challenges.memory.MemoryMatchRoute
import com.example.snorly.feature.challenges.qr.QrChallengeRoute
import com.example.snorly.feature.challenges.shake.ShakeChallengeRoute
import com.example.snorly.feature.challenges.steps.StepChallengeRoute

class ChallengeHostActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)

        setContent {
            MaterialTheme {
                Surface {
                    ChallengeHostRoute(
                        alarmId = alarmId,
                        onAllSolved = {
                            stopService(Intent(this, AlarmRingingService::class.java))
                            finishAndRemoveTask()
                        }
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_ALARM_ID = "alarm_id"
    }
}

@Composable
private fun ChallengeHostRoute(
    alarmId: Long,
    onAllSolved: () -> Unit
) {
    val context = LocalContext.current
    var challenges by remember { mutableStateOf<List<String>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var index by remember { mutableStateOf(0) }

    LaunchedEffect(alarmId) {
        loading = true
        val dao = AppDatabase.getDatabase(context.applicationContext).alarmDao()
        challenges = try {
            dao.getById(alarmId).challenge
        } catch (_: Exception) {
            emptyList()
        }
        loading = false
    }

    if (loading) return

    // No challenges => dismiss directly
    if (challenges.isEmpty()) {
        onAllSolved()
        return
    }

    // Finished all
    if (index >= challenges.size) {
        onAllSolved()
        return
    }

    val current = challenges[index].uppercase()
    Log.d("ChallengeHost", "Current challenge = $current")

    val goNext = {
        index += 1
    }


    when (current) {
        "MATH PROBLEM" ->
            MathChallengeRoute(onSolved = goNext)

        "SHAKE PHONE" ->
            ShakeChallengeRoute(requiredShakes = 20, onSolved = goNext)

        "STEP COUNTER" ->
            StepChallengeRoute(requiredSteps = 10, onSolved = goNext)
        "QR CODE" ->
            QrChallengeRoute (onSolved = goNext)
        "MEMORY GAME" ->
            MemoryMatchRoute(onCompleted = goNext)
        else -> goNext()
    }


}
