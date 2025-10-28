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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.example.sentinelle.R
import com.example.sentinelle.api.AppValues.Montserrat
import java.text.SimpleDateFormat
import java.util.Locale

private val dateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH)
private val hourFormatter = SimpleDateFormat("HH:mm", Locale.FRENCH)

@Composable
fun UpdateStatusBarColor(color: Color, context: Context) {
    SideEffect {
        (context as? Activity)?.window?.statusBarColor = color.toArgb()
        (context as? Activity)?.window?.navigationBarColor = color.toArgb()
    }
}

@Composable
fun Titre(label : String, colors: List<Color>){
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
                color= colors[3],
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = strokeWidth
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputTextArea(
    label: String,
    value: String,
    colors: List<Color>,
    onValueChange: (String) -> Unit,
    errorMessage: String? = null,
    maxLines: Int = 10
) {
    val containerColor = colors[1]
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = { onValueChange(it) },
        placeholder = { Text(label, color = colors[0]) },
        isError = errorMessage != null,
        textStyle = TextStyle(fontSize = 16.sp, color = colors[0]),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors[0],
            unfocusedTextColor = colors[0],
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor,
            disabledContainerColor = containerColor,
            errorContainerColor = colors[1],
            cursorColor = colors[0],
            focusedBorderColor = colors[4],
            unfocusedBorderColor = colors[1],
        ),
        maxLines = maxLines,
        minLines = 7,
    )
    if (errorMessage != null) {
        Text(
            text = errorMessage,
            color = colors[5],
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
        )
    }
}

@Composable
fun Input(
    label: String,
    value: String,
    colors: List<Color>,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false,
    errorMessage: String? = null
) {
    var error by remember { mutableStateOf<String?>(null) }
    val containerColor = colors[1]
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = {
            onValueChange(it)
        },
        placeholder = { Text(label, color = colors[0]) },
        isError = false,
        singleLine = false,
        textStyle = TextStyle(fontSize = 16.sp, color = colors[0]),
        shape = RoundedCornerShape(50.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors[0],
            unfocusedTextColor = colors[0],
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor,
            disabledContainerColor = containerColor,
            errorContainerColor = colors[1],
            cursorColor = colors[0],
            focusedBorderColor = colors[0],
            unfocusedBorderColor = colors[0],
        ),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
    )
    if (errorMessage != null) {
        Text(
            text = errorMessage,
            color = colors[5],
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
        )
    }
}

@Composable
fun InputColorPicker(
    label: String,
    value: String,
    colors: List<Color>,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false,
    errorMessage: String? = null,
    isColorPicker: Boolean = false, // ✅ Activer le sélecteur de couleur
    selectedColor: Color? = null, // ✅ Couleur actuellement sélectionnée
    onColorSelected: ((Colors) -> Unit)? = null // ✅ Callback
) {
    var showColorDialog by remember { mutableStateOf(false) }
    val containerColor = colors[1]

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                modifier = Modifier.weight(1f),
                value = value,
                onValueChange = {
                    onValueChange(it)
                },
                placeholder = { Text(label, color = colors[0]) },
                isError = false,
                singleLine = false,
                textStyle = TextStyle(fontSize = 16.sp, color = colors[0]),
                shape = RoundedCornerShape(40.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors[0],
                    unfocusedTextColor = colors[0],
                    focusedContainerColor = containerColor,
                    unfocusedContainerColor = containerColor,
                    disabledContainerColor = containerColor,
                    errorContainerColor = colors[1],
                    cursorColor = colors[0],
                    focusedBorderColor = colors[0],
                    unfocusedBorderColor = colors[0],
                ),
                visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            )

            // ✅ Bouton pour ouvrir le dialogue de couleurs
            if (isColorPicker) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(selectedColor ?: Color.Gray)
                        .border(2.dp, colors[0], CircleShape)
                        .clickable {
                            showColorDialog = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedColor == null) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_edit),
                            contentDescription = "Choisir une couleur",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = colors[5],
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }

    // ✅ Dialogue de sélection de couleur
    if (showColorDialog) {
        AlertDialog(
            onDismissRequest = { showColorDialog = false },
            title = {
                Text(
                    "Choisir une couleur",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    color = colors[3]
                )
            },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    items(AppValues.colorsTag) { color ->
                        Log.d("ColorPicker", "Available color: ${color.hexa}")
                        val isSelected = selectedColor?.toArgb() == Color(color.hexa.toLong(16)).toArgb()

                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor("#${color.hexa}")))
                                .border(
                                    width = if (isSelected) 4.dp else 0.dp,
                                    color = colors[0],
                                    shape = CircleShape
                                )
                                .clickable {
                                    onColorSelected?.invoke(color)
                                    showColorDialog = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.checkbox_on_background),
                                    contentDescription = "Sélectionné",
                                    tint = if (Color(color.hexa.toLong(16)).toArgb() > 0.5f) Color.Black else Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showColorDialog = false }
                ) {
                    Text(
                        "Fermer",
                        color = colors[1],
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            containerColor = colors[0],
            shape = RoundedCornerShape(16.dp)
        )
    }
}


