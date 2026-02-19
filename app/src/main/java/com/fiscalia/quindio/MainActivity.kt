package com.fiscalia.quindio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.fiscalia.quindio.ui.screens.MainScreen
import com.fiscalia.quindio.ui.theme.FiscaliaQuindioTheme
import com.fiscalia.quindio.worker.DailyResetWorker

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Programar reinicio diario
        DailyResetWorker.schedule(this)

        setContent {
            FiscaliaQuindioTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}