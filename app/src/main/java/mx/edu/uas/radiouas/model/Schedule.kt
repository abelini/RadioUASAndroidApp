package mx.edu.uas.radiouas.model

import com.google.gson.annotations.SerializedName

data class ScheduleItem(
    @SerializedName("ID") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("subtitle") val subtitle: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("dayOfWeek") val dayOfWeek: Int
)

typealias ScheduleResponse = List<ScheduleItem>