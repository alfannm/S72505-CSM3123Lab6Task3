package com.example.compasslab

// Android framework imports for sensors, UI, and lifecycle handling
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

// MainActivity acts as both the UI controller and sensor listener
class MainActivity : AppCompatActivity(), SensorEventListener {

    // SensorManager controls access to device sensors
    // Accelerometer and Magnetometer are required for compass calculation
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null

    // UI components
    // tvHeading displays the current compass heading in degrees
    // imgArrow visually points to North
    private lateinit var tvHeading: TextView
    private lateinit var imgArrow: ImageView

    // Arrays to store latest sensor readings
    // gravity holds accelerometer data (used to detect device orientation)
    // geomagnetic holds magnetometer data (used to detect magnetic field)
    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load the compass UI layout
        setContentView(R.layout.activity_main)

        // 1. Initialize UI components from XML
        tvHeading = findViewById(R.id.headingText)
        imgArrow = findViewById(R.id.compassArrow)

        // 2. Initialize SensorManager and required sensors
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    // Lifecycle method called when the app becomes visible
    override fun onResume() {
        super.onResume()

        // Register accelerometer sensor updates
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        // Register magnetometer sensor updates
        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    // Lifecycle method called when the app is paused or minimized
    override fun onPause() {
        super.onPause()

        // Unregister all sensor listeners to save battery and CPU usage
        sensorManager.unregisterListener(this)
    }

    // Called whenever a registered sensor provides new data
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        // Step 3: Store latest sensor readings
        // Accelerometer data is saved to gravity array
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values.clone()
        }

        // Magnetometer data is saved to geomagnetic array
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values.clone()
        }

        // Continue only when both sensor datasets are available
        if (gravity != null && geomagnetic != null) {

            // Step 4: Compute the rotation matrix
            // R = rotation matrix
            // I = inclination matrix
            val R = FloatArray(9)
            val I = FloatArray(9)

            val success = SensorManager.getRotationMatrix(
                R,
                I,
                gravity,
                geomagnetic
            )

            // Proceed only if rotation matrix calculation succeeds
            if (success) {

                // Step 5: Obtain device orientation values
                val orientation = FloatArray(3)
                SensorManager.getOrientation(R, orientation)

                // orientation[0] represents azimuth (direction relative to North) in radians
                val azimuth = orientation[0]

                // Step 6: Convert azimuth value into usable compass heading
                // 1. Convert radians to degrees
                var degree = Math.toDegrees(azimuth.toDouble()).toFloat()

                // 2. Normalize angle to range 0–360 degrees
                if (degree < 0) {
                    degree += 360
                }

                // Update heading text to show current direction
                tvHeading.text = "${degree.toInt()}° North"

                // 3. Rotate compass arrow
                // Negative rotation is applied so the arrow remains pointing North
                // while the phone rotates clockwise
                imgArrow.rotation = -degree
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Sensor accuracy changes are not required for this lab
    }
}
