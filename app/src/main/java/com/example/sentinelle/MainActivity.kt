package com.example.sentinelle


import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

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
        sharedPreferences = this.getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val isAuthentificated = sharedPreferences.getBoolean("is_authentificated", false)

        // Si la varriable is_authentificated est à True, alors on redirige vers la page principale
        if (isAuthentificated) {
            // Rediriger vers activity_home
            val intent = Intent(this, login_activity::class.java)
            startActivity(intent)
            finish()
        } else {
            // Si la réponse est "null", le token est invalide
            val intent = Intent(this, login_activity::class.java)
            startActivity(intent)
            finish()

        }
    }
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

    lateinit var  sharedPreferences: SharedPreferences

    // Initialisation paresseuse du LocationManager, l'objet sera créé uniquement lorsque nécessaire
//    private val locationManager by lazy {
//        // Utilisation du contexte de l'application pour éviter les fuites de mémoire liées à un contexte d'activité
//        LocationManager(this)
//    }
//}