package com.example.sos.account

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.sos.MainActivity
import com.example.sos.R
import com.example.sos.models.User
import com.example.sos.viewmodel.AccountViewModel

class AccountFragment : Fragment() {

    private lateinit var accountViewModel: AccountViewModel

    // Views
    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var roleTextView: TextView
    private lateinit var editProfileButton: Button
    private lateinit var resetPasswordButton: Button
    private lateinit var logoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accountViewModel = ViewModelProvider(this).get(AccountViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        // Initialize views
        nameTextView = view.findViewById(R.id.nameTextView)
        emailTextView = view.findViewById(R.id.emailTextView)
        phoneTextView = view.findViewById(R.id.phoneTextView)
        roleTextView = view.findViewById(R.id.roleTextView)
        editProfileButton = view.findViewById(R.id.editProfileButton)
        resetPasswordButton = view.findViewById(R.id.resetPasswordButton)
        logoutButton = view.findViewById(R.id.logoutButton)

        // Set up button listeners
        editProfileButton.setOnClickListener {
            showEditProfileDialog()
        }

        resetPasswordButton.setOnClickListener {
            showResetPasswordDialog()
        }

        logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        // Load user data
        loadUserData()

        return view
    }

    private fun loadUserData() {
        accountViewModel.getCurrentUser().observe(viewLifecycleOwner) { user ->
            if (user != null && user.id.isNotEmpty()) {
                updateUI(user)
            }
        }
    }

    private fun updateUI(user: User) {
        nameTextView.text = "${user.firstName} ${user.lastName}"
        emailTextView.text = user.email
        phoneTextView.text = user.phoneNumber
        roleTextView.text = user.role
    }

    private fun showEditProfileDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null)

        val firstNameEditText: EditText = dialogView.findViewById(R.id.firstNameEditText)
        val lastNameEditText: EditText = dialogView.findViewById(R.id.lastNameEditText)
        val phoneEditText: EditText = dialogView.findViewById(R.id.phoneEditText)
        val roleSpinner: Spinner = dialogView.findViewById(R.id.roleSpinner)

        // Set up the Spinner (Dropdown)
        val roles = arrayOf("นักศึกษา", "บุคคลธรรมดา", "บุคลากรของมหาวิทยาลัย")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, roles)
        roleSpinner.adapter = adapter

        // Pre-fill with current user data
        accountViewModel.getCurrentUser().observe(viewLifecycleOwner) { user ->
            if (user != null && user.id.isNotEmpty()) {
                firstNameEditText.setText(user.firstName)
                lastNameEditText.setText(user.lastName)
                phoneEditText.setText(user.phoneNumber)

                // Select current role in spinner
                val roleIndex = roles.indexOf(user.role)
                if (roleIndex >= 0) {
                    roleSpinner.setSelection(roleIndex)
                }

                // Remove observer after getting data
                accountViewModel.getCurrentUser().removeObservers(viewLifecycleOwner)
            }
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("แก้ไขข้อมูลส่วนตัว")
        builder.setView(dialogView)
        builder.setPositiveButton("บันทึก") { _, _ ->
            val firstName = firstNameEditText.text.toString()
            val lastName = lastNameEditText.text.toString()
            val phone = phoneEditText.text.toString()
            val role = roleSpinner.selectedItem.toString()

            if (firstName.isNotEmpty() && lastName.isNotEmpty() && phone.isNotEmpty()) {
                updateUserProfile(firstName, lastName, phone, role)
            } else {
                Toast.makeText(requireContext(), "กรุณากรอกข้อมูลให้ครบถ้วน", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("ยกเลิก", null)
        builder.show()
    }

    private fun updateUserProfile(firstName: String, lastName: String, phone: String, role: String) {
        accountViewModel.updateUserProfile(firstName, lastName, phone, role)

        accountViewModel.updateResult.observe(viewLifecycleOwner) { result ->
            if (result.success) {
                Toast.makeText(requireContext(), "อัพเดทข้อมูลสำเร็จ", Toast.LENGTH_SHORT).show()

                // Refresh user data
                loadUserData()
            } else {
                Toast.makeText(requireContext(), result.errorMessage ?: "อัพเดทข้อมูลไม่สำเร็จ", Toast.LENGTH_SHORT).show()
            }

            // Remove observer after handling result
            accountViewModel.updateResult.removeObservers(viewLifecycleOwner)
        }
    }

    private fun showResetPasswordDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_reset_password, null)
        val emailEditText: EditText = dialogView.findViewById(R.id.emailEditText)

        // Pre-fill with current user email
        accountViewModel.getCurrentUser().observe(viewLifecycleOwner) { user ->
            if (user != null && user.email.isNotEmpty()) {
                emailEditText.setText(user.email)
                // Remove observer after getting data
                accountViewModel.getCurrentUser().removeObservers(viewLifecycleOwner)
            }
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("รีเซ็ตรหัสผ่าน")
        builder.setMessage("ระบบจะส่งลิงก์สำหรับรีเซ็ตรหัสผ่านไปยังอีเมลของคุณ")
        builder.setView(dialogView)
        builder.setPositiveButton("ส่งลิงก์รีเซ็ต") { _, _ ->
            val email = emailEditText.text.toString()
            if (email.isNotEmpty()) {
                resetPassword(email)
            } else {
                Toast.makeText(requireContext(), "กรุณากรอกอีเมล", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("ยกเลิก", null)
        builder.show()
    }

    private fun resetPassword(email: String) {
        accountViewModel.resetPassword(email) { success, message ->
            if (success) {
                Toast.makeText(requireContext(), "ส่งลิงก์รีเซ็ตรหัสผ่านไปยังอีเมลของคุณแล้ว", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), message ?: "ไม่สามารถรีเซ็ตรหัสผ่านได้", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("ออกจากระบบ")
        builder.setMessage("คุณต้องการออกจากระบบหรือไม่?")
        builder.setPositiveButton("ออกจากระบบ") { _, _ ->
            logout()
        }
        builder.setNegativeButton("ยกเลิก", null)
        builder.show()
    }

    private fun logout() {
        accountViewModel.logout()

        // Navigate to MainActivity
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}