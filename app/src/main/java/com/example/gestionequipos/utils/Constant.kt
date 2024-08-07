package com.example.gestionequipos.utils

object Constants {
    private const val BASE_URL = "http://adminobr.site/"
        fun getBaseUrl(): String {
        return BASE_URL
    }

    object PartesDiarios {
        const val GET_LISTA = "get_partes_diarios.php"
        const val GUARDAR = "guardar_parte_diario.php"
        // ... otras rutas de partes diarios
    }

    object Equipos {
        const val GET_LISTA = "get_equipos.php"
        // ... otras rutas de equipos
    }
    // ... otros grupos de rutas
}
//package com.example.gestionequipos.utils
//
//object Constants {
//    const val BASE_URL = "adminobr.site/"
//
//    object PartesDiarios {
//        const val GET_LISTA = "get_partes_diarios.php"
//        const val GUARDAR = "guardar_parte_diario.php"
//        // ... otras rutas de partes diarios
//    }
//
//    object Equipos {
//        const val GET_LISTA = "get_equipos.php"
//        // ... otras rutas de equipos
//    }
//    // ... otros grupos de rutas
//}