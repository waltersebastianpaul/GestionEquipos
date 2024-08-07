package com.example.gestionequipos.ui.partediario

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionequipos.R
import com.example.gestionequipos.databinding.FragmentListaPartesBinding
import com.example.gestionequipos.ui.appdata.AppDataViewModel
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

//    private val autocompleteViewModel: AutocompleteViewModel by activityViewModels()

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

    @SuppressLint("SetTextI18n")
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
                Log.d("ListaPartesFragment", "PagingData recibido: $pagingData")
                adapter.submitData(pagingData)
            }
        }

        adapter.addLoadStateListener { loadState ->
            Log.d("ListaPartesFragment", "LoadState cambiado: ${loadState.source.refresh}")
            if (loadState.source.refresh is LoadState.NotLoading) {
                Log.d("ListaPartesFragment", "Datos recibidos: ${adapter.itemCount}")
                if (adapter.itemCount == 0) {
                    binding.emptyListMessage.visibility = View.VISIBLE
                    binding.listaPartesRecyclerView.visibility = View.GONE
                } else {
                    binding.emptyListMessage.visibility = View.GONE
                    binding.listaPartesRecyclerView.visibility = View.VISIBLE
                }
            } else if (loadState.source.refresh is LoadState.Error) {
                Log.e("ListaPartesFragment", "Error al cargar datos", (loadState.source.refresh as LoadState.Error).error)
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
            hideKeyboard()
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
            hideKeyboard()
        }

        // Configura el FloatingActionButton
        fab.setImageResource(R.drawable.ic_add)
        fab.setOnClickListener {
            findNavController().navigate(R.id.nav_partediario)
        }

        // Configura el OnClickListener para filtroFechaInicioEditText
        filtroFechaInicioEditText.setOnClickListener {
//            if (!isDatePickerOpen) {
//                isDatePickerOpen = true
//                val calendar = Calendar.getInstance()
//                val datePickerDialog = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
//                    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
//                    calendar.set(year, month, dayOfMonth)
//                    filtroFechaInicioEditText.setText(formatter.format(calendar.time))
//                    isDatePickerOpen = false
//                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
//                datePickerDialog.setOnDismissListener {
//                    isDatePickerOpen = false
//                }
//                datePickerDialog.show()
//            }
            val locale = Locale.getDefault() // Crea un nuevo objeto Locale con el idioma español
            val calendar = Calendar.getInstance(locale) // Usa el locale para el Calendar

            // Obtener la fecha del EditText si está presente
            val dateString = filtroFechaInicioEditText.text.toString()
            if (dateString.isNotBlank()) {val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = formatter.parse(dateString)
                if (date != null) {
                    calendar.time = date
                }
            }

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                    filtroFechaInicioEditText.setText(formattedDate)
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }

        // Configura el OnClickListener para filtroFechaFinEditText
        filtroFechaFinEditText.setOnClickListener {
//            if (!isDatePickerOpen) {
//                isDatePickerOpen = true
//                val calendar = Calendar.getInstance()
//                val datePickerDialog = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
//                    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
//                    calendar.set(year, month, dayOfMonth)
//                    filtroFechaFinEditText.setText(formatter.format(calendar.time))
//                    isDatePickerOpen = false
//                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
//                datePickerDialog.setOnDismissListener {
//                    isDatePickerOpen = false
//                }
//                datePickerDialog.show()
//            }
            val locale = Locale.getDefault() // Crea un nuevo objeto Locale con el idioma español
            val calendar = Calendar.getInstance(locale) // Usa el locale para el Calendar

            // Obtener la fecha del EditText si está presente
            val dateString = filtroFechaFinEditText.text.toString()
            if (dateString.isNotBlank()) {val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = formatter.parse(dateString)
                if (date != null) {
                    calendar.time = date
                }
            }

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                    filtroFechaFinEditText.setText(formattedDate)
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }
    }

    private fun hideKeyboard() {
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = requireActivity().currentFocus
        view?.let {
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
