package com.example.campusthrifts.models

data class Item(
    val id: String = "",
    val name: String = "",
    val price: Int = 0,
    val description: String = "",
    val imageUrl: String = "",
    val category: String = "",
    val uid: String = "",
    val dateAdded: Long = 0L,
    var isFavorited: Boolean = false,
    var popularityScore: Int = 0

)
