package com.example.snorly.feature.alarm.wakeup

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme

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
                AlarmRingingScreen(
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
                        val i = Intent(this, ChallengeHostActivity::class.java).apply {
                            putExtra(ChallengeHostActivity.EXTRA_ALARM_ID, alarmId)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        }
                        startActivity(i)
                        finishAndRemoveTask()
                    }
                )
            }
        }
    }
}
