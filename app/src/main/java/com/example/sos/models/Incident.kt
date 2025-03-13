// app/src/main/java/com/example/sos/models/Incident.kt
package com.example.sos.models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Incident(
    val id: String = "",
    val reporterId: String = "",
    val reporterName: String = "", // เก็บชื่อผู้แจ้งเหตุไว้เพื่อลดการเรียก query
    val incidentType: String = "", // อุบัติเหตุบนถนน, จับสัตว์, ทะเลาะวิวาท, etc.
    val location: String = "",
    val relationToVictim: String = "", // ผู้ประสบเหตุ, ผู้เห็นเหตุการณ์, เพื่อนผู้ประสบเหตุ
    val additionalInfo: String = "",
    val status: String = "รอรับเรื่อง", // รอรับเรื่อง, เจ้าหน้าที่รับเรื่องแล้ว, กำลังดำเนินการ, เสร็จสิ้น
    val assignedStaffId: String = "",
    val assignedStaffName: String = "", // เก็บชื่อเจ้าหน้าที่ไว้เพื่อลดการเรียก query
    val reportedAt: Long = System.currentTimeMillis(),
    val lastUpdatedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
) {
    // ฟังก์ชันตรวจสอบว่าเหตุการณ์กำลังดำเนินการอยู่หรือไม่
    fun isActive(): Boolean {
        return status != "เสร็จสิ้น"
    }

    // ฟังก์ชันเพื่อแสดงเวลาในรูปแบบที่อ่านง่าย
    fun getFormattedReportTime(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return dateFormat.format(Date(reportedAt))
    }

    fun getFormattedLastUpdateTime(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return dateFormat.format(Date(lastUpdatedAt))
    }

    // คอนสตรัคเตอร์ว่างสำหรับ Firebase
    constructor() : this("", "", "", "", "", "", "", "", "", "", 0, 0, null)
}