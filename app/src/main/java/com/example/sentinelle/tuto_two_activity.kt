package com.example.sentinelle

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class tuto_two_activity : AppCompatActivity() {

    private val LOCATION_PERMISSION_REQUEST_CODE: Int = 1

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
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Les permissions ne sont pas accordées, demandez-les
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }else {
                Intent(this, tuto_tree_activity::class.java).also {
                    startActivity(it)
                }
            }
        })
    }
}