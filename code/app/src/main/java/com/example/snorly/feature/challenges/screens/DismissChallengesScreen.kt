package com.example.snorly.feature.challenges.screens

import android.util.Log
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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


//    fun handleBack() {
//        val result = if (state.isEnabled) {
//            state.activeChallenges.map { it.id }
//        } else {
//            emptyList()
//        }
//        onResult(result)
//        onBack()
//    }

fun handleBack() {
    // We pass both the list AND the enabled state back to the caller
    val activeIds = state.activeChallenges.map { it.id }

    // This allows to update the AlarmEntity
    // with both the List and the boolean toggle.
    onResult(activeIds)
    onBack()
}

    // Handles the Physical/System Back button
    androidx.activity.compose.BackHandler {
        handleBack()
    }

    // 1. Setup Reorderable State
    val reorderableState = rememberReorderableLazyListState(listState) { from, to ->
        // We subtract 1 because the 'Enable Challenges' row is index 0
        val fromAdjusted = (from.index - 1).coerceAtLeast(0)
        val toAdjusted = (to.index - 1).coerceAtLeast(0)
        // This callback runs when items are moved
        viewModel.moveChallenge(fromAdjusted, toAdjusted)
    }

    Scaffold(
        topBar = { BackTopBar(title = "Dismiss Challenges", onBackClick = { handleBack() }) },
    ) { innerPadding ->


        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (state.activeChallenges.isEmpty()) {
                // --- NEW CENTERED EMPTY STATE ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray.copy(alpha = 0.3f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No Dismiss Challenges",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Add challenges to make sure you're truly awake before turning off your alarm.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = onAddClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add Challenge")
                    }
                }
            } else {
                // --- CHALLENGE LIST ---

                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
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
                                    uncheckedBorderColor = MaterialTheme.colorScheme.outline.copy(
                                        alpha = 0.5f
                                    )
                                )
                            )
                        }
                    }
                    itemsIndexed(
                        state.activeChallenges, key = { _, item -> item.id }) { index, challenge ->
                        ReorderableItem(reorderableState, key = challenge.id) { isDragging ->
                            val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)

                            ActiveChallengeCard(
//                                    modifier = Modifier
//                                        .shadow(elevation, RoundedCornerShape(12.dp))
//                                        .background(Color(0xFF1C1C1E)),

                                challenge = challenge,
                                onDelete = { viewModel.removeChallenge(challenge) },
                                dragModifier = Modifier.draggableHandle(),
                                isEnabled = state.isEnabled,
                            )
                        }
                    }

                    // --- STICKY BOTTOM ADD CARD ---
                    if (state.activeChallenges.size < state.availableChallenges.size) {
                        item {
                            OutlinedAddCard(onClick = onAddClick)
                        }
                    }

                    // Extra spacer for bottom navigation clearance
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun OutlinedAddCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray)
            Spacer(Modifier.width(8.dp))
            Text("Add Challenge", color = Color.Gray, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun ActiveChallengeCard(
    modifier: Modifier = Modifier, // Allow passing modifier
    challenge: Challenge,
    onDelete: () -> Unit,
    dragModifier: Modifier, // Specific modifier for the handle
    isEnabled: Boolean,
) {
    Card(
        modifier = modifier.fillMaxWidth(), // Apply the reorderable modifier here
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
//        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {

        val contentAlpha = if (isEnabled) 1f else 0.38f

        Row(
            modifier = Modifier.padding(16.dp).alpha(contentAlpha),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 4. Apply the drag handle modifier to the Icon
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Drag",
                tint = if (isEnabled) Color.Gray else Color.DarkGray,
                modifier = if (isEnabled) dragModifier else Modifier // This makes only the icon draggable
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
                Text(
                    challenge.difficulty,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}