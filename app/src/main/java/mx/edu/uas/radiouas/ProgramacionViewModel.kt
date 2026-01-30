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

    // 1. LOADING: Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 2. DÍA SELECCIONADO: Por defecto hoy
    private val _selectedDay = MutableStateFlow(LocalDate.now().dayOfWeek.value)
    val selectedDay: StateFlow<Int> = _selectedDay

    // 3. LA LISTA (Aquí conectamos con el Repositorio)
    // Ya no usamos _schedule local, leemos directo del Repositorio
    val schedule: StateFlow<List<ScheduleItem>> = ScheduleRepository.fullSchedule
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 4. EL PROGRAMA EN VIVO (Se actualiza solo cada minuto gracias al Repositorio)
    val currentLiveProgram = ScheduleRepository.currentLiveProgram
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        // Cargar datos al iniciar
        loadScheduleForDay(_selectedDay.value)
    }

    // Cambio de día en los botones (LUN, MAR...)
    fun onDaySelected(day: Int) {
        _selectedDay.value = day
        loadScheduleForDay(day)
    }

    // Función que descarga datos y los manda al Repositorio
    private fun loadScheduleForDay(day: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Descargamos de Internet
                val response = RetrofitInstance.api.getDailySchedule(day)

                // 2. Guardamos en el Repositorio Global (IMPORTANTE)
                // Esto hace que el "currentLiveProgram" se recalcule
                ScheduleRepository.updateSchedule(response)

            } catch (e: Exception) {
                e.printStackTrace()
                // Si falla, mandamos lista vacía al repo
                ScheduleRepository.updateSchedule(emptyList())
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Helper simple para usar en la UI (lista)
    fun isLiveNow(start: String, end: String): Boolean {
        val today = LocalDate.now().dayOfWeek.value
        if (_selectedDay.value != today) {
            return false
        }
        return ScheduleUtils.isLiveNow(start, end)
    }
}