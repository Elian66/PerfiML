package com.swperfi.perfiml.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.swperfi.perfiml.R

class KernelAdapter(private val data: List<List<String>>) : RecyclerView.Adapter<KernelAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textType: TextView = itemView.findViewById(R.id.textType)
        val textName: TextView = itemView.findViewById(R.id.textName)
        val textTempo: TextView = itemView.findViewById(R.id.textTempo)
        val textVezes: TextView = itemView.findViewById(R.id.textVezes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_kernel, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.textType.text = item[0]
        holder.textName.text = item[1]
        holder.textTempo.text = item[2]
        holder.textVezes.text = item[3]
    }

    override fun getItemCount(): Int {
        return data.size
    }
}