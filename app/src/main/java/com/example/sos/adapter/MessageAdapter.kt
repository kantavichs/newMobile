package com.example.sos.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sos.R
import com.example.sos.models.Message
import com.google.firebase.auth.FirebaseAuth
import java.util.ArrayList

class MessageAdapter : ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    private val VIEW_TYPE_MY_MESSAGE = 1
    private val VIEW_TYPE_OTHER_MESSAGE = 2
    private val VIEW_TYPE_DATE_HEADER = 3

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val TAG = "MessageAdapter"

    // เพิ่มตัวแปรเก็บข้อมูลเฉพาะภายใน adapter
    private var messageList = ArrayList<Message>()
    private var processedList = ArrayList<Any>() // จะเก็บทั้ง Message และ DateHeader

    // คลาสสำหรับ DateHeader
    data class DateHeader(val date: String)

    override fun getItemCount(): Int = processedList.size

    override fun getItemViewType(position: Int): Int {
        val item = processedList[position]

        return when (item) {
            is DateHeader -> VIEW_TYPE_DATE_HEADER
            is Message -> if (item.senderId == currentUserId) VIEW_TYPE_MY_MESSAGE else VIEW_TYPE_OTHER_MESSAGE
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d(TAG, "Creating view holder of type: $viewType")

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
            VIEW_TYPE_DATE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_date_header, parent, false)
                DateHeaderViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        try {
            when (holder) {
                is MySentMessageViewHolder -> {
                    val message = processedList[position] as Message
                    holder.bind(message)
                }
                is ReceivedMessageViewHolder -> {
                    val message = processedList[position] as Message
                    holder.bind(message)
                }
                is DateHeaderViewHolder -> {
                    val dateHeader = processedList[position] as DateHeader
                    holder.bind(dateHeader.date)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error binding view holder: ${e.message}")
        }
    }

    // ทำ override เมธอดนี้เพื่อไม่ให้ใช้ ListAdapter ปกติ
    override fun submitList(list: List<Message>?) {
        if (list == null) return

        // บันทึกข้อมูลเดิม
        messageList = ArrayList(list)

        // แปลงข้อมูลให้มี date header
        processedList = processMessageList(messageList)

        // แจ้ง adapter ว่าข้อมูลเปลี่ยน
        notifyDataSetChanged()
    }

    // เมธอดเพื่อจัดการกับ date header
    private fun processMessageList(messages: List<Message>): ArrayList<Any> {
        val result = ArrayList<Any>()
        var currentDate = ""

        for (message in messages) {
            val messageDate = message.getFormattedDate()

            // ถ้าวันที่เปลี่ยน ให้เพิ่ม header
            if (messageDate != currentDate) {
                currentDate = messageDate
                result.add(DateHeader(messageDate))
            }

            // เพิ่มข้อความ
            result.add(message)
        }

        return result
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
}