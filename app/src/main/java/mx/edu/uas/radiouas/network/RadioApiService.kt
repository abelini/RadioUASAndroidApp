package mx.edu.uas.radiouas.network

import mx.edu.uas.radiouas.model.ScheduleItem
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Definición de los endpoints de la API
interface RadioApiService {

    // Obtenemos la programación diaria.
    // El parámetro 'source' ayuda al servidor a saber que la petición viene de la App.
    @GET("api/schedule/daily?source=mobile-app")
    suspend fun getDailySchedule(
        @Query("day") day: Int
    ): List<ScheduleItem>
}

// Instancia Singleton del cliente HTTP
object RetrofitInstance {

    private const val BASE_URL = "https://spc.radiouas.org/"

    val api: RadioApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RadioApiService::class.java)
    }
}