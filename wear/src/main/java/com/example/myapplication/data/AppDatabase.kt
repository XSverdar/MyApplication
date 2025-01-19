package com.example.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SensorData::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sensorDataDao(): SensorDataDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sensor_data_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
