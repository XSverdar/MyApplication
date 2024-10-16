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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.myapplication.R
import com.example.myapplication.presentation.theme.MyApplicationTheme

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var heartRate: Sensor? = null

    val axValue = mutableStateOf("")
    val ayValue = mutableStateOf("")
    val azValue = mutableStateOf("")
    val gxValue = mutableStateOf("")
    val gyValue = mutableStateOf("")
    val gzValue = mutableStateOf("")
    val hrValue = mutableStateOf("")

    val permissions = arrayOf(
        android.Manifest.permission.BODY_SENSORS,
        android.Manifest.permission.ACTIVITY_RECOGNITION,
        android.Manifest.permission.HIGH_SAMPLING_RATE_SENSORS
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            WearApp("x1","y1","z1","x2","y2","z2","hr")
        }

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        getSensors()
    }

    fun getSensors() {
        if (permissions.any { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) {
            ActivityCompat.requestPermissions(this, permissions, 1)
        }
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
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
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            axValue.value = "${event.values[0]}"
            ayValue.value = "${event.values[1]}"
            azValue.value = "${event.values[2]}"
        }
        else if (event?.sensor?.type == Sensor.TYPE_GYROSCOPE) {
            gxValue.value = "${event.values[0]}"
            gyValue.value = "${event.values[1]}"
            gzValue.value = "${event.values[2]}"
        }
        else if (event?.sensor?.type == Sensor.TYPE_HEART_RATE) {
            hrValue.value = "${event.values[0]}"
        }

        setContent {
            WearApp(
                axValue.value, ayValue.value, azValue.value,
                gxValue.value, gyValue.value, gzValue.value,
                hrValue.value
            )
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