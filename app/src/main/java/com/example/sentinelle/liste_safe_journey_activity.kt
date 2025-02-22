package com.example.sentinelle

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sentinelle.Journey

class liste_safe_journey_activity : AppCompatActivity() {
    lateinit var listView: ListView
    var listItems = ArrayList<Journey>()
    lateinit var adapter: JourneyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_liste_safe_journey)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // Data de la liste
        listView = findViewById(R.id.ListViewSafeJourney)
        listItems = arrayListOf(
            Journey("Lundi 11 Janvier", "Trajet sécurisé de test",R.drawable.main_dark_logo),
            Journey("Lundi 12 Janvier", "Trajet sécurisé de test",R.drawable.main_dark_logo),
            Journey("Lundi 12 Janvier", "Trajet sécurisé de test",R.drawable.main_dark_logo),
            Journey("Lundi 13 Janvier", "Trajet sécurisé de test",R.drawable.main_dark_logo),
            Journey("Lundi 14 Janvier", "Trajet sécurisé de test",R.drawable.main_dark_logo),
            Journey("Lundi 15 Janvier", "Trajet sécurisé de test",R.drawable.main_dark_logo),
            )

        adapter = JourneyAdapter(this, R.layout.item_journey, listItems)
        listView.adapter = adapter

        listView.setOnItemClickListener{ parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position) as String
            Toast.makeText(this, "Tu as sélectionner $selectedItem", Toast.LENGTH_SHORT).show()
        }



    }
}