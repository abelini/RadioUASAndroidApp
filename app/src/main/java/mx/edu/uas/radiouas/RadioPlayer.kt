package mx.edu.uas.radiouas

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.Player

@Composable
fun RadioPlayerScreen() {
    val context = LocalContext.current
    val streamUrl = "https://stream9.mexiserver.com/8410/stream"

    // Estado para saber si está cargando o reproduciendo
    var isPlaying by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(streamUrl)
            setMediaItem(mediaItem)
            prepare()

            // Listener para detectar cambios en el reproductor
            addListener(object : Player.Listener {
                override fun onIsLoadingChanged(loading: Boolean) {
                    isLoading = loading
                }
                override fun onPlaybackStateChanged(state: Int) {
                    // Si el estado es BUFFERING, está cargando
                    isLoading = (state == Player.STATE_BUFFERING)
                }
                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                }
            })
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Radio UAS en Vivo", style = MaterialTheme.typography.headlineMedium, color = AzulUAS)

        Spacer(modifier = Modifier.height(30.dp))

        // Si está cargando, mostramos la "ruedita"
        if (isLoading) {
            CircularProgressIndicator(
                color = AzulUAS,
                modifier = Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text("Conectando al servidor...")
        } else {
            // Si ya cargó, mostramos el botón de Play/Pause
            Button(
                onClick = {
                    if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                },
                colors = ButtonDefaults.buttonColors(containerColor = AzulUAS),
                modifier = Modifier.size(width = 200.dp, height = 50.dp)
            ) {
                Text(if (isPlaying) "PAUSAR" else "REPRODUCIR")
            }
        }
    }
}