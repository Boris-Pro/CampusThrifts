package com.example.campusthrifts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(
    private val messages: List<Message>,
    private val currentUserId: String
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2

        // Moved the formatTime function here as a single declaration
        fun formatTime(timestamp: Long): String {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (message.senderId == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    sealed class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        class SentMessageViewHolder(view: View) : MessageViewHolder(view) {
            private val messageText: TextView = view.findViewById(R.id.messageText)
            private val timeText: TextView = view.findViewById(R.id.timeText)

            fun bind(message: Message) {
                messageText.text = message.text
                timeText.text = formatTime(message.timestamp)
            }
        }

        class ReceivedMessageViewHolder(view: View) : MessageViewHolder(view) {
            private val messageText: TextView = view.findViewById(R.id.messageText)
            private val timeText: TextView = view.findViewById(R.id.timeText)
            private val nameText: TextView = view.findViewById(R.id.nameText)

            fun bind(message: Message) {
                messageText.text = message.text
                timeText.text = formatTime(message.timestamp)
                nameText.text = message.senderName
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_sent, parent, false)
                MessageViewHolder.SentMessageViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_received, parent, false)
                MessageViewHolder.ReceivedMessageViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is MessageViewHolder.SentMessageViewHolder -> holder.bind(message)
            is MessageViewHolder.ReceivedMessageViewHolder -> holder.bind(message)
        }
    }

    override fun getItemCount() = messages.size
}