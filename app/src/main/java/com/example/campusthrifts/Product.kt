package com.example.campusthrifts

data class Product(
    val id: String = "", // Unique identifier for the product
    val name: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val userId: String = "",
    val quantity: Int = 0, // Quantity of the product
    val description: String = "" // Description of the product
)