package mx.edu.uas.radiouas.model

import com.google.gson.annotations.SerializedName

data class ScheduleItem(
    val name: String,
    val produccion: String,
    // Usaremos esto para pintar iconos nativos en el UI
    @SerializedName("icon") val iconHtml: String,

    // Horarios para mostrar
    val starts: String,
    val ends: String,

    // ¡AGREGA ESTO A TU API SI PUEDES! Si no, vendrá null y pondremos un placeholder
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("description") val description: String? = null
)

// Respuesta de la API (es una lista directa, pero por si acaso cambia)
typealias ScheduleResponse = List<ScheduleItem>