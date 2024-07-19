package com.example.gestionequipos.ui.partediario

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.DatePicker
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionequipos.R
import com.example.gestionequipos.databinding.FragmentListaPartesBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.type.Date
import java.text.SimpleDateFormat
import java.util.*
import android.widget.EditText // Importa EditText

class ListaPartesFragment : Fragment() {

    private var _binding: FragmentListaPartesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ParteDiarioViewModel by viewModels()

    private lateinit var filtroEquipoAutocomplete: AutoCompleteTextView
    private lateinit var filtroEquipoTextInputLayout: TextInputLayout
    private lateinit var filtroFechaTextInputLayout: TextInputLayout
    private lateinit var filtroFechaEditText: TextInputEditText

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

        // Configura el listener para el ícono del calendario
        filtroFechaTextInputLayout.setEndIconOnClickListener {
            mostrarDatePicker()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configura el RecyclerView
        val recyclerView = binding.listaPartesRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)

        val fab: FloatingActionButton = requireActivity().findViewById(R.id.fab)
        fab.visibility = View.VISIBLE

        viewModel.equipos.observe(viewLifecycleOwner) { equipos ->
            val equipoStrings = equipos.map { "${it.interno} - ${it.descripcion}" }.toTypedArray()
            val adapterEquipos = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, equipoStrings)
            filtroEquipoAutocomplete.setAdapter(adapterEquipos)
        }

        // Inicia la carga de datos desde el ViewModel
        viewModel.cargarDatos() // <-- Agrega esta línea

        // Observa los datos del ViewModel y configura el adaptador del RecyclerView
        viewModel.partesDiarios.observe(viewLifecycleOwner) { partesDiarios ->
            Log.d("ListaPartesFragment", "Partes diarios recibidos: $partesDiarios")
            val adapter = ParteDiarioAdapter(partesDiarios)
            recyclerView.adapter = adapter
        }

        // Configura los listeners para el filtrado
        filtroEquipoAutocomplete.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                aplicarFiltro()
            }
        })

        // Inicia la carga de datos desde el ViewModel (solo una vez)
        viewModel.cargarPartesDiarios()

        // Configura el FloatingActionButton
        fab.setImageResource(R.drawable.ic_add)
        fab.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.blue))
        fab.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))

        fab.setOnClickListener {
            findNavController().navigate(R.id.nav_partediario)
        }

        filtroFechaTextInputLayout.setEndIconOnClickListener {
            // Muestra el DatePickerDialog o MaterialDatePicker aquí
            mostrarDatePicker()
        }
    }

    private fun mostrarDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Selecciona una fecha")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fechaFormateada = simpleDateFormat.format(Date(selection)) // Corregido: usa Date(selection)
            filtroFechaEditText.setText(fechaFormateada)
            aplicarFiltro() // Aplica el filtro después de seleccionar la fecha
        }

        datePicker.show(parentFragmentManager, "datePicker")
    }

    // Función para aplicar el filtro
    private fun aplicarFiltro() {
        val equipoSeleccionadoText = filtroEquipoAutocomplete.text.toString()
        val equipoInterno = equipoSeleccionadoText.split(" - ").firstOrNull() ?: ""

        val fechaSeleccionada = filtroFechaEditText.text.toString()

        // Si el campo de equipo está vacío, muestra todos los partes diarios
        val partesDiariosFiltrados = if (equipoInterno.isBlank()) {
            viewModel.partesDiarios.value ?: emptyList() // Obtiene todos los partes diarios del ViewModel
        } else {
            viewModel.filtrarPartesDiarios(equipoInterno, fechaSeleccionada)
        }

        val adapter = ParteDiarioAdapter(partesDiariosFiltrados)
        binding.listaPartesRecyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}