// app/src/main/java/com/example/sos/repository/IncidentRepository.kt
package com.example.sos.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sos.models.Incident
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class IncidentRepository {
    private val TAG = "IncidentRepository"
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val incidentsCollection = db.collection("incidents")

    // ดึงเหตุการณ์ที่ยังไม่เสร็จสิ้นของผู้ใช้ปัจจุบัน
    fun getActiveIncidentsForCurrentUser(): LiveData<List<Incident>> {
        val incidentsLiveData = MutableLiveData<List<Incident>>()
        val userId = auth.currentUser?.uid

        if (userId == null) {
            incidentsLiveData.value = emptyList()
            return incidentsLiveData
        }

        incidentsCollection
            .whereEqualTo("reporterId", userId)
            .whereNotEqualTo("status", "เสร็จสิ้น")
            .orderBy("status")
            .orderBy("reportedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Listen failed: ${e.message}")
                    incidentsLiveData.value = emptyList()
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val incidents = snapshot.toObjects(Incident::class.java)
                    incidentsLiveData.value = incidents
                    Log.d(TAG, "Active incidents retrieved: ${incidents.size}")
                } else {
                    Log.d(TAG, "No active incidents found")
                    incidentsLiveData.value = emptyList()
                }
            }

        return incidentsLiveData
    }

    // ดึงเหตุการณ์ที่เสร็จสิ้นแล้วของผู้ใช้ปัจจุบัน
    fun getCompletedIncidentsForCurrentUser(): LiveData<List<Incident>> {
        val incidentsLiveData = MutableLiveData<List<Incident>>()
        val userId = auth.currentUser?.uid

        if (userId == null) {
            incidentsLiveData.value = emptyList()
            return incidentsLiveData
        }

        incidentsCollection
            .whereEqualTo("reporterId", userId)
            .whereEqualTo("status", "เสร็จสิ้น")
            .orderBy("completedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Listen failed: ${e.message}")
                    incidentsLiveData.value = emptyList()
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val incidents = snapshot.toObjects(Incident::class.java)
                    incidentsLiveData.value = incidents
                    Log.d(TAG, "Completed incidents retrieved: ${incidents.size}")
                } else {
                    Log.d(TAG, "No completed incidents found")
                    incidentsLiveData.value = emptyList()
                }
            }

        return incidentsLiveData
    }

    // ดึงเหตุการณ์ทั้งหมดของผู้ใช้ปัจจุบัน
    fun getAllIncidentsForCurrentUser(): LiveData<List<Incident>> {
        val incidentsLiveData = MutableLiveData<List<Incident>>()
        val userId = auth.currentUser?.uid

        if (userId == null) {
            incidentsLiveData.value = emptyList()
            return incidentsLiveData
        }

        incidentsCollection
            .whereEqualTo("reporterId", userId)
            .orderBy("reportedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Listen failed: ${e.message}")
                    incidentsLiveData.value = emptyList()
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val incidents = snapshot.toObjects(Incident::class.java)
                    incidentsLiveData.value = incidents
                    Log.d(TAG, "All incidents retrieved: ${incidents.size}")
                } else {
                    Log.d(TAG, "No incidents found")
                    incidentsLiveData.value = emptyList()
                }
            }

        return incidentsLiveData
    }

    // ฟังก์ชันสำหรับติดตามการเปลี่ยนแปลงของเหตุการณ์เดียว (live update)
    fun getIncidentLiveUpdates(incidentId: String): LiveData<Incident> {
        val incidentLiveData = MutableLiveData<Incident>()

        incidentsCollection.document(incidentId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Listen failed: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val incident = snapshot.toObject(Incident::class.java)
                    incidentLiveData.value = incident
                    Log.d(TAG, "Incident updated: ${incident?.id}, status: ${incident?.status}")
                } else {
                    Log.d(TAG, "Incident document does not exist")
                }
            }

        return incidentLiveData
    }

    // ดึงข้อมูลเบอร์โทรของเจ้าหน้าที่ที่รับเรื่อง
    fun getAssignedStaffPhone(incidentId: String): LiveData<String> {
        val phoneLiveData = MutableLiveData<String>()

        incidentsCollection.document(incidentId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val incident = document.toObject(Incident::class.java)
                    val staffId = incident?.assignedStaffId

                    if (staffId.isNullOrEmpty()) {
                        phoneLiveData.value = ""
                        return@addOnSuccessListener
                    }

                    // ดึงข้อมูลเจ้าหน้าที่จากคอลเล็กชัน staff
                    db.collection("staff").document(staffId).get()
                        .addOnSuccessListener { staffDoc ->
                            if (staffDoc != null && staffDoc.exists()) {
                                val phone = staffDoc.getString("phoneNumber") ?: ""
                                phoneLiveData.value = phone
                                Log.d(TAG, "Staff phone retrieved: $phone")
                            } else {
                                phoneLiveData.value = ""
                                Log.d(TAG, "Staff document does not exist")
                            }
                        }
                        .addOnFailureListener { e ->
                            phoneLiveData.value = ""
                            Log.e(TAG, "Error getting staff document: ${e.message}")
                        }
                } else {
                    phoneLiveData.value = ""
                    Log.d(TAG, "Incident document does not exist")
                }
            }
            .addOnFailureListener { e ->
                phoneLiveData.value = ""
                Log.e(TAG, "Error getting incident document: ${e.message}")
            }

        return phoneLiveData
    }
}
