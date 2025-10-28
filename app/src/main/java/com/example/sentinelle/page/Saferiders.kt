package com.example.sentinelle.page

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sentinelle.ApiHelper
import com.example.sentinelle.R
import com.example.sentinelle.api.AppValues
import com.example.sentinelle.api.AudioRecord
import com.example.sentinelle.api.PopupAlert
import com.example.sentinelle.api.PopupAlertRequest
import com.example.sentinelle.api.Saferider
import com.example.sentinelle.api.Saferider.Companion.getDate
import com.example.sentinelle.api.Saferider.Companion.getHourMinute
import com.example.sentinelle.api.SaferiderItemWrapper
import com.example.sentinelle.api.api_service
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun AppNavigation(
    colors: List<Color>,
    saferiders: List<Saferider>,
    OnRefresh: () -> Unit = {},
) {
    val navController = rememberNavController()
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colors[0]
    ) {

        NavHost(
            navController = navController,
            startDestination = "list"
        ) {
            composable("list") {
                SaferidersScreen(
                    saferiders = saferiders,
                    colors = colors,
                    onNavigateToDetail = { id: Int ->
                        navController.navigate("detail/$id")
                    },
                    modifier = Modifier,
                    onRefresh = {
                        OnRefresh()
                    }
                )
            }

            composable(
                route = "detail/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val saferiderId = backStackEntry.arguments?.getInt("id")

                SaferiderDetailScreen(saferiderId, colors = colors, onRefresh = {
                    OnRefresh()
                    navController.popBackStack()
                })
            }
        }
    }
}

