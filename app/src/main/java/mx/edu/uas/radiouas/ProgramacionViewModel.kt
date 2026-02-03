package mx.edu.uas.radiouas.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mx.edu.uas.radiouas.data.ScheduleRepository
import mx.edu.uas.radiouas.model.ScheduleItem
import mx.edu.uas.radiouas.network.RetrofitInstance
import mx.edu.uas.radiouas.utils.ScheduleUtils
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
class ProgramacionViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Control del día seleccionado (1 = Lunes, 7 = Domingo)
    private val _selectedDay = MutableStateFlow(LocalDate.now().dayOfWeek.value)
    val selectedDay: StateFlow<Int> = _selectedDay

    // La lista de programas viene directo del Repositorio (Single Source of Truth)
    val schedule: StateFlow<List<ScheduleItem>> = ScheduleRepository.fullSchedule
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Programa actual en vivo (se actualiza reactivamente)
    val currentLiveProgram = ScheduleRepository.currentLiveProgram
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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
                val response = RetrofitInstance.api.getDailySchedule(day)
                ScheduleRepository.updateSchedule(response)
            } catch (e: Exception) {
                // En caso de error, limpiamos o mantenemos estado anterior según prefieras.
                // Aquí enviamos lista vacía para evitar datos corruptos.
                ScheduleRepository.updateSchedule(emptyList())
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun isLiveNow(start: String, end: String): Boolean {
        val today = LocalDate.now().dayOfWeek.value
        // Solo mostramos "En Vivo" si la pestaña seleccionada es la de HOY
        if (_selectedDay.value != today) {
            return false
        }
        return ScheduleUtils.isLiveNow(start, end)
    }
}