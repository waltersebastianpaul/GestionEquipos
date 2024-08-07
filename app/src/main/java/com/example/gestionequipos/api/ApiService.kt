package com.example.gestionequipos.api

import com.example.gestionequipos.data.LoginResponse
import com.example.gestionequipos.utils.Constants
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST(Constants.Auth.LOGIN)
    suspend fun login(
        @Body requestBody: RequestBody,
        @Header("Cache-Control") cacheControl: String = "no-cache"
    ): Response<LoginResponse>

    @POST(Constants.Auth.LOGOUT) // Reemplaza con la URL correcta de tu API
    suspend fun logout(): Response<Unit>
}
