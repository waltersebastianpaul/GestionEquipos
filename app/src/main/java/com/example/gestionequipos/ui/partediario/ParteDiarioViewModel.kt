package com.example.gestionequipos.ui.partediario

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionequipos.data.Equipo
import com.example.gestionequipos.data.ListarPartesDiarios
//import com.example.gestionequipos.data.Estado
import com.example.gestionequipos.data.Obra
import com.example.gestionequipos.data.ParteDiario
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.IOException
import com.example.gestionequipos.ui.partediario.Event
import com.google.gson.GsonBuilder
import java.util.concurrent.TimeUnit

class ParteDiarioViewModel : ViewModel() {

    private val baseUrl = "https://adminobr.000webhostapp.com/" // Reemplaza con tu URL base

    private val _equipos = MutableLiveData<List<Equipo>>()
    val equipos: LiveData<List<Equipo>> = _equipos

    private val _obras = MutableLiveData<List<Obra>>()
    val obras: LiveData<List<Obra>> = _obras

    private val _mensaje = MutableLiveData<Event<String?>>()
    val mensaje: LiveData<Event<String?>> = _mensaje

    private val _partesDiarios = MutableLiveData<List<ListarPartesDiarios>>()
    val partesDiarios:LiveData<List<ListarPartesDiarios>> = _partesDiarios

    fun setMensaje(mensaje: String?) {
        _mensaje.value = Event(mensaje)
    }

    fun cargarDatos() {
        viewModelScope.launch {
            // Ejecuta las peticiones de red en unhilo secundario (Dispatchers.IO)
            val equiposDeferred = async(Dispatchers.IO) { obtenerEquiposDesdeBaseDeDatos() }
            val obrasDeferred = async(Dispatchers.IO) { obtenerObrasDesdeBaseDeDatos() }

            // Espera a que las peticiones finalicen y actualiza los LiveData en el hilo principal
            _equipos.value = equiposDeferred.await()
            _obras.value = obrasDeferred.await()}
    }

    fun guardarParteDiario(parteDiario: ParteDiario) {
        viewModelScope.launch {
            // Convierte la fecha a "aaaa/mm/dd"
            val fechaConvertida = convertirFecha(parteDiario.fecha)

            // Crea un nuevo objeto ParteDiario con la fecha convertida
            val parteDiarioConvertido = parteDiario.copy(fecha = fechaConvertida)

            val resultado = withContext(Dispatchers.IO) {
                guardarParteDiarioEnBaseDeDatos(parteDiarioConvertido)
            }
            // Maneja el resultado en el hilo principal
            if (resultado) {
                _mensaje.value = Event("Parte diario guardado con éxito") // Envuelve el mensaje en un Event
            } else {
                _mensaje.value = Event("Error al guardar el parte diario") // Envuelve el mensaje en un Event
            }
        }
    }

    fun cargarPartesDiarios() {
        viewModelScope.launch {
            val partesDiarios = obtenerPartesDiariosDesdeBaseDeDatos()
            _partesDiarios.value = partesDiarios
        }
    }

    private fun convertirFecha(fechaOriginal: String): String {
        val parts = fechaOriginal.split("/")
        return if (parts.size == 3) {
            "${parts[2]}/${parts[1]}/${parts[0]}"
        } else {
            fechaOriginal // Devuelve la fecha original si no se puede convertir
        }
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private suspend fun guardarParteDiarioEnBaseDeDatos(parteDiario: ParteDiario): Boolean {
        return withContext(Dispatchers.IO) {
            val requestBody = FormBody.Builder()
                .add("fecha", parteDiario.fecha)
                .add("equipoId", parteDiario.equipoId.toString()).add("horasInicio", parteDiario.horasInicio.toString()) // Convierte a String
                .add("horasFin", parteDiario.horasFin.toString()) // Convierte a String
                .add("horasTrabajadas", parteDiario.horasTrabajadas.toString())
                .add("observaciones", parteDiario.observaciones ?: "")
                .add("obraId", parteDiario.obraId.toString())
                .add("userCreated", parteDiario.userCreated.toString())
                .add("estadoId", parteDiario.estadoId.toString())
                .build()

            val request = Request.Builder()
                .url(baseUrl + "guardar_parte_diario.php")
                .post(requestBody)
                .build()

            try {
                val response = client.newCall(request).execute()
                response.isSuccessful // Devuelve true si la petición fue exitosa, false en caso contrario
            } catch (e: IOException) {
                false // Devuelve false en caso de error de red
            }
        }
    }

    private suspend fun obtenerEquiposDesdeBaseDeDatos(): List<Equipo> {
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

    private suspend fun obtenerObrasDesdeBaseDeDatos(): List<Obra> {
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

    private suspend fun obtenerPartesDiariosDesdeBaseDeDatos(): List<ListarPartesDiarios> {
        val url = baseUrl + "get_partes_diarios.php" // Reemplaza con la URL de tu archivo PHP

        val request = Request.Builder().url(url).build()
        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val json = response.body?.string() ?: ""
                    Log.d("ParteDiario", "JSON recibido: $json") // Agrega este log

                    // Crea una instancia de Gson con el modo lenient habilitado
                    val gson = GsonBuilder().setLenient().create()

                    val type = object : TypeToken<List<ListarPartesDiarios>>() {}.type
                    val listarPartesDiarios: List<ListarPartesDiarios> = gson.fromJson(json, type) ?: emptyList()

                    // Mapea los datos de ListarPartesDiarios a ParteDiario si lo necesitas
                    // ... (tu código de mapeo si es necesario)

                    listarPartesDiarios // Devuelve la lista de ListarPartesDiarios
                } else {
                    Log.e("Error", "Error al obtener partes diarios: Código de estado HTTP ${response.code}")
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e("Error", "Error al obtener partes diarios: ${e.message}")
                emptyList()
            }
        }
    }

    fun filtrarPartesDiarios(equipo: String, fecha: String): List<ListarPartesDiarios> {
        return _partesDiarios.value?.filter { parteDiario ->
            (equipo == "Todos" || parteDiario.interno == equipo) &&
                    (fecha.isEmpty() || parteDiario.fecha == fecha)
        } ?: emptyList()
    }
}