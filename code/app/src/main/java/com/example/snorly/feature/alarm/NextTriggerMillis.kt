package com.example.snorly.feature.alarm

import java.util.Calendar

fun nextTriggerMillis(
    hour: Int,
    minute: Int,
    days: List<Int>,
    now: Calendar = Calendar.getInstance()
): Long {
    val base = (now.clone() as Calendar).apply {
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }

    fun isSelected(calendarDay: Int): Boolean {
        val index = when (calendarDay) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> return false
        }
        return days.getOrNull(index) == 1
    }

    val repeating = days.any { it == 1 }

    // One-time alarm (no repeat days selected)
    if (!repeating) {
        if (base.timeInMillis <= now.timeInMillis) {
            base.add(Calendar.DAY_OF_YEAR, 1)
        }
        return base.timeInMillis
    }

    // Repeating alarm: find next valid day (today → +7)
    for (offset in 0..7) {
        val candidate = (base.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, offset)
        }

        if (
            isSelected(candidate.get(Calendar.DAY_OF_WEEK)) &&
            candidate.timeInMillis > now.timeInMillis
        ) {
            return candidate.timeInMillis
        }
    }

    // Fallback: one week later
    base.add(Calendar.DAY_OF_YEAR, 7)
    return base.timeInMillis
}
