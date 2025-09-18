package com.example.sentinelle.page

import android.media.MediaPlayer
import android.net.Uri
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    val api = api_service(context)
    LaunchedEffect(id) {
        try {
            if (id != null) {
                api.GetSafeRiderDetail(context, id) { jsonObject ->
                    val list = mutableListOf<AudioRecord>()
                    val dataObject = jsonObject.getJSONObject("data")
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
            .background(colors[0])
            .padding(16.dp)
    ) {
        Text(
            "Détail du Saferider #$id",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

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
            .padding(8.dp)
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
                            tint = colors[4]
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
