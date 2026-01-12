package com.example.snorly.core.common.nav

import androidx.annotation.DrawableRes
import com.example.snorly.R

enum class Destination(
    val route: String,
    val label: String,
    @DrawableRes val iconRes: Int,
    val contentDescription: String
) {
    ALARM("alarm", "Alarm", R.drawable.round_alarm_24, "Alarm"),
    SLEEP("sleep", "Sleep", R.drawable.bedtime_24dp, "Sleep"),
    REPORT("report", "Report", R.drawable.bar_chart_24dp, "Report"),
    SETTINGS("settings", "Settings", R.drawable.settings_24dp, "Settings")
}