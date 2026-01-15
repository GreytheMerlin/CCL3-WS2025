package com.example.snorly.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.snorly.core.database.entities.AlarmEntity
import com.example.snorly.core.database.entities.UserProfileEntity

@Database(
    entities = [AlarmEntity::class, UserProfileEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun UserProfileDao(): UserProfileDao

    companion object{

        @Volatile
        private var Instance: AppDatabase ?= null

        fun getDatabase(context: Context): AppDatabase{
            return Instance ?: synchronized(this){
                val instance = Room.databaseBuilder(context, AppDatabase:: class.java, "Snorly.db").fallbackToDestructiveMigration().build()

                Instance = instance

                return instance
            }
        }

    }
}