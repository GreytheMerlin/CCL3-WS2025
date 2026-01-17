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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    val largeCount = Int.MAX_VALUE
    val base = remember(items) { largeCount / 2 - (largeCount / 2 % items.size) }

    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // ✅ internal selected value (what the wheel currently shows)
    var currentValue by remember { androidx.compose.runtime.mutableIntStateOf(initialValue) }

    // ✅ suppress callback/haptics while we do programmatic scrolls
    var programmaticScroll by remember { mutableStateOf(false) }

    // ✅ Only scroll when parent requests a DIFFERENT value than what we already show
    LaunchedEffect(items, initialValue) {
        if (items.isEmpty()) return@LaunchedEffect

        val safeIndex = items.indexOf(initialValue).takeIf { it >= 0 } ?: 0
        val target = base + safeIndex

        if (initialValue != currentValue) {
            programmaticScroll = true
            listState.scrollToItem(target)
            currentValue = initialValue
            programmaticScroll = false
        }
    }

    // ✅ Selection logic: emit only when value actually changes AND not programmatic
    LaunchedEffect(listState, items) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { scrolling ->
                if (scrolling) return@collect

                val layoutInfo = listState.layoutInfo
                val visible = layoutInfo.visibleItemsInfo
                if (visible.isEmpty()) return@collect

                val viewportCenter = layoutInfo.viewportEndOffset / 2
                val centerIndex = visible
                    .minByOrNull { kotlin.math.abs((it.offset + it.size / 2) - viewportCenter) }
                    ?.index ?: return@collect

                val value = items[centerIndex.floorMod(items.size)]

                // avoid feedback loops & duplicates
                if (!programmaticScroll && value != currentValue) {
                    currentValue = value
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onItemSelected(value)
                }
            }
    }


    LazyColumn(
        modifier = modifier.width(width).height(itemHeight * 5),
        state = listState,
        flingBehavior = flingBehavior,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(count = largeCount, key = { it }) { index ->
            val itemValue = items[index % items.size]
            Box(
                modifier = Modifier
                    .height(itemHeight)
                    .graphicsLayer {
                        val viewportHeight = size.height * 5
                        val relativeIndex = index - listState.firstVisibleItemIndex
                        val itemCenterY =
                            (relativeIndex * size.height) - listState.firstVisibleItemScrollOffset + (size.height / 2)
                        val distanceFromCenter = itemCenterY - (viewportHeight / 2)
                        val normalizedDist = distanceFromCenter / (viewportHeight / 2)

                        rotationX = -60f * normalizedDist
                        val scale = 1f - (kotlin.math.abs(normalizedDist) * 0.3f)
                        scaleX = scale
                        scaleY = scale
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
private fun Int.floorMod(mod: Int): Int = ((this % mod) + mod) % mod
