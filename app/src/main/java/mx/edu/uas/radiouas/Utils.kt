package mx.edu.uas.radiouas

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.* // Usaremos estilo Rounded que es más moderno
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// Data class simple para devolver Icono y Color juntos (Opcional, pero se ve muy Pro)
data class CategoryStyle(val icon: ImageVector, val color: Color)

fun getCategoryStyle(category: String?): CategoryStyle {
    // Convertimos a minúsculas para evitar errores (Music vs music)
    return when (category?.lowercase()) {
        "music" -> CategoryStyle(Icons.Rounded.MusicNote, Color(0xFF9C27B0)) // Morado
        "science" -> CategoryStyle(Icons.Rounded.Science, Color(0xFF009688)) // Verde Azulado
        "news" -> CategoryStyle(Icons.Rounded.Newspaper, Color(0xFFD32F2F)) // Rojo Noticias
        "culture" -> CategoryStyle(Icons.Rounded.Palette, Color(0xFFFF9800)) // Naranja Arte
        "sports" -> CategoryStyle(Icons.Rounded.SportsSoccer, Color(0xFF4CAF50)) // Verde Pasto
        "entertainment" -> CategoryStyle(Icons.Rounded.Movie, Color(0xFFE91E63)) // Rosa
        "children" -> CategoryStyle(Icons.Rounded.ChildCare, Color(0xFF2196F3)) // Azul Claro
        "youth" -> CategoryStyle(Icons.Rounded.Headphones, Color(0xFF673AB7)) // Violeta
        "women" -> CategoryStyle(Icons.Rounded.Female, Color(0xFFE91E63)) // Rosa Fuerte

        // Default (Para "standard" o cualquier cosa nueva que no conozcamos)
        else -> CategoryStyle(Icons.Rounded.Radio, Color(0xFF003366)) // Azul UAS
    }
}