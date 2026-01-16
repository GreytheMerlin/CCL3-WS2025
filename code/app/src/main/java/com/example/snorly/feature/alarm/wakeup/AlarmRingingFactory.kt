package com.example.snorly.feature.alarm.wakeup

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.snorly.core.database.AppDatabase

class AlarmRingingViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val dao = AppDatabase.getDatabase(context.applicationContext).alarmDao()
        @Suppress("UNCHECKED_CAST")
        return AlarmRingingViewModel(dao) as T
    }
}