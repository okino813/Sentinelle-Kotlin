package com.example.sentinelle

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.sentinelle.api.AppValues
import com.example.sentinelle.api.api_service
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices


class TimerService : Service() {

    private var timer: CountDownTimer? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }



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

        var secondsElapsed = 0

        timer = object : CountDownTimer(totalSeconds * 1000L, 1_000L) {
            override fun onTick(millisUntilFinished: Long) {
                secondsElapsed++

                // Action chaque seconde
                var heureRestant = millisUntilFinished / 3600_000
                var minuteRestant = (millisUntilFinished % 3600_000) / 60_000
                var secondeRestant = (millisUntilFinished % 60_000) / 1000

                AppValues.hour.value = heureRestant.toInt()
                AppValues.minute.value = minuteRestant.toInt()
                AppValues.seconde.value = secondeRestant.toInt()


                Log.d("TimerService", "Heure: $heureRestant, Minute: $minuteRestant, Seconde: $secondeRestant")

                if(secondsElapsed % 10 == 0) {
                    // Action toutes les 10 secondes
                    Log.d("TimerService", "Action toutes les 10 secondes")
                    getLastLocation()

                }

                if (secondsElapsed % 300 == 0) {
                    // Action toutes les 5 minutes
                    Log.d("TimerService", "Action toutes les 5 minutes")
                }
            }


            override fun onFinish() {
                clearTimerState()
                stopSelf()
                timer = null
            }
        }.start()
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w("TimerService", "Permissions de localisation manquantes")
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                Log.d("TimerService", "Localisation → lat: ${location.latitude}, lng: ${location.longitude}")
                // Tu peux sauvegarder ou envoyer la position ici
                var api = api_service(this)
                api.sendLocation(
                    context = this,
                    latitude = location.latitude.toString(),
                    longitude = location.longitude.toString()
                ) { success ->
                    if (success) {
                        Log.d("TimerService", "Localisation envoyée avec succès")
                    } else {
                        Log.e("TimerService", "Erreur lors de l'envoi de la localisation")
                    }
                }
            } else {
                Log.d("TimerService", "Localisation non disponible")
            }
        }
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