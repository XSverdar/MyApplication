package com.example.myapplication.presentation

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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.myapplication.R
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.SensorData
import com.example.myapplication.presentation.theme.MyApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var heartRate: Sensor? = null
    private lateinit var database: AppDatabase

    val axValue = mutableStateOf("")
    val ayValue = mutableStateOf("")
    val azValue = mutableStateOf("")
    val gxValue = mutableStateOf("")
    val gyValue = mutableStateOf("")
    val gzValue = mutableStateOf("")
    val hrValue = mutableStateOf("")

    private val permissions = arrayOf(
        android.Manifest.permission.BODY_SENSORS,
        android.Manifest.permission.HIGH_SAMPLING_RATE_SENSORS
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            WearApp("x1", "y1", "z1", "x2", "y2", "z2", "hr")
        }

        database = AppDatabase.getInstance(this)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        if (permissions.any { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) {
            ActivityCompat.requestPermissions(this, permissions, 1)
        } else {
            registerSensors()
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

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_LINEAR_ACCELERATION -> handleAcceleration(it.values)
                Sensor.TYPE_GYROSCOPE -> handleGyroscope(it.values)
                Sensor.TYPE_HEART_RATE -> handleHeartRate(it.values[0])
            }
        }

        setContent {
            WearApp(
                axValue.value, ayValue.value, azValue.value,
                gxValue.value, gyValue.value, gzValue.value,
                hrValue.value
            )
        }
    }

    private fun handleAcceleration(values: FloatArray) {
        val (ax, ay, az) = values
        axValue.value = "%.2f".format(ax)
        ayValue.value = "%.2f".format(ay)
        azValue.value = "%.2f".format(az)

        /*lifecycleScope.launch(Dispatchers.IO) {
            database.sensorDataDao().insertSensorData(SensorData(type = "Accelerometer", x = ax, y = ay, z = az))
            logDatabaseData()
        }*/
    }

    private fun handleGyroscope(values: FloatArray) {
        val (gx, gy, gz) = values
        gxValue.value = "%.2f".format(gx)
        gyValue.value = "%.2f".format(gy)
        gzValue.value = "%.2f".format(gz)

        /*lifecycleScope.launch(Dispatchers.IO) {
            database.sensorDataDao().insertSensorData(SensorData(type = "Gyroscope", x = gx, y = gy, z = gz))
            logDatabaseData()
        }*/
    }

    private fun handleHeartRate(hr: Float) {
        hrValue.value = "%.0f".format(hr)

        /*lifecycleScope.launch(Dispatchers.IO) {
            database.sensorDataDao().insertSensorData(SensorData(type = "HeartRate", x = hr, y = null, z = null))
            logDatabaseData()
        }*/
    }

    private suspend fun logDatabaseData() {
        val data = database.sensorDataDao().getAllSensorData()
        withContext(Dispatchers.Main) {
            Log.d("SensorData", "Fetched data: $data")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onResume() {
        super.onResume()
        registerSensors()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }
}

@Composable
fun WearApp(x1: String, y1: String, z1: String, x2: String, y2: String, z2: String, hr: String) {
    MyApplicationTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            TimeText()
            Greeting(x1, y1, z1, x2, y2, z2, hr)
        }
    }
}

@Composable
fun Greeting(x1: String, y1: String, z1: String, x2: String, y2: String, z2: String, hr: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = stringResource(R.string.hello_world, x1, y1, z1, x2, y2, z2, hr)
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp("x1", "y1", "z1", "x2", "y2", "z2", "hr")
}
