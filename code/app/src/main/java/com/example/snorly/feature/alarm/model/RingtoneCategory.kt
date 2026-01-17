package com.example.snorly.feature.alarm.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.snorly.R

// 1. Enums to define the "Vibe" in our Data Layer
enum class ShapeType { ROUNDED_RECT, CIRCLE, SCALLOP, SHIELD, HEXAGON, PILL, STAR }
enum class ShaderType { NEBULA, GRID, WAVES, RETRO_NOISE, CYBER_GLITCH, AURORA }

// 2. The Sealed Class Definition
sealed class RingtoneItem {
    data object Device : RingtoneItem()

    // Unified Data Class for ALL Cards (Spotify, Composer, Categories)
    // This allows them all to use the same powerful ExperimentalCard
    data class Card(
        val id: String,
        val title: String,
        val subtitle: String,
        val icon: ImageVector,
        val colorStart: Color,
        val colorEnd: Color,
        val shapeStart: ShapeType,
        val shapeEnd: ShapeType = ShapeType.CIRCLE, // Default press shape
        val shader: ShaderType,
        val isSpecial: Boolean = false // For Spotify/Composer specific click handling
    ) : RingtoneItem() {
        // Dynamic Count Property
        val countText: String
            get() {
                if (isSpecial) return "" // Spotify/Composer don't show counts
                val count = RingtoneData.getSoundsForCategory(id).size
                return "$count sounds"
            }
    }
}

// 3. The Data Source
object RingtoneData {

    // Internal Sound Database
    private val classicSounds = listOf(
        SoundDefinition("c1", "Classic Alarm", R.raw.classic_alarm),
        SoundDefinition("c2", "Human Beep", R.raw.delelele),
        SoundDefinition("c3", "Retro Emergency", R.raw.alarm1000),
        SoundDefinition("c4", "Bell Tower", R.raw.classic_alarm) // Placeholder
    )
    private val natureSounds = listOf(
        SoundDefinition("n1", "Morning Birds", R.raw.morning_birds),
        SoundDefinition("n2", "Ocean Waves", R.raw.morning_birds),
        SoundDefinition("n3", "Rainfall", R.raw.morning_birds),
        SoundDefinition("n4", "Windy Forest", R.raw.morning_birds),
        SoundDefinition("n5", "River Stream", R.raw.morning_birds)
    )
    private val animalSounds = listOf(
        SoundDefinition("a1", "Rooster", R.raw.chicken),
        SoundDefinition("a2", "Mooing Cow", R.raw.moo),
        SoundDefinition("a3", "Cat Purr", R.raw.chicken),
        SoundDefinition("a4", "Dog Bark", R.raw.moo)
    )
    // Empty lists for others to demonstrate dynamic counting
    private val alarmSounds = listOf(
        SoundDefinition("al1", "Nuclear", R.raw.alarm1000),
        SoundDefinition("al2", "Siren", R.raw.alarm1000),
        SoundDefinition("al3", "Buzzer", R.raw.alarm1000),
        SoundDefinition("al4", "Pager", R.raw.alarm1000)
    )
    private val musicSounds = listOf(
        SoundDefinition("m1", "Piano", R.raw.classic_alarm),
        SoundDefinition("m2", "Synth", R.raw.classic_alarm),
        SoundDefinition("m3", "Lo-Fi", R.raw.classic_alarm),
        SoundDefinition("m4", "Jazz", R.raw.classic_alarm)
    )
    private val modernSounds = listOf(
        SoundDefinition("mo1", "Ping", R.raw.delelele),
        SoundDefinition("mo2", "Glass", R.raw.delelele),
        SoundDefinition("mo3", "Notify", R.raw.delelele),
        SoundDefinition("mo4", "Pop", R.raw.delelele)
    )
    private val abstractSounds = listOf(
        SoundDefinition("ab1", "Dream", R.raw.morning_birds),
        SoundDefinition("ab2", "Void", R.raw.morning_birds),
        SoundDefinition("ab3", "Space", R.raw.morning_birds),
        SoundDefinition("ab4", "Time", R.raw.morning_birds)
    )

