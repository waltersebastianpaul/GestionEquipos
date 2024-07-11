package com.example.gestionequipos.data

data class ParteDiario(
    val id: Int = 0,val fecha: String,
    val equipoId: Int,
    val horasInicio: String,
    val horasFin: String,
    val horasTrabajadas: Double,
    val observaciones: String?,
    val obraId: Int,
    val userCreated: Int,
    val estadoId: Int
)