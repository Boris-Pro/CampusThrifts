package com.example.campusthrifts

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class ChatFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button

    private lateinit var messageAdapter: MessageAdapter
    private val messagesList = mutableListOf<Message>()

    private val TAG = "ChatFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: Inflating layout")
        Log.d(TAG, "onCreateView started")
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        Log.d(TAG, "Layout inflated")

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("messages")

        // Initialize views
        messageRecyclerView = view.findViewById(R.id.messageRecyclerView)
        messageEditText = view.findViewById(R.id.messageEditText)
        sendButton = view.findViewById(R.id.sendButton)

        Log.d(TAG, "onCreateView: Views initialized")

        // Setup RecyclerView
        setupRecyclerView()

        // Setup message sending
        setupMessageSending()

        // Listen for messages
        listenForMessages()

        return view
    }

    private fun initializeViews(view: View) {
        try {
            messageRecyclerView = view.findViewById(R.id.messageRecyclerView)
                ?: throw NullPointerException("messageRecyclerView not found")
            messageEditText = view.findViewById(R.id.messageEditText)
                ?: throw NullPointerException("messageEditText not found")
            sendButton = view.findViewById(R.id.sendButton)
                ?: throw NullPointerException("sendButton not found")

            Log.d(TAG, "Views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views: ${e.message}")
            throw e
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: Fragment view created")
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "Setting up RecyclerView")
        messageAdapter = MessageAdapter(messagesList, auth.currentUser?.uid ?: "")
        messageRecyclerView.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true // Scrolls to bottom by default
            }
            adapter = messageAdapter
        }
        Log.d(TAG, "RecyclerView setup complete")
    }

    private fun setupMessageSending() {
        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                messageEditText.text.clear()
            }
        }
    }

    private fun sendMessage(messageText: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val message = Message(
                id = UUID.randomUUID().toString(),
                text = messageText,
                senderId = currentUser.uid,
                senderName = currentUser.displayName ?: "Anonymous",
                timestamp = System.currentTimeMillis()
            )

            database.push().setValue(message)
        }
    }

    private fun listenForMessages() {
        database.orderByChild("timestamp").addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java)
                message?.let {
                    messagesList.add(it)
                    messagesList.sortBy { it.timestamp }
                    messageAdapter.notifyDataSetChanged()
                    messageRecyclerView.scrollToPosition(messagesList.size - 1)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}