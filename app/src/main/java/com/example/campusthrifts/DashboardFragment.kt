package com.example.campusthrifts

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var productList: MutableList<Product>
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_dashboard, container, false)

        // Initialize RecyclerView and Button
        recyclerView = rootView.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().reference.child("products")

        productList = mutableListOf()
        productAdapter = ProductAdapter(productList)
        recyclerView.adapter = productAdapter

        // Fetch products from Firebase
        fetchProducts()

        // Set up Add Product button click listener
        val btnAddProduct: Button = rootView.findViewById(R.id.btnAddProduct)
        btnAddProduct.setOnClickListener {
            // Navigate to the AddProductActivity to add a new product
            val intent = Intent(context, AddProductActivity::class.java)
            startActivity(intent)
        }

        return rootView
    }

    private fun fetchProducts() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                productList.clear() // Clear the list before adding new data

                // Loop through the snapshot and add products to the list
                for (dataSnapshot in snapshot.children) {
                    val product = dataSnapshot.getValue(Product::class.java)
                    if (product != null) {
                        // Filter products by userId or add any other condition here
                        productList.add(product)
                    }
                }

                productAdapter.notifyDataSetChanged() // Notify the adapter to update the RecyclerView
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Toast.makeText(context, "Failed to load products", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
