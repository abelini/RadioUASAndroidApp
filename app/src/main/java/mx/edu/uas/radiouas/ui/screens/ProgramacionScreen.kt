package mx.edu.uas.radiouas.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.edu.uas.radiouas.getCategoryStyle
import mx.edu.uas.radiouas.model.ScheduleItem
import mx.edu.uas.radiouas.ui.viewmodel.ProgramacionViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProgramacionScreen(
    viewModel: ProgramacionViewModel = viewModel(),
    onPlayRadio: () -> Unit = {}
) {
    // 1. Estados del ViewModel
    val schedule by viewModel.schedule.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()

    // 2. Estado local para el scroll y el diálogo
    val listState = rememberLazyListState()
    var selectedProgram by remember { mutableStateOf<ScheduleItem?>(null) }

    // 3. Scroll automático al programa en vivo
    LaunchedEffect(schedule) {
        if (schedule.isNotEmpty()) {
            val liveIndex = schedule.indexOfFirst { item ->
                viewModel.isLiveNow(item.startTime, item.endTime)
            }
            if (liveIndex != -1) {
                listState.scrollToItem(liveIndex)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // --- Selector de Días ---
        DaySelector(selectedDay = selectedDay, onDaySelected = { viewModel.onDaySelected(it) })

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF003366))
            }
        } else {
            // --- Lista de Programas ---
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(schedule) { item ->
                    // Verificamos si es HOY y es AHORA
                    val isLive = viewModel.isLiveNow(item.startTime, item.endTime)

                    ProgramItem(
                        item = item,
                        isLive = isLive,
                        onClick = {
                            if (isLive) {
                                // CASO 1: Está en VIVO -> Encender Radio
                                onPlayRadio()
                            } else {
                                // CASO 2: Es FUTURO -> Agendar Recordatorio
                                selectedProgram = item
                            }
                        }
                    )
                }
            }
        }
    }

    // --- Diálogo de Recordatorio ---
    selectedProgram?.let { program ->
        ReminderDialog(
            program = program,
            onDismiss = { selectedProgram = null },
            onConfirm = {
                // Aquí iría tu lógica de AlarmManager
                println("Recordatorio agendado para: ${program.name}")
                selectedProgram = null
            }
        )
    }
}

// -----------------------------------------------------------------------------
// COMPONENTES UI
// -----------------------------------------------------------------------------

@Composable
fun DaySelector(selectedDay: Int, onDaySelected: (Int) -> Unit) {
    val days = listOf(
        "LUN" to 1, "MAR" to 2, "MIÉ" to 3, "JUE" to 4,
        "VIE" to 5, "SÁB" to 6, "DOM" to 7
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items(days) { (label, dayValue) ->
            val isSelected = selectedDay == dayValue
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onDaySelected(dayValue) }
                    .background(if (isSelected) Color(0xFF003366) else Color.Transparent)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = label,
                    color = if (isSelected) Color.White else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProgramItem(
    item: ScheduleItem,
    isLive: Boolean,
    onClick: () -> Unit
) {
    val displayStart = formatTimeForUser(item.startTime)
    val displayEnd = formatTimeForUser(item.endTime)
    val style = getCategoryStyle(item.slug)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // 1. Columna de Hora
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .width(65.dp)
                .padding(top = 4.dp)
        ) {
            Text(
                text = displayStart,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (isLive) Color(0xFF003366) else Color.Gray
            )
            Text(
                text = displayEnd,
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray,
                fontSize = 10.sp
            )
        }

        // 2. Línea divisoria
        Box(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .width(2.dp)
                .height(80.dp)
                .background(if (isLive) Color(0xFFD4AF37) else Color.LightGray.copy(alpha = 0.5f))
        )

        // 3. Tarjeta del Programa
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isLive) Color(0xFFE8F0FE) else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isLive) 4.dp else 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp), // Padding ajustado
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icono
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isLive) Color(0xFF003366) else style.color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = style.icon,
                        contentDescription = null,
                        tint = if (isLive) Color.White else style.color,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Textos
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isLive) {
                        Text(
                            text = "EN VIVO",
                            color = Color(0xFFD4AF37),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 10.sp,
                            modifier = Modifier.padding(bottom = 2.dp) // Más pegado al título
                        )
                    }
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReminderDialog(
    program: ScheduleItem,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.Notifications, contentDescription = null, tint = Color(0xFF003366)) },
        title = { Text(text = "Crear Recordatorio") },
        text = {
            Column {
                Text("¿Quieres que te avisemos cuando empiece este programa?")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = program.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Inicia a las ${formatTimeForUser(program.startTime)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003366))
            ) {
                Text("Sí, avísame")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Gray)
            }
        },
        containerColor = Color.White
    )
}

// -----------------------------------------------------------------------------
// UTILIDADES (Helpers)
// -----------------------------------------------------------------------------

@RequiresApi(Build.VERSION_CODES.O)
fun formatTimeForUser(timeStr: String): String {
    return try {
        // Parsea "14:00:00" -> LocalTime -> "02:00 PM"
        val inputFormat = DateTimeFormatter.ofPattern("HH:mm:ss")
        val outputFormat = DateTimeFormatter.ofPattern("hh:mm a")
        val time = LocalTime.parse(timeStr, inputFormat)
        time.format(outputFormat).uppercase()
    } catch (e: Exception) {
        timeStr // Si falla, devuelve el original
    }
}