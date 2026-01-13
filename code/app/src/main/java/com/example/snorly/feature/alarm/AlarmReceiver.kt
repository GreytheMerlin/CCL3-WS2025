package com.example.snorly.feature.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra("alarm_id", -1L)

        val svc = Intent(context, AlarmRingingService::class.java).apply {
            putExtra("alarm_id", alarmId)
        }
        ContextCompat.startForegroundService(context, svc)
    }
}
