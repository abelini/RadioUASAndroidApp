package mx.edu.uas.radiouas.utils

import android.os.Build
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatearFechaTitulo(tituloOriginal: String): String {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        try {
            val regex = Regex("""^(\d{4})(\d{2})(\d{2}).*""")
            val match = regex.find(tituloOriginal)
            if (match != null) {
                val (anio, mes, dia) = match.destructured
                val fecha = LocalDate.of(anio.toInt(), mes.toInt(), dia.toInt())
                val formateador = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy",
                    Locale("es", "MX")
                )
                return fecha.format(formateador)
            }
        } catch (e: Exception) { return tituloOriginal }
    }
    return tituloOriginal
}