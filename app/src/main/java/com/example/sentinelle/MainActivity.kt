package com.example.sentinelle


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import androidx.core.view.WindowCompat
import com.example.sentinelle.api.AppColors
import com.example.sentinelle.api.AppValues
import com.example.sentinelle.api.AppValues.Montserrat
import com.example.sentinelle.api.UpdateStatusBarColor
import com.example.sentinelle.api.api_service
import com.example.sentinelle.page.HomeScreen
import com.example.sentinelle.page.MapScreen
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

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient


    private lateinit var sharedPreferences: SharedPreferences
    private var isLoggedIn = mutableStateOf(false)
    private var isContrast = mutableStateOf(false)
    private lateinit var context: Context

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
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)
        auth = Firebase.auth

        // Configurer Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id)) // Remplace par ton Web client ID
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        sharedPreferences = this.getSharedPreferences("sentinelle", MODE_PRIVATE)
        isLoggedIn.value = sharedPreferences.getBoolean("is_authentificated", false)
        isContrast.value = sharedPreferences.getBoolean("isContraster", false)

        var ctext = Color(0xff399d61)


        checkAndRequestPermissions()

        context = this

        // On vérifie si le mode contraster est activé
        val api = api_service(context)

        setContent {
            if (isLoggedIn.value) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    enableEdgeToEdge()
                    if (isContrast.value) {
                        AppColors.SentiBlack = Color.Black
                        AppColors.SentiGreen = Color.Yellow
                        AppColors.SentiDarkBlue = Color.White
                        AppColors.SentiBlue = Color.Cyan
                        AppColors.SentiCyan = Color.Cyan
                    } else {
                        AppColors.SentiBlack = Color(0xff16252B)
                        AppColors.SentiGreen = Color(0xff399d61)
                        AppColors.SentiDarkBlue = Color(0x33289DD2)
                        AppColors.SentiBlue = Color(0xff0097B2)
                        AppColors.SentiCyan = Color(0xff289DD2)
                    }

                    SentiTheme {
                        BottomMenu(
                            context = context,
                            sharedPreferences = sharedPreferences,
                            isLoggedIn = isLoggedIn,
                            isContrast = isContrast
                        )
                    }
                }
            } else {
                var appState by remember { mutableStateOf<AppState>(AppState.Tutorial) }

                when (appState) {
                    is AppState.Tutorial -> TutorialScreens(onTutorialFinished = {
                        appState = AppState.Auth
                    })

                    is AppState.Auth -> FormulaireConnexion(
                        googleSignInClient = googleSignInClient,
                        launcher = googleSignInLauncher,
                        onLoginSuccess = {
                            isLoggedIn.value = true
                            sharedPreferences.edit().putBoolean("is_authentificated", true).apply()

                            appState = AppState.Main

                    })


                    is AppState.Main ->
                        BottomMenu(
                        context= context,
                        sharedPreferences = sharedPreferences,
                        isLoggedIn = isLoggedIn,
                        isContrast = isContrast
                    )
                }
            }
        }
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
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this, // ✅ ici c'est une Activity, donc pas d'erreur
                missingPermissions.toTypedArray(),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

}


@Composable
fun TutorialScreens(onTutorialFinished: () -> Unit) {
    var index by remember { mutableStateOf(0) }

    when (index) {
        0 -> TutoPage(title = "Un minuteur pas comme les autres ...", text = "Lorsque vous sortez de chez vous, vous programmer un minuteur jusqu'à votre retour !",
            imageRes = R.drawable.logo_minuteur,
            color = AppColors.SentiBlack,
            fleche = R.drawable.fleche_tuto1) {
            index++
        }
        1 -> TutoPage(title = "Suivie de votre parcours", text = "Votre trajet est enregistrer pour vous et vos proche. En cas de problème, ils vous trouverons rapidement !",
            imageRes = R.drawable.logo_carte_tuto2,
            color = AppColors.SentiBlue,
            fleche = R.drawable.fleche_tuto2) {
            index++
        }
        2 -> TutoPage(title = "Environnement sonore", text = "Votre environnement sonore est enregistrer. En cas d’agression, cela peux permettre de prouver la culpabilité de l’agresseur",
            imageRes = R.drawable.logo_record_tuto3,
            color = AppColors.SentiGreen,
            fleche = R.drawable.fleche_tuto3) {
            index++
        }
        3 -> TutoPage(title = "Prévenir vos proches", text = "Ajoutez des contacts pour que Sentinelle puisse les prévenir en cas de danger",
            imageRes = R.drawable.logo_proche_tuto_4,
            color = AppColors.SentiCyan,
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

    // Listes des couleurs :
    var colorList by remember {
        mutableStateOf(
            if (AppValues.isContrasted)
                AppValues.contrastColors
            else
                AppValues.defaultColors
        )
    }

    fun changeColor(index: Int) {
        Log.d("ChangementColor", "Changement de couleur à l'index: $index")
        if(AppValues.isContrasted)
            colorList = AppValues.contrastColors
        else
            colorList = AppValues.defaultColors

        AppValues.isContrasted = !AppValues.isContrasted
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
fun ContentScreen(modifier: Modifier = Modifier, selectedIndex : Int,
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
        1-> MapScreen()
        2-> NavigationTabExample(
            colorList,
            modifier = modifier,
        )
        3-> SettingsScreen(
            modifier = modifier,
            context = context,
            sharedPreferences = sharedPreferences,
            isLoggedIn = isLoggedIn,
            isContrast = isContrast,
            onChangeColor = onChangeColor
        )
    }
}