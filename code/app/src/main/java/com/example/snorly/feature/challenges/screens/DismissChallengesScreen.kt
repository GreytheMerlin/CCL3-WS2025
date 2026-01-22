package com.example.snorly.feature.challenges.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import com.example.snorly.core.common.components.BackTopBar
import com.example.snorly.feature.challenges.model.Challenge
import com.example.snorly.feature.challenges.viewmodel.ChallengeViewModel

@Composable
fun DismissChallengesScreen(
    onBack: () -> Unit,
    onAddClick: () -> Unit,
    onResult: (List<String>) -> Unit,
    viewModel: ChallengeViewModel = viewModel(),

) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()



    fun handleBack() {
        val result = if (state.isEnabled) {
            state.activeChallenges.map { it.id }   
        } else {
            emptyList()
        }
        onResult(result)
        onBack()
    }

    // 1. Setup Reorderable State
    val reorderableState = rememberReorderableLazyListState(listState) { from, to ->
        // This callback runs when items are moved
        viewModel.moveChallenge(from.index, to.index)
    }

    Scaffold(
        topBar = { BackTopBar(title="Dismiss Challenges", onBackClick =  {handleBack()}) },
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

            // ... Master Toggle (Same as before) ...
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
                    onCheckedChange = { viewModel.toggleFeature(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.background,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surface,
                        uncheckedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )
            }

            if (state.isEnabled) {
                Text(
                    "Active Challenges (Drag to reorder)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // 2. The Reorderable LazyColumn
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Use itemsIndexed to get the key and index
                    itemsIndexed(state.activeChallenges, key = { _, item -> item.id }) { index, challenge ->

                        // 3. Wrap item in ReorderableItem
                        ReorderableItem(reorderableState, key = challenge.id) { isDragging ->

                            // Visual feedback when dragging (elevation)
                            val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)

                            ActiveChallengeCard(
                                modifier = Modifier
                                    .shadow(elevation, RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant), // Ensure bg is set
                                challenge = challenge,
                                onDelete = { viewModel.removeChallenge(challenge) },
                                // Pass the drag handle modifier down
                                dragModifier = Modifier.draggableHandle()
                            )
                        }
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
    modifier: Modifier = Modifier, // Allow passing modifier
    challenge: Challenge,
    onDelete: () -> Unit,
    dragModifier: Modifier // Specific modifier for the handle
) {
    Card(
        modifier = modifier.fillMaxWidth(), // Apply the reorderable modifier here
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 4. Apply the drag handle modifier to the Icon
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Drag",
                tint = Color.Gray,
                modifier = dragModifier // CRITICAL: This makes only the icon draggable
            )

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