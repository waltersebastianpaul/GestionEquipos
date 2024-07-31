package com.example.gestionequipos.ui.appdata

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.gestionequipos.data.Equipo
import com.example.gestionequipos.data.Obra
import com.example.gestionequipos.data.remote.GestionEquiposApi
import kotlinx.coroutines.launch

class AppDataViewModel(application: Application) : AndroidViewModel(application) {

    private val _equipos = MutableLiveData<List<Equipo>>()
    val equipos: LiveData<List<Equipo>> = _equipos

    private val _obras = MutableLiveData<List<Obra>>()
    val obras: LiveData<List<Obra>> = _obras

    private val gestionEquiposApi: GestionEquiposApi = GestionEquiposApi.create(application)

    fun cargarDatos() {
        viewModelScope.launch {
            try {
                _equipos.value = gestionEquiposApi.getEquipos()
                _obras.value = gestionEquiposApi.getObras()
            } catch (e: Exception) {
                Log.e("AppDataViewModel", "Error al cargar datos: ${e.message}")
                // Mostrar un mensaje de error al usuario
            }
        }
    }
}