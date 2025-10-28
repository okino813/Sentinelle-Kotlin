package com.example.sentinelle


import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.example.sentinelle.api.AppValues
import com.example.sentinelle.api.AppValues.Montserrat
import com.example.sentinelle.api.UpdateStatusBarColor
import com.example.sentinelle.api.api_service
import com.example.sentinelle.page.AppNavigation
import com.example.sentinelle.page.HomeScreen
import com.example.sentinelle.page.NavigationTabExample
import com.example.sentinelle.page.SettingsScreen
import com.example.sentinelle.ui.theme.SentiTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient


    private lateinit var sharedPreferences: SharedPreferences
    private var isLoggedIn = mutableStateOf(false)
    private var isContrast = mutableStateOf(false)
    private lateinit var context: Context

    private val AUDIO_PERMISSION_REQUEST_CODE = 200
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS
    )

    private var googleSignInCallback: ((Boolean) -> Unit)? = null

    private fun getStatusCodeMeaning(statusCode: Int): String {
        return when (statusCode) {
            12500 -> "SIGN_IN_REQUIRED - Config Firebase manquante"
            12501 -> "SIGN_IN_CANCELLED - Utilisateur a annulé"
            12502 -> "SIGN_IN_CURRENTLY_IN_PROGRESS"
            12600 -> "SIGN_IN_FAILED - Échec général"
            10 -> "DEVELOPER_ERROR - Config incorrecte (SHA-1, Client ID...)"
            7 -> "NETWORK_ERROR - Problème réseau"
            4 -> "SIGN_IN_REQUIRED - Pas de compte"
            else -> "Code inconnu: $statusCode"
        }
    }

    private var googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.data != null) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                if (task.exception != null) {
                    val exception = task.exception!!
                    if (exception is ApiException) {
                        val apiException = exception as ApiException
                    }
                } else {
                    if (task.isSuccessful) {
                        val account = task.result
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleGoogleSignIn(task) { success ->
                googleSignInCallback?.invoke(success)
                googleSignInCallback = null
            }
        } else {
            googleSignInCallback?.invoke(false)
            googleSignInCallback = null
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        if (!isNetworkAvailable()) {
            showNoInternetDialog()
            return // Arrête l'exécution du reste de onCreate
        }

        auth = Firebase.auth

        // Configurer Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        sharedPreferences = this.getSharedPreferences("sentinelle", MODE_PRIVATE)
        isLoggedIn.value = sharedPreferences.getBoolean("is_authentificated", false)
        isContrast.value = sharedPreferences.getBoolean("isContraster", false)

        checkTokenValidity()

        context = this

        // Demande les permissions dès le lancement
        checkAndRequestPermissions()
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkTokenValidity() {
        val user = auth.currentUser

        if (user == null) {
            // Pas d'utilisateur → pas connecté
            Log.d("AUTH_CHECK", "Aucun utilisateur Firebase")
            isLoggedIn.value = false
            sharedPreferences.edit().putBoolean("is_authentificated", false).apply()
            setupUI()
            return
        }

        // ✅ Vérifier si le token est valide
        user.getIdToken(true) // true = force le refresh du token
            .addOnSuccessListener { result ->
                if (result.token != null) {
                    // Token valide → utilisateur vraiment connecté
                    Log.d("AUTH_CHECK", "Token Firebase valide")
                    isLoggedIn.value = true
                    sharedPreferences.edit().putBoolean("is_authentificated", true).apply()
                    setupUI()
                } else {
                    // Pas de token → déconnecter
                    Log.w("AUTH_CHECK", "Token Firebase null")
                    forceSignOut()
                }
            }
            .addOnFailureListener { exception ->
                // Erreur lors de la récupération du token → déconnecter
                Log.e("AUTH_CHECK", "Erreur token Firebase: ${exception.message}")
                forceSignOut()
            }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun forceSignOut() {
        Log.d("AUTH_CHECK", "Déconnexion forcée : token invalide ou expiré")
        auth.signOut()
        googleSignInClient.signOut()
        isLoggedIn.value = false
        sharedPreferences.edit().putBoolean("is_authentificated", false).apply()
        setupUI()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setupUI() {
        // Demande les permissions dès le lancement
        checkAndRequestPermissions()

        setContent {
            if (isLoggedIn.value) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    enableEdgeToEdge()

                    SentiTheme {
                        BottomMenu(
                            context = context,
                            sharedPreferences = sharedPreferences,
                            isLoggedIn = isLoggedIn,
                            isContrast = isContrast
                        )
                    }
                    printFirebaseToken()
                }
            } else {
                var appState by remember { mutableStateOf<AppState>(AppState.Tutorial) }

                when (appState) {
                    is AppState.Tutorial -> TutorialScreens(
                        colors = AppValues.defaultColors,
                        onTutorialFinished = {
                            appState = AppState.Auth
                        })

                    is AppState.Auth -> FormulaireConnexion(
                        colors = AppValues.defaultColors,
                        googleSignInClient = googleSignInClient,
                        launcher = googleSignInLauncher,
                        onLoginSuccess = {
                            isLoggedIn.value = true
                            sharedPreferences.edit().putBoolean("is_authentificated", true).apply()
                            appState = AppState.Main
                            checkAndRequestPermissions()
                        })

                    is AppState.Main ->
                        BottomMenu(
                            context = context,
                            sharedPreferences = sharedPreferences,
                            isLoggedIn = isLoggedIn,
                            isContrast = isContrast
                        )
                }
            }
        }
    }


    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            networkInfo?.isConnected == true
        }
    }

    private fun showNoInternetDialog() {
        AlertDialog.Builder(this)
            .setTitle("Pas de connexion Internet")
            .setMessage("Sentinelle nécessite une connexion Internet pour fonctionner.\n\nVeuillez activer votre connexion et redémarrer l'application.")
            .setPositiveButton("Fermer") { _, _ ->
                finish() // Ferme l'application
            }
            .setCancelable(false) // Empêche de fermer le dialogue en appuyant ailleurs
            .show()
    }


    private fun printFirebaseToken() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.getIdToken(false)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result?.token
                Log.d("FIREBASE_TOKEN", "Token: $token")
            } else {
                Log.e("FIREBASE_TOKEN", "Impossible de récupérer le token", task.exception)
            }
        }
    }

    private fun requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

                AlertDialog.Builder(this)
                    .setTitle("Permission de localisation en arrière-plan")
                    .setMessage("Pour votre sécurité, l'application a besoin d'accéder à votre localisation même en arrière-plan.")
                    .setPositiveButton("Autoriser") { _, _ ->
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                            LOCATION_PERMISSION_REQUEST_CODE
                        )
                    }
                    .setNegativeButton("Plus tard", null)
                    .show()
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            AUDIO_PERMISSION_REQUEST_CODE -> {
                Log.d("PERMISSIONS", "Réponse reçue pour AUDIO_PERMISSION_REQUEST_CODE")

                val granted = mutableListOf<String>()
                val denied = mutableListOf<String>()

                for (i in permissions.indices) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        granted.add(permissions[i])
                    } else {
                        denied.add(permissions[i])
                    }
                }

                Log.d("PERMISSIONS", "Accordées: $granted")
                Log.d("PERMISSIONS", "Refusées: $denied")

                if (denied.isEmpty()) {
                    Toast.makeText(this, "Toutes les permissions accordées ✅", Toast.LENGTH_SHORT).show()

                    // Si on est sur Android 10+ et qu'on n'a pas encore la permission background
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        requestBackgroundLocationPermission()
                    }
                } else {
                    showPermissionDeniedDialog()
                }
            }

            LOCATION_PERMISSION_REQUEST_CODE -> {
                Log.d("PERMISSIONS", "Réponse reçue pour LOCATION_PERMISSION_REQUEST_CODE")
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission de localisation en arrière-plan accordée ✅", Toast.LENGTH_SHORT).show()
                } else {
                    Log.w("PERMISSIONS", "Permission de localisation en arrière-plan refusée")
                }
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permissions requises")
            .setMessage("Sentinelle a besoin de ces permissions pour assurer votre sécurité :\n\n" +
                    "• 📍 Localisation : pour suivre votre parcours\n" +
                    "• 🎤 Microphone : pour enregistrer l'environnement sonore\n\n" +
                    "• 🔔 Notifications : pour vous alerter en cas d'urgence\n\n" +
                    "Ces données restent privées et ne sont utilisées qu'en cas d'urgence.")
            .setPositiveButton("Réessayer") { _, _ ->
                checkAndRequestPermissions()
            }
            .setNegativeButton("Continuer sans") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "⚠️ Certaines fonctionnalités seront limitées", Toast.LENGTH_LONG).show()
            }
            .setCancelable(false)
            .show()
    }

    private fun hasAllPermissions(): Boolean {
        val basicPermissionsGranted = REQUIRED_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }

        val backgroundLocationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Pas nécessaire sur les versions antérieures
        }

        return basicPermissionsGranted && backgroundLocationGranted
    }



    fun signInWithGoogle(callback: (Boolean) -> Unit) {
        Log.d("GoogleSignIn", "Début de la connexion Google")

        try {
            googleSignInCallback = callback
            val signInIntent = googleSignInClient.signInIntent

            googleSignInLauncher.launch(signInIntent)

        } catch (e: Exception) {
            e.printStackTrace()
            callback(false)
        }
    }


    private fun handleGoogleSignIn(task: Task<GoogleSignInAccount>, callback: (Boolean) -> Unit){
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { signInTask ->
                    if (signInTask.isSuccessful) {
                        // Connexion réussie
                        val user = auth.currentUser
                        sharedPreferences.edit().putBoolean("is_authentificated", true).apply()
                        isLoggedIn.value = true

                        Toast.makeText(this, "Connexion Google réussie", Toast.LENGTH_SHORT).show()
                        callback(true)
                    } else {
                        // Échec
                        Toast.makeText(this, "Échec de la connexion : ${signInTask.exception?.message}", Toast.LENGTH_LONG).show()
                        callback(false)
                    }
                }
        } catch (e: ApiException) {
            Toast.makeText(this, "Erreur Google SignIn : ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    // ✅ Fonctions pour email/password
    fun signInWithEmail(email: String, password: String, callback: (Boolean) -> Unit){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(this, "Connecté !", Toast.LENGTH_SHORT).show()
                    // Mettre à jour l'état de connexion
                    isLoggedIn.value = true
                    sharedPreferences.edit().putBoolean("is_authentificated", true).apply()
                    callback(true)
                } else {
                    Toast.makeText(this, "Erreur: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    callback(false)
                }
            }
    }

    fun signUpWithEmail(email: String, password: String, callback: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(this, "Compte créé !", Toast.LENGTH_SHORT).show()
                    // Mettre à jour l'état de connexion
                    isLoggedIn.value = true
                    sharedPreferences.edit().putBoolean("is_authentificated", true).apply()
                    callback(true)
                } else {
                    Toast.makeText(this, "Erreur: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    callback(false)
                }
            }
    }

    // Déconnexion
    fun signOut() {
        auth.signOut()
        googleSignInClient.signOut()
        Toast.makeText(this, "Déconnecté", Toast.LENGTH_SHORT).show()
    }

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    private fun checkAndRequestPermissions() {
        Log.d("PERMISSIONS", "Vérification des permissions")

        val allPermissions = mutableListOf<String>().apply {
            // Permissions de base
            addAll(REQUIRED_PERMISSIONS)

            // Permission de localisation en arrière-plan pour Android 10+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }

        val missingPermissions = allPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            Log.d("PERMISSIONS", "Permissions manquantes: $missingPermissions")

            // Pour Android 11+, demander d'abord les permissions normales, puis la localisation en arrière-plan
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                missingPermissions.contains(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {

                // Séparer les permissions
                val normalPermissions = missingPermissions.filter {
                    it != Manifest.permission.ACCESS_BACKGROUND_LOCATION
                }

                if (normalPermissions.isNotEmpty()) {
                    // D'abord demander les permissions normales
                    ActivityCompat.requestPermissions(
                        this,
                        normalPermissions.toTypedArray(),
                        AUDIO_PERMISSION_REQUEST_CODE
                    )
                } else {
                    // Si seule la permission background est manquante
                    requestBackgroundLocationPermission()
                }
            } else {
                // Demander toutes les permissions d'un coup pour les versions antérieures
                ActivityCompat.requestPermissions(
                    this,
                    missingPermissions.toTypedArray(),
                    AUDIO_PERMISSION_REQUEST_CODE
                )
            }
        } else {
            Log.d("PERMISSIONS", "Toutes les permissions sont accordées")
        }
    }

}


