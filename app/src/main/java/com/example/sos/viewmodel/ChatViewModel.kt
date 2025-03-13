package com.example.sos.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sos.models.ChatRoom
import com.example.sos.models.Message
import com.example.sos.repository.ChatRepository

class ChatViewModel : ViewModel() {
    private val chatRepository = ChatRepository()

    // ฟังก์ชันดึงข้อความทั้งหมดในห้องแชท
    fun getMessages(chatId: String): LiveData<List<Message>> {
        return chatRepository.getMessagesForChatRoom(chatId)
    }

    // ฟังก์ชันส่งข้อความใหม่
    fun sendMessage(chatId: String, messageText: String): LiveData<Boolean> {
        if (messageText.isBlank()) {
            val result = MutableLiveData<Boolean>()
            result.value = false
            return result
        }

        return chatRepository.sendMessage(chatId, messageText)
    }

    // ฟังก์ชันดึงข้อมูลห้องแชท
    fun getChatRoom(chatId: String): LiveData<ChatRoom> {
        return chatRepository.getChatRoomById(chatId)
    }

    // ฟังก์ชันตรวจสอบว่าห้องแชทยังใช้งานได้อยู่หรือไม่
    fun isChatActive(chatRoom: ChatRoom): Boolean {
        return chatRoom.active
    }

    // ฟังก์ชันจัดกลุ่มข้อความตามวันที่
    fun groupMessagesByDate(messages: List<Message>): Map<String, List<Message>> {
        return messages.groupBy { it.getFormattedDate() }
    }

    // ฟังก์ชันนับจำนวนข้อความที่ยังไม่ได้อ่าน
    fun countUnreadMessages(messages: List<Message>, currentUserId: String): Int {
        return messages.count { !it.read && !it.isCurrentUser(currentUserId) }
    }
}