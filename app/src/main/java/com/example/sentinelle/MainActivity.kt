package com.example.sentinelle


import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.sentinelle.api.AppColors
import com.example.sentinelle.api.api_service
import com.example.sentinelle.page.HomeScreen
import com.example.sentinelle.page.MapScreen
import com.example.sentinelle.page.MessageScreen
import com.example.sentinelle.page.SettingsScreen
import com.example.sentinelle.page.tuto.Tuto1
import com.example.sentinelle.page.tuto.Tuto2
import com.example.sentinelle.page.tuto.Tuto3
import com.example.sentinelle.page.tuto.Tuto4

sealed class Screen(val route: String) {
    object Tuto1 : Screen("tuto1")
    object Tuto2 : Screen("tuto2")
    object Tuto3 : Screen("tuto3")
    object Tuto4 : Screen("tuto4")
    object Login : Screen("login")
}


class MainActivity : ComponentActivity() {

    private lateinit var sharedPreferences : SharedPreferences
    private var  isLoggedIn = mutableStateOf(false)
    private var  isContrast = mutableStateOf(false)
    private lateinit var context: Context

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = this.getSharedPreferences("sentinelle", MODE_PRIVATE)
        isLoggedIn.value = sharedPreferences.getBoolean("is_authentificated", false)
        isContrast.value = sharedPreferences.getBoolean("isContraster", false)

        context = this

        Log.d("MainActivity", "onCreate: isLoggedIn = ${isLoggedIn.value}, isContast = ${isContrast.value}")

        // On vérifie si le mode contraster est activé
        if(isContrast.value)
        {
            AppColors.SentiBlack = Color.Black
            AppColors.SentiGreen = Color.Yellow
            AppColors.SentiDarkBlue = Color.White
            AppColors.SentiBlue = Color.White
            AppColors.SentiCyan = Color.Cyan
        }
        else{
            AppColors.SentiBlack = Color(0xff16252B)
            AppColors.SentiGreen = Color(0xff399d61)
            AppColors.SentiDarkBlue = Color(0x33289DD2)
            AppColors.SentiBlue = Color(0xff0097B2)
            AppColors.SentiCyan = Color(0xff289DD2)
        }


        val api = api_service(context)


        setContent {
           if (isLoggedIn.value) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    enableEdgeToEdge()
                    api.getInfo(context)
                    BottomMenu(
                        context = context,
                        sharedPreferences = sharedPreferences,
                        isLoggedIn = isLoggedIn,
                        isContrast = isContrast
                    )
                }
            } else {
                val navController = rememberNavController()

                NavHost(navController, startDestination = Screen.Tuto1.route) {
                    composable(Screen.Tuto1.route) { Tuto1() { navController.navigate(Screen.Tuto2.route) } }
                    composable(Screen.Tuto2.route) { Tuto2() { navController.navigate(Screen.Tuto3.route) } }
                    composable(Screen.Tuto3.route) { Tuto3() { navController.navigate(Screen.Tuto4.route) } }
                    composable(Screen.Tuto4.route) { Tuto4() { navController.navigate(Screen.Login.route) } }
                    composable(Screen.Login.route) { FormulaireConnexion(onLoginSuccess = {
                        // Mise à jour de l'état quand l'utilisateur se connecte
                        isLoggedIn.value = true
                        // Sauvegarde dans SharedPreferences
                        sharedPreferences.edit().putBoolean("is_authentificated", true).apply()
                    })
                    }
                }
            }
        }
    }
}


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
){
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen() }
            composable("map") { MapScreen() }
            composable("message") { MessageScreen() }
            composable("settings") { SettingsScreen(
                context = context,
                sharedPreferences = sharedPreferences,
                isLoggedIn = isLoggedIn,
                isContrast = isContrast
            ) }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("home", R.drawable.home, "Accueil"),
        BottomNavItem("map",  R.drawable.map, "Carte"),
        BottomNavItem("message", R.drawable.message, "Messages"),
        BottomNavItem("settings", R.drawable.settings, "Paramètres")
    )

    NavigationBar(
        containerColor = AppColors.SentiBlack,
        contentColor = AppColors.SentiGreen,
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind{
                drawLine(
                    color = Color.White,
                    start = Offset(0f,0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 2.dp.toPx()
                )
            }
    ) {

        val currentBackStack by navController.currentBackStackEntryAsState()
        val currentRoute = currentBackStack?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = item.iconRid),
                        contentDescription = item.label,
                        modifier = if (currentRoute == item.route) Modifier.size(40.dp) else Modifier.size(30.dp),
                        tint = if (currentRoute == item.route) AppColors.SentiGreen else Color.White
                    )
                },
                selected = currentRoute == item.route,

                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = AppColors.SentiBlack // fond quand l'item est sélectionné
                )
            )
        }
    }
}
