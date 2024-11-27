package com.example.campusthrifts

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class DashboardActivity : AppCompatActivity() {

    // Declare variables for the views
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var sizeGroup: RadioGroup
    private lateinit var chipGroup: ChipGroup
    private lateinit var itemDescription: EditText
    private lateinit var itemPrice: EditText
    private lateinit var uploadButton: Button
    private lateinit var selectedSize: String
    private lateinit var selectedCategory: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        // Initialize views from the XML layout
        toolbar = findViewById(R.id.toolbar)
        sizeGroup = findViewById(R.id.sizeGroup)
        chipGroup = findViewById(R.id.categoryGroup)
        itemDescription = findViewById(R.id.itemDescription)
        itemPrice = findViewById(R.id.itemPrice)
        uploadButton = findViewById(R.id.uploadButton)

        // Handle size selection from RadioGroup
        sizeGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedSize = when (checkedId) {
                R.id.sizeXS -> "XS"
                R.id.sizeS -> "S"
                R.id.sizeM -> "M"
                R.id.sizeL -> "L"
                R.id.sizeXL -> "XL"
                else -> ""
            }
        }

        // Handle category selection from ChipGroup
        chipGroup.setOnCheckedChangeListener { _, checkedId ->
            val chip: Chip? = findViewById(checkedId)
            selectedCategory = chip?.text.toString()
        }

        // Handle upload button click
        uploadButton.setOnClickListener {
            val description = itemDescription.text.toString()
            val price = itemPrice.text.toString()

            // Check if all fields are filled
            if (description.isEmpty() || price.isEmpty() || selectedSize.isEmpty() || selectedCategory.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            } else {
                // Simulate item upload (or process data as needed)
                Toast.makeText(this, "Item uploaded successfully!", Toast.LENGTH_SHORT).show()
            }
        }

        // Handling window insets for padding
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
