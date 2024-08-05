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












//    private val baseUrl = "https://adminobr.000webhostapp.com/" // Reemplaza con tu URL base
//
//    private val _mensaje = MutableLiveData<Event<String?>>()
//    val mensaje: LiveData<Event<String?>> = _mensaje
//
//    private val _isLoading = MutableLiveData<Boolean>()
//    val isLoading: LiveData<Boolean> = _isLoading
//
//    private val _error =MutableLiveData<Event<String?>>()
//    val error: LiveData<Event<String?>> = _error
//
//    private val _partesDiarios = MutableLiveData<List<ListarPartesDiarios>>()
//
//    fun setMensaje(mensaje: String?) {
//        _mensaje.value = Event(mensaje)
//    }
//
//
//    private val client = OkHttpClient.Builder()
//    .connectTimeout(30, TimeUnit.SECONDS)
//    .readTimeout(30, TimeUnit.SECONDS)
//    .writeTimeout(30, TimeUnit.SECONDS)
//    .build()
//
//    private val _filterEquipo = MutableLiveData<String>()
//    private val _filterFechaInicio = MutableLiveData<String>()
//    private val _filterFechaFin = MutableLiveData<String>()
//
//    val partesDiarios: Flow<PagingData<ListarPartesDiarios>> = Transformations.switchMap(
//        _filterEquipo,
//        _filterFechaInicio,
//        _filterFechaFin
//    ) { equipo, fechaInicio, fechaFin ->
//        Pager(PagingConfig(pageSize = 20)) {
//            ParteDiarioPagingSource(client, equipo, fechaInicio, fechaFin)
//        }.flow.cachedIn(viewModelScope)
//    }
//
//    fun setFilter(equipo: String, fechaInicio: String, fechaFin: String) {
//        _filterEquipo.value = equipo
//        _filterFechaInicio.value = fechaInicio
//        _filterFechaFin.value = fechaFin
//    }
//
//
//
//
////    fun cargarDatos() {
////        viewModelScope.launch {
////            // Ejecuta las peticiones de red en un hilo secundario (Dispatchers.IO)
////            val equiposDeferred = async(Dispatchers.IO) { obtenerEquiposDesdeBaseDeDatos() }
////            val obrasDeferred = async(Dispatchers.IO) { obtenerObrasDesdeBaseDeDatos() }
////
////            // Espera a que las peticiones finalicen y actualiza los LiveData en el hilo principal
////            _equipos.value = equiposDeferred.await()
////            _obras.value = obrasDeferred.await()}
////    }
//
//    fun guardarParteDiario(parteDiario: ParteDiario, callback: (Boolean) -> Unit) {
//
//        _isLoading.value = true
//        viewModelScope.launch {
//            try{
//            // Convierte la fecha a "aaaa/mm/dd"
//
//            val fechaConvertida = convertirFecha(parteDiario.fecha)
//
//            // Crea un nuevo objeto ParteDiario con la fecha convertida
//            val parteDiarioConvertido = parteDiario.copy(fecha = fechaConvertida)
//
//                val resultado = withContext(Dispatchers.IO) {
//                    guardarParteDiarioEnBaseDeDatos(parteDiarioConvertido)
//                }
//                if (resultado) {
//                    _mensaje.value = Event("Parte diario guardado con éxito")
//                    callback(true) // Indicar éxito
//                } else {
//                    _error.value = Event("Error al guardar el parte diario")
//                    callback(false) // Indicar error
//                }
//            } catch (e: HttpException) {
//                _error.value = Event("Error de red: ${e.message}")
//                callback(false) // Indicar error
//            } catch (e: UnknownHostException) {
//                _error.value = Event("No hay conexión a internet: ${e.message}")
//                callback(false) // Indicar error
//            } catch (e: Exception) {
//                _error.value = Event("Error al guardar el parte diario: ${e.message}")
//                callback(false) // Indicar error
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }
//
//    fun cargarPartesDiarios(callback: () -> Unit) {
//        viewModelScope.launch {
//            try {
//                val partesDiarios = obtenerPartesDiariosDesdeBaseDeDatos()
//                _partesDiarios.value = partesDiarios
//                callback()
//            } catch (e: Exception) {
//                _error.value = Event("Error al cargar los partes diarios: ${e.message}")
//            }
//        }
//    }
//
//    private fun convertirFecha(fechaOriginal: String): String {
//        val parts = fechaOriginal.split("/")
//        return if (parts.size == 3) {
//            "${parts[2]}/${parts[1]}/${parts[0]}"
//        } else {
//            fechaOriginal // Devuelve la fecha original si no se puede convertir
//        }
//    }
//
//    private suspend fun guardarParteDiarioEnBaseDeDatos(parteDiario: ParteDiario): Boolean {
//        return withContext(Dispatchers.IO){
//            try {
//                val requestBody = FormBody.Builder()
//                    .add("fecha", parteDiario.fecha)
//                    .add("equipoId", parteDiario.equipoId.toString())
//                    .add("horasInicio", parteDiario.horasInicio.toString())
//                    .add("horasFin", parteDiario.horasFin.toString())
//                    .add("horasTrabajadas", parteDiario.horasTrabajadas.toString())
//                    .add("observaciones", parteDiario.observaciones ?: "")
//                    .add("obraId", parteDiario.obraId.toString())
//                    .add("userCreated", parteDiario.userCreated.toString())
//                    .add("estadoId", parteDiario.estadoId.toString())
//                    .build()
//
//                val request = Request.Builder()
//                    .url(baseUrl + "guardar_parte_diario.php")
//                    .post(requestBody)
//                    .build()
//
//                Log.d("ParteDiarioViewModel", "Enviando datos al servidor: ${requestBody.toString()}")
//
//                for (i in 0 until requestBody.size) {
//                    Log.d("ParteDiarioViewModel", "Dato ${i}: ${requestBody.name(i)} = ${requestBody.value(i)}")
//                }
//
//                val response = client.newCall(request).execute()
//
//                Log.d("ParteDiarioViewModel", "Respuesta del servidor: ${response.code} - ${response.body?.string()}")
//
//                response.isSuccessful
//            } catch (e: IOException) {
//                Log.e("ParteDiarioViewModel", "Error de red al guardar parte diario: ${e.message}")
//                false
//            }
//        }
//    }
//    private suspend fun obtenerEquiposDesdeBaseDeDatos(): List<Equipo> {
//        val url = baseUrl + "get_equipos.php"
//        val request = Request.Builder().url(url).build()
//        return withContext(Dispatchers.IO) {
//            try {
//                val response = client.newCall(request).execute()
//                if (response.isSuccessful) {
//                    val json = response.body?.string()?: ""
//                    val gson = Gson()
//                    val type = object : TypeToken<List<Equipo>>() {}.type
//                    gson.fromJson(json, type) ?: emptyList()
//                } else {
//                    Log.e("Error", "Error al obtener equipos: Código de estado HTTP ${response.code}") // Usamos response.code()
//                    emptyList()
//                }
//            } catch (e: Exception) {Log.e("Error", "Error al obtener equipos: ${e.message}")
//                emptyList()
//            }
//        }
//    }
//
//    private suspend fun obtenerObrasDesdeBaseDeDatos(): List<Obra> {
//        val url = baseUrl + "get_obras.php"
//
//        val request = Request.Builder().url(url).build()
//        return withContext(Dispatchers.IO) {
//            try {
//                val response = client.newCall(request).execute()
//                if (response.isSuccessful) {
//                    val json = response.body?.string() ?: ""
//                    val gson = Gson()
//                    val type = object : TypeToken<List<Obra>>() {}.type
//                    gson.fromJson(json, type) ?: emptyList()
//                } else {
//                    Log.e("Error", "Error al obtener obras: Código de estado HTTP ${response.code}") // Usamos response.code()
//                    emptyList()
//                }
//            } catch (e: Exception) {
//                Log.e("Error", "Error al obtener obras: ${e.message}")
//                emptyList()
//            }
//        }
//    }
//
//    private suspend fun obtenerPartesDiariosDesdeBaseDeDatos(): List<ListarPartesDiarios> {
//        val url = baseUrl + "get_partes_diarios.php" // Reemplaza con la URL de tu archivo PHP
//
//        val request = Request.Builder().url(url).build()
//        return withContext(Dispatchers.IO) {
//            try {
//                val response = client.newCall(request).execute()
//                if (response.isSuccessful) {
//                    val json = response.body?.string() ?: ""
//                    Log.d("ParteDiario", "JSON recibido: $json") // Agrega este log
//
//                    // Crea una instancia de Gson con el modo lenient habilitado
//                    val gson = GsonBuilder().setLenient().create()
//
//                    val type = object : TypeToken<List<ListarPartesDiarios>>() {}.type
//                    val listarPartesDiarios: List<ListarPartesDiarios> = gson.fromJson(json, type) ?: emptyList()
//
//                    // Mapea los datos de ListarPartesDiarios a ParteDiario si lo necesitas
//                    // ... (tu código de mapeo si es necesario)
//
//                    listarPartesDiarios // Devuelve la lista de ListarPartesDiarios
//                } else {
//                    Log.e("Error", "Error al obtener partes diarios: Código de estado HTTP ${response.code}")
//                    emptyList()
//                }
//            } catch (e: Exception) {
//                Log.e("Error", "Error al obtener partes diarios: ${e.message}")
//                emptyList()
//            }
//        }
//    }
//
//    fun filtrarPartesDiarios(equipo: String, fecha: String): List<ListarPartesDiarios> {
//        Log.d("ParteDiarioViewModel", "Equipo a filtrar: $equipo")
//        Log.d("ParteDiarioViewModel", "Fecha a filtrar: $fecha")
//
//        val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Formato de fecha
//        val fechaFiltroDate = try {
//            formatoFecha.parse(fecha) // Convertir la fecha del filtro a Date
//        } catch (e: Exception) {
//            null // Manejar el caso en que la fecha no sea válida
//        }
//
//        val resultado = _partesDiarios.value?.filter { parteDiario ->
//            val fechaParteDiario = try {
//                formatoFecha.parse(parteDiario.fecha)
//            } catch (e: Exception) {
//                Log.e("ParteDiarioViewModel", "Error al parsear la fecha del parte diario: ${parteDiario.fecha}", e)
//                null
//            }
//
//            val equipoCoincide = equipo.isEmpty() || parteDiario.interno == equipo
//            val fechaCoincide = fechaFiltroDate == null || (fechaParteDiario != null && fechaParteDiario == fechaFiltroDate)
//
//            Log.d("ParteDiarioViewModel", "ParteDiario: ${parteDiario.fecha} - Equipo coincide: $equipoCoincide - Fecha coincide: $fechaCoincide")
//
//            equipoCoincide && fechaCoincide
//        } ?: emptyList()
//
//        Log.d("ParteDiarioViewModel", "Partes diarios filtrados: $resultado")
//        return resultado
//    }

    fun setEquipos(equipos: List<Equipo>) {
        // No necesitas hacer nada aquí, ya que no tienes _equipos
    }

    fun setObras(obras: List<Obra>) {
        // No necesitas hacer nada aquí, ya que no tienes _obras
    }


}