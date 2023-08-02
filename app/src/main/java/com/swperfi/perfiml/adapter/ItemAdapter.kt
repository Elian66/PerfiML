package com.swperfi.perfiml.adapter

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chaquo.python.Python
import com.swperfi.perfiml.R
import com.swperfi.perfiml.model.Item
import com.swperfi.perfiml.results.ResultsActivity

class ItemAdapter(private val context: Context, private val items: List<Item>, private val file: String) :
    RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_method, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, file, context)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val methodName: TextView = itemView.findViewById<TextView>(R.id.methodName)

        fun bind(item: Item, file: String, context: Context) {
            methodName.text = item.name
            methodName.setOnClickListener {
                val progressDialog: ProgressDialog = ProgressDialog(context)
                progressDialog.setMessage("Carregando Resultado...")
                progressDialog.setCancelable(false)
                progressDialog.show()

                Log.d("JSON PYTHON", "Name: ${item.name}")

                val python = Python.getInstance()
                val result = python.getModule("main").callAttr(item.function, file).toString()

                Log.d("JSON PYTHON", "Msg: $result")
                progressDialog.dismiss()

                val intent = Intent(context, ResultsActivity::class.java)
                intent.putExtra("result", result)
                intent.putExtra("behavior", item.behavior)
                intent.putExtra("function", item.function)
                context.startActivity(intent)
            }
        }
    }
}
