package com.example.snorly.feature.challenges.memory
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MemoryMatchGameScreen(
    modifier: Modifier = Modifier,
    uiState: MemoryMatchUiState,
    columns: Int,
    onCardTap: (Int) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0B0B14), Color(0xFF0D1020), Color(0xFF070812))
                )
            )
            .padding(16.dp)
    ) {
        Column(Modifier.fillMaxSize()) {

            TopBar(
                pairsFound = uiState.pairsFound,
                totalPairs = uiState.totalPairs,
                moves = uiState.moves

            )

            Spacer(Modifier.height(16.dp))

            MessagePill(text = uiState.message)

            Spacer(Modifier.height(24.dp))

            MemoryGrid(
                cards = uiState.cards,
                columns = columns,
                enabled = !uiState.inputLocked && !uiState.isMemorizePhase,
                onTap = onCardTap
            )
        }
    }
}

@Composable
private fun TopBar(pairsFound: Int, totalPairs: Int, moves: Int) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            "Memory Match",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )

    }

    Spacer(Modifier.height(12.dp))

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatBlock(value = "$pairsFound/$totalPairs", label = "Pairs")
        StatBlock(value = moves.toString(), label = "Moves")
    }
}

@Composable
private fun StatBlock(value: String, label: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(value, color = Color.White, fontWeight = FontWeight.Bold)
        Text(label, color = Color.White.copy(alpha = 0.65f), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun MessagePill(text: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF7C3AED).copy(alpha = 0.18f))
            .border(1.dp, Color(0xFFB794F4).copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Text(text, color = Color.White.copy(alpha = 0.90f))
    }
}

@Composable
private fun MemoryGrid(
    cards: List<MemoryCard>,
    columns: Int,
    enabled: Boolean,
    onTap: (Int) -> Unit
) {
    val rows = (cards.size + columns - 1) / columns
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        for (r in 0 until rows) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                for (c in 0 until columns) {
                    val idx = r * columns + c
                    if (idx < cards.size) {
                        MemoryCardView(
                            card = cards[idx],
                            enabled = enabled,
                            onClick = { onTap(cards[idx].id) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun MemoryCardView(
    card: MemoryCard,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val faceUp = card.isFaceUp || card.isMatched
    val scale by animateFloatAsState(if (card.isMatched) 0.98f else 1.0f, label = "matchScale")
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier
            .aspectRatio(1f)
            .scale(scale)
            .clip(shape)
            .then(
                if (faceUp) {
                    Modifier.background(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF2B1452), Color(0xFF1A1236))
                        ),
                        shape = shape
                    )
                } else {
                    Modifier.background(
                        color = Color.White.copy(alpha = 0.06f),
                        shape = shape
                    )
                }
            )
            .border(
                1.dp,
                if (faceUp) Color(0xFFB794F4).copy(alpha = 0.35f) else Color.White.copy(alpha = 0.12f),
                shape
            )
            .clickable(enabled = enabled && !card.isMatched, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(targetState = faceUp, label = "flipContent") { up ->
            if (up) {
                Text(card.content, style = MaterialTheme.typography.headlineMedium, color = Color.White)
            } else {
                Text("✦", style = MaterialTheme.typography.headlineMedium, color = Color.White.copy(alpha = 0.55f))
            }
        }
    }
}