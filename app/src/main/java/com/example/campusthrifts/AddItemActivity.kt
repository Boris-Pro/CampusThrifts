package com.example.campusthrifts

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class AddItemActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_item)

        // Enable edge-to-edge system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Reference UI elements
        val uploadButton: Button = findViewById(R.id.uploadImageButton)
        val addItemButton: Button = findViewById(R.id.addItemButton)
        val itemImagePreview: ImageView = findViewById(R.id.itemImagePreview)
        val itemNameInput: EditText = findViewById(R.id.itemNameInput)
        val descriptionInput: EditText = findViewById(R.id.descriptionInput)
        val priceField: EditText = findViewById(R.id.priceField)
        val sizeChipGroup: ChipGroup = findViewById(R.id.sizeChipGroup)
        val categoryChipGroup: ChipGroup = findViewById(R.id.categoryChipGroup)
        val temporaryMessage: TextView = findViewById(R.id.temporaryMessage)

        // Temporary message initially hidden
        temporaryMessage.visibility = View.GONE

        // Upload image button logic (replace with actual image picker logic if necessary)
        uploadButton.setOnClickListener {
            // For demonstration, we'll set a placeholder image
            itemImagePreview.setImageResource(R.drawable.placeholderimage)
            Toast.makeText(this, "Image uploaded!", Toast.LENGTH_SHORT).show()
        }

        // Size chip group logic using the updated method for chip group selection
        sizeChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val selectedChipId = checkedIds.firstOrNull()
            val selectedChip = selectedChipId?.let { findViewById<Chip>(it) }
            selectedChip?.let {
                Toast.makeText(this, "Selected size: ${it.text}", Toast.LENGTH_SHORT).show()
            }
        }

        // Category chip group logic using the updated method for chip group selection
        categoryChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val selectedChipId = checkedIds.firstOrNull()
            val selectedChip = selectedChipId?.let { findViewById<Chip>(it) }
            selectedChip?.let {
                Toast.makeText(this, "Selected category: ${it.text}", Toast.LENGTH_SHORT).show()
            }
        }

        // Upload item button logic
        addItemButton.setOnClickListener {
            // Collect all entered data
            val itemName = itemNameInput.text.toString()
            val description = descriptionInput.text.toString()
            val price = priceField.text.toString()
            val selectedSizeId = sizeChipGroup.checkedChipId
            val selectedCategoryId = categoryChipGroup.checkedChipId
            val selectedSize = findViewById<Chip>(selectedSizeId)?.text
            val selectedCategory = findViewById<Chip>(selectedCategoryId)?.text

            // Validate input
            if (itemName.isEmpty() || description.isEmpty() || price.isEmpty() || selectedSize == null || selectedCategory == null) {
                Toast.makeText(this, "Please fill in all fields!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Hide all fields and show temporary message
            findViewById<View>(R.id.main).visibility = View.GONE
            temporaryMessage.visibility = View.VISIBLE
            temporaryMessage.text = getString(R.string.item_added_message)

            // Optionally, reset fields after some delay (e.g., 2 seconds)
            temporaryMessage.postDelayed({
                findViewById<View>(R.id.main).visibility = View.VISIBLE
                temporaryMessage.visibility = View.GONE
                itemImagePreview.setImageResource(R.drawable.placeholderimage)
                itemNameInput.text.clear()
                descriptionInput.text.clear()
                priceField.text.clear()
                sizeChipGroup.clearCheck()
                categoryChipGroup.clearCheck()
            }, 2000)
        }
    }
}
