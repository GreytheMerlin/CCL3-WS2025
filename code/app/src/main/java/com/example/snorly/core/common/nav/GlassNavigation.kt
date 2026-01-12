package com.example.snorly.core.common.nav

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

@Composable
fun GlassBottomBar(
    navController: NavHostController,
    hazeState: HazeState
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // 1. Outer Box: Handles positioning (Floating off the bottom)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp) // Spacing from screen edges
            .height(64.dp) // Height of the bar
    ) {
        // 2. Inner Box: The actual Glass Capsule
        Box(
            modifier = Modifier
                .fillMaxSize()
                // CRITICAL: Clip to shape FIRST so Haze knows the boundary
                .clip(RoundedCornerShape(100))
                // CRITICAL: Apply hazeChild HERE, passing the matching shape
                .hazeChild(
                    state = hazeState,
                    shape = RoundedCornerShape(100),
                    style = HazeStyle(
                        tint = MaterialTheme.colorScheme.background.copy(alpha = 0.6f), // Adjust tint here
                        blurRadius = 20.dp,
                        noiseFactor = HazeDefaults.noiseFactor,
                    )
                )
                // Border sits on top of the blur
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.3f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    ),
                    shape = RoundedCornerShape(100)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Destination.entries.forEach { destination ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == destination.route } == true

                    GhostNavItem(
                        item = destination,
                        isSelected = isSelected,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GhostNavItem(
    item: Destination,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Animation for the glow opacity
    val glowAlpha by animateFloatAsState(
        targetValue = if (isSelected) 0.6f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "glow_alpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(64.dp) // Hit target size
            .clickable(
                indication = null, // No ripple
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
    ) {
        // 4. The Glow Effect (Behind the icon)
        // This is a radial gradient that only appears when selected
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), // Center (Bright)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.0f)  // Edge (Transparent)
                        )
                    ),
                    alpha = glowAlpha // Fades in/out
                )
        )

        // 5. The Icon
        Icon(
            painter = painterResource(id = item.iconRes),
            contentDescription = item.contentDescription,
            // 6. Icon Color Logic:
            // Selected = Primary Color (MoonYellow)
            // Unselected = White/Grey
            tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(24.dp)
        )
    }
}