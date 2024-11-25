package com.example.campusthrifts

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val studentId: String = "",
    val phoneNumber: String = "",
    val profileImageUrl: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)