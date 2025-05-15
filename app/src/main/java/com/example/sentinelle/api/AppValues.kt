package com.example.sentinelle.api

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.sentinelle.R

object AppValues {
// var base_url = "http://192.168.1.9:8000"
 var base_url = "http://10.0.2.2:8000"

 val Montserrat = FontFamily(
  Font(R.font.montserrat_bold_italic, FontWeight.Bold, FontStyle.Italic),
  Font(R.font.montserrat_semi_bold, FontWeight.SemiBold, FontStyle.Normal),
  Font(R.font.montserrat_bold, FontWeight.Bold, FontStyle.Normal),
  Font(R.font.montserrat_medium, FontWeight.Medium, FontStyle.Normal),
 )
}