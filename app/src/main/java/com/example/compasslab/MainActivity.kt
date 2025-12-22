package com.example.compasslab

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), SensorEventListener {

    // Sensor Manager and Sensors
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null

    // UI Components
    private lateinit var tvHeading: TextView
    private lateinit var imgArrow: ImageView

    // Arrays to store sensor data [cite: 891-895]
    // gravity = Accelerometer data
    // geomagnetic = Magnetometer data
    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Initialize UI
        tvHeading = findViewById(R.id.headingText)
        imgArrow = findViewById(R.id.compassArrow)

        // 2. Initialize Sensors [cite: 896-898]
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    override fun onResume() {
        super.onResume()
        // Register both sensors
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        // Stop updates to save battery
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        // Step 3: Update Data Arrays [cite: 901-904]
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values.clone()
        }
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values.clone()
        }

        // Proceed only if we have data from BOTH sensors
        if (gravity != null && geomagnetic != null) {

            // Step 4: Compute Rotation Matrix [cite: 905-906]
            val R = FloatArray(9)
            val I = FloatArray(9)

            val success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)

            if (success) {
                // Step 5: Get Orientation [cite: 907-909]
                val orientation = FloatArray(3)
                SensorManager.getOrientation(R, orientation)

                // orientation[0] is Azimuth (rotation around Z-axis) in radians
                val azimuth = orientation[0]

                // Step 6: Convert and Rotate [cite: 911-917]
                // 1. Convert radians to degrees
                var degree = Math.toDegrees(azimuth.toDouble()).toFloat()

                // 2. Normalize (0 to 360)
                if (degree < 0) {
                    degree += 360
                }

                // Update Text
                tvHeading.text = "${degree.toInt()}° North"

                // 3. Rotate Arrow
                // Note: We rotate NEGATIVE degree to keep the arrow pointing North
                // while the phone rotates clockwise.
                imgArrow.rotation = -degree
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }
}