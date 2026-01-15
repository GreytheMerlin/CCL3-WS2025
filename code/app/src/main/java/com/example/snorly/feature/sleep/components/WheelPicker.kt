package com.example.snorly.feature.sleep.components

import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> WheelPicker(
    items: List<T>,
    initialItem: T,
    itemLabel: (T) -> String,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    isMonospace: Boolean = false
) {
    // 1. Initial State
    // Ensure the index is safe.
    val initialIndex = items.indexOf(initialItem).coerceAtLeast(0)

    // We do NOT add offsets here anymore.
    // The padding naturally pushes index 0 to the center.
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val view = LocalView.current

    // 2. Logic to detect center item change
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .map { index -> index.coerceIn(0, items.lastIndex) }
            .distinctUntilChanged()
            .collect { centeredIndex ->
                onItemSelected(items[centeredIndex])
                // Satisfying Click
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                view.playSoundEffect(SoundEffectConstants.CLICK)
            }
    }

    // 3. Derived state for styling (scale/alpha)
    val centerIndex by remember {
        derivedStateOf { listState.firstVisibleItemIndex }
    }

    Box(modifier = modifier.height(200.dp), contentAlignment = Alignment.Center) {

        // --- 1. THE BIG HIGHLIGHT BAR ---
        // Sits behind the text, centered, spans full width of this column
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
        )

        // --- 2. SCROLLING LIST ---
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            // Vertical padding = (ContainerHeight - ItemHeight) / 2
            // (200 - 40) / 2 = 80dp
            // This pushes the first item exactly to the center
            contentPadding = PaddingValues(vertical = 80.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items.size) { index ->
                val isSelected = index == centerIndex
                val item = items[index]

                // Animate properties
                val scale by animateFloatAsState(if (isSelected) 1.2f else 0.85f, label = "scale")
                val alpha by animateFloatAsState(if (isSelected) 1f else 0.4f, label = "alpha")

                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = itemLabel(item),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                        fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .scale(scale)
                            .alpha(alpha)
                    )
                }
            }
        }

        // --- 3. FADE GRADIENTS (Top and Bottom overlay) ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1C1C1E),
                            Color.Transparent,
                            Color.Transparent,
                            Color(0xFF1C1C1E)
                        ),
                        startY = 0f,
                        endY = 500f // Approximate mapping to pixel height
                    )
                )
        )
    }
}


@Composable
fun CustomDateTimePicker(
    title: String,
    initialDate: LocalDate,
    initialTime: LocalTime,
    onCancel: () -> Unit,
    onSave: (LocalDate, LocalTime) -> Unit
) {
    var selectedDate by remember { mutableStateOf(initialDate) }
    var selectedHour by remember { mutableStateOf(initialTime.hour) }
    var selectedMinute by remember { mutableStateOf(initialTime.minute) }

    Column(modifier = Modifier.padding(24.dp)) {
        Text(
            title,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp).align(Alignment.CenterHorizontally)
        )

        Row(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val dateOptions = remember { (-15..15).map { initialDate.plusDays(it.toLong()) } }
            WheelPicker(
                items = dateOptions,
                initialItem = initialDate,
                itemLabel = { it.format(DateTimeFormatter.ofPattern("EEE, d")) },
                onItemSelected = { selectedDate = it },
                modifier = Modifier.weight(1.6f)
            )

            val hours = (0..23).toList()
            WheelPicker(
                items = hours,
                initialItem = selectedHour,
                itemLabel = { "%02d".format(it) },
                onItemSelected = { selectedHour = it },
                modifier = Modifier.weight(1f),
                isMonospace = true
            )

            Text(":", color = Color.Gray, fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

            val minutes = (0..59).toList()
            WheelPicker(
                items = minutes,
                initialItem = selectedMinute,
                itemLabel = { "%02d".format(it) },
                onItemSelected = { selectedMinute = it },
                modifier = Modifier.weight(1f),
                isMonospace = true
            )
        }

        Spacer(Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Cancel", fontSize = 16.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = { onSave(selectedDate, LocalTime.of(selectedHour, selectedMinute)) },
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Confirm", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
