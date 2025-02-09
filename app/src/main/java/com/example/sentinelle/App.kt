package com.example.sentinelle

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/**
 * @author Ahmed Guedmioui
 */
class App: Application() {

    // Méthode appelée lors de la création de l'application
    override fun onCreate() {
        super.onCreate()

        // Vérifie si la version de l'Android est >= à Oreo (API 26), car les notifications en arrière-plan
        // nécessitent un canal sur ces versions et ultérieures
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Création d'un canal de notification avec un identifiant unique
            val channel = NotificationChannel(
                LocationTrackerService.LOCATION_CHANNEL, // Utilisation du même identifiant que dans le service
                "Location", // Nom du canal qui s'affichera dans les paramètres de l'application
                NotificationManager.IMPORTANCE_LOW // Niveau de priorité des notifications, ici c'est faible
            )

            // Obtention du service de gestion des notifications
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Création du canal de notification dans le gestionnaire
            notificationManager.createNotificationChannel(channel)
        }
    }
}
