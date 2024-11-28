package com.example.campusthrifts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.campusthrifts.models.Item

class ItemAdapter(private var items: List<Item>) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    // Create a ViewHolder class to hold item views
    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemName: TextView = view.findViewById(R.id.item_name)
        val itemPrice: TextView = view.findViewById(R.id.item_price)
        val itemDescription: TextView = view.findViewById(R.id.item_description)
        val itemImage: ImageView = view.findViewById(R.id.item_image) // Add this
    }

    // Create new views (called by layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false) // Make sure `item_layout` matches your item layout XML file name
        return ItemViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentItem = items[position]
        holder.itemName.text = currentItem.name
        holder.itemPrice.text = "$${currentItem.price}" // Assuming price is in dollars
        holder.itemDescription.text = currentItem.description

        // Use Glide to load the image from URL
        Glide.with(holder.itemView.context)
            .load(currentItem.imageUrl) // imageUrl should be a property of the Item model
            .placeholder(R.drawable.placeholderimage) // Provide a placeholder while loading
            .into(holder.itemImage)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return items.size
    }

    // Method to update the items list
    fun updateItems(newItems: List<Item>) {
        items = newItems
        notifyDataSetChanged() // Refresh the list to show updated items
    }
}
