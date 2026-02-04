package mx.edu.uas.radiouas.utils

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material.icons.rounded.Female
import androidx.compose.material.icons.rounded.HeadsetMic
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.LibraryMusic
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

/**
 * Archivo de Configuración Global.
 * Single Source of Truth (Fuente única de la verdad) para URLs y Keys.
 */
object AppConfig {

    const val RADIO_NAME = "Radio UAS"
    const val RADIO_SLOGAN = "Frecuencia de la cultura viva"

    // El flujo directo de audio (Icecast/Shoutcast)
    const val STREAM_AUDIO_URL = "https://stream9.mexiserver.com/8410/stream"

    // =================================================================
    // 2. RADIO FEED Y PROGRAMACIÓN (SPC)
    // =================================================================

    // Base para las APIs de programación (Schedule)
    const val SPC_BASE_URL = "https://spc.radiouas.org/"

    // Endpoint para saber qué está sonando AHORA (título y artista en vivo)
    // Nota: Es una URL completa porque a veces se usa directo con java.net.URL o OkHttp
    const val LIVE_PROGRAM_URL = "${SPC_BASE_URL}api/schedule/now?format=json"

    const val LIVE_RADIO_FEED_TITLE = "Radio UAS"
    const val LIVE_RADIO_FEED_SUBTITLE = "Frecuencia de la cultura viva"

    // =================================================================
    // 3. WORDPRESS (Noticias)
    // =================================================================

    // Sitio principal
    const val WP_GENERAL_TITLE = "Aldo de cultura"
    const val WP_BASE_URL = "https://radio.uas.edu.mx/"
    const val WP_CATEGORY_ID = 319
    const val WP_PER_PAGE = 10
    const val WP_API_ENDPOINT = "wp-json/wp/v2/posts"


    // =================================================================
    // 4. EMBY (Podcasts y On Demand)
    // =================================================================

    // Servidor de medios
    const val EMBY_BASE_URL = "https://emby.radiouas.org:8920/"
    // Clave de API (Generada en el panel de Emby)
    const val EMBY_API_KEY = "5127307a978c45b684dd90d0888f8b84"
    // ID de la carpeta raíz de música/podcasts
    const val EMBY_LIBRARY_ID = 5
    const val EMBY_LIMIT = 75

    // =================================================================
    // 5. VIDEO
    // =================================================================

    const val VIDEO_STREAM_URL = "https://stream8.mexiserver.com:2000/hls/radiouasx/radiouasx.m3u8"
}