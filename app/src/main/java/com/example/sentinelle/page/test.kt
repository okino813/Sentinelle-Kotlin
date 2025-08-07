//package com.example.sentinelle.page
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.material3.Button
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.dp
//import com.example.sentinelle.api.AppValues
//
//@Composable
//fun MyAppParent() {
//    Column(modifier = Modifier.fillMaxSize()) {
//        ColorListStateful()
//    }
//}
//
//@Composable
//fun ColorListStateful() {
//    var colorList by remember {
//        mutableStateOf(
//            if(AppValues.isContrasted)
//                listOf(
//                    AppValues.SentiBlack,
//                    AppValues.SentiGreen,
//                    AppValues.SentiDarkBlue,
//                    AppValues.SentiBlue,
//                    AppValues.SentiCyan
//                )
//            else
//                listOf(
//                    AppValues.SentiBlackContrast,
//                    AppValues.SentiGreenContrast,
//                    AppValues.SentiDarkBlueContrast,
//                    AppValues.SentiBlueContrast,
//                    AppValues.SentiCyanContrast
//                )
//        )
//    }
//
//    // Fonction pour changer une couleur ou toute la liste
//    fun changeColor(index: Int) {
//        if(AppValues.isContrasted)
//            colorList = listOf(
//                AppValues.SentiBlack,
//                AppValues.SentiGreen,
//                AppValues.SentiDarkBlue,
//                AppValues.SentiBlue,
//                AppValues.SentiCyan
//            )
//        else
//            colorList = listOf(
//                AppValues.SentiBlackContrast,
//                AppValues.SentiGreenContrast,
//                AppValues.SentiDarkBlueContrast,
//                AppValues.SentiBlueContrast,
//                AppValues.SentiCyanContrast
//            )
//
//        AppValues.isContrasted = !AppValues.isContrasted
//    }
//
//    ColorListStateless(colors = colorList, onChangeColor = ::changeColor)
//}
//
//
//@Composable
//fun ColorListStateless(
//    colors: List<Color>,
//    onChangeColor: (Int) -> Unit
//) {
//    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//        Row {
//            colors.forEachIndexed { index, color ->
//                Box(
//                    modifier = Modifier
//                        .size(60.dp)
//                        .padding(8.dp)
//                        .background(color)
//                        .clickable { onChangeColor(index) }
//                )
//            }
//        }
//        Spacer(modifier = Modifier.height(16.dp))
//        Button(onClick = { onChangeColor(-1) }) {
//            Text("Changer toutes les couleurs")
//        }
//
//        textAdd(colors)
//    }
//}
//
//
//@Composable
//fun textAdd(colors : List<Color>){
//    Text(
//        text = "Nombre de couleurs : ${colors.size}",
//        modifier = Modifier.padding(16.dp),
//        color = colors[1]
//    )
//}