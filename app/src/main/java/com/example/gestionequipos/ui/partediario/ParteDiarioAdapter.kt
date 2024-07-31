package com.example.gestionequipos.ui.partediario

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionequipos.R
import com.example.gestionequipos.data.ListarPartesDiarios
import com.example.gestionequipos.databinding.ItemParteDiarioBinding
import java.text.SimpleDateFormat
import java.util.*

class ParteDiarioAdapter(
    private val partesDiarios: List<ListarPartesDiarios>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_DIVIDER = 1
    }

    class ParteDiarioViewHolder(binding: ItemParteDiarioBinding) : RecyclerView.ViewHolder(binding.root) {
        val fechaTextView: TextView = binding.fechaTextView
        val equipoTextView: TextView = binding.equipoTextView
        val horasTextView: TextView = binding.horasTextView
    }

    class DividerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ITEM) {
            val binding = ItemParteDiarioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ParteDiarioViewHolder(binding)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_divider, parent, false)
            DividerViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ParteDiarioViewHolder) {
            val parteDiario = partesDiarios[position / 2] // Ajusta la posición debido a los divisores

            // Formatea la fecha
            val formatoEntrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatoSalida = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fechaFormateada = try {
                val fechaDate = formatoEntrada.parse(parteDiario.fecha)
                formatoSalida.format(fechaDate!!)
            } catch (e: Exception) {
                parteDiario.fecha
            }

            // Asigna los valores a los TextViews
            holder.fechaTextView.text = "Fecha: $fechaFormateada"
            holder.equipoTextView.text = "Equipo: ${parteDiario.interno}"
            holder.horasTextView.text = "Horas: ${parteDiario.horas_trabajadas}"

            // Agrega un listener para el clic en el elemento (si lo necesitas)
            holder.itemView.setOnClickListener {
                val posicion = holder.adapterPosition
                if (posicion != RecyclerView.NO_POSITION) {
                    Log.d("ParteDiarioAdapter", "Clic en el elemento $posicion")
                    // ... tu lógica para manejar el clic
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position % 2 == 0) VIEW_TYPE_ITEM else VIEW_TYPE_DIVIDER
    }

    override fun getItemCount(): Int {
        return partesDiarios.size * 2 // Duplica el tamaño para incluir los divisores
    }
}
