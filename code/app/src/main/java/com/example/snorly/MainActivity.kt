package com.example.snorly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.snorly.core.common.nav.NavController
import com.example.snorly.core.ui.theme.SnorlyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SnorlyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavController(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
