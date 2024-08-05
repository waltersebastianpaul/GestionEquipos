package com.example.gestionequipos.ui.partediario

import android.app.DatePickerDialog
import android.os.Bundle
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionequipos.R
import com.example.gestionequipos.databinding.FragmentListaPartesBinding
import com.example.gestionequipos.ui.appdata.AppDataViewModel
import com.example.gestionequipos.ui.autocomplete.AutocompleteViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ListaPartesFragment : Fragment() {

    private var _binding: FragmentListaPartesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ParteDiarioViewModel by viewModels()

    private lateinit var filtroEquipoAutocomplete: AutoCompleteTextView
    private lateinit var filtroEquipoTextInputLayout: TextInputLayout
    private lateinit var filtroFechaInicioTextInputLayout: TextInputLayout
    private lateinit var filtroFechaFinTextInputLayout: TextInputLayout
    private lateinit var filtroFechaInicioEditText: TextInputEditText
    private lateinit var filtroFechaFinEditText: TextInputEditText

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
        filtroFechaInicioTextInputLayout = binding.filtroFechaInicioTextInputLayout
        filtroFechaFinTextInputLayout = binding.filtroFechaFinTextInputLayout
        filtroFechaInicioEditText = binding.filtroFechaInicioEditText
        filtroFechaFinEditText = binding.filtroFechaFinEditText

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configura el RecyclerView
        val recyclerView = binding.listaPartesRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)

        val adapter = ParteDiarioAdapter()
        recyclerView.adapter = adapter

        val fab: FloatingActionButton = requireActivity().findViewById(R.id.fab)
        fab.visibility = View.VISIBLE

        val appDataViewModel: AppDataViewModel by activityViewModels()

        appDataViewModel.equipos.observe(viewLifecycleOwner) { equipos ->
            Log.d("ListaPartesFragment", "Equipos recibidos: $equipos")
            val equipoStrings = equipos.map { "${it.interno} - ${it.descripcion}" }
            val adapterEquipos = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, equipoStrings)
            binding.filtroEquipoAutocomplete.setAdapter(adapterEquipos)
        }

        // Observa los datos del ViewModel y configura el adaptador del RecyclerView
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.partesDiarios.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
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
            }
        })

        // Configura el botón para limpiar todos los filtros
        val clearAllFiltersButton: Button = binding.root.findViewById(R.id.clearAllFiltersButton)
        clearAllFiltersButton.setOnClickListener {
            filtroEquipoAutocomplete.setText("")
            filtroFechaInicioEditText.setText("")
            filtroFechaFinEditText.setText("")
            viewModel.setFilter("", "", "")
        }

        // Configura el botón para aplicar los filtros
        val applyFiltersButton: Button = binding.root.findViewById(R.id.applyFiltersButton)
        applyFiltersButton.setOnClickListener {
            val equipo = filtroEquipoAutocomplete.text.toString()
            val equipoInterno = equipo.split(" - ").firstOrNull() ?: ""
            // Usa equipoInterno para filtrar
            val fechaInicio = filtroFechaInicioEditText.text.toString()
            val fechaFin = filtroFechaFinEditText.text.toString()
            Log.d("ListaPartesFragment", "Aplicando filtro - Equipo: $equipoInterno, FechaInicio: $fechaInicio, FechaFin: $fechaFin")
            viewModel.setFilter(equipoInterno, fechaInicio, fechaFin)
        }

        // Configura el FloatingActionButton
        fab.setImageResource(R.drawable.ic_add)
        fab.setOnClickListener {
            findNavController().navigate(R.id.nav_partediario)
        }

        // Configura el OnClickListener para filtroFechaInicioEditText
        filtroFechaInicioEditText.setOnClickListener {
            if (!isDatePickerOpen) {
                isDatePickerOpen = true
                val calendar = Calendar.getInstance()
                val datePickerDialog = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    calendar.set(year, month, dayOfMonth)
                    filtroFechaInicioEditText.setText(formatter.format(calendar.time))
                    isDatePickerOpen = false
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                datePickerDialog.setOnDismissListener {
                    isDatePickerOpen = false
                }
                datePickerDialog.show()
            }
        }

        // Configura el OnClickListener para filtroFechaFinEditText
        filtroFechaFinEditText.setOnClickListener {
            if (!isDatePickerOpen) {
                isDatePickerOpen = true
                val calendar = Calendar.getInstance()
                val datePickerDialog = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    calendar.set(year, month, dayOfMonth)
                    filtroFechaFinEditText.setText(formatter.format(calendar.time))
                    isDatePickerOpen = false
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                datePickerDialog.setOnDismissListener {
                    isDatePickerOpen = false
                }
                datePickerDialog.show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
