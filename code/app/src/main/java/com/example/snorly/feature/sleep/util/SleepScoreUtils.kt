package com.example.snorly.feature.sleep.util

import androidx.health.connect.client.records.SleepSessionRecord
import java.time.Duration

object SleepScoreUtils {

    fun calculateScore(record: SleepSessionRecord): Int {
        val duration = Duration.between(record.startTime, record.endTime).toMinutes()

        // 1. If we have NO stages (Manual/Simple entry), score based on Duration only
        if (record.stages.isEmpty()) {
            return calculateDurationScore(duration)
        }

        // 2. If we HAVE stages, use the advanced formula
        // Extract stage durations
        var deepMinutes = 0L
        var remMinutes = 0L
        var awakeMinutes = 0L

        record.stages.forEach { stage ->
            val stageDur = Duration.between(stage.startTime, stage.endTime).toMinutes()
            when (stage.stage) {
                SleepSessionRecord.STAGE_TYPE_DEEP -> deepMinutes += stageDur
                SleepSessionRecord.STAGE_TYPE_REM -> remMinutes += stageDur
                SleepSessionRecord.STAGE_TYPE_AWAKE -> awakeMinutes += stageDur
                SleepSessionRecord.STAGE_TYPE_OUT_OF_BED -> awakeMinutes += stageDur
            }
        }

        // --- SCORING ALGORITHM ---
        // Total Score = Duration (40%) + Deep (20%) + REM (20%) + Efficiency (20%)

        // A. Duration Score (Max 40) - Target 7-8.5 hours
        val durationScore = when {
            duration >= 420 -> 40 // 7h+
            duration >= 360 -> 30 // 6h+
            duration >= 300 -> 20 // 5h+
            else -> 10
        }

        // B. Deep Sleep Score (Max 20) - Target 10-20% of total
        val deepPercent = if(duration > 0) (deepMinutes * 100 / duration) else 0
        val deepScore = when {
            deepPercent >= 15 -> 20
            deepPercent >= 10 -> 15
            else -> 5
        }

        // C. REM Sleep Score (Max 20) - Target 20-25% of total
        val remPercent = if(duration > 0) (remMinutes * 100 / duration) else 0
        val remScore = when {
            remPercent >= 20 -> 20
            remPercent >= 15 -> 15
            else -> 5
        }

        // D. Efficiency Score (Max 20) - Penalty for being awake
        val awakePercent = if(duration > 0) (awakeMinutes * 100 / duration) else 0
        val efficiencyScore = when {
            awakePercent <= 5 -> 20  // Very efficient
            awakePercent <= 10 -> 15
            awakePercent <= 15 -> 10
            else -> 5
        }

        return (durationScore + deepScore + remScore + efficiencyScore)
    }

    // Fallback for manual/old devices
    private fun calculateDurationScore(minutes: Long): Int {
        return when {
            minutes >= 450 -> 100 // 7.5h
            minutes >= 420 -> 90  // 7h
            minutes >= 360 -> 75  // 6h
            minutes >= 300 -> 60  // 5h
            else -> 40
        }
    }

    fun getScoreColor(score: Int): androidx.compose.ui.graphics.Color {
        return when {
            score >= 85 -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
            score >= 70 -> androidx.compose.ui.graphics.Color(0xFFFFC107) // Amber
            else -> androidx.compose.ui.graphics.Color(0xFFFF5252)        // Red
        }
    }
}