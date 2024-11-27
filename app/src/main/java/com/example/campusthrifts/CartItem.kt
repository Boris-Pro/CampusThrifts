package com.example.campusthrifts

data class CartItem(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    var quantity: Int = 0
) {
    fun getTotalPrice(): Double {
        return price * quantity
    }
}