package mx.edu.uas.radiouas.ui.screens

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
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
    val videoUrl = AppConfig.VIDEO_STREAM_URL
    val apiUrl = AppConfig.LIVE_PROGRAM_URL

    // 1. Estados para la información del programa
    var programName by remember { mutableStateOf("Cargando programa...") }
    var producerName by remember { mutableStateOf("Sintonizando señal...") }

    // 2. Efecto para cargar los datos al abrir la pantalla
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                // Hacemos la petición HTTP cruda (igual que en tu ViewModel)
                val jsonString = URL(apiUrl).readText()
                val jsonObject = JSONObject(jsonString)

                val programa = jsonObject.optString("programa", AppConfig.LIVE_RADIO_FEED_TITLE)
                val produccion = jsonObject.optString("produccion", AppConfig.LIVE_RADIO_FEED_SUBTITLE)

                // Actualizamos la UI en el hilo principal
                withContext(Dispatchers.Main) {
                    programName = programa
                    producerName = produccion
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    programName = AppConfig.LIVE_RADIO_FEED_TITLE
                    producerName = AppConfig.LIVE_RADIO_FEED_SUBTITLE
                }
            }
        }
    }

    // 3. Configuración del Player (Tu código original)
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    // 4. Diseño de la pantalla
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)) // Fondo gris claro suave
    ) {
        // --- ENCABEZADO CON INFO ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text(
                text = "AHORA EN RADIOUAS TV",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Red,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = programName,
                style = MaterialTheme.typography.titleLarge, // Texto grande
                color = Color(0xFF003366), // Azul UAS
                fontWeight = FontWeight.Bold
            )
            Text(
                text = producerName,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        // --- REPRODUCTOR DE VIDEO ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black), // Fondo negro para el video
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                        controllerAutoShow = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f) // Formato Widescreen estándar
            )
        }

        // Espacio extra abajo o más contenido si quisieras
    }
}