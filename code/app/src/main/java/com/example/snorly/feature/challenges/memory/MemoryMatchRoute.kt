package com.example.snorly.feature.challenges.memory

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MemoryMatchRoute(
    modifier: Modifier = Modifier,
    pairCount: Int = 6,
    columns: Int = 4,
    memorizeMillis: Long = 1800L,
    onCompleted: () -> Unit,
    vm: MemoryMatchViewModel = viewModel()
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        vm.startGame(pairCount = pairCount, memorizeMillis = memorizeMillis)
    }

    LaunchedEffect(state.isCompleted) {
        if (state.isCompleted) {
            onCompleted()
        }
    }

    MemoryMatchGameScreen(
        modifier = modifier,
        uiState = state,
        columns = columns,
        onCardTap = vm::onCardTap
    )
}