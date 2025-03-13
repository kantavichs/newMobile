package com.example.sos.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sos.R
import com.example.sos.models.Message
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter : ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    private val VIEW_TYPE_MY_MESSAGE = 1
    private val VIEW_TYPE_OTHER_MESSAGE = 2
    private val VIEW_TYPE_DATE_HEADER = 3

    private var lastDate: String = ""
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)

        // Check if this is the first message or if the date has changed from the previous message
        if (position == 0 || !getItem(position - 1).getFormattedDate().equals(message.getFormattedDate())) {
            return VIEW_TYPE_DATE_HEADER
        }

        // Determine if this is a user's message or someone else's
        return if (message.senderId == currentUserId) {
            VIEW_TYPE_MY_MESSAGE
        } else {
            VIEW_TYPE_OTHER_MESSAGE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_MY_MESSAGE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_sent, parent, false)
                MySentMessageViewHolder(view)
            }
            VIEW_TYPE_OTHER_MESSAGE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_received, parent, false)
                ReceivedMessageViewHolder(view)
            }
            else -> { // VIEW_TYPE_DATE_HEADER
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_date_header, parent, false)
                DateHeaderViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)

        when (holder) {
            is MySentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> holder.bind(message)
            is DateHeaderViewHolder -> {
                // For date headers, bind the date
                holder.bind(message.getFormattedDate())
            }
        }
    }

    // ViewHolder for messages sent by the current user
    class MySentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.textMessage)
        private val timeText: TextView = itemView.findViewById(R.id.textMessageTime)

        fun bind(message: Message) {
            messageText.text = message.message
            timeText.text = message.getFormattedTime()
        }
    }

    // ViewHolder for messages received from others
    class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.textMessage)
        private val timeText: TextView = itemView.findViewById(R.id.textMessageTime)
        private val nameText: TextView = itemView.findViewById(R.id.textSenderName)

        fun bind(message: Message) {
            messageText.text = message.message
            timeText.text = message.getFormattedTime()
            nameText.text = message.senderName
        }
    }

    // ViewHolder for date headers
    class DateHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateText: TextView = itemView.findViewById(R.id.textDate)

        fun bind(date: String) {
            dateText.text = date
        }
    }

    private class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }

    // Override getItemCount to ensure we're accounting for date headers
    override fun getItemCount(): Int {
        val originalCount = super.getItemCount()
        if (originalCount == 0) return 0

        // Count date headers
        var dateHeaderCount = 1 // Always at least one date header for the first message
        for (i in 1 until originalCount) {
            if (!getItem(i).getFormattedDate().equals(getItem(i - 1).getFormattedDate())) {
                dateHeaderCount++
            }
        }

        return originalCount // The ListAdapter will manage this for us with our getItemViewType override
    }
}