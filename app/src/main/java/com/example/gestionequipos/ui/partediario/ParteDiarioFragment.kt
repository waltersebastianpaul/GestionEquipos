package com.example.gestionequipos.ui.partediario

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.gestionequipos.R
import com.example.gestionequipos.databinding.FragmentParteDiarioBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import okhttp3.*
import java.io.IOException
import java.util.Locale
import com.example.gestionequipos.data.ParteDiario
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ParteDiarioFragment : Fragment() {

    private var _binding: FragmentParteDiarioBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ParteDiarioViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentParteDiarioBinding.inflate(inflater, container, false)
        val view = binding.root

//        // Inicializa los AutoCompleteTextViews aquí
//        val equipoAutocomplete: AutoCompleteTextView = binding.equipoAutocomplete
//        val obraAutocomplete: AutoCompleteTextView = binding.obraAutocomplete
//        val estadoAutocomplete: AutoCompleteTextView = binding.estadoAutocomplete

        // Observa los datos del ViewModel y configura los adapters
        viewModel.equipos.observe(viewLifecycleOwner) { equipos ->
            val equipoStrings = equipos.map { "${it.interno} - ${it.descripcion}" }.toTypedArray()
            val adapterEquipos = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, equipoStrings)
            binding.equipoAutocomplete.setAdapter(adapterEquipos)
            Log.d("ParteDiario", "Equipos: $equipos")
        }

        viewModel.obras.observe(viewLifecycleOwner) { obras ->
            val obraStrings = obras.map { "${it.centro_costo} - ${it.nombre}" }.toTypedArray()
            val adapterObras = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, obraStrings)
            binding.obraAutocomplete.setAdapter(adapterObras)
            Log.d("ParteDiario", "Obras: $obras")
        }

        // Eliminar si no hay errores
//        viewModel.estados.observe(viewLifecycleOwner) { estados ->
//            // val estadoStrings = estados.map { "${it.nombre}" }.toTypedArray()
//            val estadoStrings = estados.map { it.nombre.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }.toTypedArray()
//            val adapterEstados = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, estadoStrings)
//            binding.estadoAutocomplete.setAdapter(adapterEstados)
//            Log.d("ParteDiario", "Estados: $estados")
//        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Referencias a los elementos del layout
        val fechaEditText: TextInputEditText = binding.fechaEditText
        val fechaTextInputLayout: TextInputLayout = binding.fechaTextInputLayout

        val equipoTextInputLayout: TextInputLayout = binding.equipoTextInputLayout
        val equipoAutocomplete: AutoCompleteTextView = binding.equipoAutocomplete

        val horasInicioEditText: EditText = binding.horasInicioEditText
        val horasInicioTextInputLayout: TextInputLayout = binding.horasInicioTextInputLayout

        val horasFinEditText: EditText = binding.horasFinEditText
        val horasFinTextInputLayout: TextInputLayout = binding.horasFinTextInputLayout

        val horasTrabajadasEditText: EditText = binding.horasTrabajadasEditText

        val observacionesEditText: EditText = binding.observacionesEditText

        val obraTextInputLayout: TextInputLayout = binding.obraTextInputLayout
        val obraAutocomplete: AutoCompleteTextView = binding.obraAutocomplete

        // Eliminar si no hay errores
