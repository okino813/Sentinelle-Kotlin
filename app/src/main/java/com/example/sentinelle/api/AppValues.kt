package com.example.sentinelle.api

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.sentinelle.R

object AppValues {
 var base_url = "http://192.168.1.102:8000"
// var base_url = "http://10.0.2.2:8000"

 val Montserrat = FontFamily(
  Font(R.font.montserrat_bold_italic, FontWeight.Bold, FontStyle.Italic),
  Font(R.font.montserrat_semi_bold, FontWeight.SemiBold, FontStyle.Normal),
  Font(R.font.montserrat_bold, FontWeight.Bold, FontStyle.Normal),
  Font(R.font.montserrat_medium, FontWeight.Medium, FontStyle.Normal),
 )

 var isContrasted = false;

 val defaultColors = listOf(
  Color(0xff16252B), // SentiBlack
  Color(0xff399d61), // SentiGreen
  Color(0x33289DD2), // SentiDarkBlue
  Color(0xff0097B2), // SentiBlue
  Color(0xff289DD2)  // SentiCyan
 )


val contrastColors = listOf(
 Color.Black, // SentiBlackContrast
 Color.Yellow, // SentiGreenContrast
 Color.White, // SentiDarkBlueContrast
 Color.Cyan, // SentiBlueContrast
 Color.Cyan // SentiCyanContrast
)


 var isTimerRunning = { mutableStateOf(false) }

 var hour = mutableStateOf(0)
 var minute = mutableStateOf(0)
 var seconde = mutableStateOf(0)

 var firstname: String? = ""
 var lastname: String? = ""
 var phone: String? = ""
 var email: String? = ""
 var message: String? = ""
 var contacts = mutableStateListOf<Contact>()

}

@Immutable
data class Contact(
 val id: Int,
 val name: String,
 val phone: String,
 var selected: Boolean
)