// app/src/main/java/com/example/sos/models/SurvivalGuide.kt
package com.example.sos.models

data class SurvivalGuide(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val incidentType: String = "", // เกี่ยวข้องกับประเภทเหตุการณ์ใด (อุบัติเหตุบนถนน, จับสัตว์, ทะเลาะวิวาท, etc.)
    val imageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    // ฟังก์ชันตรวจสอบว่าคู่มือมีรูปภาพหรือไม่
    fun hasImage(): Boolean {
        return imageUrl.isNotEmpty()
    }

    // คอนสตรัคเตอร์ว่างสำหรับ Firebase
    constructor() : this("", "", "", "", "", 0, 0)
}