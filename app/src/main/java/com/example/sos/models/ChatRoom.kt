// app/src/main/java/com/example/sos/models/ChatRoom.kt
package com.example.sos.models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatRoom(
    val id: String = "", // ใช้ incidentId เป็น id ของ chat room
    val incidentId: String = "",
    val incidentType: String = "", // ประเภทเหตุการณ์
    val userId: String = "",
    val userName: String = "",
    val staffId: String = "",
    val staffName: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val active: Boolean = true // ห้องแชทยังใช้งานได้หรือไม่ (เหตุการณ์ยังไม่จบ)
) {
    // ฟังก์ชันเพื่อแสดงเวลาในรูปแบบที่อ่านง่าย
    fun getFormattedLastMessageTime(): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return dateFormat.format(Date(lastMessageTime))
    }

    // ฟังก์ชันเพื่อแสดงวันที่ในรูปแบบที่อ่านง่าย
    fun getFormattedLastMessageDate(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(Date(lastMessageTime))
    }

    // คอนสตรัคเตอร์ว่างสำหรับ Firebase
    constructor() : this("", "", "", "", "", "", "", "", 0, 0, true)
}