package com.example.sentinelle.api

import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.NumberPicker
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.example.sentinelle.R
import com.example.sentinelle.api.AppValues.Montserrat

object AppColors{
    var SentiBlack = Color(0xff16252B)
    var SentiGreen = Color(0xff399d61)
    var SentiDarkBlue = Color(0x33289DD2)
    var SentiBlue = Color(0xff0097B2)
    var SentiCyan = Color(0xff289DD2)
}

@Composable
fun UpdateStatusBarColor(color: Color, context: Context) {
    SideEffect {
        (context as? Activity)?.window?.statusBarColor = color.toArgb()
        (context as? Activity)?.window?.navigationBarColor = color.toArgb()
    }
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
                color= AppColors.SentiCyan,
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
        textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
        shape = RoundedCornerShape(50.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = AppColors.SentiCyan,
            unfocusedBorderColor = AppColors.SentiGreen,
            cursorColor = AppColors.SentiBlack,
            focusedTextColor = AppColors.SentiBlack,
            unfocusedTextColor = AppColors.SentiBlack,
            containerColor = AppColors.SentiGreen,
            errorContainerColor = AppColors.SentiGreen
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
fun Bouton(text: String, OnClick: () -> Unit){
    Button(
        onClick = OnClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.SentiBlue,       // Couleur de fond du bouton
            contentColor = AppColors.SentiBlack         // Couleur du texte
        ),
        shape = RoundedCornerShape(8.dp),

    ) {
        Text(
            text,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal,
        )
    }
}

@Composable
fun RedBouton(text: String, OnClick: () -> Unit){
    Button(
        onClick = OnClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFF54B4B),       // Couleur de fond du bouton
            contentColor = AppColors.SentiBlack         // Couleur du texte
        ),
        shape = RoundedCornerShape(8.dp),

        ) {
        Text(
            text,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal,
        )
    }
}

@Composable
fun BoutonStartStop(text: String, OnClick: () -> Unit){
    Button(
        onClick = OnClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.SentiGreen,       // Couleur de fond du bouton
            contentColor = AppColors.SentiBlack         // Couleur du texte
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text,
            textAlign = TextAlign.Center,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal,
            fontSize = 16.sp
        )
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
fun TimeSelector(
    initialHours: Int = 0,
    initialMinutes: Int = 0,
    initialSeconds: Int = 0,
    onTimeSelected: (Int, Int, Int) -> Unit
) {
    var hours by remember { mutableStateOf(initialHours) }
    var minutes by remember { mutableStateOf(initialMinutes) }
    var seconds by remember { mutableStateOf(initialSeconds) }

    var showPickerFor by remember { mutableStateOf<String?>(null) }

    val pickerRange = when (showPickerFor) {
        "hours" -> 0..24
        "minutes", "seconds" -> 0..59
        else -> 0..0
    }

    val selectedValue = when (showPickerFor) {
        "hours" -> hours
        "minutes" -> minutes
        "seconds" -> seconds
        else -> 0
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            TimeField("Heures", hours) { showPickerFor = "hours" }
            TimeField("Minutes", minutes) { showPickerFor = "minutes" }
            TimeField("Secondes", seconds) { showPickerFor = "seconds" }
        }

        Button(onClick = {
            onTimeSelected(hours, minutes, seconds)
        }) {
            Text("Valider")
        }
    }

    if (showPickerFor != null) {
        AlertDialog(
            onDismissRequest = { showPickerFor = null },
            title = { Text("Choisir ${showPickerFor}") },
            text = {
                AndroidView(
                    factory = { context ->
                        NumberPicker(context).apply {
                            minValue = pickerRange.first
                            maxValue = pickerRange.last
                            value = selectedValue
                            setOnValueChangedListener { _, _, newVal ->
                                when (showPickerFor) {
                                    "hours" -> hours = newVal
                                    "minutes" -> minutes = newVal
                                    "seconds" -> seconds = newVal
                                }
                            }
                        }
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = { showPickerFor = null }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun TimeField(label: String, value: Int, onClick: () -> Unit) {
    OutlinedTextField(
        value = value.toString().padStart(2, '0'),
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        modifier = Modifier
            .width(100.dp)
            .clickable { onClick() }
    )
}



fun Int.dpToPx(context: Context): Int =
    (this * context.resources.displayMetrics.density).toInt()



@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun CustomNumberPicker(
    selectedValue: Int,
    list: List<Int>,
    onValueChange: (Int) -> Unit
)
{
    AndroidView(
        modifier = Modifier.wrapContentSize().clipToBounds(),
            factory = { context ->
                val view = LayoutInflater.from(context).inflate(
                    R.layout.number_picker, null
                )

                val numberPicker = view.findViewById<NumberPicker>(R.id.numberPicker)

                try {
                    val count = numberPicker.childCount
                    for (i in 0 until count) {
                        val child = numberPicker.getChildAt(i)
                        if (child is EditText) {
                            child.setTextColor(AppColors.SentiGreen.toArgb())
//                            child.textSize = 20f // Facultatif : taille
                        }
                    }

                    // Change aussi les lignes du NumberPicker (les dividers)
                    val fields = NumberPicker::class.java.declaredFields
                    for (field in fields) {
                        if (field.name == "mSelectionDivider") {
                            field.isAccessible = true
                            field.set(numberPicker, ColorDrawable(AppColors.SentiGreen.toArgb()))
                            break
                        }
                    }
                    numberPicker.invalidate()

                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("CustomNumberPicker", "Erreur lors de la personnalisation du NumberPicker", e)
                }

                try {
                    val selectorWheelPaintField = NumberPicker::class.java.getField("mSelectorWheelPaint")
                    selectorWheelPaintField.isAccessible = true
                    val paint = selectorWheelPaintField.get(numberPicker) as Paint
                    paint.color = AppColors.SentiGreen // ou une autre couleur
                    numberPicker.invalidate()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                numberPicker.layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    150.dpToPx(context)
                )

                numberPicker.minValue = list.first()
                numberPicker.maxValue = list.last()
                numberPicker.value = selectedValue

//                    numberPicker.textSize = 48f

                numberPicker.setOnValueChangedListener { numberPicker, old, new ->
                    onValueChange(numberPicker.value)
                }

                numberPicker.dividerPadding = 16

                numberPicker
            },

            update = { view ->
                view.minValue = list.first()
                view.maxValue = list.last()
                view.value = selectedValue
            }

    )

}


@Composable
fun PopupAlert(
    message: String,
    isSuccess: Boolean,
    onDismiss: () -> Unit
) {

    Dialog(onDismissRequest = onDismiss) {
        var color = Color.Transparent
        if(isSuccess)
        {
            color = AppColors.SentiGreen
        }
        else{
            color = Color.Red
        }
        Box(
            modifier = Modifier
                .padding(16.dp)
                .background(
                    color = AppColors.SentiBlack, // Utilise ta couleur personnalisée
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = 2.dp,
                    color = color, // Utilise ta couleur personnalisée
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Attention !",
                    color = AppColors.SentiGreen,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    fontFamily = Montserrat,
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.SentiGreen, contentColor = AppColors.SentiBlack),
                ) {
                    Text("Compris")
                }
            }
        }
    }
}