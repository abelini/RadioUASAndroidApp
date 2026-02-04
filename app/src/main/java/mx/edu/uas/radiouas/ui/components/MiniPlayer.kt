package mx.edu.uas.radiouas.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
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
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import mx.edu.uas.radiouas.ui.viewmodel.RadioViewModel

@Composable
fun MiniPlayer(
    viewModel: RadioViewModel,
    onPlayerClick: () -> Unit
) {
    // Obtenemos estados del ViewModel
    val isPlaying = viewModel.isPlaying
    val currentTitle = viewModel.currentTitle.ifEmpty { "Radio UAS" }
    val currentSubtitle = viewModel.currentSubtitle
    val coverUrl = viewModel.currentCoverUrl
    val isLive = viewModel.isLive

    // Progreso real del audio
    val realProgress = viewModel.currentProgress

    // ESTADO LOCAL: Controla el Slider mientras el usuario lo arrastra
    var sliderPosition by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    // Sincronización: Si NO estamos arrastrando, actualizamos con el progreso real
    LaunchedEffect(realProgress) {
        if (!isDragging) {
            sliderPosition = realProgress
        }
    }

    // Solo mostramos el MiniPlayer si hay algo cargado (o es la radio por defecto)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlayerClick() },
        shadowElevation = 8.dp,
        color = Color(0xFFF0F0F0) // Gris muy claro para diferenciar del fondo blanco
    ) {
        Column {
            // --- BARRA DE PROGRESO INTERACTIVA ---
            if (isLive) {
                // EN VIVO: Barra indeterminada (carga infinita) o estática completa
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = Color(0xFFD4AF37), // Dorado/Amarillo UAS
                    trackColor = Color.LightGray
                )
            } else {
                // PODCAST: Slider interactivo
                Slider(
                    value = sliderPosition,
                    onValueChange = { newPos ->
                        isDragging = true
                        sliderPosition = newPos
                    },
                    onValueChangeFinished = {
                        viewModel.seekTo(sliderPosition)
                        isDragging = false
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF003366), // Azul UAS
                        activeTrackColor = Color(0xFF003366),
                        inactiveTrackColor = Color.LightGray
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp) // Altura reducida para que no ocupe mucho espacio
                        .offset(y = (-8).dp) // Subimos un poco el slider para pegarlo al borde
                )
            }

            // --- CONTENIDO DEL PLAYER ---
            Row(
                modifier = Modifier
                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp, top = if(isLive) 12.dp else 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Imagen (Carátula)
                AsyncImage(
                    model = coverUrl,
                    contentDescription = "Carátula",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Gray)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // 2. Textos (Título y Subtítulo)
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = currentTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.Black
                    )
                    Text(
                        text = if(isLive) "EN VIVO • $currentSubtitle" else currentSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.DarkGray
                    )
                }

                // 3. Botón Play/Pause
                IconButton(
                    onClick = { viewModel.toggleReproduccion() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                        tint = Color(0xFF003366),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}