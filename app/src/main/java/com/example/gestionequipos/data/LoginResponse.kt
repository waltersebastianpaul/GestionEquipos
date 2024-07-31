package com.example.gestionequipos.data

data class LoginResponse(
    val success: Boolean,
    val message: String?,
    val user: User
)