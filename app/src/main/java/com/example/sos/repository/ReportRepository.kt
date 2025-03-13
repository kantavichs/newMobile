// app/src/main/java/com/example/sos/repository/ReportRepository.kt
package com.example.sos.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sos.models.Incident
import com.example.sos.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReportRepository {
    private val TAG = "ReportRepository"
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val incidentsCollection = db.collection("incidents")
    private val usersCollection = db.collection("users")
    private val chatsCollection = db.collection("chats")

    // รายงานเหตุการณ์ใหม่
    fun reportIncident(incidentType: String, location: String, relationToVictim: String,
                       additionalInfo: String): LiveData<ReportResult> {
        val resultLiveData = MutableLiveData<ReportResult>()
        val userId = auth.currentUser?.uid

        if (userId == null) {
            resultLiveData.value = ReportResult(false, null, "ไม่พบผู้ใช้งาน กรุณาเข้าสู่ระบบใหม่")
            return resultLiveData
        }

        // ดึงข้อมูลผู้ใช้เพื่อเก็บชื่อ
        usersCollection.document(userId).get()
            .addOnSuccessListener { userDoc ->
                if (userDoc != null && userDoc.exists()) {
                    val user = userDoc.toObject(User::class.java)
                    val reporterName = user?.getFullName() ?: "ไม่ระบุชื่อ"

                    // สร้างเหตุการณ์ใหม่
                    val newIncidentRef = incidentsCollection.document()
                    val currentTime = System.currentTimeMillis()

                    val incident = Incident(
                        id = newIncidentRef.id,
                        reporterId = userId,
                        reporterName = reporterName,
                        incidentType = incidentType,
                        location = location,
                        relationToVictim = relationToVictim,
                        additionalInfo = additionalInfo,
                        status = "รอรับเรื่อง",
                        reportedAt = currentTime,
                        lastUpdatedAt = currentTime
                    )

                    newIncidentRef.set(incident)
                        .addOnSuccessListener {
                            // สร้าง chat room สำหรับเหตุการณ์นี้
                            createChatRoom(incident.id, incident.incidentType, userId, reporterName)

                            Log.d(TAG, "Incident reported successfully: ${incident.id}")
                            resultLiveData.value = ReportResult(true, incident.id, null)
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error creating incident: ${e.message}")
                            resultLiveData.value = ReportResult(false, null, e.message)
                        }
                } else {
                    Log.d(TAG, "User document not found")
                    resultLiveData.value = ReportResult(false, null, "ไม่พบข้อมูลผู้ใช้")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting user document: ${e.message}")
                resultLiveData.value = ReportResult(false, null, e.message)
            }

        return resultLiveData
    }

    // สร้างห้องแชทสำหรับเหตุการณ์
    private fun createChatRoom(incidentId: String, incidentType: String, userId: String, userName: String) {
        val chatRoom = hashMapOf(
            "id" to incidentId,
            "incidentId" to incidentId,
            "incidentType" to incidentType,
            "userId" to userId,
            "userName" to userName,
            "staffId" to "",
            "staffName" to "",
            "lastMessage" to "รอเจ้าหน้าที่รับเรื่อง",
            "lastMessageTime" to System.currentTimeMillis(),
            "unreadCount" to 0,
            "active" to true
        )

        chatsCollection.document(incidentId).set(chatRoom)
            .addOnSuccessListener {
                Log.d(TAG, "Chat room created successfully")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error creating chat room: ${e.message}")
            }
    }

    // ดึงข้อมูลเหตุการณ์ตาม ID
    fun getIncidentById(incidentId: String): LiveData<Incident> {
        val incidentLiveData = MutableLiveData<Incident>()

        incidentsCollection.document(incidentId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val incident = document.toObject(Incident::class.java)
                    incidentLiveData.value = incident
                    Log.d(TAG, "Incident data retrieved: ${incident?.id}")
                } else {
                    Log.d(TAG, "No incident document found for id: $incidentId")
                    incidentLiveData.value = Incident() // Return empty incident
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting incident document: ${e.message}")
                incidentLiveData.value = Incident() // Return empty incident
            }

        return incidentLiveData
    }

    // คลาสสำหรับผลลัพธ์การรายงานเหตุการณ์
    data class ReportResult(
        val success: Boolean,
        val incidentId: String?,
        val message: String?
    )
}