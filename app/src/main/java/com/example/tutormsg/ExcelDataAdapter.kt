package com.example.tutormsg

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tutormsg.databinding.ItemExcelDataBinding

class ExcelDataAdapter : RecyclerView.Adapter<ExcelDataAdapter.ViewHolder>() {

    private var data: List<List<String>> = emptyList()

    fun setData(data: List<List<String>>) {
        this.data = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=ItemExcelDataBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        //LayoutInflater.from(parent.context).inflate(R.layout.item_todo, "")
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder){
            with(data[position]){
                bind.tvRecy.text = data[position].toString()
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(val bind: ItemExcelDataBinding) : RecyclerView.ViewHolder(bind.root) {

    }
}
