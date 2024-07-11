package com.example.gestionequipos.data

data class Obra(val id: Int, val centro_costo: String, val nombre: String) {
    override fun toString(): String {
        return "$centro_costo - $nombre"
    }
}