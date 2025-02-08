package com.example.sentinelle

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class home_choice_activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_choice)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // On récupère le btn connexion
        val btn_connexion = findViewById<Button>(R.id.btn_connexion)

        btn_connexion.setOnClickListener(View.OnClickListener { view: View? ->
            Intent(this, login_activity::class.java).also {
                startActivity(it)
            }
        })

        // On récupère le btn d'inscription
        val btn_inscription = findViewById<Button>(R.id.btn_inscription)

        btn_inscription.setOnClickListener(View.OnClickListener { view: View? ->
            Intent(this, activity_register::class.java).also {
                startActivity(it)
            }
        })
    }
}