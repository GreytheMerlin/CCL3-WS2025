package com.example.snorly.feature.alarm.wakeup

import android.R
import android.app.*
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.example.snorly.core.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.snorly.feature.alarm.ToneGenerator.Instrument
import com.example.snorly.feature.alarm.ToneGenerator.ToneGenerator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class AlarmRingingService : Service() {

    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    // Job to control the custom sequencer loop
    private var composedPlaybackJob: Job? = null
    private var isServiceRunning = false

    // Create a scope for database operations
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        createChannel()
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isServiceRunning = true
        val alarmId = intent?.getLongExtra("alarm_id", -1L) ?: -1L

        // Full-screen activity intent
        val fsIntent = Intent(this, AlarmRingingActivity::class.java).apply {
            putExtra("alarm_id", alarmId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val fsPendingIntent = PendingIntent.getActivity(
            this,
            1000 + alarmId.toInt(),
            fsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Alarm")
            .setContentText("Tap to open")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fsPendingIntent, true)
            .setContentIntent(fsPendingIntent)
            .setOngoing(true)
            .build()

        startForeground(NOTIF_ID, notif)

        // Launch UI immediately (some OEMs need both)
        startActivity(fsIntent)

        // Start ringing (use your stored ringtone later; here: default alarm sound)
        startActivity(fsIntent)

        // 4. Fetch Sound & Play asynchronously
        serviceScope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    if (alarmId != -1L) {
                        val db = AppDatabase.getDatabase(applicationContext)
                        val alarm = db.alarmDao().getById(alarmId)
                        val uriString = alarm?.ringtoneUri

                        if (uriString?.startsWith("composed:") == true) {
                            // Case A: Custom Composed Ringtone
                            val id = uriString.removePrefix("composed:").toLongOrNull()
                            if (id != null) {
                                // Fetch the note sequence from the other table
                                val sequence = db.composedRingtoneDao().getById(id)?.noteSequence
                                PlaybackData.Composed(sequence)
                            } else {
                                PlaybackData.Standard(null)
                            }
                        } else {
                            // Case B: Standard File
                            PlaybackData.Standard(uriString?.let { Uri.parse(it) })
                        }
                    } else {
                        PlaybackData.Standard(null)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    PlaybackData.Standard(null)
                }
            }

            // 5. Start Appropriate Player
            when (result) {
                is PlaybackData.Composed -> startComposedRinging(result.sequence)
                is PlaybackData.Standard -> startRinging(result.uri ?: Settings.System.DEFAULT_ALARM_ALERT_URI)
            }
        }

        startVibration()

        return START_NOT_STICKY
    }

    private fun startRinging(uri: Uri) {
        try {
            player?.release()
            player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(this@AlarmRingingService, uri)
                isLooping = true
                prepare()
                start()
            }
        } catch (_: Exception) {
            // If anything fails, don't crash the alarm
            // Fallback: If specific file fails (e.g. deleted), try default
            if (uri != Settings.System.DEFAULT_ALARM_ALERT_URI) {
                startRinging(Settings.System.DEFAULT_ALARM_ALERT_URI)
            }
        }
    }

    // --- SEQUENCER (For Composed Songs) ---
    private suspend fun startComposedRinging(sequenceString: String?) {
        // Fallback if compose was deleted
        if (sequenceString.isNullOrBlank()) {
            startRinging(Settings.System.DEFAULT_ALARM_ALERT_URI)
            return
        }

        // Parse Notes
        val notes = try {
            sequenceString.split(";").mapNotNull { part ->
                val segments = part.split("|")
                if (segments.size == 3) {
                    Triple(
                        Instrument.valueOf(segments[0]),
                        segments[1].toDouble(),
                        segments[2].toLong()
                    )
                } else null
            }.sortedBy { it.third }
        } catch (e: Exception) {
            emptyList()
        }

        if (notes.isEmpty()) {
            startRinging(Settings.System.DEFAULT_ALARM_ALERT_URI)
            return
        }

        // Loop the sequence indefinitely
        composedPlaybackJob?.cancel()
        composedPlaybackJob = serviceScope.launch(Dispatchers.Default) {
            while (isActive && isServiceRunning) {
                val loopStart = System.currentTimeMillis()

                for (note in notes) {
                    if (!isActive) break
                    val (instrument, freq, offset) = note

                    // Timing Logic
                    val targetTime = loopStart + offset
                    val waitTime = targetTime - System.currentTimeMillis()
                    if (waitTime > 0) delay(waitTime)

                    // Play note (Fire-and-forget to allow chords)
                    launch { ToneGenerator.playNote(freq, instrument) }
                }

                // Wait 2 seconds before looping again
                delay(2000)
            }
        }
    }

    private fun startVibration() {
        val vib = vibrator ?: return
        val pattern = longArrayOf(0, 600, 400) // on/off repeating
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vib.vibrate(pattern, 0)
        }
    }

    override fun onDestroy() {
        isServiceRunning = false
        // STOP SEQUENCER
        composedPlaybackJob?.cancel()

        // STOP MEDIA PLAYER
        player?.stop()
        player?.release()
        player = null

        // STOP VIBRATOR
        vibrator?.cancel()
        // CANCEL SCOPE
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val mgr = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Alarms",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alarm notifications"
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        mgr.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "alarm_channel"
        private const val NOTIF_ID = 42
    }

    // Helper Sealed Class for Playback Mode
    private sealed class PlaybackData {
        data class Standard(val uri: Uri?) : PlaybackData()
        data class Composed(val sequence: String?) : PlaybackData()
    }
}
