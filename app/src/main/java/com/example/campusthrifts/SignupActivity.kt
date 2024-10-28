package com.example.campusthrifts

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.campusthrifts.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("users")

        binding.signupButton.setOnClickListener {
            val email = binding.signupEmail.text.toString()
            val password = binding.signupPassword.text.toString()
            val username = binding.signupUserName.text.toString()
            val studentId = binding.signupStudentId.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && username.isNotEmpty() && studentId.isNotEmpty()) {
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val userId = firebaseAuth.currentUser?.uid

                            // Proceed only if userId is not null
                            userId?.let {
                                val user = User(uid = it, username = username, studentId = studentId, email = email)

                                database.child(it).setValue(user)
                                    .addOnCompleteListener { dbTask ->
                                        if (dbTask.isSuccessful) {
                                            Toast.makeText(this, "Signup successful", Toast.LENGTH_SHORT).show()
                                            startActivity(Intent(this, LoginActivity::class.java))
                                            finish()
                                        } else {
                                            Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }
                        } else {
                            Toast.makeText(this, "Signup failed", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this@SignupActivity, "Empty fields are not allowed", Toast.LENGTH_SHORT).show()
            }
        }


        binding.loginRedirect.setOnClickListener {
            startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
            finish()
        }
    }
}
