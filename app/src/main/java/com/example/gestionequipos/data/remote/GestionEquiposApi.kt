package com.example.gestionequipos.data.remote

import android.app.Application
import com.example.gestionequipos.data.Equipo
import com.example.gestionequipos.data.Obra
import com.example.gestionequipos.utils.Constants
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface GestionEquiposApi {

    @GET(Constants.Equipos.GET_LISTA) // Ruta corregida
    suspend fun getEquipos(): List<Equipo>

    @GET(Constants.Obras.GET_LISTA) // Ruta corregida
    suspend fun getObras(): List<Obra>

    companion object {
        private val BASE_URL = Constants.getBaseUrl() //"https://adminobr.000webhostapp.com/" // URL base corregida

        fun create(application: Application): GestionEquiposApi {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            val httpClient = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            val gson = GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss") // Ajusta el formato de fecha si es necesario
                .create()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient)
                .build()

            return retrofit.create(GestionEquiposApi::class.java)
        }
    }
}