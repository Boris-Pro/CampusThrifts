package com.example.campusthrifts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ProductAdapter(private val productList: List<Product>) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

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

        fun bind(product: Product) {
            productName.text = product.name
            productPrice.text = "$${product.price}"

            // You can use Glide or Picasso to load the image from URL
            Glide.with(itemView.context)
                .load(product.imageUrl)
                .into(productImage)
        }
    }
}
