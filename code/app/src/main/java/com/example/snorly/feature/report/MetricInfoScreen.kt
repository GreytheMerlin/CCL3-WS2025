package com.example.snorly.feature.report

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.snorly.core.common.components.MainTopBar

@Composable
fun MetricInfoScreen(metricType: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val info = when (metricType) {
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

            Text(
                "Recommendations",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            info.recommendations.forEach { rec ->
                Row(Modifier.padding(vertical = 4.dp)) {
                    Text("•", color = Color(0xFFFCDC5F), modifier = Modifier.padding(end = 8.dp))
                    Text(rec, color = Color.Gray, fontSize = 14.sp)
                }
            }


            Spacer(Modifier.height(32.dp))

            // PRO TIPS SECTION
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFCDC5F).copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            null,
                            tint = Color(0xFFFCDC5F),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Snorly Pro Tips",
                            color = Color(0xFFFCDC5F),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    info.proTips.forEach { tip ->
                        Text(
                            "• $tip",
                            color = Color.White.copy(0.9f),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }

//            Spacer(Modifier.height(32.dp))
//
//            // --- DEEP DIVE RESOURCES ---
//            Text("Deep Dive Resources", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
//            Spacer(Modifier.height(8.dp))
//            info.resources.forEach { (label, url) ->
//                Card(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 6.dp)
//                        .clickable {
//                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//                            context.startActivity(intent)
//                        },
//                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
//                ) {
//                    Row(
//                        modifier = Modifier.padding(16.dp),
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.SpaceBetween
//                    ) {
//                        Text(label, color = Color(0xFF4A90E2), fontSize = 14.sp, fontWeight = FontWeight.Medium)
//                        Icon(Icons.Default.OpenInNew, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
//                    }
//                }
//            }
//            Spacer(Modifier.height(40.dp))
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