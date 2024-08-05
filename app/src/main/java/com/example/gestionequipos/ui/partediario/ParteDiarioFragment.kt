package com.example.gestionequipos.ui.partediario


import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.distinctUntilChanged
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionequipos.R
import com.example.gestionequipos.data.ParteDiario
import com.example.gestionequipos.databinding.FragmentParteDiarioBinding
import com.example.gestionequipos.ui.appdata.AppDataViewModel
import com.example.gestionequipos.ui.autocomplete.AutocompleteViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ParteDiarioFragment : Fragment() {

    private var _binding: FragmentParteDiarioBinding? = null
    private val binding get() = _binding!!

    private var isDatePickerOpen = false // Variable para controlar si el DatePicker está abierto
    private val autocompleteViewModel: AutocompleteViewModel by activityViewModels()
    private lateinit var fechaEditText: TextInputEditText

    private val viewModel: ParteDiarioViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentParteDiarioBinding.inflate(inflater, container, false)

        fechaEditText = binding.fechaEditText

        val view = binding.root

        val appDataViewModel: AppDataViewModel by activityViewModels()

        appDataViewModel.equipos.observe(viewLifecycleOwner) { equipos ->
            Log.d("ParteDiarioFragment", "Equipos recibidos en el fragmento: $equipos")
            val equipoStrings = equipos.map { "${it.interno} - ${it.descripcion}" }
            val adapterEquipos =ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, equipoStrings)
            binding.equipoAutocomplete.setAdapter(adapterEquipos)
        }

        appDataViewModel.obras.observe(viewLifecycleOwner) { obras ->
            Log.d("ParteDiarioFragment", "Obras recibidas en el fragmento: $obras")
            val obraStrings = obras.map { "${it.centro_costo} - ${it.nombre}" }
            val adapterObras = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, obraStrings)
            binding.obraAutocomplete.setAdapter(adapterObras)
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

        setEditTextToUppercase(equipoAutocomplete)
        setEditTextToUppercase(obraAutocomplete)

        binding.verPartesButton.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_listaPartesFragment)
        }

        // TextWatcher para calcular horas trabajadas
        val horasTextWatcher = object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                calcularHorasTrabajadas()
            }
        }

        // TextWatcher llamando la function addTextWatcher(textInputLayout: TextInputLayout, errorMessage: String)
        addTextWatcher(binding.fechaTextInputLayout, "Campo requerido")
        addTextWatcher(binding.equipoTextInputLayout, "Campo requerido")
        addTextWatcher(binding.horasInicioTextInputLayout, "Campo requerido")
        addTextWatcher(binding.horasFinTextInputLayout, "Campo requerido")
        addTextWatcher(binding.obraTextInputLayout, "Campo requerido")

        // Ejecuta la accion horasTextWatcher cada vez que se ingresa a horasInicioEditText o horasFinEditText
        horasInicioEditText.addTextChangedListener(horasTextWatcher)
        horasFinEditText.addTextChangedListener(horasTextWatcher)


        // Observa el LiveData para mensajes
        viewModel.mensaje.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { mensaje ->
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
            }
        }

        // Configura el OnClickListener para filtroFechaEditText
        fechaEditText.setOnClickListener {
            val locale = Locale.getDefault() // Crea un nuevo objeto Locale con el idioma español
            val calendar = Calendar.getInstance(locale) // Usa el locale para el Calendar

            // Obtener la fecha del EditText si está presente
            val dateString = fechaEditText.text.toString()
            if (dateString.isNotBlank()) {val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = formatter.parse(dateString)
                calendar.time = date
            }

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                    fechaEditText.setText(formattedDate)
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }

        val appDataViewModel: AppDataViewModel by activityViewModels()

        appDataViewModel.equipos.observe(viewLifecycleOwner) { equipos ->
            if (!equipos.isNullOrEmpty()) {
                val equipoStrings = equipos.map{ "${it.interno} - ${it.descripcion}" }
                val adapterEquipos = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, equipoStrings)
                binding.equipoAutocomplete.setAdapter(adapterEquipos)
            }
        }

        appDataViewModel.obras.observe(viewLifecycleOwner) { obras ->
            if (!obras.isNullOrEmpty()) {
                val obraStrings = obras.map { "${it.centro_costo} - ${it.nombre}" }
                val adapterObras = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, obraStrings)
                binding.obraAutocomplete.setAdapter(adapterObras)
            }
        }

        // Referencia al RecyclerView
        val recyclerView: RecyclerView = binding.partesDiariosRecyclerView

        // Listener para el botón guardar
        guardarButton.setOnClickListener {
            val selectedEquipoText = binding.equipoAutocomplete.text.toString()
            val equipoInterno = selectedEquipoText.split(" - ").firstOrNull() ?: ""
            val selectedEquipo = autocompleteViewModel.equipos.value?.find { it.interno.trim().equals(equipoInterno.trim(), ignoreCase = true) }

            val selectedObraText = binding.obraAutocomplete.text.toString()
            val obraCentroCosto = selectedObraText.split(" - ").firstOrNull() ?: ""
            val selectedObra = autocompleteViewModel.obras.value?.find { it.centro_costo.trim().equals(obraCentroCosto.trim(), ignoreCase = true) }

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
                    estadoId = 1 // "Estado:Activo"
                )

                viewModel.guardarParteDiario(parteDiario) { success -> // Agrega una lambda como callback
                    if (success) {
                        deshabilitarFormulario()
                        fab.visibility = View.VISIBLE
                    } else {
                        // Manejar el caso de error al guardar
                        Toast.makeText(requireContext(), "Error al guardar el parte diario", Toast.LENGTH_SHORT).show()
                    }
                }

                viewModel.mensaje.observe(viewLifecycleOwner) { event ->
                    event.getContentIfNotHandled()?.let { mensaje ->
                        // Mostrar el mensaje en un Toast o realizar otra acción
                        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        fab.setImageResource(R.drawable.ic_add)
        fab.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        fab.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white)))

        fab.setOnClickListener {
            limpiarFormulario()
        }

        // Listener para el botón nuevo parte
        nuevoParteButton.setOnClickListener {
            limpiarFormulario()
            fab.visibility = View.GONE
        }

        // Inicia la carga de datos desde el ViewModel
