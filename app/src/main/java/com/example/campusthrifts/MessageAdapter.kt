// MessageAdapter.kt
package com.example.campusthrifts

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter(
    private var messages: MutableList<ChatMessage>,
    private val currentUserId: String
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    companion object {
        private const val VIEW_TYPE_MESSAGE_SENT = 1
        private const val VIEW_TYPE_MESSAGE_RECEIVED = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layout = when (viewType) {
            VIEW_TYPE_MESSAGE_SENT -> R.layout.item_message_sent
            VIEW_TYPE_MESSAGE_RECEIVED -> R.layout.item_message_received
            else -> throw IllegalArgumentException("Invalid view type")
        }

        val view = LayoutInflater.from(parent.context)
            .inflate(layout, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount() = messages.size

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (message.senderId == currentUserId) {
            VIEW_TYPE_MESSAGE_SENT
        } else {
            VIEW_TYPE_MESSAGE_RECEIVED
        }
    }

    fun updateMessages(newMessages: List<ChatMessage>) {
        messages.clear()
        messages.addAll(newMessages)
        Log.d("MessageAdapter", "Updating messages with ${newMessages.size} items")
        notifyDataSetChanged()
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.text_message_body)
        private val timeText: TextView = itemView.findViewById(R.id.text_message_time)
        private val nameText: TextView? = itemView.findViewById(R.id.text_message_name)

        fun bind(message: ChatMessage) {
            messageText.text = message.text
            timeText.text = formatTimestamp(message.timestamp)
            nameText?.text = message.senderName
        }

        private fun formatTimestamp(timestamp: Long): String {
            // Add your timestamp formatting logic here
            return android.text.format.DateFormat.format("HH:mm", timestamp).toString()
        }
    }
}