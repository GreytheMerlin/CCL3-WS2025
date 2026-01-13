package com.example.snorly.feature.alarm.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InfiniteWheelPicker(
    modifier: Modifier = Modifier,
    width: Dp = 45.dp,
    itemHeight: Dp = 36.dp,
    items: List<Int>,
    initialValue: Int,
    onItemSelected: (Int) -> Unit
) {
    val haptics = LocalHapticFeedback.current

    // 1. Setup Infinite Scroll Logic
    // We start in the middle of a massive list so user can scroll up/down immediately
    val largeCount = Int.MAX_VALUE
    val startIndex = largeCount / 2 - (largeCount / 2 % items.size) + items.indexOf(initialValue)

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // 2. Haptics & Selection Logic
    // Detect when the center item changes to trigger a "tick"
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .map { index ->
                // Calculate the "center" item index based on layout
                val layoutInfo = listState.layoutInfo
                val visibleItems = layoutInfo.visibleItemsInfo
                if (visibleItems.isNotEmpty()) {
                    val viewportCenter = layoutInfo.viewportEndOffset / 2
                    visibleItems.minByOrNull { kotlin.math.abs((it.offset + it.size / 2) - viewportCenter) }?.index ?: index
                } else index
            }
            .distinctUntilChanged()
            .collect { centerIndex ->
                // Play Haptic Tick
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)

                // Report back the real value
                val realIndex = centerIndex % items.size
                onItemSelected(items[realIndex])
            }
    }

    LazyColumn(
        modifier = modifier
            .width(width)
            .height(itemHeight * 5), // Show 5 items (2 above, 1 center, 2 below)
        state = listState,
        flingBehavior = flingBehavior,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(
            count = largeCount,
            key = { it } // Unique keys for performance
        ) { index ->
            val itemValue = items[index % items.size]

            // 3. The 3D Drum Effect
            Box(
                modifier = Modifier
                    .height(itemHeight)
                    .graphicsLayer {
                        // FIXED MATH:
                        // 1. Calculate height of the viewing area (the drum)
                        val viewportHeight = size.height * 5

                        // 2. Calculate where THIS item is relative to the top of the list currently
                        // We subtract 'firstVisibleItemIndex' to normalize the huge index back to 0, 1, 2...
                        val relativeIndex = index - listState.firstVisibleItemIndex

                        // 3. Calculate exact Y position of the center of this item
                        val itemCenterY = (relativeIndex * size.height) - listState.firstVisibleItemScrollOffset + (size.height / 2)

                        // 4. Calculate how far this item is from the vertical center of the viewport
                        val distanceFromCenter = itemCenterY - (viewportHeight / 2)

                        // 5. Normalize distance (0 = center, 1 = top/bottom edge)
                        val normalizedDist = distanceFromCenter / (viewportHeight / 2)

                        // 6. Apply effects based on distance
                        // Rotate slightly on X axis to look like a drum
                        rotationX = -60f * normalizedDist

                        // Scale down items that are further away
                        val scale = 1f - (kotlin.math.abs(normalizedDist) * 0.3f)
                        scaleX = scale
                        scaleY = scale

                        // Fade out items at the edges.
                        // We clamp it to 0f so it doesn't crash with negative alpha numbers
                        alpha = (1f - (kotlin.math.abs(normalizedDist) * 1.2f)).coerceIn(0f, 1f)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = itemValue.toString().padStart(2, '0'),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}