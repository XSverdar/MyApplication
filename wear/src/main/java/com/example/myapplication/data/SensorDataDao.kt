package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SensorDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSensorData(sensorData: SensorData)

    @Query("SELECT * FROM sensor_data ORDER BY timestamp DESC")
    suspend fun getAllSensorData(): List<SensorData>
}
