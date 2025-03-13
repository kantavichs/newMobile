package com.example.sos.adapter

import android.util.Log
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

    private val TAG = "ChatRoomAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_room, parent, false)
        return ChatRoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        val chatRoom = getItem(position)
        try {
            holder.bind(chatRoom, onItemClick)
            Log.d(TAG, "Binding chat room: ${chatRoom.id}, active: ${chatRoom.active}")
        } catch (e: Exception) {
            Log.e(TAG, "Error binding chat room: ${e.message}")
        }
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
            // ตั้งค่าข้อมูลทั่วไป
            tvIncidentType.text = chatRoom.incidentType
            tvLastMessage.text = chatRoom.lastMessage
            tvLastMessageTime.text = chatRoom.getFormattedLastMessageTime()

            // แสดงชื่อเจ้าหน้าที่ถ้ามี
            if (chatRoom.staffName.isNotEmpty()) {
                tvStaffName.text = "เจ้าหน้าที่: ${chatRoom.staffName}"
                tvStaffName.visibility = View.VISIBLE
            } else {
                tvStaffName.visibility = View.GONE
            }

            // แสดงจำนวนข้อความที่ยังไม่ได้อ่าน
            if (chatRoom.unreadCount > 0) {
                tvUnreadCount.visibility = View.VISIBLE
                tvUnreadCount.text = chatRoom.unreadCount.toString()
            } else {
                tvUnreadCount.visibility = View.GONE
            }

            // ตั้งค่าสถานะ (กำลังดำเนินการ/เสร็จสิ้น)
            if (chatRoom.active) {
                tvStatus.text = "กำลังดำเนินการ"
                tvStatus.setTextColor(itemView.context.getColor(R.color.chat_active))
                cardView.setCardBackgroundColor(itemView.context.getColor(R.color.chat_active_bg))
            } else {
                tvStatus.text = "เสร็จสิ้น"
                tvStatus.setTextColor(itemView.context.getColor(R.color.chat_inactive))
                cardView.setCardBackgroundColor(itemView.context.getColor(R.color.chat_inactive_bg))
            }

            // ตั้งค่า click listener
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
            return oldItem.lastMessageTime == newItem.lastMessageTime &&
                    oldItem.active == newItem.active &&
                    oldItem.unreadCount == newItem.unreadCount &&
                    oldItem.staffName == newItem.staffName
        }
    }
}