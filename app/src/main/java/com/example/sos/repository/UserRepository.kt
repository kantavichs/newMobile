// app/src/main/java/com/example/sos/repository/UserRepository.kt
package com.example.sos.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sos.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {
    private val TAG = "UserRepository"
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    // เช็คว่ามีผู้ใช้ล็อกอินอยู่หรือไม่
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    // ลงทะเบียนผู้ใช้ใหม่
    fun registerUser(email: String, password: String, user: User, onComplete: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid ?: return@addOnSuccessListener
                val newUser = user.copy(id = userId)

                usersCollection.document(userId).set(newUser)
                    .addOnSuccessListener {
                        Log.d(TAG, "User registered successfully: $userId")
                        onComplete(true, null)
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error saving user data: ${e.message}")
                        onComplete(false, e.message)
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Registration failed: ${e.message}")
                onComplete(false, e.message)
            }
    }

    // เข้าสู่ระบบ
    fun loginUser(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Log.d(TAG, "Login successful for: $email")
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Login failed: ${e.message}")
                onComplete(false, e.message)
            }
    }

    // ดึงข้อมูลผู้ใช้ปัจจุบัน
    fun getCurrentUser(): LiveData<User> {
        val userLiveData = MutableLiveData<User>()
        val userId = auth.currentUser?.uid

        if (userId == null) {
            userLiveData.value = User() // Return empty user
            return userLiveData
        }

        usersCollection.document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = document.toObject(User::class.java)
                    userLiveData.value = user
                    Log.d(TAG, "Current user data retrieved: ${user?.getFullName()}")
                } else {
                    Log.d(TAG, "No user document found")
                    userLiveData.value = User() // Return empty user
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting user document: ${e.message}")
                userLiveData.value = User() // Return empty user
            }

        return userLiveData
    }

    // ออกจากระบบ
    fun logout() {
        auth.signOut()
        Log.d(TAG, "User logged out")
    }

    // อัพเดทข้อมูลผู้ใช้
    fun updateUserProfile(user: User, onComplete: (Boolean, String?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onComplete(false, "User not logged in")
            return
        }

        val updatedUser = user.copy(id = userId)

        usersCollection.document(userId).set(updatedUser)
            .addOnSuccessListener {
                Log.d(TAG, "User profile updated successfully")
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error updating user profile: ${e.message}")
                onComplete(false, e.message)
            }
    }

    // รีเซ็ตรหัสผ่าน
    fun resetPassword(email: String, onComplete: (Boolean, String?) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                Log.d(TAG, "Password reset email sent to: $email")
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to send password reset email: ${e.message}")
                onComplete(false, e.message)
            }
    }

    // ดึงข้อมูลผู้ใช้ตาม ID
    fun getUserById(userId: String): LiveData<User> {
        val userLiveData = MutableLiveData<User>()

        usersCollection.document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = document.toObject(User::class.java)
                    userLiveData.value = user
                    Log.d(TAG, "User data retrieved: ${user?.getFullName()}")
                } else {
                    Log.d(TAG, "No user document found for id: $userId")
                    userLiveData.value = User() // Return empty user
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting user document: ${e.message}")
                userLiveData.value = User() // Return empty user
            }

        return userLiveData
    }
}