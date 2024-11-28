// ChatViewModel.kt
package com.example.campusthrifts

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.UUID

class ChatViewModel : ViewModel() {
    private val _messages = MutableLiveData<List<ChatMessage>>()
    val messages: LiveData<List<ChatMessage>> = _messages

    private lateinit var chatRoomId: String
    private lateinit var database: DatabaseReference

    fun initChat(chatRoomId: String, otherUserName: String) {
        this.chatRoomId = chatRoomId
        database = FirebaseDatabase.getInstance().reference.child("chatRooms").child(chatRoomId).child("messages")
        fetchMessages()
        listenForNewMessages()
    }

    private fun fetchMessages() {
        Log.d("ChatViewModel", "Fetching messages from chat room ID: $chatRoomId")
        database.orderByChild("timestamp").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messagesList = mutableListOf<ChatMessage>()
                for (childSnapshot in snapshot.children) {
                    val message = childSnapshot.getValue(ChatMessage::class.java)
                    message?.let {
                        messagesList.add(it)
                    }
                }
                Log.d("ChatViewModel", "Fetched ${messagesList.size} messages")
                messagesList.forEachIndexed { index, msg ->
                    Log.d("ChatViewModel", "Message $index: ${msg.text}")
                }
                _messages.value = messagesList
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatViewModel", "Failed to fetch messages: ${error.message}")
            }
        })
    }

    private fun listenForNewMessages() {
        Log.d("ChatViewModel", "Listening for new messages in chat room ID: $chatRoomId")
        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                // Only add new messages that come after initial fetch
                val message = snapshot.getValue(ChatMessage::class.java)
                message?.let {
                    val currentList = _messages.value?.toMutableList() ?: mutableListOf()
                    // Check if message already exists to avoid duplicates
                    if (!currentList.any { existing -> existing.id == it.id }) {
                        currentList.add(it)
                        Log.d("ChatViewModel", "New message added: ${it.text}")
                        _messages.postValue(currentList)
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("ChatViewModel", "Child changed: ${snapshot.key}")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                Log.d("ChatViewModel", "Child removed: ${snapshot.key}")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("ChatViewModel", "Child moved: ${snapshot.key}, previousChildName: $previousChildName")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatViewModel", "Failed to listen for new messages: ${error.message}")
            }
        })
    }

    fun sendMessage(messageText: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val message = ChatMessage(
                id = UUID.randomUUID().toString(),
                text = messageText,
                senderId = currentUser.uid,
                senderName = currentUser.displayName ?: "Anonymous",
                timestamp = System.currentTimeMillis()
            )
            Log.d("ChatViewModel", "Sending message: ${message.text}")
            database.push().setValue(message)
        }
    }
}