package com.example.snorly.feature.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ReportScreen( modifier: Modifier = Modifier) {
    val colors = listOf(
        Color.Red, Color.Blue, Color.Green, Color.Magenta,
        Color.Yellow, Color.Cyan, Color.DarkGray
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        // IMPORTANT: Add padding at the bottom equal to BottomBar height + extra
        // so you can scroll the items all the way up behind the glass.
        contentPadding = PaddingValues(bottom = 120.dp, top = 20.dp, start = 20.dp, end = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(20) { index ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors[index % colors.size]),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Item #$index",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }
}