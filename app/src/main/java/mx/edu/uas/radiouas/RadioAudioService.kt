package mx.edu.uas.radiouas

import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class RadioAudioService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null

    // Usamos @OptIn porque configurar los DataSources es una API avanzada de Media3
    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        // 1. CONFIGURACIÓN DE RED (ESTO ES LO QUE FALTABA)
        // Creamos una configuración HTTP que tenga un nombre de agente y permita redirecciones
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("RadioUAS_App") // Le damos una identidad a la app
            .setAllowCrossProtocolRedirects(true) // Permitimos http -> https

        // 2. FÁBRICA DE MEDIOS
        // Le decimos a ExoPlayer que use nuestra configuración de red
        val mediaSourceFactory = DefaultMediaSourceFactory(this)
            .setDataSourceFactory(dataSourceFactory)

        // 3. CREAR EL PLAYER
        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory) // <--- Aquí aplicamos la configuración
            .build()

        // 4. CREAR LA SESIÓN
        mediaSession = MediaSession.Builder(this, player!!)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }
}