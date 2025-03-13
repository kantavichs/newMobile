package com.example.sos.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sos.models.User
import com.example.sos.repository.UserRepository

class AccountViewModel : ViewModel() {
    private val userRepository = UserRepository()

    private val _updateResult = MutableLiveData<UpdateResult>()
    val updateResult: LiveData<UpdateResult> = _updateResult

    // ฟังก์ชันดึงข้อมูลผู้ใช้ปัจจุบัน
    fun getCurrentUser(): LiveData<User> {
        return userRepository.getCurrentUser()
    }

    // ฟังก์ชันอัพเดทข้อมูลผู้ใช้
    fun updateUserProfile(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        role: String
    ) {
        // ตรวจสอบข้อมูลเบื้องต้น
        if (firstName.isEmpty() || lastName.isEmpty() || phoneNumber.isEmpty()) {
            _updateResult.value = UpdateResult(false, "กรุณากรอกข้อมูลให้ครบถ้วน")
            return
        }

        // ดึงข้อมูลผู้ใช้ปัจจุบันก่อน
        val userLiveData = userRepository.getCurrentUser()
        userLiveData.observeForever { currentUser ->
            // ใช้ข้อมูลเดิมสำหรับฟิลด์ที่ไม่ได้อัพเดท
            val updatedUser = currentUser.copy(
                firstName = firstName,
                lastName = lastName,
                phoneNumber = phoneNumber,
                role = role
            )

            // อัพเดทข้อมูลผู้ใช้
            userRepository.updateUserProfile(updatedUser) { success, errorMessage ->
                if (success) {
                    _updateResult.value = UpdateResult(true)
                } else {
                    _updateResult.value = UpdateResult(false, errorMessage ?: "อัพเดทข้อมูลไม่สำเร็จ")
                }
            }

            // ลบ observer เมื่อเสร็จสิ้น
            userLiveData.removeObserver {}
        }
    }

    // ฟังก์ชันออกจากระบบ
    fun logout() {
        userRepository.logout()
    }

    // ฟังก์ชันรีเซ็ตรหัสผ่าน
    fun resetPassword(email: String, onComplete: (Boolean, String?) -> Unit) {
        userRepository.resetPassword(email, onComplete)
    }

    // ฟังก์ชันตรวจสอบความถูกต้องของเบอร์โทรศัพท์
    fun isValidPhoneNumber(phoneNumber: String): Boolean {
        // เบอร์โทรศัพท์ควรเป็นตัวเลข 9-10 หลัก
        val phoneRegex = "^[0-9]{9,10}$".toRegex()
        return phoneRegex.matches(phoneNumber)
    }

    // คลาสสำหรับผลลัพธ์การอัพเดทข้อมูล
    data class UpdateResult(
        val success: Boolean,
        val errorMessage: String? = null
    )
}