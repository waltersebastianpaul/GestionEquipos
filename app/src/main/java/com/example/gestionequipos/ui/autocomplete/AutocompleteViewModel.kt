package com.example.gestionequipos.ui.autocomplete

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.gestionequipos.data.Equipo
import com.example.gestionequipos.data.Obra
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AutocompleteViewModel(application: Application) : AndroidViewModel(application) {
    private val _obras = MutableLiveData<List<Obra>>()
    val obras: LiveData<List<Obra>> = _obras

    private val _equipos = MutableLiveData<List<Equipo>>()
    val equipos: LiveData<List<Equipo>> = _equipos

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        cargarDatos()
    }

    fun cargarDatos() {
        viewModelScope.launch {
            try {
                // Limpia las listas
                _obras.value = emptyList()
                _equipos.value = emptyList()

                Log.d("AutocompleteViewModel", "Cargando obras...")
                val obrasResult = withContext(Dispatchers.IO) {
                    try {
                        AutocompleteRepository.getObras(getApplication())
                    } catch (e: Exception) {
                        Log.e("AutocompleteViewModel", "Error al obtener obras: ${e.message}")
                        null
                    }
                }
                _obras.value = obrasResult ?: emptyList()
                Log.d("AutocompleteViewModel", "Obras cargadas: ${_obras.value}")

                Log.d("AutocompleteViewModel", "Cargando equipos...")
                val equiposResult = withContext(Dispatchers.IO) {
                    try {
                        AutocompleteRepository.getEquipos(getApplication())
                    } catch (e: Exception) {
                        Log.e("AutocompleteViewModel", "Error al obtener equipos: ${e.message}")
                        null
                    }
                }
                _equipos.value = equiposResult ?: emptyList()
                Log.d("AutocompleteViewModel", "Equipos cargados: ${_equipos.value}")

                if (_obras.value.isNullOrEmpty() || _equipos.value.isNullOrEmpty()) {
                    _error.value = "Error al cargar datos: obras o equipos están vacíos."
                } else {
                    _error.value = null
                }
            } catch (e: Exception) {
                Log.e("AutocompleteViewModel", "Error al cargar datos: ${e.message}")
                _error.value = "Error al cargar los datos: ${e.message}"
            }
        }
    }

}
