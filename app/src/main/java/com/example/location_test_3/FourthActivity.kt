package com.chaquo.python.location_test_3

import android.Manifest
import android.content.pm.PackageManager
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import android.content.Intent
import android.widget.Button
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView


class FourthActivity : AppCompatActivity(), SensorEventListener, LocationListener {

    private lateinit var latitude_text: TextView
    private lateinit var longitude_text: TextView
    private lateinit var azimuth_text: TextView
    private lateinit var place_text: TextView
    private lateinit var dist_text: TextView
    private lateinit var turn_angle: TextView
    private lateinit var searchBar: AutoCompleteTextView


    private lateinit var sensorManager: SensorManager
    private var rotationVectorSensor: Sensor? = null
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var latitude_val: String = "0.0"
    private var longitude_val: String = "0.0"
    private var orientation_val: String = "0.0"

    private lateinit var locationManager: LocationManager
    private lateinit var pythonModule: PyObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fourth)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        val py = Python.getInstance()
        pythonModule = py.getModule("python_t1")

        // Find views
        latitude_text = findViewById(R.id.textView4)
        longitude_text = findViewById(R.id.textView3)
        azimuth_text = findViewById(R.id.textView)
        place_text = findViewById(R.id.textView5)
        dist_text = findViewById(R.id.textView6)
        turn_angle = findViewById(R.id.textView7)
        searchBar = findViewById(R.id.searchBar)

        val suggestions = pythonModule.callAttr("get_suggestions").asList().map { it.toString() }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, suggestions)
        searchBar.setAdapter(adapter)

        val buttonBack = findViewById<Button>(R.id.button2)
        // Set an OnClickListener on the button
        buttonBack.setOnClickListener {
            // Create an Intent to navigate to SecondActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent) // Start the second activity
        }

        // Set up LocationManager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Set up SensorManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    }

    private fun handleLocationUpdate() {
        val name = searchBar.text.toString()
        if (name.isNotEmpty()) {
            // For testing purposes
            latitude_val = "19.1375"
            longitude_val = "72.9125"
            orientation_val = "45"

            val result: PyObject = pythonModule.callAttr("get_place_in_view", latitude_val, longitude_val, orientation_val, name)

            // Extract name, distance, turn angle, and other_tags
            val placeName = result.asList()[0].toString()
            val distance = result.asList()[1].toString() + " meters"
            val turnAngle = result.asList()[2].toString() + " degrees"
            val otherTags = result.asList()[3].toString()

            // Display name, distance, and turn angle
            place_text.text = placeName
            dist_text.text = distance
            turn_angle.text = turnAngle

            // Split other_tags by comma and display each property on a new line
            val formattedTags = otherTags.split(",").joinToString("\n") { it.trim() }
            val otherTagsTextView: TextView = findViewById(R.id.textView8) // Assuming you have a TextView for other tags
            otherTagsTextView.text = formattedTags

        } else {
            place_text.text = "Please enter a name"
        }
    }

    private fun location_startUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000,
                0f,
                this
            )
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                1000,
                0f,
                this
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun onLocationChanged(location: Location) {
        handleLocationUpdate()
    }

    private fun angle_processOrientation(azimuth: Float) {
        val normalizedAzimuth = (azimuth + 360) % 360
        val roundedAzimuth = String.format("%.1f", normalizedAzimuth).toDouble()
        orientation_val = roundedAzimuth.toString()
        azimuth_text.text = roundedAzimuth.toString()
    }

    override fun onResume() {
        super.onResume()
        location_startUpdates()
        rotationVectorSensor?.let { sensor ->
            sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onPause() {
        super.onPause()
        locationManager.removeUpdates(this)
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, it.values)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                val azimuth = Math.toDegrees(orientationAngles[0].toDouble())
                angle_processOrientation(azimuth.toFloat())
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle sensor accuracy changes if needed
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                location_startUpdates()
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}
