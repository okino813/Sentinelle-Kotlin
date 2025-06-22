package com.example.sentinelle

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class TimerService : Service() {

    private var timer: CountDownTimer? = null


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START_TIMER" -> {
                val totalSeconds = intent.getIntExtra("totalSeconds", 0)
                startForeground(1, buildNotification()) // ✅ Foreground
                startCountdown(totalSeconds)
            }
            "STOP_TIMER" -> {
                stopCountdown()
            }
        }
        return START_STICKY
    }



    private fun startCountdown(totalSeconds: Int) {
        val endTimestamp = System.currentTimeMillis() + totalSeconds * 1000L
        getSharedPreferences("sentinelle_prefs", MODE_PRIVATE).edit()
            .putBoolean("is_timer_running", true)
            .putLong("end_timestamp", endTimestamp)
            .apply()

        timer = object : CountDownTimer(totalSeconds * 1000L, 10_000L) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d("TimerService", "Il reste ${millisUntilFinished / 1000} secondes")
            }

            override fun onFinish() {
                clearTimerState()
                stopSelf()
                timer = null
            }
        }.start()
    }

    private fun buildNotification(): Notification {
        val channelId = "saferider_timer"
        val channelName = "SafeRider Timer"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(chan)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("SafeRider en cours")
            .setContentText("Minuteur en cours d’exécution…")
            .setSmallIcon(R.drawable.main_icon_dark)
            .build()
    }

    private fun clearTimerState() {
        getSharedPreferences("sentinelle_prefs", MODE_PRIVATE).edit()
            .putBoolean("is_timer_running", false)
            .remove("end_timestamp")
            .apply()
    }

    private fun stopCountdown() {
        timer?.cancel()
        timer = null
        clearTimerState()
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}