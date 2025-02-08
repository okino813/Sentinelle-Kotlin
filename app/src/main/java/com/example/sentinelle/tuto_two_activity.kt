package com.example.sentinelle

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class tuto_two_activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tuto_two)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // On récupère le btn flêche 2
        val btn_fleche_tuto2 = findViewById<ImageView>(R.id.btn_fleche_tuto2)

        btn_fleche_tuto2.setOnClickListener(View.OnClickListener { view: View? ->
            Intent(this, tuto_tree_activity::class.java).also {
                startActivity(it)
            }
        })
    }
}