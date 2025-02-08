package com.example.sentinelle

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class tuto_tree_activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tuto_tree)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // On récupère le btn flêche 3
        val btn_fleche_tuto3 = findViewById<ImageView>(R.id.btn_fleche_tuto3)

        btn_fleche_tuto3.setOnClickListener(View.OnClickListener { view: View? ->
            Intent(this, tuto_four_activity::class.java).also {
                startActivity(it)
            }
        })
    }
}