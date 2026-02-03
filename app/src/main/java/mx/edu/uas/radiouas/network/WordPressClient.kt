package mx.edu.uas.radiouas.network

import mx.edu.uas.radiouas.utils.AppConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WordPressClient {
    val api: WordPressApi by lazy {
        Retrofit.Builder()
            .baseUrl(AppConfig.WP_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WordPressApi::class.java)
    }
}