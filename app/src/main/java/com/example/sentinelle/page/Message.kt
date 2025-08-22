package com.example.sentinelle.page

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sentinelle.api.AppValues
import com.example.sentinelle.api.Bouton
import com.example.sentinelle.api.Contact
import com.example.sentinelle.api.ContactItem
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
fun MessageScreen(
    colors: List<Color>,
) {

    var showDialog by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf<Boolean>(false) }
    var messageDialogue by remember { mutableStateOf("") }

    var message by remember { mutableStateOf(AppValues.message.toString()) }
    var messageError by remember { mutableStateOf<String?>(null) }

    var context = LocalContext.current
    val api = api_service(context)

    fun validationMessage(){
        var valide = true;


        if (message.length < 50) {
            messageError = "Le message doit faire plus de 50 caractères"
            valide = false
        }


        if (valide) {
            messageError = null
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
    }


    MessageScreenStateless(
        colors = colors,
        message = message,
        messageError = messageError,
        onMessageChange = { message = it },
        validationMessage = { validationMessage() } // Fonction de validation
    )

    // Et dans le corps de SettingsScreen (en bas du Column par exemple) :
    if (showDialog) {
        PopupAlert(messageDialogue,colors = colors, isSuccess = isSuccess) {
            showDialog = false
        }
    }


}
@Composable
fun MessageScreenStateless(
    colors: List<Color>,
    message: String,
    messageError: String?,
    onMessageChange: (String) -> Unit,
    validationMessage: () -> Unit // Fonction de validation par défaut
) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .background(colors[0])
                .padding(16.dp)
                .fillMaxSize(),
        ) {
            UpdateStatusBarColor(colors[0], LocalContext.current)
            Spacer(Modifier.height(16.dp))

            Text(
                "Message personalisé",
                color = colors[3],
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
                colors = colors,
                onValueChange = onMessageChange,
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

            Bouton("Enregistrer",modifier = Modifier.align(Alignment.CenterHorizontally), colors = colors,
                OnClick = {
                validationMessage()
            })
        }
    }
}

class ContactViewModel : ViewModel() {

    // State observable par Compose
    var contacts by mutableStateOf<List<Contact>>(emptyList())
//        private set

    init {
        // Initialisation depuis AppValues si tu utilises un stockage global
        // Adapte le mapping selon la structure réelle des objets dans AppValues.contacts
        try {
            contacts = AppValues.contacts.map { c ->
                Contact(
                    id = c.id,
                    name = c.name,
                    phone = c.phone,
                    selected = c.selected // si AppValues stocke selected
                )
            }
        } catch (e: Exception) {
            // fallback si AppValues n'existe pas ou format différent
            contacts = emptyList()
        }
    }

    // Ajoute (immutably)
    fun addContact(contact: Contact) {
        contacts = contacts + contact
    }

    // Supprime (immutably)
    fun deleteContact(contactId: Int) {
        contacts = contacts.filterNot { it.id == contactId }
    }

    // Basculer la sélection (optimistic update)
    fun setContactSelected(contactId: Int, isSelected: Boolean) {
        contacts = contacts.map { c ->
            if (c.id == contactId) c.copy(selected = isSelected) else c
        }
    }

    // Remettre une liste complète (utile si tu veux re-sync depuis le serveur)
    fun updateContacts(newList: List<Contact>) {
        contacts = newList
    }
}


