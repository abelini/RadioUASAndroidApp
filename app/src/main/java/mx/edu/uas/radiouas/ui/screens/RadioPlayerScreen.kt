package mx.edu.uas.radiouas.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PauseCircleFilled
import androidx.compose.material.icons.rounded.PlayCircleFilled
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.edu.uas.radiouas.R // Asegúrate de que esto importe tus recursos
import mx.edu.uas.radiouas.RadioViewModel

// Definimos los colores aquí o impórtalos de tu tema
val AzulUAS = Color(0xFF002D56)
val AzulClaro = Color(0xFF005099)
val OroUAS = Color(0xFFD4AF37) // Un dorado/amarillo para detalles

@Composable
fun RadioPlayerScreen(viewModel: RadioViewModel) {

    // --- ANIMACIÓN DE PULSO ---
    // Esto hace que el logo crezca y se encoja rítmicamente si está sonando
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (viewModel.isPlaying) 1.05f else 1f, // Solo pulsa si está sonando
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // --- FONDO DEGRADADO ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(AzulUAS, Color.Black)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // 1. INDICADOR "EN VIVO"
            LiveBadge(isLive = true)

            Spacer(modifier = Modifier.height(40.dp))

            // 2. PORTADA / LOGO ANIMADO
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(280.dp)
                    .scale(scale) // Aplicamos la animación aquí
            ) {
                // Sombra difusa detrás del logo
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .shadow(elevation = 20.dp, shape = CircleShape, spotColor = OroUAS)
                )

                // Imagen del Logo
                // NOTA: Asegúrate de tener 'logo_uas' en res/drawable, o usa R.drawable.ic_launcher_foreground
                Image(
                    painter = painterResource(id = R.drawable.logo_radio_app), // CAMBIAR POR TU LOGO
                    contentDescription = "Logo Radio UAS",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(260.dp)
                        .clip(CircleShape)
                        .background(Color.White) // Fondo blanco por si el logo es PNG transparente
                        .padding(10.dp) // Un pequeño borde blanco
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // 3. INFORMACIÓN DEL PROGRAMA
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = viewModel.currentTitle.ifEmpty { "Radio UAS" },
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = viewModel.currentSubtitle.ifEmpty { "Señal con valor" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(56.dp))

            // 4. CONTROLES DE REPRODUCCIÓN
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Botón Play/Pause Gigante
                IconButton(
                    onClick = { viewModel.playRadioOAlternar() },
                    modifier = Modifier.size(90.dp)
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(
                            color = OroUAS,
                            modifier = Modifier.size(70.dp),
                            strokeWidth = 4.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (viewModel.isPlaying) Icons.Rounded.PauseCircleFilled else Icons.Rounded.PlayCircleFilled,
                            contentDescription = "Reproducir",
                            tint = Color.White,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Footer: Frecuencia
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.SignalCellularAlt, contentDescription = null, tint = OroUAS, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "XHUAS 96.1 FM • Culiacán, Sinaloa",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// COMPONENTE AUXILIAR: Insignia "EN VIVO"
@Composable
fun LiveBadge(isLive: Boolean) {
    if (isLive) {
        // Animación de parpadeo para el punto rojo
        val infiniteTransition = rememberInfiniteTransition(label = "live_blink")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(800),
                repeatMode = RepeatMode.Reverse
            ), label = "alpha"
        )

        Surface(
            color = Color.Red.copy(alpha = 0.1f),
            shape = RoundedCornerShape(4.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.Red.copy(alpha = alpha)) // Aplicamos parpadeo
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "EN VIVO",
                    color = Color.Red,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}