package com.example.campusthrifts

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.campusthrifts.databinding.ActivityAddProductBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.bumptech.glide.Glide

class AddProductActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var storageReference: StorageReference
    private var imageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var binding: ActivityAddProductBinding
    private var isEditing = false
    private var existingImageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase instances
        database = FirebaseDatabase.getInstance().reference
        storageReference = FirebaseStorage.getInstance().reference

        // Get product details from intent if available (for editing)
        val productId = intent.getStringExtra("productId")
        if (productId != null) {
            isEditing = true
            // Fetch and load product details for editing
            loadProductForEditing(productId)
        }

        // Select image button (only enabled for new products)
        if (isEditing) {
            binding.btnSelectImage.isEnabled = false // Disable image selection when editing
        } else {
            binding.btnSelectImage.setOnClickListener {
                openGallery()
            }
        }

        // Upload product button (will either add or edit based on the presence of productId)
        binding.btnUploadProduct.setOnClickListener {
            if (binding.etProductName.text.isNotEmpty() && binding.etProductPrice.text.isNotEmpty()
                && binding.etProductQuantity.text.isNotEmpty() && binding.etProductDescription.text.isNotEmpty()) {

                if (isEditing) {
                    editProductInDatabase(productId ?: "") // Edit existing product
                } else {
                    if (imageUri != null) {
                        uploadProductToStorage() // Add new product
                    } else {
                        Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Open gallery to select an image
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // Handle the image selection result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            binding.ivSelectedImage.setImageURI(imageUri) // Display selected image
        }
    }

    // Upload product to Firebase Storage
    private fun uploadProductToStorage() {
        val filePath = imageUri?.let { storageReference.child("product_images/${System.currentTimeMillis()}.jpg") }

        filePath?.putFile(imageUri!!)?.addOnSuccessListener {
            filePath.downloadUrl.addOnSuccessListener { uri ->
                // Get the download URL of the image
                val imageUrl = uri.toString()

                // Create a product object and save it to the database
                val product = Product(
                    id = database.child("products").push().key ?: "", // Generate a unique ID
                    name = binding.etProductName.text.toString(),
                    price = binding.etProductPrice.text.toString().toDouble(),
                    imageUrl = imageUrl,
                    userId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                    quantity = binding.etProductQuantity.text.toString().toInt(),
                    description = binding.etProductDescription.text.toString()
                )

                // Store the product in the database under the 'products' node
                database.child("products").child(product.id).setValue(product)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Product added successfully", Toast.LENGTH_SHORT).show()
                        finish() // Close the activity
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to add product", Toast.LENGTH_SHORT).show()
                    }
            }
        }?.addOnFailureListener {
            Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
        }
    }

    // Load product details for editing
    private fun loadProductForEditing(productId: String) {
        val productRef = database.child("products").child(productId)
        productRef.get().addOnSuccessListener { snapshot ->
            val product = snapshot.getValue(Product::class.java)
            if (product != null) {
                binding.etProductName.setText(product.name)
                binding.etProductPrice.setText(product.price.toString())
                binding.etProductQuantity.setText(product.quantity.toString())
                binding.etProductDescription.setText(product.description)

                existingImageUrl = product.imageUrl // Store the existing image URL
                Glide.with(this)
                    .load(product.imageUrl)
                    .into(binding.ivSelectedImage) // Display existing image
            }
        }
    }

    // Edit the product in Firebase Database
    private fun editProductInDatabase(productId: String) {
        val updatedProduct = Product(
            id = productId,
            name = binding.etProductName.text.toString(),
            price = binding.etProductPrice.text.toString().toDouble(),
            quantity = binding.etProductQuantity.text.toString().toInt(),
            description = binding.etProductDescription.text.toString(),
            userId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
            imageUrl = existingImageUrl ?: "" // Keep the existing image URL
        )

        // Update product details in Firebase
        database.child("products").child(productId).setValue(updatedProduct)
            .addOnSuccessListener {
                Toast.makeText(this, "Product updated successfully", Toast.LENGTH_SHORT).show()
                finish() // Close the activity
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update product", Toast.LENGTH_SHORT).show()
            }
    }
}
