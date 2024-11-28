// Category.kt package com.example.campusthrifts.models
package com.example.campusthrifts.models

data class Category(
    val name: String = "",
    val items: Map<String, Boolean>? = null
)
