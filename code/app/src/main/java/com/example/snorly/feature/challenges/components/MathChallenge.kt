
@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.snorly.feature.alarm.components




import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardBackspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import kotlin.random.Random

@Composable
fun SolveChallengeScreen(
    modifier: Modifier = Modifier,
    title: String = "Solve to Dismiss",
    equation: String = "48 + 14 = ?",
    value: String,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onClose: () -> Unit,
    maxDigits: Int = 4,
) {
    Box(modifier = modifier.fillMaxSize()) {
        StarryGradientBackground()

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopBar(title = title, onClose = onClose)

            Spacer(Modifier.height(54.dp))

            Text(
                text = equation,
                color = Color.White,
                fontSize = 56.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = (-1).sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(22.dp))

            AnswerDisplay(
                text = value.ifBlank { " " },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(90.dp))

            Keypad(
                onDigit = { d ->
                    if (value.length < maxDigits) onValueChange(value + d)
                },
                onBackspace = {
                    if (value.isNotEmpty()) onValueChange(value.dropLast(1))
                },
                onConfirm = onConfirm
            )

            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun TopBar(
    title: String,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.95f),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.weight(1f))

        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = Color.White.copy(alpha = 0.95f),
            modifier = Modifier
                .size(28.dp)
                .clickable(onClick = onClose)
                .padding(2.dp)
        )
    }
}

@Composable
private fun AnswerDisplay(
    text: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(22.dp)

    Box(
        modifier = modifier
            .height(96.dp)
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color.White.copy(alpha = 0.06f)
                    ),
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.10f),
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 42.sp,
            fontWeight = FontWeight.Light
        )
    }
}

@Composable
private fun Keypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onConfirm: () -> Unit
) {
    val spacing = 16.dp
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
            KeypadButton(text = "1", modifier = Modifier.weight(1f)) { onDigit("1") }
            KeypadButton(text = "2", modifier = Modifier.weight(1f)) { onDigit("2") }
            KeypadButton(text = "3", modifier = Modifier.weight(1f)) { onDigit("3") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
            KeypadButton(text = "4", modifier = Modifier.weight(1f)) { onDigit("4") }
            KeypadButton(text = "5", modifier = Modifier.weight(1f)) { onDigit("5") }
            KeypadButton(text = "6", modifier = Modifier.weight(1f)) { onDigit("6") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
            KeypadButton(text = "7", modifier = Modifier.weight(1f)) { onDigit("7") }
            KeypadButton(text = "8", modifier = Modifier.weight(1f)) { onDigit("8") }
            KeypadButton(text = "9", modifier = Modifier.weight(1f)) { onDigit("9") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
            KeypadIconButton(
                icon = Icons.AutoMirrored.Filled.KeyboardBackspace,
                contentDescription = "Backspace",
                modifier = Modifier.weight(1f),
                containerColor = Color.White.copy(alpha = 0.10f),
                iconTint = Color.White
            ) { onBackspace() }

            KeypadButton(text = "0", modifier = Modifier.weight(1f)) { onDigit("0") }

            KeypadIconButton(
                icon = Icons.Default.Check,
                contentDescription = "Confirm",
                modifier = Modifier.weight(1f),
                containerColor = Color(0xFF0D3B7A), // deep blue like screenshot
                iconTint = Color.White
            ) { onConfirm() }
        }
    }
}

@Composable
private fun KeypadButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    FrostedKey(
        modifier = modifier,
        onClick = onClick
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun KeypadIconButton(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    containerColor: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    FrostedKey(
        modifier = modifier,
        onClick = onClick,
        containerColor = containerColor
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
private fun FrostedKey(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    containerColor: Color = Color.White.copy(alpha = 0.10f),
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .aspectRatio(1.25f) // gives that rounded-square key size feeling
            .clip(shape)
            .background(containerColor)
            .border(1.dp, Color.White.copy(alpha = 0.08f), shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
private fun StarryGradientBackground() {
    // Background gradient close to the screenshot (deep navy to darker)
    val bg = Brush.radialGradient(
        colors = listOf(
            Color(0xFF1B1E3A),
            Color(0xFF0B0C17),
            Color(0xFF05050A)
        ),
        center = Offset(0.5f, 0.15f),
        radius = 1200f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .drawWithCache {
                val w = size.width
                val h = size.height

                val rnd = kotlin.random.Random(42)
                val count = 140

                val stars = List(count) {
                    val x = rnd.nextFloat() * w
                    val y = rnd.nextFloat() * h
                    val r = rnd.nextFloat() * 2.2f + 0.6f
                    val a = rnd.nextFloat() * 0.55f + 0.10f
                    Star(x, y, r, a)
                }

                onDrawBehind {
                    stars.forEach { s ->
                        drawCircle(
                            color = Color.White.copy(alpha = s.alpha),
                            radius = s.radius,
                            center = Offset(s.x, s.y)
                        )
                    }
                }
            }
    )
}

private data class Star(val x: Float, val y: Float, val radius: Float, val alpha: Float)
