package com.example.campusthrifts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.campusthrifts.models.Item
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ItemAdapter(private var items: List<Item>, private val onFavoriteClick: (Item) -> Unit) :
    RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    // Create a ViewHolder class to hold item views
    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemName: TextView = view.findViewById(R.id.item_name)
        val itemPrice: TextView = view.findViewById(R.id.item_price)
        val itemDescription: TextView = view.findViewById(R.id.item_description)
        val itemImage: ImageView = view.findViewById(R.id.item_image)
        val favoriteIcon: ImageView = view.findViewById(R.id.favorite_icon)
    }

    // Create new views (called by layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
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

        // Set favorite icon based on item's favorite status
        val favoriteIconRes = if (currentItem.isFavorited) {
            R.drawable.ic_heart_filled // Assuming you have this drawable resource
        } else {
            R.drawable.ic_heart_outline // Assuming you have this drawable resource
        }
        holder.favoriteIcon.setImageResource(favoriteIconRes)

        // Handle favorite icon click
        holder.favoriteIcon.setOnClickListener {
            // Toggle the favorite state
            val newFavoriteStatus = !currentItem.isFavorited
            currentItem.isFavorited = newFavoriteStatus

            // Update UI
            val updatedIconRes = if (newFavoriteStatus) {
                R.drawable.ic_heart_filled
            } else {
                R.drawable.ic_heart_outline
            }
            holder.favoriteIcon.setImageResource(updatedIconRes)

            // Update the popularity score in Firebase
            updateItemPopularity(currentItem, newFavoriteStatus)

            // Trigger the favorite click listener
            onFavoriteClick(currentItem)
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

    // Method to update the popularity score of the item in Firebase
    private fun updateItemPopularity(item: Item, isFavorited: Boolean) {
        val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("popular_items")

        // Retrieve or update the popularity score of the item
        val itemId = item.id
        val popularItemRef = database.child(itemId)

        popularItemRef.get().addOnSuccessListener {
            val currentScore = it.child("popularity_score").getValue(Int::class.java) ?: 0
            val newScore = if (isFavorited) currentScore + 1 else currentScore - 1

            // Update the popularity score in Firebase
            popularItemRef.child("popularity_score").setValue(newScore.coerceAtLeast(0))
        }
    }
}
