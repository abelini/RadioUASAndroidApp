package mx.edu.uas.radiouas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

class RadioViewModel(application: Application) : AndroidViewModel(application) {

    // Inicializamos ExoPlayer de forma única
    val player: ExoPlayer = ExoPlayer.Builder(application).build()

    fun reproducirAudio(url: String) {
        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    fun detener() {
        player.stop()
    }

    override fun onCleared() {
        super.onCleared()
        player.release() // Liberamos memoria al cerrar la app
    }
}