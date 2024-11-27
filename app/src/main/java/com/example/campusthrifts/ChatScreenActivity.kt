package com.example.campusthrifts

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChatScreenActivity : AppCompatActivity() {

    private lateinit var recyclerViewMessages: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var sendButton: ImageButton
    private lateinit var emojiButton: ImageButton
    private lateinit var editTextMessage: EditText
    private lateinit var backButton: ImageView
    private var messageList = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_screen)

        // Initialize views
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages)
        sendButton = findViewById(R.id.sendButton)
        emojiButton = findViewById(R.id.emojiButton)
        editTextMessage = findViewById(R.id.editTextMessage)
        backButton = findViewById(R.id.backButton)

        // Set up RecyclerView with MessageAdapter
        messageAdapter = MessageAdapter(messageList)
        recyclerViewMessages.layoutManager = LinearLayoutManager(this)
        recyclerViewMessages.adapter = messageAdapter

        // Handle back button click
        backButton.setOnClickListener {
            finish() // Go back to the previous screen
        }

        // Handle send button click
        sendButton.setOnClickListener {
            val messageText = editTextMessage.text.toString()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                editTextMessage.text.clear() // Clear input field after sending
            }
        }

        // Handle emoji button click (optional feature)
        emojiButton.setOnClickListener {
            // Implement emoji selection logic here
        }

        // Load initial messages if any (Optional)
        loadInitialMessages()
    }

    // Function to send a message
    private fun sendMessage(messageText: String) {
        val message = Message(messageText, System.currentTimeMillis(), true) // Assuming 'true' means the message is sent by the user
        messageList.add(message)
        messageAdapter.notifyItemInserted(messageList.size - 1)
        recyclerViewMessages.scrollToPosition(messageList.size - 1) // Scroll to the latest message
    }

    // Function to load initial messages (optional)
    private fun loadInitialMessages() {
        // You can load previous chat messages from a database or API here
        // For demonstration purposes, we'll add a few sample messages
        messageList.add(Message("Hey, how are you?", System.currentTimeMillis(), false))
        messageList.add(Message("I'm good! How about you?", System.currentTimeMillis(), true))
        messageAdapter.notifyDataSetChanged()
    }
}
