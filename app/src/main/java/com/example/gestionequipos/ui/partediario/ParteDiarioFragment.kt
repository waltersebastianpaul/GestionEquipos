package com.example.gestionequipos.ui.partediario

import android.content.Context
import android.content.res.ColorStateList
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionequipos.R
import com.example.gestionequipos.databinding.FragmentParteDiarioBinding
import java.util.Locale
import com.example.gestionequipos.data.ParteDiario
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.Date

class ParteDiarioFragment : Fragment() {

    private var _binding: FragmentParteDiarioBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ParteDiarioViewModel by viewModels()
    private var isDatePickerOpen = false // Variable para controlar si el DatePicker está abierto

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentParteDiarioBinding.inflate(inflater, container, false)
        val view = binding.root

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

        val guardarButton: Button = binding.guardarButton
        val nuevoParteButton: Button = binding.nuevoParteButton

        val fab: FloatingActionButton = requireActivity().findViewById(R.id.fab)
        fab.visibility = View.GONE

        // TextWatcher para calcular horas trabajadas
        val horasTextWatcher = object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                calcularHorasTrabajadas()
            }
        }

        // TextWatcher para fechaEditText
        fechaEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (fechaTextInputLayout.isErrorEnabled) { // Verifica si ya existe un error
                    if (s.isNullOrEmpty()) {
                        fechaTextInputLayout.error = "Campo requerido"
                    } else {
                        fechaTextInputLayout.isErrorEnabled = false // Borra el error si el campo ya no está vacío
                    }
                }
            }
        })

        // TextWatcher para equipoAutocomplete
        equipoAutocomplete.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (equipoTextInputLayout.isErrorEnabled) {
                    if (s.isNullOrEmpty()){
                        equipoTextInputLayout.error = "Campo requerido"
                    } else {
                        equipoTextInputLayout.isErrorEnabled = false
                    }
                }
            }
        })

        // TextWatcher para horasInicioEditText
        horasInicioEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (horasInicioTextInputLayout.isErrorEnabled) {
                    if (s.isNullOrEmpty()) {
                        horasInicioTextInputLayout.error = "Campo requerido"
                    } else {
                        horasInicioTextInputLayout.isErrorEnabled = false}
                }
                calcularHorasTrabajadas()
            }
        })

        // TextWatcher para horasFinEditText
        horasFinEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (horasFinTextInputLayout.isErrorEnabled) {
                    if (s.isNullOrEmpty()) {
                        horasFinTextInputLayout.error = "Campo requerido"
                    } else {
                        horasFinTextInputLayout.isErrorEnabled = false
                    }
                }
                calcularHorasTrabajadas()
            }
        })

        // TextWatcher para obraAutocomplete
        obraAutocomplete.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (obraTextInputLayout.isErrorEnabled) {
                    if (s.isNullOrEmpty()) {
                        obraTextInputLayout.error = "Campo requerido"
                    } else {
                        obraTextInputLayout.isErrorEnabled = false
                    }
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
        fechaEditText.setOnClickListener {
            if (!isDatePickerOpen) { // Verifica si el DatePicker ya está abierto
                isDatePickerOpen = true // Marca el DatePicker como abierto

                // Obtiene el InputMethodManager
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager

                // Cierra el teclado
                imm?.hideSoftInputFromWindow(fechaEditText.windowToken, 0)

                // Muestra el DatePicker
                val datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Selecciona una fecha")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build()

                datePicker.addOnPositiveButtonClickListener { selection ->
                    // Formatea la fecha seleccionada y actualiza el EditText
                    val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val fechaFormateada = simpleDateFormat.format(Date(selection))
                    fechaEditText.setText(fechaFormateada)
                }

                datePicker.addOnDismissListener {
                    isDatePickerOpen = false // Marca el DatePicker como cerrado al cerrarse
                }

                datePicker.show(parentFragmentManager, "datePicker")
            }
        }

        // Referencia al RecyclerView
        val recyclerView: RecyclerView = binding.partesDiariosRecyclerView

        // Crea una instancia del adaptador
        val adapter = ParteDiarioAdapter(emptyList()) // Inicialmente la lista está vacía

        // Configura el RecyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Observa los partes diarios del ViewModel y actualiza el adaptador
        viewModel.partesDiarios.observe(viewLifecycleOwner) { partesDiarios ->
            // Crea una nueva instancia del adaptador con la nueva lista
            val adapter = ParteDiarioAdapter(partesDiarios)
            recyclerView.adapter = adapter
            adapter.notifyDataSetChanged()
        }

        // Listener para el botón guardar
        guardarButton.setOnClickListener {
            val selectedEquipoText = binding.equipoAutocomplete.text.toString()
            val equipoInterno = selectedEquipoText.split(" - ").firstOrNull() ?: ""
            val selectedEquipo = viewModel.equipos.value?.find { it.interno.trim().lowercase() == equipoInterno.trim().lowercase() }

            val selectedObraText = binding.obraAutocomplete.text.toString()
            val obraCentroCosto = selectedObraText.split(" - ").firstOrNull() ?: ""
            val selectedObra = viewModel.obras.value?.find { it.centro_costo.trim().lowercase() == obraCentroCosto.trim().lowercase() }

            // Obtiene el ID del usuario del Intent
            val userId = requireActivity().intent.getIntExtra("id", -1) // -1 como valor predeterminado si no se encuentra

            if (validarCampos()) {
                // Guardar los datos
                val parteDiario = ParteDiario(
                    fecha = fechaEditText.text.toString(),
                    equipoId = selectedEquipo?.id ?: 0, // Usar operador de acceso seguro o valor predeterminado
                    horasInicio = horasInicioEditText.text.toString().toInt(),
                    horasFin = horasFinEditText.text.toString().toInt(),
                    horasTrabajadas = horasTrabajadasEditText.text.toString().toInt(),
                    observaciones = observacionesEditText.text.toString(),
                    obraId = selectedObra?.id ?: 0,
                    userCreated = userId, // Usa el ID del usuario obtenido
                    estadoId = 1 // "Eatado:Activo" // selectedEstado?.id ?: 0
                )
                viewModel.guardarParteDiario(parteDiario)
                deshabilitarFormulario()
                fab.visibility = View.VISIBLE
            }

        }

        fab.setImageResource(R.drawable.ic_add)
        fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.blue)))
        fab.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white)))

        fab.setOnClickListener {
            limpiarFormulario()
        }

        // Listener para el botón guardar
        nuevoParteButton.setOnClickListener {
            limpiarFormulario()
        }

        // Inicia la carga de datos desde el ViewModel
        viewModel.cargarDatos()
    }

    private fun limpiarFormulario() {
        binding.fechaEditText.text?.clear()
        binding.equipoAutocomplete.text?.clear()
        binding.obraAutocomplete.text?.clear()
        binding.horasInicioEditText.text?.clear()
        binding.horasFinEditText.text?.clear()
        binding.observacionesEditText.text?.clear()

        binding.fechaEditText.isEnabled = true
        binding.equipoAutocomplete.isEnabled = true
        binding.horasInicioEditText.isEnabled = true
        binding.horasFinEditText.isEnabled = true
        binding.observacionesEditText.isEnabled = true
        binding.obraAutocomplete.isEnabled = true
        binding.guardarButton.isEnabled = true
        // Habilita el ícono del calendario
        binding.fechaTextInputLayout.isEndIconVisible = true
    }

    private fun deshabilitarFormulario() {
        binding.fechaEditText.isEnabled = false
        binding.equipoAutocomplete.isEnabled = false
        binding.horasInicioEditText.isEnabled = false
        binding.horasFinEditText.isEnabled = false
        binding.observacionesEditText.isEnabled = false
        binding.obraAutocomplete.isEnabled = false
        binding.guardarButton.isEnabled = false
        // Deshabilita el ícono del calendario
        binding.fechaTextInputLayout.isEndIconVisible = false
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

        if (!camposValidos) {
            Toast.makeText(requireContext(), "Por favor, complete todos los campos requeridos", Toast.LENGTH_SHORT).show()
            // O puedes usar un AlertDialog para mostrar la advertencia
        }

        return camposValidos
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