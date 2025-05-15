package com.example.sentinelle.page

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.PowerManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.sentinelle.LocationManager
import com.example.sentinelle.LocationTrackerService
import com.example.sentinelle.R
import com.example.sentinelle.api.AppColors
import com.example.sentinelle.api.CustomNumberPicker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.system.exitProcess

/**
 * A simple [androidx.fragment.app.Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun HomeScreen() {
    var state by remember {
        mutableStateOf(1)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors().SentiBlack),
//        contentAlignment = Alignment.Center
    )
    {
        Text(
            "Bienvenue sur l'accueil",
            color = Color.White,
            )

        CustomNumberPicker(
            selectedValue = state,
            list = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,11,12,13,14,15,16,17,18,19,20,21,22,23),
            onValueChange = {
                state = it
    }
    )

        Text("Voici le state : $state")
    }
}

class HomeFragment : Fragment() {

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

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {

            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialisation des composants graphique et des varriables

        locationManager = LocationManager(requireContext())

        npHours = view.findViewById(R.id.np_hours)
        npMinutes = view.findViewById(R.id.np_minutes)
        npSeconds = view.findViewById(R.id.np_seconds)
        btnStart = view.findViewById(R.id.btn_start)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        npHours.minValue = 0
        npHours.maxValue = 23
        npMinutes.minValue = 0
        npMinutes.maxValue = 59
        npSeconds.minValue = 0
        npSeconds.maxValue = 59





        // On check les autorisations
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            // Permission accordée
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), REQUEST_CODE_BACKGROUND_LOCATION)
        }



        // Si le bouton starts est presser
        btnStart.setOnClickListener {
            if (isTimerRunning) {
                stopTimer()
            } else {
                startTimer()
            }
            isTracking = !isTracking
        }
    }

    // Fonction pour démarrer le suivi de localisation
    private fun startLocationTracking() {
        // Crée un intent pour démarrer le service avec l'action START
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Les permissions ne sont pas accordées, demandez-les
            ActivityCompat.requestPermissions(
                requireContext() as Activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            val intent = Intent(requireContext(), LocationTrackerService::class.java)
            intent.action = LocationTrackerService.Action.START.name
            // Démarre le service en arrière-plan
            requireContext().startService(intent)
        }
    }

    // Fonction pour arrêter le suivi de localisation
    private fun stopLocationTracking() {
        // Crée un intent pour arrêter le service avec l'action STOP
        val intent = Intent(requireContext(), LocationTrackerService::class.java)
        intent.action = LocationTrackerService.Action.STOP.name

        // Arrête le service en arrière-plan
        requireContext().stopService(intent)
    }

    // Arrêt du timer
    private fun stopTimer() {


        // On change le status de la safe journey

        // On récupère la varriable isConnected dans le SharedPreferences pour checker si l'utilisateur est déjà connecté
        sharedPreferences = requireContext().getSharedPreferences("app_state", Context.MODE_PRIVATE)
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
                    requireActivity().runOnUiThread {
                        val NewToken = responseData.substring(1)
                        Log.e("LaToken", "Voici la ${NewToken}")

                        if (NewToken != "null") {
                            // Si la réponse est différente de "null", succès

                            // On ajoute les identifiants (email et token) dans les SharedPreferences
                            val sharedPreferences = requireContext().getSharedPreferences("app_state",
                                Context.MODE_PRIVATE
                            )
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


                        }
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Log.e("LoginError", "Erreur: ${e.message}", e)
                    Toast.makeText(requireContext(), "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    // Démarage du timer
    private fun startTimer() {

        // On récupère la varriable isConnected dans le SharedPreferences pour checker si l'utilisateur est déjà connecté
        sharedPreferences = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
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
                        requireActivity().runOnUiThread {
                            Log.e("LeToken", "Voici la ${responseData.toString()}")
                            val NewToken = responseData.substring(1)

                            Log.d("LATOKEN", "${NewToken}")
                            if (NewToken.toString() != "null") {
                                // Si la réponse est différente de "null", succès

                                // On ajoute les identifiants (email et token) dans les SharedPreferences
                                val sharedPreferences = requireContext().getSharedPreferences("app_state",
                                    Context.MODE_PRIVATE
                                )
                                val editor = sharedPreferences.edit()
                                editor.putString("email", email)
                                editor.putString("token", NewToken)
                                editor.putBoolean("is_authentificated", true)
                                editor.apply()
                                // C'est Good
                                // On démare la localisation
                                startLocationTracking()
                            }
                        }

                    }
                } catch (e: Exception) {
                    requireActivity().runOnUiThread {
                        Log.e("LoginError", "Erreur: ${e.message}", e)
                        Toast.makeText(requireContext(), "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()



            timer = object : CountDownTimer(totalTimeInMillis, 1000) {

                override fun onTick(millisUntilFinished: Long) {

                    val remainingSeconds = (millisUntilFinished / 1000).toInt()
                    val remainingHours = remainingSeconds / 3600
                    val remainingMinutes = (remainingSeconds % 3600) / 60
                    val remainingSecs = remainingSeconds % 60

                    requireActivity().runOnUiThread {
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
                    sharedPreferences = requireContext().getSharedPreferences("app_state", Context.MODE_PRIVATE)
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
                                requireActivity().runOnUiThread {
                                    Log.e("LeToken", "Voici la ${responseData.toString()}")
                                    val NewToken = responseData.substring(1)
                                    if (NewToken.toString() != "null") {
                                        // Si la réponse est différente de "null", succès

                                        // On ajoute les identifiants (email et token) dans les SharedPreferences
                                        val sharedPreferences = requireContext().getSharedPreferences("app_state",
                                            Context.MODE_PRIVATE
                                        )
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

                                        Toast.makeText(requireContext(), "Alerte délcancher !", Toast.LENGTH_SHORT).show()

                                    }
                                }
                            }
                        } catch (e: Exception) {
                            requireActivity().runOnUiThread {
                                Log.e("LoginError", "Erreur: ${e.message}", e)
                                Toast.makeText(requireContext(), "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }.start()
                }
            }.start()
        } else {
            Toast.makeText(requireContext(), "Veuillez régler une durée valide !", Toast.LENGTH_SHORT).show()
        }
    }


    // Déconnexion de l'utilisateur
    fun logoutUser() {
        val sharedPreferences = requireContext().getSharedPreferences("app_state",
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        editor.putString("email", "")
        editor.putString("token", "null")
        editor.putBoolean("is_authentificated", false)
        editor.apply()
        Toast.makeText(requireContext(), "Déconnexion...", Toast.LENGTH_SHORT).show()
        exitProcess(0)
    }


}