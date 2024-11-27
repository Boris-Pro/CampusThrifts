package com.example.campusthrifts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.campusthrifts.databinding.FragmentSearchProductViewBinding

class ProductAdapter(
    private var products: List<Products>,
    private val onItemClick: (Products) -> Unit // Added click listener
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = FragmentSearchProductViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product)
    }

    override fun getItemCount(): Int = products.size

    fun updateList(newProducts: List<Products>) {
        products = newProducts
        notifyDataSetChanged()
    }

    inner class ProductViewHolder(private val binding: FragmentSearchProductViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Products) {
            binding.productName.text = product.name
            binding.productPrice.text = "Price: $${product.price}"

            // Load the product image (if any) using Glide or similar image loading library
            Glide.with(itemView.context)
                .load(product.imageUrl)
                .into(binding.productImage)

            // Set the click listener
            itemView.setOnClickListener {
                onItemClick(product) // Trigger the click listener
            }
        }
    }
}