//        val estadoAutocomplete: AutoCompleteTextView = binding.estadoAutocomplete

        val guardarButton: Button = binding.guardarButton

        // TextWatcher para calcular horas trabajadas
        val horasTextWatcher = object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                calcularHorasTrabajadas()
            }
        }

        fechaEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    fechaTextInputLayout.error = "Campo requerido"
                    fechaTextInputLayout.isErrorEnabled = true
                } else {
                    fechaTextInputLayout.isErrorEnabled = false
                }
            }
        })

        // TextWatcher para equipoAutocomplete
        equipoAutocomplete.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    equipoTextInputLayout.error = "Campo requerido"
                    equipoTextInputLayout.isErrorEnabled = true
                } else {
                    equipoTextInputLayout.isErrorEnabled= false
                }
            }
        })

        // TextWatcher para horasInicioEditText
        horasInicioEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    horasInicioTextInputLayout.error = "Campo requerido" // Asegúrate de tener un TextInputLayout para horasInicioEditText
                    horasInicioTextInputLayout.isErrorEnabled = true
                } else {
                    horasInicioTextInputLayout.isErrorEnabled = false
                }
                calcularHorasTrabajadas() // Llama a la función para actualizar las horas trabajadas
            }
        })

        // TextWatcher para horasFinEditText
        horasFinEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    horasFinTextInputLayout.error = "Campo requerido" // Asegúrate de tener un TextInputLayout para horasFinEditText
                    horasFinTextInputLayout.isErrorEnabled = true
                } else {
                    horasFinTextInputLayout.isErrorEnabled = false
                }
                calcularHorasTrabajadas() // Llama a la función para actualizar las horas trabajadas
            }
        })

        // TextWatcher para obraAutocomplete
        obraAutocomplete.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    obraTextInputLayout.error = "Campo requerido" // Asegúrate de tener un TextInputLayout para obraAutocomplete
                    obraTextInputLayout.isErrorEnabled = true
                } else {
                    obraTextInputLayout.isErrorEnabled = false
                }
            }
        })

        // Ejecuta la accion horasTextWatcher cada vez que se ingresa a horasInicioEditText o horasFinEditText
        horasInicioEditText.addTextChangedListener(horasTextWatcher)
        horasFinEditText.addTextChangedListener(horasTextWatcher)


        // Observa el LiveData para mensajes
        viewModel.mensaje.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { mensaje ->
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
            }
        }

        // Configura el DatePicker para fechaEditText
//        fechaEditText.setOnClickListener {
//            val calendar = Calendar.getInstance()
//            val year = calendar.get(Calendar.YEAR)
//            val month = calendar.get(Calendar.MONTH)
//            val day = calendar.get(Calendar.DAY_OF_MONTH)
//
//            val datePickerDialog = DatePickerDialog(
//                requireContext(),
//                { _, selectedYear, selectedMonth, dayOfMonth ->
//                    val formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, dayOfMonth)
//                    fechaEditText.setText(formattedDate)
//                },
//                year,
//                month,
//                day
//            )
//            datePickerDialog.show()
//        }


        // Configura el DatePicker para fechaEditText
        fechaEditText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Obtener la fechaactual del campo fechaEditText
                val fechaActual = fechaEditText.text.toString()
                val calendar = Calendar.getInstance()
                if (fechaActual.isNotBlank()) {
                    try {
                        val parts = fechaActual.split("/")
                        val dia = parts[0].toInt()
                        val mes = parts[1].toInt() - 1 // Los meses en Calendar van de 0 a 11
                        val anio = parts[2].toInt()
                        calendar.set(anio, mes, dia)
                    } catch (e: Exception) {
                        // Manejar elcaso en que la fecha no tenga el formato correcto
                    }
                }

                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                val datePickerDialog = DatePickerDialog(
                    requireActivity(), // Usar requireActivity() en lugar de requireContext()
                    { _: DatePicker, selectedYear: Int, selectedMonth: Int, dayOfMonth: Int ->
                        val formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, selectedMonth + 1, selectedYear)
                        fechaEditText.setText(formattedDate) // Especificar el tipo del argumento
                    },
                    year,
                    month,
                    day
                )
                datePickerDialog.show() // Llamar a show() en el objeto datePickerDialog
            } else if (!hasFocus && fechaEditText.text.toString().isNotBlank()) {
                // Validar la fecha cuando el campo pierde el foco y no está vacío
                if (!isValidDate(fechaEditText.text.toString())) {
                    fechaTextInputLayout.error ="Fecha inválida"
                    Toast.makeText(requireContext(), "Fecha inválida", Toast.LENGTH_SHORT).show()
                } else {
                    fechaTextInputLayout.error = null
                }
            }
        }

