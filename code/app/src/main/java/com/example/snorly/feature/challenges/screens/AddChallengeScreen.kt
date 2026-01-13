package com.example.snorly.feature.challenges.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.snorly.core.common.components.BackTopBar
import com.example.snorly.feature.challenges.model.Challenge
import com.example.snorly.feature.challenges.viewmodel.ChallengeViewModel

@Composable
fun AddChallengeScreen(
    onBack: () -> Unit,
    onChallengeClick: (String) -> Unit, // Navigate to Detail with ID
    viewModel: ChallengeViewModel = viewModel() // Shared ViewModel scope needed in NavHost
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { BackTopBar("Add Challenge", onBack) }
    ) { innerPadding ->
        if (state.availableChallenges.isEmpty()) {
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No more challenges available!")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(innerPadding)
            ) {
                items(state.availableChallenges) { challenge ->
                    AvailableChallengeCard(challenge) { onChallengeClick(challenge.id) }
                }
            }
        }
    }
}

@Composable
fun AvailableChallengeCard(
    challenge: Challenge,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.aspectRatio(1f).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(challenge.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(challenge.icon, contentDescription = null, tint = challenge.color, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(challenge.title, style = MaterialTheme.typography.titleMedium)
            Text(challenge.difficulty, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}