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
        Log.d(TAG, "Getting all guides from Firestore")

        guidesCollection
            .get() // เปลี่ยนจาก orderBy("title") เป็นการดึงทั้งหมดก่อน
            .addOnSuccessListener { documents ->
                val guides = ArrayList<SurvivalGuide>()
                if (documents != null && !documents.isEmpty) {
                    Log.d(TAG, "Found ${documents.size()} guide documents")

                    for (document in documents) {
                        try {
                            // แสดง raw data เพื่อตรวจสอบ
                            Log.d(TAG, "Raw document data: ${document.id} => ${document.data}")

                            // สร้างออบเจ็กต์ guide แบบ manual แทนการใช้ toObject
                            val guide = SurvivalGuide(
                                id = document.getString("id") ?: document.id,
                                title = document.getString("title") ?: "",
                                content = document.getString("content") ?: "",
                                incidentType = document.getString("incidentType") ?: "",
                                imageUrl = document.getString("imageUrl") ?: "",
                                createdAt = (document.getTimestamp("createdAt")?.seconds ?: 0) * 1000,
                                updatedAt = (document.getTimestamp("updatedAt")?.seconds ?: 0) * 1000
                            )

                            if (guide.title.isNotEmpty()) {
                                guides.add(guide)
                                Log.d(TAG, "Added guide: ${guide.id} - ${guide.title}")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing guide: ${e.message}")
                        }
                    }

                    guidesLiveData.value = guides
                    Log.d(TAG, "Total guides loaded: ${guides.size}")
                } else {
                    Log.d(TAG, "No guides found in Firestore")
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