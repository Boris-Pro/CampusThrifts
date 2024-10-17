package com.example.campusthrifts

import android.os.Bundle
import android.widget.ImageButton
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChatScreenActivity : AppCompatActivity() {

    private lateinit var messageAdapter: MessageAdapter
    private lateinit var recyclerViewMessages: RecyclerView
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSend: ImageButton

    // A sample list of messages for demonstration purposes
    private val messages = mutableListOf<Message>(
        Message("Hi!", "12:00 PM", true),
        Message("Hello! How are you?", "12:01 PM", false),
        Message("I'm good, thank you!", "12:02 PM", true)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_screen)

        // Initialize views
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages)
        editTextMessage = findViewById(R.id.editTextMessage)
        buttonSend = findViewById(R.id.sendButton)

        // Set up the RecyclerView with the adapter
        messageAdapter = MessageAdapter(messages)
        recyclerViewMessages.apply {
            layoutManager = LinearLayoutManager(this@ChatScreenActivity)
            adapter = messageAdapter
        }

        // Handle sending a message
        buttonSend.setOnClickListener {
            sendMessage()
        }
    }

    private fun sendMessage() {
        val messageText = editTextMessage.text.toString()
        if (messageText.isNotEmpty()) {
            // Create a new message
            val newMessage = Message(messageText, "12:03 PM", true)

            // Add the message to the list and notify the adapter
            messages.add(newMessage)
            messageAdapter.notifyItemInserted(messages.size - 1)

            // Scroll to the bottom of the chat
            recyclerViewMessages.scrollToPosition(messages.size - 1)

            // Clear the input field
            editTextMessage.text.clear()
        }
    }
}

