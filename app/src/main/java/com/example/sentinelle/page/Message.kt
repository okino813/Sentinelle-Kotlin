package com.example.sentinelle.page

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sentinelle.api.AppColors
import com.example.sentinelle.api.AppValues
import com.example.sentinelle.api.Bouton
import com.example.sentinelle.api.InputTextArea
import com.example.sentinelle.api.PopupAlert
import com.example.sentinelle.api.UpdateStatusBarColor
import com.example.sentinelle.api.api_service

/**
 * A simple [androidx.fragment.app.Fragment] subclass.
 * Use the [MessageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

@Composable
fun MessageScreen(modifier: Modifier = Modifier) {

    var showDialog by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf<Boolean>(false) }
    var messageDialogue by remember { mutableStateOf("") }

    var message by remember { mutableStateOf(AppValues.message.toString()) }
    var messageError by remember { mutableStateOf<String?>(null) }


    var context = LocalContext.current
    val api = api_service(context)
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .background(AppColors.SentiBlack)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UpdateStatusBarColor(AppColors.SentiBlack, LocalContext.current)
            Text(
                "Message personalisé",
                color = AppColors.SentiBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(Modifier.height(16.dp))
            Text(
                "Le message que vous entrez ci-dessous, sera envoyer à votre contact si vous ne parvenez pas à désactiver le compte à rebours.",
                color = Color.White,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(Modifier.height(16.dp))


            InputTextArea(
                "Entre votre messages personnalisé",
                value = message,
                onValueChange = { message = it },
                messageError
            )

            Spacer(modifier = Modifier.height(8.dp))

            Bouton("Enregistrer", OnClick = {
                // Reset des erreurs
                messageError = null
                var valide = true;

                if (message.length < 50) {
                    messageError = "Le message doit faire plus de 50 caractères"
                    valide = false
                }


                if (valide) {
                    api.SaveMessage(context, message) { success ->
                        if (success) {
                            AppValues.message = message
                            showDialog = true // Affiche le dialogue
                            isSuccess = true
                            messageDialogue = "Infos mises à jour avec succès"

                            Log.d("UI", "Infos mises à jour avec succès")
                        } else {
                            isSuccess = false
                            showDialog = true // Affiche le dialogue
                            messageDialogue = "Erreur lors de la mise à jour des infos"
                            Log.d("UI", "Erreur lors de la mise à jour des infos")
                        }

                    }
                }
            })

            // Et dans le corps de SettingsScreen (en bas du Column par exemple) :
            if (showDialog) {
                PopupAlert(messageDialogue, isSuccess) {
                    showDialog = false
                }
            }


        }
    }
}


@Composable
fun AlbumScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Album Screen")
    }
}

@Composable
fun PlaylistScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Playlist Screen")
    }
}

enum class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    MESSAGE("message", "Message", Icons.Default.ThumbUp, "Message"),
    ALBUM("album", "Album", Icons.Default.ThumbUp, "Album"),
    PLAYLISTS("playlist", "Playlist", Icons.Default.Home, "Playlist")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationTabExample(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val startDestination = Destination.MESSAGE
    var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

    Scaffold(
        modifier = modifier,
        containerColor = AppColors.SentiBlack
    ) { contentPadding ->
        PrimaryTabRow(
            selectedTabIndex = selectedDestination,
            modifier = Modifier.padding(contentPadding),
            containerColor = AppColors.SentiBlack,
            contentColor = AppColors.SentiBlue,
        ) {
            Destination.entries.forEachIndexed { index, destination ->
                Tab(
                    selected = selectedDestination == index,
                    onClick = {
                        navController.navigate(route = destination.route)
                        selectedDestination = index
                    },
                    text = {
                        Text(
                            text = destination.label,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }
        AppNavHost(navController, startDestination)
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: Destination,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController,
        startDestination = startDestination.route,
    ) {
        Destination.entries.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    Destination.MESSAGE -> MessageScreen()
                    Destination.ALBUM -> AlbumScreen()
                    Destination.PLAYLISTS -> PlaylistScreen()
                }
            }
        }
    }
}