package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sensor_data")
data class SensorData(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ax: Float?,
    val ay: Float?,
    val az: Float?,
    val gx: Float?,
    val gy: Float?,
    val gz: Float?,
    val hr: Float?,
    val timestamp: Long = System.currentTimeMillis()
)
