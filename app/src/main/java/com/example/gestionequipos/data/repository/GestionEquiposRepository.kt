package com.example.gestionequipos.data.repository

import android.app.Application
import com.example.gestionequipos.data.remote.GestionEquiposApi
import com.example.gestionequipos.data.Equipo
import com.example.gestionequipos.data.Obra

class GestionEquiposRepository(application: Application) {
    private val api = GestionEquiposApi.create(application)

    suspend fun getEquipos(): List<Equipo> {
        return api.getEquipos()
    }

    suspend fun getObras(): List<Obra> {
        return api.getObras()
    }
}