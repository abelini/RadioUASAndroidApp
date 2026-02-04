package mx.edu.uas.radiouas.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object ScheduleUtils {

    @RequiresApi(Build.VERSION_CODES.O)
    private val STATION_ZONE = ZoneId.of("America/Mazatlan")

    @RequiresApi(Build.VERSION_CODES.O)
    fun isLiveNow(startTimeStr: String, endTimeStr: String): Boolean {
        val now = LocalTime.now(STATION_ZONE)

        return try {
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            val start = LocalTime.parse(startTimeStr, formatter)
            val end = LocalTime.parse(endTimeStr, formatter)

            // Caso especial: Programas que cruzan la medianoche (ej: 23:00 a 01:00)
            if (end.isBefore(start) || end == LocalTime.MIDNIGHT) {
                // Es válido si es después del inicio (noche) O antes del final (madrugada)
                !now.isBefore(start) || now.isBefore(end)
            } else {
                // Caso normal (ej: 08:00 a 09:00)
                // Usamos !isBefore para que incluya el segundo exacto de inicio (08:00:00)
                !now.isBefore(start) && now.isBefore(end)
            }
        } catch (e: Exception) {
            false
        }
    }
}