package com.example.gestionequipos

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.example.gestionequipos.ui.appdata.AppDataViewModel

class MyApplication : Application() {

    private val viewModelStore: ViewModelStore = ViewModelStore()

    private val viewModelStoreOwner = object : ViewModelStoreOwner {
        override val viewModelStore: ViewModelStore
            get() = this@MyApplication.viewModelStore
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("MyApplication", "onCreate: Iniciando aplicación")

        val appDataViewModel = ViewModelProvider(viewModelStoreOwner, ViewModelProvider.AndroidViewModelFactory(this)).get(AppDataViewModel::class.java)
        Log.d("MyApplication", "onCreate: ViewModel creado")

        appDataViewModel.cargarDatos()
        Log.d("MyApplication", "onCreate: Datos iniciales cargados")
    }

    override fun onTerminate() {
        super.onTerminate()
        viewModelStore.clear()
    }
}