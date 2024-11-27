package com.example.campusthrifts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.campusthrifts.databinding.FragmentProductDetailBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProductDetailFragment : Fragment(R.layout.fragment_product_detail) {

    private lateinit var binding: FragmentProductDetailBinding
    private var productId: String? = null
    private lateinit var product: Products
    private lateinit var recommendedAdapter: ProductAdapter
    private val recommendedProducts: MutableList<Products> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProductDetailBinding.inflate(inflater, container, false)

        // Get the productId passed from the previous fragment or activity
        productId = arguments?.getString("productId")
        Log.d("ProductDetailFragment", "Product ID: $productId")

        // Initialize the recommended products adapter with an item click listener
        recommendedAdapter = ProductAdapter(recommendedProducts) { recommendedProduct ->
            // When an item is clicked, navigate to its details page
            navigateToProductDetail(recommendedProduct)
        }

        // Set up the RecyclerView for recommended products
        binding.recommendedRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recommendedRecyclerView.adapter = recommendedAdapter

        // Load the current product details if productId is not null
        if (productId != null) {
            loadProductDetails(productId!!)
        }

        return binding.root
    }

    private fun loadProductDetails(productId: String) {
        val db = FirebaseDatabase.getInstance().getReference("products").child(productId)

        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Check if the product exists
                if (snapshot.exists()) {
                    // Convert the snapshot to a Product object
                    val product = snapshot.getValue(Products::class.java)

                    // Check if product is not null
                    product?.let {
                        // Set the views with the product data
                        val userRef = FirebaseDatabase.getInstance().getReference("users").child(it.userId!!)

                        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val sellerName = snapshot.child("username").getValue(String::class.java)
                                binding.sellerName.text = "Seller : $sellerName"
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

                        // Set quantity options in the spinner
                        val quantity = it.quantity ?: 1 // Assuming the product object has a 'quantity' field
                        val quantityList = (1..quantity).toList() // Create a list from 1 to the available quantity
                        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, quantityList)
                        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.productQuantitySpinner.adapter = spinnerAdapter

                        // Set the selected item to 1 if there's no existing quantity or the quantity value
                        binding.productQuantitySpinner.setSelection(0) // Default to the first item (quantity 1)

                        // Save the current product to the local variable
                        this@ProductDetailFragment.product = it

                        // Now, load recommended products based on this product's category
                        loadRecommendedProducts(it.category)

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

    private fun loadRecommendedProducts(category: String?) {
        // Query Firebase for products in the same category, excluding the current product
        val db = FirebaseDatabase.getInstance().getReference("products")
        db.orderByChild("category").equalTo(category).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                recommendedProducts.clear() // Clear the existing list before adding new products
                for (productSnapshot in snapshot.children) {
                    val recommendedProduct = productSnapshot.getValue(Products::class.java)
                    Log.d("ProductDetails", "Recommended Product: ${recommendedProduct?.name}")

                    if (recommendedProduct != null && recommendedProduct.id != product.id) {
                        Log.d("ProductDetails", "Adding recommended product: ${recommendedProduct.name}")
                        // Add the recommended product to the list
                        recommendedProducts.add(recommendedProduct)
                    }
                }

                // Update the adapter with the new list of recommended products
                recommendedAdapter.updateList(recommendedProducts)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProductDetails", "Failed to load recommended products", error.toException())
            }
        })
    }

    private fun navigateToProductDetail(product: Products) {
        // This is where you handle navigation to the product details screen
        // You can pass the product ID or any other necessary data to the next fragment/activity
        val bundle = Bundle()
        bundle.putString("productId", product.id)

        // Example: using a FragmentTransaction (assuming you are using fragments)
        val fragment = ProductDetailFragment()
        fragment.arguments = bundle
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment) // Use the appropriate container ID
            .addToBackStack(null)
            .commit()
    }
}

