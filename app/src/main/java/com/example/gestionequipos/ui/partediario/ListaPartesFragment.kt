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
import android.widget.Toast
import androidx.compose.material3.Snackbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.distinctUntilChanged
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionequipos.R
import com.example.gestionequipos.databinding.FragmentListaPartesBinding
import com.example.gestionequipos.ui.appdata.AppDataViewModel
import com.example.gestionequipos.ui.autocomplete.AutocompleteViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ListaPartesFragment : Fragment() {

    private var _binding: FragmentListaPartesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ParteDiarioViewModel by viewModels()

    private lateinit var filtroEquipoAutocomplete: AutoCompleteTextView
    private lateinit var filtroEquipoTextInputLayout: TextInputLayout
    private lateinit var filtroFechaTextInputLayout: TextInputLayout
    private lateinit var filtroFechaEditText: TextInputEditText

    private var isDatePickerOpen = false // Variable para controlar si el DatePicker está abierto

    private val autocompleteViewModel: AutocompleteViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListaPartesBinding.inflate(inflater, container, false)

        filtroEquipoAutocomplete = binding.filtroEquipoAutocomplete
        filtroEquipoTextInputLayout = binding.filtroEquipoTextInputLayout
        filtroFechaTextInputLayout = binding.filtroFechaTextInputLayout
        filtroFechaEditText = binding.filtroFechaEditText

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configura el RecyclerView
        val recyclerView = binding.listaPartesRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)

        val fab: FloatingActionButton = requireActivity().findViewById(R.id.fab)
        fab.visibility = View.VISIBLE

        val appDataViewModel: AppDataViewModel by activityViewModels()

        appDataViewModel.equipos.observe(viewLifecycleOwner) { equipos ->
            Log.d("ParteDiarioFragment", "Equipos recibidos: $equipos")
            val equipoStrings = equipos.map { "${it.interno} - ${it.descripcion}" }
            val adapterEquipos = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, equipoStrings)
            binding.filtroEquipoAutocomplete.setAdapter(adapterEquipos)
        }

//        appDataViewModel.obras.observe(viewLifecycleOwner) { obras ->
//            Log.d("ParteDiarioFragment", "Obras recibidas: $obras")
//            val obraStrings = obras.map { "${it.centro_costo} - ${it.nombre}" }
//            val adapterObras = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, obraStrings)
//            binding.obraAutocomplete.setAdapter(adapterObras)
//        }



        //eliminar de aca en adelante si funciona

