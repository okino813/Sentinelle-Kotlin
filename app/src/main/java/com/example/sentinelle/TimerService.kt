package com.example.sentinelle

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaRecorder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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

// Classe pour stocker les coordonnées GPS
data class LocationData(
    val latitude: String,
    val longitude: String,
    val timestamp: Long = System.currentTimeMillis()
)

class TimerService : Service() {

    private var timer: CountDownTimer? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: com.google.android.gms.location.LocationCallback

    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var currentAudioFile: File? = null
    private val audioFileQueue = ArrayList<File>()

    // Queue pour les coordonnées GPS
    private val locationQueue = ArrayList<LocationData>()

    // BroadcastReceiver pour détecter les changements de connectivité
    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
                if (isNetworkAvailable()) {
                    Log.d("TimerService", "Connexion rétablie - envoi des queues")
                    sendQueuedLocations()
                    sendQueuedAudioFiles()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Enregistrer le receiver pour les changements de réseau
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, filter)

        // Préparer le callback pour les mises à jour de localisation
        locationCallback = object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                for (location in result.locations) {
                    Log.d("TimerService", "Localisation → lat: ${location.latitude}, lng: ${location.longitude}")

                    // Ajouter à la queue au lieu d'envoyer directement
                    val locationData = LocationData(
                        latitude = location.latitude.toString(),
                        longitude = location.longitude.toString()
                    )
                    locationQueue.add(locationData)
                    Log.d("TimerService", "Coordonnées ajoutées à la queue (${locationQueue.size} en attente)")

                    // Tenter d'envoyer immédiatement si connexion disponible
                    if (isNetworkAvailable()) {
                        sendQueuedLocations()
                    }
                }
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
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
                    handleAudioRecording()

                    // Tenter d'envoyer les queues si connexion disponible
                    if (isNetworkAvailable()) {
                        sendQueuedAudioFiles()
                        sendQueuedLocations()
                    } else {
                        Log.d("TimerService", "Pas de connexion - données en queue (Audio: ${audioFileQueue.size}, GPS: ${locationQueue.size})")
                    }

                    Log.d("TimerService", "Action toutes les 5 minutes")
                }
            }

            override fun onFinish() {
                stopAudioRecording()

                // Envoi final - même sans connexion pour vider les queues à l'arrêt
                sendQueuedAudioFiles()
                sendQueuedLocations()

                clearTimerState()
                stopSelf()
                timer = null
            }
        }.start()
    }

    private fun sendQueuedLocations() {
        if (locationQueue.isEmpty()) {
            return
        }

        if (!isNetworkAvailable()) {
            Log.d("TimerService", "Pas de connexion réseau - ${locationQueue.size} coordonnées en attente")
            return
        }

        val api = api_service(this)
        val locationsToSend = ArrayList(locationQueue)

        Log.d("TimerService", "Envoi de ${locationsToSend.size} coordonnées en queue")

        // Envoyer chaque coordonnée
        locationsToSend.forEach { locationData ->
            api.sendLocation(
                context = this,
                latitude = locationData.latitude,
                longitude = locationData.longitude
            ) { success ->
                if (success) {
                    Log.d("TimerService", "Coordonnées envoyées avec succès: lat=${locationData.latitude}, lng=${locationData.longitude}")
                    // Supprimer de la queue
                    locationQueue.remove(locationData)
                } else {
                    Log.e("TimerService", "Erreur lors de l'envoi des coordonnées: lat=${locationData.latitude}, lng=${locationData.longitude}")
                    // Les coordonnées restent dans la queue pour un nouvel essai
                }
            }
        }
    }

    private fun sendQueuedAudioFiles() {
        if (audioFileQueue.isEmpty()) {
            return
        }

        if (!isNetworkAvailable()) {
            Log.d("TimerService", "Pas de connexion réseau - ${audioFileQueue.size} fichiers audio en attente")
            return
        }

        val api = api_service(this)
        val filesToSend = ArrayList(audioFileQueue)

        Log.d("TimerService", "Envoi de ${filesToSend.size} fichiers audio en queue")

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
            }

            isRecording = true
            Log.d("TimerService", "Enregistrement audio démarré: ${currentAudioFile?.name}")
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
                        Log.d("TimerService", "Fichier audio ajouté à la queue: ${file.name} (${audioFileQueue.size} en attente)")

                        // Tenter d'envoyer immédiatement si connexion disponible
                        if (isNetworkAvailable()) {
                            sendQueuedAudioFiles()
                        }
                        else{
                            Log.d("TimerService", "Pas de connexion - données en queue (Audio: ${audioFileQueue.size})")
                        }
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

                // Ajouter à la queue au lieu d'envoyer directement
                val locationData = LocationData(
                    latitude = location.latitude.toString(),
                    longitude = location.longitude.toString()
                )
                locationQueue.add(locationData)
                Log.d("TimerService", "Dernière localisation ajoutée à la queue")

                // Tenter d'envoyer immédiatement si connexion disponible
                if (isNetworkAvailable()) {
                    sendQueuedLocations()
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
            .setContentText("Minuteur en cours d'exécution…")
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

        // Tenter d'envoyer les queues restantes (même sans connexion pour le log)
        sendQueuedAudioFiles()
        sendQueuedLocations()

        clearTimerState()
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()

        // Désinscrire le receiver
        try {
            unregisterReceiver(networkReceiver)
        } catch (e: IllegalArgumentException) {
            Log.w("TimerService", "Receiver déjà désinscrit")
        }

        // S'assurer que l'enregistrement est arrêté et que les données sont envoyées
        if (isRecording) {
            stopCountdown()
            stopLocationUpdates()
        }

        // Dernier envoi des données restantes
        sendQueuedLocations()
        sendQueuedAudioFiles()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}