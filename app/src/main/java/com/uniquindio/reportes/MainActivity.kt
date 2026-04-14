package com.uniquindio.reportes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.uniquindio.reportes.core.navigation.AppNavigation
import com.uniquindio.reportes.ui.theme.ReportesCiudadanosTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReportesCiudadanosTheme {
                AppNavigation()
            }
        }
    }
}