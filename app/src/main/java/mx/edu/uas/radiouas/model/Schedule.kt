package mx.edu.uas.radiouas.model

import com.google.gson.annotations.SerializedName

data class ScheduleItem(
    @SerializedName("ID") val id: Int,
    val name: String,
    val subtitle: String,
    val category: String, // "music", "standard", etc.

    // Mapeamos los nombres del JSON a nombres de variables más cómodos
    @SerializedName("std_starts") val startTime: String, // "14:00:00"
    @SerializedName("std_ends") val endTime: String,     // "14:30:00"

    val dayOfWeek: Int
)

// Respuesta de la API (es una lista directa, pero por si acaso cambia)
typealias ScheduleResponse = List<ScheduleItem>