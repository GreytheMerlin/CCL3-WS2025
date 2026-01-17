package com.example.snorly.feature.challenges.steps

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun StepChallengeRoute(
    requiredSteps: Int,
    onSolved: () -> Unit,
    modifier: Modifier = Modifier,
    vm: StepChallengeViewModel = viewModel()
) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(SensorManager::class.java) }
    val stepSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) }
    val sensorMissing = stepSensor == null

    val state by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(requiredSteps, sensorMissing) {
        vm.start(requiredSteps = requiredSteps, sensorMissing = sensorMissing)
    }

    LaunchedEffect(Unit) {
        vm.effects.collectLatest { effect ->
            when (effect) {
                StepChallengeViewModel.Effect.Solved -> onSolved()
            }
        }
    }

    DisposableEffect(stepSensor) {
        if (stepSensor == null) {
            onDispose { }
        } else {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val total = event.values.firstOrNull() ?: return
                    vm.onTotalStepsSinceBoot(total)
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }

            sensorManager.registerListener(listener, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
            onDispose { sensorManager.unregisterListener(listener) }
        }
    }

    StepChallengeScreen(
        modifier = modifier,
        state = state
    )
}
