package mx.edu.uas.radiouas.utils

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material.icons.rounded.Female
import androidx.compose.material.icons.rounded.HeadsetMic
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Newspaper
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Podcasts
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime


// Data class simple para devolver Icono y Color juntos (Opcional, pero se ve muy Pro)
data class CategoryStyle(val icon: ImageVector, val color: Color)

fun getCategoryStyle(slug: String?): CategoryStyle {
    return when (slug?.lowercase()) {
        "music" -> CategoryStyle(Icons.Rounded.MusicNote, Color(0xFF9C27B0))
        "science" -> CategoryStyle(Icons.Rounded.Science, Color(0xFF009688))
        "news" -> CategoryStyle(Icons.Rounded.Newspaper, Color(0xFF1976D2))
        "culture" -> CategoryStyle(Icons.Rounded.Palette, Color(0xFFFF9800))
        "sports" -> CategoryStyle(Icons.Rounded.SportsSoccer, Color(0xFF2E7D32))
        "entertainment" -> CategoryStyle(Icons.Rounded.Movie, Color(0xFFF44336))
        "children" -> CategoryStyle(Icons.Rounded.ChildCare, Color(0xFF03A9F4))
        "youth" -> CategoryStyle(Icons.Rounded.People, Color(0xFFFF5722))
        "women" -> CategoryStyle(Icons.Rounded.Female, Color(0xFFE91E63))
        "journal" -> CategoryStyle(Icons.Rounded.HeadsetMic, Color(0xFFD32F2F))
        "academic" -> CategoryStyle(Icons.Rounded.School, Color(0xFF3F51B5))
        "health" -> CategoryStyle(Icons.Rounded.HealthAndSafety, Color(0xFF43A047))
        "podcast" -> CategoryStyle(Icons.Rounded.Podcasts, Color(0xFF607D8B))
        "magazine" -> CategoryStyle(Icons.Rounded.LibraryMusic, Color(0xFF673AB7))
        else -> CategoryStyle(Icons.Rounded.Radio, Color(0xFF003366))
    }
}


// Obtiene el nombre del día en Español (ej: "Lunes", "Martes")
@RequiresApi(Build.VERSION_CODES.O)
fun obtenerDiaActual(): String {
    val hoy = LocalDate.now().dayOfWeek
    // Mapeo manual para asegurar compatibilidad con tus tabs
    return when (hoy) {
        DayOfWeek.MONDAY -> "Lunes"
        DayOfWeek.TUESDAY -> "Martes"
        DayOfWeek.WEDNESDAY -> "Miércoles"
        DayOfWeek.THURSDAY -> "Jueves"
        DayOfWeek.FRIDAY -> "Viernes"
        DayOfWeek.SATURDAY -> "Sábado"
        DayOfWeek.SUNDAY -> "Domingo"
        else -> "Lunes"
    }
}

// Verifica si la hora actual está dentro del rango del programa
@RequiresApi(Build.VERSION_CODES.O)
fun esProgramaEnVivo(horaInicioStr: String, horaFinStr: String): Boolean {
    return try {
        val horaActual = LocalTime.now()
        // Asumiendo formato "HH:mm" o "HH:mm:ss" desde tu API
        val inicio = LocalTime.parse(horaInicioStr.take(5))
        val fin = LocalTime.parse(horaFinStr.take(5))

        // Lógica: Si inicio < fin (mismo día) O si cruza medianoche
        if (inicio.isBefore(fin)) {
            horaActual.isAfter(inicio) && horaActual.isBefore(fin)
        } else {
            // Caso especial: programa que cruza la medianoche (ej: 23:00 a 01:00)
            horaActual.isAfter(inicio) || horaActual.isBefore(fin)
        }
    } catch (e: Exception) {
        false
    }
}