@Composable
fun Modifier.simpleVerticalScrollbar(
    state: LazyListState,
    width: Dp = 6.dp
): Modifier {
    val targetAlpha = if (state.isScrollInProgress) 1f else 0f
    val duration = if (state.isScrollInProgress) 150 else 500

    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = duration),
        label = "scrollbarAlpha"
    )

    return this.then(
        Modifier.drawWithContent {
            drawContent()

            val firstVisibleIndex = state.layoutInfo.visibleItemsInfo.firstOrNull()?.index
            val needDrawScrollbar = state.isScrollInProgress || alpha > 0f

            if (needDrawScrollbar && firstVisibleIndex != null && state.layoutInfo.totalItemsCount > 0) {
                val elementHeight = size.height / state.layoutInfo.totalItemsCount
                val scrollbarOffsetY = firstVisibleIndex * elementHeight
                val scrollbarHeight = state.layoutInfo.visibleItemsInfo.size * elementHeight

                drawRect(
                    color = Color.White.copy(alpha = 0.6f), // couleur du scroll
                    topLeft = Offset(size.width - width.toPx(), scrollbarOffsetY),
                    size = Size(width.toPx(), scrollbarHeight),
                    alpha = alpha
                )
            }
        }
    )
}


@Composable
fun Bouton(text: String,colors: List<Color>, modifier: Modifier = Modifier, OnClick: () -> Unit){
    Button(
        onClick = OnClick,
        // Centrer le bouton
        modifier = modifier
            .padding(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors[3],       // Couleur de fond du bouton
            contentColor = colors[0]      // Couleur du texte
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
fun RedBouton(text: String, colors: List<Color>, OnClick: () -> Unit){
    Button(
        onClick = OnClick,
        modifier = Modifier
            .wrapContentWidth(Alignment.CenterHorizontally),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors[5],       // Couleur de fond du bouton
            contentColor = colors[0]        // Couleur du texte
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
fun BoutonStartStop(text: String, colors: List<Color>, OnClick: () -> Unit){
    Button(
        onClick = OnClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = colors[1],       // Couleur de fond du bouton
            contentColor = colors[0]         // Couleur du texte
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
            modifier = Modifier.size(180.dp)
        )
    }

}


fun Int.dpToPx(context: Context): Int =
    (this * context.resources.displayMetrics.density).toInt()



@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun CustomNumberPicker(
    selectedValue: MutableState<Int>,
    list: List<Int>,
    colors: List<Color>,
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
                        child.setTextColor(colors[1].toArgb())
//                            child.textSize = 20f // Facultatif : taille
                    }
                }

                // Change aussi les lignes du NumberPicker (les dividers)
                val fields = NumberPicker::class.java.declaredFields
                for (field in fields) {
                    if (field.name == "mSelectionDivider") {
                        field.isAccessible = true
                        field.set(numberPicker, ColorDrawable(colors[1].toArgb()))
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
                paint.color = colors[1] // ou une autre couleur
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
            numberPicker.value = selectedValue.value

            numberPicker.textSize = 100f

            numberPicker.setOnValueChangedListener { numberPicker, old, new ->
                onValueChange(numberPicker.value)
            }

            numberPicker.dividerPadding = 16

            numberPicker
        },

        update = { view ->
            view.minValue = list.first()
            view.maxValue = list.last()
            view.value = selectedValue.value
        }

    )

}


@Composable
fun PopupAlert(
    message: String,
    isSuccess: Boolean,
    colors: List<Color>,
    onDismiss: () -> Unit
) {

    Dialog(onDismissRequest = onDismiss) {
        var color = Color.Transparent
        if(isSuccess)
        {
            color = colors[1]
        }
        else{
            color = colors[5]
        }
        Box(
            modifier = Modifier
                .padding(16.dp)
                .background(
                    color = colors[0],
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = 2.dp,
                    color = color,
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
                    color = colors[1],
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
                    colors = ButtonDefaults.buttonColors(containerColor = colors[1], contentColor = colors[0]),
                ) {
                    Text("Compris")
                }
            }
        }
    }
}

@Composable
fun PopupAlertRequest(
    message: String,
    isSuccess: Boolean,
    colors: List<Color>,
    onDismiss: () -> Unit,
    onAccept: () -> Unit,
) {

    Dialog(onDismissRequest = onDismiss) {
        var color = Color.Transparent
        if(isSuccess)
        {
            color = colors[1]
        }
        else{
            color = colors[5]
        }
        Box(
            modifier = Modifier
                .padding(16.dp)
                .background(
                    color = colors[0],
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = 2.dp,
                    color = color,
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
                    color = colors[1],
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



                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                )
                {
                    Button(
                        onClick = onAccept,
                        colors = ButtonDefaults.buttonColors(containerColor = colors[1], contentColor = colors[0]),
                    ) {
                        Text("Valider")
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = colors[5], colors[0]),
                    ) {
                        Text("Refuser")
                    }
                }
            }
        }
    }
}


