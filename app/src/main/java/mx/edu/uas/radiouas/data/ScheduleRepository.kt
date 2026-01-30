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

// Usamos 'object' para que sea un Singleton (una única instancia para toda la app)
object ScheduleRepository {

    // 1. Aquí guardamos la lista completa cuando baja del servidor
    private val _fullSchedule = MutableStateFlow<List<ScheduleItem>>(emptyList())
    val fullSchedule: StateFlow<List<ScheduleItem>> = _fullSchedule.asStateFlow()

    // 2. Un reloj interno que emite un "tic" cada minuto
    @RequiresApi(Build.VERSION_CODES.O)
    private val _ticker = flow {
        while (true) {
            emit(LocalTime.now())
            delay(60_000) // Espera 1 minuto
        }
    }

    // 3. MAGIA: Combinamos la Lista + El Reloj
    // Cada vez que cambie la lista O pase un minuto, esto se recalcula solo.
    @RequiresApi(Build.VERSION_CODES.O)
    val currentLiveProgram: Flow<ScheduleItem?> = _fullSchedule.combine(_ticker) { schedule, _ ->
        schedule.find { item ->
            ScheduleUtils.isLiveNow(item.startTime, item.endTime)
        }
    }

    // Función para cargar los datos (llámala donde hacías el fetch antes)
    suspend fun updateSchedule(newList: List<ScheduleItem>) {
        _fullSchedule.emit(newList)
    }
}