@Composable
fun TutorialScreens(colors : List<Color>, onTutorialFinished: () -> Unit) {
    var index by remember { mutableStateOf(0) }

    when (index) {
        0 -> TutoPage(title = "Un minuteur pas comme les autres ...", text = "Lorsque vous sortez de chez vous, vous programmer un minuteur jusqu'à votre retour !",
            imageRes = R.drawable.logo_minuteur,
            color = colors[0],
            fleche = R.drawable.fleche_tuto1) {
            index++
        }
        1 -> TutoPage(title = "Suivie de votre parcours", text = "Votre trajet est enregistrer pour vous et vos proche. En cas de problème, ils vous trouverons rapidement !",
            imageRes = R.drawable.logo_carte_tuto2,
            color = colors[3],
            fleche = R.drawable.fleche_tuto2) {
            index++
        }
        2 -> TutoPage(title = "Environnement sonore", text = "Votre environnement sonore est enregistrer. En cas d’agression, cela peux permettre de prouver la culpabilité de l’agresseur",
            imageRes = R.drawable.logo_record_tuto3,
            color = colors[1],
            fleche = R.drawable.fleche_tuto3) {
            index++
        }
        3 -> TutoPage(title = "Prévenir vos proches", text = "Ajoutez des contacts pour que Sentinelle puisse les prévenir en cas de danger",
            imageRes = R.drawable.logo_proche_tuto_4,
            color = colors[4],
            fleche = R.drawable.fleche_tuto4) {
            onTutorialFinished()
        }
    }
}

