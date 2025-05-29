package com.example.sentinelle


import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.sentinelle.api.AppColors
import com.example.sentinelle.page.HomeScreen
import com.example.sentinelle.page.MapScreen
import com.example.sentinelle.page.MessageScreen
import com.example.sentinelle.page.SettingsScreenPreview
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()

            NavHost(navController, startDestination = Screen.Tuto1.route) {
                composable(Screen.Tuto1.route) { Tuto1() { navController.navigate(Screen.Tuto2.route) } }
                composable(Screen.Tuto2.route) { Tuto2() { navController.navigate(Screen.Tuto3.route) } }
                composable(Screen.Tuto3.route) { Tuto3() { navController.navigate(Screen.Tuto4.route) } }
                composable(Screen.Tuto4.route) { Tuto4() { navController.navigate(Screen.Login.route) } }
                composable(Screen.Login.route) { LoginScreen { /* etc */ } }
            }

            sharedPreferences = this.getSharedPreferences("auth_prefs", MODE_PRIVATE)
            val isAuthentificated = sharedPreferences.getBoolean("is_authentificated", false)

            var isLoggedIn by remember { mutableStateOf(false) }

            if (isLoggedIn) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    enableEdgeToEdge()
                }
                Apps()
            } else {
                LoginScreen(onLoginSuccess = { isLoggedIn = true })
            }

            }
        }
    }
}

        // On récupère la varriable isConnected dans le SharedPreferences pour checker si l'utilisateur est déjà connecté


        // Si la varriable is_authentificated est à True, alors on redirige vers la page principale
        if (isAuthentificated) {
            // Rediriger vers MainActivity_page
            val intent = Intent(this, MainActivity_page()::class.java)
            startActivity(intent)
            finish()
        } else {
            // Si la réponse est "null", le token est invalide


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

data class BottomNavItem(
    val route: String,
    val iconRid: Int,
    val label: String
)

@RequiresApi(Build.VERSION_CODES.Q)
@Preview
@Composable
fun Apps() {
    BottomMenu()
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun BottomMenu(){
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
            composable("settings") { SettingsScreenPreview() }
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
        containerColor = AppColors().SentiBlack,
        contentColor = AppColors().SentiGreen,
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
                        tint = if (currentRoute == item.route) AppColors().SentiGreen else Color.White
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
                    indicatorColor = AppColors().SentiBlack // fond quand l'item est sélectionné
                )
            )
        }
    }
}
