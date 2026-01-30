package mx.edu.uas.radiouas

import android.app.Application
import android.content.ComponentName
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
import org.json.JSONObject
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class RadioViewModel(application: Application) : AndroidViewModel(application) {

    // --- VARIABLES DE LA RADIO ---
    private val RADIO_LOGO_URL = "https://radiouas.org/wp-content/uploads/2023/04/cropped-Logo-Radio-UAS-2023-1.png"
    var currentProgramUrl = "https://spc.radiouas.org/api/schedule/now?format=json"
    var streamURL = "https://stream9.mexiserver.com/8410/stream"

    private var liveProgramName: String = "Radio UAS - 96.1 FM"
    private var liveProductionName: String = "Señal en vivo"

    // --- ESTADOS DE UI ---
    var isPlaying by mutableStateOf(false)
    var isLoading by mutableStateOf(false)

    var currentTitle by mutableStateOf("Cargando...")
    var currentSubtitle by mutableStateOf("Conectando servicio...")

    // --- VARIABLES DE PODCASTS ---
    var listaPodcasts by mutableStateOf<List<Podcast>>(emptyList())
    var programaSeleccionado by mutableStateOf<Podcast?>(null)
    var listaEpisodios by mutableStateOf<List<Podcast>>(emptyList())
    var hayMasEpisodios by mutableStateOf(true)
    var cargandoMas by mutableStateOf(false)

    // --- CONEXIÓN CON EL SERVICIO (NUEVO) ---
    // El player ahora puede ser nulo mientras conecta
    var player: Player? by mutableStateOf(null)
    private var controllerFuture: ListenableFuture<MediaController>? = null

    init {
        iniciarConexionServicio()

        // Cargamos datos iniciales
        obtenerProgramacionEnVivo()
        cargarProgramas()
    }

    private fun iniciarConexionServicio() {
        val context = getApplication<Application>()

        // 1. Creamos el Token para buscar nuestro RadioAudioService
        val sessionToken = SessionToken(
            context,
            ComponentName(context, RadioAudioService::class.java)
        )

        // 2. Construimos el MediaController (Async)
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        // 3. Escuchamos cuando la conexión esté lista
        controllerFuture?.addListener({
            try {
                // Aquí obtenemos el control real del servicio
                val controller = controllerFuture?.get()
                this.player = controller

                // Configuración inicial del controller
                controller?.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(playing: Boolean) {
                        isPlaying = playing
                    }
                    override fun onPlaybackStateChanged(state: Int) {
                        isLoading = (state == Player.STATE_BUFFERING)
                    }
                    // Sincronizar metadatos si la app se cerró y volvió a abrir
                    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                        mediaMetadata.title?.let { currentTitle = it.toString() }
                        mediaMetadata.artist?.let { currentSubtitle = it.toString() }
                    }
                })

                // Actualizamos estado inicial por si ya estaba sonando
                isPlaying = controller?.isPlaying == true

                // Si ya hay algo sonando, recuperamos los títulos
                if (controller?.mediaItemCount ?: 0 > 0) {
                    controller?.mediaMetadata?.title?.let { currentTitle = it.toString() }
                    controller?.mediaMetadata?.artist?.let { currentSubtitle = it.toString() }
                } else {
                    // Si no suena nada, mostramos info de la radio
                    currentTitle = liveProgramName
                    currentSubtitle = liveProductionName
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())
    }

    // --- 1. LÓGICA RADIO EN VIVO (JSON) ---
    private fun obtenerProgramacionEnVivo() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val jsonString = URL(currentProgramUrl).readText()
                val jsonObject = JSONObject(jsonString)
                val programa = jsonObject.optString("programa", "Radio UAS")
                val produccion = jsonObject.optString("produccion", "En Vivo")

                liveProgramName = programa
                liveProductionName = produccion

                withContext(Dispatchers.Main) {
                    // Solo actualizamos si estamos en modo Radio (o vacío)
                    val urlActual = player?.currentMediaItem?.localConfiguration?.uri?.toString()
                    if (urlActual == null || urlActual == streamURL) {
                        currentTitle = liveProgramName
                        currentSubtitle = liveProductionName
                    }
                }
            } catch (e: Exception) {
                liveProgramName = "Radio UAS - 96.1 FM"
                liveProductionName = "Señal En Vivo"
            }
        }
    }

    // --- 2. LÓGICA PODCASTS (EMBY) ---
    fun cargarProgramas() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val respuesta = EmbyClient.api.getItems(
                    parentId = "5",
                    itemTypes = "MusicAlbum",
                    recursive = true,
                    sortBy = "SortName",
                    sortOrder = "Ascending"
                )
                val listaMapeada = respuesta.Items.map { item ->
                    Podcast(
                        id = item.Id,
                        titulo = item.Name,
                        descripcion = item.AlbumArtist ?: "Producción Radio UAS",
                        urlImagen = EmbyClient.getImageUrl(item.Id),
                        streamUrl = ""
                    )
                }
                withContext(Dispatchers.Main) { listaPodcasts = listaMapeada }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun abrirPrograma(podcast: Podcast) {
        programaSeleccionado = podcast
        listaEpisodios = emptyList()
        hayMasEpisodios = true
        cargarBloqueEpisodios(0)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun cargarMasEpisodios() {
        if (!cargandoMas && hayMasEpisodios) cargarBloqueEpisodios(listaEpisodios.size)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun cargarBloqueEpisodios(inicio: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                cargandoMas = true
                val limite = 20
                val respuesta = EmbyClient.api.getItems(
                    parentId = programaSeleccionado!!.id,
                    itemTypes = "Audio",
                    recursive = true,
                    limit = limite,
                    startIndex = inicio,
                    sortBy = "SortName",
                    sortOrder = "Descending"
                )
                if (respuesta.Items.size < limite) hayMasEpisodios = false

                val nuevosEpisodios = respuesta.Items.map { item ->
                    Podcast(
                        id = item.Id,
                        titulo = formatearFechaTitulo(item.Name),
                        descripcion = programaSeleccionado!!.titulo,
                        urlImagen = programaSeleccionado!!.urlImagen,
                        streamUrl = EmbyClient.getStreamUrl(item.Id)
                    )
                }
                withContext(Dispatchers.Main) {
                    listaEpisodios = listaEpisodios + nuevosEpisodios
                    cargandoMas = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                cargandoMas = false
            }
        }
    }

    // --- 3. CONTROL DE REPRODUCCIÓN ---

    fun playRadioOAlternar() {
        val p = player ?: return

        val urlActual = p.currentMediaItem?.localConfiguration?.uri?.toString()
        if (urlActual == streamURL) {
            toggleReproduccion()
        } else {
            // USAMOS LA IMAGEN LOCAL (R.drawable.logo_radio)
            reproducirAudio(
                streamURL,
                liveProgramName,
                liveProductionName,
                obtenerUriLocal(R.drawable.logo_radio_app) // <--- CAMBIO AQUÍ
            )
        }
    }

    fun toggleReproduccion() {
        val p = player ?: return

        if (p.playbackState == Player.STATE_IDLE || p.mediaItemCount == 0) {
            // USAMOS LA IMAGEN LOCAL
            reproducirAudio(
                streamURL,
                liveProgramName,
                liveProductionName,
                obtenerUriLocal(R.drawable.logo_radio_app) // <--- CAMBIO AQUÍ
            )
        } else {
            if (isPlaying) p.pause() else p.play()
        }
    }

    // Esta función convierte un recurso R.drawable.xxx en un String Uri
    private fun obtenerUriLocal(resourceId: Int): String {
        return "android.resource://${getApplication<Application>().packageName}/$resourceId"
    }

    // Método helper para pausar explícitamente (útil para la UI)
    fun pausar() {
        player?.pause()
    }

    fun reproducirAudio(url: String, titulo: String, subtitulo: String, imagenUrl: String) {
        val p = player ?: return

        this.currentTitle = titulo
        this.currentSubtitle = subtitulo
        isLoading = true

        // Construimos la metadata CON LA IMAGEN
        val metadata = MediaMetadata.Builder()
            .setTitle(titulo)
            .setArtist(subtitulo)
            .setArtworkUri(android.net.Uri.parse(imagenUrl))
            .build()
        val mimeType = if (url == streamURL) MimeTypes.AUDIO_MPEG else null
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

    fun detener() { player?.stop() }

    fun cerrarPrograma() { programaSeleccionado = null }

    override fun onCleared() {
        super.onCleared()
        // Importante: Liberamos la conexión al servicio (MediaController)
        // El servicio puede seguir sonando, pero el ViewModel se desconecta
        MediaController.releaseFuture(controllerFuture!!)
    }

    // --- UTILS ---
    @RequiresApi(Build.VERSION_CODES.O)
    private fun formatearFechaTitulo(tituloOriginal: String): String {
        try {
            val regex = Regex("""^(\d{4})(\d{2})(\d{2}).*""")
            val match = regex.find(tituloOriginal)
            if (match != null) {
                val (anio, mes, dia) = match.destructured
                val fecha = LocalDate.of(anio.toInt(), mes.toInt(), dia.toInt())
                val formateador = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale("es", "MX"))
                return fecha.format(formateador)
            }
        } catch (e: Exception) { return tituloOriginal }
        return tituloOriginal
    }
}