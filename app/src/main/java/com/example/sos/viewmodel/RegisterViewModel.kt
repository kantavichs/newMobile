package com.example.sos.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sos.models.User
import com.example.sos.repository.UserRepository

class RegisterViewModel : ViewModel() {
    private val userRepository = UserRepository()

    private val _registrationResult = MutableLiveData<RegistrationResult>()
    val registrationResult: LiveData<RegistrationResult> = _registrationResult

    // ฟังก์ชันลงทะเบียนผู้ใช้ใหม่
    fun registerUser(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phoneNumber: String,
        role: String
    ) {
        // ตรวจสอบข้อมูลเบื้องต้น
        if (email.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || phoneNumber.isEmpty()) {
            _registrationResult.value = RegistrationResult(false, "กรุณากรอกข้อมูลให้ครบถ้วน")
            return
        }

        // สร้างออบเจ็กต์ User
        val user = User(
            email = email,
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phoneNumber,
            role = role,
            createdAt = System.currentTimeMillis()
        )

        // ลงทะเบียนผู้ใช้
        userRepository.registerUser(email, password, user) { success, errorMessage ->
            if (success) {
                _registrationResult.value = RegistrationResult(true)
            } else {
                _registrationResult.value = RegistrationResult(false, errorMessage ?: "ลงทะเบียนไม่สำเร็จ")
            }
        }
    }

    // ฟังก์ชันตรวจสอบความถูกต้องของอีเมล
    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$".toRegex()
        return emailRegex.matches(email)
    }

    // ฟังก์ชันตรวจสอบความถูกต้องของรหัสผ่าน
    fun isValidPassword(password: String): Boolean {
        val passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}\$".toRegex()
        return passwordRegex.matches(password)
    }

    // คลาสสำหรับผลลัพธ์การลงทะเบียน
    data class RegistrationResult(
        val success: Boolean,
        val errorMessage: String? = null
    )
}