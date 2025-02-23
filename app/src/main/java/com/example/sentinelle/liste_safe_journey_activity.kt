package com.example.sentinelle

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sentinelle.Journey
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import com.example.sentinelle.activity_home
import org.json.JSONObject
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
                        // Parsez la réponse JSON
                        val jsonObject = JSONObject(responseData)
                        val NewToken = jsonObject.getString("token")
                        val safeJourneysArray = jsonObject.getJSONArray("safe_journeys")

                        if (NewToken.toString() != "null") {

                        // Mettez à jour SharedPreferences avec le nouveau token
                        val editor = sharedPreferences.edit()
                        editor.putString("email", email)
                        editor.putString("token", NewToken)
                        editor.putBoolean("is_authentificated", true)
                        editor.apply()

                        // C'est Good

                        // Traitez les données safe_journey

                        listView = findViewById(R.id.ListViewSafeJourney)

                        val listItems = arrayListOf<Journey>()
                        for (i in 0 until safeJourneysArray.length()) {
                            val safeJourney = safeJourneysArray.getJSONObject(i)
                            val data_date = safeJourney.getString("date").split(" ").toTypedArray()
                            val date = data_date[0]
                            val hour_start = data_date[1]
                            // val date = safeJourney.getString("date")
                            val state = safeJourney.getString("state")
                            val icon = if (state == "ACTIVE") R.drawable.tick else R.drawable.warning // Exemple d'icône
                            listItems.add(Journey(date, "${hour_start.substring(0, 5).replace(":", "h")} - ?", icon))
                        }

                        adapter = JourneyAdapter(this, R.layout.item_journey, listItems)
                        listView.adapter = adapter

                        listView.setOnItemClickListener{ parent, view, position, id ->
                            val selectedItem = parent.getItemAtPosition(position) as String
                            Toast.makeText(this, "Tu as sélectionner $selectedItem", Toast.LENGTH_SHORT).show()
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