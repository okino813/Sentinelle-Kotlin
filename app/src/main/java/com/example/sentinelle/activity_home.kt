package com.example.sentinelle


import androidx.activity.enableEdgeToEdge
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlin.system.exitProcess
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.*
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.reflect.Field
import kotlin.system.exitProcess


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

        // Activation du WakeLock pour éviter que l'application ne soit mise en veille
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::LocationWakelock")
        wakeLock.acquire(10 * 60 * 1000L)

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
        }
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

        // On crée le safejourney
        val url = "https://boutique-casse-tete.com/sentinelle/index.php"

        val client = OkHttpClient()

        val formBody = FormBody.Builder()
            .add("email", email)
            .add("token", token)
            .add("task", "launch_minuteur")
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
                            // C'est Good

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


        val hours = npHours.value
        val minutes = npMinutes.value
        val seconds = npSeconds.value
        totalTimeInMillis = ((hours * 3600) + (minutes * 60) + seconds) * 1000L

        if (totalTimeInMillis > 0) {
            timer?.cancel()
            isTimerRunning = true
            btnStart.text = "Arrêter"

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
                        getLocation()
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

                    val urlBis = "https://boutique-casse-tete.com/sentinelle/index.php"
                    val clientBis = OkHttpClient()
                    val formBodyBis = FormBody.Builder()
                        .add("email", emailBis)
                        .add("token", tokenBis)
                        .add("task", "expire_minuteur")
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

        val formBody = FormBody.Builder()
            .add("email", email)
            .add("token", token)
            .add("task", "close_minuteur")
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

                            timer?.cancel()
                            isTimerRunning = false
                            btnStart.text = "Démarrer"

                            // Réinitialisation des NumberPickers à 0
                            npHours.value = 0
                            npMinutes.value = 0
                            npSeconds.value = 0


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

    // Récupère la localisation en continu même en arrière-plan
    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(10000) // Mise à jour toutes les 10 secondes
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    sendLocationToServer(location)
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun sendLocationToServer(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude
        Log.d("Location", "Latitude: $latitude, Longitude: $longitude")

        sharedPreferences = this.getSharedPreferences("app_state", Context.MODE_PRIVATE)
        val email = sharedPreferences.getString("email", "") ?: ""
        val token = sharedPreferences.getString("token", "") ?: ""

        val url = "https://boutique-casse-tete.com/sentinelle/index.php"
        val client = OkHttpClient()
        val formBody = FormBody.Builder()
            .add("email", email)
            .add("token", token)
            .add("latitude", latitude.toString())
            .add("longitude", longitude.toString())
            .add("task", "send_GPS")
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        Thread {
            try {
                val response = client.newCall(request).execute()
                val responseData: String = response.body?.string().toString()
                runOnUiThread {
                    if (response.isSuccessful && responseData != "null") {
                        //val sharedPreferences = getSharedPreferences("app_state", MODE_PRIVATE)
                        //val editor = sharedPreferences.edit()
                        //editor.putString("token", responseData.trim())
                        //editor.putBoolean("is_authentificated", true)
                        //editor.apply()
                        Log.d("LocationGo", "Latitude: $latitude, Longitude: $longitude");
                    } else {
                        logoutUser()
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

    // Gère les permissions de localisation
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation()
            } else {
                Toast.makeText(this, "Permission de localisation refusée", Toast.LENGTH_SHORT).show()
            }
        }
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
    private fun logoutUser() {
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
