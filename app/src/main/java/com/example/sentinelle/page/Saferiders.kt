package com.example.sentinelle.page

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sentinelle.api.Saferider
import com.example.sentinelle.api.SaferiderItemWrapper
import com.example.sentinelle.api.api_service

//
@Composable
fun AppNavigation(
    colors: List<Color>,
    saferiders: List<Saferider>
    ) {
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
                onNavigateToDetail = { id : Int ->
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

@Composable
fun SaferidersScreen(
    colors: List<Color>,
    modifier: Modifier,
    saferiders: List<Saferider>,
    onNavigateToDetail: (Int) -> Unit,
) {
    Box(
        modifier = Modifier
            .background(colors[0])
    ) {
        Column(
            modifier = modifier
                .padding(top=50.dp, start= 16.dp, end=16.dp, bottom=16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
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
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    userScrollEnabled = true
                ) {
                    items(
                        saferiders,
                        key = { saferider -> saferider.id }
                    ) { saferider ->
                        SaferiderItemWrapper(
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
//


@Composable
fun SaferiderDetailScreen(id: Int?) {
    val context = LocalContext.current
    val api = api_service(context)
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        api.GetSafeRiderDetail(context, id_saferider = id) { jsonObject ->
            Log.d("testgetdetail", "Détail du saferider: $jsonObject")
        }
        if (id != null) {

            Text("Détail du Saferider #$id", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        } else {
            Text("Aucun ID trouvé", color = Color.Red)
        }
    }
}
