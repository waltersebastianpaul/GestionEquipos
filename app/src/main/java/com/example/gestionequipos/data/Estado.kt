package com.example.gestionequipos.data

data class Estado(val id: Int, val nombre: String) {
    override fun toString(): String {
        return nombre
    }
}