package com.example.sentinelle.api

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.sentinelle.R

object AppValues {
 var base_url = "http://192.168.1.100:8000"
// var base_url = "http://10.0.2.2:8000"

 val Montserrat = FontFamily(
  Font(R.font.montserrat_bold_italic, FontWeight.Bold, FontStyle.Italic),
  Font(R.font.montserrat_semi_bold, FontWeight.SemiBold, FontStyle.Normal),
  Font(R.font.montserrat_bold, FontWeight.Bold, FontStyle.Normal),
  Font(R.font.montserrat_medium, FontWeight.Medium, FontStyle.Normal),
 )

 var firstname: String? = ""
 var lastname: String? = ""
 var phone: String? = ""
 var email: String? = null
 var message: String? = null
}
data class Contact(
 val id: Int,
 val name: String,
 val phone: String,
 val selected: Boolean
)