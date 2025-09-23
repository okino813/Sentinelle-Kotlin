package com.example.sentinelle

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaRecorder
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
import java.io.File

class TimerService : Service() {

    private var timer: CountDownTimer? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: com.google.android.gms.location.LocationCallback

    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var currentAudioFile: File? = null
    private val audioFileQueue = ArrayList<File>()

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Préparer le callback pour les mises à jour de localisation
        locationCallback = object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                for (location in result.locations) {
                    Log.d("TimerService", "Localisation → lat: ${location.latitude}, lng: ${location.longitude}")

                    // Envoyer à l'API
                    val api = api_service(this@TimerService)
                    api.sendLocation(
                        context = this@TimerService,
                        latitude = location.latitude.toString(),
                        longitude = location.longitude.toString()
                    ) { success ->
                        if (success) {
                            Log.d("TimerService", "Localisation envoyée avec succès")
                        } else {
                            Log.e("TimerService", "Erreur lors de l'envoi de la localisation")
                        }
                    }
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w("TimerService", "Permissions de localisation manquantes")
            return
        }

        val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
            10_000L // 10 secondes
        )
            .setPriority(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            mainLooper
        )
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
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
                stopLocationUpdates()

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

        // Démarrer l'enregistrement audio
        startAudioRecording()

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
                    startLocationUpdates()

                }

                if(secondsElapsed % 300 == 0) {
                    // Action toutes les 5 minutes
                    // Ici il faudrait que l'enregistrement audio se coupe. S'enregistre et le path
                    // soit stocker dans un tableau pour l'envoi à l'API
                    // Une fois stocker dans le tableau, on démare un autre enregistrement audio
                    // Le tableau s'envoie à l'api toute les 5 minutes.
                    // Le tableau sert pour si jamais l'utilisateur n'a pas de réseau, les données sont stockées
                    // et envoyées plus tard.
                    handleAudioRecording()
                    sendQueuedAudioFiles()
                    Log.d("TimerService", "Action toutes les 5 minutes")
                }
            }


            override fun onFinish() {
                stopAudioRecording()
                sendQueuedAudioFiles()
                clearTimerState()
                stopSelf()
                timer = null
            }
        }.start()
    }

    private fun startAudioRecording() {
        if (!hasAudioPermission()) {
            Log.w("TimerService", "Permission RECORD_AUDIO manquante")
            return
        }

        try {
            // Créer un fichier unique pour l'enregistrement
            val timestamp = System.currentTimeMillis()
            currentAudioFile = File(getExternalFilesDir(null), "audio_$timestamp.m4a")

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)   // conteneur MP4
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)      // codec AAC
                setAudioEncodingBitRate(128000)                      // meilleure qualité
                setAudioSamplingRate(44100)                          // qualité CD
                setOutputFile(currentAudioFile?.absolutePath)

                prepare()
                start()


            isRecording = true
                Log.d("TimerService", "Enregistrement audio démarré: ${currentAudioFile?.name}")
            }
        } catch (e: Exception) {
            Log.e("TimerService", "Erreur lors du démarrage de l'enregistrement: ${e.message}")
            isRecording = false
        }
    }

    private fun stopAudioRecording() {
        if (isRecording && mediaRecorder != null) {
            try {
                mediaRecorder?.stop()
                mediaRecorder?.release()
                mediaRecorder = null
                isRecording = false

                // Ajouter le fichier à la queue d'envoi
                currentAudioFile?.let { file ->
                    if (file.exists() && file.length() > 0) {
                        audioFileQueue.add(file)
                        Log.d("TimerService", "Fichier audio ajouté à la queue: ${file.name}")
                    } else {
                        Log.w("TimerService", "Fichier audio vide ou inexistant")
                    }
                }

            } catch (e: Exception) {
                Log.e("TimerService", "Erreur lors de l'arrêt de l'enregistrement: ${e.message}")
            }
        }
    }

    private fun handleAudioRecording() {
        // Arrêter l'enregistrement en cours
        stopAudioRecording()

        // Démarrer un nouvel enregistrement
        startAudioRecording()
    }


    private fun sendQueuedAudioFiles() {
        if (audioFileQueue.isNotEmpty()) {
            val api = api_service(this)
            val filesToSend = ArrayList(audioFileQueue)

            // Envoyer chaque fichier
            filesToSend.forEach { file ->
                api.sendAudioFile(
                    context = this,
                    audioFile = file
                ) { success ->
                    if (success) {
                        Log.d("TimerService", "Fichier audio envoyé avec succès: ${file.name}")
                        // Supprimer le fichier de la queue et du système
                        audioFileQueue.remove(file)
                        if (file.exists()) {
                            file.delete()
                        }
                    } else {
                        Log.e("TimerService", "Erreur lors de l'envoi du fichier audio: ${file.name}")
                        // Le fichier reste dans la queue pour un nouvel essai
                    }
                }
            }
        }
    }

    private fun hasAudioPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
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

        // Arrêter l'enregistrement en cours
        stopAudioRecording()

        // Envoyer les fichiers restants
        sendQueuedAudioFiles()

        clearTimerState()
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        // S'assurer que l'enregistrement est arrêté
        if (isRecording) {
            stopCountdown()
            stopLocationUpdates()

        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}