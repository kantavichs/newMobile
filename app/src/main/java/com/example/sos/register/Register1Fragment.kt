//java/com/example/sos/register/Register1Fragment.kt
package com.example.sos.register

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.sos.R

class Register1Fragment : Fragment() {

    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var roleSpinner: Spinner
    private lateinit var nextButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register1, container, false)

        firstNameEditText = view.findViewById(R.id.firstNameEditText)
        lastNameEditText = view.findViewById(R.id.lastNameEditText)
        emailEditText = view.findViewById(R.id.emailEditText)
        phoneEditText = view.findViewById(R.id.phoneEditText)
        roleSpinner = view.findViewById(R.id.roleSpinner)
        nextButton = view.findViewById(R.id.nextButton)

        firstNameEditText.inputType = (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PERSON_NAME)
        lastNameEditText.inputType = (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PERSON_NAME)

        // Set up the Spinner (Dropdown)
        val roles = arrayOf("นักศึกษา", "บุคคลธรรมดา", "บุคลากรของมหาวิทยาลัย")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, roles)
        roleSpinner.adapter = adapter

        nextButton.setOnClickListener {
            val firstName = firstNameEditText.text.toString()
            val lastName = lastNameEditText.text.toString()
            val email = emailEditText.text.toString()
            val phone = phoneEditText.text.toString()
            val role = roleSpinner.selectedItem.toString()

            if (isValidRegister1(firstName, lastName, email, phone)) {
                //Navigate to Register2Fragment and pass data
                val bundle = Bundle()
                bundle.putString("firstName", firstName)
                bundle.putString("lastName", lastName)
                bundle.putString("email", email)
                bundle.putString("phone", phone)
                bundle.putString("role", role)

                val register2Fragment = Register2Fragment()
                register2Fragment.arguments = bundle

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, register2Fragment)
                    .addToBackStack(null)
                    .commit()
            } else {
                Toast.makeText(requireContext(), "กรุณากรอกข้อมูลให้ครบถ้วนและถูกต้อง", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun isValidRegister1(firstName: String, lastName: String, email: String, phone: String): Boolean {
        val emailRegex = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$".toRegex()

        // ตรวจสอบแค่ว่ามีการป้อนข้อมูล ไม่ว่าจะเป็นภาษาใด
        return firstName.trim().isNotEmpty() &&
                lastName.trim().isNotEmpty() &&
                emailRegex.matches(email) &&
                phone.isNotEmpty()
    }
}