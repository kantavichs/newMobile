package com.example.sos.models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Incident(
    val id: String = "",
    val reporterId: String = "",
    val reporterName: String = "",
    val incidentType: String = "",
    val location: String = "",
    val relationToVictim: String = "",
    val additionalInfo: String = "",
    val status: String = "รอรับเรื่อง",
    val assignedStaffId: String = "",
    val assignedStaffName: String = "",
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