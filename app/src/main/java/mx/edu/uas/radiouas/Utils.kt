package mx.edu.uas.radiouas

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material.icons.rounded.Female
import androidx.compose.material.icons.rounded.HeadsetMic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Podcasts
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// Data class simple para devolver Icono y Color juntos (Opcional, pero se ve muy Pro)
data class CategoryStyle(val icon: ImageVector, val color: Color)

fun getCategoryStyle(slug: String?): CategoryStyle {
    // Usamos el 'slug' que viene del JSON
    return when (slug?.lowercase()) {
        "music" -> CategoryStyle(Icons.Rounded.MusicNote, Color(0xFF9C27B0))

        // NUEVOS SLUGS DETECTADOS EN TU JSON:
        "journal" -> CategoryStyle(Icons.Rounded.HeadsetMic, Color(0xFFD32F2F)) // Rojo Noticias
        "academic" -> CategoryStyle(Icons.Rounded.School, Color(0xFF009688))   // Verde Académico
        "science" -> CategoryStyle(Icons.Rounded.Science, Color(0xFF009688))
        "women" -> CategoryStyle(Icons.Rounded.Female, Color(0xFFE91E63))
        "children" -> CategoryStyle(Icons.Rounded.ChildCare, Color(0xFF2196F3))

        // Otros que podrías usar a futuro:
        "culture" -> CategoryStyle(Icons.Rounded.Palette, Color(0xFFFF9800))
        "sports" -> CategoryStyle(Icons.Rounded.SportsSoccer, Color(0xFF4CAF50))
        "podcast" -> CategoryStyle(Icons.Rounded.Podcasts, Color(0xFFFF5722))
        // Default (Para cuando venga null o algo desconocido)
        else -> CategoryStyle(Icons.Rounded.Radio, Color(0xFF003366))
    }
}