@Composable
fun TutoPage(title: String, text: String, imageRes: Int, color: Color, fleche: Int, onNext: () -> Unit) {
    UpdateStatusBarColor(color, LocalContext.current)
    Surface(modifier = Modifier.fillMaxSize(), color = color) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(painter = painterResource(id = imageRes), contentDescription = null, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.padding(10.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
            )
            Spacer(modifier = Modifier.padding(10.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                fontFamily = Montserrat,
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Normal,
            )
            Spacer(modifier = Modifier.padding(20.dp))

            Image(
                painter = painterResource(id = fleche),
                contentDescription = "Fleche suivant",
                modifier = Modifier.align(Alignment.CenterHorizontally)
                    .clickable{
                            onNext()
                    }
            )
        }
    }
}

// ************ Si l'utilisateur est connecté ************

data class BottomNavItem(
    val route: String,
    val iconRid: Int,
    val label: String
)


@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun BottomMenu(
    context: Context,
    sharedPreferences: SharedPreferences,
    isLoggedIn: MutableState<Boolean>,
    isContrast: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {

    AppValues.isContrasted = sharedPreferences.getBoolean("isContraster", false)

    var colorList by remember { mutableStateOf(AppValues.defaultColors) }

    // On vérifie si le mode contraster est activé
    if(AppValues.isContrasted) {
        colorList = AppValues.contrastColors
    }
    else{
        colorList = AppValues.defaultColors
    }

    fun changeColor(index: Int) {
        AppValues.isContrasted = !AppValues.isContrasted

        if(AppValues.isContrasted)
            colorList = AppValues.contrastColors
        else
            colorList = AppValues.defaultColors
        sharedPreferences.edit().putBoolean("isContraster", AppValues.isContrasted).commit()
    }

    val navItemList = listOf(
        NavItem("Home", R.drawable.home,0),
        NavItem("Maps", R.drawable.map,0),
        NavItem("Messages", R.drawable.message,0),
        NavItem("Paramètres", R.drawable.settings,0),
    )


    var selectedIndex by remember {
        mutableIntStateOf(0)
    }
    val api = api_service(context)
    api.getInfo(context)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = colorList[0],
                contentColor = colorList[1],
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawLine(
                            color = Color.White,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
            ) {
                navItemList.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected =  selectedIndex == index ,
                        onClick = {
                            selectedIndex = index
                        },

                        icon = {
                            BadgedBox(badge = {
                                if(navItem.badgeCount>0)
                                    Badge(){
                                        Text(text = navItem.badgeCount.toString())
                                    }
                            }) {
                                Icon(
                                    painter = painterResource(id = navItem.icon),
                                    modifier = if (selectedIndex == index) Modifier.size(40.dp) else Modifier.size(30.dp),
                                    contentDescription = "Icon",

                                )
                            }

                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = colorList[0],
                            selectedIconColor = colorList[1],
                            unselectedIconColor = Color.White,
                        ),
                    )
                }
            }
        }
    ) { innerPadding ->
        ContentScreen(
            modifier = Modifier.padding(innerPadding),
            selectedIndex,
            context = context,
            sharedPreferences = sharedPreferences,
            isLoggedIn = isLoggedIn,
            isContrast = isContrast,
            colorList = colorList,
            onChangeColor = ::changeColor
        )
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun ContentScreen(modifier: Modifier, selectedIndex : Int,
    context: Context,
    sharedPreferences: SharedPreferences,
    isLoggedIn: MutableState<Boolean>,
    isContrast: MutableState<Boolean>,
    colorList: List<Color>,
    onChangeColor: (Int) -> Unit
){
    when(selectedIndex){
        0-> HomeScreen(
            colorList,
            modifier = modifier
        )
        1-> AppNavigation(
            colorList,
            AppValues.saferiders,
        )
        2-> NavigationTabExample(
            colorList,
            modifier = modifier,
        )
        3-> SettingsScreen(
            modifier = modifier,
            context = context,
            colors = colorList,
            sharedPreferences = sharedPreferences,
            isLoggedIn = isLoggedIn,
            isContrast = isContrast,
            onChangeColor = onChangeColor,
            googleSignInClient = GoogleSignIn.getClient(
                context,
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.web_client_id))
                    .requestEmail()
                    .build()

        )
        )
    }
}