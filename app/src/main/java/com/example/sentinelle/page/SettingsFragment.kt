package com.example.sentinelle.page

import android.app.Activity
import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.example.sentinelle.api.AppColors
import com.example.sentinelle.api.Bouton
import com.example.sentinelle.api.Input
import com.example.sentinelle.api.api_service

/**
 * A simple [androidx.fragment.app.Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@Composable
fun SettingsScreen() {
    var firstname by remember { mutableStateOf("") }
    var firstnameError by remember { mutableStateOf<String?>(null) }

    var lastname by remember { mutableStateOf("") }
    var lastnameError by remember { mutableStateOf<String?>(null) }

    var phone by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf<String?>(null) }
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

            // Validation
            var isValid = true


//            if (isValid) {
//                val api = api_service(context)
//                api.register(email, motDePasse)
//                var intent = Intent(context, MainActivity_page::class.java)
//                context.startActivity(intent)
//            }
        })



        val context = LocalContext.current
        val api = api_service(context)
        Bouton("Logout", OnClick = {
            api.logout(context, context as Activity) // depuis une Activity
        })
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