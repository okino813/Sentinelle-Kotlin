package com.example.sentinelle

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * @author Ahmed Guedmioui
 */
class LocationTrackerService : Service() {

    // Création d'une coroutine scope sur le thread IO, utilisée pour gérer les tâches en arrière-plan
    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO // L'IO pour gérer les appels réseau, accès aux bases de données, etc.
    )

    // Méthode appelée lorsque le service est lié, ici on retourne null car ce service n'est pas lié
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    // Méthode appelée lorsque le service est démarré, gère les actions START et STOP
    override fun onStartCommand(
        intent: Intent?, flags: Int, startId: Int
    ): Int {

        // Vérifie quelle action a été envoyée dans l'intent et lance l'action correspondante
        when (intent?.action) {
            Action.START.name -> start() // Démarre le suivi de localisation
            Action.STOP.name -> stop() // Arrête le service de localisation
        }

        // Retourne la valeur de super.onStartCommand pour indiquer que le service continue à fonctionner
        return super.onStartCommand(intent, flags, startId)
    }

    // Démarre le suivi de localisation en avant-plan, avec une notification persistante
    private fun start() {

        // Création du gestionnaire de localisation
        val locationManager = LocationManager(applicationContext)

        // Obtention du service de gestion des notifications
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Création d'une notification persistante qui sera affichée pendant l'exécution du service
        val notification = NotificationCompat
            .Builder(this, LOCATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Location Tracker")
            .setStyle(NotificationCompat.BigTextStyle()) // Style étendu pour afficher plus de texte

        // Démarre le service en avant-plan avec la notification
        startForeground(1, notification.build())

        // Lance une coroutine pour suivre la localisation en arrière-plan
        scope.launch {
            // Collecte les mises à jour de localisation en continu
            locationManager.trackLocation().collect { location ->
                // Extrait les 4 derniers chiffres de la latitude et longitude pour afficher une version réduite
                val latitude = location.latitude.toString().takeLast(4)
                val longitude = location.longitude.toString().takeLast(4)

                // Met à jour la notification avec les nouvelles coordonnées
                notificationManager.notify(
                    1,
                    notification.setContentText(
                        "Location: ..$latitude / ..$longitude" // Affiche la localisation mise à jour
                    ).build()
                )
            }
        }
    }

    // Arrête le suivi de localisation et arrête le service
    private fun stop() {
        stopForeground(STOP_FOREGROUND_REMOVE) // Retire le service de l'avant-plan
        stopSelf() // Arrête le service
    }

    // Méthode appelée lors de la destruction du service, annule les coroutines en cours
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel() // Annule la coroutine scope pour arrêter toutes les coroutines en cours
    }

    // Enumération définissant les actions possibles du service
    enum class Action {
        START, STOP
    }

    // Constante pour le canal de notification, utilisée pour configurer le service de notification
    companion object {
        const val LOCATION_CHANNEL = "location_channel"
    }
}
