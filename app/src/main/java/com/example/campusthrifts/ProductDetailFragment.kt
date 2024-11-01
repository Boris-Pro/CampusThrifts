package com.example.campusthrifts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.TextView


class ProductDetailFragment : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_product_detail, container, false)

        // Retrieve arguments
        val productName = arguments?.getString("PRODUCT_NAME")
        val productPrice = arguments?.getDouble("PRODUCT_PRICE")
        val productImageId = arguments?.getInt("PRODUCT_IMAGE_ID")

        view.findViewById<TextView>(R.id.itemNameLabel).text = productName
        view.findViewById<TextView>(R.id.itemPriceLabel).text = "$${productPrice}"
        view.findViewById<ImageView>(R.id.itemImage).setImageResource(productImageId ?: R.drawable.default_image)

        return view
    }
}
