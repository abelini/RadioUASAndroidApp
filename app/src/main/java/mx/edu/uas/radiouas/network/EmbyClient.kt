package mx.edu.uas.radiouas.network

import mx.edu.uas.radiouas.utils.AppConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object EmbyClient {

    fun getAudioUrl(itemId: String): String {
        return "${AppConfig.EMBY_BASE_URL}Audio/$itemId/stream.mp3?static=true&api_key=${AppConfig.EMBY_API_KEY}"
    }

    private fun getUnsafeOkHttpClient(): OkHttpClient {
        /* ... mismo código de seguridad SSL ... */
        try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
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
        return "${AppConfig.EMBY_BASE_URL}emby/Audio/$itemId/stream.mp3?static=true&api_key=${AppConfig.EMBY_API_KEY}"
    }

    fun getImageUrl(itemId: String): String {
        return "${AppConfig.EMBY_BASE_URL}emby/Items/$itemId/Images/Primary?api_key=${AppConfig.EMBY_API_KEY}"
    }

    val api: EmbyApi by lazy {
        Retrofit.Builder()
            .baseUrl(AppConfig.EMBY_BASE_URL)
            .client(getUnsafeOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EmbyApi::class.java) // Aquí ya reconoce EmbyApi porque lo definimos en el otro archivo
    }
}