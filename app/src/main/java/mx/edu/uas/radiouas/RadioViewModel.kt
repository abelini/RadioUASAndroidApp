package mx.edu.uas.radiouas

import android.app.Application
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
import java.net.URL

@UnstableApi
class RadioViewModel(application: Application) : AndroidViewModel(application) {

    var currentProgram = "https://spc.radiouas.org/api/schedule/now"
    var streamURL = "https://stream9.mexiserver.com/8410/stream"
    private var nombreProgramaEnVivo: String = "Radio UAS - 96.1 FM"

    var isPlaying by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    var currentTitle by mutableStateOf("Cargando programación...")

    var listaPodcasts by mutableStateOf<List<Podcast>>(emptyList())

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
        }) // <--- Aquí cerramos el Listener correctamente

        // CORRECCIÓN: Llamamos a la función FUERA del listener, pero dentro del init
        obtenerProgramacionEnVivo()
    }

    private fun obtenerProgramacionEnVivo() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val nombre = URL(currentProgram).readText()

                // Guardamos el nombre "en la memoria" del ViewModel
                if (nombre.isNotBlank()) {
                    nombreProgramaEnVivo = nombre
                } else {
                    nombreProgramaEnVivo = "Radio UAS - 96.1 FM"
                }

                withContext(Dispatchers.Main) {
                    // Solo actualizamos el título visible si NO estamos escuchando un podcast
                    // (O sea, si estamos en la radio o no hay nada)
                    val urlActual = player.currentMediaItem?.localConfiguration?.uri?.toString()
                    if (urlActual == null || urlActual == streamURL) {
                        currentTitle = nombreProgramaEnVivo
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    currentTitle = "Radio UAS - 96.1 FM"
                }
            }
        }
    }

    fun cargarProgramas() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Llamamos a la API (Retrofit)
                // Nota: Ajusta "parentId" o "itemTypes" según tu estructura en Emby.
                // Usualmente para programas/albums usamos IncludeItemTypes="MusicAlbum"
                val respuesta = EmbyClient.api.getItems(
                    parentId = "5", // ID de tu carpeta de Podcasts en Emby
                    itemTypes = "MusicAlbum",
                    recursive = true
                )

                // 2. CONVERSIÓN (Mapping)
                // Transformamos cada EmbyItem en un Podcast bonito
                val listaMapeada = respuesta.Items.map { item ->
                    Podcast(
                        id = item.Id,
                        titulo = item.Name,
                        descripcion = "Programa de Radio UAS", // EmbyItem a veces no trae descripción simple

                        // USAMOS LAS FUNCIONES DEL CLIENTE PARA GENERAR LAS URLS
                        urlImagen = EmbyClient.getImageUrl(item.Id),
                        streamUrl = EmbyClient.getStreamUrl(item.Id) // Ojo: Esto es si es un audio directo. Si es un Album, la lógica cambia un poco (ver nota abajo)
                    )
                }

                // 3. Actualizamos la UI en el hilo principal
                withContext(Dispatchers.Main) {
                    listaPodcasts = listaMapeada
                }

            } catch (e: Exception) {
                e.printStackTrace()
                // Aquí podrías poner un estado de error
            }
        }
    }
    fun playRadioOAlternar() {
        val urlActual = player.currentMediaItem?.localConfiguration?.uri?.toString()

        // Verificamos: ¿Lo que suena ES la URL de la radio?
        if (urlActual == streamURL) {
            // SÍ ES RADIO: Funcionamos como botón normal (Pausa/Play)
            toggleReproduccion()
        } else {
            // NO ES RADIO (Es Podcast o nada): CORTAMOS Y PONEMOS RADIO

            // Recuperamos el título del programa en vivo
            currentTitle = nombreProgramaEnVivo

            // Forzamos la reproducción de la radio
            reproducirAudio(streamURL, currentTitle)
        }
    }
    fun toggleReproduccion() {
        if (player.playbackState == Player.STATE_IDLE || player.mediaItemCount == 0) {
            // Si está vacío, cargamos la radio con el título actual de la API
            reproducirAudio(streamURL, currentTitle)
        } else {
            // Si ya tiene algo, pausamos o reanudamos
            if (isPlaying) player.pause() else player.play()
        }
    }

    // Esta función sirve tanto para RADIO como para PODCASTS
    fun reproducirAudio(url: String, titulo: String) {
        // 1. Actualizamos la variable visual inmediatamente
        this.currentTitle = titulo

        isLoading = true

        // 2. Creamos los metadatos usando el título recibido
        val metadata = MediaMetadata.Builder()
            .setTitle(titulo)
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setMediaMetadata(metadata)
            .build()

        player.stop()
        player.clearMediaItems() // Importante limpiar lo anterior
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    fun detener() {
        player.stop()
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}