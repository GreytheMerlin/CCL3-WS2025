package com.example.snorly.feature.alarm.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// Define the types of items we have
sealed class RingtoneItem {
    // The top banner
    data object Device : RingtoneItem()

    // The grid items
    data class Category(
        val id: String,
        val title: String,
        val subtitle: String,
        val countText: String? = null, // e.g. "4 sounds"
        val icon: ImageVector,
        val colorStart: Color, // For the gradient background
        val colorEnd: Color
    ) : RingtoneItem()

    // Special integrations
    data object Spotify : RingtoneItem()
    data object Composer : RingtoneItem()
}

// Hardcoded data source for the UI
object RingtoneData {
    val items = listOf(
        // Row 1
        RingtoneItem.Spotify,
        RingtoneItem.Composer,

        // Row 2
        RingtoneItem.Category("classic", "Classic", "Traditional alarm sounds", "4 sounds", Icons.Default.Notifications, Color(0xFF656E74), Color(0xFF38434D)),
        RingtoneItem.Category("alarms", "Alarms", "Attention-grabbing", "4 sounds", Icons.Outlined.ErrorOutline, Color(0xFFB74D4D), Color(0xFF5A1E1E)),

        // Row 3
        RingtoneItem.Category("nature", "Nature", "Peaceful natural sounds", "5 sounds", Icons.Default.Landscape, Color(0xFF5B7E48), Color(0xFF2A3D20)),
        RingtoneItem.Category("music", "Music", "Musical wake-up tunes", "4 sounds", Icons.Default.MusicNote, Color(0xFF5D6578), Color(0xFF2C3242)),

        // Row 4
        RingtoneItem.Category("modern", "Modern", "Contemporary sounds", "4 sounds", Icons.Default.GraphicEq, Color(0xFF587883), Color(0xFF253A42)),
        RingtoneItem.Category("abstract", "Abstract", "Unique and creative", "4 sounds", Icons.Default.Palette, Color(0xFF8B8B4A), Color(0xFF424223))
    )
}