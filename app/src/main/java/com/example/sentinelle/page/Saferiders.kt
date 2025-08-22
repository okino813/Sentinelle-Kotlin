package com.example.sentinelle.page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sentinelle.api.AppValues
import com.example.sentinelle.api.Saferider
import com.example.sentinelle.api.SaferiderItem
import com.example.sentinelle.api.api_service

@Composable
fun AppNavigation(saferiders: List<Saferider>, colors: List<Color>) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "list"
    ) {
        // Écran liste
        composable("list") {
            SaferidersScreen(
                saferiders = saferiders,
                colors = colors,
                onNavigateToDetail = { id ->
                    navController.navigate("detail/$id")
                },
                modifier = Modifier,
            )
        }

        // Écran détail (avec paramètre)
        composable(
            route = "detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val saferiderId = backStackEntry.arguments?.getInt("id")
            SaferiderDetailScreen(saferiderId)
        }
    }
}

class SaferiderViewModel : ViewModel() {

    // State observable par Compose
    var saferiders by mutableStateOf<List<Saferider>>(emptyList())

    init {
        // Initialisation depuis AppValues si tu utilises un stockage global
        // Adapte le mapping selon la structure réelle des objets dans AppValues.saferider
        try {
            saferiders = AppValues.saferiders.map { c ->
                Saferider(
                    id = c.id,
                    path = c.path,
                    start_date = c.start_date,
                    theorotical_end_date = c.theorotical_end_date,
                    real_end_date = c.real_end_date,
                    locked = c.locked,
                    status = c.status
                )
            }
        } catch (e: Exception) {
            // fallback si AppValues n'existe pas ou format différent
            saferiders = emptyList()
        }
    }
}

@Composable
fun SaferidersScreen(
    colors: List<Color>,
    modifier: Modifier,
    saferiders: List<Saferider>,
    onNavigateToDetail: (Int) -> Unit,
) {
    // Déclaration des variables
    val context = LocalContext.current
    val api = api_service(context) // ton service réseau
    Box(
        modifier = Modifier
            .background(colors[0])
    ) {
        Column(
            modifier = modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.SpaceBetween,
        )
        {
            Text(
                "Liste des SafeRiders",
                color = colors[3],
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(Modifier.height(16.dp))
            Text(
                "Voici la liste des trajets protégés récents, appuyez longtemps sur un trajet pour visualisé les enregistrements",
                color = Color.White,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Box(modifier = Modifier.fillMaxSize()) {

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(saferiders, key = { it.id }) { saferider ->
                        SaferiderItem(
                            saferider = saferider,
                            colors = colors,
                            onDelete = {
//                                onDeleteContact(contact)
                            },
                            onClick = {id ->
                                onNavigateToDetail(id)
                            }
                        )
                    }
                }
            }
        }

    }
}



    // Ajoute (immutably)
//    fun addContact(contact: Contact) {
//        contacts = contacts + contact
//    }
//
//    // Supprime (immutably)
//    fun deleteContact(contactId: Int) {
//        contacts = contacts.filterNot { it.id == contactId }
//    }
//
//    // Basculer la sélection (optimistic update)
//    fun setContactSelected(contactId: Int, isSelected: Boolean) {
//        contacts = contacts.map { c ->
//            if (c.id == contactId) c.copy(selected = isSelected) else c
//        }
//    }
//
//    // Remettre une liste complète (utile si tu veux re-sync depuis le serveur)
//    fun updateContacts(newList: List<Contact>) {
//        contacts = newList
//    }



//@Composable
//fun SaferidersStateless(
//    colors: List<Color>,
//    modifier: Modifier,
//    onNavigateToDetail: (Int) -> Unit = {},
//    saferiders: List<Saferider>,
//) {
//
//}

@Composable
fun SaferiderDetailScreen(id: Int?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (id != null) {
            Text("Détail du Saferider #$id", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        } else {
            Text("Aucun ID trouvé", color = Color.Red)
        }
    }
}
