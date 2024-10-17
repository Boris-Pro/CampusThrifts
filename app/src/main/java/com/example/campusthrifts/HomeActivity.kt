package com.example.campusthrifts

import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    private lateinit var recyclerViewPopular: RecyclerView
    private lateinit var recyclerViewNewCollections: RecyclerView
    private lateinit var recyclerViewCategories: RecyclerView
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var cartIcon: ImageView
    private lateinit var userProfilePhoto: ImageView
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize views
        cartIcon = findViewById(R.id.cartIcon)
        userProfilePhoto = findViewById(R.id.userProfilePhoto)
        searchView = findViewById(R.id.searchView)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        recyclerViewPopular = findViewById(R.id.recyclerViewPopular)
        recyclerViewNewCollections = findViewById(R.id.recyclerViewNewCollections)
        recyclerViewCategories = findViewById(R.id.recyclerViewCategories)

        // Set up Toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Handle Toolbar Navigation Clicks
        toolbar.setNavigationOnClickListener {
            // Handle hamburger menu click
            Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show()
        }

        // Set up RecyclerViews
        recyclerViewPopular.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewNewCollections.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewCategories.layoutManager = LinearLayoutManager(this)

        // Data for RecyclerViews
        val popularItems = listOf(
            Item("Water Bottle", "Stainless steel", 500.0, R.drawable.placeholderimage),
            Item("Pack of Markers", "Set of 10", 300.0, R.drawable.placeholderimage)
        )

        val newCollectionsItems = listOf(
            Item("Macbook", "2020 Model", 60000.0, R.drawable.placeholderimage),
            Item("White T-Shirt", "Comfortable fit", 500.0, R.drawable.placeholderimage)
        )

        val categoryList = listOf(
            Category("Electronics", R.drawable.placeholderimage),
            Category("Clothing", R.drawable.placeholderimage),
            Category("Books", R.drawable.placeholderimage)
        )

        // Set adapters for RecyclerViews
        recyclerViewPopular.adapter = ItemAdapter(popularItems)
        recyclerViewNewCollections.adapter = ItemAdapter(newCollectionsItems)
        recyclerViewCategories.adapter = CategoryAdapter(categoryList)

        // Set up Bottom Navigation
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already on home
                    true
                }
                R.id.nav_add -> {
                    Toast.makeText(this, "Add Item Clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_wishlist -> {
                    Toast.makeText(this, "Wishlist Clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_profile -> {
                    Toast.makeText(this, "Profile Clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        // Set up click listeners
        cartIcon.setOnClickListener {
            // Handle cart icon click
            Toast.makeText(this, "Cart Clicked", Toast.LENGTH_SHORT).show()
        }

        userProfilePhoto.setOnClickListener {
            // Handle profile photo click
            Toast.makeText(this, "Profile Clicked", Toast.LENGTH_SHORT).show()
        }

        // Handle search queries
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Handle search query submission
                Toast.makeText(this@HomeActivity, "Searching for $query", Toast.LENGTH_SHORT).show()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Handle search text changes (e.g., filter list)
                return false
            }
        })
    }
}
