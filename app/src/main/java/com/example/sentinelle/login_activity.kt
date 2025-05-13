package com.example.sentinelle

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sentinelle.api.AppColors
import com.example.sentinelle.api.Bouton
import com.example.sentinelle.api.Input
import com.example.sentinelle.api.Logo
import com.example.sentinelle.api.Titre

@Composable
fun FormulaireConnexion() {
    var motDePasse by remember { mutableStateOf("") }
    var motDePasseConfirm by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var inscriptionMode by remember { mutableStateOf(true) }

    // États pour les erreurs
    var emailError by remember { mutableStateOf<String?>(null) }
    var motDePasseError by remember { mutableStateOf<String?>(null) }
    var ConfrimmotDePasseError by remember { mutableStateOf<String?>(null) }

    var checked by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppColors().SentiBlack
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Ici qu'on va mettre le contenu de la page
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Logo()

                if (inscriptionMode) {
                    Titre("Inscription")
                } else {
                    Titre("Connexion")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Input("Email", value = email, onValueChange = { email = it }, false, emailError)

                Spacer(modifier = Modifier.height(8.dp))

                Input(
                    "Mot de passe",
                    value = motDePasse,
                    onValueChange = { motDePasse = it },
                    true,
                    errorMessage = motDePasseError
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (inscriptionMode) {
                    Input(
                        "Confirmation du mot de passe",
                        value = motDePasseConfirm,
                        onValueChange = { motDePasseConfirm = it },
                        true,
                        errorMessage = ConfrimmotDePasseError
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (inscriptionMode) {
                    // Check box et test pour accepter les CGU
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { checked = it },
                            colors = androidx.compose.material3.CheckboxDefaults.colors(
                                checkedColor = AppColors().SentiGreen,
                                uncheckedColor = AppColors().SentiCyan,
                                checkmarkColor = AppColors().SentiBlack
                            )
                        )
                        Spacer(modifier = Modifier.padding(8.dp))

                        Text(
                            text = "J'accepte les Conditions Générales d'utilisations",
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceAround
                )
                {
                    if (inscriptionMode) {
                        Bouton("S'inscrire'", OnClick = {
                            // Reset des erreurs
                            emailError = null
                            motDePasseError = null
                            ConfrimmotDePasseError = null

                            // Validation
                            var isValid = true
                            if (!email.contains("@")) {
                                emailError = "Email invalide"
                                isValid = false
                            }
                            if (motDePasse.length < 6) {
                                motDePasseError = "Au moins 6 caractères"
                                isValid = false
                            }

                            if (motDePasseConfirm != motDePasse) {
                                ConfrimmotDePasseError = "Les mots de passe ne correspondent pas"
                                isValid = false
                            }

                            if (!checked) {
                                Toast.makeText(
                                    context,
                                    "Vous devez accepter les CGU",
                                    Toast.LENGTH_SHORT
                                ).show()
                                isValid = false
                            }

                            if (isValid) {
                                val action = "Inscription"
                                Toast.makeText(context, "$action réussie !", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        })
                    } else {
                        Bouton("Se connecter", OnClick = {
                            // Reset des erreurs
                            emailError = null
                            motDePasseError = null

                            // Validation
                            var isValid = true
                            if (!email.contains("@")) {
                                emailError = "Email invalide"
                                isValid = false
                            }
                            if (motDePasse.length < 6) {
                                motDePasseError = "Au moins 6 caractères"
                                isValid = false
                            }

                            if (isValid) {
                                val action = "Connexion"
                                Toast.makeText(context, "$action réussie !", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        })
                    }

                    if (inscriptionMode) {
                        Text(
                            text = "Se connecter",
                            // ALign center
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            modifier = Modifier
                                .padding(16.dp)
                                .clickable {
                                    inscriptionMode = !inscriptionMode
                                }
                        )
                    } else {
                        Text(
                            text = "Crée un compte",
                            // ALign center
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            modifier = Modifier
                                .padding(16.dp)
                                .clickable {
                                    inscriptionMode = !inscriptionMode
                                }
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewPages(){
    FormulaireConnexion()
}

class login_activity : ComponentActivity() {

    private var isSignupMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                PreviewPages()
            }
        }















//
//        val api = api_service(this)
//
//        // Récupération du bouton
//        val btn_loginClick = findViewById<Button>(R.id.btnConnexion)
//        val btn_change_mode = findViewById<TextView>(R.id.btnChangeSingupMode)
//        val h2Page = findViewById<TextView>(R.id.h2Connexion)
//
//        // Récupération des champs
//        val editEmail = findViewById<TextInputEditText>(R.id.editEmail)
//        val editPassword = findViewById<TextInputEditText>(R.id.editPassword)
//        val editPasswordConfirm = findViewById<TextInputEditText>(R.id.editConfirmPassword)
//
//        val checkboxCGU = findViewById<CheckBox>(R.id.CheckBoxRegister)
//
//        // Valeur par defaut
//        editPasswordConfirm.visibility = View.GONE
//        checkboxCGU.visibility = View.GONE
//        h2Page.text = "Connexion"
//
//
//        btn_change_mode.setOnClickListener{
//            isSignupMode = !isSignupMode
//            if(isSignupMode){
//                btn_change_mode.text = "Se connecter"
//                btn_loginClick.text = "S'inscrire"
//                h2Page.text = "Inscription"
//                editPasswordConfirm.visibility = View.VISIBLE
//                checkboxCGU.visibility = View.VISIBLE
//            }else{
//                btn_change_mode.text = "S'inscrire"
//                h2Page.text = "Connexion"
//                btn_loginClick.text = "Se connecter"
//                editPasswordConfirm.visibility = View.GONE
//                checkboxCGU.visibility = View.GONE
//            }
//        }
//
//        btn_loginClick.setOnClickListener {
//            val Email = editEmail.text.toString()
//            val Password = editPassword.text.toString()
//            val PasswordConfirm = editPasswordConfirm.text.toString()
//            val isChecked:Boolean = checkboxCGU.isChecked()
//
//            if(isSignupMode){
//                // Inscription
//                // On vérifie les informations renseigner
//                if(Email.isEmpty()){
//                    editEmail.error = "Veuillez renseigner votre adresse mail"
//                }
//                else if(Password.isEmpty()){
//                    editPassword.error = "Veuillez renseigner votre mot de passe"
//                }
//                else if(PasswordConfirm.isEmpty()){
//                    editPasswordConfirm.error = "Veuillez confirmer votre mot de passe"
//                }
//                else {
//                    // On vérifie l'email
//                    if (Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
//                        // On vérifie le mot de passe
//                        if (Password == PasswordConfirm) {
//                            // Si les conditions générales sont accepté par l'utilisateur
//                            if (isChecked) {
//                                // Les vérifications sont correcte !
//                                api.register(Email, Password)
//                                var intent = Intent(this@login_activity, MainActivity_page::class.java)
//                                startActivity(intent)
//                                finish()
//                            } else {
//                                Toast.makeText(this, "Vous devez accepter les CGU", Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                        else {
//                            editPassword.error = "Mots de passe invalides"
//                            editPasswordConfirm.error = "Mots de passe invalides"
//                        }
//                    }
//                    else {
//                        editEmail.error = "Adresse mail invalide"
//                    }
//                }
//            }
//            else{
//                // Scred Connexion
//                // On vérifie les informations renseigner
//                if(Email.isEmpty()){
//                    editEmail.error = "Veuillez renseigner tous les champs"
//                }
//                else if(Password.isEmpty()){
//                    editPassword.error = "Veuillez renseigner tous les champs"
//                }
//                else {
//                    // On continue les vérifications
//                    if (Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
//                        // On lance le processus de connexion
//                        lifecycleScope.launch {
//                            val result = api.login(Email, Password)
//                            if(result){
//                                var intent = Intent(this@login_activity, activity_home::class.java)
//                                startActivity(intent)
//                                finish()
//                            }
//                        }
////                    api.testToken()
//                    }
//                    else {
//                        editEmail.error = "Numéro invalide"
//                    }
//
//                }
//            }
//        }
//    }
//}

    }
}

