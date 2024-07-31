package com.example.gestionequipos.data.local

import android.app.Application
import android.content.Context

class PrefsHelper(private val application: Application) {

    private val sharedPreferences = application.getSharedPreferences("app_data", Context.MODE_PRIVATE)

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