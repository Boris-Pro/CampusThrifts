package com.example.campusthrifts

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
//import androidx.compose.ui.semantics.text
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var userEmailTextView: TextView
    private lateinit var btnLogout: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        userEmailTextView = findViewById(R.id.userEmailTextView)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            userEmailTextView.text = currentUser.email
        } else {
            userEmailTextView.text = "No user logged in"
        }
        btnLogout = findViewById(R.id.logoutButton)
        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}