package com.example.campusthrifts

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import coil.load
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.UUID

class ProfileFragment : Fragment() {

    private lateinit var backButton: ImageButton
    private lateinit var profileImageButton: ImageButton
    private lateinit var cameraButton: ImageButton
    private lateinit var saveButton: Button
    private lateinit var removeImageButton: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var firstNameEditText: TextInputEditText
    private lateinit var lastNameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var studentIdEditText: TextInputEditText
    private lateinit var phoneNumberEditText: TextInputEditText

    private var imageUri: Uri? = null

    private val storageReference = FirebaseStorage.getInstance().reference
    private val firestore = FirebaseFirestore.getInstance()
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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI elements
        backButton = view.findViewById(R.id.imageButton)
        profileImageButton = view.findViewById(R.id.imageButton2)
        cameraButton = view.findViewById(R.id.imageButton3)
        saveButton = view.findViewById(R.id.button2)
        removeImageButton = view.findViewById(R.id.removeImageButton)
        progressBar = view.findViewById(R.id.progressBar)

        firstNameEditText = view.findViewById(R.id.textInputEditText)
        lastNameEditText = view.findViewById(R.id.textInputEditText3)
        emailEditText = view.findViewById(R.id.textInputEditText4)
        studentIdEditText = view.findViewById(R.id.textInputEditText2)
        phoneNumberEditText = view.findViewById(R.id.textInputEditText5)

        // Set click listeners
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        cameraButton.setOnClickListener {
            selectImageFromGallery()
        }

        saveButton.setOnClickListener {
            saveProfileSettings()
        }

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
                profileImageButton.load(it) {
                    crossfade(true)
                    placeholder(R.drawable.profile_image)
                    error(R.drawable.profile_image)
                }
                uploadImageToFirebase(it)
            }
        } else {
            Toast.makeText(requireContext(), "Image selection cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageToFirebase(uri: Uri) {
        val currentUser = auth.currentUser ?: run {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        val fileName = "profile_images/$userId/${UUID.randomUUID()}.jpg"
        val fileRef = storageReference.child(fileName)

        progressBar.visibility = View.VISIBLE

        val compressedImage = compressImage(uri) ?: run {
            Toast.makeText(requireContext(), "Image compression failed", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
            return
        }

        fileRef.putBytes(compressedImage)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    saveProfileImageUrl(downloadUri.toString())
                    Toast.makeText(requireContext(), "Profile image uploaded successfully", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
            }
    }

    private fun compressImage(uri: Uri): ByteArray? {
        return try {
            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            outputStream.toByteArray()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun saveProfileImageUrl(url: String) {
        val currentUser = auth.currentUser ?: run {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid

        firestore.collection("users").document(userId)
            .update("profileImageUrl", url)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile image URL saved", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to save profile image URL: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadProfileImage() {
        val currentUser = auth.currentUser ?: run {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
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
                            placeholder(R.drawable.profile_image)
                            error(R.drawable.profile_image)
                        }
                    }
                } else {
                    profileImageButton.setImageResource(R.drawable.profile_image)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to load profile image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeProfileImage() {
        val currentUser = auth.currentUser ?: run {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid

        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.contains("profileImageUrl")) {
                    val url = document.getString("profileImageUrl")
                    url?.let {
                        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(it)
                        storageRef.delete()
                            .addOnSuccessListener {
                                firestore.collection("users").document(userId)
                                    .update("profileImageUrl", FieldValue.delete())
                                    .addOnSuccessListener {
                                        profileImageButton.setImageResource(R.drawable.profile_image)
                                        Toast.makeText(requireContext(), "Profile image removed", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(requireContext(), "Failed to remove profile image URL: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Failed to delete image from storage: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(requireContext(), "No profile image to remove", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to fetch profile image URL: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveProfileSettings() {
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val studentId = studentIdEditText.text.toString().trim()
        val phoneNumber = phoneNumberEditText.text.toString().trim()

        val currentUser = auth.currentUser ?: run {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid

        val userUpdates = hashMapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "studentId" to studentId,
            "phoneNumber" to phoneNumber
        )

        firestore.collection("users").document(userId)
            .update(userUpdates as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile settings saved", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to save profile settings: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}