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
        SoundDefinition("c2", "The Human Beep", R.raw.delelele),
        SoundDefinition("c3", "V8", R.raw.v8),
        SoundDefinition("c4", "Minions", R.raw.classic_beedoo_minions),
        SoundDefinition("c5", "Beep", R.raw.classic_beep),
        SoundDefinition("c7", "Space scan", R.raw.classic_space_scan),
        SoundDefinition("c8", "Urgent", R.raw.classic_urgent),
        SoundDefinition("c9", "Dream of victory", R.raw.classig_dreaming_of_victory),

    )
    private val natureSounds = listOf(
        SoundDefinition("n1", "Morning Birds", R.raw.morning_birds),
        SoundDefinition("n2", "Ocean Waves", R.raw.nature_ocean),
        SoundDefinition("n3", "Rainfall", R.raw.nature_rain),
        SoundDefinition("n4", "Chainsaw", R.raw.nature_chainsaw),
        SoundDefinition("n5", "Night Forest", R.raw.nature_night_forest),
        SoundDefinition("n6", "Wildlife", R.raw.nature_wildlife),
    )
    private val animalSounds = listOf(
        SoundDefinition("a1", "Chicken", R.raw.chicken),
        SoundDefinition("a2", "Mooing Cow", R.raw.moo),
        SoundDefinition("a3", "Cat Purr", R.raw.animal_cat),
        SoundDefinition("a4", "Bird", R.raw.animal_bird),
        SoundDefinition("a5", "Farm", R.raw.animal_farm),
        SoundDefinition("a6", "Horse", R.raw.animal_horse),
        SoundDefinition("a7", "Monkey", R.raw.animal_monkey),
        SoundDefinition("a8", "Pig", R.raw.animal_pig),
        SoundDefinition("a9", "Frog", R.raw.animal_frog),
        SoundDefinition("a10", "Crickets", R.raw.animal_crickets),

    )
    // Empty lists for others to demonstrate dynamic counting
    private val alarmSounds = listOf(
        SoundDefinition("al1", "Nuclear", R.raw.alarm_nuclear),
        SoundDefinition("al2", "Fire", R.raw.alarm_fire),
        SoundDefinition("al3", "Retro", R.raw.alarm_retro),
        SoundDefinition("al4", "Imperial", R.raw.imperial_alarm),
        SoundDefinition("al5", "Loud alarm", R.raw.alarm_loud),

    )
    private val motivational = listOf(
        SoundDefinition("ai1", "The German", R.raw.motivational_speech_german),
        SoundDefinition("ai2", "Soldier", R.raw.motivational_speech_soldier),
        SoundDefinition("ai3", "Pirate", R.raw.motivational_speech_pirat),
        SoundDefinition("ai4", "Rise & Shine", R.raw.motivational_grind),

    )
    private val Funny = listOf(
        SoundDefinition("f1", "Opening Bier", R.raw.bierflasche_offnen),
        SoundDefinition("f2", "it rattles", R.raw.es_rappelt_im_karton),
        SoundDefinition("f3", "Take your phone", R.raw.geh_mal_an_dein_handy_ran),
        SoundDefinition("f4", "Pipi Langstrumpf", R.raw.hey_pippi_langstrumpf_remix),
        SoundDefinition("f5", "Is here no one?", R.raw.ist_da_keiner),
        SoundDefinition("f6", "Benny Hill", R.raw.lustiger_benny_hill),
        SoundDefinition("f7", "Raumschiff Surprise", R.raw.raumschiff_suprise_space_taxi),
        SoundDefinition("f8", "stairway fart", R.raw.stairway_fart_wars),
        SoundDefinition("f9", "Car Wash", R.raw.funny_car_wash),
        SoundDefinition("f10", "Car Explosion", R.raw.funny_car_explosion),
        SoundDefinition("f11", "Road Construction", R.raw.funny_road_construction),
    )
    private val abstractSounds = listOf(
        SoundDefinition("ab1", "Made by laurens1", R.raw.abstract_ringtone_1),
        SoundDefinition("ab2", "Made by laurens2", R.raw.abstract_ringtone_2),
        SoundDefinition("ab3", "Made by laurens3", R.raw.abstract_ringtone_3),
        SoundDefinition("ab4", "Made by laurens4", R.raw.abstract_ringtone_4),
        SoundDefinition("ab5", "Made by laurens5", R.raw.abstract_ringtone_5),
        SoundDefinition("ab6", "Made by laurens6", R.raw.abstract_ringtone_6),
        SoundDefinition("ab7", "Made by laurens7", R.raw.abstract_ringtone_7),
        SoundDefinition("ab8", "Made by laurens8", R.raw.abstract_ringtone_8),
        SoundDefinition("ab9", "Made by laurens9", R.raw.abstract_ringtone_9),
        SoundDefinition("ab10", "Made by laurens10", R.raw.abstract_ringtone_10),
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
            id = "motivational", title = "Motivational Speech", subtitle = "",
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
            id = "funny", title = "Funny", subtitle = "Funny Sounds",
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
            "motivational" -> motivational
            "funny" -> Funny
            "abstract" -> abstractSounds
            else -> emptyList()
        }
    }

    data class SoundDefinition(val id: String, val title: String, val resId: Int)
}