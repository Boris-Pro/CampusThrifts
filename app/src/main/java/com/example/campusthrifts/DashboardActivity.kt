package com.example.appname

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Handle Back Button with the new back press dispatcher
        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            // Use the back press dispatcher
            onBackPressedDispatcher.onBackPressed() // This is the replacement for the deprecated onBackPressed()
        }

        // Handle Size Button Clicks
        val buttonXXS = findViewById<Button>(R.id.buttonXXS)
        val buttonXS = findViewById<Button>(R.id.buttonXS)
        val buttonS = findViewById<Button>(R.id.buttonS)
        val buttonM = findViewById<Button>(R.id.buttonM)
        val buttonL = findViewById<Button>(R.id.buttonL)
        val buttonXL = findViewById<Button>(R.id.buttonXL)

        // Set click listeners for size buttons (you can define actions here)
        buttonXXS.setOnClickListener { /* Handle click for XXS */ }
        buttonXS.setOnClickListener { /* Handle click for XS */ }
        buttonS.setOnClickListener { /* Handle click for S */ }
        buttonM.setOnClickListener { /* Handle click for M */ }
        buttonL.setOnClickListener { /* Handle click for L */ }
        buttonXL.setOnClickListener { /* Handle click for XL */ }

        // Handle Category Button Clicks
        val buttonClothes = findViewById<Button>(R.id.buttonClothes)
        val buttonElectronics = findViewById<Button>(R.id.buttonElectronics)
        val buttonAccessories = findViewById<Button>(R.id.buttonAccessories)
        val buttonBooks = findViewById<Button>(R.id.buttonBooks)
        val buttonPersonalCare = findViewById<Button>(R.id.buttonPersonalCare)
        val buttonShoes = findViewById<Button>(R.id.buttonShoes)

        // Set click listeners for category buttons (you can define actions here)
        buttonClothes.setOnClickListener { /* Handle click for Clothes */ }
        buttonElectronics.setOnClickListener { /* Handle click for Electronics */ }
        buttonAccessories.setOnClickListener { /* Handle click for Accessories */ }
        buttonBooks.setOnClickListener { /* Handle click for Books */ }
        buttonPersonalCare.setOnClickListener { /* Handle click for Personal Care */ }
        buttonShoes.setOnClickListener { /* Handle click for Shoes */ }

        // Handle Upload Button Click
        val buttonUpload = findViewById<Button>(R.id.buttonUpload)
        buttonUpload.setOnClickListener {
            // You can add upload logic here, for now it will just print a message
            // For example, you can launch an intent to select an image or start an upload
            println("Upload button clicked")
        }

        // Bottom Navigation Icons
        val homeIcon = findViewById<ImageView>(R.id.homeIcon)
        val heartIcon = findViewById<ImageView>(R.id.heartIcon)
        val uploadIcon = findViewById<ImageView>(R.id.uploadIcon)

        // Set click listeners for bottom navigation icons
        homeIcon.setOnClickListener {
            // Check if the HomeActivity exists. Create this activity if needed.
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        heartIcon.setOnClickListener {
            // Check if the FavoritesActivity exists. Create this activity if needed.
            val intent = Intent(this, FavoritesActivity::class.java)
            startActivity(intent)
        }

        uploadIcon.setOnClickListener {
            // Stay in current activity or handle specific logic
            println("Upload Icon clicked")
        }
    }
}