    // Public Items List (Fully Mapped to Design System)
    val items = listOf(
        // -- Row 1: Specials --
        RingtoneItem.Card(
            id = "spotify", title = "Spotify", subtitle = "Your music",
            icon = Icons.Default.MusicNote,
            colorStart = Color(0xFF1DB954), colorEnd = Color(0xFF0B0F0C),
            shapeStart = ShapeType.PILL, shader = ShaderType.WAVES,
            isSpecial = true
        ),
        RingtoneItem.Card(
            id = "composer", title = "Composer", subtitle = "Create custom",
            icon = Icons.Default.Build,
            colorStart = Color(0xFFFFD700), colorEnd = Color(0xFF3E2723),
            shapeStart = ShapeType.HEXAGON, shader = ShaderType.GRID,
            isSpecial = true
        ),

        // -- Row 2: Basic --
        RingtoneItem.Card(
            id = "classic", title = "Classic", subtitle = "Traditional sounds",
            icon = Icons.Default.Notifications,
            colorStart = Color(0xFF78909C), colorEnd = Color(0xFF263238),
            shapeStart = ShapeType.ROUNDED_RECT, shader = ShaderType.RETRO_NOISE
        ),
        RingtoneItem.Card(
            id = "alarms", title = "Alarms", subtitle = "Attention-grabbing",
            icon = Icons.Outlined.ErrorOutline,
            colorStart = Color(0xFFFF5252), colorEnd = Color(0xFF1A0505),
            shapeStart = ShapeType.SHIELD, shader = ShaderType.CYBER_GLITCH
        ),

        // -- Row 3: Nature --
        RingtoneItem.Card(
            id = "nature", title = "Nature", subtitle = "Peaceful sounds",
            icon = Icons.Default.Landscape,
            colorStart = Color(0xFF66BB6A), colorEnd = Color(0xFF004D40),
            shapeStart = ShapeType.SCALLOP, shader = ShaderType.AURORA
        ),
        RingtoneItem.Card(
            id = "music", title = "Music", subtitle = "Wake-up tunes",
            icon = Icons.Default.MusicNote,
            colorStart = Color(0xFFE040FB), colorEnd = Color(0xFF311B92),
            shapeStart = ShapeType.ROUNDED_RECT, shader = ShaderType.WAVES
        ),

        // -- Row 4: Others --
        RingtoneItem.Card(
            id = "animals", title = "Animals", subtitle = "Wild wake up",
            icon = Icons.Default.MusicNote,
            colorStart = Color(0xFFFF9800), colorEnd = Color(0xFFBF360C),
            shapeStart = ShapeType.STAR, shader = ShaderType.NEBULA
        ),
        RingtoneItem.Card(
            id = "modern", title = "Modern", subtitle = "Contemporary",
            icon = Icons.Default.GraphicEq,
            colorStart = Color(0xFF00E5FF), colorEnd = Color(0xFF0D47A1),
            shapeStart = ShapeType.ROUNDED_RECT, shader = ShaderType.GRID,
            shapeEnd = ShapeType.PILL // Special end shape
        ),
        RingtoneItem.Card(
            id = "abstract", title = "Abstract", subtitle = "Creative sounds",
            icon = Icons.Default.Palette,
            colorStart = Color(0xFFFF4081), colorEnd = Color(0xFF4A148C),
            shapeStart = ShapeType.SCALLOP, shader = ShaderType.NEBULA
        )
    )

    fun getSoundsForCategory(categoryId: String): List<SoundDefinition> {
        return when (categoryId) {
            "classic" -> classicSounds
            "nature" -> natureSounds
            "animals" -> animalSounds
            "alarms" -> alarmSounds
            "music" -> musicSounds
            "modern" -> modernSounds
            "abstract" -> abstractSounds
            else -> emptyList()
        }
    }

    data class SoundDefinition(val id: String, val title: String, val resId: Int)
}