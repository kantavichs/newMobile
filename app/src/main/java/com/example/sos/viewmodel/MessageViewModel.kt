package com.example.sos.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.sos.models.ChatRoom
import com.example.sos.repository.ChatRepository

class MessageViewModel : ViewModel() {
    private val chatRepository = ChatRepository()

    // ฟังก์ชันดึงรายการห้องแชทของผู้ใช้ปัจจุบัน
    fun getChatRooms(): LiveData<List<ChatRoom>> {
        return chatRepository.getChatRoomsForCurrentUser()
    }

    // ฟังก์ชันกรองห้องแชทที่ยังใช้งานได้
    fun getActiveChatRooms(chatRooms: List<ChatRoom>): List<ChatRoom> {
        return chatRooms.filter { it.active }
    }

    // ฟังก์ชันกรองห้องแชทที่ไม่ใช้งานแล้ว
    fun getInactiveChatRooms(chatRooms: List<ChatRoom>): List<ChatRoom> {
        return chatRooms.filter { !it.active }
    }

    // ฟังก์ชันเรียงลำดับห้องแชทตามเวลาข้อความล่าสุด
    fun sortChatRoomsByLastMessageTime(chatRooms: List<ChatRoom>): List<ChatRoom> {
        return chatRooms.sortedByDescending { it.lastMessageTime }
    }

    // ฟังก์ชันนับจำนวนห้องแชทที่มีข้อความที่ยังไม่ได้อ่าน
    fun countChatRoomsWithUnreadMessages(chatRooms: List<ChatRoom>): Int {
        return chatRooms.count { it.unreadCount > 0 }
    }

    // ฟังก์ชันกรองห้องแชทตามประเภทเหตุการณ์
    fun filterChatRoomsByIncidentType(chatRooms: List<ChatRoom>, incidentType: String): List<ChatRoom> {
        if (incidentType.isEmpty()) {
            return chatRooms
        }
        return chatRooms.filter { it.incidentType == incidentType }
    }

    // ฟังก์ชันค้นหาห้องแชท
    fun searchChatRooms(chatRooms: List<ChatRoom>, query: String): List<ChatRoom> {
        if (query.isEmpty()) {
            return chatRooms
        }

        val lowerCaseQuery = query.lowercase()
        return chatRooms.filter {
            it.incidentType.lowercase().contains(lowerCaseQuery) ||
                    it.staffName.lowercase().contains(lowerCaseQuery) ||
                    it.lastMessage.lowercase().contains(lowerCaseQuery)
        }
    }
}