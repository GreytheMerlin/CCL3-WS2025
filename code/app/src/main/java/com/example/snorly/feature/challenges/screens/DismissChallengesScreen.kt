package com.example.snorly.feature.challenges.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.snorly.R
import com.example.snorly.core.common.components.BackTopBar
import com.example.snorly.feature.challenges.model.Challenge
import com.example.snorly.feature.challenges.viewmodel.ChallengeViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun DismissChallengesScreen(
    onBack: () -> Unit,
    onAddClick: () -> Unit,
    onResult: (Boolean, List<String>) -> Unit,
    viewModel: ChallengeViewModel = viewModel(),

    ) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()


    fun handleBack() {
        val result = state.activeChallenges.map { it.id }
        onResult(state.isEnabled, result)
        onBack()
    }

    // save when using android back navigation
    androidx.activity.compose.BackHandler {
        handleBack()
    }

    // 1. Setup Reorderable State
    val reorderableState = rememberReorderableLazyListState(listState) { from, to ->
        // This callback runs when items are moved
        viewModel.moveChallenge(from.index, to.index)
    }

    // haptics
    val haptic = LocalHapticFeedback.current

    Scaffold(
        topBar = { BackTopBar(title = "Dismiss Challenges", onBackClick = { handleBack() }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .animateContentSize()
        ) {
            AnimatedContent(
                targetState = state.activeChallenges.isNotEmpty(),
                transitionSpec = {
                    fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "EmptyStateTransition"
            ) { hasItems ->
                // hasItems is the boolean state.activeChallenges.isNotEmpty()
                if (hasItems) {
                    Column(modifier = Modifier.fillMaxSize()) {
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
                                checked = state.isEnabled, onCheckedChange = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) // Light tick
                                    viewModel.toggleFeature(it)
                                }, colors = SwitchDefaults.colors(
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

                        // Header logic
                        val headerText =
                            if (state.isEnabled) "Active Challenges (Drag to reorder)" else "Challenges (Disabled)"
                        Text(
                            headerText,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (state.isEnabled) 1f else 0.6f),
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
                            itemsIndexed(
                                state.activeChallenges,
                                key = { _, item -> item.id }) { index, challenge ->

                                // Wrap item in ReorderableItem
                                ReorderableItem(
                                    reorderableState, key = challenge.id
                                ) { isDragging ->

                                    // Visual feedback when dragging (elevation)
                                    val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)

                                    ActiveChallengeCard(
                                        modifier = Modifier
                                            .shadow(elevation)
                                            .animateItem(
                                                fadeInSpec = tween(300),
                                                fadeOutSpec = tween(300),
                                                placementSpec = spring(stiffness = Spring.StiffnessLow)
                                            ),
                                        challenge = challenge,
                                        enabled = state.isEnabled,
                                        onDelete = {
                                            // TRIGGER HAPTIC ON DELETE
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.removeChallenge(challenge)
                                        },
                                        // Pass the drag handle modifier down
                                        dragModifier = if (state.isEnabled) Modifier.draggableHandle() else Modifier
                                    )
                                }
                            }
                            if (state.isEnabled && state.availableChallenges.isNotEmpty()) {
                                item {
                                    Spacer(Modifier.height(8.dp)) // Extra breathing room after the last card
                                    Button(
                                        onClick = onAddClick, shape = RoundedCornerShape(12.dp),
//                                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surface),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Add Challenge", fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(Modifier.height(16.dp))
                                }
                            }
                        }}
                    } else {
                        ChallengeEmptyState(onAddClick = onAddClick)
                    }
                }
            }
        }
    }


@Composable
fun ActiveChallengeCard(
    modifier: Modifier = Modifier, // Allow passing modifier
    challenge: Challenge,
    enabled: Boolean,
    onDelete: () -> Unit,
    dragModifier: Modifier // Specific modifier for the handle
) {
    // Determine the transparency based on enabled state
    val contentAlpha = if (enabled) 1f else 0.4f

    Card(
        modifier = modifier.fillMaxWidth(), // Apply the reorderable modifier here
        shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = if (enabled) 1f else 0.5f
            )
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            // 4. Apply the drag handle modifier to the Icon
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Drag",
                tint = Color.Gray.copy(alpha = contentAlpha),
                modifier = dragModifier // CRITICAL: This makes only the icon draggable
            )

            Spacer(Modifier.width(16.dp))

            // Icon Box
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(challenge.color.copy(alpha = if (enabled) 0.2f else 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    challenge.icon,
                    contentDescription = null,
                    tint = challenge.color.copy(alpha = contentAlpha)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    challenge.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)
                )
                Text(
                    challenge.difficulty,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray.copy(alpha = contentAlpha)
                )
            }

            IconButton(onClick = onDelete, enabled = enabled) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = contentAlpha)
                )
            }
        }
    }
}

@Composable
fun ChallengeEmptyState(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
//        Icon(
//            imageVector = Icons.Default.Extension,
//            contentDescription = null,
//            modifier = Modifier.size(80.dp),
//            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
//        )
        Image(
            painter = painterResource(id = R.drawable.snorly_arcade),
            contentDescription = null,
            modifier = Modifier.size(240.dp)
        )



        Spacer(Modifier.height(24.dp))

        Text(
            text = "No challenges added",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Add a task to ensure you're fully awake before the alarm turns off.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onAddClick,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Add first challenge", fontWeight = FontWeight.Bold)
        }
    }
}