package mx.edu.uas.radiouas.network

import mx.edu.uas.radiouas.utils.AppConfig
import mx.edu.uas.radiouas.model.EmbyResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface EmbyApi {
    @GET("emby/Items")
    suspend fun getPodcasts(
        @Query("ParentId") parentId: Int = AppConfig.EMBY_LIBRARY_ID,
        @Query("api_key") apiKey: String = AppConfig.EMBY_API_KEY,
        @Query("IncludeItemTypes") itemTypes: String? = "MusicAlbum",
        @Query("Recursive") recursive: Boolean = true,
        @Query("Fields") fields: String = "PrimaryImageAspectRatio,Overview,DateCreated",
        @Query("Limit") limit: Int? = AppConfig.EMBY_LIMIT,
        @Query("StartIndex") startIndex: Int? = 0,
        @Query("SortBy") sortBy: String = "DateCreated",
        @Query("SortOrder") sortOrder: String = "Descending"
    ): EmbyResponse

    @GET("emby/Items")
    suspend fun getEpisodes(
        @Query("ParentId") albumId: String, // <--- Este es el ID dinámico del álbum
        @Query("IncludeItemTypes") itemTypes: String = "Audio",
        @Query("Recursive") recursive: Boolean = true,
        @Query("SortBy") sortBy: String = "SortName",
        @Query("api_key") apiKey: String = AppConfig.EMBY_API_KEY,
        @Query("Fields") fields: String = "PrimaryImageAspectRatio,Overview,MediaSources"
    ): EmbyResponse
}