package com.example.gestionequipos.ui.partediario

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionequipos.data.ListarPartesDiarios
import com.example.gestionequipos.databinding.ItemParteDiarioBinding
import java.text.SimpleDateFormat
import java.util.*

class ParteDiarioAdapter(
    private val partesDiarios: List<ListarPartesDiarios>) :
    RecyclerView.Adapter<ParteDiarioAdapter.ParteDiarioViewHolder>() { // Corrección:Usa ParteDiarioViewHolder

    class ParteDiarioViewHolder( // Define la clase ParteDiarioViewHolder
        binding: ItemParteDiarioBinding) : RecyclerView.ViewHolder(binding.root) {
        val parteDiarioTextView = binding.parteDiarioTextView // Define parteDiarioTextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParteDiarioViewHolder { // Corrección: Usa ParteDiarioViewHolder
        val binding = ItemParteDiarioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ParteDiarioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ParteDiarioViewHolder, position: Int) {
        val parteDiario = partesDiarios[position]// Formatea la fecha
        val formatoEntrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatoSalida = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaFormateada = try {
            val fechaDate = formatoEntrada.parse(parteDiario.fecha)
            formatoSalida.format(fechaDate!!)
        } catch (e: Exception) {
            parteDiario.fecha // En caso de error, muestra la fecha original
        }

        val textoParteDiario = "Fecha: $fechaFormateada\n" + //Usa fechaFormateada aquí
                "Equipo: ${parteDiario.interno}    " +
                "Ini: ${parteDiario.horas_inicio}     " +
                "Fin: ${parteDiario.horas_fin}   " +
                "Total: ${parteDiario.horas_trabajadas}"
        holder.parteDiarioTextView.text = textoParteDiario

        // Agrega un listener para el clicen el elemento
        holder.itemView.setOnClickListener {
            val posicion = holder.adapterPosition
            if (posicion != RecyclerView.NO_POSITION) {
                Log.d("ParteDiarioAdapter", "Clic en el elemento $posicion")
            }
        }
    }

    override fun getItemCount(): Int = partesDiarios.size
}