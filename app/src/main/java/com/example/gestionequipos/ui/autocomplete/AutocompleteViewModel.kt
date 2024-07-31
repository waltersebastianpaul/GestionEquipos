package com.example.gestionequipos.ui.autocomplete

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

class AutocompleteViewModel(application: Application) : AndroidViewModel(application) {

    private val _equipos = MutableLiveData<List<Equipo>>()
    val equipos: LiveData<List<Equipo>> =_equipos

    private val _obras = MutableLiveData<List<Obra>>()
    val obras: LiveData<List<Obra>> = _obras

    private val gestionEquiposApi: GestionEquiposApi = GestionEquiposApi.create(application)

    init {
        Log.d("AutocompleteViewModel", "Inicializando ViewModel")
        cargarDatos()
    }

    fun cargarDatos() {
        viewModelScope.launch {
            Log.d("AutocompleteViewModel", "Cargando datos...")
            try {
                val equipos = gestionEquiposApi.getEquipos()
                Log.d("AutocompleteViewModel", "Equipos cargados: $equipos")
                _equipos.value = equipos

                val obras = gestionEquiposApi.getObras()
                Log.d("AutocompleteViewModel", "Obras cargadas: $obras")
                _obras.value = obras
            } catch (e: Exception) {
                Log.e("AutocompleteViewModel", "Error al cargar datos: ${e.message}")
                // Mostrar un mensaje de error al usuario, por ejemplo, con un Toast o Snackbar
            }
        }
    }
}