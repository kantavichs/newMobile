package com.example.sos.register

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.sos.MainActivity
import com.example.sos.R
import com.example.sos.viewmodel.RegisterViewModel
import com.example.sos.login.LoginFragment
import androidx.core.content.ContextCompat

class Register2Fragment : Fragment() {

    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var backButton: Button
    private lateinit var confirmButton: Button
    private lateinit var registerViewModel: RegisterViewModel

    private var email: String = ""
    private var firstName: String = ""
    private var lastName: String = ""
    private var phoneNumber: String = ""
    private var role: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerViewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register2, container, false)

        passwordEditText = view.findViewById(R.id.passwordEditText)
        confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText)
        backButton = view.findViewById(R.id.backButton)
        confirmButton = view.findViewById(R.id.confirmButton)

        // รับข้อมูลจาก arguments
        arguments?.let {
            email = it.getString("email", "")
            firstName = it.getString("firstName", "")
            lastName = it.getString("lastName", "")
            phoneNumber = it.getString("phone", "")
            role = it.getString("role", "")

            Log.d("Register2", "Received data: $email, $firstName, $lastName, $phoneNumber, $role")
        }

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack() // ย้อนกลับไป Register1Fragment
        }

        // ตรวจสอบผลลัพธ์การลงทะเบียน
        registerViewModel.registrationResult.observe(viewLifecycleOwner) { result ->
            if (result.success) {
                Toast.makeText(requireContext(), "ลงทะเบียนสำเร็จ", Toast.LENGTH_SHORT).show()
                navigateToLogin()
            } else {
                Toast.makeText(requireContext(), result.errorMessage ?: "การลงทะเบียนล้มเหลว", Toast.LENGTH_SHORT).show()
            }
        }

        confirmButton.setOnClickListener {
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (isValidRegister2(password, confirmPassword)) {
                // ลงทะเบียนผู้ใช้ผ่าน ViewModel
                registerViewModel.registerUser(email, password, firstName, lastName, phoneNumber, role)
            } else {
                Toast.makeText(requireContext(), "รหัสผ่านไม่ตรงกัน หรือไม่ถูกต้อง", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun navigateToLogin() {
        try {
            // Get activity and make sure it's MainActivity
            val mainActivity = requireActivity() as? MainActivity
            if (mainActivity == null) {
                Log.e("Register2", "Activity is not MainActivity")
                restartApp()
                return
            }

            // Clear the fragment back stack
            mainActivity.supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)

            // Set button state BEFORE loading the fragment
            val loginButton = mainActivity.findViewById<Button>(R.id.loginButton)
            val registerButton = mainActivity.findViewById<Button>(R.id.registerButton)

            if (loginButton != null && registerButton != null) {
                // Login button should be ENABLED for login screen
                loginButton.isEnabled = true
                loginButton.backgroundTintList = ContextCompat.getColorStateList(mainActivity, R.color.myred)

                // Register button should be disabled
                registerButton.isEnabled = false
                registerButton.backgroundTintList = ContextCompat.getColorStateList(mainActivity, R.color.mylightred)
            } else {
                Log.e("Register2", "Buttons not found")
            }

            // Create and show the login fragment
            val loginFragment = LoginFragment()
            mainActivity.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, loginFragment)
                .commit()

        } catch (e: Exception) {
            Log.e("Register2", "Error navigating to login: ${e.message}")
            restartApp()
        }
    }

    private fun restartApp() {
        // Fallback method - restart the app completely
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun isValidRegister2(password: String, confirmPassword: String): Boolean {
        val passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}\$".toRegex()
        return password == confirmPassword && passwordRegex.matches(password)
    }
}