package com.chaquo.python.location_test_3

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.chaquo.python.PyObject
import android.content.Intent

class MainActivity : AppCompatActivity(){


    private lateinit var pythonModule: PyObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val intent = Intent(this, SecondActivity::class.java)
//        startActivity(intent)


        // Find the button by its ID
        val buttonLook = findViewById<Button>(R.id.button3)
        val buttonSearch = findViewById<Button>(R.id.button1)

        // Set an OnClickListener on the button
        buttonLook.setOnClickListener {
            // Create an Intent to navigate to SecondActivity
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent) // Start the second activity
        }

        // Set an OnClickListener on the button
        buttonSearch.setOnClickListener {
            // Create an Intent to navigate to SecondActivity
            val intent = Intent(this, FourthActivity::class.java)
            startActivity(intent) // Start the second activity
        }
    }

}
