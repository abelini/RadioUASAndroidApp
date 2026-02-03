package mx.edu.uas.radiouas

import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class RadioAudioService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        // 1. CONFIGURACIÓN DE AUDIO (IMPORTANTE PARA INTERRUPCIONES)
        // Esto asegura que si te llaman, la radio se pause automáticamente,
        // y si llega una notificación, baje el volumen momentáneamente.
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        // 2. CONFIGURACIÓN DE RED
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("RadioUAS_App/1.0")
            .setAllowCrossProtocolRedirects(true)
        // .setConnectTimeoutMs(8000) // Opcional: Tiempo de espera

        val mediaSourceFactory = DefaultMediaSourceFactory(this)
            .setDataSourceFactory(dataSourceFactory)

        // 3. CREAR EL PLAYER
        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .setAudioAttributes(audioAttributes, true) // Activa el manejo de foco de audio
            .setHandleAudioBecomingNoisy(true) // Pausa si desconectan los audífonos
            .setWakeMode(C.WAKE_MODE_NETWORK)  // VITAL: Evita que el WiFi se duerma al apagar pantalla
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

    // Esto ayuda a limpiar el servicio si el usuario cierra la app desde "recientes"
    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }
}