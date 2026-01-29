package mx.edu.uas.radiouas.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.edu.uas.radiouas.model.ScheduleItem
import mx.edu.uas.radiouas.ui.viewmodel.ProgramacionViewModel
import androidx.compose.foundation.lazy.rememberLazyListState

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProgramacionScreen(
    viewModel: ProgramacionViewModel = viewModel()
) {
    val schedule by viewModel.schedule.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(schedule) {
        if (schedule.isNotEmpty()) {
            // Busamos el índice del primer elemento que esté "En Vivo"
            val liveIndex = schedule.indexOfFirst { item ->
                viewModel.isLiveNow(item.starts, item.ends)
            }

            // Si encontramos uno, hacemos scroll suave hasta él
            if (liveIndex != -1) {
                listState.animateScrollToItem(liveIndex)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {

        DaySelector(selectedDay) { newDay ->
            viewModel.onDaySelected(newDay)
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF003366))
            }
        } else {
            LazyColumn(
                state = listState, // <--- 3. CONECTAMOS EL ESTADO AQUÍ
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(schedule) { item ->
                    val isLive = viewModel.isLiveNow(item.starts, item.ends)
                    ProgramItem(item, isLive)
                }
            }
        }
    }
}



@Composable
fun DaySelector(selectedDay: Int, onDaySelected: (Int) -> Unit) {
    val days = listOf("LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB", "DOM")

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items(days.size) { index ->
            val dayNum = index + 1
            val isSelected = dayNum == selectedDay

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onDaySelected(dayNum) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = days[index],
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color(0xFF003366) else Color.Gray
                )
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD4AF37)) // Oro UAS
                    )
                }
            }
        }
    }
}

@Composable
fun ProgramItem(item: ScheduleItem, isLive: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Columna de HORA
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.width(60.dp).padding(top = 4.dp)
        ) {
            Text(
                text = item.starts,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (isLive) Color(0xFF003366) else Color.Gray
            )
            Text(
                text = item.ends,
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray,
                fontSize = 10.sp
            )
        }

        // Línea divisoria visual
        Box(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .width(2.dp)
                .height(80.dp) // Altura fija o dinámica
                .background(if (isLive) Color(0xFFD4AF37) else Color.LightGray.copy(alpha = 0.5f))
        )

        // Tarjeta de Contenido
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isLive) Color(0xFFE8F0FE) else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isLive) 4.dp else 1.dp),
            modifier = Modifier.fillMaxWidth().height(80.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icono (Basado en tu HTML o lógica)
                // Aquí hacemos un truco: Si el HTML dice 'music', ponemos nota, etc.
                val iconVector = getIconForHtml(item.iconHtml)

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isLive) Color(0xFF003366) else Color(0xFFF0F0F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = if (isLive) Color.White else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    if (isLive) {
                        Text(
                            text = "EN VIVO",
                            color = Color(0xFFD4AF37), // Oro
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = item.produccion,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// Función auxiliar simple para adivinar el icono
fun getIconForHtml(html: String): ImageVector {
    return when {
        html.contains("music") -> Icons.Default.MusicNote
        html.contains("school") -> Icons.Default.School
        html.contains("user") -> Icons.Default.Mic // Entrevistas/Hablado
        else -> Icons.Default.MusicNote
    }
}