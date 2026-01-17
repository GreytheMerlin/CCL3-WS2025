package com.example.snorly.feature.alarm.components

import androidx.compose.runtime.Composable
import androidx.graphics.shapes.RoundedPolygon
import com.example.snorly.core.ui.shapes.rememberCircle
import com.example.snorly.core.ui.shapes.rememberHexagon
import com.example.snorly.core.ui.shapes.rememberPill
import com.example.snorly.core.ui.shapes.rememberRoundedRect
import com.example.snorly.core.ui.shapes.rememberScallop
import com.example.snorly.core.ui.shapes.rememberShield
import com.example.snorly.core.ui.shapes.rememberSoftStar
import com.example.snorly.feature.alarm.model.ShapeType

@Composable
fun getShapeForType(type: ShapeType): RoundedPolygon {
    return when (type) {
        ShapeType.ROUNDED_RECT -> rememberRoundedRect()
        ShapeType.CIRCLE -> rememberCircle()
        ShapeType.SCALLOP -> rememberScallop()
        ShapeType.SHIELD -> rememberShield()
        ShapeType.HEXAGON -> rememberHexagon()
        ShapeType.PILL -> rememberPill()
        ShapeType.STAR -> rememberSoftStar()
    }
}