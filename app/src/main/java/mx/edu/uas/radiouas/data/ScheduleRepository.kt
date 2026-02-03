package mx.edu.uas.radiouas.data

import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import mx.edu.uas.radiouas.model.ScheduleItem
import mx.edu.uas.radiouas.utils.ScheduleUtils
import java.time.LocalTime

// Singleton para manejar los datos de la programación en toda la app
object ScheduleRepository {

    // Fuente de verdad única
    private val _fullSchedule = MutableStateFlow<List<ScheduleItem>>(emptyList())
    val fullSchedule: StateFlow<List<ScheduleItem>> = _fullSchedule.asStateFlow()

    // Reloj interno: emite un pulso cada minuto
    @RequiresApi(Build.VERSION_CODES.O)
    private val _ticker = flow {
        while (true) {
            emit(LocalTime.now())
            delay(60_000) // 60 segundos
        }
    }

    // Lógica reactiva: Recalcula el programa en vivo si cambia la lista O si pasa el tiempo
    @RequiresApi(Build.VERSION_CODES.O)
    val currentLiveProgram: Flow<ScheduleItem?> = _fullSchedule.combine(_ticker) { schedule, _ ->
        schedule.find { item ->
            ScheduleUtils.isLiveNow(item.startTime, item.endTime)
        }
    }

    suspend fun updateSchedule(newList: List<ScheduleItem>) {
        _fullSchedule.emit(newList)
    }
}