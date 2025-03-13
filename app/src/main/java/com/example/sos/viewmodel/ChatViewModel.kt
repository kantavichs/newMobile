package com.example.sos.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sos.models.ChatRoom
import com.example.sos.models.Message
import com.example.sos.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth

class ChatViewModel : ViewModel() {
    private val chatRepository = ChatRepository()
    private val TAG = "ChatViewModel"

    // ฟังก์ชันดึงข้อความทั้งหมดในห้องแชท
    fun getMessages(chatId: String): LiveData<List<Message>> {
        Log.d(TAG, "Getting messages for chatId: $chatId")
        return chatRepository.getMessagesForChatRoom(chatId)
    }

    // ฟังก์ชันส่งข้อความใหม่
    fun sendMessage(chatId: String, messageText: String): LiveData<Boolean> {
        if (messageText.isBlank()) {
            val result = MutableLiveData<Boolean>()
            result.value = false
            return result
        }

        Log.d(TAG, "Sending message to chatId: $chatId")
        return chatRepository.sendMessage(chatId, messageText)
    }

    // ฟังก์ชันดึงข้อมูลห้องแชท
    fun getChatRoom(chatId: String): LiveData<ChatRoom> {
        Log.d(TAG, "Getting chat room for chatId: $chatId")
        return chatRepository.getChatRoomById(chatId)
    }

    // ฟังก์ชันตรวจสอบว่าห้องแชทยังใช้งานได้อยู่หรือไม่
    fun isChatActive(chatRoom: ChatRoom): Boolean {
        return chatRoom.active
    }

    // ฟังก์ชันตรวจสอบว่าข้อความเป็นของผู้ใช้ปัจจุบันหรือไม่
    fun isCurrentUserMessage(message: Message): Boolean {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        return message.senderId == currentUserId
    }
}