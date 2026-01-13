package com.example.snorly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.snorly.core.common.nav.SnorlyApp
import com.example.snorly.core.database.AppDatabase
import com.example.snorly.core.database.entities.AlarmEntity
import com.example.snorly.core.ui.theme.SnorlyTheme
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

       val db = AppDatabase.getDatabase(this)
        val alarmDao = db.alarmDao()

      lifecycleScope.launch {
            alarmDao.addAlarm(
                AlarmEntity(
                    time = "08:00",
                    challenge = "Test",
                    ringtone = "Default",
                    vibration = "On",
                    days = listOf(1, 1,1,1,1,1,1),
                    isActive = false

                    )
            )
        }

            enableEdgeToEdge()
            setContent {
                SnorlyTheme {
                    SnorlyApp()
                }
            }

    }
}



