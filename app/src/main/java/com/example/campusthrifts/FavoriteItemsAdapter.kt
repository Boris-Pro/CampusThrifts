package com.example.campusthrifts.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.campusthrifts.R
import com.example.campusthrifts.models.Item
import com.bumptech.glide.Glide

class FavoriteItemsAdapter(private val itemList: List<Item>) :
    RecyclerView.Adapter<FavoriteItemsAdapter.FavoriteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemName: TextView = itemView.findViewById(R.id.item_name)
        private val itemImage: ImageView = itemView.findViewById(R.id.item_image)

        fun bind(item: Item) {
            itemName.text = item.name
            Glide.with(itemView.context).load(item.imageUrl).into(itemImage)
        }
    }
}
