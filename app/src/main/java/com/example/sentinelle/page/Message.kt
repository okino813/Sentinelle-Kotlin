package com.example.sentinelle.page

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.example.sentinelle.api.Input
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

            Text(
                "A la suite de votre message personnalisé, un liens avec un code permettra à votre contact de consulter votre trajet ainsi que l’environnement sonore.",
                color = Color.White,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Start)
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
fun ContactScreen(modifier: Modifier = Modifier) {
    var showDialog by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    var messageDialogue by remember { mutableStateOf("") }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val api = api_service(context)
    val contacts = AppValues.contacts

    UpdateStatusBarColor(AppColors.SentiBlack, LocalContext.current)

    // Utilisez LazyColumn pour tout le contenu
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.SentiBlack)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                "Ajouter un contact",
                color = AppColors.SentiBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(Modifier.height(16.dp))
            Text(
                "Ajouter votre contact ci-dessous.",
                color = Color.White,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Text(
                "Exemple : 0712131415",
                color = Color.White,
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Italic,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(Modifier.height(16.dp))
        }

        item {
            Input("Nom et Prénom", value = name, onValueChange = { name = it }, false, nameError)
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Input("Numéro de téléphone", value = phone, onValueChange = { phone = it }, false, phoneError)
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Bouton("Enregistrer", OnClick = {
                nameError = null
                phoneError = null
                var valide = true

                if (name.length < 1) {
                    nameError = "Le nom doit être renseigné"
                    valide = false
                }

                val phoneRegex = Regex("^0[1-9][0-9]{8}\$")
                if (!phone.matches(phoneRegex)) {
                    phoneError = "Numéro de téléphone invalide"
                    valide = false
                }

                if (valide) {
                    api.AddContact(context, name, phone) { success ->
                        if (success) {
                            name = ""
                            phone = ""
                            showDialog = true
                            isSuccess = true
                            messageDialogue = "Contact ajouté !"
                        } else {
                            isSuccess = false
                            showDialog = true
                            messageDialogue = "Erreur lors de création du contact"
                        }
                    }
                }
            })
        }

        item {
            Text(
                "Maximum : 5",
                color = Color.White,
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Italic,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        item {
            Spacer(Modifier.height(16.dp))
        }

        item {
            Text(
                "Sélectionner des contacts",
                color = AppColors.SentiBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Text(
                "Veuillez nous indiquer, les contacts, que vous souhaitez que Sentinelle contacte dans le cas où vous ne parvenez pas à désactiver le compte à rebours, ou en cas de déclenchement du bouton ALERTER",
                color = Color.White,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(Modifier.height(8.dp))
        }

        // Ajout direct des contacts dans la même LazyColumn
        items(contacts) { contact ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(2f)) {
                    Text(
                        text = contact.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = contact.phone,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }

                IconButton(onClick = {
                    AppValues.contacts.remove(contact)

                    api.deleteContact(
                        context,
                        contact.id
                    ) { success ->
                        if (success) {
                            Log.d("TESTDelete", "Contact ${contact.name} (ID: ${contact.id}) deleted successfully")
                        } else {
                            Log.d("TESTDelete", "Failed to delete contact ${contact.name} (ID: ${contact.id})")
                        }
                    }
                }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = Color.Red
                    )
                }

                Checkbox(
                    checked = contact.selected.value,
                    onCheckedChange = { check ->
                        contact.selected.value = check

                        Log.d("TESTCheck", "Contact ${contact.name} updated to selected: $check")

                        api.selectedContact(
                            context,
                            contact.id,
                            check
                        ) { success ->
                            if (success) {
                                Log.d("TESTCheck", "Contact ${contact.name} (ID: ${contact.id}) updated successfully")
                            } else {
                                Log.d("TESTCheck", "Failed to update contact ${contact.name} (ID: ${contact.id})")
                            }
                        }
                    }
                )
            }
        }

        // Ajout d'un espace en bas pour éviter que le dernier élément soit coupé
        item {
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showDialog) {
        PopupAlert(messageDialogue, isSuccess) {
            showDialog = false
        }
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
    CONTACT("contact", "Contact", Icons.Default.ThumbUp, "Contact"),
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
        containerColor = AppColors.SentiBlack,
        topBar = {
            PrimaryTabRow(
                selectedTabIndex = selectedDestination,
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
        }
    ) { contentPadding ->
        // Passer le contentPadding au NavHost
        AppNavHost(
            navController = navController,
            startDestination = startDestination,
            contentPadding = contentPadding
        )
    }
}


@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: Destination,
    contentPadding: PaddingValues, // Nouveau paramètre
    modifier: Modifier = Modifier
) {
    NavHost(
        navController,
        startDestination = startDestination.route,
        modifier = modifier.padding(contentPadding) // Appliquer le padding ici
    ) {
        Destination.entries.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    Destination.MESSAGE -> MessageScreen()
                    Destination.CONTACT -> ContactScreen()
                    Destination.PLAYLISTS -> PlaylistScreen()
                }
            }
        }
    }
}