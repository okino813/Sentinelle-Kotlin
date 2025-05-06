package com.example.sentinelle

import android.os.Bundle
import android.util.Log
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
import com.example.sentinelle.api.api_service
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class activity_register : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Récupération du bouton inscription
        // On récupère le btn d'inscription
        val btn_registerClick = findViewById<Button>(R.id.btnRegister)

        val api = api_service(this)

        btn_registerClick.setOnClickListener(View.OnClickListener { view: View? ->
            // On récupère tout les champs de la page register
            val editEmail = findViewById<EditText>(R.id.editEmailRegister)
            val editPassword = findViewById<EditText>(R.id.editPasswordRegister)
            val editPasswordConfirm = findViewById<EditText>(R.id.editConfirmPasswordRegister)
            val editCheckBox = findViewById<CheckBox>(R.id.CheckBoxRegister)
            val tvError = findViewById<TextView>(R.id.errorRegister)

            // On récupère les valeurs en Sting des champs remplissable
            val Email = editEmail.text.toString()
            val Password = editPassword.text.toString()
            val PasswordConfirm = editPasswordConfirm.text.toString()
            val isChecked:Boolean = editCheckBox.isChecked()



                // On vérifie les informations renseigner
                if(Email.isEmpty() || Password.isEmpty() || PasswordConfirm.isEmpty()){
                    tvError.text = "Veuillez renseigner tous les champs"
                    tvError.visibility = View.VISIBLE
                }
                else {
                    // On continue les vérifications

                            // On vérifie l'email
                            if (Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
                                    // On vérifie le mot de passe
                                    if (Password == PasswordConfirm) {
                                        // Si les conditions générales sont accepté par l'utilisateur
                                        if (isChecked) {
                                            // Les vérifications sont correcte !
                                            // On crée l'utilisateur
                                            api.register(Email, Password)
                                        } else {
                                            tvError.text = "Veuillez accepter les CGU"
                                            tvError.visibility = View.VISIBLE
                                        }

                                    } else {
                                        editPassword.error = "Mots de passe invalides"
                                        editPasswordConfirm.error = "Mots de passe invalides"
                                    }
                            } else {
                                editEmail.error = "Adresse mail invalide"
                            }
                }

        })
    }

    private fun createUser(email: String, password: String, passwordConfirm: String) {
        // URL de ton fichier PHP qui renvoie les données
        val url = "https://boutique-casse-tete.com/sentinelle/createUser.php" // Remplace avec ton URL

        // Créer un client HTTP
        val client = OkHttpClient()

        // Créer le corps de la requête avec les données à envoyer en POST
        val formBody = FormBody.Builder()
            .add("email", email)
            .add("password", password)
            .add("passwordConfirm", passwordConfirm)
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

                Log.d("responseData", responseData)

                // Vérifier si la réponse est valide
                if (response.isSuccessful) {
                    // Convertir la réponse JSON en JSONObject
                    val jsonResponse = JSONObject(responseData)
                    val message = jsonResponse.getString("message") // Récupérer le message de la réponse
                    val status = jsonResponse.getBoolean("status") // Récupérer le statut de la réponse

                    // Mettre à jour l'interface utilisateur selon le statut
                    runOnUiThread {
                        if (status) {
                            // Si le statut est true, succès
                            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                        } else {
                            // Si le statut est false, erreur
                            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    // Si la réponse n'est pas réussie
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Erreur lors de la création de l'utilisateur", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    // Affichage de l'erreur dans l'input de l'adresse mail
                    val editTextEmail = findViewById<EditText>(R.id.editEmailRegister)
                    editTextEmail.error = "Erreur: ${e.message}" // Affiche l'erreur dans l'input
                    Toast.makeText(applicationContext, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}