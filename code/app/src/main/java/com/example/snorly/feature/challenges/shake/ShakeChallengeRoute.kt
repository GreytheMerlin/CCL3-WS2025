package com.example.snorly.feature.challenges.shake

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ShakeChallengeRoute(
    requiredShakes: Int,
    onSolved: () -> Unit,
    modifier: Modifier = Modifier,
    vm: ShakeChallengeViewModel = viewModel()
) {
    val state = vm.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(requiredShakes) {
        vm.start(requiredShakes)
    }

    LaunchedEffect(Unit) {
        vm.effects.collectLatest { effect ->
            when (effect) {
                ShakeChallengeViewModel.Effect.Solved -> onSolved()
            }
        }
    }

    ShakeChallengeScreen(
        modifier = modifier,
        state = state,
        onGForce = vm::onGForce
    )
}
