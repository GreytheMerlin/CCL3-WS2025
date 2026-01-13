package com.example.snorly.feature.challenges.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

data class Challenge(
    val id: String,
    val title: String,
    val description: String,
    val difficulty: String, // "Easy", "Hard"
    val icon: ImageVector,
    val color: Color
)

// Mock Data Source
object ChallengeDataSource {
    val allChallenges = listOf(
        Challenge("1", "Memory Game", "Match pairs to dismiss", "Medium", Icons.Default.GridView, Color(0xFF4CAF50)),
        Challenge("2", "Math Problem", "Solve 3 equations", "Hard", Icons.Default.Calculate, Color(0xFF2196F3)),
        Challenge("3", "Step Counter", "Walk 20 steps", "Easy", Icons.Default.DirectionsWalk, Color(0xFFFF9800)),
        Challenge("4", "Shake Phone", "Shake vigorously for 5s", "Medium", Icons.Default.Vibration, Color(0xFF9C27B0)),
        Challenge("5", "QR Code", "Scan a specific barcode", "Hard", Icons.Default.QrCode, Color(0xFF607D8B)),
        Challenge("6", "Typing", "Type a random sentence", "Medium", Icons.Default.Keyboard, Color(0xFFE91E63))
    )
}