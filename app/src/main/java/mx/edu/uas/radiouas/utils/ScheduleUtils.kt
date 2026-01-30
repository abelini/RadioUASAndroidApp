package mx.edu.uas.radiouas.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object ScheduleUtils {

    // Función pura: Solo recibe datos y devuelve SI/NO
    @RequiresApi(Build.VERSION_CODES.O)
    fun isLiveNow(startTimeStr: String, endTimeStr: String): Boolean {
        val now = LocalTime.now() // Hora exacta del sistema

        return try {
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            val start = LocalTime.parse(startTimeStr, formatter)
            val end = LocalTime.parse(endTimeStr, formatter)

            // Caso especial: Programas que cruzan la medianoche (ej: 23:00 a 01:00)
            if (end.isBefore(start) || end == LocalTime.MIDNIGHT) {
                now.isAfter(start) || (now.isBefore(end) && now != end)
            } else {
                // Caso normal
                now.isAfter(start) && now.isBefore(end)
            }
        } catch (e: Exception) {
            false
        }
    }
}