@Composable
fun ContactScreen(
    colors: List<Color>,
    modifier: Modifier = Modifier,
    viewModel: ContactViewModel = viewModel() // import : androidx.lifecycle.viewmodel.compose.viewModel
) {
    val context = LocalContext.current
    val api = api_service(context) // ton service réseau
    val contacts = viewModel.contacts // lecture directe du state (recomposition automatique)

    var showDialog by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    var messageDialogue by remember { mutableStateOf("") }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    // --- Fonctions locales utilisant le ViewModel (optimistic UI) ---

    // Sélection d'un contact : on met à jour l'UI immédiatement,
    // on appelle l'API et on revert si échec
    fun onSelectContact(contact: Contact, checked: Boolean) {
        Log.d("ContactScreen", "optimistic select ${contact.name} -> $checked")
        // Optimistic update
        viewModel.setContactSelected(contact.id, checked)

        api.selectedContact(context, contact.id, checked) { success ->
            if (!success) {
                Log.d("ContactScreen", "API failed to update selection for ${contact.id}, reverting")
                // revert
                viewModel.setContactSelected(contact.id, !checked)
                isSuccess = false
                messageDialogue = "Impossible de mettre à jour la sélection (réseau)"
                showDialog = true
            } else {
                // Si tu veux synchroniser AppValues global :
                try {
                    AppValues.contacts.find { it.id == contact.id }?.selected = checked
                } catch (_: Exception) { /* ignore si AppValues pas présent */ }
            }
        }
    }

    // Suppression : on supprime localement, tente la suppression réseau, et ré-ajoute en cas d'échec
    fun onDeleteContact(contact: Contact) {
        val previous = contacts // snapshot
        viewModel.deleteContact(contact.id)

        api.deleteContact(context, contact.id) { success ->
            if (!success) {
                Log.d("ContactScreen", "Failed to delete ${contact.id}, restoring local state")
                // restore previous list
                viewModel.updateContacts(previous)
                isSuccess = false
                messageDialogue = "Suppression impossible (réseau)"
                showDialog = true
            } else {
                // sync AppValues if needed
                try {
                    AppValues.contacts.removeAll { it.id == contact.id }
                } catch (_: Exception) {}
            }
        }
    }

    // Ajout d'un contact (on attend la réponse de l'API pour récupérer l'ID réel)
    fun onAddContact() {
        nameError = null
        phoneError = null
        var valide = true

        if (name.isBlank()) {
            nameError = "Le nom doit être renseigné"
            valide = false
        }

        val phoneRegex = Regex("^0[1-9][0-9]{8}\$")
        if (!phone.matches(phoneRegex)) {
            phoneError = "Numéro de téléphone invalide"
            valide = false
        }

        if (!valide) return

        // appel réseau
        api.AddContact(context, name, phone) { success, id_contact ->
            if (success && id_contact != null) {
                val newContact = Contact(
                    id = id_contact,
                    name = name,
                    phone = phone,
                    selected = false
                )
                // mettre à jour ViewModel (source de vérité)
                viewModel.addContact(newContact)

                // sync AppValues si tu en as besoin
                try {
                    AppValues.contacts.add(
                        Contact(
                            id = id_contact,
                            name = name,
                            phone = phone,
                            selected = false
                        )
                    )
                } catch (_: Exception) { }

                // feedback UI
                name = ""
                phone = ""
                isSuccess = true
                messageDialogue = "Contact ajouté !"
                showDialog = true
            } else {
                isSuccess = false
                messageDialogue = "Erreur lors de création du contact"
                showDialog = true
            }
        }
    }

    // --- Affichage ---
    ContactScreenStateless(
        colors = colors,
        modifier = modifier,
        phone = phone,
        phoneError = phoneError,
        name = name,
        nameError = nameError,
        onNameChange = { name = it },
        onPhoneChange = { phone = it },
        valideNewContact = { onAddContact() },
        contacts = contacts,
        onDeleteContact = { onDeleteContact(it) },
        onSelectContact = { contact, checked -> onSelectContact(contact, checked) }
    )

    if (showDialog) {
        PopupAlert(messageDialogue, colors = colors, isSuccess = isSuccess) {
            showDialog = false
        }
    }
}

@Composable
fun ContactScreenStateless(
    colors: List<Color>,
    modifier: Modifier = Modifier,
    phone: String,
    phoneError: String?,
    name: String,
    nameError: String?,
    onNameChange : (String) -> Unit,
    onPhoneChange : (String) -> Unit,
    valideNewContact: () -> Unit,
    contacts: List<Contact>,
    onDeleteContact: (Contact) -> Unit,
    onSelectContact: (Contact, Boolean) -> Unit,
) {
    UpdateStatusBarColor(colors[0], LocalContext.current)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors[0])
            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // --- Formulaire ---
        Text(
            "Ajouter un contact",
            color = colors[3],
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Text(
            "Ajouter votre contact ci-dessous.",
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            "Exemple : 0712131415",
            color = Color.White,
            fontStyle = FontStyle.Italic,
            fontSize = 16.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Input("Nom et Prénom", value = name, colors = colors, onValueChange = onNameChange, false, nameError)

        Spacer(modifier = Modifier.height(8.dp))

        Input("Numéro de téléphone", value = phone, colors = colors, onValueChange = onPhoneChange, false, phoneError)

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Bouton("Enregistrer", colors = colors, OnClick = {
                valideNewContact()
            })
        }

        Text(
            "Maximum : 5",
            color = Color.White,
            fontStyle = FontStyle.Italic,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Text(
            "Sélectionner des contacts",
            color = colors[3],
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            "Veuillez nous indiquer les contacts que vous souhaitez que Sentinelle contacte ...",
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        // LazyColumn prend le reste de l'espace pour permettre le scroll correctement
        Box(modifier = Modifier.fillMaxSize()) {

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(contacts, key = { it.id }) { contact ->
                    ContactItem(
                        contact = contact,
                        colors = colors,
                        onDelete = { onDeleteContact(contact) },
                        onSelect = { checked -> onSelectContact(contact, checked) }
                    )
                }
            }
        }

//        Spacer(Modifier.height(16.dp))
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
fun NavigationTabExample(
    colors : List<Color>,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val startDestination = Destination.MESSAGE
    var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

    Scaffold(
        modifier = modifier,
        containerColor = colors[0],
        topBar = {
            PrimaryTabRow(
                selectedTabIndex = selectedDestination,
                containerColor = colors[0],
                contentColor = colors[3],
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
            colors,
            navController = navController,
            startDestination = startDestination,
            contentPadding = contentPadding
        )
    }
}


@Composable
fun AppNavHost(
    colors: List<Color>,
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
                    Destination.MESSAGE -> MessageScreen(
                        colors = colors
                    )
                    Destination.CONTACT -> ContactScreen(
                        colors = colors
                    )
                    Destination.PLAYLISTS -> PlaylistScreen()
                }
            }
        }
    }
}