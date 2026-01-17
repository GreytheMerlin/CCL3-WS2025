package com.example.snorly.core.ui.shapes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.circle
import androidx.graphics.shapes.rectangle
import androidx.graphics.shapes.star

// 1. SCALLOP (Cloud/Flower) - For Nature/Abstract
@Composable
fun rememberScallop(): RoundedPolygon {
    return remember {
        RoundedPolygon.star(
            numVerticesPerRadius = 12,
            innerRadius = 0.92f,
            rounding = CornerRounding(radius = 0.6f)
        )
    }
}

// 2. SHIELD (Triangle/Badge) - For Alarms/Warnings
@Composable
fun rememberShield(): RoundedPolygon {
    return remember {
        RoundedPolygon(
            numVertices = 3,
            rounding = CornerRounding(radius = 0.2f),
            centerX = 0.5f, centerY = 0.55f // Slight offset to center visual weight
        )
    }
}

// 3. HEXAGON (Tech) - For Composer
@Composable
fun rememberHexagon(): RoundedPolygon {
    return remember {
        RoundedPolygon(
            numVertices = 6,
            rounding = CornerRounding(radius = 0.2f)
        )
    }
}

// 4. PILL (Capsule) - For Spotify/Modern
@Composable
fun rememberPill(): RoundedPolygon {
    return remember {
        RoundedPolygon.star(
            numVerticesPerRadius = 4,
            innerRadius = 0.65f, // Squeezed box
            rounding = CornerRounding(radius = 0.8f)
        )
    }
}

// 5. STAR (Playful) - For Animals
@Composable
fun rememberSoftStar(): RoundedPolygon {
    return remember {
        RoundedPolygon.star(
            numVerticesPerRadius = 5,
            innerRadius = 0.6f,
            rounding = CornerRounding(radius = 0.3f)
        )
    }
}

@Composable
fun rememberRoundedRect(): RoundedPolygon {
    return remember {
        RoundedPolygon.rectangle(
            rounding = CornerRounding(radius = 0.1f, smoothing = 1f) // Relative radius (0.1 = 10% of size)
        )
    }
}

@Composable
fun rememberRoundedStar(): RoundedPolygon {
    return remember {
        RoundedPolygon.star(
            numVerticesPerRadius = 8,
            innerRadius = 0.7f,
            rounding = CornerRounding(radius = 0.2f)
        )
    }
}

@Composable
fun rememberCircle(): RoundedPolygon {
    return remember {
        RoundedPolygon.circle(numVertices = 8)
    }
}