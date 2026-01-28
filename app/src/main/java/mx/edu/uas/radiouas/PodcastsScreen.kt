package mx.edu.uas.radiouas

import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.items
// Importante: Estos colores deben estar definidos en tu archivo de colores o cámbialos por:
// val AzulUAS = androidx.compose.ui.graphics.Color(0xFF003366)

@OptIn(UnstableApi::class)
@Composable
fun PodcastsScreen(radioViewModel: RadioViewModel) {
    var items by remember { mutableStateOf<List<EmbyItem>>(emptyList()) }
    var currentFolderId by remember { mutableStateOf<String?>("5") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) } // Para ver el error en pantalla

    LaunchedEffect(currentFolderId) {
        isLoading = true
        errorMessage = null
        try {
            val itemType = if (currentFolderId == "5") "MusicAlbum" else "Audio"
            // Forzamos Recursive = true para la primera carga si viene vacío
            val response = EmbyClient.api.getItems(
                parentId = currentFolderId,
                itemTypes = itemType,
                recursive = (currentFolderId == "5")
            )

            if (response.Items.isEmpty()) {
                errorMessage = "No se encontraron elementos en esta categoría."
            } else {
                items = response.Items
            }
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
            // Esto nos dirá si es SSL, Timeout o qué
            errorMessage = "Error: ${e.localizedMessage ?: "Conexión fallida"}"
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Programas Radio UAS", style = MaterialTheme.typography.headlineSmall, color = AzulUAS)

        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        // Si hay error, lo mostramos con letras rojas
        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            Button(onClick = { currentFolderId = "5" }) { Text("Reintentar") }
        }

        LazyColumn {
            items(radioViewModel.listaPodcasts) { item ->
                PodcastCard(
                    podcast = item,
                    radioViewModel = radioViewModel,
                    onClick = {
                        // Lógica: Si ya es este y suena, pausa. Si no, reproduce.
                        if (radioViewModel.currentTitle == item.titulo && radioViewModel.isPlaying) {
                            radioViewModel.player.pause()
                        } else {
                            radioViewModel.reproducirAudio(item.streamUrl, item.titulo)
                        }
                    }
                )
            }
        }
    }
}