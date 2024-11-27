package com.example.campusthrifts

import android.app.AlertDialog
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: DashboardProductAdapter
    private lateinit var productList: MutableList<Product>
    private lateinit var database: DatabaseReference

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_dashboard, container, false)

        recyclerView = rootView.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        database = FirebaseDatabase.getInstance().reference.child("products")

        productList = mutableListOf()
        productAdapter = DashboardProductAdapter(productList, ::onEditClick, ::onDeleteClick, ::onViewDetailClick)
        recyclerView.adapter = productAdapter

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
        if (currentUserId == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productList.clear()

                for (dataSnapshot in snapshot.children) {
                    val product = dataSnapshot.getValue(Product::class.java)
                    if (product != null && product.userId == currentUserId) {
                        productList.add(product)
                    }
                }

                productAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load products", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Edit Product
    private fun onEditClick(product: Product) {
        val intent = Intent(context, AddProductActivity::class.java).apply {
            putExtra("productId", product.id)
            putExtra("productName", product.name)
            putExtra("productPrice", product.price)
            putExtra("productQuantity", product.quantity)
            putExtra("productDescription", product.description)
            putExtra("productImageUrl", product.imageUrl)
        }
        startActivity(intent)
    }

    // Delete Product
    private fun onDeleteClick(product: Product) {
        // Show confirmation dialog before deleting
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Product")
            .setMessage("Are you sure you want to delete this product?")
            .setPositiveButton("Yes") { dialog, _ ->
                deleteProductFromFirebase(product)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // Delete product from Firebase
    private fun deleteProductFromFirebase(product: Product) {
        database.child(product.id).removeValue()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Product deleted", Toast.LENGTH_SHORT).show()
                fetchProducts()  // Reload the product list
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to delete product", Toast.LENGTH_SHORT).show()
            }
    }

    // View Product Details
    private fun onViewDetailClick(product: Product) {
        val intent = Intent(context, DashboardProductDetailedActivity::class.java).apply {
            putExtra("productId", product.id)
            putExtra("productName", product.name)
            putExtra("productPrice", product.price)
            putExtra("productQuantity", product.quantity)
            putExtra("productDescription", product.description)
            putExtra("productImageUrl", product.imageUrl)
        }
        startActivity(intent)
    }
}


