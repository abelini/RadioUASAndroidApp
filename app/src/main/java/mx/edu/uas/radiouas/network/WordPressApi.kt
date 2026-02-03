package mx.edu.uas.radiouas.network

import mx.edu.uas.radiouas.utils.AppConfig
import mx.edu.uas.radiouas.model.WPPost
import retrofit2.http.GET
import retrofit2.http.Query

interface WordPressApi {
    @GET(AppConfig.WP_API_ENDPOINT)
    suspend fun getUltimasNoticias(
        @Query("categories") category: Int = AppConfig.WP_CATEGORY_ID,
        @Query("per_page") perPage: Int = AppConfig.WP_PER_PAGE
    ): List<WPPost>
}