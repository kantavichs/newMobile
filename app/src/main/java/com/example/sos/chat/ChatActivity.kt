package com.example.sos.chat

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sos.adapter.MessageAdapter
import com.example.sos.databinding.ActivityChatBinding
import com.example.sos.models.ChatRoom
import com.example.sos.models.Message
import com.example.sos.viewmodel.ChatViewModel

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var adapter: MessageAdapter
    private var chatId: String = ""
    private var chatRoom: ChatRoom? = null

    // เพิ่มตัวแปรเพื่อเก็บสถานะ
    private var isFirstLoad = true
    private var messagesList = mutableListOf<Message>()

    private val TAG = "ChatActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatId = intent.getStringExtra("chatId") ?: ""

        Log.d(TAG, "Started with chatId: $chatId")

        if (chatId.isEmpty()) {
            Toast.makeText(this, "ไม่พบข้อมูลแชท", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        chatViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)

        // แสดง loading
        showLoading(true)

        // ซ่อนข้อความว่างก่อน
        binding.tvEmpty.visibility = View.GONE

        // ตั้งค่า RecyclerView
        adapter = MessageAdapter()
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true // แสดงข้อความล่าสุดด้านล่าง
        binding.rvMessages.layoutManager = linearLayoutManager
        binding.rvMessages.adapter = adapter

        // โหลดข้อมูลห้องแชท
        chatViewModel.getChatRoom(chatId).observe(this) { room ->
            Log.d(TAG, "ChatRoom loaded: ${room.id}, active: ${room.active}")
            chatRoom = room

            // ตั้งค่าหัวข้อแชท
            updateChatHeader(room)

            // ตรวจสอบว่าห้องแชทยังใช้งานได้หรือไม่
            if (!room.active) {
                binding.inputLayout.visibility = View.GONE
                binding.inactiveLayout.visibility = View.VISIBLE
            } else {
                binding.inputLayout.visibility = View.VISIBLE
                binding.inactiveLayout.visibility = View.GONE
            }
        }

        // โหลดข้อมูลแชท
        chatViewModel.getMessages(chatId).observe(this) { messages ->
            Log.d(TAG, "Messages loaded: ${messages.size}")

            // เก็บข้อมูลเดิม
            messagesList = messages.toMutableList()

            // อัปเดต UI เมื่อได้รับข้อมูลครั้งแรก
            if (isFirstLoad) {
                showLoading(false)
                isFirstLoad = false
            }

            // แสดงข้อความว่างเมื่อไม่มีข้อความ
            if (messages.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvMessages.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.rvMessages.visibility = View.VISIBLE

                // อัปเดตข้อมูลใน adapter
                adapter.submitList(messages)

                // เลื่อนไปข้อความล่าสุด
                binding.rvMessages.postDelayed({
                    if (messages.isNotEmpty()) {
                        binding.rvMessages.smoothScrollToPosition(adapter.itemCount - 1)
                    }
                }, 100)
            }
        }

        // ตั้งค่าปุ่มส่งข้อความ
        binding.btnSend.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                binding.btnSend.isEnabled = false
                Log.d(TAG, "Sending message: $message")

                chatViewModel.sendMessage(chatId, message).observe(this) { success ->
                    binding.btnSend.isEnabled = true

                    if (success) {
                        binding.etMessage.text.clear()
                        Log.d(TAG, "Message sent successfully")
                    } else {
                        Toast.makeText(this, "ไม่สามารถส่งข้อความได้", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "Failed to send message")
                    }
                }
            }
        }

        // ปุ่มย้อนกลับ
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun updateChatHeader(chatRoom: ChatRoom) {
        // ตั้งค่าหัวเรื่องและรายละเอียด
        binding.tvChatTitle.text = chatRoom.incidentType

        val subtitle = if (chatRoom.staffName.isNotEmpty()) {
            "เจ้าหน้าที่: ${chatRoom.staffName}"
        } else {
            "รอเจ้าหน้าที่รับเรื่อง"
        }

        binding.tvChatSubtitle.text = subtitle
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            binding.rvMessages.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
            binding.tvEmpty.visibility = View.GONE
        } else {
            binding.progressBar.visibility = View.GONE
            // ไม่ต้องเปลี่ยนสถานะของ RecyclerView และ tvEmpty ที่นี่
            // เพราะเราจะจัดการในส่วนของ observer แทน
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // ทำความสะอาดข้อมูล
        messagesList.clear()
    }
}