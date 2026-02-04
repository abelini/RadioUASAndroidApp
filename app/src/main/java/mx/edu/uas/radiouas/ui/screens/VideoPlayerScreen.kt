package mx.edu.uas.radiouas.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.view.View
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.edu.uas.radiouas.utils.AppConfig
import org.json.JSONObject
import java.net.URL

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen() {
    val context = LocalContext.current
    val activity = context as? Activity
    val view = LocalView.current

    // Detectamos la orientación ACTUAL de la pantalla
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Estados
    val videoUrl = AppConfig.VIDEO_STREAM_URL
    val apiUrl = AppConfig.LIVE_PROGRAM_URL
    var programName by remember { mutableStateOf("Cargando programa...") }
    var producerName by remember { mutableStateOf("Sintonizando señal...") }
    var isPlayerPlaying by remember { mutableStateOf(false) }

    // --- FUNCIÓN HELPER: MODO INMERSIVO (OCULTAR BARRAS) ---
    fun toggleSystemBars(fullscreen: Boolean) {
        activity?.let { act ->
            val window = act.window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            if (fullscreen) {
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // --- EFECTO: REACCIONAR AL GIRO DE PANTALLA ---
    // Si la pantalla gira (físicamente o por botón), ajustamos las barras
    LaunchedEffect(isLandscape) {
        toggleSystemBars(isLandscape)
    }

    // --- PLAYER ---
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
            playWhenReady = true
        }
    }

    // --- ESCUCHA DE ESTADO (PLAY/PAUSE) ---
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                isPlayerPlaying = isPlaying
                // LÓGICA CLAVE:
                // Si Play -> Deja que el sensor decida (Sensor permite girar).
                // Si Pause -> Obliga a ponerlo derecho (Portrait).
                activity?.requestedOrientation = if (isPlaying) {
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.stop()
            exoPlayer.release()
            // AL SALIR: Restaurar orden
            toggleSystemBars(false)
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    // --- CARGA DE DATOS ---
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val jsonString = URL(apiUrl).readText()
                val jsonObject = JSONObject(jsonString)
                withContext(Dispatchers.Main) {
                    programName = jsonObject.optString("programa", AppConfig.LIVE_RADIO_FEED_TITLE)
                    producerName = jsonObject.optString("produccion", AppConfig.LIVE_RADIO_FEED_SUBTITLE)
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // --- UI ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isLandscape) Color.Black else Color(0xFFF5F5F5))
    ) {
        // 1. INFO DEL PROGRAMA (Solo visible en Portrait)
        if (!isLandscape) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Text(text = "AHORA EN RADIOUAS TV", style = MaterialTheme.typography.labelSmall, color = Color.Red, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = programName, style = MaterialTheme.typography.titleLarge, color = Color(0xFF003366), fontWeight = FontWeight.Bold)
                Text(text = producerName, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }

        // 2. CONTENEDOR DEL VIDEO
        Box(
            modifier = Modifier
                // AQUÍ ESTÁ EL TRUCO:
                // Si es Landscape -> fillMaxSize (ocupa TODO, sin bordes).
                // Si es Portrait -> fillMaxWidth + aspectRatio (Mantiene formato TV).
                .then(
                    if (isLandscape) Modifier.fillMaxSize()
                    else Modifier.fillMaxWidth().aspectRatio(16f / 9f)
                )
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                        controllerAutoShow = true

                        // CONECTAR EL BOTÓN [ ] DEL PLAYER A LA ORIENTACIÓN
                        setControllerOnFullScreenModeChangedListener { isFullscreenReq ->
                            if (isFullscreenReq) {
                                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                            } else {
                                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Relleno inferior solo si estamos en vertical
        if(!isLandscape) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}