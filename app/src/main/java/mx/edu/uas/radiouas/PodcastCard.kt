package mx.edu.uas.radiouas

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi

import androidx.compose.material.icons.filled.PlayArrow // Cambiamos PlayCircle por PlayArrow (más seguro)
import androidx.compose.material.icons.filled.Pause     // Cambiamos PauseCircle por Pause
import androidx.compose.material.icons.filled.MusicNote
import coil.compose.AsyncImage

// --- COMPONENTE VISUAL ---
@OptIn(UnstableApi::class)
@Composable
fun PodcastCard(
    podcast: Podcast,
    radioViewModel: RadioViewModel,
    onClick: () -> Unit
) {
    // LÓGICA VISUAL:
    // Comparamos si el título actual del ViewModel coincide con este podcast
    val esEstePodcast = radioViewModel.currentTitle == podcast.titulo
    // Verificamos si realmente está sonando (para poner Pause o Play)
    val estaSonando = esEstePodcast && radioViewModel.isPlaying

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 12.dp)
            .height(90.dp) // Altura fija para uniformidad
            .clickable { onClick() }, // Click en toda la tarjeta
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            // Si está sonando, le damos un fondo sutilmente diferente (opcional)
            containerColor = if (esEstePodcast) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. IMAGEN DE PORTADA (Con Coil)
            AsyncImage(
                model = podcast.urlImagen,
                contentDescription = "Portada del Podcast",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(74.dp) // Cuadrado de la imagen
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray) // Color mientras carga
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 2. INFORMACIÓN (Título y Descripción)
            Column(
                modifier = Modifier.weight(1f), // Ocupa el espacio disponible
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = podcast.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    // Si suena, pintamos el texto del color primario (AzulUAS)
                    color = if (esEstePodcast) AzulUAS else MaterialTheme.colorScheme.onSurface
                )

                if (podcast.descripcion.isNotEmpty()) {
                    Text(
                        text = podcast.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 3. BOTÓN DE ACCIÓN (Play/Pause)
            IconButton(
                onClick = onClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (estaSonando) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (estaSonando) "Pausar" else "Reproducir",
                    tint = if (esEstePodcast) AzulUAS else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}