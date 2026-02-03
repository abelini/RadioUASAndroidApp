package mx.edu.uas.radiouas

import android.app.Application
import android.content.ComponentName
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mx.edu.uas.radiouas.model.EmbyItem
import mx.edu.uas.radiouas.network.EmbyClient
import mx.edu.uas.radiouas.utils.AppConfig
import org.json.JSONObject
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class RadioViewModel(application: Application) : AndroidViewModel(application) {

    // --- CONFIGURACIÓN (Desde AppConfig) ---
    // Usamos las constantes centralizadas
    private val streamURL = AppConfig.STREAM_AUDIO_URL
    private val programApiUrl = AppConfig.LIVE_PROGRAM_URL

    // Textos por defecto para cuando falla la API o inicia
    private var liveProgramName: String = "Radio UAS - 96.1 FM"
    private var liveProductionName: String = "Señal en vivo"

    // --- ESTADO DE LA UI (COMPOSE) ---
    var isPlaying by mutableStateOf(false)
    var isLoading by mutableStateOf(false)

    // Variables para mostrar en el MiniPlayer
    var currentTitle by mutableStateOf("Cargando...")
    var currentSubtitle by mutableStateOf("Conectando servicio...")
    var currentCoverUrl by mutableStateOf<String?>(null) // Para la portada
    var isLive by mutableStateOf(true) // True = Radio FM, False = Podcast

    // --- REPRODUCTOR MEDIA3 ---
    var player: Player? by mutableStateOf(null)
    private var controllerFuture: ListenableFuture<MediaController>? = null

    init {
        // 1. Iniciar conexión con el servicio
        iniciarConexionServicio()
        // 2. Obtener nombres del programa de radio actual
        obtenerProgramacionEnVivo()
    }

    // -------------------------------------------------------------------------
    // CONEXIÓN CON EL SERVICIO (Media3)
    // -------------------------------------------------------------------------
    private fun iniciarConexionServicio() {
        val context = getApplication<Application>()
        val sessionToken =
            SessionToken(context, ComponentName(context, RadioAudioService::class.java))

        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                val controller = controllerFuture?.get()
                this.player = controller
                configurarListenerPlayer(controller)
                sincronizarEstadoInicial(controller)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())
    }

    private fun configurarListenerPlayer(controller: MediaController?) {
        controller?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(state: Int) {
                isLoading = (state == Player.STATE_BUFFERING)
            }

            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                // Actualizamos la UI cuando cambia la canción/programa internamente
                mediaMetadata.title?.let { currentTitle = it.toString() }
                mediaMetadata.artist?.let { currentSubtitle = it.toString() }

                // Intentamos recuperar la imagen si viene en los metadatos
                mediaMetadata.artworkUri?.let {
                    currentCoverUrl = it.toString()
                }
            }
        })
    }

    private fun sincronizarEstadoInicial(controller: MediaController?) {
        isPlaying = controller?.isPlaying == true

        // Si ya hay algo sonando, recuperamos sus datos
        if ((controller?.mediaItemCount ?: 0) > 0) {
            val item = controller?.currentMediaItem
            val uri = item?.localConfiguration?.uri.toString()

            currentTitle = item?.mediaMetadata?.title.toString()
            currentSubtitle = item?.mediaMetadata?.artist.toString()
            currentCoverUrl = item?.mediaMetadata?.artworkUri?.toString()

            // Determinamos si es radio en vivo basándonos en la URL exacta
            isLive = (uri == streamURL)
        } else {
            // Si no hay nada, preparamos datos de Radio
            establecerDatosRadioDefault()
        }
    }

    // -------------------------------------------------------------------------
    // LÓGICA DE REPRODUCCIÓN (RADIO Y PODCAST)
    // -------------------------------------------------------------------------

    // 1. REPRODUCIR PODCAST
    fun playPodcast(episodio: EmbyItem, imagenAlbumUrl: String) {
        // Obtenemos la URL del audio usando EmbyClient
        val audioUrl = EmbyClient.getAudioUrl(episodio.id)

        // Actualizamos estado visual inmediato
        isLive = false
        currentTitle = episodio.name
        currentSubtitle = "Podcast Radio UAS"
        currentCoverUrl = imagenAlbumUrl

        reproducirAudio(
            url = audioUrl,
            titulo = episodio.name,
            subtitulo = "Podcast",
            imagenUrl = imagenAlbumUrl,
            esStreamRadio = false
        )
    }

    // 2. REPRODUCIR RADIO (O Alternar Play/Pause si ya es radio)
    fun playRadioOAlternar() {
        val p = player ?: return
        val urlActual = p.currentMediaItem?.localConfiguration?.uri?.toString()

        if (urlActual == streamURL) {
            // Ya es radio, solo pausamos/reanudamos
            if (isPlaying) p.pause() else p.play()
        } else {
            // Estaba en podcast o detenido, forzamos Radio
            playRadioForce()
        }
    }

    // En RadioViewModel.kt

    // Función genérica para el botón Play/Pause del MiniPlayer
    fun toggleReproduccion() {
        val p = player ?: return

        // Si el reproductor está vacío o detenido por completo, iniciamos la radio por defecto
        if (p.playbackState == Player.STATE_IDLE || p.mediaItemCount == 0) {
            playRadioForce()
        } else {
            // Si ya tiene algo cargado (Radio o Podcast), solo alternamos estado
            if (isPlaying) {
                p.pause()
            } else {
                p.play()
            }
        }
    }

    // Fuerza la reproducción de la radio (usado al dar clic en el botón flotante de Radio)
    private fun playRadioForce() {
        isLive = true
        establecerDatosRadioDefault()

        reproducirAudio(
            url = streamURL,
            titulo = liveProgramName,
            subtitulo = liveProductionName,
            imagenUrl = obtenerUriLocal(R.drawable.logo_radio_app), // Asegúrate de tener este drawable
            esStreamRadio = true
        )
    }

    // Método genérico interno para cargar media en ExoPlayer
    private fun reproducirAudio(
        url: String,
        titulo: String,
        subtitulo: String,
        imagenUrl: String,
        esStreamRadio: Boolean
    ) {
        val p = player ?: return
        isLoading = true

        val metadata = MediaMetadata.Builder()
            .setTitle(titulo)
            .setArtist(subtitulo)
            .setArtworkUri(Uri.parse(imagenUrl))
            .build()

        // Si es radio usamos AUDIO_MPEG (MP3), si es podcast dejamos que ExoPlayer detecte
        val mimeType = if (esStreamRadio) MimeTypes.AUDIO_MPEG else MimeTypes.AUDIO_MPEG

        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setMimeType(mimeType)
            .setMediaMetadata(metadata)
            .build()

        p.stop()
        p.clearMediaItems()
        p.setMediaItem(mediaItem)
        p.prepare()
        p.play()
    }

    // -------------------------------------------------------------------------
    // DATOS AUXILIARES
    // -------------------------------------------------------------------------

    private fun establecerDatosRadioDefault() {
        currentTitle = liveProgramName
        currentSubtitle = liveProductionName
        currentCoverUrl = obtenerUriLocal(R.drawable.logo_radio_app)
    }

    private fun obtenerProgramacionEnVivo() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Usamos la URL de AppConfig
                val jsonString = URL(programApiUrl).readText()
                val jsonObject = JSONObject(jsonString)

                liveProgramName = jsonObject.optString("programa", "Radio UAS - 96.1 FM")
                liveProductionName = jsonObject.optString("produccion", "Señal En Vivo")

                withContext(Dispatchers.Main) {
                    // Si actualmente estamos escuchando radio, actualizamos los textos en pantalla
                    if (isLive) {
                        currentTitle = liveProgramName
                        currentSubtitle = liveProductionName
                    }
                }
            } catch (e: Exception) {
                // Error silencioso, mantenemos defaults
            }
        }
    }

    private fun obtenerUriLocal(resourceId: Int): String {
        return "android.resource://${getApplication<Application>().packageName}/$resourceId"
    }

    override fun onCleared() {
        super.onCleared()
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }
}