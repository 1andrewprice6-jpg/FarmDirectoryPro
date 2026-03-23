package com.example.farmdirectoryupgraded

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
// import androidx.compose.foundation.layout.weight // this line is removed to fix build issues

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}