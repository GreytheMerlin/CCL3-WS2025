package com.example.snorly.feature.challenges.math

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MathChallengeRoute(
    onSolved: () -> Unit,
    modifier: Modifier = Modifier,
    vm: MathChallengeViewModel = viewModel()
) {
    val state = vm.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(Unit) {
        vm.effects.collectLatest { effect ->
            when (effect) {
                MathChallengeViewModel.Effect.Solved -> onSolved()
            }
        }
    }

    MathChallengeScreen(
        state = state,
        modifier = modifier,
        onDigit = vm::onDigit,
        onBackspace = vm::onBackspace,
        onConfirm = vm::onConfirm
    )
}
