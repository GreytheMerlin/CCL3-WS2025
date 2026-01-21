package com.example.snorly

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.snorly.core.common.nav.SnorlyApp
import com.example.snorly.core.ui.theme.SnorlyTheme



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen() // Initialize
        super.onCreate(savedInstanceState)

        // We'll use a local variable or State to track if we are ready
        var isReady = false

        // Keep the splash screen on-screen until DataStore is loaded
        splashScreen.setKeepOnScreenCondition { !isReady }

        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
        }

            enableEdgeToEdge()
            setContent {
                SnorlyTheme {
                    SnorlyApp(onDataLoaded = { isReady = true })
                }
            }

    }
}



