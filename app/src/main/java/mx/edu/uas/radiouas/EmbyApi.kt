package mx.edu.uas.radiouas

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

// --- MODELOS DE DATOS ---

data class EmbyResponse(
    val Items: List<EmbyItem>
)

data class EmbyItem(
    val Id: String,
    val Name: String,
    val Type: String, // Folder, MusicAlbum, Audio
    val AlbumArtist: String? = null
)

// --- INTERFAZ DE LA API ---

interface EmbyApi {
    @GET("emby/Items")
    suspend fun getItems(
        @Query("ParentId") parentId: String? = "5",
        @Query("api_key") apiKey: String = "5127307a978c45b684dd90d0888f8b84",
        @Query("IncludeItemTypes") itemTypes: String? = null,
        @Query("Recursive") recursive: Boolean = false,
        @Query("Fields") fields: String = "PrimaryImageAspectRatio"
    ): EmbyResponse
}

// --- CLIENTE SINGLETON ---

object EmbyClient {
    private const val BASE_URL = "https://emby.radiouas.org:8920/"

    /**
     * Crea un cliente OkHttp que ignora la validación de certificados SSL.
     * Útil para servidores con certificados autofirmados.
     */
    private fun getUnsafeOkHttpClient(): OkHttpClient {
        try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) {
                }

                override fun checkServerTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())

            return OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    fun getStreamUrl(itemId: String): String {
        return "${BASE_URL}emby/Audio/$itemId/stream?api_key=5127307a978c45b684dd90d0888f8b84"
    }

    // Instancia de Retrofit vinculada a la interfaz
    val api: EmbyApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getUnsafeOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EmbyApi::class.java)
    }
}