/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

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
import kotlinx.coroutines.launch


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

    private var prevAx: Float? = null
    private var prevAy: Float? = null
    private var prevAz: Float? = null
    private var prevGx: Float? = null
    private var prevGy: Float? = null
    private var prevGz: Float? = null
    private var prevHr: Float? = null

    val permissions = arrayOf(
        android.Manifest.permission.BODY_SENSORS,
        android.Manifest.permission.HIGH_SAMPLING_RATE_SENSORS
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            WearApp("x1","y1","z1","x2","y2","z2","hr")
        }

        database = AppDatabase.getInstance(this)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        getSensors()
    }

    fun getSensors() {
        if (permissions.any { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) {
            ActivityCompat.requestPermissions(this, permissions, 1)
        }
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
            gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
            heartRate = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

            accelerometer?.also {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
            }
            gyroscope?.also {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
            }
        }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_LINEAR_ACCELERATION -> {
                val ax = event.values[0]
                val ay = event.values[1]
                val az = event.values[2]

                // Only update if there's a change in the values
                if (ax != prevAx || ay != prevAy || az != prevAz) {
                    axValue.value = String.format("%.2f", ax)
                    ayValue.value = String.format("%.2f", ay)
                    azValue.value = String.format("%.2f", az)

                    // Insert the new data into the database
                    lifecycleScope.launch {
                        val sensorData = SensorData(ax = ax, ay = ay, az = az, gx = null, gy = null, gz = null, hr = null)
                        database.sensorDataDao().insertSensorData(sensorData)

                        // After inserting, fetch the data to check
                        fetchSensorData()  // Fetch and log the stored data
                    }

                    // Update previous values
                    prevAx = ax
                    prevAy = ay
                    prevAz = az
                }
            }
            Sensor.TYPE_GYROSCOPE -> {
                val gx = event.values[0]
                val gy = event.values[1]
                val gz = event.values[2]

                // Only update if there's a change in the values
                if (gx != prevGx || gy != prevGy || gz != prevGz) {
                    gxValue.value = String.format("%.2f", gx)
                    gyValue.value = String.format("%.2f", gy)
                    gzValue.value = String.format("%.2f", gz)

                    // Insert the new data into the database
                    lifecycleScope.launch {
                        val sensorData = SensorData(ax = null, ay = null, az = null, gx = gx, gy = gy, gz = gz, hr = null)
                        database.sensorDataDao().insertSensorData(sensorData)

                        // After inserting, fetch the data to check
                        fetchSensorData()  // Fetch and log the stored data
                    }

                    // Update previous values
                    prevGx = gx
                    prevGy = gy
                    prevGz = gz
                }
            }
            Sensor.TYPE_HEART_RATE -> {
                val hr = event.values[0]

                // Only update if there's a change in the heart rate
                if (hr != prevHr) {
                    hrValue.value = String.format("%.0f", hr)

                    // Insert the new data into the database
                    lifecycleScope.launch {
                        val sensorData = SensorData(ax = null, ay = null, az = null, gx = null, gy = null, gz = null, hr = hr)
                        database.sensorDataDao().insertSensorData(sensorData)

                        // After inserting, fetch the data to check
                        fetchSensorData()  // Fetch and log the stored data
                    }

                    // Update previous value
                    prevHr = hr
                }
            }
        }

        // Update the UI with the new values if there's a change
        setContent {
            WearApp(
                axValue.value, ayValue.value, azValue.value,
                gxValue.value, gyValue.value, gzValue.value,
                hrValue.value
            )
        }
    }

    fun fetchSensorData() {
        lifecycleScope.launch {
            val sensorDataList = database.sensorDataDao().getAllSensorData()
            // Log the result or display it for verification
            Log.d("SensorData", "Fetched Data: $sensorDataList")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
        }
        gyroscope?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
        }
        heartRate?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
        }
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
fun WearApp(x1: String, y1: String, z1: String,
            x2: String, y2: String, z2: String,
            hr: String) {
    MyApplicationTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            TimeText()
            Greeting(x1 = x1, y1 = y1, z1 = z1,
                x2 = x2, y2 = y2, z2 = z2,
                hr = hr)
        }
    }
}

@Composable
fun Greeting(x1: String, y1: String, z1: String,
             x2: String, y2: String, z2: String,
             hr: String) {
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
    WearApp("x1","y1","z1","x2","y2","z2", "hr")
}