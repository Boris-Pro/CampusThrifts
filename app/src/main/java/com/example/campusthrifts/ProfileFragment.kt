package com.example.campusthrifts

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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
    private lateinit var usernameEditText: TextInputEditText
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
        usernameEditText = view.findViewById(R.id.usernameText)
        emailEditText = view.findViewById(R.id.textInputEditText4)
        studentIdEditText = view.findViewById(R.id.textInputEditText2)
        phoneNumberEditText = view.findViewById(R.id.textInputEditText5)

        // Load user profile data when the fragment is created
        loadUserProfile()

        // Set click listeners
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        cameraButton.setOnClickListener {
            checkAndRequestPermissions()
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

    private fun loadUserProfile() {
        val currentUser = auth.currentUser ?: run {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        firestore.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                progressBar.visibility = View.GONE
                if (document.exists()) {
                    try {
                        val user = document.toObject(User::class.java)
                        user?.let { populateUserData(it) }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Error parsing user data", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    createNewUserDocument(currentUser.uid, currentUser.email)
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Failed to load profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun populateUserData(user: User) {
        firstNameEditText.setText(user.firstName)
        lastNameEditText.setText(user.lastName)
        usernameEditText.setText(user.username)
        emailEditText.setText(user.email)
        studentIdEditText.setText(user.studentId)
        phoneNumberEditText.setText(user.phoneNumber)

        if (user.profileImageUrl.isNotEmpty()) {
            profileImageButton.load(user.profileImageUrl) {
                crossfade(true)
                placeholder(R.drawable.profile_image)
                error(R.drawable.profile_image)
            }
        }
    }

    private fun createNewUserDocument(userId: String, email: String?) {
        val newUser = User(
            uid = userId,
            username = "",
            email = email ?: "",
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )

        progressBar.visibility = View.VISIBLE
        firestore.collection("users").document(userId)
            .set(newUser)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Profile created successfully", Toast.LENGTH_SHORT).show()
                populateUserData(newUser)
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Failed to create profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkAndRequestPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13 and above
            when {
                requireContext().checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES)
                        == PackageManager.PERMISSION_GRANTED -> {
                    selectImageFromGallery()
                }
                else -> {
                    requestPermissions(
                        arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES),
                        PERMISSION_REQUEST_CODE
                    )
                }
            }
        } else {
            // Below Android 13
            when {
                requireContext().checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED -> {
                    selectImageFromGallery()
                }
                else -> {
                    requestPermissions(
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        PERMISSION_REQUEST_CODE
                    )
                }
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectImageFromGallery()
                } else {
                    Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun handleImagePickerResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            imageUri?.let {
                // Don't update the UI here, let it update after successful upload
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
                    // Update the UI immediately with the new image
                    profileImageButton.load(downloadUri) {
                        crossfade(true)
                        placeholder(R.drawable.profile_image)
                        error(R.drawable.profile_image)
                    }

                    // Save the URL to Firestore
                    saveProfileImageUrl(downloadUri.toString())
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
        val currentUser = auth.currentUser ?: return

        val updates = hashMapOf<String, Any>(
            "profileImageUrl" to url,
            "updatedAt" to Timestamp.now()
        )

        progressBar.visibility = View.VISIBLE
        firestore.collection("users").document(currentUser.uid)
            .update(updates)  // Use update instead of set to prevent overwriting other fields
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Profile image updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Failed to update profile image: ${e.message}", Toast.LENGTH_SHORT).show()
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
        val currentUser = auth.currentUser ?: run {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val username = usernameEditText.text.toString().trim()

        // Validate username
        if (username.isEmpty()) {
            usernameEditText.error = "Username cannot be empty"
            return
        }

        if (!isValidUsername(username)) {
            usernameEditText.error =
                "Username must be 3-20 characters long and contain only letters, numbers, and underscores"
            return
        }

        progressBar.visibility = View.VISIBLE

        checkUsernameAvailability(username) { isAvailable ->
            requireActivity().runOnUiThread {
                if (isAvailable) {
                    proceedWithSaving(currentUser.uid, username)
                } else {
                    progressBar.visibility = View.GONE
                    usernameEditText.error = "Username is already taken"
                    Toast.makeText(
                        requireContext(),
                        "Username is already taken",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun checkUsernameAvailability(username: String, callback: (Boolean) -> Unit) {
        val currentUser = auth.currentUser ?: run {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            callback(false)
            return
        }

        progressBar.visibility = View.VISIBLE

        // First check if this is the user's current username
        firestore.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { currentUserDoc ->
                val currentUsername = currentUserDoc.getString("username") ?: ""

                // If the username hasn't changed, no need to check availability
                if (currentUsername == username) {
                    progressBar.visibility = View.GONE
                    callback(true)
                    return@addOnSuccessListener
                }

                // Check if username is taken by another user
                firestore.collection("users")
                    .whereEqualTo("username", username)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        progressBar.visibility = View.GONE
                        callback(querySnapshot.isEmpty)
                    }
                    .addOnFailureListener { e ->
                        progressBar.visibility = View.GONE
                        Log.e("ProfileFragment", "Username check failed", e)
                        Toast.makeText(
                            requireContext(),
                            "Failed to check username availability: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        callback(false)
                    }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Log.e("ProfileFragment", "Current user fetch failed", e)
                Toast.makeText(
                    requireContext(),
                    "Failed to fetch current user data: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                callback(false)
            }
    }

    private fun proceedWithSaving(userId: String, username: String) {
        progressBar.visibility = View.VISIBLE

        // First fetch the existing document
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val existingCreatedAt = document.getTimestamp("createdAt")
                val existingUid = document.getString("uid")

                val updates = hashMapOf(
                    "firstName" to firstNameEditText.text.toString().trim(),
                    "lastName" to lastNameEditText.text.toString().trim(),
                    "username" to username,
                    "email" to emailEditText.text.toString().trim(),
                    "studentId" to studentIdEditText.text.toString().trim(),
                    "phoneNumber" to phoneNumberEditText.text.toString().trim(),
                    "updatedAt" to Timestamp.now(),
                    // Preserve the original createdAt and uid
                    "createdAt" to (existingCreatedAt ?: Timestamp.now()),
                    "uid" to (existingUid ?: userId)
                )

                firestore.collection("users").document(userId)
                    .set(updates, SetOptions.merge())  // Use merge to preserve any other fields
                    .addOnSuccessListener {
                        progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        progressBar.visibility = View.GONE
                        Log.e("ProfileFragment", "Profile update failed", e)
                        Toast.makeText(
                            requireContext(),
                            "Failed to update profile: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Log.e("ProfileFragment", "Failed to fetch existing data", e)
                Toast.makeText(
                    requireContext(),
                    "Failed to fetch existing data: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun isValidUsername(username: String): Boolean {
        // Username should be 3-20 characters long
        // Only allow letters, numbers, and underscores
        val usernamePattern = "^[a-zA-Z0-9_]{3,20}$"
        return username.matches(usernamePattern.toRegex())
    }


}