//        // Observa los datos del ViewModel y configura los adapters
//        autocompleteViewModel.equipos.distinctUntilChanged()
//            .observe(viewLifecycleOwner) { equipos ->
//                val equipoStrings =
//                    equipos.map { "${it.interno} - ${it.descripcion}" }.toTypedArray()
//                val adapterEquipos = ArrayAdapter(
//                    requireContext(),
//                    android.R.layout.simple_dropdown_item_1line,
//                    equipoStrings
//                )
//                binding.filtroEquipoAutocomplete.setAdapter(adapterEquipos)
//                Log.d("ParteDiario", "Equipos: $equipos")
//
//                setEditTextToUppercase(filtroEquipoAutocomplete)
//            }
//
//        // Inicia la carga de datos desde el ViewModel
//        // Verifica si las listas están vacías
//        if (autocompleteViewModel.obras.value.isNullOrEmpty() ||
//            autocompleteViewModel.equipos.value.isNullOrEmpty()
//        ) {
//            autocompleteViewModel.cargarDatos()
//        }

        // Observa los datos del ViewModel y configura el adaptador del RecyclerView
        viewModel.partesDiarios.observe(viewLifecycleOwner) { partesDiarios ->
            Log.d("ListaPartesFragment", "Partes diarios recibidos: $partesDiarios")
            val adapter = ParteDiarioAdapter(partesDiarios)
            recyclerView.adapter = adapter
        }

        // TextWatcher para filtroEquipoAutocomplete
        filtroEquipoAutocomplete.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isEmpty()) {
                    filtroEquipoAutocomplete.dismissDropDown()
                } else {
                    filtroEquipoAutocomplete.showDropDown()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (filtroEquipoTextInputLayout.isErrorEnabled) {
                    if (s.isNullOrEmpty()) {
                        filtroEquipoTextInputLayout.error = "Campo requerido"
                    } else {
                        filtroEquipoTextInputLayout.isErrorEnabled = false
                    }
                }
                aplicarFiltro() // Llama a aplicarFiltro() después de cada cambio en el texto
            }
        })

        // Configura el botón para limpiar todos los filtros
        val clearAllFiltersButton: Button = binding.root.findViewById(R.id.clearAllFiltersButton)
        clearAllFiltersButton.setOnClickListener {
            filtroEquipoAutocomplete.setText("")
            filtroFechaEditText.setText("")
            aplicarFiltro()
        }

        // Configura el FloatingActionButton
        fab.setImageResource(R.drawable.ic_add)
        fab.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        fab.imageTintList =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))

        fab.setOnClickListener {
            findNavController().navigate(R.id.nav_partediario)
        }

        // Configura el OnClickListener para filtroFechaEditText
        filtroFechaEditText.setOnClickListener {
            val locale = Locale.getDefault() // Crea un nuevo objeto Locale con el idioma español
            val calendar = Calendar.getInstance(locale) // Usa el locale para el Calendar

            // Obtener la fecha del EditText si está presente
            val dateString = filtroFechaEditText.text.toString()
            if (dateString.isNotBlank()) {val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = formatter.parse(dateString)
                calendar.time = date
            }

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val formattedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                    filtroFechaEditText.setText(formattedDate)
                    aplicarFiltro()
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }

    }

    private fun setEditTextToUppercase(editText: AutoCompleteTextView) {
        editText.filters = arrayOf(InputFilter.AllCaps())
    }

    private fun aplicarFiltro() {
        val equipoSeleccionadoText = filtroEquipoAutocomplete.text.toString()
        val equipoInterno = equipoSeleccionadoText.split(" - ").firstOrNull() ?: ""
        Log.d("ListaPartesFragment", "Equipo seleccionado: $equipoInterno")

        val fechaSeleccionadaText = filtroFechaEditText.text.toString()
        Log.d("ListaPartesFragment", "Fecha seleccionada en el filtro: $fechaSeleccionadaText")

        val fechaSeleccionada = if (fechaSeleccionadaText.isNotBlank()) {
            val formatoEntrada = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formatoSalida = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fechaDate = formatoEntrada.parse(fechaSeleccionadaText)
            val fechaFormateada = formatoSalida.format(fechaDate)
            Log.d("ListaPartesFragment", "Fecha formateada: $fechaFormateada")
            fechaFormateada
        } else {
            ""
        }

        val partesDiariosFiltrados = viewModel.filtrarPartesDiarios(equipoInterno, fechaSeleccionada)
        Log.d("ListaPartesFragment", "Número de partes diarios filtrados: ${partesDiariosFiltrados.size}")

        val adapter = ParteDiarioAdapter(partesDiariosFiltrados)
        binding.listaPartesRecyclerView.adapter = adapter
    }

    private var autocompletesCargados = false
    private var partesDiariosCargados = false

    override fun onResume(){
        super.onResume()

        autocompletesCargados = false
        partesDiariosCargados = false

        // Verifica si las listas están vacías
        if (autocompleteViewModel.obras.value.isNullOrEmpty() ||
            autocompleteViewModel.equipos.value.isNullOrEmpty()) {
            autocompleteViewModel.cargarDatos()
        } else {
            autocompletesCargados = true
        }

        // Inicia la cargade datos
        viewModel.cargarPartesDiarios {
            partesDiariosCargados = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}