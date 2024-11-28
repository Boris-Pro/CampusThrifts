package com.example.campusthrifts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.campusthrifts.models.Item
import com.google.firebase.database.FirebaseDatabase


class LandscapeItemAdapter(private var items: List<Item>) : RecyclerView.Adapter<LandscapeItemAdapter.ItemViewHolder>() {

    // Create a ViewHolder class to hold item views
    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemName: TextView = view.findViewById(R.id.item_name)
        val itemPrice: TextView = view.findViewById(R.id.item_price)
        val itemDescription: TextView = view.findViewById(R.id.item_description)
        val itemImage: ImageView = view.findViewById(R.id.item_image)
        val favoriteIcon: ImageView = view.findViewById(R.id.favorite_icon) // Add this
    }

    // Create new views (called by layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_landscape_layout, parent, false) // Ensure layout file matches
        return ItemViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentItem = items[position]
        holder.itemName.text = currentItem.name
        holder.itemPrice.text = holder.itemView.context.getString(R.string.item_price_placeholder, currentItem.price)
        holder.itemDescription.text = currentItem.description

        // Use Glide to load the image from URL
        Glide.with(holder.itemView.context)
            .load(currentItem.imageUrl)
            .placeholder(R.drawable.placeholderimage)
            .into(holder.itemImage)

        // Set the favorite icon based on whether the item is favorited or not
        holder.favoriteIcon.setImageResource(if (currentItem.isFavorited) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline)

        // Handle click event on favorite icon
        holder.favoriteIcon.setOnClickListener {
            currentItem.isFavorited = !currentItem.isFavorited
            notifyItemChanged(position)

            // Optionally, update the Firebase database for the favorite state
            // Use the FirebaseDatabase instance to update popularity score
            val database = FirebaseDatabase.getInstance()
            val productRef = database.getReference("products").child(currentItem.id)
            if (currentItem.isFavorited) {
                productRef.child("popularity_score").setValue(currentItem.popularityScore + 1)
            } else {
                productRef.child("popularity_score").setValue(currentItem.popularityScore - 1)
            }
        }
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
