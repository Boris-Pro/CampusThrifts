import android.os.Message
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

class ChatViewModel : ViewModel() {
    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private lateinit var chatRoomId: String
    private lateinit var database: DatabaseReference

    fun initChat(chatRoomId: String, otherUserName: String) {
        this.chatRoomId = chatRoomId
        database = FirebaseDatabase.getInstance().reference.child("chatRooms").child(chatRoomId).child("messages")
        listenForMessages()
    }

    private fun listenForMessages() {
        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java)
                message?.let {
                    val currentList = _messages.value?.toMutableList() ?: mutableListOf()
                    currentList.add(it)
                    _messages.postValue(currentList)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun sendMessage(messageText: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val message = com.example.campusthrifts.Message(
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