package com.example.campusthrifts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.campusthrifts.databinding.FragmentProductDetailBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class ProductDetailFragment : Fragment(R.layout.fragment_product_detail) {

    private lateinit var binding: FragmentProductDetailBinding
    private var productId: String? = null
    private lateinit var product: Products

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProductDetailBinding.inflate(inflater, container, false)

        // Get the productId passed from the previous fragment or activity
        productId = arguments?.getString("productId")
        Log.d("ProductDetailFragment", "Product ID: $productId")

        if (productId != null) {
            loadProductDetails(productId!!)
        }

        return binding.root
    }

    private fun loadProductDetails(productId: String) {
        val db = FirebaseDatabase.getInstance().getReference("items").child(productId)

        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Check if the product exists
                if (snapshot.exists()) {
                    // Convert the snapshot to a Product object
                    val product = snapshot.getValue(Products::class.java)

                    // Check if product is not null
                    product?.let {
                        // Set the views with the product data
                        val userRef = FirebaseDatabase.getInstance().getReference("users").child(it.uid!!)
                        Log.d("ProductDetails", "User ID: ${it.uid}")

                        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val sellerName = snapshot.child("username").getValue(String::class.java)
                                binding.sellerName.text = "Seller Name: $sellerName"
                                Log.d("ProductDetails", "Seller Name: $sellerName")
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("ProductDetails", "Failed to load seller info", error.toException())
                            }
                        })
                        Glide.with(requireContext()).load(it.imageUrl).into(binding.productImage)
                        binding.productName.text = it.name
                        binding.productDescription.text = it.description
                        binding.productCategory.text = it.category
                        binding.productPrice.text = "Price: $${it.price}"

                        // Add to Cart button logic
                        binding.addCartButton.setOnClickListener {
                            // Handle Add to Cart functionality here
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle failure (e.g., show a Toast or log the error)
                Log.e("ProductDetails", "Failed to load product details", error.toException())
            }
        })
    }
}
