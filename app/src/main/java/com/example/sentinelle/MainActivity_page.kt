package com.example.sentinelle

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import com.example.sentinelle.api.api_service

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

