package com.example.sos.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sos.R
import com.example.sos.models.ChatRoom

class ChatRoomAdapter(
    private val onItemClick: (String, Boolean) -> Unit
) : ListAdapter<ChatRoom, ChatRoomAdapter.ChatRoomViewHolder>(ChatRoomDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_room, parent, false)
        return ChatRoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        val chatRoom = getItem(position)
        holder.bind(chatRoom, onItemClick)
    }

    class ChatRoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardView)
        private val tvIncidentType: TextView = itemView.findViewById(R.id.tvIncidentType)
        private val tvStaffName: TextView = itemView.findViewById(R.id.tvStaffName)
        private val tvLastMessage: TextView = itemView.findViewById(R.id.tvLastMessage)
        private val tvLastMessageTime: TextView = itemView.findViewById(R.id.tvLastMessageTime)
        private val tvUnreadCount: TextView = itemView.findViewById(R.id.tvUnreadCount)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

        fun bind(chatRoom: ChatRoom, onItemClick: (String, Boolean) -> Unit) {
            tvIncidentType.text = chatRoom.incidentType

            // Show staff name if assigned, otherwise show status
            if (chatRoom.staffName.isNotEmpty()) {
                tvStaffName.text = "เจ้าหน้าที่: ${chatRoom.staffName}"
                tvStaffName.visibility = View.VISIBLE
            } else {
                tvStaffName.visibility = View.GONE
            }

            tvLastMessage.text = chatRoom.lastMessage
            tvLastMessageTime.text = chatRoom.getFormattedLastMessageTime()

            // Show unread count if there are unread messages
            if (chatRoom.unreadCount > 0) {
                tvUnreadCount.visibility = View.VISIBLE
                tvUnreadCount.text = chatRoom.unreadCount.toString()
            } else {
                tvUnreadCount.visibility = View.GONE
            }

            // Show active/inactive status
            if (chatRoom.active) {
                tvStatus.text = "กำลังดำเนินการ"
                tvStatus.setTextColor(itemView.context.getColor(R.color.chat_active))
                cardView.setCardBackgroundColor(itemView.context.getColor(R.color.chat_active_bg))
            } else {
                tvStatus.text = "เสร็จสิ้น"
                tvStatus.setTextColor(itemView.context.getColor(R.color.chat_inactive))
                cardView.setCardBackgroundColor(itemView.context.getColor(R.color.chat_inactive_bg))
            }

            // Set click listener
            itemView.setOnClickListener {
                onItemClick(chatRoom.id, chatRoom.active)
            }
        }
    }

    private class ChatRoomDiffCallback : DiffUtil.ItemCallback<ChatRoom>() {
        override fun areItemsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean {
            return oldItem == newItem
        }
    }
}