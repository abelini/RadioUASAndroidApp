package mx.edu.uas.radiouas.network

import mx.edu.uas.radiouas.model.ScheduleItem
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// 1. La "Carta del Menú" (Interfaz)
// Aquí definimos qué le podemos pedir al servidor
interface RadioApiService {

    @GET("api/schedule/daily?source=mobile-app")
    suspend fun getDailySchedule(
        @Query("day") day: Int // Enviamos ?day=1
    ): List<ScheduleItem>
}

// 2. El "Cliente" (Objeto Singleton)
// Este es el objeto que usarás en el ViewModel para hacer la llamada
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