// ... (resto de tu código)

//        fechaEditText.setOnFocusChangeListener { _, hasFocus ->if (!hasFocus && fechaEditText.text.toString().isNotBlank()) {
//            if (!isValidDate(fechaEditText.text.toString())) {
//                fechaTextInputLayout.error = "Fecha inválida"
//                // Puedes mostrar un mensaje de alerta aquí si lo deseas, por ejemplo:
//                Toast.makeText(requireContext(), "Fecha inválida", Toast.LENGTH_SHORT).show()
//            } else {
//                fechaTextInputLayout.error = null
//            }
//        }
//        }

        // Eliminar si no hay errores
//        // Configura el comportamiento de estadoAutocomplete
//        estadoAutocomplete.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
//            if (hasFocus) {
//                estadoAutocomplete.showDropDown()
//            }
//        }

        // Observa los datos del ViewModel y actualiza los AutoCompleteTextViews
//        viewModel.equipos.observe(viewLifecycleOwner) { equipos ->
//            val adapterEquipos = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, equipos.map { it.interno })
//            equipoAutocomplete.setAdapter(adapterEquipos)
//            Log.d("ParteDiario", "Equipos: $equipos")
//        }

//        viewModel.obras.observe(viewLifecycleOwner) { obras ->
//            val adapterObras = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, obras.map { it.centro_costo })
//            obraAutocomplete.setAdapter(adapterObras)
//            Log.d("ParteDiario", "Obras: $obras")
//        }
//
//        viewModel.estados.observe(viewLifecycleOwner) { estados ->
//            val adapterEstados = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, estados.map { it.nombre })
//            estadoAutocomplete.setAdapter(adapterEstados)
//            Log.d("ParteDiario", "Estados: $estados")
//        }

        // Listener para el botón guardar
        guardarButton.setOnClickListener {
            val selectedEquipoText = binding.equipoAutocomplete.text.toString()
            val equipoInterno = selectedEquipoText.split(" - ").firstOrNull() ?: ""
            val selectedEquipo = viewModel.equipos.value?.find { it.interno.trim().lowercase() == equipoInterno.trim().lowercase() }

            val selectedObraText = binding.obraAutocomplete.text.toString()
            val obraCentroCosto = selectedObraText.split(" - ").firstOrNull() ?: ""
            val selectedObra = viewModel.obras.value?.find { it.centro_costo.trim().lowercase() == obraCentroCosto.trim().lowercase() }

//            val selectedEstadoText = binding.estadoAutocomplete.text.toString()
//            val estadoNombre = selectedEstadoText.split(" - ").firstOrNull() ?: ""
//            val selectedEstado = viewModel.estados.value?.find { it.nombre.trim().lowercase() == estadoNombre.trim().lowercase() }



            if (validarCampos()) {
                // Guardar los datos
                val parteDiario = ParteDiario(
                    fecha = fechaEditText.text.toString(),
                    equipoId = selectedEquipo?.id ?: 0, // Usar operador de acceso seguro o valor predeterminado
                    horasInicio = horasInicioEditText.text.toString(),
                    horasFin = horasFinEditText.text.toString(),
                    horasTrabajadas = horasTrabajadasEditText.text.toString().toDoubleOrNull() ?: 0.0,
                    observaciones = observacionesEditText.text.toString(),
                    obraId = selectedObra?.id ?: 0,
                    // Crear un adaptar para obtener el ID del usuario actual
                    userCreated = 1, // Adaptar para obtener el ID del usuario actual
                    estadoId = 1 // "Eatado:Activo" // selectedEstado?.id ?: 0
                )
                viewModel.guardarParteDiario(parteDiario)
            }

        }

        // Inicia la carga de datos desde el ViewModel
        viewModel.cargarDatos()
    }

    private fun validarCampos(): Boolean {
        var camposValidos = true

        if (binding.fechaEditText.text.isNullOrEmpty()) {
            binding.fechaTextInputLayout.error = "Campo requerido"
            binding.fechaTextInputLayout.isErrorEnabled = true
            camposValidos = false
        } else {
            binding.fechaTextInputLayout.isErrorEnabled = false
        }

        if (binding.equipoAutocomplete.text.isNullOrEmpty()) {
            binding.equipoTextInputLayout.error = "Campo requerido"
            binding.equipoTextInputLayout.isErrorEnabled = true
            camposValidos = false
        } else {binding.equipoTextInputLayout.isErrorEnabled = false
        }

        if (binding.horasInicioEditText.text.isNullOrEmpty()) {
            binding.horasInicioTextInputLayout.error = "Campo requerido"
            binding.horasInicioTextInputLayout.isErrorEnabled = true
            camposValidos = false
        } else {
            binding.horasInicioTextInputLayout.isErrorEnabled =false
        }

        if (binding.horasFinEditText.text.isNullOrEmpty()) {
            binding.horasFinTextInputLayout.error = "Campo requerido"
            binding.horasFinTextInputLayout.isErrorEnabled = true
            camposValidos = false
        } else {
            binding.horasFinTextInputLayout.isErrorEnabled = false
        }

        if (binding.obraAutocomplete.text.isNullOrEmpty()) {
            binding.obraTextInputLayout.error = "Campo requerido"
            binding.obraTextInputLayout.isErrorEnabled = true
            camposValidos = false
        } else {
            binding.obraTextInputLayout.isErrorEnabled = false
        }
        // Eliminar si no hay errores
        // Se comenta porque la idea es que el estado se ingrese como activo, predeterminada y automaticamente
//        if (binding.estadoAutocomplete.text.isNullOrEmpty()) {
//            binding.estadoTextInputLayout.error = "Campo requerido"
//            binding.estadoTextInputLayout.isErrorEnabled = true
//            camposValidos = false
//        } else {
//            binding.estadoTextInputLayout.isErrorEnabled = false
//        }

        if (!camposValidos) {
            Toast.makeText(requireContext(), "Por favor, complete todos los campos requeridos", Toast.LENGTH_SHORT).show()
            // O puedes usar un AlertDialog para mostrar la advertencia
        }

        return camposValidos
    }

    private fun isValidDate(fecha: String): Boolean {
        val parts = fecha.split("/")
        if (parts.size != 3) return false
        val dia = parts[0].toIntOrNull() ?:return false
        val mes = parts[1].toIntOrNull() ?: return false
        val anio = parts[2].toIntOrNull() ?: return false
        if (mes !in 1..12) return false
        if (dia !in 1..daysInMonth(mes, anio)) return false
        return true
    }

    private fun daysInMonth(mes: Int, anio: Int): Int {
        return when (mes) {
            2 -> if (isLeapYear(anio)) 29 else 28
            4, 6, 9,11 -> 30
            else -> 31
        }
    }

    private fun isLeapYear(year: Int): Boolean {
        return when {
            year % 400 == 0 -> true
            year % 100 == 0 -> false
            year % 4 == 0 -> true
            else -> false
        }
    }

    private fun calcularHorasTrabajadas() {
        val horasInicioText = binding.horasInicioEditText.text.toString()
        val horasFinText = binding.horasFinEditText.text.toString()

        if (horasInicioText.isNotEmpty() && horasFinText.isNotEmpty()) {
            try {
                val horasInicio = horasInicioText.toDouble()
                val horasFin = horasFinText.toDouble()
                val horasTrabajadas = (horasFin - horasInicio).toInt() // Convertir a entero
                binding.horasTrabajadasEditText.setText(horasTrabajadas.toString())
            } catch (e: NumberFormatException) {
                // Manejar el caso en que los valores no sean números válidos
                binding.horasTrabajadasEditText.setText("")}
        } else {
            // Si alguno de los campos está vacío, limpia horasTrabajadasEditText
            binding.horasTrabajadasEditText.setText("")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}