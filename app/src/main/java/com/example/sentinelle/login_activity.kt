package com.example.sentinelle

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.sentinelle.api.Bouton
import com.example.sentinelle.api.Input
import com.example.sentinelle.api.Logo
import com.example.sentinelle.api.Titre
import com.example.sentinelle.api.UpdateStatusBarColor
import com.example.sentinelle.api.api_service
import com.google.android.gms.auth.api.signin.GoogleSignInClient

@Composable
fun FormulaireConnexion(
    colors : List<Color>,
    googleSignInClient: GoogleSignInClient,
    launcher: ActivityResultLauncher<Intent>,
    onLoginSuccess : () -> Unit
) {
    var motDePasse by remember { mutableStateOf("") }
    var motDePasseConfirm by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var inscriptionMode by remember { mutableStateOf(false) }

    // États pour les erreurs
    var emailError by remember { mutableStateOf<String?>(null) }
    var motDePasseError by remember { mutableStateOf<String?>(null) }
    var ConfrimmotDePasseError by remember { mutableStateOf<String?>(null) }

    var checked by remember { mutableStateOf(false) }

    var loginSuccess by remember { mutableStateOf(false) }
    var loginTried by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as MainActivity

    UpdateStatusBarColor(colors[0], context)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colors[0]
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
                    Titre("Inscription", colors = colors)
                } else {
                    Titre("Connexion", colors = colors)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Input("Email", value = email,colors = colors, onValueChange = { email = it }, false, emailError)

                Spacer(modifier = Modifier.height(8.dp))

                Input("Mot de passe",value = motDePasse, colors = colors, onValueChange = { motDePasse = it }, true, errorMessage = motDePasseError)

                Spacer(modifier = Modifier.height(8.dp))

                if (inscriptionMode) {
                    Input("Confirmation du mot de passe", value = motDePasseConfirm,  colors = colors, onValueChange = { motDePasseConfirm = it }, true, errorMessage = ConfrimmotDePasseError)
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
                                checkedColor = colors[1],
                                uncheckedColor = colors[3],
                                checkmarkColor = colors[0]
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
                        Bouton("S'inscrire", colors = colors, OnClick = {
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
                                activity.signUpWithEmail(email, motDePasse) {
                                    success ->
                                    if (success) {
                                        emailError = null
                                        motDePasseError = null
                                        onLoginSuccess()
                                    } else {
                                        emailError = "Email déjà utilisé"
                                    }
                                }
                            }
                        })
                    }
                    else {
                        Bouton("Se connecter", colors = colors, OnClick = {
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
                                val api = api_service(context)
                                activity.signInWithEmail(email, motDePasse) {
                                        success ->
                                    if (success) {
                                        emailError = null
                                        motDePasseError = null
                                        onLoginSuccess()
                                    } else {
                                        emailError = "Email ou mot de passe incorrect"
                                        motDePasseError = "Email ou mot de passe incorrect"
                                    }
                                }
                            }
                        })
                        // Si login réussi → redirection
                        if (loginSuccess && loginTried) {
                            LaunchedEffect(Unit) {
                                onLoginSuccess()
                            }
                        }
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
                    }
                    else {
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
                Divider( // 👈 ligne de séparation
                    color = Color.LightGray,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                )

                {
                    Button(
                        onClick = {
                            Log.d("GoogleSignIn", "Bouton Google cliqué")
                            activity.signInWithGoogle { success ->
                                if (success) onLoginSuccess()
                            }
                        },
                        modifier = Modifier
                            .height(50.dp),
//                        .padding(top = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),

                    )
                    {
                        Image(
                            painter = painterResource(id = R.drawable.google_logo), // ajoute ton icône Google ici
                            contentDescription = "Logo Google",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Connexion avec Google")
                    }
                }
            }
        }
    }
}