@Composable
fun ContactItem(
    contact: Contact,
    colors: List<Color>,
    onDelete: () -> Unit,
    onSelect: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(2f)) {
            Text(contact.name, color = Color.White, fontWeight = FontWeight.Bold)
            Text(contact.phone, color = Color.Gray, fontSize = 14.sp)
        }

        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = colors[5])
        }

        Checkbox(
            checked = contact.selected,
            onCheckedChange = onSelect
        )
    }
}

@Composable
fun TagItem(
    tag: Tag,
    colors: List<Color>,
    onDelete: () -> Unit
) {
    val tag_color = Color(android.graphics.Color.parseColor("#${tag.hexa}"))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TagLabel(tag = tag)

        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = colors[5])
        }
    }
}

@Composable
fun TagLabel(
    tag: Tag
) {
    val tag_color = Color(android.graphics.Color.parseColor("#${tag.hexa}"))
    Box(
        modifier = Modifier
            .background(tag_color)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = tag.name,
            color = Color.White,
            fontStyle = FontStyle.Normal,
            fontSize = 16.sp
        )
    }
}


@Composable
fun SaferiderItem(
    saferider: Saferider,
    colors: List<Color>,
    onDelete: () -> Unit,
    onClick: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .clickable {
                onClick(saferider.id)
            }
            .padding(vertical = 8.dp)
        ,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically

    ) {
        // Icon
        if (saferider.status == 1)
            Icon(
                painterResource(id = R.drawable.validate),
                contentDescription = "Audio",
                tint = Color.Unspecified,
                modifier = Modifier.padding(end = 7.dp).size(35.dp),
            )
        else if(saferider.status == 0)
            Icon(
                painterResource(id = R.drawable.pending),
                contentDescription = "Audio",
                tint = Color.Unspecified,
                modifier = Modifier.padding(end = 8.dp).size(30.dp),
            )
        else{
            Icon(
                painterResource(id = R.drawable.danger),
                contentDescription = "Audio",
                tint = Color.Unspecified,
                modifier = Modifier.padding(end = 8.dp).size(30.dp),
            )
        }

        Column(modifier = Modifier.weight(2f)) {
            Text(saferider.formattedDate, color = Color.White, fontWeight = FontWeight.Bold)
            Text(saferider.timeRange, color = Color.Gray, fontSize = 14.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(saferider.duration, color = Color.White, fontSize = 15.sp)
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = colors[5])
            }
        }
    }
}

@Composable
fun SaferiderItemWrapper(
    saferider: Saferider,
    colors: List<Color>,
    onDelete: () -> Unit,
    onClick: (Int) -> Unit
) {
    SaferiderItem(
        saferider = saferider,
        colors = colors,
        onDelete = onDelete,
        onClick = onClick,
    )
}

