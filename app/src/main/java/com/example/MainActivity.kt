package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    companion object {
        lateinit var instance: MainActivity
            private set
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        
        // Initialize Shell utilities & setup mock files
        ShellUtils.initialize(applicationContext)
        
        // Unleash the charging limit evaluation background engine
        ChargingController.startDaemon(applicationContext)
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = com.example.ui.theme.AmoledBlack
                ) {
                    MainScreen()
                }
            }
        }
    }
}

