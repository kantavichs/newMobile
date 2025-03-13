// app/src/main/java/com/example/sos/models/User.kt
package com.example.sos.models

data class User(
    val id: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val role: String = "", // นักศึกษา, บุคลากร, บุคคลทั่วไป
    val createdAt: Long = System.currentTimeMillis()
) {
    // ฟังก์ชันสำหรับแสดงชื่อเต็ม
    fun getFullName(): String {
        return "$firstName $lastName"
    }

    // คอนสตรัคเตอร์ว่างสำหรับ Firebase
    constructor() : this("", "", "", "", "", "", 0)
}
