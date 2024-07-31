package com.example.gestionequipos.api

import com.example.gestionequipos.data.LoginResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("login.php")
    suspend fun login(
        @Body requestBody: RequestBody,
        @Header("Cache-Control") cacheControl: String = "no-cache"
    ): Response<LoginResponse>

    @POST("logout.php") // Reemplaza con la URL correcta de tu API
    suspend fun logout(): Response<Unit>
}
