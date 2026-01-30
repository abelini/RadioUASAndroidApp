package mx.edu.uas.radiouas.model

import com.google.gson.annotations.SerializedName

data class ScheduleItem(
    @SerializedName("ID") val id: Int,
    val name: String,
    val subtitle: String,

    val slug: String,

    val startTime: String,
    val endTime: String,

    val dayOfWeek: Int
)

// Respuesta de la API (es una lista directa, pero por si acaso cambia)
typealias ScheduleResponse = List<ScheduleItem>