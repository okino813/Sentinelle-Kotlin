package com.example.sentinelle

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
import com.example.sentinelle.api.api_service
import com.example.sentinelle.page.HomeScreen
import com.example.sentinelle.page.MapScreen
import com.example.sentinelle.page.MessageScreen
import com.example.sentinelle.page.SettingsScreenPreview

class MainActivity_page : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Apps()
        }

        val apiService = api_service(this)
        apiService.getInfo(this)

    }
}
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
//        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
//
//        // Charger le fragment par défaut
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.fragment_container, HomeFragment())
//            .commit()
//
//        bottomNav.setOnItemSelectedListener {
//            when (it.itemId) {
//                R.id.home -> {
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.fragment_container, HomeFragment())
//                        .commit()
//                    true
//                }
//                R.id.map -> {
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.fragment_container, MapFragment())
//                        .commit()
//                    true
//                }
//                R.id.message_contact -> {
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.fragment_container, MessageFragment())
//                        .commit()
//                    true
//                }
//                R.id.settings -> {
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.fragment_container, SettingsFragment())
//                        .commit()
//                    true
//                }
//                else -> false
//            }
//        }
//    }
//}