@Composable
fun SaferidersScreen(
    colors: List<Color>,
    modifier: Modifier,
    saferiders: List<Saferider>,
    onNavigateToDetail: (Int) -> Unit,
    onRefresh: () -> Unit
) {

    var localSaferiders by remember { mutableStateOf(saferiders) }

    LaunchedEffect(saferiders) {
        localSaferiders = saferiders
    }

    var showDialog by remember { mutableStateOf(false) }
    var showDialogRequest by remember { mutableStateOf(false) }
    var isSuccessRequest by remember { mutableStateOf<Boolean>(false) }
    var isSuccess by remember { mutableStateOf<Boolean>(false) }
    var messageDialogueRequest by remember { mutableStateOf("") }
    var messageDialogue by remember { mutableStateOf("") }

    var onAccept by remember { mutableStateOf<() -> Unit>({}) }
    var onDismiss by remember { mutableStateOf<() -> Unit>({}) }

    val context = LocalContext.current
    var api = api_service(context)

    Box(
        modifier = Modifier
            .background(colors[0])
    ) {
        Column(
            modifier = modifier
                .padding(top = 50.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "Liste des SafeRiders",
                color = colors[3],
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(Modifier.height(16.dp))
            Text(
                "Voici la liste des trajets protégés récents, appuyez longtemps sur un trajet pour visualiser les enregistrements",
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
                        localSaferiders,
                        key = { saferider -> saferider.id }
                    ) { saferider ->
                        SaferiderItemWrapper(
                            saferider = saferider,
                            colors = colors,
                            onDelete = {
                                showDialogRequest = true
                                messageDialogueRequest = "Voulez-vous vraiment supprimer ce Saferider ?"
                                onAccept = {
                                    // On test si le minuteur n'ai pas en cours
                                    val prefs = context.getSharedPreferences("sentinelle_prefs", Context.MODE_PRIVATE)
                                    val isRunning = prefs.getBoolean("is_timer_running", false)
                                    if(isRunning){
                                        messageDialogue = "Impossible de supprimer un Saferider pendant un trajet protégé"
                                        isSuccess = false
                                        showDialog = true
                                        showDialogRequest = false
                                    }
                                    else{


                                        api.DeleteSaferider(context, saferider.id) { success ->
                                            if (success) {
                                                AppValues.saferiders.remove(saferider)
                                                localSaferiders = localSaferiders.filter { it.id != saferider.id }
                                                api.getInfo(context)

                                                messageDialogue = "Le Saferider a été supprimé"
                                                isSuccess = true
                                                showDialog = true
                                                showDialogRequest = false
                                                Handler(Looper.getMainLooper()).postDelayed({
                                                    showDialog = false
                                                }, 1500)
                                            } else {
                                                messageDialogue = "Erreur lors de la suppression"
                                                isSuccess = false
                                                showDialog = true
                                                showDialogRequest = false

                                            }
                                        }


                                    }
                                }

                                onDismiss = {
                                    showDialogRequest = false
                                }
                            },
                            onClick = { id ->
                                onNavigateToDetail(id)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        PopupAlert(messageDialogue,colors = colors, isSuccess = isSuccess) {
            showDialog = false
        }
    }


    if(showDialogRequest){
        PopupAlertRequest(message = messageDialogueRequest,colors = colors, isSuccess = isSuccessRequest, onAccept = onAccept, onDismiss = onDismiss)
    }
}

@Composable
fun SafeRiderMap(
    coordinates: List<Pair<Double, Double>>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(coordinates) {
        Log.d("SafeRiderMap", "Nombre de coordonnées: ${coordinates.size}")
    }

    if (coordinates.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color.Gray.copy(alpha = 0.3f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Aucune coordonnée disponible",
                color = Color.White,
                fontSize = 16.sp
            )
        }
        return
    }

    var mapImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Créer l'URL de l'API
    val mapUrl = remember(coordinates) {
        buildMapBoxUrl(coordinates)
    }

    Log.d("SafeRiderMap", "URL de la carte: $mapUrl")

    // Charger l'image de la carte
    LaunchedEffect(mapUrl) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("SafeRiderMap", "Chargement de: $mapUrl")

                val url = URL(mapUrl)
                val connection = url.openConnection().apply {
                    setRequestProperty("User-Agent", "SafeRider-App")
                    connectTimeout = 10000
                    readTimeout = 10000
                }

                val inputStream = connection.getInputStream()
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                withContext(Dispatchers.Main) {
                    if (bitmap != null) {
                        mapImage = bitmap.asImageBitmap()
                        isLoading = false
                        errorMessage = null
                        Log.d("SafeRiderMap", "Carte chargée avec succès")
                    } else {
                        errorMessage = "Impossible de décoder l'image"
                        isLoading = false
                    }
                }
            } catch (e: Exception) {
                Log.e("SafeRiderMap", "Erreur: ${e.message}")
                withContext(Dispatchers.Main) {
                    errorMessage = "Erreur réseau: ${e.message?.take(50) ?: "Inconnue"}"
                    isLoading = false
                }
            }
        }
    }


    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(Color.Black.copy(alpha = 0.1f))
    ) {
        when {
            isLoading -> {
                // Indicateur de chargement
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = colors[1],
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Génération de la carte...",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            errorMessage != null -> {
                // Message d'erreur avec retry
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painterResource(id = android.R.drawable.stat_notify_error),
                            contentDescription = "Erreur",
                            tint = colors[4],
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            errorMessage!!,
                            color = Color.White,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Appuyez pour réessayer",
                            color = colors[3],
                            fontSize = 10.sp,
                            modifier = Modifier.clickable {
                                isLoading = true
                                errorMessage = null
                            }
                        )
                    }
                }
            }

            mapImage != null -> {
                // Afficher la carte
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        bitmap = mapImage!!,
                        contentDescription = "Carte du trajet",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

private fun buildMapBoxUrl(coordinates: List<Pair<Double, Double>>): String {
    val accessToken = "AIzaSyBZWWgX4v2Yl0i7G96tLfygcH-qMLevBIs"
    // Construction du path : longitude,latitude séparés par |
    val pathCoords = coordinates.joinToString("|") { "${it.first},${it.second}" }
    val urltest= "https://maps.googleapis.com/maps/api/staticmap?size=600x400&path=color:0x0000ff|weight:5|$pathCoords&key=$accessToken\n"

    return urltest
}

@OptIn(ExperimentalLayoutApi::class)
@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun SaferiderDetailScreen(id: Int?, colors: List<Color>, onRefresh: () -> Unit = {}) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    var audioList by remember { mutableStateOf<List<AudioRecord>>(emptyList()) }
    var saferider by remember { mutableStateOf<Saferider?>(null) }
    var coords by remember { mutableStateOf<List<Pair<Double, Double>>>(emptyList()) }
    var coordsComplete by remember { mutableStateOf<List<Triple<Double, Double, Double>>>(emptyList()) }
    val scrollState = rememberScrollState()

    var all_tags by remember {mutableStateOf<List<Triple<Int, String, String>>>(emptyList())}
    var saferider_tags by remember {mutableStateOf<List<Triple<Int, String, String>>>(emptyList())}

    var showDialog by remember { mutableStateOf(false) }
    var showDialogRequest by remember { mutableStateOf(false) }
    var isSuccessRequest by remember { mutableStateOf<Boolean>(false) }
    var isSuccess by remember { mutableStateOf<Boolean>(false) }
    var messageDialogueRequest by remember { mutableStateOf("") }
    var messageDialogue by remember { mutableStateOf("") }

    var onAccept by remember { mutableStateOf<() -> Unit>({}) }
    var onDismiss by remember { mutableStateOf<() -> Unit>({}) }

    val api = api_service(context)
    LaunchedEffect(id) {
        try {
            if (id != null) {
                api.GetSafeRiderDetail(context, id) { jsonObject ->
                    val list = mutableListOf<AudioRecord>()
                    val dataObject = jsonObject.getJSONObject("data")

                    all_tags = buildList {
                        val TagsArray = dataObject.getJSONArray("all_tag")
                        for (i in 0 until TagsArray.length()) {
                            val item = TagsArray.getJSONObject(i)
                            add(Triple(item.getInt("id"), item.getString("name"), item.getString("hexa")))
                        }
                    }

                    saferider_tags = buildList {
                        val TagsArray = dataObject.getJSONArray("saferider_tags")
                        for (i in 0 until TagsArray.length()) {
                            val item = TagsArray.getJSONObject(i)
                            add(Triple(item.getInt("id"), item.getString("name"), item.getString("hexa")))
                        }
                    }

                    val audioArray = dataObject.getJSONArray("audio_records")
                    Log.d("SaferiderDetail", "Détails du Saferider: $jsonObject")
                    for (i in 0 until audioArray.length()) {
                        val item = audioArray.getJSONObject(i)
                        list.add(
                            AudioRecord(
                                item.getInt("id"),
                                item.getString("date"),
                                item.getString("path")
                            )
                        )
                    }
                    audioList = list
                    saferider = Saferider(
                        id = dataObject.optInt("id_saferider"),
                        path = dataObject.optString("path", ""),
                        start_date = dataObject.optString("start_date", ""),
                        theorotical_end_date = dataObject.optString("theorotical_end_date", ""),
                        real_end_date = dataObject.optString("real_end_date", ""),
                        locked = dataObject.optBoolean("locked", false),
                        status = dataObject.optInt("status", 0)
                    )

                    coords = buildList {
                        val coordArray = dataObject.getJSONArray("coordinates")
                        for (i in 0 until coordArray.length()) {
                            val item = coordArray.getJSONObject(i)
                            add(Pair(item.getString("latitude").toDouble(), item.getString("longitude").toDouble()))
                        }
                    }

                    coordsComplete = buildList {
                        val coordArray = dataObject.getJSONArray("coordinates")
                        for (i in 0 until coordArray.length()) {
                            val item = coordArray.getJSONObject(i)
                            add(Triple(item.getString("latitude").toDouble(), item.getString("longitude").toDouble(), item.getString("date").toDouble()))
                        }
                    }


                }
            }
        } catch (e: Exception) {
            Log.e("SaferiderDetail", "Erreur lors de la récupération des détails du Saferider", e)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 50.dp, start = 16.dp,bottom = 120.dp, end = 16.dp)
            .background(colors[0])
            .verticalScroll(scrollState)

//            .simpleVerticalScrollbar(listState)
    ) {
        Text(
            "Trajet Protégé",
            color = colors[3],
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        saferider?.let { s ->
            Row {
                Text(
                    "Début : ",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                )

                Text(
                    " ${getDate(s.start_date)} à ${getHourMinute(s.start_date)}",
                    color = Color.White,
                    fontWeight = FontWeight.Normal,
                    fontSize = 17.sp,
                    fontStyle = FontStyle.Italic
                )
            }

            Row {
                Text(
                    "Fin      : ",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                )

                Text(
                    " ${getDate(s.real_end_date)} à ${getHourMinute(s.real_end_date)}",
                    color = Color.White,
                    fontWeight = FontWeight.Normal,
                    fontSize = 17.sp,
                    fontStyle = FontStyle.Italic
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (s.status == 1) {
                    Row {
                        Icon(
                            painterResource(id = R.drawable.validate),
                            contentDescription = "Audio",
                            tint = Color.Unspecified,
                            modifier = Modifier.padding(end = 8.dp).size(30.dp),
                        )

                        Text(
                            "Arrêté à temps",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                        )
                    }
                } else if (s.status == 0) {
                    Row {
                        Icon(
                            painterResource(id = R.drawable.pending),
                            contentDescription = "Audio",
                            tint = Color.Unspecified,
                            modifier = Modifier.padding(end = 8.dp).size(30.dp),
                        )

                        Text(
                            "En cours",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                        )
                    }
                } else {
                    Row {
                        Icon(
                            painterResource(id = R.drawable.danger),
                            contentDescription = "Audio",
                            tint = Color.Unspecified,
                            modifier = Modifier.padding(end = 8.dp).size(30.dp),
                        )

                        Text(
                            "A été déclanché",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                        )
                    }
                }




                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painterResource(id = R.drawable.download),
                        contentDescription = "Download",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(30.dp)
                            .clickable {
                                var succes = api.DownloadSaferider(context, listOf(s), coordsComplete, audioList)
                                if(succes){
                                    isSuccess = true
                                    showDialog = true
                                    messageDialogue = "Saferider téléchargé dans vos documents !"
                                }
                                else{
                                    isSuccess = false
                                    showDialog = true
                                    messageDialogue = "Erreur lors du téléchargement"
                                }
                            }
                    )
                    Icon(
                        painterResource(id = R.drawable.trash),
                        contentDescription = "Delete",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(30.dp)
                            .clickable {
                                Log.d("IconClick", "Icône supprimé !")
                                // Demander la confirmation avant de supprimer
                                showDialogRequest = true
                                messageDialogueRequest = "Voulez-vous vraiment supprimer ce Saferider ?"
                                onAccept = {
                                    val prefs = context.getSharedPreferences("sentinelle_prefs", Context.MODE_PRIVATE)
                                    val isRunning = prefs.getBoolean("is_timer_running", false)
                                    if(isRunning){
                                        messageDialogue = "Impossible de supprimer un Saferider pendant un trajet protégé"
                                        isSuccess = false
                                        showDialog = true
                                        showDialogRequest = false
                                    }
                                    else {
                                        api.DeleteSaferider(context, s.id) { success ->
                                            if (success) {

                                                messageDialogue = "Le Saferider a été supprimé"
                                                isSuccess = true
                                                showDialog = true
                                                showDialogRequest = false
                                                Handler(Looper.getMainLooper()).postDelayed({
                                                    showDialog = false
                                                    onRefresh() // Cela va recharger AppValues.saferiders
                                                }, 1500)
                                            } else {
                                                messageDialogue = "Erreur lors de la suppression"
                                                isSuccess = false
                                                showDialog = true
                                                showDialogRequest = false

                                            }
                                        }
                                    }
                                }

                                onDismiss = {
                                    showDialogRequest = false
                                }

                            }
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))


        SafeRiderMap(
            coordinates = coords,
            colors = colors,
        )

        Spacer(Modifier.height(16.dp))


        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            all_tags.forEach { tag ->
                var isSelected by remember(saferider_tags) {
                    mutableStateOf(saferider_tags.any { it.first == tag.first })
                }
                Box(
                    modifier = Modifier
                        .background(
                            // Utilisation de la couleur hexa
                            if (isSelected) Color(tag.third.toLong(16)).copy(alpha = 0.8f)
                            else Color.Gray.copy(alpha = 0.3f)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .clickable(
                            enabled = true,
                            onClick = {
                                if (isSelected) {
                                    // Supprimer le tag
                                    api.RemoveTagFromSaferider(context, id!!, tag.first) { success ->
                                        if (success) {
                                            saferider_tags = saferider_tags.filter { it.first != tag.first }
                                        }
                                    }
                                    isSelected = false
                                } else {
                                    // Ajouter le tag
                                    api.AddTagToSaferider(context, id!!, tag.first) { success ->
                                        if (success) {
                                            saferider_tags = saferider_tags + tag
                                        }
                                    }
                                    isSelected = true
                                }
                            }
                        )
                ) {
                    Text(
                        text = tag.second,
                        color = Color.White,
                        fontStyle = FontStyle.Normal,
                        fontSize = 16.sp
                    )
                }
            }
        }


        Column {
            audioList.forEach { audio ->
                ExoPlayerAudioPlayer(audio = audio, colors = colors)
            }
        }

        if (showDialog) {
            PopupAlert(messageDialogue,colors = colors, isSuccess = isSuccess) {
                showDialog = false
            }
        }


        if(showDialogRequest){
            PopupAlertRequest(message = messageDialogueRequest,colors = colors, isSuccess = isSuccessRequest, onAccept = onAccept, onDismiss = onDismiss)
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun ExoPlayerAudioPlayer(audio: AudioRecord, colors: List<Color>) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var progress by remember { mutableStateOf(0f) }
    var duration by remember { mutableStateOf(0L) }
    var currentPosition by remember { mutableStateOf(0L) }

    // Créer ExoPlayer pour ce composant spécifique
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            // Listener pour les changements d'état
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                    Log.d("ExoPlayer", "Playing state changed: $playing")
                }

                override fun onPlayerError(error: PlaybackException) {
                    Log.e("ExoPlayer", "Erreur de lecture: ${error.message}", error)
                    isPlaying = false
                }

                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_READY -> {
                            duration = this@apply.duration
                            Log.d("ExoPlayer", "Player ready, duration: $duration")
                        }
                        Player.STATE_ENDED -> {
                            isPlaying = false
                            progress = 0f
                            Log.d("ExoPlayer", "Playback ended")
                        }
                        else -> {}
                    }
                }
            })
        }
    }

    // Nettoyer l'ExoPlayer quand le composable est détruit
    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
            Log.d("ExoPlayer", "Player released")
        }
    }

    // Mettre à jour la progression
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying && exoPlayer.isPlaying) {
                currentPosition = exoPlayer.currentPosition
                if (duration > 0) {
                    progress = currentPosition / duration.toFloat()
                }
                delay(500)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 20.dp, end = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painterResource(id = R.drawable.frame),
                contentDescription = "Audio",
                tint = colors[1],
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(60.dp)
            )

            Column {
                Row {
                    IconButton(onClick = {
                        Log.d("ExoPlayer", "Clic sur le bouton play/pause")

                        if (isPlaying) {
                            exoPlayer.pause()
                        } else {
                            if (exoPlayer.mediaItemCount == 0) {
                                // Préparer le media item avec headers
                                coroutineScope.launch {
                                    try {
                                        val token = ApiHelper.getToken()
                                        val url = "${AppValues.base_url}/api/listen/${audio.path}"

                                        Log.d("ExoPlayer", "URL: $url")

                                        // Créer DataSource avec headers d'authentification
                                        val dataSourceFactory = DefaultHttpDataSource.Factory()
                                            .setDefaultRequestProperties(
                                                mapOf("Authorization" to "Bearer $token")
                                            )

                                        // Créer le MediaItem
                                        val mediaItem = MediaItem.fromUri(url)

                                        // Configurer ExoPlayer avec la DataSource
                                        exoPlayer.setMediaSource(
                                            androidx.media3.exoplayer.source.ProgressiveMediaSource
                                                .Factory(dataSourceFactory)
                                                .createMediaSource(mediaItem)
                                        )

                                        exoPlayer.prepare()
                                    } catch (e: Exception) {
                                        Log.e("ExoPlayer", "Erreur lors de la préparation", e)
                                    }
                                }
                            }
                            exoPlayer.play()
                        }
                    }) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Lecture",
                            tint = colors[4],
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Text(
                        "Enregistrement à ${audio.formattedDate}",
                        color = Color.White
                    )
                }

                Slider(
                    value = progress,
                    onValueChange = { newValue ->
                        progress = newValue
                    },
                    onValueChangeFinished = {
                        if (duration > 0) {
                            val seekPosition = (progress * duration).toLong()
                            exoPlayer.seekTo(seekPosition)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 10.dp, start = 10.dp),
                    colors = androidx.compose.material3.SliderDefaults.colors(
                        thumbColor = colors[3],
                        activeTrackColor = colors[1],
                        inactiveTrackColor = colors[2]
                    )
                )
            }
        }
    }
}