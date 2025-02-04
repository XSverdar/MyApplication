package com.example.myapplication.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.SensorData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var heartRate: Sensor? = null
    private lateinit var database: AppDatabase

    private var isRecording by mutableStateOf(false)

    val axValue = mutableStateOf("")
    val ayValue = mutableStateOf("")
    val azValue = mutableStateOf("")
    val gxValue = mutableStateOf("")
    val gyValue = mutableStateOf("")
    val gzValue = mutableStateOf("")
    val hrValue = mutableStateOf("")

    private val permissions = arrayOf(
        Manifest.permission.BODY_SENSORS,
        Manifest.permission.HIGH_SAMPLING_RATE_SENSORS
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { WearApp(::startRecording, ::stopRecording, isRecording, axValue.value, ayValue.value, azValue.value, gxValue.value, gyValue.value, gzValue.value, hrValue.value) }
        database = AppDatabase.getInstance(this)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        if (permissions.any { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) {
            ActivityCompat.requestPermissions(this, permissions, 1)
        }
    }

    private fun registerSensors() {
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        heartRate = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST) }
        gyroscope?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST) }
        heartRate?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST) }
    }

    private fun unregisterSensors() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!isRecording) return
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_LINEAR_ACCELERATION -> handleAcceleration(it.values)
                Sensor.TYPE_GYROSCOPE -> handleGyroscope(it.values)
                Sensor.TYPE_HEART_RATE -> handleHeartRate(it.values[0])
            }
        }
    }

    private fun handleAcceleration(values: FloatArray) {
        axValue.value = "%.2f".format(values[0])
        ayValue.value = "%.2f".format(values[1])
        azValue.value = "%.2f".format(values[2])
        storeSensorData("Accelerometer", values[0], values[1], values[2])
    }

    private fun handleGyroscope(values: FloatArray) {
        gxValue.value = "%.2f".format(values[0])
        gyValue.value = "%.2f".format(values[1])
        gzValue.value = "%.2f".format(values[2])
        storeSensorData("Gyroscope", values[0], values[1], values[2])
    }

    private fun handleHeartRate(hr: Float) {
        hrValue.value = "%.0f".format(hr)
        storeSensorData("HeartRate", hr, null, null)
    }

    private fun storeSensorData(type: String, x: Float?, y: Float?, z: Float?) {
        lifecycleScope.launch(Dispatchers.IO) {
            val sensorData = SensorData(
                type = type,
                x = x ?: 0f, // Default to 0 if null
                y = y ?: 0f, // Default to 0 if null
                z = z ?: 0f  // Default to 0 if null
            )
            database.sensorDataDao().insertSensorData(sensorData)
        }
    }

    private fun startRecording() {
        isRecording = true
        registerSensors()
    }

    private fun stopRecording() {
        isRecording = false
        unregisterSensors()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

@Composable
fun WearApp(startRecording: () -> Unit, stopRecording: () -> Unit, isRecording: Boolean, x1: String, y1: String, z1: String, x2: String, y2: String, z2: String, hr: String) {
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!isRecording) {
            Button(onClick = startRecording) { Text("START") }
        } else {
            Text("Accelerometer: $x1, $y1, $z1\nGyroscope: $x2, $y2, $z2\nHeart Rate: $hr", textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = stopRecording) { Text("STOP") }
        }
    }
}
