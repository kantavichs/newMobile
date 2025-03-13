// app/src/main/java/com/example/sos/repository/ChatRepository.kt
package com.example.sos.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sos.models.ChatRoom
import com.example.sos.models.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatRepository {
    private val TAG = "ChatRepository"
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val chatsCollection = db.collection("chats")
    private val messagesCollection = db.collection("messages")

    // ดึงรายการห้องแชทของผู้ใช้ปัจจุบัน
    fun getChatRoomsForCurrentUser(): LiveData<List<ChatRoom>> {
        val chatRoomsLiveData = MutableLiveData<List<ChatRoom>>()
        val userId = auth.currentUser?.uid

        Log.d(TAG, "Getting chat rooms for user: $userId")

        if (userId == null) {
            Log.e(TAG, "User not logged in")
            chatRoomsLiveData.value = emptyList()
            return chatRoomsLiveData
        }

        chatsCollection
            .whereEqualTo("userId", userId)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Listen failed: ${e.message}")
                    chatRoomsLiveData.value = emptyList()
                    return@addSnapshotListener
                }

                try {
                    if (snapshot != null && !snapshot.isEmpty) {
                        val chatRooms = snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.toObject(ChatRoom::class.java)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error converting chat room document: ${e.message}")
                                null
                            }
                        }

                        // เรียงลำดับตามเวลาข้อความล่าสุด (ล่าสุดอยู่บน)
                        val sortedChatRooms = chatRooms.sortedByDescending { it.lastMessageTime }

                        Log.d(TAG, "Chat rooms retrieved: ${sortedChatRooms.size}")
                        chatRoomsLiveData.value = sortedChatRooms
                    } else {
                        Log.d(TAG, "No chat rooms found")
                        chatRoomsLiveData.value = emptyList()
                    }
                } catch (ex: Exception) {
                    Log.e(TAG, "Error processing chat rooms: ${ex.message}")
                    chatRoomsLiveData.value = emptyList()
                }
            }

        return chatRoomsLiveData
    }

    // ส่งข้อความใหม่
    fun sendMessage(chatRoomId: String, messageText: String): LiveData<Boolean> {
        val resultLiveData = MutableLiveData<Boolean>()
        val userId = auth.currentUser?.uid

        Log.d(TAG, "Attempting to send message for chatId: $chatRoomId")

        if (userId == null) {
            Log.e(TAG, "User not logged in")
            resultLiveData.value = false
            return resultLiveData
        }

        if (chatRoomId.isEmpty()) {
            Log.e(TAG, "Cannot send message: empty chatId")
            resultLiveData.value = false
            return resultLiveData
        }

        // ดึงข้อมูล chat room เพื่อตรวจสอบสถานะและดึงชื่อผู้ใช้
        chatsCollection.document(chatRoomId).get()
            .addOnSuccessListener { chatDoc ->
                if (chatDoc != null && chatDoc.exists()) {
                    val chatRoom = chatDoc.toObject(ChatRoom::class.java)

                    if (chatRoom != null && chatRoom.active) {
                        // สร้าง message ใหม่
                        val newMessageRef = messagesCollection.document()
                        val currentTime = System.currentTimeMillis()

                        // ตรวจสอบว่าชื่อผู้ส่งไม่ว่าง
                        var senderName = chatRoom.userName
                        if (senderName.isEmpty()) {
                            // ถ้าไม่มีชื่อใน chatRoom ให้ดึงจาก users collection
                            senderName = "ผู้ใช้งาน" // ค่าเริ่มต้น
                            db.collection("users").document(userId).get().addOnSuccessListener { userDoc ->
                                if (userDoc != null && userDoc.exists()) {
                                    val firstName = userDoc.getString("firstName") ?: ""
                                    val lastName = userDoc.getString("lastName") ?: ""
                                    if (firstName.isNotEmpty() || lastName.isNotEmpty()) {
                                        senderName = "$firstName $lastName".trim()
                                    }
                                }
                            }
                        }

                        val message = Message(
                            id = newMessageRef.id,
                            chatId = chatRoomId,
                            senderId = userId,
                            senderName = senderName,
                            senderType = "user",
                            message = messageText,
                            timestamp = currentTime,
                            read = false
                        )

                        newMessageRef.set(message)
                            .addOnSuccessListener {
                                // อัพเดท chat room ด้วยข้อความล่าสุด
                                updateChatRoomLastMessage(chatRoomId, messageText, currentTime)
                                Log.d(TAG, "Message sent successfully: ${message.id}")
                                resultLiveData.value = true
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error sending message: ${e.message}")
                                resultLiveData.value = false
                            }
                    } else {
                        Log.d(TAG, "Chat room is not active or null")
                        resultLiveData.value = false
                    }
                } else {
                    Log.e(TAG, "Chat room document not found")
                    resultLiveData.value = false
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting chat room document: ${e.message}")
                resultLiveData.value = false
            }

        return resultLiveData
    }

    // ดึงข้อความทั้งหมดในห้องแชท
    fun getMessagesForChatRoom(chatRoomId: String): LiveData<List<Message>> {
        val messagesLiveData = MutableLiveData<List<Message>>()

        Log.d(TAG, "Getting messages for chatId: $chatRoomId")

        if (chatRoomId.isEmpty()) {
            Log.e(TAG, "Cannot get messages: empty chatId")
            messagesLiveData.value = emptyList()
            return messagesLiveData
        }

        // ใช้ listener แบบ real-time เพื่อให้ได้ข้อมูลที่อัปเดตตลอดเวลา
        messagesCollection
            .whereEqualTo("chatId", chatRoomId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Error getting messages: ${e.message}")
                    return@addSnapshotListener
                }

                try {
                    if (snapshot != null) {
                        val messages = ArrayList<Message>()

                        for (doc in snapshot.documents) {
                            val message = doc.toObject(Message::class.java)
                            if (message != null) {
                                messages.add(message)
                            }
                        }

                        // เรียงลำดับตามเวลาอีกครั้ง เพื่อความแน่ใจ
                        messages.sortBy { it.timestamp }

                        Log.d(TAG, "Messages retrieved: ${messages.size}")
                        messagesLiveData.value = messages

                        // อัพเดทสถานะการอ่านข้อความ
                        if (messages.isNotEmpty()) {
                            updateReadStatus(chatRoomId)
                        }
                    } else {
                        Log.d(TAG, "No messages snapshot data")
                        messagesLiveData.value = emptyList()
                    }
                } catch (ex: Exception) {
                    Log.e(TAG, "Error processing messages: ${ex.message}")
                    messagesLiveData.value = emptyList()
                }
            }

        return messagesLiveData
    }



    // อัพเดทข้อความล่าสุดในห้องแชท
    private fun updateChatRoomLastMessage(chatRoomId: String, message: String, timestamp: Long) {
        chatsCollection.document(chatRoomId)
            .update(
                mapOf(
                    "lastMessage" to message,
                    "lastMessageTime" to timestamp,
                    "unreadCount" to 0 // รีเซ็ต unreadCount ฝั่งผู้ใช้ เพราะเป็นผู้ส่งล่าสุด
                )
            )
            .addOnSuccessListener {
                Log.d(TAG, "Chat room last message updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error updating chat room last message: ${e.message}")
            }
    }

    // อัพเดทสถานะการอ่านข้อความ
    private fun updateReadStatus(chatRoomId: String) {
        val userId = auth.currentUser?.uid ?: return

        messagesCollection
            .whereEqualTo("chatId", chatRoomId)
            .whereNotEqualTo("senderId", userId) // เฉพาะข้อความที่ไม่ได้ส่งโดยผู้ใช้ปัจจุบัน
            .whereEqualTo("read", false)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.update("read", true)
                }

                // รีเซ็ต unreadCount ในห้องแชท
                if (documents.size() > 0) {
                    chatsCollection.document(chatRoomId)
                        .update("unreadCount", 0)
                }

                Log.d(TAG, "Updated read status for ${documents.size()} messages")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error updating read status: ${e.message}")
            }
    }

    // ดึงข้อมูลห้องแชทตาม ID
    fun getChatRoomById(chatRoomId: String): LiveData<ChatRoom> {
        val chatRoomLiveData = MutableLiveData<ChatRoom>()

        Log.d(TAG, "Getting chat room by ID: $chatRoomId")

        if (chatRoomId.isEmpty()) {
            Log.e(TAG, "Cannot get chat room: empty chatId")
            chatRoomLiveData.value = ChatRoom() // Return empty chat room
            return chatRoomLiveData
        }

        chatsCollection.document(chatRoomId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    try {
                        val chatRoom = document.toObject(ChatRoom::class.java)
                        chatRoomLiveData.value = chatRoom
                        Log.d(TAG, "Chat room data retrieved: ${chatRoom?.id}, active: ${chatRoom?.active}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting chat room document: ${e.message}")
                        chatRoomLiveData.value = ChatRoom() // Return empty chat room
                    }
                } else {
                    Log.e(TAG, "No chat room document found for id: $chatRoomId")
                    chatRoomLiveData.value = ChatRoom() // Return empty chat room
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting chat room document: ${e.message}")
                chatRoomLiveData.value = ChatRoom() // Return empty chat room
            }

        return chatRoomLiveData
    }
}