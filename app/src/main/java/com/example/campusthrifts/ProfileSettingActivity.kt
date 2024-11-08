// ProfileSettingActivity.kt

package com.example.campusthrifts

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import coil.load // Ensure Coil's load extension is imported
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.UUID

class ProfileSettingActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton // imageButton
    private lateinit var profileImageButton: ImageButton // imageButton2
    private lateinit var cameraButton: ImageButton // imageButton3
    private lateinit var saveButton: Button // button2
    private lateinit var removeImageButton: Button // removeImageButton
    private lateinit var progressBar: ProgressBar

    // TextInputEditTexts
    private lateinit var firstNameEditText: TextInputEditText // textInputEditText
    private lateinit var lastNameEditText: TextInputEditText // textInputEditText3
    private lateinit var emailEditText: TextInputEditText // textInputEditText4
    private lateinit var studentIdEditText: TextInputEditText // textInputEditText2
    private lateinit var phoneNumberEditText: TextInputEditText // textInputEditText5

    private var imageUri: Uri? = null

    private val storageReference = FirebaseStorage.getInstance().reference
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setting) // Ensure the layout file name is correct

        // Initialize UI elements
        backButton = findViewById(R.id.imageButton)
        profileImageButton = findViewById(R.id.imageButton2)
        cameraButton = findViewById(R.id.imageButton3)
        saveButton = findViewById(R.id.button2)
        removeImageButton = findViewById(R.id.removeImageButton)
        progressBar = findViewById(R.id.progressBar)

        firstNameEditText = findViewById(R.id.textInputEditText)
        lastNameEditText = findViewById(R.id.textInputEditText3)
        emailEditText = findViewById(R.id.textInputEditText4)
        studentIdEditText = findViewById(R.id.textInputEditText2)
        phoneNumberEditText = findViewById(R.id.textInputEditText5)

        // Initialize the image picker launcher
        imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            handleImagePickerResult(result.resultCode, result.data)
        }

        // Set click listener for back button to finish the activity
        backButton.setOnClickListener {
            finish()
        }

        // Set click listener to open image picker via cameraButton (imageButton3)
        cameraButton.setOnClickListener {
            selectImageFromGallery()
        }

        // Set click listener for save button
        saveButton.setOnClickListener {
            saveProfileSettings()
        }

        // Set click listener for remove image button
        removeImageButton.setOnClickListener {
            removeProfileImage()
        }

        // Load existing profile image
        loadProfileImage()
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun handleImagePickerResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            imageUri?.let {
                // Display the selected image using Coil
                profileImageButton.load(it) {
                    crossfade(true)
                    placeholder(R.drawable.profile_image) // Ensure you have a placeholder drawable
                    error(R.drawable.profile_image)
                }
                // Upload the image
                uploadImageToFirebase(it)
            }
        } else {
            Toast.makeText(this, "Image selection cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageToFirebase(uri: Uri) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        val fileName = "profile_images/$userId/${UUID.randomUUID()}.jpg"
        val fileRef = storageReference.child(fileName)

        // Show ProgressBar
        progressBar.visibility = View.VISIBLE

        // Optionally compress the image
        val compressedImage = compressImage(uri)
        if (compressedImage == null) {
            Toast.makeText(this, "Image compression failed", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
            return
        }

        fileRef.putBytes(compressedImage)
            .addOnSuccessListener {
                // Get download URL
                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    // Save URL to Firestore
                    saveProfileImageUrl(downloadUri.toString())
                    Toast.makeText(this, "Profile image uploaded successfully", Toast.LENGTH_SHORT).show()
                    // Hide ProgressBar
                    progressBar.visibility = View.GONE
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                // Hide ProgressBar
                progressBar.visibility = View.GONE
            }
    }

    private fun compressImage(uri: Uri): ByteArray? {
        return try {
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream) // Compress to 80% quality
            outputStream.toByteArray()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun saveProfileImageUrl(url: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid

        firestore.collection("users").document(userId)
            .update("profileImageUrl", url)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile image URL saved", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save profile image URL: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadProfileImage() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid

        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.contains("profileImageUrl")) {
                    val url = document.getString("profileImageUrl")
                    url?.let {
                        profileImageButton.load(it) {
                            crossfade(true)
                            placeholder(R.drawable.profile_image) // Ensure you have a placeholder drawable
                            error(R.drawable.profile_image)
                        }
                    }
                } else {
                    // Optionally set a default image if none exists
                    profileImageButton.setImageResource(R.drawable.profile_image)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load profile image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeProfileImage() {
        val currentUser = auth.currentUser ?: run {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid

        // Fetch the current profileImageUrl from Firestore
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.contains("profileImageUrl")) {
                    val url = document.getString("profileImageUrl")
                    url?.let {
                        // Delete the image from Firebase Storage
                        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(it)
                        storageRef.delete()
                            .addOnSuccessListener {
                                // Remove the URL from Firestore
                                firestore.collection("users").document(userId)
                                    .update("profileImageUrl", FieldValue.delete())
                                    .addOnSuccessListener {
                                        // Set default image
                                        profileImageButton.setImageResource(R.drawable.profile_image)
                                        Toast.makeText(this, "Profile image removed", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Failed to remove profile image URL: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to delete image from storage: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "No profile image to remove", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch profile image URL: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveProfileSettings() {
        // Implement saving of other profile settings like first name, last name, email, etc.
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val studentId = studentIdEditText.text.toString().trim()
        val phoneNumber = phoneNumberEditText.text.toString().trim()

        // Validate inputs as needed

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid

        val userUpdates = hashMapOf<String, Any>(
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "studentId" to studentId,
            "phoneNumber" to phoneNumber
            // Add other fields as necessary
        )

        firestore.collection("users").document(userId)
            .update(userUpdates)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile settings saved", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save profile settings: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
