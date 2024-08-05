package com.example.gestionequipos.ui.partediario

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionequipos.data.ListarPartesDiarios
import com.example.gestionequipos.databinding.ItemParteDiarioBinding

class ParteDiarioAdapter : PagingDataAdapter<ListarPartesDiarios, ParteDiarioAdapter.ParteDiarioViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParteDiarioViewHolder {
        val binding = ItemParteDiarioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ParteDiarioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ParteDiarioViewHolder, position: Int) {
        val parteDiario = getItem(position)
        parteDiario?.let { holder.bind(it) }
    }

    class ParteDiarioViewHolder(private val binding: ItemParteDiarioBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(parteDiario: ListarPartesDiarios) {
            binding.fechaTextView.text = parteDiario.fecha
            binding.equipoTextView.text = parteDiario.interno
            binding.horasTextView.text = parteDiario.horas_trabajadas.toString()
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<ListarPartesDiarios>() {
            override fun areItemsTheSame(oldItem: ListarPartesDiarios, newItem: ListarPartesDiarios): Boolean {
                return oldItem.id_parte_diario == newItem.id_parte_diario
            }

            override fun areContentsTheSame(oldItem: ListarPartesDiarios, newItem: ListarPartesDiarios): Boolean {
                return oldItem == newItem
            }
        }
    }
}
