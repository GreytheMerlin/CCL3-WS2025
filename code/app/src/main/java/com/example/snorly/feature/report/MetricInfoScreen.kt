package com.example.snorly.feature.report

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.snorly.core.common.components.MainTopBar

@Composable
fun MetricInfoScreen(metricType: String, onBack: () -> Unit) {
    val info = when(metricType) {
        "consistency" -> ExplainerProvider.consistency
        else -> ExplainerProvider.sleepScore
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = { MainTopBar(title = info.title, onActionClick = onBack) } // Reuse your back logic
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            InfoSection("How it's Calculated", info.scoreLogic, Icons.Default.Rule)
            Spacer(Modifier.height(24.dp))
            InfoSection("Medical Insight", info.medicalInsight, Icons.Default.Favorite)
            Spacer(Modifier.height(24.dp))

            Text("Recommendations", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            info.recommendations.forEach { rec ->
                Row(Modifier.padding(vertical = 4.dp)) {
                    Text("•", color = Color(0xFFFCDC5F), modifier = Modifier.padding(end = 8.dp))
                    Text(rec, color = Color.Gray, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun InfoSection(title: String, body: String, icon: ImageVector) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color(0xFFFCDC5F), modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Spacer(Modifier.height(8.dp))
        Text(body, color = Color.Gray, fontSize = 15.sp, lineHeight = 22.sp)
    }
}