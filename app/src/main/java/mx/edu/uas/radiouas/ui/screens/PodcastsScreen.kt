package mx.edu.uas.radiouas.ui.screens

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import mx.edu.uas.radiouas.ui.viewmodel.RadioViewModel
import mx.edu.uas.radiouas.model.EmbyItem
import mx.edu.uas.radiouas.network.EmbyClient
import mx.edu.uas.radiouas.ui.components.AudioWaveIndicator
import mx.edu.uas.radiouas.utils.formatearFechaTitulo

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PodcastsScreen(viewModel: RadioViewModel) {

    // ESTADOS
    var albums by remember { mutableStateOf<List<EmbyItem>>(emptyList()) }
    var episodes by remember { mutableStateOf<List<EmbyItem>>(emptyList()) }
    var selectedAlbum by remember { mutableStateOf<EmbyItem?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // CARGA INICIAL (ÁLBUMES)
    LaunchedEffect(Unit) {
        try {
            val response = EmbyClient.api.getPodcasts()
            albums = response.items
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
        }
    }

    // CARGA DE EPISODIOS
    LaunchedEffect(selectedAlbum) {
        if (selectedAlbum != null) {
            isLoading = true
            try {
                val response = EmbyClient.api.getEpisodes(albumId = selectedAlbum!!.id)
                // REVERTIDO: Tomamos la lista tal cual viene del API (tú ya la ordenaste allá)
                episodes = response.items
            } catch (e: Exception) {
                // Error silencioso
            } finally {
                isLoading = false
            }
        }
    }

    // --- NAVEGACIÓN HACIA ATRÁS ---
    BackHandler(enabled = selectedAlbum != null) {
        selectedAlbum = null
        episodes = emptyList()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading && albums.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            if (selectedAlbum == null) {
                // VISTA 1: GRID DE ÁLBUMES
                AlbumsGrid(albums = albums) { album ->
                    selectedAlbum = album
                }
            } else {
                // VISTA 2: LISTA DE EPISODIOS
                EpisodesList(
                    album = selectedAlbum!!,
                    episodes = episodes,
                    // CAMBIO: Pasamos ID en vez de Título
                    currentPlayingId = viewModel.currentMediaId,
                    isPlaying = viewModel.isPlaying,
                    onBack = { selectedAlbum = null },
                    onTrackClick = { track ->
                        // CAMBIO: Lógica basada en IDs (más robusta)
                        if (viewModel.currentMediaId == track.id) {
                            viewModel.toggleReproduccion()
                        } else {
                            val coverUrl = EmbyClient.getImageUrl(selectedAlbum!!.id)
                            viewModel.playPodcast(
                                episodio = track,
                                imagenAlbumUrl = coverUrl,
                                nombreAlbum = selectedAlbum!!.name
                            )
                        }
                    }
                )
            }
        }
    }
}

// --- COMPONENTES UI ---

@Composable
fun AlbumsGrid(albums: List<EmbyItem>, onAlbumClick: (EmbyItem) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Podcasts",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF002D56),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (albums.isEmpty()) {
            Text("Cargando podcasts...", color = Color.Gray)
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 140.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(albums) { album ->
                AlbumCard(album, onAlbumClick)
            }
        }
    }
}

@Composable
fun AlbumCard(album: EmbyItem, onClick: (EmbyItem) -> Unit) {
    Column(
        modifier = Modifier
            .width(150.dp)
            .clickable { onClick(album) }
    ) {
        AsyncImage(
            model = EmbyClient.getImageUrl(album.id),
            contentDescription = album.name,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = album.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EpisodesList(
    album: EmbyItem,
    episodes: List<EmbyItem>,
    currentPlayingId: String?, // Recibimos el ID
    isPlaying: Boolean,
    onBack: () -> Unit,
    onTrackClick: (EmbyItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Cabecera
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            AsyncImage(
                model = EmbyClient.getImageUrl(album.id),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha=0.4f)))

            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(top = 40.dp, start = 16.dp)
                    .align(Alignment.TopStart)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
            }

            Text(
                text = album.name,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            )
        }

        // Lista de Episodios
        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            items(episodes) { episode ->

                // CAMBIO: Comparamos ID con ID
                val esElQueSuena = (episode.id == currentPlayingId)
                val iconoEsPause = (esElQueSuena && isPlaying)

                EpisodeItem(
                    episode = episode,
                    mostrarIconoPause = iconoEsPause,
                    esElActivo = esElQueSuena,
                    onClick = { onTrackClick(episode) }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EpisodeItem(
    episode: EmbyItem,
    mostrarIconoPause: Boolean,
    esElActivo: Boolean,
    onClick: () -> Unit
) {
    val colorIcono = if (esElActivo) Color(0xFF002D56) else Color.Gray
    val colorTexto = if (esElActivo) Color(0xFF002D56) else Color.Black
    val pesoTexto = if (esElActivo) FontWeight.Bold else FontWeight.Normal

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(if (esElActivo) 6.dp else 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = if (mostrarIconoPause) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (mostrarIconoPause) "Pausar" else "Reproducir",
                tint = colorIcono,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = formatearFechaTitulo(episode.name),
                style = MaterialTheme.typography.bodyLarge,
                color = colorTexto,
                fontWeight = pesoTexto,
                modifier = Modifier.weight(1f)
            )

            if (mostrarIconoPause) {
                Spacer(modifier = Modifier.width(8.dp))
                AudioWaveIndicator(color = colorIcono)
            }
        }
    }
}