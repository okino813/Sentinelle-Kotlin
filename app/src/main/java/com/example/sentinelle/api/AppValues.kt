package com.example.sentinelle.api

import android.icu.text.SimpleDateFormat
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.sentinelle.R
import java.util.Date
import java.util.Locale

object AppValues {
 const val base_url = "http://192.168.1.102:8000"
//const val base_url = "http://10.0.2.2:8000"

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
  Color(0xff289DD2),  // SentiCyan
  Color(0xFFA72525)  // SentiRed
 )


val contrastColors = listOf(
 Color.Black, // SentiBlackContrast
 Color.Yellow, // SentiGreenContrast
 Color.White, // SentiDarkBlueContrast
 Color.Cyan, // SentiBlueContrast
 Color.Cyan, // SentiCyanContrast
 Color.Red, // SentiRedContrast
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
 var saferiders = mutableStateListOf<Saferider>()

}

@Immutable
data class Contact(
 val id: Int,
 val name: String,
 val phone: String,
 var selected: Boolean
)

@Immutable
data class Saferider(
 val id: Int,
 val path: String,
 val start_date: String,
 val theorotical_end_date: String,
 val real_end_date: String,
 val locked: Boolean,
 val status: Int,
) {
 // Formatter statiques (créés une seule fois)
 companion object {
  val dateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH)
  val hourFormatter = SimpleDateFormat("HH:mm", Locale.FRENCH)

  fun getDate(s: String): String {
   return try {
    val netDate = Date(s.toLong() * 1000)
    val formatted = dateFormatter.format(netDate)
    val parts = formatted.split(" ")
    val day = parts[0]
    val month = parts[1].replaceFirstChar { it.uppercase() }
    val year = parts[2]
    "$day $month $year"
   } catch (e: Exception) {
    "?"
   }
  }

  fun getHourMinute(s: String?): String {
   return try {
    if (s.isNullOrEmpty()) "?"
    else hourFormatter.format(Date(s.toLong() * 1000)).replace(":", "h")
   } catch (e: Exception) {
    "?"
   }
  }

  fun getDuration(start: String, end: String): String {
   return try {
    if (end.isEmpty() || end == "0") {
     "En cours"
    } else {
     val startDate = Date(start.toLong() * 1000)
     val endDate = Date(end.toLong() * 1000)
     val duration = endDate.time - startDate.time
     val minutes = (duration / 1000 / 60) % 60
     val hours = (duration / 1000 / 60 / 60)
     if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }
   } catch (e: Exception) {
    "?"
   }
  }
 }

 // ✅ Valeurs dérivées précalculées
 val formattedDate: String = getDate(start_date)
 val timeRange: String = "${getHourMinute(start_date)} - ${getHourMinute(real_end_date)}"
 val duration: String = getDuration(start_date, real_end_date)
 val theoroticalEndTime: String = getHourMinute(theorotical_end_date)
}

@Immutable
data class AudioRecord(
 val id: Int,
 val date: String,
 val path: String
) {
 val formattedDate: String = try {
  val sdf = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.FRENCH)
  sdf.format(Date(date.toLong() * 1000))
 } catch (e: Exception) { "?" }
}