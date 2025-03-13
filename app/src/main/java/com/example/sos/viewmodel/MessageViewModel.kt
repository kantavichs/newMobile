package com.example.sos.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sos.models.ChatRoom
import com.example.sos.repository.ChatRepository

class MessageViewModel : ViewModel() {
    private val chatRepository = ChatRepository()
    private val TAG = "MessageViewModel"

    // เก็บข้อมูลห้องแชททั้งหมด
    private var allChatRooms: List<ChatRoom> = emptyList()

    // ฟังก์ชันดึงรายการห้องแชทของผู้ใช้ปัจจุบัน
    fun getChatRooms(): LiveData<List<ChatRoom>> {
        Log.d(TAG, "Getting chat rooms for current user")
        return chatRepository.getChatRoomsForCurrentUser()
    }

    // เก็บข้อมูลห้องแชท
    fun setChatRoomsList(chatRooms: List<ChatRoom>) {
        this.allChatRooms = chatRooms
        Log.d(TAG, "Chat room list set with ${chatRooms.size} rooms")
    }

    // ดึงข้อมูลห้องแชททั้งหมด
    fun getAllChatRooms(): List<ChatRoom> {
        return allChatRooms
    }

    // ฟังก์ชันกรองห้องแชทที่ยังใช้งานได้
    fun getActiveChatRooms(): List<ChatRoom> {
        val active = allChatRooms.filter { it.active }
        Log.d(TAG, "Filtered ${active.size} active chat rooms")
        return active
    }

    // ฟังก์ชันกรองห้องแชทที่ไม่ใช้งานแล้ว
    fun getInactiveChatRooms(): List<ChatRoom> {
        val inactive = allChatRooms.filter { !it.active }
        Log.d(TAG, "Filtered ${inactive.size} inactive chat rooms")
        return inactive
    }

    // ฟังก์ชันค้นหาห้องแชท
    fun searchChatRooms(query: String): List<ChatRoom> {
        if (query.isEmpty()) {
            return allChatRooms
        }

        val lowerCaseQuery = query.lowercase()
        val result = allChatRooms.filter {
            it.incidentType.lowercase().contains(lowerCaseQuery) ||
                    it.staffName.lowercase().contains(lowerCaseQuery) ||
                    it.lastMessage.lowercase().contains(lowerCaseQuery)
        }

        Log.d(TAG, "Search for '$query' found ${result.size} results")
        return result
    }

    // ฟังก์ชันเรียงลำดับห้องแชทตามเวลาข้อความล่าสุด
    fun sortChatRoomsByLastMessageTime(chatRooms: List<ChatRoom>): List<ChatRoom> {
        return chatRooms.sortedByDescending { it.lastMessageTime }
    }

    // ฟังก์ชันนับจำนวนห้องแชทที่มีข้อความที่ยังไม่ได้อ่าน
    fun countChatRoomsWithUnreadMessages(): Int {
        return allChatRooms.count { it.unreadCount > 0 }
    }

    // ฟังก์ชันกรองห้องแชทตามประเภทเหตุการณ์
    fun filterChatRoomsByIncidentType(incidentType: String): List<ChatRoom> {
        if (incidentType.isEmpty()) {
            return allChatRooms
        }
        return allChatRooms.filter { it.incidentType == incidentType }
    }
}