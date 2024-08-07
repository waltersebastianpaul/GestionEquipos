package com.example.gestionequipos.ui.autocomplete

import android.content.Context
import android.util.Log
import com.example.gestionequipos.data.Equipo
import com.example.gestionequipos.data.Obra
import com.example.gestionequipos.utils.Constants
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object AutocompleteRepository {
    private val baseUrl = Constants.getBaseUrl() //"https://adminobr.000webhostapp.com/" // Reemplaza con tu URL base

    private var obras: List<Obra>? = null
    private var equipos: List<Equipo>? = null

    suspend fun getObras(context: Context): List<Obra> {
        Log.d("Obras llega a getObras", "Obras: $obras")
        if (obras == null) {
            obras = cargarObrasDesdeBaseDeDatos(context)
        }
        return obras ?: emptyList()
    }

    suspend fun getEquipos(context: Context): List<Equipo> {
        if (equipos == null) {
            equipos = cargarEquiposDesdeBaseDeDatos(context)
        }
        return equipos ?: emptyList()
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private suspend fun cargarObrasDesdeBaseDeDatos(context: Context): List<Obra> {
        val url = baseUrl + "get_obras.php"

        val request = Request.Builder().url(url).build()
        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val json = response.body?.string() ?: ""
                    val gson = Gson()
                    val type = object : TypeToken<List<Obra>>() {}.type
                    gson.fromJson(json, type) ?: emptyList()
                } else {
                    Log.e("Error", "Error al obtener obras: Código de estado HTTP ${response.code}") // Usamos response.code()
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e("Error", "Error al obtener obras: ${e.message}")
                emptyList()
            }
        }
    }

    private suspend fun cargarEquiposDesdeBaseDeDatos(context: Context): List<Equipo> {
        val url = baseUrl + "get_equipos.php"
        val request = Request.Builder().url(url).build()
        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val json = response.body?.string()?: ""
                    val gson = Gson()
                    val type = object : TypeToken<List<Equipo>>() {}.type
                    gson.fromJson(json, type) ?: emptyList()
                } else {
                    Log.e("Error", "Error al obtener equipos: Código de estado HTTP ${response.code}") // Usamos response.code()
                    emptyList()
                }
            } catch (e: Exception) {Log.e("Error", "Error al obtener equipos: ${e.message}")
                emptyList()
            }
        }
    }
}
