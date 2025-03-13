// app/src/main/java/com/example/sos/repository/GuidesRepository.kt
package com.example.sos.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sos.models.SurvivalGuide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class GuidesRepository {
    private val TAG = "GuidesRepository"
    private val db = FirebaseFirestore.getInstance()
    private val guidesCollection = db.collection("survivalGuides")

    // ดึงคู่มือเอาตัวรอดทั้งหมด
    fun getAllGuides(): LiveData<List<SurvivalGuide>> {
        val guidesLiveData = MutableLiveData<List<SurvivalGuide>>()

        // เพิ่ม log เพื่อติดตามการทำงาน
        Log.d(TAG, "Fetching all guides from Firestore")

        guidesCollection
            .orderBy("title") // เรียงตามชื่อคู่มือ
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val guides = ArrayList<SurvivalGuide>()

                    // แปลงข้อมูลทีละเอกสาร เพื่อให้จับข้อผิดพลาดได้ง่าย
                    for (doc in documents) {
                        try {
                            val guide = doc.toObject(SurvivalGuide::class.java)
                            if (guide.id.isNotEmpty()) { // ตรวจสอบว่า id ไม่ว่าง
                                guides.add(guide)
                                Log.d(TAG, "Successfully parsed guide: ${guide.id} - ${guide.title}")
                            } else {
                                // กรณีที่ id ว่าง แต่ข้อมูลอื่นอ่านได้
                                val guideWithId = guide.copy(id = doc.id)
                                guides.add(guideWithId)
                                Log.d(TAG, "Adding missing ID to guide: ${doc.id} - ${guide.title}")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing guide document ${doc.id}: ${e.message}")

                            // ลองสร้างคู่มือแบบ manual จากข้อมูลดิบ
                            try {
                                val manualGuide = SurvivalGuide(
                                    id = doc.id,
                                    title = doc.getString("title") ?: "",
                                    content = doc.getString("content") ?: "",
                                    incidentType = doc.getString("incidentType") ?: "",
                                    imageUrl = doc.getString("imageUrl") ?: "",
                                    createdAt = doc.getLong("createdAt") ?: 0,
                                    updatedAt = doc.getLong("updatedAt") ?: 0
                                )
                                if (manualGuide.title.isNotEmpty()) {
                                    guides.add(manualGuide)
                                    Log.d(TAG, "Manually created guide: ${manualGuide.id} - ${manualGuide.title}")
                                }
                            } catch (e2: Exception) {
                                Log.e(TAG, "Failed to manually create guide: ${e2.message}")
                            }
                        }
                    }

                    guidesLiveData.value = guides
                    Log.d(TAG, "Total guides loaded: ${guides.size}")
                } else {
                    Log.d(TAG, "No guides found")
                    guidesLiveData.value = emptyList()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting guides: ${e.message}")
                guidesLiveData.value = emptyList()
            }

        return guidesLiveData
    }

    // ดึงคู่มือเอาตัวรอดตามประเภทเหตุการณ์
    fun getGuidesByIncidentType(incidentType: String): LiveData<List<SurvivalGuide>> {
        val guidesLiveData = MutableLiveData<List<SurvivalGuide>>()

        guidesCollection
            .whereEqualTo("incidentType", incidentType)
            .orderBy("title")
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val guides = documents.toObjects(SurvivalGuide::class.java)
                    guidesLiveData.value = guides
                    Log.d(TAG, "Guides for incident type $incidentType retrieved: ${guides.size}")
                } else {
                    Log.d(TAG, "No guides found for incident type: $incidentType")
                    guidesLiveData.value = emptyList()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting guides by incident type: ${e.message}")
                guidesLiveData.value = emptyList()
            }

        return guidesLiveData
    }

    // ดึงคู่มือเอาตัวรอดตาม ID
    fun getGuideById(guideId: String): LiveData<SurvivalGuide> {
        val guideLiveData = MutableLiveData<SurvivalGuide>()

        guidesCollection.document(guideId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val guide = document.toObject(SurvivalGuide::class.java)
                    guideLiveData.value = guide
                    Log.d(TAG, "Guide data retrieved: ${guide?.title}")
                } else {
                    Log.d(TAG, "No guide document found for id: $guideId")
                    guideLiveData.value = SurvivalGuide() // Return empty guide
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting guide document: ${e.message}")
                guideLiveData.value = SurvivalGuide() // Return empty guide
            }

        return guideLiveData
    }
}