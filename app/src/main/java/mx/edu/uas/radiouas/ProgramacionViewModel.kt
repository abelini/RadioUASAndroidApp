package mx.edu.uas.radiouas.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.edu.uas.radiouas.model.ScheduleItem
import mx.edu.uas.radiouas.network.RetrofitInstance // O como llames a tu instancia
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class ProgramacionViewModel : ViewModel() {

    private val _schedule = MutableStateFlow<List<ScheduleItem>>(emptyList())
    val schedule: StateFlow<List<ScheduleItem>> = _schedule

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 1 = Lunes, ... 7 = Domingo (Ajuste para Java Time que usa 1=Lunes)
    private val _selectedDay = MutableStateFlow(LocalDate.now().dayOfWeek.value)
    val selectedDay: StateFlow<Int> = _selectedDay

    init {
        loadScheduleForDay(_selectedDay.value)
    }

    fun onDaySelected(day: Int) {
        _selectedDay.value = day
        loadScheduleForDay(day)
    }

    private fun loadScheduleForDay(day: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Llamada a la API
                val response = RetrofitInstance.api.getDailySchedule(day)
                _schedule.value = response
            } catch (e: Exception) {
                e.printStackTrace()
                _schedule.value = emptyList() // Manejo básico de error
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Lógica para saber si un programa es "Ahora"
    @RequiresApi(Build.VERSION_CODES.O)
    fun isLiveNow(startStr: String, endStr: String): Boolean {
        // Solo calculamos "En vivo" si estamos viendo el día de HOY
        if (_selectedDay.value != LocalDate.now().dayOfWeek.value) return false

        return try {
            // Formateador para "6:45 AM" (Inglés por AM/PM)
            val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)

            val startTime = LocalTime.parse(startStr.uppercase(), formatter)
            var endTime = LocalTime.parse(endStr.uppercase(), formatter)
            val now = LocalTime.now()

            // Caso especial: Si termina a media noche o pasa de día (ej: 11 PM a 1 AM)
            // Para simplicidad de esta versión, asumimos horarios del mismo día
            if (endTime.isBefore(startTime)) {
                // Si termina antes de empezar, es que termina al día siguiente (madrugada)
                // Ajuste lógico simple:
                return now.isAfter(startTime) || now.isBefore(endTime)
            }

            now.isAfter(startTime) && now.isBefore(endTime)
        } catch (e: Exception) {
            false
        }
    }
}