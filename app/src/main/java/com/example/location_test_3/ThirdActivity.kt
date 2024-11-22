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
import android.os.Looper
import android.util.Log
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AppCompatActivity
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import android.content.Intent
import android.widget.Button
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class ThirdActivity : AppCompatActivity(), SensorEventListener, LocationListener {
    private lateinit var latitude_text: TextView
    private lateinit var longitude_text: TextView
    private lateinit var azimuth_text: TextView
    private lateinit var place_text: TextView

    private lateinit var sensorManager: SensorManager
    private var rotationVectorSensor: Sensor? = null
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var latitude_val: String = "0.0"
    private var longitude_val: String = "0.0"
    private var orientation_val: String = "0.0"


    private lateinit var locationManager: LocationManager

    private lateinit var pythonModule: PyObject

//    private lateinit var fusedLocationClient: FusedLocationProviderClient
//    private lateinit var locationCallback: LocationCallback
//    private lateinit var locationRequest: LocationRequest


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // setContentView(com.chaquo.python.location_test_3.R.layout.activity_main)
        setContentView(R.layout.activity_third)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if( !Python.isStarted() ) {
            Python.start( AndroidPlatform( this ) )
        }

        val py = Python.getInstance()
        pythonModule = py.getModule( "python_t2" )

        // fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        latitude_text = findViewById(R.id.textView4)
        longitude_text = findViewById(R.id.textView3)
        azimuth_text = findViewById(R.id.textView)
        place_text = findViewById((R.id.textView5))

        // text2.setText("Hello")
        latitude_text.text = "Hello"
        longitude_text.text = "Hello"
        azimuth_text.text = "Hello"

        val buttonBack = findViewById<Button>(R.id.button4)
        // Set an OnClickListener on the button
        buttonBack.setOnClickListener {
            // Create an Intent to navigate to SecondActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent) // Start the second activity
        }

        // Initialize LocationManager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//        location_setupLocationRequest()
//        location_setupLocationCallback()

        // Initialize SensorManager and sensors
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Get rotation vector sensor
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    }

//    private fun location_setupLocationRequest() {
//        locationRequest = LocationRequest.create().apply {
//            interval = 5000 // 10 seconds
//            fastestInterval = 1000 // 5 seconds
//            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//        }
//    }

//    private fun location_setupLocationCallback() {
//        locationCallback = object : LocationCallback() {
//            override fun onLocationResult(locationResult: LocationResult) {
//                locationResult.lastLocation?.let { location ->
//                    location_processlocation(location)
//                }
//            }
//        }
//    }

    private fun location_startUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            // Request location permissions
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        // Request location updates from GPS and Network providers
        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000, // 1 seconds
                0f, // 0 meters
                this
            )
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                1000, // 1 seconds
                0f, // 0 meters
                this
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
//
//        fusedLocationClient.requestLocationUpdates(
//            locationRequest,
//            locationCallback,
//            Looper.getMainLooper()
//        )
    }

    // LocationListener methods
    override fun onLocationChanged(location: Location) {
        location_processlocation(location)
    }


    // Your function that runs on location change
    private fun location_processlocation(location: Location) {
        // Handle location update here
        // Example: Log the location
        val latitude = location.latitude
        val longitude = location.longitude
        latitude_val = latitude.toString()
        longitude_val = longitude.toString()
        latitude_text.setText(longitude.toString())
        longitude_text.setText(latitude.toString())

        // For testing
//        latitude_val = "19.1340"
//        longitude_val = "72.9160"
//        orientation_val = "45"

        val result: PyObject = pythonModule.callAttr("get_place_in_view", latitude_val,longitude_val, orientation_val, "45", "500" )
        place_text.text = result.toString()
    }

    private fun angle_processOrientation(azimuth: Float) {
        // Your custom logic for processing azimuthal angle
        // Convert azimuth to degrees and handle as needed
        val normalizedAzimuth = (azimuth + 360) % 360
        val roundedAzimuth = String.format("%.1f", normalizedAzimuth).toDouble()
        orientation_val = roundedAzimuth.toString()

        // Log or perform actions with the azimuthal angle
        azimuth_text.setText(roundedAzimuth.toString())
    }

    // Orientation-related functions
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
        //fusedLocationClient.removeLocationUpdates(locationCallback)
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
