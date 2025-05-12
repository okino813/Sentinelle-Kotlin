package com.example.sentinelle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class AppColors{
    var SentiBlack = Color(0xff16252B)
    var SentiGreen = Color(0xff399d61)
    var SentiDarkBlue = Color(0x33289DD2)
    var SentiBlue = Color(0xff0097B2)
    var SentiCyan = Color(0xff289DD2)
}

class login_activity : ComponentActivity() {

    private var isSignupMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
        setContent {
            MaterialTheme {
                PreviewPages()
            }
        }


    }
}


// Les composable sont en dehors de la class
@Composable
fun Pages(){
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppColors().SentiBlack
    ){
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
           // Ici qu'on va mettre le contenu de la page
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)){
                Logo()
                Titre()
                FormulaireConnexion()

            }

        }
    }

}

@Composable
fun Input(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(label, color = Color.Black) },
        singleLine = true,
        shape = RoundedCornerShape(50.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppColors().SentiCyan,
            unfocusedBorderColor = AppColors().SentiGreen,
            cursorColor = AppColors().SentiBlack,
            focusedTextColor = AppColors().SentiBlack,
            unfocusedTextColor = AppColors().SentiBlack,
            unfocusedContainerColor = AppColors().SentiGreen,
            focusedContainerColor = AppColors().SentiGreen,
        ),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,

    )
}

@Composable
fun FormulaireConnexion() {
    var motDePasse by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    Spacer(modifier = Modifier.height(16.dp))

    Input(
        "Email",
        value = email,
        onValueChange = {email = it},
        false
    )

    Spacer(modifier = Modifier.height(8.dp))

    Input(
        "Mot de passe",
        value = motDePasse,
        onValueChange = {motDePasse = it},
        true
    )

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = {
            // Ici tu fais la vérification ou l'appel à Firebase/Django
            println("Email: $email - Mot de passe: $motDePasse")
        }
    ) {
        Text("Se connecter")
    }
}


@Composable
fun Logo(){
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        Image(
            painter = painterResource(R.drawable.main_icon_dark),
            contentDescription = "Logo Sentinelle",
        )
    }

}


@Composable
fun Titre(){
    Text(
        text= "Connexion",
        color = Color.White,
        style = MaterialTheme.typography.headlineMedium.copy(
            textDecoration = TextDecoration.None
        ),
        modifier = Modifier.drawBehind{
            val strokeWidth = 5.dp.toPx()
            val y = size.height - strokeWidth / 3
            drawLine(
                color= AppColors().SentiCyan,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = strokeWidth
            )
        }
    )
}


@Preview
@Composable
fun PreviewPages(){
    Pages()
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
