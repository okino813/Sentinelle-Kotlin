package com.example.sentinelle.page

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sentinelle.api.AppColors
import com.example.sentinelle.api.AppValues
import com.example.sentinelle.api.Bouton
import com.example.sentinelle.api.Input
import com.example.sentinelle.api.PopupAlert
import com.example.sentinelle.api.api_service


@Composable
fun SettingsScreen() {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var firstname by remember { mutableStateOf("") }
    var firstnameError by remember { mutableStateOf<String?>(null) }

    var lastname by remember { mutableStateOf("") }
    var lastnameError by remember { mutableStateOf<String?>(null) }

    var phone by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf<String?>(null) }

    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }

    var NewPassword by remember { mutableStateOf("") }
    var NewPasswordError by remember { mutableStateOf<String?>(null) }

    var ConfirmNewPassword by remember { mutableStateOf("") }
    var ConfirmNewPasswordError by remember { mutableStateOf<String?>(null) }

    var showDialog by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf<Boolean>(false) }
    var messageDialogue by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        firstname = AppValues.firstname.toString()
        lastname = AppValues.lastname.toString()
        phone = AppValues.phone.toString()
    }

    val context = LocalContext.current
    val api = api_service(context)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(AppColors().SentiBlack)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Mon compte",
            color = AppColors().SentiBlue,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(Modifier.height(16.dp))

        Input("Prénom", value = firstname, onValueChange = { firstname = it }, false, firstnameError)
        Spacer(modifier = Modifier.height(8.dp))
        Input("Nom", value = lastname, onValueChange = { lastname = it }, false, lastnameError)
        Spacer(modifier = Modifier.height(8.dp))
        Input("Numéro de téléphone", value = phone, onValueChange = { phone = it }, false, phoneError)
        Spacer(modifier = Modifier.height(8.dp))

        Bouton("Enregistrer", OnClick = {
            // Reset des erreurs
            firstnameError = null
            lastnameError = null
            phoneError = null
            var valide = true;

            if(firstname.length < 1) {
                firstnameError = "Prénom invalide"
                valide = false
            }

            if(lastname.length < 1) {
                lastnameError = "Nom de famille invalide"
                valide = false
            }

            val phoneRegex = Regex("^0[1-9][0-9]{8}\$")
            if (!phone.matches(phoneRegex)) {
                phoneError = "Numéro de téléphone invalide"
                valide = false
            }

            if(valide){
                api.SaveInfoAcount(context,firstname, lastname, phone){ success ->
                    if (success) {
                        AppValues.firstname = firstname
                        AppValues.lastname = lastname
                        AppValues.phone = phone
                        showDialog = true // Affiche le dialogue
                        isSuccess = true
                        messageDialogue ="Infos mises à jour avec succès"

                        Log.d("UI", "Infos mises à jour avec succès")
                    } else {
                        isSuccess = false
                        showDialog = true // Affiche le dialogue
                        messageDialogue ="Erreur lors de la mise à jour des infos"
                        Log.d("UI", "Erreur lors de la mise à jour des infos")
                    }

                }
            }
        })


        Spacer(Modifier.height(16.dp))

        Text(
            "Sécurité",
            color = AppColors().SentiBlue,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(Modifier.height(16.dp))

        Input("Mot de passe actuel", value = password, onValueChange = { password = it }, true, passwordError)
        Spacer(modifier = Modifier.height(8.dp))
        Input("Nouveau mot de passe", value = NewPassword, onValueChange = { NewPassword = it }, true, NewPasswordError)
        Spacer(modifier = Modifier.height(8.dp))
        Input("Confirmation du mot de passe", value = ConfirmNewPassword, onValueChange = { ConfirmNewPassword = it }, true, ConfirmNewPasswordError)
        Spacer(modifier = Modifier.height(8.dp))

        Bouton("Enregistrer", OnClick = {
            // Reset des erreurs
            passwordError = null
            NewPasswordError = null
            ConfirmNewPasswordError = null
            var valide = true;

            if(password.length < 1) {
                passwordError = "Merci de saisir votre mot de passe actuel"
                valide = false
            }

            if(NewPassword.length >= 1) {
                if (NewPassword.length < 6) {
                    NewPasswordError = "Le mot de passe doit contenir au moins 6 caractères"
                    valide = false
                }
            }
            else {
                NewPasswordError = "Merci de saisir votre mot de passe actuel"
                valide = false
            }

            if (ConfirmNewPassword != NewPassword) {
                ConfirmNewPasswordError = "Les mots de passe ne correspondent pas"
                valide = false
            }



            if(valide){
                api.saveNewPassword(context,password, NewPassword){ success ->
                    if (success) {
                        showDialog = true // Affiche le dialogue
                        isSuccess = true
                        messageDialogue = "Votre mot de passe a été mis à jour avec succès."
                        Log.d("UI", "Mot de passe mis à jour avec succès")
                    } else {
                        showDialog = true // Affiche le dialogue
                        isSuccess = false
                        messageDialogue = "Erreur dans la mise à jour du mot de passe"
                        Log.d("UI", "Erreur lors de la mise à jour du mot de passe")
                    }

                }
            }
        })


        Spacer(Modifier.height(16.dp))

        Text(
            "Autres paramêtre",
            color = AppColors().SentiBlue,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text("Mode contraster", color = AppColors().SentiBlue, modifier = Modifier.weight(1f))
            var isContraster by remember {
                mutableStateOf(
                    context.getSharedPreferences("settings", Activity.MODE_PRIVATE)
                        .getBoolean("isContraster", false)
                )
            }
            Switch(
                checked = isContraster,
                onCheckedChange = { checked ->
                    isContraster = checked
                    val sharedPreferences = context.getSharedPreferences("settings", Activity.MODE_PRIVATE)
                    sharedPreferences.edit().putBoolean("isContraster", checked).apply()

                    // Redémarre l'application
                    val activity = context as? Activity
                    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                    intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    activity?.finish()
                    Runtime.getRuntime().exit(0)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AppColors().SentiBlue
                )
            )
        }


        Bouton("Logout", OnClick = {
            api.logout(context, context as Activity) // depuis une Activity
        })

        // Et dans le corps de SettingsScreen (en bas du Column par exemple) :
        if (showDialog) {
            PopupAlert(messageDialogue, isSuccess) {
                showDialog = false
            }
        }
    }
}

@Preview
@Composable
fun SettingsScreenPreview() {
    SettingsScreen()
}
