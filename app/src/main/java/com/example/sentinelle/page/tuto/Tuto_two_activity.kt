package com.example.sentinelle.page.tuto

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sentinelle.R
import com.example.sentinelle.api.AppColors
import com.example.sentinelle.api.AppValues.Montserrat

@Composable
fun Tuto2(onNext : () -> Unit) {
    val context = LocalContext.current
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppColors().SentiBlue
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            // Ici qu'on va mettre le contenu de la page
            Column() {
                Image(
                    painter = painterResource(id = R.drawable.logo_carte_tuto2),
                    contentDescription = "Logo Carte",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.padding(10.dp))

                Text(
                    "Suivie de votre parcours",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.padding(10.dp))

                Text(
                    "Votre trajet est enregistrer pour vous et vos proche. En cas de problème, ils vous trouverons rapidement !",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.SemiBold,
                    fontStyle = FontStyle.Normal,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.padding(20.dp))

                Image(
                    painter = painterResource(id = R.drawable.fleche_tuto2),
                    contentDescription = "Fleche suivant",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                        .clickable{
                            onNext()
                        }
                )

            }
        }
    }
}

