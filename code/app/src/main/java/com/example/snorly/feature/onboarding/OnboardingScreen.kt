package com.example.snorly.feature.onboarding

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AlarmOn
import androidx.compose.material.icons.outlined.BatteryAlert
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.example.snorly.feature.alarm.components.premiumBackground
import com.example.snorly.feature.onboarding.components.PagerIndicator
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onFinish: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 5 })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LifecycleResumeEffect(Unit) {
        viewModel.updatePermissionStatus()
        onPauseOrDispose { }
    }

    // Launchers for system settings
    val healthConnectLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract(),
        onResult = { granted: Set<String> ->
            viewModel.updatePermissionStatus()
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .premiumBackground() // Using your grain/shine effect
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = false // Control flow via buttons to ensure permission checks
            ) { pageIndex ->
                OnboardingPageContent(
                    page = getPageData(pageIndex, uiState),
                    uiState = uiState,
                    onAction = { type, isSkip ->
                        handlePermissionAction(type, context, viewModel, healthConnectLauncher, isSkip) {
                            // Move to next page or finish
                            if (pagerState.currentPage < 4) {
                                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                            } else {
                                viewModel.completeOnboarding()
                                onFinish()
                            }
                        }
                    }
                )
            }

            // Bottom Navigation Area
            PagerIndicator(
                pagerState = pagerState,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 48.dp)
            )
        }
    }
}

@Composable
fun OnboardingPageContent(
    page: OnboardingPage,
    uiState: OnboardingUiState,
    onAction: (PermissionType, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Large Themed Icon
        Box(
            modifier = Modifier
                .size(180.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary // MoonYellow
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant, // TextMute
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(64.dp))

        // Primary Action Button
        Button(
            onClick = { onAction(page.permissionType, false) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(text = page.buttonText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        // Skip
        if (page.permissionType != PermissionType.WELCOME) {
            TextButton(onClick = { onAction(page.permissionType, true) }) {
                Text("Maybe later", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}



private fun getPageData(index: Int, uiState: OnboardingUiState) = when (index) {
    0 -> OnboardingPage(
        "Welcome to Snorly",
        "Better mornings start with a reliable wakeup. Let's get your device ready.",
        Icons.Outlined.Bedtime,
        "Get Started",
        PermissionType.WELCOME
    )
    1 -> OnboardingPage(
        "Precise Wakeup",
        "To ensure your alarm sounds at exactly the right time, we need permission to schedule precise alerts.",
        Icons.Outlined.AlarmOn,
        "Allow Alarms",
        PermissionType.EXACT_ALARM
    )
    2 -> OnboardingPage(
        "Wakeup Insurance",
        "Some system settings can silence alarms to save battery. For total reliability, please set Snorly to 'Unrestricted'.",
        Icons.Outlined.BatteryAlert,
        "Fix Battery Settings",
        PermissionType.BATTERY
    )
    3 -> {
        if (uiState.isHealthConnectAvailable) {
            OnboardingPage(
                "Sleep Insights",
                "Sync your sleep data with Health Connect to see trends and improve your consistency score.",
                Icons.Outlined.HealthAndSafety,
                "Connect Health",
                PermissionType.HEALTH_CONNECT
            )
        } else {
            // DISCLAIMER VERSION
            OnboardingPage(
                "Health Connect Unavailable",
                "Your device does not support Health Connect. Sleep insights will be limited to local manual entries only.",
                Icons.Outlined.Block, // Or a warning icon
                "Continue",
                PermissionType.WELCOME // Use WELCOME type to just call onNext()
            )
        }
    }
    else -> OnboardingPage("Ready!", "You're all set for better sleep.", Icons.Outlined.CheckCircle, "Done", PermissionType.WELCOME)
}