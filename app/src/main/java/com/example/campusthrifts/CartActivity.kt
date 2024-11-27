package com.example.campusthrifts

import android.os.Bundle
import android.widget.Toast
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

class CartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        val userId =intent.getStringExtra("USER_ID")

        if (savedInstanceState == null) {
            val cartFragment = CartFragment().apply {
                arguments = Bundle().apply {
                    putString("USER_ID", userId)
                }
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, cartFragment)
                .commit()
        }
    }
}