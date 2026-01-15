package com.example.snorly.feature.challenges.math

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun MathChallengeScreen(
    state: MathChallengeUiState,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = Brush.verticalGradient(
        listOf(Color(0xFF0B0F1A), Color(0xFF060812))
    )

    Surface(modifier.fillMaxSize()) {
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
                    text = "${state.a} + ${state.b} = ?",
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White
                )

                Spacer(Modifier.height(32.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(92.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x22FFFFFF))
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (state.input.isEmpty()) " " else state.input,
                            style = MaterialTheme.typography.displaySmall,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (state.error) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Wrong answer. Try again.",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(Modifier.weight(1f))

                Keypad(
                    onDigit = onDigit,
                    onBackspace = onBackspace,
                    onConfirm = onConfirm
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
