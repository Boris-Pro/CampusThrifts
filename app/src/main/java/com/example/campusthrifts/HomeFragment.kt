package com.example.campusthrifts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusthrifts.databinding.FragmentHomeBinding
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter
    private lateinit var database: DatabaseReference
    private var productList: MutableList<Products> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().getReference("products")

        // Set up RecyclerView
        productAdapter = ProductAdapter(productList) { product ->
            onProductClick(product)  // Handle item click
        }
        binding.productsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.productsRecyclerView.adapter = productAdapter

        // Load products from Firebase
        fetchProducts()

        // Filter icon click functionality
        binding.filterIcon.setOnClickListener {
            showCategoryMenu()
        }

        return binding.root
    }

    private fun fetchProducts() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productList.clear()
                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(Products::class.java)
                    product?.let { productList.add(it) }
                }
                Log.d("HomeFragment", "Fetched products: $productList")
                productAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to fetch products", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun showCategoryMenu() {
        val popupMenu = PopupMenu(requireContext(), binding.filterIcon)
        popupMenu.menu.add("Newest")
        popupMenu.menu.add("Category")

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.title) {
                "Newest" -> sortByNewest()
                "Category" -> showCategorySelection()
            }
            true
        }
        popupMenu.show()
    }

    private fun sortByNewest() {
        val sortedList = productList.sortedByDescending { it.dateAdded }
        productAdapter.updateList(sortedList)
    }

    private fun showCategorySelection() {
        val categories = listOf(
            "Clothing", "Furniture", "Electronics", "Books", "Other"
        )
        val popupMenu = PopupMenu(requireContext(), binding.filterIcon)
        categories.forEach { popupMenu.menu.add(it) }

        popupMenu.setOnMenuItemClickListener { item ->
            filterByCategory(item.title.toString())
            true
        }
        popupMenu.show()
    }

    private fun filterByCategory(category: String) {
        val filteredList = productList.filter { it.category == category }
        productAdapter.updateList(filteredList)
    }

    private fun onProductClick(product: Products) {
        // Handle the product item click and navigate to ProductDetailFragment
        val fragment = ProductDetailFragment()
        val bundle = Bundle()
        bundle.putString("productId", product.id)  // Pass the product ID or other relevant data
        Log.d("HomeFragment", "Product ID: ${product.id}")
        fragment.arguments = bundle

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
