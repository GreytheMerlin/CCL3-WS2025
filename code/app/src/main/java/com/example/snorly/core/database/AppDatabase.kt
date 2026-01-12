package com.example.snorly.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.snorly.core.database.entities.AlarmEntity

@Database(
    entities = [AlarmEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao

    companion object{

        @Volatile
        private var Instance: AppDatabase ?= null

        fun getDatabase(context: Context): AppDatabase{
            return Instance ?: synchronized(this){
                val instance = Room.databaseBuilder(context, AppDatabase:: class.java, "Snorly").build()

                Instance = instance

                return instance
            }
        }

    }
}