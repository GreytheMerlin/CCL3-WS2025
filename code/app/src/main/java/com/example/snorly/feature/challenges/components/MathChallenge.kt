package com.example.snorly.feature.challenges.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun MathChallengeScreen(
    onSolved: () -> Unit
) {
    // generate once
    val a = remember { Random.nextInt(10, 60) }
    val b = remember { Random.nextInt(10, 60) }
    val answer = remember { a + b }

    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    val bg = Brush.verticalGradient(
        listOf(Color(0xFF0B0F1A), Color(0xFF060812))
    )

    Surface(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(bg)
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Solve to Dismiss",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )

                Spacer(Modifier.height(80.dp))

                Text(
                    text = "$a + $b = ?",
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White
                )

                Spacer(Modifier.height(32.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(92.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0x22FFFFFF)
                    )
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (input.isEmpty()) " " else input,
                            style = MaterialTheme.typography.displaySmall,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (error) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Wrong answer. Try again.",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(Modifier.weight(1f))

                Keypad(
                    onDigit = { d ->
                        if (input.length < 6) {
                            input += d
                            error = false
                        }
                    },
                    onBackspace = {
                        if (input.isNotEmpty()) input = input.dropLast(1)
                        error = false
                    },
                    onConfirm = {
                        val v = input.toIntOrNull()
                        if (v == answer) onSolved() else error = true
                    }
                )
            }
        }
    }
}

@Composable
private fun Keypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onConfirm: () -> Unit
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("⌫", "0", "✓")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                row.forEach { label ->
                    KeypadButton(
                        label = label,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            when (label) {
                                "⌫" -> onBackspace()
                                "✓" -> onConfirm()
                                else -> onDigit(label)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isConfirm = label == "✓"
    val bg = if (isConfirm) Color(0xFF0B4AA2) else Color(0x22FFFFFF)

    Box(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
        )
    }
}
