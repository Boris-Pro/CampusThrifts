package com.example.campusthrifts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class DashboardProductAdapter(
    private val productList: List<Product>,
    private val onEditClick: (Product) -> Unit,  // Callback for edit action
    private val onDeleteClick: (Product) -> Unit, // Callback for delete action
    private val onViewDetailClick: (Product) -> Unit // Callback for view detail action
) : RecyclerView.Adapter<DashboardProductAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.bind(product)
    }

    override fun getItemCount(): Int = productList.size

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val productName: TextView = itemView.findViewById(R.id.productName)
        private val productPrice: TextView = itemView.findViewById(R.id.productPrice)
        private val productImage: ImageView = itemView.findViewById(R.id.productImage)
        private val editButton: ImageButton = itemView.findViewById(R.id.editButton)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
        private val viewDetailButton: Button = itemView.findViewById(R.id.viewDetailButton)

        fun bind(product: Product) {
            productName.text = product.name
            productPrice.text = "$${product.price}"

            // Load image using Glide
            Glide.with(itemView.context)
                .load(product.imageUrl)
                .into(productImage)

            // Set click listeners for the edit and delete buttons
            editButton.setOnClickListener {
                onEditClick(product) // Call the edit callback with the product
            }

            deleteButton.setOnClickListener {
                onDeleteClick(product) // Call the delete callback with the product
            }

            viewDetailButton.setOnClickListener {
                onViewDetailClick(product) // Call the view detail callback with the product
            }
        }
    }
}