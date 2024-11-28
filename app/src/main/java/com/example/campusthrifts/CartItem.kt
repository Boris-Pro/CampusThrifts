package com.example.campusthrifts

data class CartItem(
    val productId: String? = null,   // Null by default
    val name: String? = null,         // Null by default
    val price: Double? = null,        // Null by default
    val quantity: Int = 1,            // Default to 1
    val imageUrl: String? = null      // Null by default
) {
    constructor() : this(null, null, null, 1, null)
}