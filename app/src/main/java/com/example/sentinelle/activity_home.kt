package com.example.sentinelle


import androidx.activity.enableEdgeToEdge
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlin.system.exitProcess
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sentinelle.page.tuto.TutoOneActivity
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.reflect.Field
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class activity_home : AppCompatActivity() {
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private lateinit var npHours: NumberPicker
    private lateinit var npMinutes: NumberPicker
    private lateinit var npSeconds: NumberPicker
    private lateinit var btnStart: Button
    private var timer: CountDownTimer? = null
    private var totalTimeInMillis: Long = 0
    private val REQUEST_CODE_BACKGROUND_LOCATION = 100
    private var isTimerRunning = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var wakeLock: PowerManager.WakeLock
    lateinit var  sharedPreferences: SharedPreferences

    private lateinit var locationManager: LocationManager



    private var isTracking = false // Variable pour suivre si le service est en cours de suivi ou non

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        setContentView(R.layout.activity_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // On récupère le btn de l'onglet des safe journey
        val btn_ongletSafeJourney = findViewById<ImageView>(R.id.btnOngletJourney)

        btn_ongletSafeJourney.setOnClickListener(View.OnClickListener { view: View? ->
            Intent(this, liste_safe_journey_activity::class.java).also {
                startActivity(it)
            }
        })

        locationManager = LocationManager(this)




        // Demande à l'utilisateur de désactiver les optimisations de batterie
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val packageName = packageName
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = android.net.Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }

        npHours = findViewById(R.id.np_hours)
        npMinutes = findViewById(R.id.np_minutes)
        npSeconds = findViewById(R.id.np_seconds)
        btnStart = findViewById(R.id.btn_start)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)



        npHours.minValue = 0
        npHours.maxValue = 23
        npMinutes.minValue = 0
        npMinutes.maxValue = 59
        npSeconds.minValue = 0
        npSeconds.maxValue = 59

        setNumberPickerTextColor(npHours, Color.GREEN)
        setNumberPickerTextColor(npMinutes, Color.GREEN)
        setNumberPickerTextColor(npSeconds, Color.GREEN)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            // Permission en arrière-plan accordée
        } else {
            // Demander la permission
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), REQUEST_CODE_BACKGROUND_LOCATION)
        }

        btnStart.setOnClickListener {
            if (isTimerRunning) {
                stopTimer()
            } else {
                startTimer()
            }
            isTracking = !isTracking // Change l'état du suivi
        }
    }

    // Fonction pour démarrer le suivi de localisation
    private fun startLocationTracking() {
        // Crée un intent pour démarrer le service avec l'action START
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Les permissions ne sont pas accordées, demandez-les
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            val intent = Intent(this, LocationTrackerService::class.java)
            intent.action = LocationTrackerService.Action.START.name
            // Démarre le service en arrière-plan
            startService(intent)
        }
    }

    // Fonction pour arrêter le suivi de localisation
    private fun stopLocationTracking() {
        // Crée un intent pour arrêter le service avec l'action STOP
        val intent = Intent(this, LocationTrackerService::class.java)
        intent.action = LocationTrackerService.Action.STOP.name

        // Arrête le service en arrière-plan
        stopService(intent)
    }

    // Démarage du timer
    private fun startTimer() {

        // On récupère la varriable isConnected dans le SharedPreferences pour checker si l'utilisateur est déjà connecté
        sharedPreferences = this.getSharedPreferences("app_state", Context.MODE_PRIVATE)
        val isAuthentificated = sharedPreferences.getBoolean("is_authentificated", false)
        val emailPreference = sharedPreferences.getString("email", "")
        val tokenPreference = sharedPreferences.getString("token", "")
        var email = emailPreference.toString()
        var token = tokenPreference.toString()


        val hours = npHours.value
        val minutes = npMinutes.value
        val seconds = npSeconds.value
        totalTimeInMillis = ((hours * 3600) + (minutes * 60) + seconds) * 1000L



        if (totalTimeInMillis > 0) {
            timer?.cancel()
            isTimerRunning = true
            btnStart.text = "Arrêter"

            // On crée le safejourney
            val url = "https://boutique-casse-tete.com/sentinelle/index.php"

            val client = OkHttpClient()

            // Calculer le temps total en millisecondes
            val totalMillis : Long = (npHours.value * 3600L + npMinutes.value * 60L + npSeconds.value) * 1000L


            // Obtenir l'heure actuelle en millisecondes
            val currentTimeMillis = System.currentTimeMillis()

            // Calculer l'heure de fin théorique
            val heureFinTheoriqueMillis = currentTimeMillis + totalMillis

            // Formater les heures
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val heureActuelle = dateFormat.format(Date(currentTimeMillis))
            val heureFinTheorique = dateFormat.format(Date(heureFinTheoriqueMillis))



            // Ajouter les champs au FormBody
            val formBody = FormBody.Builder()
                .add("email", email)
                .add("token", token)
                .add("task", "launch_minuteur")
                .add("heure_fin_theorique", heureFinTheorique)
                .add("heure_actuelle", heureActuelle)
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

                            Log.d("LATOKEN", "${NewToken}")
                            if (NewToken.toString() != "null") {
                                // Si la réponse est différente de "null", succès

                                // On ajoute les identifiants (email et token) dans les SharedPreferences
                                val sharedPreferences = getSharedPreferences("app_state", MODE_PRIVATE)
                                val editor = sharedPreferences.edit()
                                editor.putString("email", email)
                                editor.putString("token", NewToken)
                                editor.putBoolean("is_authentificated", true)
                                editor.apply()
                                // C'est Good
                                // On démare la localisation
                                startLocationTracking()



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



            timer = object : CountDownTimer(totalTimeInMillis, 1000) {

                override fun onTick(millisUntilFinished: Long) {

                    val remainingSeconds = (millisUntilFinished / 1000).toInt()
                    val remainingHours = remainingSeconds / 3600
                    val remainingMinutes = (remainingSeconds % 3600) / 60
                    val remainingSecs = remainingSeconds % 60

                    runOnUiThread {
                        npHours.value = remainingHours
                        npMinutes.value = remainingMinutes
                        npSeconds.value = remainingSecs
                    }



                    // Si 10 secondes sont passer, alors on récupère la localisation
                    if (remainingSeconds % 10 == 0) {


                        locationManager.getLocation { latitude, longitude ->
                            Log.d("activity_homeSA", "Localisation récupérée : $latitude, $longitude")
                        }
                    }

                }

                override fun onFinish() {
                    // On change le status de la safe journey
                    sharedPreferences = this@activity_home.getSharedPreferences("app_state", Context.MODE_PRIVATE)
                    val isAuthentificatedBis = sharedPreferences.getBoolean("is_authentificated", false)
                    val emailPreferenceBis = sharedPreferences.getString("email", "")
                    val tokenPreferenceBis = sharedPreferences.getString("token", "")
                    var emailBis = emailPreferenceBis.toString()
                    var tokenBis = tokenPreferenceBis.toString()

                    // Obtenir l'heure actuelle en millisecondes
                    val currentTimeMillisBis = System.currentTimeMillis()

                    // Formater les heures
                    val dateFormatBis = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val heureActuelleBis = dateFormat.format(Date(currentTimeMillis))

                    val urlBis = "https://boutique-casse-tete.com/sentinelle/index.php"
                    val clientBis = OkHttpClient()
                    val formBodyBis = FormBody.Builder()
                        .add("email", emailBis)
                        .add("token", tokenBis)
                        .add("task", "expire_minuteur")
                        .add("heure_actuelle", heureActuelleBis)
                        .build()
                    val requestBis = Request.Builder()
                        .url(urlBis)
                        .post(formBodyBis)
                        .build()
                    Thread {
                        try {
                            val response = clientBis.newCall(requestBis).execute()
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
                                        timer?.cancel()
                                        isTimerRunning = false
                                        btnStart.text = "Démarrer"
                                        // Réinitialisation des NumberPickers à 0
                                        npHours.value = 0
                                        npMinutes.value = 0
                                        npSeconds.value = 0

                                        Toast.makeText(applicationContext, "Alerte délcancher !", Toast.LENGTH_SHORT).show()

                                    } else {
                                        // Si la réponse est "null", le token est invalide
                                        logoutUser()
                                    }
                                }
                            } else {
                                // Si la réponse HTTP n'est pas réussie
                                runOnUiThread {
                                    Toast.makeText(applicationContext, "Erreur lors de la connexion de l'utilisateur", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this@activity_home, TutoOneActivity::class.java))
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
            }.start()
        } else {
            Toast.makeText(this, "Veuillez régler une durée valide !", Toast.LENGTH_SHORT).show()
        }
    }

    // Arrêt du timer
    private fun stopTimer() {


        // On change le status de la safe journey

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


        // Obtenir l'heure actuelle en millisecondes
        val currentTimeMillis = System.currentTimeMillis()
        // Formater les heures
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val heureActuelle = dateFormat.format(Date(currentTimeMillis))

        val formBody = FormBody.Builder()
            .add("email", email)
            .add("token", token)
            .add("task", "close_minuteur")
            .add("heure_actuelle", heureActuelle)
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
                        val NewToken = responseData.substring(1)
                        Log.e("LaToken", "Voici la ${NewToken}")

                        if (NewToken != "null") {
                            // Si la réponse est différente de "null", succès

                            // On ajoute les identifiants (email et token) dans les SharedPreferences
                            val sharedPreferences = getSharedPreferences("app_state", MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putString("email", email)
                            editor.putString("token", NewToken)
                            editor.putBoolean("is_authentificated", true)
                            editor.apply()

                            timer?.cancel()
                            isTimerRunning = false
                            btnStart.text = "Démarrer"

                            // Réinitialisation des NumberPickers à 0
                            npHours.value = 0
                            npMinutes.value = 0
                            npSeconds.value = 0

                            // On stop la localisation
                            stopLocationTracking()


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



    // Remise à zero des NumberPickers
    private fun setNumberPickerTextColor(numberPicker: NumberPicker, color: Int) {
        try {
            val selectorWheelPaintField: Field = numberPicker.javaClass.getDeclaredField("mSelectorWheelPaint")
            selectorWheelPaintField.isAccessible = true
            (selectorWheelPaintField.get(numberPicker) as android.graphics.Paint).color = color
            for (i in 0 until numberPicker.childCount) {
                val child: View = numberPicker.getChildAt(i)
                if (child is android.widget.EditText) {
                    child.setTextColor(color)
                }
            }
            numberPicker.invalidate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
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