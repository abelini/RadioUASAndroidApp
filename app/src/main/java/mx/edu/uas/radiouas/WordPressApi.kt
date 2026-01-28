package mx.edu.uas.radiouas

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// 1. El modelo de lo que WordPress nos responde
data class WPPost(
    val id: Int,
    val title: WPTitle,
    val content: WPContent,
    val jetpack_featured_media_url: String? = null
)

data class WPTitle(val rendered: String)

data class WPContent(val rendered: String)

// 2. La interfaz que define la ruta de la API
interface WordPressApi {
    @GET("wp-json/wp/v2/posts?categories=319&per_page=10")
    suspend fun getUltimasNoticias(): List<WPPost>
}

// 3. El objeto que crea la conexión (Singleton)
object RetrofitInstance {
    val api: WordPressApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://radio.uas.edu.mx/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WordPressApi::class.java)
    }
}