//        // Verifica si las listas están vacías
//        if (autocompleteViewModel.obras.value.isNullOrEmpty() ||
//            autocompleteViewModel.equipos.value.isNullOrEmpty()) {
//            autocompleteViewModel.cargarDatos()
//        }

//        val autocompleteViewModel: AutocompleteViewModel by activityViewModels()

        autocompleteViewModel.equipos.observe(viewLifecycleOwner) { equipos ->
            Log.d("ParteDiarioFragment", "Equipos recibidos en el fragmento: $equipos")
            if (!equipos.isNullOrEmpty()) {
                val equipoStrings = equipos.map { "${it.interno} - ${it.descripcion}" }
                val adapterEquipos = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, equipoStrings)
                binding.equipoAutocomplete.setAdapter(adapterEquipos)
            }
        }

        autocompleteViewModel.obras.observe(viewLifecycleOwner) { obras ->
            Log.d("ParteDiarioFragment", "Obras recibidas en el fragmento: $obras")
            if (!obras.isNullOrEmpty()) {
                val obraStrings = obras.map { "${it.centro_costo} - ${it.nombre}" }
                val adapterObras = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, obraStrings)
                binding.obraAutocomplete.setAdapter(adapterObras)
            }
        }
    }

    private fun setEditTextToUppercase(editText: AutoCompleteTextView) {
        editText.filters = arrayOf(InputFilter.AllCaps())
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

    private fun addTextWatcher(textInputLayout: TextInputLayout, errorMessage: String) {
        val editText = textInputLayout.editText
        editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Implementación vacía o tu código aquí
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Implementación vacía o tu código aquí
            }

            override fun afterTextChanged(s: Editable?) {
                if (textInputLayout.isErrorEnabled) {
                    if (s.isNullOrEmpty()) {
                        textInputLayout.error = errorMessage
                    } else {
                        textInputLayout.isErrorEnabled = false
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        limpiarFormulario()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}