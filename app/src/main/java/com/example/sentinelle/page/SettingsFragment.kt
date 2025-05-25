package com.example.sentinelle.page

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.example.sentinelle.api.AppColors
import com.example.sentinelle.api.AppValues
import com.example.sentinelle.api.Bouton
import com.example.sentinelle.api.Input
import com.example.sentinelle.api.api_service
import kotlinx.coroutines.launch

/**
 * A simple [androidx.fragment.app.Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
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
            var valide = false;

            if(firstname.length >= 1){
                if(lastname.length >= 1){
                    val phoneRegex = Regex("^0[1-9][0-9]{8}\$")
                    if (phone.matches(phoneRegex)) {
                        valide = true
                    } else {
                        phoneError = "Numéro de téléphone invalide"
                        valide = false
                    }
                }
                else {
                    lastnameError = "Nom de famille invalide"
                    valide = false
                }
            }else {
                lastnameError = "Prénom invalide"
                valide = false
            }

            if(valide){
                api.SaveInfoAcount(context,firstname, lastname, phone){ success ->
                    if (success) {
                        AppValues.firstname = firstname
                        AppValues.lastname = lastname
                        AppValues.phone = phone
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Infos mises à jour avec succès")
                        }
                        Log.d("UI", "Infos mises à jour avec succès")
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Erreur lors de la mise à jour des infos")
                        }
                        Log.d("UI", "Erreur lors de la mise à jour des infos")
                    }

                }
            }
            else{
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Les données saisies sont invalides")
                }
            }
        })







        Bouton("Logout", OnClick = {
            api.logout(context, context as Activity) // depuis une Activity
        })

        SnackbarHost(hostState = snackbarHostState) { data: SnackbarData ->
            val isSuccess = data.visuals.message.contains("succès", ignoreCase = true)
            Snackbar(
                snackbarData = data,
                containerColor = if (isSuccess) Color(0xFF4CAF50) else Color(0xFFF44336),
                contentColor = Color.White
            )
        }
    }
}

@Preview
@Composable
fun SettingsScreenPreview() {
    SettingsScreen()
}

class SettingsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SettingsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingsFragment().apply {
            }
    }
}