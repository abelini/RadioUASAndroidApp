package mx.edu.uas.radiouas.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun AudioWaveIndicator(
    color: Color,
    modifier: Modifier = Modifier
) {
    // Configuración de la animación infinita
    val transition = rememberInfiniteTransition(label = "audioWave")

    // Fase animada que va de 0 a 2*PI (un ciclo completo) constantemente
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing), // Duración del ciclo
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    // Lienzo para dibujar las barras
    Canvas(modifier = modifier.size(width = 24.dp, height = 16.dp)) {
        val barWidth = 4.dp.toPx() // Ancho de cada barra
        val gap = 3.dp.toPx()      // Espacio entre barras
        val maxBarHeight = size.height
        val numberOfBars = 3

        repeat(numberOfBars) { index ->
            // Calculamos la altura usando seno para un movimiento suave.
            // Le damos un desfase (index * 1.2f) a cada barra para que no se muevan igual.
            // (sin(...) + 1) / 2 normaliza el valor entre 0 y 1.
            // .coerceAtLeast(0.2f) asegura que la barra nunca desaparezca por completo.
            val factor = ((sin(phase + index * 1.2f).toFloat() + 1f) / 2f).coerceAtLeast(0.2f)

            val currentBarHeight = maxBarHeight * factor

            // Posición X de la barra actual
            val x = index * (barWidth + gap)
            // Posición Y (dibujamos desde abajo hacia arriba)
            val y = maxBarHeight - currentBarHeight

            drawRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, currentBarHeight)
            )
        }
    }
}