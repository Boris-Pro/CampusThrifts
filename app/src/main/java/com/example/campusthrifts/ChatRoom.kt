package com.example.campusthrifts

data class ChatRoom(
    val id: String = "",
    val participants: List<String> = listOf(),
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = 0,
    val createdAt: Long = 0
)