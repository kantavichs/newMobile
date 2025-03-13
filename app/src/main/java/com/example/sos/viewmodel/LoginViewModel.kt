package com.example.sos.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sos.repository.UserRepository

class LoginViewModel : ViewModel() {
    private val userRepository = UserRepository()

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    // ฟังก์ชันเข้าสู่ระบบ
    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _loginResult.value = LoginResult(false, "กรุณากรอกอีเมลและรหัสผ่าน")
            return
        }

        userRepository.loginUser(email, password) { success, errorMessage ->
            if (success) {
                _loginResult.value = LoginResult(true)
            } else {
                _loginResult.value = LoginResult(false, errorMessage ?: "เข้าสู่ระบบไม่สำเร็จ")
            }
        }
    }

    // ฟังก์ชันตรวจสอบว่ามีผู้ใช้ล็อกอินอยู่หรือไม่
    fun isUserLoggedIn(): Boolean {
        return userRepository.isUserLoggedIn()
    }

    // ฟังก์ชันรีเซ็ตรหัสผ่าน
    fun resetPassword(email: String, onComplete: (Boolean, String?) -> Unit) {
        if (email.isEmpty()) {
            onComplete(false, "กรุณากรอกอีเมล")
            return
        }

        userRepository.resetPassword(email, onComplete)
    }

    // คลาสสำหรับผลลัพธ์การล็อกอิน
    data class LoginResult(
        val success: Boolean,
        val errorMessage: String? = null
    )
}