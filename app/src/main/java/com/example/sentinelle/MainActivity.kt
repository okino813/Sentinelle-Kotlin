package com.example.sentinelle

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.text.substring


import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    lateinit var  sharedPreferences: SharedPreferences

    // Initialisation paresseuse du LocationManager, l'objet sera créé uniquement lorsque nécessaire
    private val locationManager by lazy {
        // Utilisation du contexte de l'application pour éviter les fuites de mémoire liées à un contexte d'activité
        LocationManager(this)
    }

    // Définition des permissions nécessaires pour accéder à la localisation et afficher des notifications
    private val permissions = arrayOf(
        // Permission pour accéder à la localisation approximative
        Manifest.permission.ACCESS_COARSE_LOCATION,

        // Permission pour accéder à la localisation précise
        Manifest.permission.ACCESS_FINE_LOCATION,

        // Permission pour poster des notifications
        Manifest.permission.POST_NOTIFICATIONS
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // On récupère la varriable isConnected dans le SharedPreferences pour checker si l'utilisateur est déjà connecté
        sharedPreferences = this.getSharedPreferences("app_state", MODE_PRIVATE)
        val isAuthentificated = sharedPreferences.getBoolean("is_authentificated", false)
        val emailPreference = sharedPreferences.getString("email", "")
        val tokenPreference = sharedPreferences.getString("token", "")
        var email = emailPreference.toString()
        var token = tokenPreference.toString()


        /* val sharedPreferences = getSharedPreferences("app_state", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("is_authentificated", false)
        editor.apply() */

        // Si la varriable is_authentificated est à True, alors on redirige vers la page principale
        if (isAuthentificated) {
            val url = "https://boutique-casse-tete.com/sentinelle/index.php"

            val client = OkHttpClient()

            val formBody = FormBody.Builder()
                .add("email", email)
                .add("token", token)
                .add("task", "launch_connexion")
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
                            Log.e("LeToken", "Voici la ${responseData.toString()}")
                            val NewToken = responseData.substring(1)
                            if (NewToken.toString() != "null") {
                                // Si la réponse est différente de "null", succès

                                // On ajoute les identifiants (email et token) dans les SharedPreferences
                                val sharedPreferences = getSharedPreferences("app_state", MODE_PRIVATE)
                                val editor = sharedPreferences.edit()
                                editor.putString("email", email)
                                editor.putString("token", NewToken)
                                editor.putBoolean("is_authentificated", true)
                                editor.apply()
                                // Rediriger vers activity_home
                                val intent = Intent(this, activity_home::class.java)
                                startActivity(intent)
                            } else {
                                // Si la réponse est "null", le token est invalide
                                val sharedPreferences = getSharedPreferences("app_state", MODE_PRIVATE)
                                val editor = sharedPreferences.edit()
                                editor.putString("email", email)
                                editor.putString("token", "null")
                                editor.putBoolean("is_authentificated", false)
                                editor.apply()
                                Toast.makeText(applicationContext, "Déconnexion...", Toast.LENGTH_SHORT).show()


                                // Redirection vers tuto_one_activity
                                val intent = Intent(this, TutoOneActivity::class.java)
                                startActivity(intent)
                            }
                        }
                    } else {
                        // Si la réponse HTTP n'est pas réussie
                        runOnUiThread {
                            // Redirection vers tuto_one_activity
                            Toast.makeText(applicationContext, "Erreur lors de la connexion de l'utilisateur", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, TutoOneActivity::class.java)
                            startActivity(intent)

                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Log.e("LoginError", "Erreur: ${e.message}", e)
                        Toast.makeText(applicationContext, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        } else {
            // Si l'utilisateur n'est pas authentifié, redirection après 1 seconde
            // Redirection vers tuto_one_activity
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this, TutoOneActivity::class.java)
                startActivity(intent)

            }, 1000)
        }



        // Toast.makeText(this,"Lancement avec Succès", Toast.LENGTH_SHORT).show()
    }
}