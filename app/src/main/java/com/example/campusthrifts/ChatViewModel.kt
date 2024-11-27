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
        database.orderByKey().addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messagesList = mutableListOf<ChatMessage>()
                for (childSnapshot in snapshot.children) {
                    val message = childSnapshot.getValue(ChatMessage::class.java)
                    message?.let {
                        messagesList.add(it)
                    }
                }
                Log.d("ChatViewModel", "Fetched ${messagesList.size} messages")
                _messages.postValue(messagesList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatViewModel", "Failed to fetch messages: ${error.message}")
            }
        })
    }

    private fun listenForNewMessages() {
        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(ChatMessage::class.java)
                message?.let {
                    val currentList = _messages.value?.toMutableList() ?: mutableListOf()
                    currentList.add(it)
                    Log.d("ChatViewModel", "New message added: ${it.text}")
                    _messages.postValue(currentList)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
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
            database.push().setValue(message)
        }
    }
}