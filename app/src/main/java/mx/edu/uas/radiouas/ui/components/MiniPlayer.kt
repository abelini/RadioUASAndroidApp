package mx.edu.uas.radiouas.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
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
import coil.compose.AsyncImage
import mx.edu.uas.radiouas.ui.viewmodel.RadioViewModel

@Composable
fun MiniPlayer(
    viewModel: RadioViewModel,
    onClick: () -> Unit // Para expandir el player
) {
    // Solo mostramos el MiniPlayer si hay algo cargado (título no vacío o player listo)
    if (viewModel.currentTitle.isEmpty()) return

    // Diseño principal: Tarjeta elevada
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp) // Un poco más alto para que quepa la imagen bien
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // 1. CONTENIDO PRINCIPAL (Imagen + Textos + Botón)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 16.dp), // Espacio para el botón play
                verticalAlignment = Alignment.CenterVertically
            ) {
                // --- IMAGEN DE PORTADA / LOGO ---
                AsyncImage(
                    model = viewModel.currentCoverUrl, // Viene del ViewModel
                    contentDescription = "Portada",
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                        .background(Color.LightGray), // Fondo mientras carga
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(8.dp))

                // --- TEXTOS ---
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = viewModel.currentTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.Black
                    )
                    Text(
                        text = if (viewModel.isLive) "EN VIVO" else viewModel.currentSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        // Si es en vivo, ponemos el texto rojo, si no, gris
                        color = if (viewModel.isLive) Color.Red else Color.Gray
                    )
                }

                // --- BOTÓN PLAY/PAUSE ---
                IconButton(onClick = { viewModel.toggleReproduccion() }) {
                    Icon(
                        imageVector = if (viewModel.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Reproducir",
                        tint = Color(0xFF002D56),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // 2. BARRA DE PROGRESO (Solo si es Podcast)
            if (!viewModel.isLive) {
                LinearProgressIndicator(
                    progress = { viewModel.currentProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.BottomCenter), // Pegada al fondo
                    color = Color(0xFF002D56), // Azul UAS
                    trackColor = Color.LightGray.copy(alpha = 0.5f),
                )
            }
        }
    }
}