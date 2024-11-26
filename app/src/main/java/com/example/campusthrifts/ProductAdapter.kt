package com.example.campusthrifts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ProductAdapter(private var productList: List<Products>) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    fun updateList(newList: List<Products>) {
        productList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_search_product_view, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.bind(product)
    }

    override fun getItemCount(): Int = productList.size

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productImage: ImageView = itemView.findViewById(R.id.productImage)
        private val productName: TextView = itemView.findViewById(R.id.productName)
        private val productPrice: TextView = itemView.findViewById(R.id.productPrice)


        fun bind(product: Products) {
            productName.text = product.name
            productPrice.text = "Price: $${product.price}"


            Glide.with(itemView.context)
                .load(product.imageUrl)
                .placeholder(R.drawable.placeholderimage)
                .into(productImage)
        }
    }
}