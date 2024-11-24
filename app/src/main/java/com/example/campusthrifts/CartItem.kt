package com.example.campusthrifts

data class CartItem(
    val name: String,
    val price: Double,
    var quantity: Int
) {
    fun getTotalPrice(): Double {
        return price * quantity
    }
}