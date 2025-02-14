package com.example.aplikacja_final

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikacja_final.databinding.ItemDaneBinding

class MyFirstAdapter(
    private val dane: MutableList<Dane>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<MyFirstAdapter.MyViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onItemSelected(position: Int)
    }

    inner class MyViewHolder(binding: ItemDaneBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        val textID: TextView = binding.itemId
        val textC1step: TextView = binding.itemC1step
        val textC1kat: TextView = binding.itemC1kat
        val textC2kat: TextView = binding.itemC2kat
        val textroll_chwyt: TextView = binding.itemRollChwyt
        val textChwytak: TextView = binding.itemChwytak
        val deleteImageView: ImageView = binding.deleteId

        init {
            itemView.setOnClickListener(this)
            deleteImageView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(position)
                }
            }
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                if (position == selectedPosition) {
                    // Deselect the currently selected item
                    selectedPosition = RecyclerView.NO_POSITION
                } else {
                    // Select the new item
                    notifyItemChanged(selectedPosition)
                    selectedPosition = position
                }
                notifyItemChanged(position)
                listener.onItemSelected(selectedPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemDaneBinding = ItemDaneBinding.inflate(inflater, parent, false)
        return MyViewHolder(itemDaneBinding)
    }

    override fun getItemCount(): Int {
        return dane.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.textID.text = dane[position].id.toString()
        holder.textC1step.text = dane[position].c1.toString()
//        holder.textC1x0.text = dane[position].c1x0.toString()
//        holder.textC1y0.text = dane[position].c1y0.toString()
        holder.textC1kat.text = dane[position].c1kat.toString()
//        holder.textC2x0.text = dane[position].c2x0.toString()
//        holder.textC2y0.text = dane[position].c2y0.toString()
        holder.textC2kat.text = dane[position].c2kat.toString()
        holder.textroll_chwyt.text = dane[position].roll_chwyt.toString()
        holder.textChwytak.text = dane[position].chwytak.toString()

        if (position == selectedPosition) {
            holder.itemView.setBackgroundResource(R.color.background_color) // Tło dla zaznaczonego elementu
        } else {
            holder.itemView.setBackgroundResource(R.color.selected_background_color) // Tło dla nie zaznaczonego elementu
        }
    }

    fun addItem(d: Dane) {
        dane.add(d)
        notifyItemInserted(dane.size - 1)
        updateList()
    }

    fun removeItem(position: Int) {
        if (position >= 0 && position < dane.size  ) {
            dane.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, dane.size)
            updateList()
        }
    }

    fun updateList() {
        for (i in dane.indices) {
            dane[i].id = i + 1 // jak będą problemy, wrócić do
        }
        notifyDataSetChanged()
    }

    fun getSelectedPosition(): Int {
        return selectedPosition
    }
}
