package com.example.sentinelle

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import android.util.Log

class login_activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Récupération du bouton de connexion
        val btn_loginClick = findViewById<Button>(R.id.btnLogin)

        btn_loginClick.setOnClickListener(View.OnClickListener { view: View? ->
            // On récupère tout les champs de la page register
            val editLoginEmail = findViewById<EditText>(R.id.editEmailLogin)
            val editLoginPassword = findViewById<EditText>(R.id.editPasswordLogin)
            val ErrorLogin = findViewById<TextView>(R.id.errorLogin)

            // On récupère les valeurs en Sting des champs remplissable
            val EmailLogin = editLoginEmail.text.toString()
            val PasswordLogin = editLoginPassword.text.toString()



            // On vérifie les informations renseigner
            if(EmailLogin.isEmpty() || PasswordLogin.isEmpty()){
                ErrorLogin.text = "Veuillez renseigner tous les champs"
                ErrorLogin.visibility = View.VISIBLE
            }
            else {
                // On continue les vérifications
                if (Patterns.EMAIL_ADDRESS.matcher(EmailLogin).matches()) {
                    // On lance le processus de connexion
                    login(EmailLogin, PasswordLogin)
                }
                else {
                    editLoginEmail.error = "Numéro invalide"
                }

            }

        })
    }

    private fun login(email: String, password: String) {
        // Ici on envoi tout les champs renseigner par l'utilisateur au serveur

        val url = "https://boutique-casse-tete.com/sentinelle/connexion.php"

        // Créer un client HTTP
        val client = OkHttpClient()

        // Créer le corps de la requête avec les données à envoyer en POST
        val formBody = FormBody.Builder()
            .add("email", email)
            .add("password", password)
            .build()

        // Créer une requête HTTP POST
        val request = Request.Builder()
            .url(url)
            .post(formBody) // Utiliser POST et ajouter le corps
            .build()

        // Exécuter la requête dans un thread de fond pour ne pas bloquer l'interface utilisateur
        Thread {
            try {
                val response = client.newCall(request).execute()
                val responseData: String =
                    response.body?.string().toString() // Récupérer la réponse

                // Vérifier si la réponse est valide
                if (response.isSuccessful) {
                    // Convertir la réponse JSON en JSONObject
                    val jsonResponse = JSONObject(responseData)
                    val message = jsonResponse.getString("message") // Récupérer le message de la réponse
                    val status = jsonResponse.getBoolean("status") // Récupérer le statut de la réponse
                    val token = jsonResponse.getString("token") // Récupérer le statut de la réponse

                    // Mettre à jour l'interface utilisateur selon le statut
                    runOnUiThread {
                        if (status) {
                            // Si le statut est true, succès

                            // On ajoute les identifiants (email et token) dans le shared preferences
                            val sharedPreferences = getSharedPreferences("app_state", MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putString("email", email)
                            editor.putString("token", token)
                            editor.putBoolean("is_authentificated", true)
                            editor.apply()

                            startActivity(Intent(this, activity_home::class.java))


                        } else {
                            // Si le statut est false, erreur
                            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    // Si la réponse n'est pas réussie
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Erreur lors de la connexion de l'utilisateur", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Log.e("LoginError", "Erreur: ${e.message}", e)
                    // Affichage de l'erreur dans l'input de l'adresse mail
                    Toast.makeText(applicationContext, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}
