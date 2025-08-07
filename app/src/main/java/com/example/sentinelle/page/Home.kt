package com.example.sentinelle.page

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sentinelle.api.AppValues
import com.example.sentinelle.api.UpdateStatusBarColor
import com.example.sentinelle.api.api_service


@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun HomeScreen(
    colors : List<Color>,
    modifier: Modifier = Modifier,
    onChangeColor: (Int) -> Unit
) {
    val heures = AppValues.hour
    val minutes = AppValues.minute
    val secondes = AppValues.seconde


    val values = (0..59).toList()
    val heuresValues = (0..23).toList()
    var context = LocalContext.current;
    val api = api_service(context)

    var showErrorDialog by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }

    var isTimerRunning by remember { mutableStateOf(false) }

    val prefs = context.getSharedPreferences("sentinelle_prefs", MODE_PRIVATE)
    val isRunning = prefs.getBoolean("is_timer_running", false)

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("sentinelle_prefs", Context.MODE_PRIVATE)
        val isRunning = prefs.getBoolean("is_timer_running", false)

        if (isRunning) {
            val endTimestamp = prefs.getLong("end_timestamp", 0L)
            val remainingMillis = endTimestamp - System.currentTimeMillis()
            if (remainingMillis > 0) {
                isTimerRunning = true
            } else {
                prefs.edit().putBoolean("is_timer_running", false).remove("end_timestamp").apply()
                isTimerRunning = false
            }
        }
    }

    HomeScreenStateless(
        colors = colors,
        modifier = modifier,
        onChangeColor = onChangeColor
    )

//    Box(
//        modifier = Modifier.fillMaxSize(),
//        contentAlignment = Alignment.Center,
//    ) {
//        Column(
//            modifier = modifier
//                .fillMaxSize()
//                .background(AppColors.SentiBlack)
//                .padding(16.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            UpdateStatusBarColor(AppColors.SentiBlack, LocalContext.current)
//            Text(
//                "Minuteur",
//                color = AppColors.SentiBlue,
//                fontWeight = FontWeight.Bold,
//                fontSize = 20.sp,
//                modifier = Modifier.align(Alignment.Start)
//            )
//            Spacer(Modifier.height(16.dp))
//
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                CustomNumberPicker(
//                    selectedValue = heures,
//                    list = heuresValues,
//                    onValueChange = { heures.value = it }
//                )
//
//                Text(
//                    ":",
//                    fontSize = 50.sp,
//                    color = AppColors.SentiCyan,
//                    fontWeight = FontWeight.Bold
//                )
//
//                CustomNumberPicker(
//                    selectedValue = minutes,
//                    list = values,
//                    onValueChange = { minutes.value = it }
//                )
//
//                Text(
//                    ":",
//                    fontSize = 50.sp,
//                    color = AppColors.SentiCyan,
//                    fontWeight = FontWeight.Bold
//                )
//
//                CustomNumberPicker(
//                    selectedValue = secondes,
//                    list = values,
//                    onValueChange = { secondes.value = it }
//                )
//            }
//
//            Spacer(Modifier.height(8.dp))
//
//            Row(
//                horizontalArrangement = Arrangement.SpaceBetween,
//                modifier = Modifier.fillMaxWidth()
//                    .padding(start = 10.dp, end = 10.dp)
//
//            ) {
//                Text("Heures", color = AppColors.SentiCyan)
//                Text("Minutes", color = AppColors.SentiCyan)
//                Text("Secondes", color = AppColors.SentiCyan)
//            }
//
//            Spacer(Modifier.height(24.dp))
//
//            BoutonStartStop(
//                if (isTimerRunning) "Stop" else "Départ",
//                {
//                    if (!isTimerRunning) {
//                        // Vérification de lancement via API
//                        api.startTimer(
//                            context,
//                            heures.value,
//                            minutes.value,
//                            secondes.value,
//                        ) { success, error ->
//                            if (success) {
//                                Log.d("TESTCheck", "Timer started")
//                                val totalseconds = heures.value * 3600 + minutes.value * 60 + secondes.value
//                                val intent = Intent(context, TimerService::class.java).apply {
//                                    action = "START_TIMER"
//                                    putExtra("totalSeconds", totalseconds)
//                                }
//                                ContextCompat.startForegroundService(context, intent)
//                                isTimerRunning = true
//                            } else {
//                                errorMessage = error ?: "Erreur inconnue"
//                                isSuccess = false
//                                showErrorDialog = true
//                            }
//                        }
//                    } else {
//                        // Arrêter le minuteur
//                        val intent = Intent(context, TimerService::class.java).apply {
//                            action = "STOP_TIMER"
//                        }
//
//                        context.startService(intent)
//                        isTimerRunning = false
//
//                        Log.d("TimerService", "Timer stopped, reset values")
//
//                        api.stopTimer(context) { success, error ->
//                            if (success) {
//                                // Remise à zéro des valeurs du minuteur
//                                heures.value = 0
//                                minutes.value = 0
//                                secondes.value = 0
//
//                            } else {
//                                errorMessage = error ?: "Erreur inconnue"
//                                isSuccess = false
//                                showErrorDialog = true
//                            }
//                        }
//                    }
//                }
//            )
//
//            if (showErrorDialog) {
//                PopupAlert(errorMessage, isSuccess) {
//                    showErrorDialog = false
//                }
//            }
//
//            Spacer(Modifier.height(32.dp))
//
//            Text(
//                "Vous êtes en danger ?",
//                color = AppColors.SentiBlue,
//                fontWeight = FontWeight.Bold,
//                fontSize = 20.sp,
//                modifier = Modifier.align(Alignment.Start)
//            )
//
//            Spacer(Modifier.height(26.dp))
//
//            Button(
//                onClick = { /* Lancer alerte */ },
//                modifier = Modifier
//                    .size(120.dp),
//                shape = CircleShape,
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(0xFFA72525) // ou une autre couleur
//                )
//            ) {
//                Text("ALERTER", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
//            }
//
//            Spacer(Modifier.height(24.dp))
//
//            Text(
//                "Appuyez sur ce bouton pour prévenir votre proche",
//                fontStyle = FontStyle.Italic,
//                color = Color.White,
//                fontSize = 14.sp,
//                textAlign = TextAlign.Center
//            )
//
//            Spacer(Modifier.height(8.dp))
//
//            Text(
//                "Attention : Si vous cliquez sur le bouton “Alerter”, votre contact aura accès à votre localisation ainsi que l’accès à votre micro",
//                color = Color.White,
//                fontSize = 12.sp,
//                textAlign = TextAlign.Center,
//                //            modifier = Modifier.padding(horizontal = 16.dp)
//            )
//        }
//    }
}

@Composable
fun RoundedCornerShape(x0: Dp) {
    TODO("Not yet implemented")
}


@Composable
fun HomeScreenStateless(
    colors : List<Color>,
    modifier: Modifier = Modifier,
    onChangeColor: (Int) -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ){
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(colors[0])
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        )
        {
            UpdateStatusBarColor(colors[0], LocalContext.current)
            Text(
                "Minuteur",
                color = colors[3],
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(Modifier.height(16.dp))
        }

        BtnChangeColor(onChangeColor)
    }
}

@Composable
fun BtnChangeColor(
    onChangeColor: (Int) -> Unit
){
    Button(onClick = { onChangeColor(0) }) {
        Text("Changer toutes les couleurs")
    }
}



