package com.example.campusthrifts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserListFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private val users = mutableListOf<User>()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_list, container, false)
        recyclerView = view.findViewById(R.id.userRecyclerView)
        setupRecyclerView()
        loadUsers()
        return view
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(users) { selectedUser ->
            openChat(selectedUser)
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = userAdapter
        }
    }

    private fun loadUsers() {
        FirebaseFirestore.getInstance().collection("users")
            .whereNotEqualTo("uid", currentUserId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("UserListFragment", "Listen failed.", e)
                    return@addSnapshotListener
                }

                users.clear()
                for (doc in snapshot?.documents ?: emptyList()) {
                    val user = doc.toObject(User::class.java)
                    user?.let { users.add(it) }
                }
                userAdapter.notifyDataSetChanged()
            }
    }

    private fun openChat(user: User) {
        val chatRoomId = getChatRoomId(currentUserId!!, user.uid)
        createOrGetChatRoom(chatRoomId, user) { roomId ->
            val fragment = ChatFragment.newInstance(roomId, user.username)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun getChatRoomId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
    }

    private fun createOrGetChatRoom(chatRoomId: String, otherUser: User, onComplete: (String) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("chatRooms").document(chatRoomId).get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    val chatRoom = ChatRoom(
                        id = chatRoomId,
                        participants = listOf(currentUserId!!, otherUser.uid),
                        createdAt = System.currentTimeMillis()
                    )
                    db.collection("chatRooms").document(chatRoomId).set(chatRoom)
                }
                onComplete(chatRoomId)
            }
    }
}