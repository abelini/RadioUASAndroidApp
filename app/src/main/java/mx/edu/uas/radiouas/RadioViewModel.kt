package mx.edu.uas.radiouas

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject // <--- IMPORTANTE: Para leer el JSON
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@UnstableApi
class RadioViewModel(application: Application) : AndroidViewModel(application) {

    // --- VARIABLES DE LA RADIO ---
    var currentProgramUrl = "https://spc.radiouas.org/api/schedule/current_program"
    var streamURL = "https://stream9.mexiserver.com/8410/stream"

    // Almacenamos los datos "En Vivo" aquí para recuperarlos cuando soltamos un podcast
    private var liveProgramName: String = "Radio UAS - 96.1 FM"
    private var liveProductionName: String = "Señal En Vivo"

    // --- ESTADOS DE UI ---
    var isPlaying by mutableStateOf(false)
    var isLoading by mutableStateOf(false)

    // LO QUE SE MUESTRA EN PANTALLA:
    var currentTitle by mutableStateOf("Cargando programación...")
    var currentSubtitle by mutableStateOf("Espere un momento...") // <--- NUEVO

    // --- VARIABLES DE PODCASTS ---
    var listaPodcasts by mutableStateOf<List<Podcast>>(emptyList())
    var programaSeleccionado by mutableStateOf<Podcast?>(null)
    var listaEpisodios by mutableStateOf<List<Podcast>>(emptyList())
    var hayMasEpisodios by mutableStateOf(true)
    var cargandoMas by mutableStateOf(false)

    // --- CONFIGURACIÓN PLAYER ---
    val player: ExoPlayer = ExoPlayer.Builder(application)
        .setMediaSourceFactory(
            DefaultMediaSourceFactory(application)
                .setDataSourceFactory(
                    DefaultHttpDataSource.Factory()
                        .setUserAgent("RadioUAS_Android")
                        .setAllowCrossProtocolRedirects(true)
                )
        )
        .build()

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
            override fun onPlaybackStateChanged(state: Int) {
                isLoading = (state == Player.STATE_BUFFERING)
            }
        })

        obtenerProgramacionEnVivo()
        cargarProgramas()
    }

    // --- 1. LÓGICA RADIO EN VIVO (JSON) ---
    private fun obtenerProgramacionEnVivo() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Leemos el texto de la URL
                val jsonString = URL(currentProgramUrl).readText()

                // Parseamos el JSON
                val jsonObject = JSONObject(jsonString)
                val programa = jsonObject.optString("programa", "Radio UAS")
                val produccion = jsonObject.optString("produccion", "En Vivo")

                // Guardamos en variables internas
                liveProgramName = programa
                liveProductionName = produccion

                withContext(Dispatchers.Main) {
                    // Si estamos escuchando la radio (o nada), actualizamos la pantalla
                    val urlActual = player.currentMediaItem?.localConfiguration?.uri?.toString()
                    if (urlActual == null || urlActual == streamURL) {
                        currentTitle = liveProgramName
                        currentSubtitle = liveProductionName
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback en caso de error de red
                liveProgramName = "Radio UAS - 96.1 FM"
                liveProductionName = "Señal En Vivo"
                withContext(Dispatchers.Main) {
                    if (currentTitle == "Cargando programación...") {
                        currentTitle = liveProgramName
                        currentSubtitle = liveProductionName
                    }
                }
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
                    sortOrder = "Ascending" // A-Z
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
                        // Aquí guardamos el nombre del Álbum para usarlo de subtítulo luego
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

    // --- 3. CONTROL DE REPRODUCCIÓN UNIFICADO ---

    fun playRadioOAlternar() {
        val urlActual = player.currentMediaItem?.localConfiguration?.uri?.toString()
        if (urlActual == streamURL) {
            // Ya es radio, solo Play/Pause
            toggleReproduccion()
        } else {
            // Era podcast, cambiamos a Radio y recuperamos los textos del JSON
            reproducirAudio(streamURL, liveProgramName, liveProductionName)
        }
    }

    fun toggleReproduccion() {
        if (player.playbackState == Player.STATE_IDLE || player.mediaItemCount == 0) {
            // Si está vacío, cargar Radio
            reproducirAudio(streamURL, liveProgramName, liveProductionName)
        } else {
            if (isPlaying) player.pause() else player.play()
        }
    }

    // FUNCIÓN MAESTRA: Acepta URL, Título y Subtítulo
    fun reproducirAudio(url: String, titulo: String, subtitulo: String) {
        this.currentTitle = titulo
        this.currentSubtitle = subtitulo // Actualizamos UI

        isLoading = true

        val metadata = MediaMetadata.Builder()
            .setTitle(titulo)
            .setArtist(subtitulo) // Guardamos el subtítulo en metadata también
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setMediaMetadata(metadata)
            .build()

        player.stop()
        player.clearMediaItems()
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    fun detener() { player.stop() }

    fun cerrarPrograma() { programaSeleccionado = null }

    override fun onCleared() {
        super.onCleared()
        player.release()
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