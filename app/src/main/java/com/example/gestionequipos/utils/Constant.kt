package com.example.gestionequipos.utils

sealed class Constants {
    companion object {
        private const val BASE_URL = "http://adminobr.site/"
        fun getBaseUrl(): String {
            return BASE_URL
        }
    }

    object PartesDiarios {
        const val GET_LISTA = "gestionequipos/get_partes_diarios.php"
        const val GUARDAR = "gestionequipos/guardar_parte_diario.php"
        // ... otras rutas de obras si es necesario
    }

    object Equipos {
        const val GET_LISTA = "gestionequipos/get_equipos.php"
        // ... otras rutas de obras si es necesario
    }

    object Obras {
        const val GET_LISTA = "gestionequipos/get_obras.php"
        // ... otras rutas de obras si es necesario
    }
}
