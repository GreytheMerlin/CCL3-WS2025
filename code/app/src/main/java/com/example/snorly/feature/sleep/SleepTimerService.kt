package com.example.snorly.feature.sleep


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.snorly.MainActivity
import com.example.snorly.R

class SleepTimerService : Service() {

    companion object {
        const val CHANNEL_ID = "sleep_timer"
        const val NOTIF_ID = 9001

        const val ACTION_START = "sleep.timer.START"
        const val ACTION_STOP = "sleep.timer.STOP"
    }

    private val nm by lazy { getSystemService(NotificationManager::class.java) }
    private val handler by lazy { Handler(mainLooper) }

    private var startMs: Long = 0L
    private var running = false

    private val ticker = object : Runnable {
        override fun run() {
            if (!running) return
            val elapsed = SystemClock.elapsedRealtime() - startMs
            nm.notify(NOTIF_ID, buildNotification(formatElapsed(elapsed)))
            handler.postDelayed(this, 1000L) // update every 1s
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopTimer()
                return START_NOT_STICKY
            }
            ACTION_START, null -> {
                startTimer()
                return START_STICKY
            }
            else -> return START_STICKY
        }
    }

    private fun startTimer() {

        Log.d("trackestart", "${running}")
        if (running) return
        running = true
        startMs = SystemClock.elapsedRealtime()

        ensureChannel()
        startForeground(NOTIF_ID, buildNotification("00:00"))
        handler.post(ticker)
    }

    private fun stopTimer() {
        running = false
        handler.removeCallbacks(ticker)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        running = false
        handler.removeCallbacks(ticker)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(timeText: String): Notification {
        val openAppPI = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopPI = PendingIntent.getService(
            this,
            1,
            Intent(this, SleepTimerService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val small = RemoteViews(packageName, R.layout.notif_sleep_small).apply {
            setTextViewText(R.id.tv_time, timeText)
        }


        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sleep) // <- create/replace this icon
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openAppPI)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(small)
            .build()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sleep Timer",
                NotificationManager.IMPORTANCE_LOW
            )
            nm.createNotificationChannel(channel)
        }
    }

    private fun formatElapsed(ms: Long): String {
        val totalSeconds = ms / 1000
        val s = (totalSeconds % 60).toInt()
        val m = ((totalSeconds / 60) % 60).toInt()
        val h = (totalSeconds / 3600).toInt()

        return if (h > 0) "%d:%02d:%02d".format(h, m, s)
        else "%02d:%02d".format(m, s)
    }
}