package mx.edu.uas.radiouas

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import coil.compose.AsyncImage

@OptIn(UnstableApi::class)
@Composable
fun PodcastCard(
    podcast: Podcast,
    radioViewModel: RadioViewModel,
    onClick: () -> Unit
) {
    // --- LÓGICA DE ICONOS ---

    // 1. ¿Estamos viendo una lista de EPISODIOS o de PROGRAMAS?
    // Si 'programaSeleccionado' no es null, significa que estamos DENTRO de un álbum (viendo episodios).
    val esEpisodio = radioViewModel.programaSeleccionado != null

    // 2. Lógica de Reproducción (Solo importa si es episodio)
    val esEstePodcast = radioViewModel.currentTitle == podcast.titulo
    val estaSonando = esEstePodcast && radioViewModel.isPlaying

    // 3. Selección del Icono
    val icono = if (esEpisodio) {
        if (estaSonando) Icons.Filled.Pause else Icons.Filled.PlayArrow
    } else {
        Icons.AutoMirrored.Filled.ArrowForwardIos // Icono de "Ir a detalle"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 12.dp)
            .height(90.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            // Resaltamos el fondo solo si es un episodio sonando
            containerColor = if (esEpisodio && esEstePodcast)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- IMAGEN ---
            AsyncImage(
                model = podcast.urlImagen,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(74.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // --- TEXTOS ---
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = podcast.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (esEpisodio && esEstePodcast) AzulUAS else MaterialTheme.colorScheme.onSurface
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

            // --- BOTÓN / INDICADOR ---
            IconButton(
                onClick = onClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    // Si es navegación (flecha), usamos gris. Si es Play, usamos azul o primario.
                    tint = if (!esEpisodio) Color.Gray
                    else if (esEstePodcast) AzulUAS
                    else MaterialTheme.colorScheme.primary,

                    // La flecha se ve mejor un poco más pequeña (24dp) que el botón de Play (40dp)
                    modifier = Modifier.size(if (esEpisodio) 40.dp else 24.dp)
                )
            }
        }
    }
}