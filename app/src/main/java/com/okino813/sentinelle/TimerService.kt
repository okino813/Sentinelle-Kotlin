package com.okino813.sentinelle

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
import android.media.MediaRecorder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.okino813.sentinelle.api.AppValues
import com.okino813.sentinelle.api.api_service
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File

// Classe pour stocker les coordonnées GPS
data class LocationData(
    val latitude: String,
    val longitude: String,
    val timestamp: Long = System.currentTimeMillis() / 1000
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
    private var isSendingLocations = false
    private val sentLocations = HashSet<String>()


    // Remplacez vos variables de classe par ceci :
    private var isSendingAudio = false
    private val audioFilesBeingSent = HashSet<String>() // Fichiers actuellement en cours d'envoi
    private var lastAudioSendTime = 0L // Pour éviter les envois trop rapprochés

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

                    // Ajouter à la queue
                    val locationData = LocationData(
                        latitude = location.latitude.toString(),
                        longitude = location.longitude.toString(),
                        timestamp = System.currentTimeMillis() / 1000
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
                startForeground(1, buildNotification())
                startCountdown(totalSeconds)
            }
            "RESUME_TIMER" -> {
                val totalSeconds = intent.getIntExtra("totalSeconds", 0)
                startForeground(1, buildNotification())
                startCountup(totalSeconds)
            }
            "STOP_TIMER" -> {
                stopCountdown()
                stopLocationUpdates()
            }
        }
        return START_STICKY
    }

    private fun startCountup(totalSeconds: Int) {
        val endTimestamp = System.currentTimeMillis() + totalSeconds * 1000L
        getSharedPreferences("sentinelle_prefs", MODE_PRIVATE).edit()
            .putBoolean("is_timer_running", true)
            .putLong("end_timestamp", endTimestamp)
            .apply()

        var secondsElapsed = 0

        // Démarrer l'enregistrement audio
        startAudioRecording()

        timer = object : CountDownTimer(totalSeconds * 1000L, 1_000L) {
            override fun onTick(millisLeft: Long) {
                secondsElapsed++

                // Action chaque seconde
                var heurePasser = secondsElapsed / 3600
                var minutePasser = (secondsElapsed % 3600) / 60
                var secondePasser = (secondsElapsed % 60)

                AppValues.hour.value = heurePasser.toInt()
                AppValues.minute.value = minutePasser.toInt()
                AppValues.seconde.value = secondePasser.toInt()

                Log.d("TimerService", "Heure: $heurePasser, Minute: $minutePasser, Seconde: $secondePasser")

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
                // Envoi final - même sans connexion potur vider les queues à l'arrêt
                sendQueuedAudioFiles()
                sendQueuedLocations()

                clearTimerState()
                stopSelf()
                timer = null
            }
        }.start()
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
                }
            }

            override fun onFinish() {
                stopAudioRecording()
                // Envoi final - même sans connexion potur vider les queues à l'arrêt
                sendQueuedAudioFiles()
                sendQueuedLocations()

                val intent = Intent(this@TimerService, TimerService::class.java).apply {
                    action = "RESUME_TIMER"
                    putExtra("totalSeconds", 172800)
                }
                ContextCompat.startForegroundService(this@TimerService, intent)

                // On envoi une notification pour dire que le minuteur a été atteins
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val notification = NotificationCompat.Builder(this@TimerService, "saferider_timer")
                    .setContentTitle("Minuteur terminé")
                    .setContentText("Le minuteur de SafeRider est arrivé à son terme.")
                    .setSmallIcon(R.drawable.main_icon_dark)
                    .setAutoCancel(true)
                    .build()

                notificationManager.notify(2, notification)




//                clearTimerState()
//                stopSelf()
//                timer = null
            }
        }.start()
    }

    private fun sendQueuedLocations() {
        if (locationQueue.isEmpty() || isSendingLocations) {
            return
        }

        if (!isNetworkAvailable()) {
            Log.d("TimerService", "Pas de connexion réseau - ${locationQueue.size} coordonnées en attente")
            return
        }

        isSendingLocations = true
        val api = api_service(this)

        // Créer un identifiant unique pour chaque location
        val locationsToSend = locationQueue.filter { locationData ->
            val locationId = "${locationData.latitude}_${locationData.longitude}_${locationData.timestamp}"
            !sentLocations.contains(locationId)
        }

        if (locationsToSend.isEmpty()) {
            isSendingLocations = false
            return
        }

        Log.d("TimerService", "Envoi de ${locationsToSend.size} coordonnées en queue")

        var pendingUploads = locationsToSend.size

        locationsToSend.forEach { locationData ->
            val locationId = "${locationData.latitude}_${locationData.longitude}_${locationData.timestamp}"
            sentLocations.add(locationId)

            api.sendLocation(
                context = this,
                latitude = locationData.latitude,
                longitude = locationData.longitude,
                timestamp = locationData.timestamp.toString()
            ) { success ->
                pendingUploads--

                if (success) {
                    Log.d("TimerService", "Coordonnées envoyées avec succès: lat=${locationData.latitude}, lng=${locationData.longitude}")
                    locationQueue.remove(locationData)
                } else {
                    Log.e("TimerService", "Erreur lors de l'envoi des coordonnées: lat=${locationData.latitude}, lng=${locationData.longitude}")
                    sentLocations.remove(locationId)
                }

                if (pendingUploads == 0) {
                    isSendingLocations = false
                }
            }
        }
    }

    private fun sendQueuedAudioFiles() {
        Log.d("TimerService", "sendQueuedAudioFiles() appelée - Queue: ${audioFileQueue.size}, isSending: $isSendingAudio")

        if (audioFileQueue.isEmpty()) {
            Log.d("TimerService", "Queue audio vide - arrêt")
            return
        }

        if (isSendingAudio) {
            Log.d("TimerService", "Envoi déjà en cours - arrêt")
            return
        }

        if (!isNetworkAvailable()) {
            Log.d("TimerService", "Pas de connexion réseau - ${audioFileQueue.size} fichiers audio en attente")
            return
        }

        // DÉBOUNCE : éviter les envois trop rapprochés (moins de 3 secondes)
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAudioSendTime < 3000) {
            Log.d("SendAudio", "Envoi trop rapproché ignoré (débounce)")
            return
        }
        lastAudioSendTime = currentTime

        // Filtrer les fichiers qui ne sont pas déjà en cours d'envoi
        val filesToSend = audioFileQueue.filter { file ->
            !audioFilesBeingSent.contains(file.absolutePath)
        }

        if (filesToSend.isEmpty()) {
            Log.d("SendAudio", "Tous les fichiers sont déjà en cours d'envoi")
            return
        }

        Log.d("SendAudio", "=== DÉBUT ENVOI AUDIO ===")
        Log.d("SendAudio", "Fichiers à envoyer: ${filesToSend.size}")
        filesToSend.forEach { file ->
            Log.d("SendAudio", "- ${file.name} (${file.length()} bytes)")
        }

        isSendingAudio = true
        val api = api_service(this)
        var pendingUploads = filesToSend.size

        filesToSend.forEach { file ->
            // Marquer ce fichier comme en cours d'envoi
            audioFilesBeingSent.add(file.absolutePath)

            Log.d("SendAudio", "Envoi du fichier: ${file.name}")

            api.sendAudioFile(
                context = this,
                audioFile = file
            ) { success ->
                pendingUploads--

                Log.d("SendAudio", "Callback reçu pour ${file.name} - Success: $success, Restant: $pendingUploads")

                if (success) {
                    Log.d("SendAudio", "✅ Fichier audio envoyé avec succès: ${file.name}")
                    // Supprimer le fichier de TOUTES les listes
                    audioFileQueue.remove(file)
                    audioFilesBeingSent.remove(file.absolutePath)

                    // Supprimer le fichier du système
                    try {
                        if (file.exists() && file.delete()) {
                            Log.d("SendAudio", "Fichier supprimé du système: ${file.name}")
                        }
                    } catch (e: Exception) {
                        Log.e("SendAudio", "Erreur suppression fichier: ${e.message}")
                    }
                } else {
                    Log.e("SendAudio", "❌ Erreur lors de l'envoi du fichier audio: ${file.name}")
                    // Retirer seulement de la liste des fichiers en cours d'envoi pour permettre un nouvel essai
                    audioFilesBeingSent.remove(file.absolutePath)
                }

                // Réinitialiser le flag quand tous les envois sont terminés
                if (pendingUploads == 0) {
                    isSendingAudio = false
                    Log.d("SendAudio", "=== FIN ENVOI AUDIO === (isSendingAudio = false)")
                    Log.d("SendAudio", "Queue finale: ${audioFileQueue.size} fichiers")
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

                currentAudioFile?.let { file ->
                    if (file.exists() && file.length() > 0) {
                        Log.d("TimerService", "📁 Fichier audio créé: ${file.name} (${file.length()} bytes)")
                        audioFileQueue.add(file)
                        Log.d("TimerService", "Queue audio mise à jour: ${audioFileQueue.size} fichiers en attente")

                        // Lister tous les fichiers en queue
                        audioFileQueue.forEachIndexed { index, queueFile ->
                            Log.d("TimerService", "Queue[$index]: ${queueFile.name}")
                        }
                    } else {
                        Log.w("TimerService", "⚠️ Fichier audio vide ou inexistant: ${file?.name}")
                    }
                }
            } catch (e: Exception) {
                Log.e("TimerService", "Erreur lors de l'arrêt de l'enregistrement: ${e.message}")
            }
        }
    }

    private fun handleAudioRecording() {
        Log.d("TimerService", "handleAudioRecording() - Cycle de 5 minutes")

        // Arrêter l'enregistrement en cours
        stopAudioRecording()

        // Attendre un peu avant de redémarrer (pour éviter les conflits)
        android.os.Handler(mainLooper).postDelayed({
            startAudioRecording()
        }, 500) // 500ms de délai
    }

    private fun hasAudioPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
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
        val api = api_service(this)
        api.getInfo(this) // Mettre à jour les infos après l'arrêt
    }

    override fun onDestroy() {
        super.onDestroy()

        try {
            unregisterReceiver(networkReceiver)
        } catch (e: IllegalArgumentException) {
            Log.w("TimerService", "Receiver déjà désinscrit")
        }

        if (isRecording) {
            stopCountdown()
            stopLocationUpdates()
        }

        // Nettoyer les sets de tracking
        audioFilesBeingSent.clear()

        Log.d("TimerService", "🔄 onDestroy - Tentative d'envoi final")

        // Dernier envoi des données restantes avec un délai pour éviter les conflits
        android.os.Handler(mainLooper).postDelayed({
            sendQueuedAudioFiles()
        }, 1000) // 1 seconde de délai
    }

    override fun onBind(intent: Intent?): IBinder? = null
}