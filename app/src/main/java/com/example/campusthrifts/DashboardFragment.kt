package com.example.campusthrifts

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID


class DashboardFragment : Fragment() {
    private lateinit var productNameInput: EditText
    private lateinit var productPriceInput: EditText
    private lateinit var productDescriptionInput: EditText
    private lateinit var productCategorySpinner: Spinner
    private lateinit var selectImageButton: Button
    private lateinit var submitProductButton: Button

    private var selectedImageUri: Uri? = null

    // Category options for the spinner
    private val categories = arrayOf("Clothes", "Furniture", "Electronics", "Appliance", "TextBooks", "Other")

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val currentUser: FirebaseUser? = firebaseAuth.currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        // Initialize views
        productNameInput = view.findViewById(R.id.productNameInput)
        productPriceInput = view.findViewById(R.id.productPriceInput)
        productDescriptionInput = view.findViewById(R.id.productDescriptionInput)
        productCategorySpinner = view.findViewById(R.id.productCategorySpinner)
        selectImageButton = view.findViewById(R.id.selectImageButton)
        submitProductButton = view.findViewById(R.id.submitProductButton)

        // Set up the category spinner
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        productCategorySpinner.adapter = adapter

        // Set button listeners
        selectImageButton.setOnClickListener {
            openImagePicker()
        }

        submitProductButton.setOnClickListener {
            uploadProductToFirebase()
        }

        return view
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 1001)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == AppCompatActivity.RESULT_OK) {
            selectedImageUri = data?.data
        }
    }

    private fun uploadProductToFirebase() {
        val productName = productNameInput.text.toString().trim()
        val productPrice = productPriceInput.text.toString().trim()
        val productDescription = productDescriptionInput.text.toString().trim()
        val productCategory = productCategorySpinner.selectedItem.toString().trim()
        val dateAdded = System.currentTimeMillis() // Current timestamp for dateAdded

        // Get the current user's UID
        val userUid = currentUser?.uid

        if (productName.isEmpty() || productPrice.isEmpty() || productDescription.isEmpty() || selectedImageUri == null || userUid == null) {
            Toast.makeText(requireContext(), "Please fill all fields and select an image.", Toast.LENGTH_SHORT).show()
            return
        }

        // Upload image to Firebase Storage
        val storageReference = FirebaseStorage.getInstance().reference.child("product_images/${UUID.randomUUID()}.jpg")
        storageReference.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                storageReference.downloadUrl.addOnSuccessListener { imageUrl ->
                    // Save product to Firebase Database
                    val databaseReference = FirebaseDatabase.getInstance().reference.child("products")
                    val productId = databaseReference.push().key
                    val product = Products(
                        id = productId,
                        name = productName,
                        price = productPrice.toDouble(),
                        description = productDescription,
                        imageUrl = imageUrl.toString(),
                        category = productCategory,
                        dateAdded = dateAdded,
                        uid = userUid // Add the user UID here
                    )

                    databaseReference.child(productId!!).setValue(product)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(requireContext(), "Product added successfully!", Toast.LENGTH_SHORT).show()
                                requireActivity().onBackPressed()
                            } else {
                                Toast.makeText(requireContext(), "Failed to add product.", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Image upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}