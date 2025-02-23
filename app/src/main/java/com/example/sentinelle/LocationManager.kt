package com.example.sentinelle

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import com.example.sentinelle.activity_home
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * @author Ahmed Guedmioui
 */
//@SuppressLint("MissingPermission") // Suppression de l'alerte de permission manquante
public class LocationManager(
    private val context: Context // Le contexte de l'application, utilisé pour obtenir des services système
) {

    private var lastLocationTime: Long = 0

    // Initialisation du client de localisation fusionnée pour obtenir les informations de localisation
    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun getLocation(
        onSuccess: (latitude: String, longitude: String) -> Unit
    ) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val latitude = location.latitude.toString()
                val longitude = location.longitude.toString()

                Log.d("LocationPIPI", "Latitude: $latitude, Longitude: $longitude")

                sendLocationToServer(latitude, longitude) // Envoie au serveur

                onSuccess(latitude, longitude) // Callback avec les coordonnées
            } else {
                Log.e("LocationError", "Impossible d'obtenir la localisation")
            }
        }.addOnFailureListener { exception ->
            Log.e("LocationError", "Erreur lors de la récupération de la localisation", exception)
        }
    }


    private fun sendLocationToServer(latitude: String, longitude: String) {
        val sharedPreferences = context.getSharedPreferences("app_state", Context.MODE_PRIVATE)
        val email = sharedPreferences.getString("email", "") ?: ""
        val token = sharedPreferences.getString("token", "") ?: ""

        // Obtenir l'heure actuelle en millisecondes
        val currentTimeMillis = System.currentTimeMillis()

        // Formater les heures
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val heureActuelle = dateFormat.format(Date(currentTimeMillis))

        val url = "https://boutique-casse-tete.com/sentinelle/index.php"
        val client = OkHttpClient()
        val formBody = FormBody.Builder()
            .add("email", email)
            .add("token", token)
            .add("latitude", latitude)
            .add("longitude", longitude)
            .add("task", "send_GPS")
            .add("heure_actuelle", heureActuelle)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        Thread {
            try {
                val response = client.newCall(request).execute()
                val responseData: String = response.body?.string().toString()
                (context as? activity_home)?.runOnUiThread {
                    if (response.isSuccessful && responseData != "null") {
                        Log.d("LocationGo", "Latitude: $latitude, Longitude: $longitude")
                    } else {
                        (context as? activity_home)?.logoutUser()
                    }
                }
            } catch (e: Exception) {
                (context as? activity_home)?.runOnUiThread {
                    Log.e("LoginError", "Erreur: ${e.message}", e)
                }
            }
        }.start()
    }




    // Fonction pour suivre la localisation en continu et envoyer les mises à jour via un Flow
    fun trackLocation(): Flow<Location> {
        return callbackFlow {
            // Création d'un callback pour recevoir les résultats de localisation
            val locationCallback = locationCallback { location ->
                // Envoi de la localisation dès qu'elle est reçue
                launch {
                    send(location)

                }
            }

            // Création de la requête de localisation avec des paramètres spécifiques
            val request = LocationRequest.Builder(1000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY) // 🔥 Priorité pour une localisation très précise
                .setIntervalMillis(1000) // Intervalle de mise à jour toutes les secondes
                .setMinUpdateIntervalMillis(500) // Délai minimal entre chaque mise à jour
                .build()

            // Demande des mises à jour de localisation via le client fusionné
            fusedLocationClient.requestLocationUpdates(
                request, // La requête définissant la fréquence et la priorité
                locationCallback, // Le callback qui recevra les mises à jour
                Looper.getMainLooper() // Utilisation du looper principal pour gérer les mises à jour sur le thread principal
            )

            // Nettoyage des mises à jour de localisation lorsque le Flow est fermé
            awaitClose {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        }
    }

    // Fonction pour créer un LocationCallback qui sera appelé lorsqu'une nouvelle localisation est disponible
    private fun locationCallback(
        onResult: (location: Location) -> Unit
    ): LocationCallback {
        return object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                result.locations.lastOrNull()?.let { location ->
                    val currentTime = location.time
                    if (currentTime > lastLocationTime) {
                        lastLocationTime = currentTime
                        val latitude = location.latitude
                        val longitude = location.longitude

                        // On envoi les coordonée au serveur

                        onResult(location)
                    } else {
                        Log.d("LocationManagerErreur", "Localisation ignorée (temps non-monotone) : Latitude = ${location.latitude}, Longitude = ${location.longitude}")
                    }
                }
            }
        }
    }
}
