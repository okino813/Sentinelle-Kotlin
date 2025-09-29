package com.example.sentinelle.page

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sentinelle.api.AppValues
import com.example.sentinelle.api.Bouton
import com.example.sentinelle.api.Input
import com.example.sentinelle.api.PopupAlert
import com.example.sentinelle.api.RedBouton
import com.example.sentinelle.api.UpdateStatusBarColor
import com.example.sentinelle.api.api_service


@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    colors : List<Color>,
    context: Context,
    sharedPreferences: SharedPreferences,
    isLoggedIn: MutableState<Boolean>,
    isContrast: MutableState<Boolean>,
    onChangeColor: (Int) -> Unit

) {
    var firstname by remember { mutableStateOf(AppValues.firstname.toString()) }
    var firstnameError by remember { mutableStateOf<String?>(null) }

    var lastname by remember { mutableStateOf(AppValues.lastname.toString()) }
    var lastnameError by remember { mutableStateOf<String?>(null) }

    var phone by remember { mutableStateOf(AppValues.phone.toString()) }
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

    val api = api_service(context)

    fun changeInfoPerso(){
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
            api.SaveInfoAccount(context,firstname, lastname, phone){ success ->
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
    }

    fun changePassword(){
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
    }

    fun logout(){
        api.logout(
            context = context,
            onLogoutSuccess = {
                sharedPreferences.edit().putBoolean("is_authentificated", false).commit()
                sharedPreferences.edit().putBoolean("isContraster", false).commit()
                isLoggedIn.value = false

                // Redémarre l'application
                val activity = context as? Activity
                val intent =
                    context.packageManager.getLaunchIntentForPackage(context.packageName)
                intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                activity?.finish()
                Runtime.getRuntime().exit(0)
            },
            onLogoutFailure = { error ->
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    SettingsScreenStateless(
        modifier = modifier,
        colors = colors,
        isContrast = isContrast,
        firstname = firstname,
        lastname = lastname,
        phone = phone,
        password = password,
        NewPassword = NewPassword,
        ConfirmNewPassword = ConfirmNewPassword,
        firstnameError = firstnameError ?: "",
        lastnameError = lastnameError ?: "",
        phoneError = phoneError ?: "",
        passwordError = passwordError ?: "",
        NewPasswordError = NewPasswordError ?: "",
        ConfirmNewPasswordError = ConfirmNewPasswordError ?: "",
        onFirstnameChange = { firstname = it },
        onLastnameChange = { lastname = it },
        onPhoneChange = { phone = it },
        onPasswordChange = { password = it },
        onNewPasswordChange = { NewPassword = it },
        onNewConfirmPassword = { ConfirmNewPassword = it },
        onChangeColor = onChangeColor,
        valideInfoPerso = { changeInfoPerso() },
        validePassword = { changePassword() },
        logout = { logout() }
    )


    if (showDialog) {
        PopupAlert(messageDialogue, colors = colors, isSuccess = isSuccess) {
            showDialog = false
        }
    }

}

@Composable
fun SettingsScreenStateless(
    modifier: Modifier = Modifier,
    colors : List<Color>,
    isContrast: MutableState<Boolean>,
    firstname : String,
    lastname: String,
    phone: String,
    password: String,
    ConfirmNewPassword: String,
    NewPassword: String,
    firstnameError: String,
    lastnameError: String,
    phoneError: String,
    passwordError: String,
    NewPasswordError: String,
    ConfirmNewPasswordError: String,
    onFirstnameChange: (String) -> Unit,
    onLastnameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onNewConfirmPassword: (String) -> Unit,
    onChangeColor: (Int) -> Unit,
    valideInfoPerso: () -> Unit,
    validePassword: () -> Unit,
    logout: () -> Unit,
){

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(colors[0])
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        UpdateStatusBarColor(colors[0], LocalContext.current)
        Text(
            "Mon compte",
            color = colors[3],
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(Modifier.height(16.dp))

        Input("Prénom", value = firstname, colors = colors, onValueChange = onFirstnameChange, false, firstnameError)
        Input("Nom", value = lastname, colors = colors, onValueChange = onLastnameChange, false, lastnameError)
        Input("Numéro de téléphone", value = phone, colors = colors, onValueChange =  onPhoneChange, false, phoneError)

        Bouton("Enregistrer", colors = colors, OnClick = {
            valideInfoPerso()
        })


        Spacer(Modifier.height(16.dp))

        Text(
            "Sécurité",
            color = colors[3],
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(Modifier.height(16.dp))

        Input("Mot de passe actuel", value = password, colors = colors, onValueChange = onPasswordChange, true, passwordError)
        Input("Nouveau mot de passe", value = NewPassword, colors = colors, onValueChange = onNewPasswordChange, true, NewPasswordError)
        Input("Confirmation du mot de passe", value = ConfirmNewPassword, colors = colors, onValueChange = onNewConfirmPassword, true, ConfirmNewPasswordError)

        Bouton("Enregistrer", colors = colors, OnClick = {
            validePassword()
        })


        Spacer(Modifier.height(16.dp))

        Text(
            "Accessibilité",
            color = colors[3],
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(colors[1]),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween

        ) {
            Text(
                "Mode contraster",
                color = colors[0],
                modifier = Modifier.weight(1f).padding(start = 16.dp, top = 4.dp),
            )
            Switch(
                checked = isContrast.value,
                onCheckedChange = { checked ->
                    isContrast.value = checked
                    onChangeColor(0)
                },

                modifier = Modifier.padding(end = 8.dp),

                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors[4],
                    uncheckedThumbColor = colors[4],
                    checkedTrackColor = colors[0],
                    uncheckedTrackColor = colors[0],
                )
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "Autres paramêtre",
            color = colors[3],
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                "Supprimé tout mes trajets",
                color = colors[5],
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
            )

            RedBouton("Supprimer", colors = colors, OnClick = {
                print("test")
            })
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                "Supprimer mon compte",
                color = colors[5],
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
            )

            RedBouton("Supprimer", colors = colors, OnClick = {
                print("test")
            })

        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                "Se Déconnecter ?",
                color = colors[5],
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
            )

            RedBouton("Déconexion", colors = colors, OnClick = {
                logout()
            })

        }
    }
}
