package com.example.snorly.feature.report

import androidx.compose.ui.graphics.Color

data class WeeklyStats(
    val avgDuration: String, // "7h 30m"
    val avgScore: Int,          // 85
    val avgBedtime: String,     // "23:15"
    val avgWakeup: String,       // "07:10"
)

// Updated Data Class
data class ComparisonResult(
    val recentAvgHours: Double,
    val olderAvgHours: Double,
    val diffMinutes: Int, // e.g. +30 or -15
    val diffScore: Int = 0, // Placeholder for quality score diff
    val recentAvgScore: Int,
    val olderAvgScore: Int
)

data class ConsistencyResult(
    val overallScore: Int,
    val avgBedtimeOffsetMin: Long,
    val avgWakeupOffsetMin: Long,
    val bedtimeColor: Color,
    val wakeupColor: Color,
    val targetBedFormatted: String,
    val targetWakeFormatted: String,
    val label: String,
    val color: Color
)

data class MetricExplainer(
    val title: String,
    val scoreLogic: String,
    val medicalInsight: String,
    val recommendations: List<String>,
    val proTips: List<String>,
    val resources: List<Pair<String, String>> // Label to URL
)

object ExplainerProvider {
    val sleepScore = MetricExplainer(
        title = "Snorly Sleep Score",
        scoreLogic = """
            Snorly uses a 100-point scale based on the 'Golden 8' benchmark (480 minutes of actual sleep). 
            
            • Duration (Primary): You earn points for every minute slept up to 8 hours. 
            • Efficiency: If your device provides Sleep Stages, we analyze the ratio of Deep and REM sleep.
            • Consistency: Frequent wake-ups or 'Out of Bed' events detected by Health Connect will apply a penalty to your final score.
            
            A score of 100 means you hit the perfect recovery window for your brain and body.
        """.trimIndent(),
        medicalInsight = """
            Sleep isn't just 'down time.' It is an active metabolic process. 
            
            During Deep Sleep, your brain's Glymphatic System opens up, physically washing away neurotoxic waste products (like beta-amyloid) that accumulate during the day. 
            
            Without reaching a high score, your brain effectively starts the next day 'unwashed,' leading to the common feeling of brain fog.
        """.trimIndent(),
        recommendations = listOf(
            "The 480-Min Benchmark: Total time in bed is not time asleep. Aim for 8.5 hours in bed to reach a 480-minute sleep goal.",
            "Caffeine Half-Life: Caffeine has a 6-hour half-life. To protect your Deep Sleep architecture, stop caffeine intake 10 hours before your target bedtime.",
            "Thermal Trigger: Core temperature must drop to initiate sleep. A hot shower 90 mins before bed actually cools the core by drawing blood to the surface.",
            "Digital Sunset: Shift your environment to warm, low-level lighting 2 hours before bed. This signals the pineal gland to begin natural melatonin synthesis."
        ),
        proTips = listOf(
            "Morning Sunlight: View direct sunlight (not through a window) for 10-30 mins within an hour of waking. This sets a timer for melatonin production 16 hours later.",
            "Selective Blue Blocking: Avoid blue-light glasses during the day. You need high-intensity blue light from the sun to stay alert. Use them ONLY after sunset.",
            "No 'Snoozing': The sleep you get after hitting snooze is fragmented and high in cortisol. It’s better to set your alarm for the latest possible time.",
            "View the Sunset: Seeing the low-angle sun in the evening adjusts the sensitivity of your retinas, making you less susceptible to blue light later that night."
        ),
        resources = listOf(
            "Sleep Foundation: What is Sleep Quality?" to "https://www.sleepfoundation.org/sleep-hygiene/how-is-sleep-quality-calculated",
            "The Glymphatic System Explained" to "https://www.nih.gov/news-events/news-releases/brain-may-flush-waste-during-sleep"
        )
    )

    val consistency = MetricExplainer(
        title = "Circadian Consistency",
        scoreLogic = """
            Snorly calculates this using 'Phase Anchoring.' We measure the variance in your Bedtime and Wakeup times against your set targets. 
            
            Every 15 minutes of deviation from your 'Anchor Time' results in a score reduction. The brain is an anticipatory organ; it begins secretal hormonal 'pre-flight' checks 90 minutes before your expected wakeup. If your timing is inconsistent, the brain cannot prepare, resulting in 'Sleep Inertia' (morning grogginess).
        """.trimIndent(),
        medicalInsight = """
            Your master biological clock, the Suprachiasmatic Nucleus (SCN), relies on consistency to regulate everything from insulin sensitivity to testosterone and cortisol production. 
            
            Varying your sleep times by more than 60 minutes between weekdays and weekends creates 'Social Jetlag.' This effectively puts your body in a state of permanent jetlag, even without traveling, which is linked to metabolic syndrome and reduced life expectancy.
        """.trimIndent(),
        recommendations = listOf(
            "The 7-Day Rule: Your brain doesn't have a 'weekend mode.' Try to keep your wakeup time within 30 minutes of your weekday target, even on Saturdays.",
            "Prioritize the Wakeup: If you go to bed late, still wake up at your 'Anchor Time.' It is better to have one short night of sleep than to shift your entire circadian phase.",
            "The Melatonin Bridge: Natural melatonin begins rising 2 hours before your consistent bedtime. If you stay in bright light during this window, you 'reset' the clock, making it harder to fall asleep the next night.",
            "Strategic Napping: If you are sleep-deprived, take a 20-minute 'Power Nap' before 3:00 PM rather than sleeping in the next morning."
        ),
        proTips = listOf(
            "Sunlight is a Drug: You need 10,000 to 100,000 lux (sunlight) to properly suppress melatonin. Standard office lighting is only 500 lux. Get outside within 30 mins of your anchor wakeup.",
            "Ditch the Day-Time Blue Blockers: Do NOT wear blue-light glasses during the day. High-intensity blue light from the sun is required to anchor your SCN and keep your mood stable.",
            "Evening Low-Angle Light: Viewing the sunset triggers a 'circadian dead zone' that makes your eyes less sensitive to the blue light from your phone later that evening.",
            "Temperature Inversion: Your body naturally cools down at night. If you sleep in, you interfere with the natural rise in core temperature required for alertness, leading to a 'heavy' feeling all day."
        ),
        resources = listOf(
            "The Science of Social Jetlag" to "https://www.nature.com/articles/s41598-017-03171-4",
            "Mastering Your Circadian Clock (Huberman Lab)" to "https://www.hubermanlab.com/episode/master-your-sleep-and-be-alert-when-awake",
            "Circadian Rhythm and Metabolic Health" to "https://www.ncbi.nlm.nih.gov/pmc/articles/PMC6123576/"
        )
    )
}