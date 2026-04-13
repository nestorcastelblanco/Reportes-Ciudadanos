package com.example.seguimiento1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.seguimiento1.core.navigation.AppNavigation
import com.example.seguimiento1.ui.theme.Seguimiento1Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Seguimiento1Theme {
                AppNavigation()
            }
        }
    }
}