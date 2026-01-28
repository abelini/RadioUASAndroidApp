package mx.edu.uas.radiouas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun MiniPlayer(radioViewModel: RadioViewModel) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding() // Respeta los gestos de Android
            .padding(8.dp)
            .height(72.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Información del Programa
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = radioViewModel.currentTitle ?: "Radio UAS",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    // Muestra "En espera" si no suena, o "En vivo" si ya le diste play
                    text = if (radioViewModel.isPlaying) "En Vivo" else "Toca para escuchar",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }

            // Botón de Acción
            IconButton(
                onClick = { radioViewModel.toggleReproduccion() },
                modifier = Modifier.size(48.dp)
            ) {
                if (radioViewModel.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Icon(
                        imageVector = if (radioViewModel.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}