package com.example.snorly.feature.challenges.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun DismissChallengesScreen(
    onBack: () -> Unit,
    onAddClick: () -> Unit,
    viewModel: ChallengeViewModel = viewModel() // Use Hilt or Factory in real app
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { BackTopBar("Dismiss Challenges", onBack) },
        floatingActionButton = {
            if (state.isEnabled) {
                FloatingActionButton(
                    onClick = onAddClick,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

            // 1. Master Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enable Challenges", style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = state.isEnabled,
                    onCheckedChange = { viewModel.toggleFeature(it) }
                )
            }

            if (state.isEnabled) {
                Text(
                    "Active Challenges (Order matters)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // 2. Draggable List (Simulated with LazyColumn)
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(state.activeChallenges) { index, challenge ->
                        ActiveChallengeCard(
                            challenge = challenge,
                            onDelete = { viewModel.removeChallenge(challenge) }
                        )
                    }
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Challenges are disabled", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun ActiveChallengeCard(
    challenge: Challenge,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.DragHandle, contentDescription = "Drag", tint = Color.Gray)
            Spacer(Modifier.width(16.dp))

            // Icon Box
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(challenge.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(challenge.icon, contentDescription = null, tint = challenge.color)
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(challenge.title, style = MaterialTheme.typography.titleMedium)
                Text(challenge.difficulty, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}