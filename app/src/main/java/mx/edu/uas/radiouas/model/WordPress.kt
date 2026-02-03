package mx.edu.uas.radiouas.model

import com.google.gson.annotations.SerializedName

data class WPPost(
    val id: Int,
    val title: WPTitle,
    val content: WPContent,
    // Mapeamos el campo JSON "jetpack_featured_media_url" a una variable kotlin
    @SerializedName("jetpack_featured_media_url")
    val featuredMediaUrl: String? = null
)

data class WPTitle(val rendered: String)

data class WPContent(val rendered: String)