package com.example.snorly.feature.challenges.qr

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun QrChallengeRoute(
    expectedValue: String? = null,
    onSolved: () -> Unit,
    modifier: Modifier = Modifier,
    vm: QrChallengeViewModel = viewModel()
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    // set expected value once
    LaunchedEffect(expectedValue) {
        vm.setExpectedValue(expectedValue)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        vm.onPermissionResult(granted)
    }

    LaunchedEffect(Unit) {
        vm.effects.collectLatest { effect ->
            when (effect) {
                QrChallengeViewModel.Effect.Solved -> onSolved()
            }
        }
    }

    QrChallengeScreen(
        modifier = modifier,
        state = state,
        onEnableCamera = { permissionLauncher.launch(Manifest.permission.CAMERA) },
        onTryAgainPermission = {
            vm.requestPermissionAgain()
            permissionLauncher.launch(Manifest.permission.CAMERA)
        },
        onQrScanned = vm::onQrScanned,
        onSimulateSuccess = vm::simulateSuccessScan
    )
}