package com.example.gestionequipos.ui.partediario

import android.util.Log
import androidx.lifecycle.*
import androidx.paging.*
import com.example.gestionequipos.data.Equipo
import com.example.gestionequipos.data.ListarPartesDiarios
import com.example.gestionequipos.data.Obra
import com.example.gestionequipos.data.ParteDiario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.HttpException
import java.io.IOException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit


import androidx.lifecycle.*
import androidx.paging.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


import androidx.lifecycle.*
import androidx.paging.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ParteDiarioViewModel : ViewModel() {

    private val baseUrl = "https://adminobr.000webhostapp.com/"

    private val _mensaje = MutableLiveData<Event<String?>>()
    val mensaje: LiveData<Event<String?>> = _mensaje

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<Event<String?>>()
    val error: LiveData<Event<String?>> = _error

    private val _filterEquipo = MutableLiveData<String>()
    private val _filterFechaInicio = MutableLiveData<String>()
    private val _filterFechaFin = MutableLiveData<String>()

    private val client = OkHttpClient.Builder().build()

    val partesDiarios: Flow<PagingData<ListarPartesDiarios>> = combine(
        _filterEquipo.asFlow(),
        _filterFechaInicio.asFlow(),
        _filterFechaFin.asFlow()
    ) { equipo, fechaInicio, fechaFin ->
        Triple(equipo, fechaInicio, fechaFin)
    }.flatMapLatest { (equipo, fechaInicio, fechaFin) ->
        Log.d("ParteDiarioViewModel", "Fetching data with filter - Equipo: $equipo, FechaInicio: $fechaInicio, FechaFin: $fechaFin")
        Pager(PagingConfig(pageSize = 20)) {
            ParteDiarioPagingSource(client, baseUrl, equipo ?: "", fechaInicio ?: "", fechaFin ?: "")
        }.flow.cachedIn(viewModelScope)
    }

    fun setFilter(equipo: String, fechaInicio: String, fechaFin: String) {
        Log.d("ParteDiarioViewModel", "Setting filter - Equipo: $equipo, FechaInicio: $fechaInicio, FechaFin: $fechaFin")
        _filterEquipo.value = equipo
        _filterFechaInicio.value = fechaInicio
        _filterFechaFin.value = fechaFin
    }

    fun guardarParteDiario(parteDiario: ParteDiario, callback: (Boolean) -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val fechaConvertida = convertirFecha(parteDiario.fecha)
                val parteDiarioConvertido = parteDiario.copy(fecha = fechaConvertida)
                val resultado = withContext(Dispatchers.IO) {
                    guardarParteDiarioEnBaseDeDatos(parteDiarioConvertido)
                }
                if (resultado) {
                    _mensaje.value = Event("Parte diario guardado con éxito")
                    callback(true)
                } else {
                    _error.value = Event("Error al guardar el parte diario")
                    callback(false)
                }
            } catch (e: Exception) {
                _error.value = Event("Error al guardar el parte diario: ${e.message}")
                callback(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun convertirFecha(fechaOriginal: String): String {
        val parts = fechaOriginal.split("/")
        return if (parts.size == 3) {
            "${parts[2]}/${parts[1]}/${parts[0]}"
        } else {
            fechaOriginal
        }
    }

    private suspend fun guardarParteDiarioEnBaseDeDatos(parteDiario: ParteDiario): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = FormBody.Builder()
                    .add("fecha", parteDiario.fecha)
                    .add("equipoId", parteDiario.equipoId.toString())
                    .add("horasInicio", parteDiario.horasInicio.toString())
                    .add("horasFin", parteDiario.horasFin.toString())
                    .add("horasTrabajadas", parteDiario.horasTrabajadas.toString())
                    .add("observaciones", parteDiario.observaciones ?: "")
                    .add("obraId", parteDiario.obraId.toString())
                    .add("userCreated", parteDiario.userCreated.toString())
                    .add("estadoId", parteDiario.estadoId.toString())
                    .build()

                val request = Request.Builder()
                    .url("$baseUrl/guardar_parte_diario.php")
                    .post(requestBody)
                    .build()

                Log.d("ParteDiarioViewModel", "Enviando datos al servidor: ${requestBody.toString()}")

                val response = client.newCall(request).execute()

                Log.d("ParteDiarioViewModel", "Respuesta del servidor: ${response.code} - ${response.body?.string()}")

                response.isSuccessful
            } catch (e: IOException) {
                Log.e("ParteDiarioViewModel", "Error de red al guardar parte diario: ${e.message}")
                false
            }
        }
    }
    fun setEquipos(equipos: List<Equipo>) {
        // No necesitas hacer nada aquí, ya que no tienes _equipos
    }

    fun setObras(obras: List<Obra>) {
        // No necesitas hacer nada aquí, ya que no tienes _obras
    }
}
