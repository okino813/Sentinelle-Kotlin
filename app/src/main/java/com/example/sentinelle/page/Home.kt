package com.example.sentinelle.page

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sentinelle.api.AppColors
import com.example.sentinelle.api.BoutonStartStop
import com.example.sentinelle.api.CustomNumberPicker

/**
 * A simple [androidx.fragment.app.Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun HomeScreen() {
    val heures = remember { mutableStateOf(0) }
    val minutes = remember { mutableStateOf(0) }
    val secondes = remember { mutableStateOf(0) }

    val values = (0..59).toList()
    val heuresValues = (0..23).toList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors().SentiBlack)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Minuteur",
            color = AppColors().SentiBlue,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CustomNumberPicker(
                selectedValue = heures.value,
                list = heuresValues,
                onValueChange = { heures.value = it }
            )

            Text(":", fontSize = 50.sp, color = AppColors().SentiCyan, fontWeight = FontWeight.Bold)

            CustomNumberPicker(
                selectedValue = minutes.value,
                list = values,
                onValueChange = { minutes.value = it }
            )

            Text(":", fontSize = 50.sp, color = AppColors().SentiCyan, fontWeight = FontWeight.Bold)

            CustomNumberPicker(
                selectedValue = secondes.value,
                list = values,
                onValueChange = { secondes.value = it }
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp)

        ) {
                Text("Heures", color = AppColors().SentiCyan)
                Text("Minutes", color = AppColors().SentiCyan)
                Text("Secondes", color = AppColors().SentiCyan)
        }

        Spacer(Modifier.height(24.dp))

        BoutonStartStop(
            "Départ",
             { /* Démarrer le minuteur */ },
        )

        Spacer(Modifier.height(32.dp))

        Text(
            "Vous êtes en danger ?",
            color = AppColors().SentiBlue,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(Modifier.height(26.dp))

        Button(
            onClick = { /* Lancer alerte */ },
            modifier = Modifier
                .size(150.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFA72525) // ou une autre couleur
            )
        ) {
            Text("ALERTER", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Appuyez sur ce bouton pour prévenir votre proche",
            fontStyle = FontStyle.Italic,
            color = Color.White,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Attention : Si vous cliquez sur le bouton “Alerter”, votre contact aura accès à votre localisation ainsi que l’accès à votre micro",
            color = Color.White,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun RoundedCornerShape(x0: Dp) {
    TODO("Not yet implemented")
}

@RequiresApi(Build.VERSION_CODES.Q)
@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}
