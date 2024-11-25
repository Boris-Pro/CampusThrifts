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

class AddProductActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var storageReference: StorageReference
    private var imageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var binding: ActivityAddProductBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase instances
        database = FirebaseDatabase.getInstance().reference
        storageReference = FirebaseStorage.getInstance().reference

        // Select image button
        binding.btnSelectImage.setOnClickListener {
            openGallery()
        }

        // Upload product button
        binding.btnUploadProduct.setOnClickListener {
            if (imageUri != null && binding.etProductName.text.isNotEmpty() && binding.etProductPrice.text.isNotEmpty()) {
                uploadProductToStorage()
            } else {
                Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show()
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
                    name = binding.etProductName.text.toString(),
                    price = binding.etProductPrice.text.toString().toDouble(),
                    imageUrl = imageUrl,
                    userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                )

                // Store the product in the database under the global 'products' node
                database.child("products").push().setValue(product)
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
}
