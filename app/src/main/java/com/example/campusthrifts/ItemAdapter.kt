package com.example.campusthrifts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItemAdapter(private val itemList: List<Item>) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImage: ImageView = itemView.findViewById(R.id.itemImage)
        val itemName: TextView = itemView.findViewById(R.id.item_name)
        val itemDescription: TextView = itemView.findViewById(R.id.item_description)
        val itemPrice: TextView = itemView.findViewById(R.id.item_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.itemName.text = item.name
        holder.itemDescription.text = item.description
        holder.itemPrice.text = "$${item.price}"
        holder.itemImage.setImageResource(item.imageResId)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}
