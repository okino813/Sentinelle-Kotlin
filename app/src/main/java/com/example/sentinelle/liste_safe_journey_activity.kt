package com.example.sentinelle

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import com.example.sentinelle.page.tuto.TutoOneActivity
import org.json.JSONObject
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.system.exitProcess

class liste_safe_journey_activity : AppCompatActivity() {
    lateinit var listView: ListView
    var listItems = ArrayList<Journey>()
    lateinit var adapter: JourneyAdapter
    lateinit var  sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_liste_safe_journey)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // On récupère les données sur le serveur
        // On récupère la varriable isConnected dans le SharedPreferences pour checker si l'utilisateur est déjà connecté
        sharedPreferences = this.getSharedPreferences("app_state", Context.MODE_PRIVATE)
        val isAuthentificated = sharedPreferences.getBoolean("is_authentificated", false)
        val emailPreference = sharedPreferences.getString("email", "")
        val tokenPreference = sharedPreferences.getString("token", "")
        var email = emailPreference.toString()
        var token = tokenPreference.toString()

        // On crée le safejourney
        val url = "https://boutique-casse-tete.com/sentinelle/index.php"

        val client = OkHttpClient()

        val formBody = FormBody.Builder()
            .add("email", email)
            .add("token", token)
            .add("task", "printListSafeJourney")
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        Thread {
            try {
                val response = client.newCall(request).execute()
                val responseData: String = response.body?.string().toString() // Récupérer la réponse sous forme de String

                // Vérifier si la réponse est valide
                if (response.isSuccessful) {
                    // Ici, on compare simplement si la réponse est "null" ou non
                    runOnUiThread {
                            // Parse la réponse JSON
                            val jsonObject = JSONObject(responseData)
                            val newToken = jsonObject.optString("token", "")
                            Log.d("RToken", "$newToken")

                            // Vérifie si le token est valide avant de continuer
                            if (!newToken.isNullOrEmpty()) {
                                // Mise à jour de SharedPreferences avec le nouveau token
                                val editor = sharedPreferences.edit()
                                editor.putString("email", email)
                                editor.putString("token", newToken)
                                editor.putBoolean("is_authentificated", true)
                                editor.apply()


                                // Récupération du tableau JSON "safe_journeys"
                                val safeJourneysArray = jsonObject.optJSONArray("safe_journeys")

                                // Vérifie si le tableau n'est pas null et s'il contient des éléments
                                if (safeJourneysArray != null && safeJourneysArray.length() > 0) {
                                    listView = findViewById(R.id.ListViewSafeJourney)
                                    val listItems = arrayListOf<Journey>()

                                    for (i in 0 until safeJourneysArray.length()) {
                                        val safeJourney = safeJourneysArray.optJSONObject(i)
                                        if (safeJourney != null) {

                                            // Vérifie si "date" est null ou vide
                                            val dataDate = safeJourney.optString("date", "")
                                            val dateParts = dataDate.split(" ")
                                            val date = dateParts.getOrNull(0) ?: "Date inconnue"
                                            val hourStart = dateParts.getOrNull(1)?.substring(0, 5)
                                                ?.replace(":", "h") ?: "??h??"

                                            // Vérifie si "hour_timer_stop" est null ou vide
                                            val dataDateTimerStop = safeJourney.optString("hour_timer_stop", "")

                                            val timerStopParts = dataDateTimerStop.split(" ")
                                            val hourStop = timerStopParts.getOrNull(1)?.take(5)?.replace(":", "h") ?: "??h??"

                                            // Vérifie si "state" est null
                                            val state = safeJourney.optString("state", "UNKNOWN")
                                            var icon: Int = R.drawable.warning
                                            if (state == "INACTIVE"){
                                                icon = R.drawable.tick
                                            } else if (state == "PENDING") {
                                                icon = R.drawable.pending
                                            } else{
                                                icon = R.drawable.warning
                                            }

                                            val durationText = try {
                                                val start = LocalTime.parse(hourStart.replace("h", ":"), DateTimeFormatter.ofPattern("HH:mm"))
                                                val stop = LocalTime.parse(hourStop.replace("h", ":"), DateTimeFormatter.ofPattern("HH:mm"))

                                                val duration = Duration.between(start, stop)
                                                val hours = duration.toHours()
                                                val minutes = duration.toMinutes() % 60

                                                if (hours > 0) {
                                                    "${hours}h${minutes}min"
                                                } else {
                                                    "${minutes}min"
                                                }
                                            } catch (e: Exception) {
                                                Log.e("ERROR", "Erreur lors du calcul de la durée: ${e.message}")
                                                "Durée inconnue"
                                            }

                                            Log.d("CACA", "$durationText")


                                            // Ajoute l'élément à la liste
                                            listItems.add(
                                                Journey(
                                                    date,
                                                    "$hourStart - $hourStop",
                                                    durationText,
                                                    icon
                                                )
                                            )
                                        }
                                    }

                                    // Met en place l'adapter
                                    adapter = JourneyAdapter(this, R.layout.item_journey, listItems)
                                    listView.adapter = adapter

                                    // Gestion du clic sur un élément de la liste
                                    listView.setOnItemClickListener { parent, view, position, id ->
                                        val selectedItem = listItems[position]
                                        Toast.makeText(
                                            this,
                                            "Tu as sélectionné ${selectedItem.titre}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                }




                    } else {
                            // Si la réponse est "null", le token est invalide
                            logoutUser()
                        }
                    }
                } else {
                    // Si la réponse HTTP n'est pas réussie
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Erreur lors de la connexion de l'utilisateur", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, TutoOneActivity::class.java))
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Log.e("LoginError", "Erreur: ${e.message}", e)
                    Toast.makeText(applicationContext, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()



    }

    // Déconnexion de l'utilisateur
    fun logoutUser() {
        val sharedPreferences = getSharedPreferences("app_state", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("email", "")
        editor.putString("token", "null")
        editor.putBoolean("is_authentificated", false)
        editor.apply()
        Toast.makeText(applicationContext, "Déconnexion...", Toast.LENGTH_SHORT).show()
        exitProcess(0)
    }
}