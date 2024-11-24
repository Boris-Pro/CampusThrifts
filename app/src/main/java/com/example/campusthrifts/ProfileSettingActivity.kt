package com.example.campusthrifts

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import android.view.View
import androidx.core.view.WindowInsetsCompat

class ProfileSettingActivity : AppCompatActivity() {

    private lateinit var editTextName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextOldPassword: EditText
    private lateinit var editTextNewPassword: EditText
    private lateinit var editTextConfirmPassword: EditText
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setting)


        editTextName = findViewById(R.id.editTextName)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextOldPassword = findViewById(R.id.editTextOldPassword)
        editTextNewPassword = findViewById(R.id.editTextNewPassword)
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword)


        sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE)


        loadProfile()


        findViewById<View>(R.id.buttonSave).setOnClickListener {
            saveProfile()
        }
    }

    private fun loadProfile() {

        val name = sharedPreferences.getString("name", "Enter your name")
        val email = sharedPreferences.getString("email", "Enter your email")
        val password = sharedPreferences.getString("password", "")


        editTextName.setText(name)
        editTextEmail.setText(email)
    }

    private fun saveProfile() {

        val oldPassword = editTextOldPassword.text.toString()
        val newPassword = editTextNewPassword.text.toString()
        val confirmPassword = editTextConfirmPassword.text.toString()
        val name = editTextName.text.toString()
        val email = editTextEmail.text.toString()


        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty() || name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            return
        }


        val storedPassword = sharedPreferences.getString("password", "")


        if (oldPassword != storedPassword) {
            Toast.makeText(this, "Old password is incorrect", Toast.LENGTH_SHORT).show()
            return
        }


        if (newPassword != confirmPassword) {
            Toast.makeText(this, "New password and confirmation do not match", Toast.LENGTH_SHORT).show()
            return
        }


        val editor = sharedPreferences.edit()
        editor.putString("name", name)
        editor.putString("email", email)
        editor.putString("password", newPassword)
        editor.apply()


        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
    }
}