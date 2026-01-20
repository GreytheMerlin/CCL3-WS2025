package com.example.snorly

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.snorly.core.common.nav.SnorlyApp
import com.example.snorly.core.ui.theme.SnorlyTheme



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
        }

        super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            setContent {
                SnorlyTheme {
                    SnorlyApp()
                }
            }

    }
}



