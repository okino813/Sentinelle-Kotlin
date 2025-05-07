package com.example.sentinelle

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.sentinelle.api.api_service
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class login_activity : AppCompatActivity() {

    private var isSignupMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val api = api_service(this)

        // Récupération du bouton
        val btn_loginClick = findViewById<Button>(R.id.btnConnexion)
        val btn_change_mode = findViewById<TextView>(R.id.btnChangeSingupMode)
        val h2Page = findViewById<TextView>(R.id.h2Connexion)

        // Récupération des champs
        val editEmail = findViewById<TextInputEditText>(R.id.editEmail)
        val editPassword = findViewById<TextInputEditText>(R.id.editPassword)
        val editPasswordConfirm = findViewById<TextInputEditText>(R.id.editConfirmPassword)

        val checkboxCGU = findViewById<CheckBox>(R.id.CheckBoxRegister)

        // Valeur par defaut
        editPasswordConfirm.visibility = View.GONE
        checkboxCGU.visibility = View.GONE
        h2Page.text = "Connexion"


        btn_change_mode.setOnClickListener{
            isSignupMode = !isSignupMode
            if(isSignupMode){
                btn_change_mode.text = "Se connecter"
                btn_loginClick.text = "S'inscrire"
                h2Page.text = "Inscription"
                editPasswordConfirm.visibility = View.VISIBLE
                checkboxCGU.visibility = View.VISIBLE
            }else{
                btn_change_mode.text = "S'inscrire"
                h2Page.text = "Connexion"
                btn_loginClick.text = "Se connecter"
                editPasswordConfirm.visibility = View.GONE
                checkboxCGU.visibility = View.GONE
            }
        }

        btn_loginClick.setOnClickListener {
            val Email = editEmail.text.toString()
            val Password = editPassword.text.toString()
            val PasswordConfirm = editPasswordConfirm.text.toString()
            val isChecked:Boolean = checkboxCGU.isChecked()

            if(isSignupMode){
                // Inscription
                // On vérifie les informations renseigner
                if(Email.isEmpty()){
                    editEmail.error = "Veuillez renseigner votre adresse mail"
                }
                else if(Password.isEmpty()){
                    editPassword.error = "Veuillez renseigner votre mot de passe"
                }
                else if(PasswordConfirm.isEmpty()){
                    editPasswordConfirm.error = "Veuillez confirmer votre mot de passe"
                }
                else {
                    // On vérifie l'email
                    if (Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
                        // On vérifie le mot de passe
                        if (Password == PasswordConfirm) {
                            // Si les conditions générales sont accepté par l'utilisateur
                            if (isChecked) {
                                // Les vérifications sont correcte !
                                api.register(Email, Password)
                                var intent = Intent(this@login_activity, activity_home::class.java)
                                startActivity(intent)
                                finish()

                            } else {
                                Toast.makeText(this, "Vous devez accepter les CGU", Toast.LENGTH_SHORT).show()
                            }

                        }
                        else {
                            editPassword.error = "Mots de passe invalides"
                            editPasswordConfirm.error = "Mots de passe invalides"
                        }
                    }
                    else {
                        editEmail.error = "Adresse mail invalide"
                    }
                }

            }
            else{
                // Scred Connexion
                // On vérifie les informations renseigner
                if(Email.isEmpty()){
                    editEmail.error = "Veuillez renseigner tous les champs"
                }
                else if(Password.isEmpty()){
                    editPassword.error = "Veuillez renseigner tous les champs"
                }
                else {
                    // On continue les vérifications
                    if (Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
                        // On lance le processus de connexion
                        lifecycleScope.launch {
                            val result = api.login(Email, Password)
                            if(result){
                                var intent = Intent(this@login_activity, activity_home::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }
//                    api.testToken()
                    }
                    else {
                        editEmail.error = "Numéro invalide"
                    }

                }
            }
        }
    }
}
