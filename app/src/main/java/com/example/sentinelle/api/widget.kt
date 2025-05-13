package com.example.sentinelle.api

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.unit.dp
import com.example.sentinelle.R

class AppColors{
    var SentiBlack = Color(0xff16252B)
    var SentiGreen = Color(0xff399d61)
    var SentiDarkBlue = Color(0x33289DD2)
    var SentiBlue = Color(0xff0097B2)
    var SentiCyan = Color(0xff289DD2)
}

@Composable
fun Titre(label : String){
    Text(
        text= label,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Input(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false,
    errorMessage: String? = null
) {
    var error by remember { mutableStateOf<String?>(null) }
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = {
            onValueChange(it)
        },
        placeholder = { Text(label, color = Color.Black) },
        isError = errorMessage != null,
        singleLine = true,
        shape = RoundedCornerShape(50.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = AppColors().SentiCyan,
            unfocusedBorderColor = AppColors().SentiGreen,
            cursorColor = AppColors().SentiBlack,
            focusedTextColor = AppColors().SentiBlack,
            unfocusedTextColor = AppColors().SentiBlack,
            containerColor = AppColors().SentiGreen,
            errorContainerColor = AppColors().SentiGreen
        ),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
    )
    if (errorMessage != null) {
        Text(
            text = errorMessage,
            color = Color.Red,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
        )
    }
}

@Composable
fun Bouton(test: String, OnClick: () -> Unit){
    Button(
        onClick = OnClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors().SentiBlue,       // Couleur de fond du bouton
            contentColor = AppColors().SentiBlack         // Couleur du texte
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(test)
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