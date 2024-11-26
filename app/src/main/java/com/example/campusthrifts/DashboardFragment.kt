package com.example.campusthrifts

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import coil.load
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.UUID



class DashboardFragment : Fragment() {
    private lateinit var selectImageButton: Button
    private lateinit var productImageView: ImageView
    private lateinit var productNameEditText: EditText
    private lateinit var productDescriptionEditText: EditText
    private lateinit var productPriceEditText: EditText
    private lateinit var productCategorySpinner: Spinner
    private lateinit var saveProductButton: Button

    private var productImageUri: Uri? = null

    private val storageReference = FirebaseStorage.getInstance().reference
    private val databaseReference = FirebaseDatabase.getInstance().reference.child("items")
    private val auth = FirebaseAuth.getInstance()

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            handleImagePickerResult(result.resultCode, result.data)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectImageButton = view.findViewById(R.id.selectImageButton)
        productImageView = view.findViewById(R.id.productImageView)
        productNameEditText = view.findViewById(R.id.productNameEditText)
        productDescriptionEditText = view.findViewById(R.id.productDescriptionEditText)
        productPriceEditText = view.findViewById(R.id.productPriceEditText)
        productCategorySpinner = view.findViewById(R.id.productCategorySpinner)
        saveProductButton = view.findViewById(R.id.saveProductButton)

        setupCategorySpinner()

        selectImageButton.setOnClickListener {
            selectImageFromGallery()
        }

        saveProductButton.setOnClickListener {
            saveProduct()
        }
    }

    private fun setupCategorySpinner() {
        val categories = arrayOf("Clothes","Shoes", "Furniture", "Electronics", "Books","Appliances", "Other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        productCategorySpinner.adapter = adapter
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun handleImagePickerResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            productImageUri = data.data
            productImageUri?.let {
                productImageView.load(it)
            }
        } else {
            Toast.makeText(requireContext(), "Image selection cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveProduct() {
        val name = productNameEditText.text.toString().trim()
        val description = productDescriptionEditText.text.toString().trim()
        val priceText = productPriceEditText.text.toString().trim()
        val category = productCategorySpinner.selectedItem.toString()
        val price = priceText.toDoubleOrNull()

        if (name.isEmpty() || description.isEmpty() || price == null || category.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid ?: return
        val timestamp = System.currentTimeMillis()

        if (productImageUri != null) {
            uploadImageAndSaveProduct(name, description, price, category, userId, timestamp)
        } else {
            saveProductToDatabase(name, description, price, null, category, userId, timestamp)
        }
    }

    private fun uploadImageAndSaveProduct(
        name: String,
        description: String,
        price: Double,
        category: String,
        userId: String,
        timestamp: Long
    ) {
        val productId = UUID.randomUUID().toString()
        val imageRef = storageReference.child("product_images/$productId.jpg")

        val compressedImage = compressImage(productImageUri!!)

        imageRef.putBytes(compressedImage)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveProductToDatabase(name, description, price, uri.toString(), category, userId, timestamp)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun compressImage(uri: Uri): ByteArray {
        val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return outputStream.toByteArray()
    }

    private fun saveProductToDatabase(
        name: String,
        description: String,
        price: Double,
        imageUrl: String?,
        category: String,
        userId: String,
        timestamp: Long
    ) {
        val productId = databaseReference.push().key ?: return
        val product = Products(
            id = productId,
            name = name,
            price = price,
            description = description,
            imageUrl = imageUrl,
            category = category,
            dateAdded = timestamp,
            uid = userId
        )

        databaseReference.child(productId).setValue(product)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Product added successfully", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressed()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to add product: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}