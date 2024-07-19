package com.example.gestionequipos.data

data class User(
    val id: Int,
    val nombre: String,
    val email: String,
    val rol: List<String>,
    val permisos: List<String>
)

// Para agregar algun otro dato del usuario, se debe agregar el campo
// no solo en la clase User, sino tambien el el Backend, en la API login.php


// Obtiene el ID del usuario del Intent
// Luego en cualquier parte

// String
// val email = intent.getStringExtra("email", "No hay email disponible")

// Integer
// val userId = requireActivity().intent.getIntExtra("id", -1) // -1 como valor predeterminado si no se encuentra


// val email = intent.getStringExtra("email") ?: "No hay email disponible"

// Desde Fragmento
// Importante, si se trabaja desde un Fragmento, usarrequireActivity().intent
// val userId = requireActivity().intent.getIntExtra("id", -1) // -1 como valor predeterminado si no se encuentra
