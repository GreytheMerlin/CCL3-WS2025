package com.example.snorly.feature.alarm.wakeup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.snorly.R

@Composable
fun AlarmRingingScreen(
    alarmId: Long,
    onStopRinging: () -> Unit,
    onSnooze: (snoozeMinutes: Int) -> Unit,
    onOpenChallenge: () -> Unit,
) {
    val context = LocalContext.current
    val vm: AlarmRingingViewModel = viewModel(
        factory = AlarmRingingViewModelFactory(context)
    )

    val state by vm.uiState.collectAsState()

    LaunchedEffect(alarmId) {
        vm.init(alarmId)
    }

    when {
        state.loading -> {
            // your loading UI
            LoadingFullScreen()
        }

        state.error != null -> {
            // fallback screen
            RingingScreen(
                timeText = state.timeText,
                showSnooze = false,
                snoozeMinutes = 0,
                hasChallenge = false,
                onDismiss = onStopRinging,
                onDismissWithChallenge = {},
                onSnooze = {}
            )
        }

        else -> {
            RingingScreen(
                timeText = state.timeText,
                showSnooze = state.snoozeMinutes > 0,
                snoozeMinutes = state.snoozeMinutes,
                hasChallenge = state.hasChallenge,
                onDismiss = onStopRinging,
                onDismissWithChallenge = onOpenChallenge,
                onSnooze = { onSnooze(state.snoozeMinutes) }
            )
        }
    }
}

@Composable
private fun RingingScreen(
    timeText: String,
    showSnooze: Boolean,
    snoozeMinutes: Int,
    hasChallenge: Boolean,
    onDismiss: () -> Unit,
    onDismissWithChallenge: () -> Unit,
    onSnooze: () -> Unit
) {
    val bg = Color(0xFF0B0B0B)
    val border = Color(0xFF2A2A2A)
    val text = Color.White

    Surface(modifier = Modifier.fillMaxSize(), color = bg) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp)
        ) {


            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 88.dp, bottom = 190.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Big time
                Text(
                    text = timeText,
                    color = text,
                    fontSize = 88.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-2).sp
                )

                Spacer(Modifier.height(18.dp))

                // "Wake" (italic-ish)
                Text(
                    text = "Wake",
                    color = text,
                    fontSize = 92.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-1).sp
                )

                // Big "UP!" with mascot overlay (like screenshot)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                ) {
                    Text(
                        text = "UP!",
                        color = text,
                        fontSize = 110.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-2).sp
                    )

                    Image(
                        painter = painterResource(R.drawable.snorly_wakeup),
                        contentDescription = null,
                        modifier = Modifier
                            .size(190.dp)
                            .align(Alignment.CenterEnd)
                            .offset(x = (-18).dp, y = 26.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Bottom buttons (pill outline)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Button 1: Start Dismiss Challenge OR Dismiss
                PillOutlineButton(
                    text = if (hasChallenge) "Start Dismiss Challenge" else "Dismiss",
                    onClick = { if (hasChallenge) onDismissWithChallenge() else onDismiss() },
                    borderColor = border
                )

                // Button 2: Snooze
                if (showSnooze) {
                    PillOutlineButton(
                        text = "Snooze (${snoozeMinutes} min)",
                        onClick = onSnooze,
                        borderColor = border
                    )
                }
            }
        }
    }
}

@Composable
private fun PillOutlineButton(
    text: String,
    onClick: () -> Unit,
    borderColor: Color
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color.White
        )
    ) {
        Icon(
            imageVector = Icons.Outlined.Schedule,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color.White
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

@Composable
fun LoadingFullScreen() {
    Surface(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}