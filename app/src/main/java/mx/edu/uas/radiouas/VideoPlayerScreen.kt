package mx.edu.uas.radiouas

import androidx.annotation.OptIn
import androidx.compose.runtime.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.media3.common.util.UnstableApi

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen() {
    val context = LocalContext.current
    val videoUrl = "https://stream8.mexiserver.com:2000/hls/radiouasx/radiouasx.m3u8"

    // Usamos remember para que el player no se recree infinitamente
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
            playWhenReady = true
        }
    }

    // Es vital soltar el player para que no sature la memoria
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    // Usamos la configuración mínima para evitar errores de renderizado
                    useController = true
                    controllerAutoShow = true
                }
            },
            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
        )
    }
}