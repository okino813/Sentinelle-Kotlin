package com.example.sentinelle.page

import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sentinelle.ApiHelper
import com.example.sentinelle.R
import com.example.sentinelle.api.AppValues
import com.example.sentinelle.api.AudioRecord
import com.example.sentinelle.api.Saferider
import com.example.sentinelle.api.Saferider.Companion.getDate
import com.example.sentinelle.api.Saferider.Companion.getHourMinute
import com.example.sentinelle.api.SaferiderItemWrapper
import com.example.sentinelle.api.api_service
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    colors: List<Color>,
    saferiders: List<Saferider>
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
                )
            }

            composable(
                route = "detail/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val saferiderId = backStackEntry.arguments?.getInt("id")
                SaferiderDetailScreen(saferiderId, colors = colors)
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
) {
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
                        saferiders,
                        key = { saferider -> saferider.id }
                    ) { saferider ->
                        SaferiderItemWrapper(
                            saferider = saferider,
                            colors = colors,
                            onDelete = {},
                            onClick = { id ->
                                onNavigateToDetail(id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SaferiderDetailScreen(id: Int?, colors: List<Color>) {
    val context = LocalContext.current
    val mediaPlayer = remember { MediaPlayer() }

    var audioList by remember { mutableStateOf<List<AudioRecord>>(emptyList()) }

    var saferider by remember { mutableStateOf<Saferider?>(null) }



    // Varriable pour stocker les données du Saferider
    var id_saferider by remember { mutableStateOf("") }
    var path by remember { mutableStateOf("") }
    var start_date by remember { mutableStateOf("") }
    var real_end_date by remember { mutableStateOf("") }
    var theorotical_end_date by remember { mutableStateOf("") }
    var locked by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }

    val api = api_service(context)
    LaunchedEffect(id) {
        try {
            if (id != null) {
                api.GetSafeRiderDetail(context, id) { jsonObject ->
                    val list = mutableListOf<AudioRecord>()
                    val dataObject = jsonObject.getJSONObject("data")
                    // Créer l'objet Saferider avec les données du JSON

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
                }
            }
        } catch (e: Exception) {
            Log.e(
                "SaferiderDetail",
                "Erreur lors de la récupération des détails du Saferider",
                e
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 50.dp, start = 16.dp, end = 16.dp)
            .background(colors[0])
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

            Row( verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween)
            {

                if(s.status == 1){
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
                } else if(s.status == 0){
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

                // Icon download et delete.
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
                                // Code à exécuter au clic
                                Log.d("IconClick", "Icône downloads !")
                            }
//                            .padding(end = 10.dp)
                    )
                    Icon(
                        painterResource(id = R.drawable.trash),
                        contentDescription = "Delete",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(30.dp)
                            .clickable {
                                Log.d("IconClick", "Icône supprimé !")
                            }
                    )
                }
            }


        }
        LazyColumn {
            items(audioList, key = { it.id }) { audio ->
                AudioPlayer(audio = audio, mediaPlayer = mediaPlayer, colors = colors)
            }
        }
    }
}

@Composable
fun AudioPlayer(audio: AudioRecord, mediaPlayer: MediaPlayer, colors: List<Color>) {
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var duration by remember { mutableStateOf(0) }


    val lifecycleOwner = LocalLifecycleOwner.current

    // Arrêter le son quand la fenêtre est détruite ou mise en pause
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP || event == Lifecycle.Event.ON_PAUSE) {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                    isPlaying = false
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
                isPlaying = false
            }
            mediaPlayer.release()
        }
    }

    // Met à jour la progression toutes les 500ms
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (mediaPlayer.isPlaying) {
                progress = mediaPlayer.currentPosition / mediaPlayer.duration.toFloat()
                delay(500)
            }
        }
        else{
            progress = 0f
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top= 50.dp ,end = 8.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                painterResource(id = R.drawable.frame),
                contentDescription = "Audio",
                tint = colors[1],
                modifier = Modifier.padding(end = 8.dp).size(60.dp)
            )

            var context = LocalContext.current
            var api = api_service(context)
            val scope = rememberCoroutineScope()
            Column {
                Row {

                    IconButton(onClick = {
                        if (isPlaying) {
                            mediaPlayer.stop()
                            isPlaying = false

                            return@IconButton
                        }
                        scope.launch {
                            try {
                                val token = ApiHelper.getToken()
                                val headers = mapOf("Authorization" to "Bearer $token")
                                val url =
                                    Uri.parse("${AppValues.base_url}/api/listen/${audio.path}")

                                mediaPlayer.reset()
                                mediaPlayer.setDataSource(context, url, headers)
                                mediaPlayer.prepareAsync()
                                mediaPlayer.setOnPreparedListener {
                                    it.start()
                                    duration = it.duration
                                    isPlaying = true
                                }
                            } catch (e: Exception) {
                                Log.e("AudioPlay", "Erreur lecture audio", e)
                            }
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
                            val seekTo = (progress * duration).toInt()
                            mediaPlayer.seekTo(seekTo)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(end = 10.dp, start = 10.dp),
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
