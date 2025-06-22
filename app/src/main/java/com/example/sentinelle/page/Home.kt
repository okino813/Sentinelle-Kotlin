package com.example.sentinelle.page

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.example.sentinelle.api.PopupAlert
import com.example.sentinelle.api.UpdateStatusBarColor
import com.example.sentinelle.api.api_service



@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    val heures = remember { mutableStateOf(0) }
    val minutes = remember { mutableStateOf(0) }
    val secondes = remember { mutableStateOf(0) }

    val values = (0..59).toList()
    val heuresValues = (0..23).toList()
    var context = LocalContext.current;
    val api = api_service(context)

    var showErrorDialog by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(AppColors.SentiBlack)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            UpdateStatusBarColor(AppColors.SentiBlack, LocalContext.current)
            Text(
                "Minuteur",
                color = AppColors.SentiBlue,
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

                Text(
                    ":",
                    fontSize = 50.sp,
                    color = AppColors.SentiCyan,
                    fontWeight = FontWeight.Bold
                )

                CustomNumberPicker(
                    selectedValue = minutes.value,
                    list = values,
                    onValueChange = { minutes.value = it }
                )

                Text(
                    ":",
                    fontSize = 50.sp,
                    color = AppColors.SentiCyan,
                    fontWeight = FontWeight.Bold
                )

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
                Text("Heures", color = AppColors.SentiCyan)
                Text("Minutes", color = AppColors.SentiCyan)
                Text("Secondes", color = AppColors.SentiCyan)
            }

            Spacer(Modifier.height(24.dp))

            BoutonStartStop(
                "Départ",
                {
                    /* Démarrer le minuteur */

                    // On fais les tests de lancement
                    api.startTimer(
                        context,
                        heures.value,
                        minutes.value,
                        secondes.value,
                    ) { success, error ->
                        if (success) {
                            Log.d("TESTCheck", "Timer start successfully")
                        } else {
                            Log.d("TESTCheck", "Timer start failed")
                            errorMessage = error ?: "Erreur inconnue"
                            isSuccess = false
                            showErrorDialog = true
                        }
                    }
                },
            )

            if (showErrorDialog) {
                PopupAlert(errorMessage, isSuccess) {
                    showErrorDialog = false
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(
                "Vous êtes en danger ?",
                color = AppColors.SentiBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(Modifier.height(26.dp))

            Button(
                onClick = { /* Lancer alerte */ },
                modifier = Modifier
                    .size(120.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFA72525) // ou une autre couleur
                )
            ) {
                Text("ALERTER", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Appuyez sur ce bouton pour prévenir votre proche",
                fontStyle = FontStyle.Italic,
                color = Color.White,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Attention : Si vous cliquez sur le bouton “Alerter”, votre contact aura accès à votre localisation ainsi que l’accès à votre micro",
                color = Color.White,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                //            modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
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
