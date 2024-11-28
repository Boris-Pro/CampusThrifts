package com.example.campusthrifts

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.campusthrifts.databinding.ActivityDashboardProductDetailedBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class DashboardProductDetailedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardProductDetailedBinding
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivityDashboardProductDetailedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().reference

        // Retrieve the productId passed via the intent
        val productId = intent.getStringExtra("productId")

        if (productId != null) {
            loadProductDetails(productId)
        } else {
            Toast.makeText(this, "Product ID is missing", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Set up the back button
        binding.backButton.setOnClickListener {
            onBackPressed()  // Go back to the previous activity
        }
    }

    private fun loadProductDetails(productId: String) {
        // Show loading indicator
        binding.progressBar.visibility = View.VISIBLE

        // Get the product details from Firebase
        database.child("products").child(productId).get().addOnSuccessListener { snapshot ->
            val product = snapshot.getValue(Product::class.java)
            if (product != null) {
                // Set the product details on the UI
                binding.productName.text = product.name
                binding.productCategory.text = product.category
                binding.productPrice.text = getString(R.string.product_price, product.price)
                binding.productDescription.text = product.description
                binding.productQuantity.text = getString(R.string.product_quantity, product.quantity)

                // Load the product image using Glide
                Glide.with(this)
                    .load(product.imageUrl)
                    .into(binding.productImage)
            } else {
                Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show()
                finish()
            }

            // Hide loading indicator
            binding.progressBar.visibility = View.GONE
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load product details", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}