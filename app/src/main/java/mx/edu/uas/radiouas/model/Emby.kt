package mx.edu.uas.radiouas.model

import com.google.gson.annotations.SerializedName

// Respuesta principal de Emby
data class EmbyResponse(
    @SerializedName("Items") val items: List<EmbyItem>
)

// Cada ítem (Álbum o Canción)
data class EmbyItem(
    @SerializedName("Id") val id: String,
    @SerializedName("Name") val name: String,
    @SerializedName("Type") val type: String,
    @SerializedName("AlbumArtist") val albumArtist: String? = null
)