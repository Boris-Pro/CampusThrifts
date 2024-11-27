package com.example.campusthrifts

data class Product(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val userId: String = "",
    val quantity: Int = 0,
    val description: String = "",
    val category: String = "",
    val dateAdded: Long = System.currentTimeMillis()
)