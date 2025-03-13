// app/src/main/java/com/example/sos/models/Message.kt
package com.example.sos.models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Message(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "", // เก็บชื่อผู้ส่งไว้เพื่อลดการเรียก query
    val senderType: String = "", // "user" หรือ "staff"
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val read: Boolean = false
) {
    // ฟังก์ชันเพื่อแสดงเวลาในรูปแบบที่อ่านง่าย
    fun getFormattedTime(): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }

    fun getFormattedDate(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }

    // ฟังก์ชันตรวจสอบว่าเป็นข้อความของผู้ใช้คนปัจจุบัน
    fun isCurrentUser(currentUserId: String): Boolean {
        return senderId == currentUserId
    }

    // คอนสตรัคเตอร์ว่างสำหรับ Firebase
    constructor() : this("", "", "", "", "", "", 0, false)
}