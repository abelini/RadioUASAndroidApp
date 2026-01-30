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
        // Validación básica: Si no es el día seleccionado, no puede estar en vivo
        if (_selectedDay.value != java.time.LocalDate.now().dayOfWeek.value) return false

        return try {
            // Formateador estricto para "HH:mm:ss"
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")

            val startTime = LocalTime.parse(startStr, formatter)
            val endTime = LocalTime.parse(endStr, formatter)
            val now = LocalTime.now()

            // Caso especial: Programas que cruzan la medianoche (ej: 23:00 a 00:00)
            if (endTime.isBefore(startTime) || endTime == LocalTime.MIDNIGHT) {
                return now.isAfter(startTime) || (now.isBefore(endTime) && now != endTime)
            }

            // Caso normal
            now.isAfter(startTime) && now.isBefore(endTime)
        } catch (e: Exception) {
            false
        }
    }
}