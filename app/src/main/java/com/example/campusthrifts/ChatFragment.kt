package com.example.campusthrifts

import ChatViewModel
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class ChatFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private var chatRoomId: String? = null
    private var otherUserName: String? = null

    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button

    private lateinit var viewModel: ChatViewModel

    private val TAG = "ChatFragment"

    companion object {
        fun newInstance(chatRoomId: String, otherUserName: String): ChatFragment {
            return ChatFragment().apply {
                arguments = Bundle().apply {
                    putString("chatRoomId", chatRoomId)
                    putString("otherUserName", otherUserName)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatRoomId = arguments?.getString("chatRoomId")
        otherUserName = arguments?.getString("otherUserName")
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        initializeViews(view)
        return view
    }

    private fun initializeViews(view: View) {
        try {
            messageRecyclerView = view.findViewById(R.id.messageRecyclerView)
            messageEditText = view.findViewById(R.id.messageEditText)
            sendButton = view.findViewById(R.id.sendButton)
            Log.d(TAG, "Views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views: ${e.message}")
            throw e
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(ChatViewModel::class.java)

        setupRecyclerView()
        setupMessageSending()
        observeMessages()

        chatRoomId?.let { roomId ->
            otherUserName?.let { userName ->
                viewModel.initChat(roomId, userName)
                updateToolbar()
            }
        }
    }

    private fun updateToolbar() {
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = otherUserName
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "Setting up RecyclerView")
        messageAdapter = MessageAdapter(mutableListOf(), auth.currentUser?.uid ?: "")
        messageRecyclerView.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }
        Log.d(TAG, "RecyclerView setup complete")
    }

    private fun setupMessageSending() {
        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                viewModel.sendMessage(messageText)
                messageEditText.text.clear()
            }
        }
    }

    private fun observeMessages() {
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            messageAdapter.updateMessages(messages as List<Message>)
            messageRecyclerView.scrollToPosition(messages.size - 1)
        }
    }
}