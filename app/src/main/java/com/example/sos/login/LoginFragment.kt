package com.example.sos.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.sos.R
import com.example.sos.home.HomeActivity
import com.example.sos.repository.UserRepository

class LoginFragment : Fragment() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var forgotPasswordButton: Button
    private lateinit var userRepository: UserRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        // สร้าง UserRepository
        userRepository = UserRepository()

        emailEditText = view.findViewById(R.id.emailEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)
        loginButton = view.findViewById(R.id.loginButtonSubmit)
        forgotPasswordButton = view.findViewById(R.id.forgotPasswordButton)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // ตรวจสอบรูปแบบข้อมูลก่อน
            if (isValidFormat(email, password)) {
                // แสดง loading หรือปิดปุ่มระหว่างรอการตรวจสอบ
                loginButton.isEnabled = false

                // ตรวจสอบการเข้าสู่ระบบกับ Firebase
                userRepository.loginUser(email, password) { success, message ->
                    // เปิดปุ่มเมื่อได้ผลลัพธ์แล้ว
                    loginButton.isEnabled = true

                    if (success) {
                        Toast.makeText(requireContext(), "เข้าสู่ระบบสำเร็จ", Toast.LENGTH_SHORT).show()
                        val intent = Intent(requireContext(), HomeActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish() // ปิด MainActivity
                    } else {
                        Toast.makeText(requireContext(), message ?: "อีเมลหรือรหัสผ่านไม่ถูกต้อง", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "รูปแบบอีเมลหรือรหัสผ่านไม่ถูกต้อง", Toast.LENGTH_SHORT).show()
            }
        }

        // เพิ่ม Event Handler สำหรับปุ่มลืมรหัสผ่าน
        forgotPasswordButton.setOnClickListener {
            val email = emailEditText.text.toString()
            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "กรุณากรอกอีเมลที่ต้องการรีเซ็ตรหัสผ่าน", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            userRepository.resetPassword(email) { success, message ->
                if (success) {
                    Toast.makeText(requireContext(), "ส่งอีเมลรีเซ็ตรหัสผ่านแล้ว กรุณาตรวจสอบกล่องข้อความของคุณ", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(requireContext(), message ?: "ไม่สามารถส่งอีเมลรีเซ็ตรหัสผ่านได้", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return view
    }

    // ตรวจสอบเฉพาะรูปแบบของข้อมูล
    private fun isValidFormat(email: String, password: String): Boolean {
        val emailRegex = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$".toRegex()
        val passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}\$".toRegex()

        return emailRegex.matches(email) && passwordRegex.matches(password)
    }
}