package com.example.campusthrifts

data class Product(
    val id: String = "",              // Unique product ID
    val name: String = "",            // Product name
    val price: Double = 0.0,          // Product price
    val imageUrl: String = "",        // URL of the product image
    val userId: String = "",          // User ID of the seller
    val quantity: Int = 0,            // Quantity available
    val description: String = "",     // Product description
    val category: String = ""         // Product category
)