package com.example.gestionequipos.application

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.example.gestionequipos.ui.appdata.AppDataViewModel
import com.example.gestionequipos.utils.EmailSender

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

// Inicio - codigo para manejo de errores y reporte via mail (JavaMail)
        // Configurar el manejador de excepciones
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            // Construir el reporte de error
            val report = StringBuilder()
            report.append("Error en la aplicación:\n")
            report.append("Thread: ${thread.name}\n")
            report.append("Excepción: ${exception.message}\n")
            report.append("Traza de la pila:\n")
            for (element in exception.stackTrace) {
                report.append("$element\n")
            }

            // Enviar el reporte por correo electrónico usando la clase EmailSender (JavaMail)
            val emailSender = EmailSender()
            emailSender.sendEmail(report.toString())

            // Llamar al manejador de excepciones por defecto
            defaultHandler?.uncaughtException(thread, exception)
        }

    }
// Fin - codigo para manejo de errores y reporte via mail (JavaMail)

    override fun onTerminate() {
        super.onTerminate()
        viewModelStore.clear()
    }
}