package com.example.campusthrifts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.campusthrifts.models.Category

class CategoryAdapter(private var categories: List<Category>) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    // Create a ViewHolder class to hold category views
    class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryName: TextView = view.findViewById(R.id.category_name)
        val categoryDescription: TextView = view.findViewById(R.id.category_description)
    }

    // Create new views (called by layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_item_layout, parent, false) // Make sure `category_item_layout` matches your item layout XML file name
        return CategoryViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val currentCategory = categories[position]
        holder.categoryName.text = currentCategory.name
        holder.categoryDescription.text = currentCategory.description
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return categories.size
    }

    // Method to update the categories list
    fun updateCategories(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged()
    }
}

