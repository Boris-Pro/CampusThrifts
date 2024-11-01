package com.example.campusthrifts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class ProductAdapter(
    private val fragmentManager: FragmentManager,
    private val productList: List<Product>,
) : BaseAdapter() {


    override fun getCount(): Int = productList.size

    override fun getItem(position: Int): Any = productList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(parent.context).inflate(R.layout.fragment_search_product_view, parent, false)

        // Get references to the views in list_item_product.xml
        val productImage: ImageView = view.findViewById(R.id.productImage)
        val productName: TextView = view.findViewById(R.id.productName)
        val productPrice: TextView = view.findViewById(R.id.productPrice)

        // Set data to views
        val product = productList[position]
        productName.text = product.name
        productPrice.text = "$${product.price}"
        productImage.setImageResource(product.imageResourceId)

        // Set an OnClickListener to invoke the onProductClick callback
        view.setOnClickListener {
            // Set up the fragment with arguments
            val productDetailFragment = ProductDetailFragment().apply {
                arguments = Bundle().apply {
                    putString("PRODUCT_NAME", product.name)
                    putDouble("PRODUCT_PRICE", product.price)
                    putInt("PRODUCT_IMAGE_ID", product.imageResourceId)
                }
            }

            // Replace the fragment
            fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, productDetailFragment) // Adjust container ID as needed
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}
