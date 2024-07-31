package com.example.gestionequipos.ui.appdata

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.gestionequipos.data.local.PrefsHelper
import com.example.gestionequipos.data.Equipo
import com.example.gestionequipos.data.Obra
import com.example.gestionequipos.data.repository.GestionEquiposRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class AppDataViewModel(application: Application) : AndroidViewModel(application) {private val repository = GestionEquiposRepository(application)

    private val _equipos = MutableLiveData<List<Equipo>>()
    val equipos: LiveData<List<Equipo>> = _equipos

    private val _obras = MutableLiveData<List<Obra>>()
    val obras: LiveData<List<Obra>> = _obras

    private val prefsHelper = PrefsHelper(application)
    private val gson = Gson()

    fun cargarDatosIniciales() {
        viewModelScope.launch {
            try {
                val equiposCache = cargarEquiposDesdeCache()
                val obrasCache = cargarObrasDesdeCache()

                Log.d("AppDataViewModel", "Datos cargados desde la caché: equipos = $equiposCache, obras = $obrasCache")

                if (equiposCache != null && obrasCache != null) {
                    _equipos.value = equiposCache ?: emptyList() // Asignar lista vacía si es null
                    _obras.value = obrasCache ?: emptyList() // Asignar lista vacía si es null
                } else {
                    val equipos = repository.getEquipos()
                    val obras = repository.getObras()

                    Log.d("AppDataViewModel", "Datos cargados desde la API: equipos = $equipos, obras = $obras")

                    guardarEquiposEnCache(equipos)
                    guardarObrasEnCache(obras)

                    _equipos.value = equipos
                    _obras.value = obras
                }
            } catch (e: Exception) {
                Log.e("AppDataViewModel", "Error al cargar datos: ${e.message}", e)
                // Manejar errores de carga
                // Puedes mostrar un mensaje de error o intentar recargar los datos
            }
        }
    }

    private fun cargarEquiposDesdeCache(): List<Equipo>? {
        val equiposJson = prefsHelper.getEquipos()
        return if (equiposJson != null) {
            val type = object : TypeToken<List<Equipo>>() {}.type
            gson.fromJson(equiposJson, type)
        } else {
            null
        }
    }

    private fun cargarObrasDesdeCache(): List<Obra>? {
        val obrasJson = prefsHelper.getObras()
        return if (obrasJson != null) {
            val type = object : TypeToken<List<Obra>>() {}.type
            gson.fromJson(obrasJson, type)
        } else {
            null
        }
    }

    private fun guardarEquiposEnCache(equipos: List<Equipo>) {
        val equiposJson = gson.toJson(equipos)
        prefsHelper.saveEquipos(equiposJson)
    }

    private fun guardarObrasEnCache(obras: List<Obra>) {
        val obrasJson = gson.toJson(obras)
        prefsHelper.saveObras(obrasJson)
    }
}

class PrefsHelper(private val application: Application) {

    private val sharedPreferences = application.getSharedPreferences("app_data", 0)

    fun saveEquipos(equiposJson: String) {
        sharedPreferences.edit().putString("equipos", equiposJson).apply()
    }

    fun getEquipos(): String? {
        return sharedPreferences.getString("equipos", null)
    }

    fun saveObras(obrasJson: String) {
        sharedPreferences.edit().putString("obras", obrasJson).apply()
    }

    fun getObras(): String? {
        return sharedPreferences.getString("obras", null)
    }
}