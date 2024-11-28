package com.example.campusthrifts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(private val messages: MutableList<Any>, private val currentUserId: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_MESSAGE_SENT = 0
        const val VIEW_TYPE_MESSAGE_RECEIVED = 1
        const val VIEW_TYPE_DATE_HEADER = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (messages[position]) {
            is ChatMessage -> {
                if ((messages[position] as ChatMessage).senderId == currentUserId) {
                    VIEW_TYPE_MESSAGE_SENT
                } else {
                    VIEW_TYPE_MESSAGE_RECEIVED
                }
            }
            is String -> VIEW_TYPE_DATE_HEADER
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_MESSAGE_SENT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_sent, parent, false)
                SentMessageViewHolder(view)
            }
            VIEW_TYPE_MESSAGE_RECEIVED -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_received, parent, false)
                ReceivedMessageViewHolder(view)
            }
            VIEW_TYPE_DATE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_date_header, parent, false)
                DateHeaderViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            VIEW_TYPE_MESSAGE_SENT -> {
                val message = messages[position] as ChatMessage
                (holder as SentMessageViewHolder).bind(message)
            }
            VIEW_TYPE_MESSAGE_RECEIVED -> {
                val message = messages[position] as ChatMessage
                (holder as ReceivedMessageViewHolder).bind(message)
            }
            VIEW_TYPE_DATE_HEADER -> {
                val dateHeader = messages[position] as String
                (holder as DateHeaderViewHolder).bind(dateHeader)
            }
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    fun updateMessages(newMessages: List<ChatMessage>) {
        val formattedMessages = newMessages.map { message ->
            val date = Date(message.timestamp)
            val sdf = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
            val formattedDate = sdf.format(date)
            message.copy(formattedDate = formattedDate)
        }

        val result = mutableListOf<Any>()

        formattedMessages.forEachIndexed { index, message ->
            if (index == 0 || formattedMessages[index - 1].formattedDate != message.formattedDate) {
                result.add(message.formattedDate)
            }
            result.add(message)
        }

        messages.clear()
        messages.addAll(result)
        notifyDataSetChanged()
    }

    class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageBody: TextView = itemView.findViewById(R.id.text_message_body)
        private val messageTime: TextView = itemView.findViewById(R.id.text_message_time)

        fun bind(message: ChatMessage) {
            messageBody.text = message.text
            messageTime.text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))
        }
    }

    class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageName: TextView = itemView.findViewById(R.id.text_message_name)
        private val messageBody: TextView = itemView.findViewById(R.id.text_message_body)
        private val messageTime: TextView = itemView.findViewById(R.id.text_message_time)

        fun bind(message: ChatMessage) {
            messageName.text = message.senderName
            messageBody.text = message.text
            messageTime.text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))
        }
    }

    class DateHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateHeader: TextView = itemView.findViewById(R.id.text_date_header)

        fun bind(date: String) {
            dateHeader.text = date
        }
    }
}