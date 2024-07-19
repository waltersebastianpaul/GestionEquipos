package com.example.gestionequipos.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gestionequipos.MainActivity
import com.example.gestionequipos.api.ApiService
import com.example.gestionequipos.databinding.ActivityLoginBinding
import com.example.gestionequipos.api.ApiUtils
import com.example.gestionequipos.data.ErrorResponse
import com.example.gestionequipos.data.LoginRequest
import com.example.gestionequipos.data.LoginResponse
import com.example.gestionequipos.utils.ProgressDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import retrofit2.Response
import java.net.UnknownHostException

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val apiService: ApiService by lazy { ApiUtils.getApiService() }

    private lateinit var usuarioEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var usuarioTextInputLayout: TextInputLayout
    private lateinit var passwordTextInputLayout: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        usuarioEditText = binding.usuarioEditText
        passwordEditText = binding.passwordEditText
        usuarioTextInputLayout = binding.usuarioTextInputLayout
        passwordTextInputLayout = binding.passwordTextInputLayout

        // TextWatcher para usuarioEditText
        usuarioEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (usuarioTextInputLayout.isErrorEnabled) { // Verifica si ya existe un error
                    if (s.isNullOrEmpty()) {
                        usuarioTextInputLayout.error = "Campo requerido"
                    } else {
                        usuarioTextInputLayout.isErrorEnabled = false // Borra el error si el campo ya no está vacío
                    }
                }
            }
        })

        // TextWatcher para passwordEditText
        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (passwordTextInputLayout.isErrorEnabled) { // Verifica si ya existe un error
                    if (s.isNullOrEmpty()) {
                        passwordTextInputLayout.error = "Campo requerido"
                    } else {
                        passwordTextInputLayout.isErrorEnabled = false // Borra el error si el campo ya no está vacío
                    }
                }
            }
        })

        // Listener para el botón de login
        binding.loginButton.setOnClickListener {
            val usuario = usuarioEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (validarCampos()) {
                val progressDialog = ProgressDialogFragment.show(supportFragmentManager) // Mostrar ProgressDialog
                lifecycleScope.launch {
                    try {
                        val loginRequest = LoginRequest(usuario, password)
                        val requestBody = Gson().toJson(loginRequest)
                            .toRequestBody("application/json".toMediaTypeOrNull())
                        val response = apiService.login(requestBody)

                        handleLoginResponse(response)

                    } catch (e: HttpException) {
                        // Error de red
                        Log.e("LoginActivity", "Error de red: ${e.message}")
                        Toast.makeText(this@LoginActivity, "Error de red", Toast.LENGTH_SHORT).show()
                    } catch (e: UnknownHostException) {
                        // Error de conexión a internet
                        Log.e("LoginActivity", "Error de conexión a internet: ${e.message}")
                        Toast.makeText(this@LoginActivity, "No hay conexión a internet", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        // Otro error
                        Log.e("LoginActivity", "Error en la autenticación", e)
                        Toast.makeText(this@LoginActivity, "Error en la autenticación", Toast.LENGTH_SHORT).show()
                    } finally {
                        progressDialog.dismiss() // Ocultar ProgressDialog
                    }
                }
            }
        }
    }

    private fun handleLoginResponse(response: Response<LoginResponse>) {
        if (response.isSuccessful) {
            val loginResponse = response.body()
            Log.d("LoginActivity", "Respuesta del servidor: $loginResponse")

            if (loginResponse != null) {
                if (loginResponse.success) {
                    // Inicio de sesión exitoso
                    Log.d("LoginActivity", "Inicio de sesión exitoso")
                    // Inicia la actividad principal
                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra("id", loginResponse.user.id) // Agrega el id como extra
                        putExtra("nombre", loginResponse.user.nombre) // Agrega el nombre como extra
                        putExtra("email", loginResponse.user.email) // Agrega el email como extra
                        putStringArrayListExtra("rol", ArrayList(loginResponse.user.rol))
                    }
                    startActivity(intent)
                    finish() // Cierra la actividad de login
                } else {
                    // Error de autenticación
                    val errorMessage = loginResponse.message ?: "Error de autenticación desconocido"
                    Log.e("LoginActivity", "Error de autenticación: $errorMessage")
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.e("LoginActivity", "Respuesta del servidor nula")
                Toast.makeText(this, "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Error en la respuesta del servidor
            val errorBody = response.errorBody()?.string()
            val errorMessage = parseErrorMessage(errorBody)
            Log.e("LoginActivity", "Error en la respuesta del servidor: ${response.code()} - $errorMessage")
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun parseErrorMessage(errorBody: String?): String {
        return if (errorBody.isNullOrEmpty()) {
            "Error en la respuesta del servidor"
        } else {
            try {
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                errorResponse.message ?: "Error en la respuesta del servidor"
            } catch (e: Exception) {
                "Error en la respuesta del servidor"
            }
        }
    }

    private fun validarCampos(): Boolean {
        var camposValidos = true

        if (usuarioEditText.text.isNullOrEmpty()) {
            usuarioTextInputLayout.error = "Campo requerido"
            usuarioTextInputLayout.isErrorEnabled = true
            camposValidos = false
        } else {
            usuarioTextInputLayout.isErrorEnabled = false
        }

        if (passwordEditText.text.isNullOrEmpty()) {
            passwordTextInputLayout.error = "Campo requerido"
            passwordTextInputLayout.isErrorEnabled = true
            camposValidos = false
        } else {
            passwordTextInputLayout.isErrorEnabled = false
        }

        if (!camposValidos) {
            Toast.makeText(this, "Por favor, complete todos los campos requeridos", Toast.LENGTH_SHORT).show()
        }

        return camposValidos
    }
}


//package com.example.gestionequipos.auth
//
//import android.content.Intent
//import android.os.Bundle
//import android.text.Editable
//import android.text.TextWatcher
//import android.util.Log
//import android.widget.Toast
//import com.google.android.material.snackbar.Snackbar
//import androidx.appcompat.app.AppCompatActivity
//import androidx.lifecycle.lifecycleScope
//import com.example.gestionequipos.MainActivity
//import com.example.gestionequipos.api.ApiService
//import com.example.gestionequipos.databinding.ActivityLoginBinding
//import com.example.gestionequipos.api.ApiUtils
//import com.example.gestionequipos.data.ErrorResponse
//import com.example.gestionequipos.data.LoginRequest
//import com.example.gestionequipos.data.LoginResponse
//import com.google.android.material.textfield.TextInputEditText
//import com.google.android.material.textfield.TextInputLayout
//import com.google.gson.Gson
//import kotlinx.coroutines.launch
//import okhttp3.MediaType.Companion.toMediaTypeOrNull
//import okhttp3.RequestBody.Companion.toRequestBody
//import retrofit2.HttpException
//import retrofit2.Response
//import java.net.UnknownHostException
//
//class LoginActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityLoginBinding
//    private val apiService: ApiService by lazy { ApiUtils.getApiService() }
//
//    private lateinit var usuarioEditText: TextInputEditText
//    private lateinit var passwordEditText: TextInputEditText
//    private lateinit var usuarioTextInputLayout: TextInputLayout
//    private lateinit var passwordTextInputLayout: TextInputLayout
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityLoginBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        usuarioEditText = binding.usuarioEditText
//        passwordEditText = binding.passwordEditText
//        usuarioTextInputLayout = binding.usuarioTextInputLayout
//        passwordTextInputLayout = binding.passwordTextInputLayout
//
//        // TextWatcher para usuarioEditText
//        usuarioEditText.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//
//            override fun afterTextChanged(s: Editable?) {
//                if (usuarioTextInputLayout.isErrorEnabled) { // Verifica si ya existe un error
//                    if (s.isNullOrEmpty()) {
//                        usuarioTextInputLayout.error = "Campo requerido"
//                    } else {
//                        usuarioTextInputLayout.isErrorEnabled = false // Borra el error si el campo ya no está vacío
//                    }
//                }
//            }
//        })
//
//
//        // TextWatcher para passwordEditText
//        passwordEditText.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//
//            override fun afterTextChanged(s: Editable?) {
//                if (passwordTextInputLayout.isErrorEnabled) { // Verifica si ya existe un error
//                    if (s.isNullOrEmpty()) {
//                        passwordTextInputLayout.error = "Campo requerido"
//                    } else {
//                        passwordTextInputLayout.isErrorEnabled = false // Borra el error si el campo ya no está vacío
//                    }
//                }
//            }
//        })
//
//        // Listener para el botón de login
//        binding.loginButton.setOnClickListener {
//            val usuario = usuarioEditText.text.toString()
//            val password = passwordEditText.text.toString()
//
//            if (validarCampos()) {
//                lifecycleScope.launch {
//                    try {
//                        val loginRequest = LoginRequest(usuario, password)
//                        val requestBody = Gson().toJson(loginRequest)
//                            .toRequestBody("application/json".toMediaTypeOrNull())
//                        val response = apiService.login(requestBody)
//
//                        handleLoginResponse(response)
//
//                    } catch (e: HttpException) {
//                        // Error de red
//                        Log.e("LoginActivity", "Error de red: ${e.message}")
//                        Toast.makeText(this@LoginActivity, "Error de red", Toast.LENGTH_SHORT).show()
//                    } catch (e: UnknownHostException) {
//                        // Error de conexión a internet
//                        Log.e("LoginActivity", "Error de conexión a internet: ${e.message}")
//                        Toast.makeText(this@LoginActivity, "No hay conexión a internet", Toast.LENGTH_SHORT).show()
//                    } catch (e: Exception) {
//                        // Otro error
//                        Log.e("LoginActivity", "Error en la autenticación", e)
//                        Toast.makeText(this@LoginActivity, "Error en la autenticación", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//        }
//    }
//
//    private fun handleLoginResponse(response: Response<LoginResponse>) {
//        if (response.isSuccessful) {
//            val loginResponse = response.body()
//            Log.d("LoginActivity", "Respuesta del servidor: $loginResponse")
//
//            if (loginResponse != null) {
//                if (loginResponse.success) {
//                    // Inicio de sesión exitoso
//                    Log.d("LoginActivity", "Inicio de sesión exitoso")
//                    // Inicia la actividad principal
//                    val intent = Intent(this, MainActivity::class.java).apply {
//                        putExtra("id", loginResponse.user.id) // Agrega el id como extra
//                        putExtra("nombre", loginResponse.user.nombre) // Agrega el nombre como extra
//                        putExtra("email", loginResponse.user.email) // Agrega el email como extra
//                        putStringArrayListExtra("rol", ArrayList(loginResponse.user.rol))
//                    }
//                    startActivity(intent)
//                    finish() // Cierra la actividad de login
//                } else {
//                    // Error de autenticación
//                    val errorMessage = loginResponse.message ?: "Error de autenticación desconocido"
//                    Log.e("LoginActivity", "Error de autenticación: $errorMessage")
//                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
//                }
//            } else {
//                Log.e("LoginActivity", "Respuesta del servidor nula")
//                Toast.makeText(this, "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show()
//            }
//        } else {
//            // Error en la respuesta del servidor
//            val errorBody = response.errorBody()?.string()
//            val errorMessage = parseErrorMessage(errorBody)
//            Log.e("LoginActivity", "Error en la respuesta del servidor: ${response.code()} - $errorMessage")
//            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun parseErrorMessage(errorBody: String?): String {
//        return if (errorBody.isNullOrEmpty()) {
//            "Error en la respuesta del servidor"
//        } else {
//            try {
//                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
//                errorResponse.message ?: "Error en la respuesta del servidor"
//            } catch (e: Exception) {
//                "Error en la respuesta del servidor"
//            }
//        }
//    }
//
//    private fun validarCampos(): Boolean {
//        var camposValidos = true
//
//        if (usuarioEditText.text.isNullOrEmpty()) {
//            usuarioTextInputLayout.error = "Campo requerido"
//            usuarioTextInputLayout.isErrorEnabled = true
//            camposValidos = false
//        } else {
//            usuarioTextInputLayout.isErrorEnabled = false
//        }
//
//        if (passwordEditText.text.isNullOrEmpty()) {
//            passwordTextInputLayout.error = "Campo requerido"
//            passwordTextInputLayout.isErrorEnabled = true
//            camposValidos = false
//        } else {
//            passwordTextInputLayout.isErrorEnabled = false
//        }
//
//        if (!camposValidos) {
//            Toast.makeText(this, "Por favor, complete todos los campos requeridos", Toast.LENGTH_SHORT).show()
//        }
//
//        return camposValidos
//    }
//}


// viejo //


//package com.example.gestionequipos.auth
//
//import android.content.Intent
//import android.os.Bundle
//import android.text.Editable
//import android.text.TextWatcher
//import android.util.Log
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.lifecycle.lifecycleScope
//import com.example.gestionequipos.MainActivity
//import com.example.gestionequipos.api.ApiService
//import com.example.gestionequipos.databinding.ActivityLoginBinding
//import com.example.gestionequipos.api.ApiUtils
//import com.example.gestionequipos.data.LoginRequest
//import com.google.android.material.textfield.TextInputEditText
//import com.google.android.material.textfield.TextInputLayout
//import com.google.gson.Gson
//import kotlinx.coroutines.launch
//import okhttp3.MediaType.Companion.toMediaTypeOrNull
//import okhttp3.RequestBody.Companion.toRequestBody
//import retrofit2.HttpException
//
//class LoginActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityLoginBinding
//    private val apiService: ApiService by lazy { ApiUtils.getApiService() }
//
//    private lateinit var usuarioEditText: TextInputEditText
//    private lateinit var passwordEditText: TextInputEditText
//    private lateinit var usuarioTextInputLayout: TextInputLayout
//    private lateinit var passwordTextInputLayout: TextInputLayout
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityLoginBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        usuarioEditText = binding.usuarioEditText
//        passwordEditText = binding.passwordEditText
//        usuarioTextInputLayout = binding.usuarioTextInputLayout
//        passwordTextInputLayout = binding.passwordTextInputLayout
//
//        // TextWatcher para usuarioEditText
//        usuarioEditText.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//
//            override fun afterTextChanged(s: Editable?) {
//                if (s.isNullOrEmpty()) {
//                    usuarioTextInputLayout.error = "Campo requerido"
//                    usuarioTextInputLayout.isErrorEnabled = true
//                } else {
//                    usuarioTextInputLayout.isErrorEnabled = false
//                }
//            }
//        })
//
//
//        // TextWatcher para passwordEditText
//        passwordEditText.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//
//            override fun afterTextChanged(s: Editable?) {
//                if (s.isNullOrEmpty()) {
//                    passwordTextInputLayout.error = "Campo requerido"
//                    passwordTextInputLayout.isErrorEnabled = true
//                } else {
//                    passwordTextInputLayout.isErrorEnabled = false
//                }
//            }
//        })
//
//        // Listener para el botón de login
//        binding.loginButton.setOnClickListener {
//
//            val usuario = usuarioEditText.text.toString()
//            val password = passwordEditText.text.toString()
//
//            if (validarCampos()) {
//                val contextoActivity = this // Almacena el contexto de la actividad
//                lifecycleScope.launch {
//                    try {
//                        val loginRequest = LoginRequest(usuario, password)
//                        val requestBody = Gson().toJson(loginRequest)
//                            .toRequestBody("application/json".toMediaTypeOrNull())
//                        val response = apiService.login(requestBody)
//
//                        if (response.isSuccessful) {
//                            val loginResponse = response.body()
//                            Log.d("LoginActivity", "Respuesta del servidor: $loginResponse")
//
//                            if (loginResponse != null) {
//                                if (loginResponse.success) {
//                                    // Inicio de sesión exitoso
//                                    Log.d("LoginActivity", "Inicio de sesión exitoso")
//                                    // Inicia la actividad principal
//                                    val intent = Intent(contextoActivity, MainActivity::class.java)
//                                    intent.putExtra("id", loginResponse.user.id) // Agrega el id como extra
//                                    intent.putExtra("nombre", loginResponse.user.nombre) // Agrega el nombre como extra
//                                    intent.putExtra("email", loginResponse.user.email) // Agrega el email como extra
//                                    intent.putStringArrayListExtra(
//                                        "rol",
//                                        ArrayList(loginResponse.user.rol)
//                                    )
//                                    startActivity(intent)
//                                    finish() // Cierra la actividad de login
//
//                                } else {
//                                    // Error de autenticación
//                                    val errorMessage = loginResponse.message
//                                    Log.e("LoginActivity", "Error de autenticación: $errorMessage")
//                                    Toast.makeText(
//                                        this@LoginActivity, // Usa el contexto de la actividad
//                                        errorMessage,
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                }
//                            } else {
//                                Log.e("LoginActivity", "Respuesta del servidor nula")
//                                Toast.makeText(
//                                    applicationContext, // Usa "applicationContext" como contexto
//                                    "Error en la respuesta del servidor",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            }
//                        } else {
//                            // Error en la respuesta del servidor
//                            val errorBody = response.errorBody()?.string()
//                            Log.e(
//                                "LoginActivity",
//                                "Error en la respuesta del servidor: ${response.code()} - $errorBody"
//                            )
//                            Toast.makeText(
//                                this@LoginActivity, // Usa el contexto de la actividad
//                                "Error en la respuesta del servidor",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                    } catch (e: HttpException) {
//                        // Error de red
//                        Log.e("LoginActivity", "Error de red: ${e.message()}")
//                        Toast.makeText(
//                            this@LoginActivity,
//                            "Error de red",
//                            Toast.LENGTH_SHORT).show()
//                    } catch (e: Exception) {
//                        // Otro error
//                        Log.e("LoginActivity", "Error en la autenticación", e)
//                        Toast.makeText(
//                            this@LoginActivity,
//                            "Error en la autenticación",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//            }
//        }
//    }
//
//    private fun validarCampos(): Boolean {
//        var camposValidos = true
//
//        if (usuarioEditText.text.isNullOrEmpty()) {
//            usuarioTextInputLayout.error = "Campo requerido"
//            usuarioTextInputLayout.isErrorEnabled = true
//            camposValidos = false
//        } else {
//            usuarioTextInputLayout.isErrorEnabled = false
//        }
//
//        if (passwordEditText.text.isNullOrEmpty()) {
//            passwordTextInputLayout.error = "Campo requerido"
//            passwordTextInputLayout.isErrorEnabled = true
//            camposValidos = false
//        } else {
//            passwordTextInputLayout.isErrorEnabled = false
//        }
//
//        if (!camposValidos) {
//            Toast.makeText(this, "Por favor, complete todos los campos requeridos", Toast.LENGTH_SHORT).show()
//        }
//
//        return camposValidos
//    }
//}
