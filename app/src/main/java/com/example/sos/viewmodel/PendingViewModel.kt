package com.example.sos.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.sos.models.Incident
import com.example.sos.repository.IncidentRepository

class PendingViewModel : ViewModel() {
    private val incidentRepository = IncidentRepository()

    // ฟังก์ชันดึงเหตุการณ์ที่ยังไม่เสร็จสิ้น (กำลังดำเนินการ)
    fun getActiveIncidents(): LiveData<List<Incident>> {
        return incidentRepository.getActiveIncidentsForCurrentUser()
    }

    // ฟังก์ชันดึงเหตุการณ์ที่เสร็จสิ้นแล้ว
    fun getCompletedIncidents(): LiveData<List<Incident>> {
        return incidentRepository.getCompletedIncidentsForCurrentUser()
    }

    // ฟังก์ชันดึงเหตุการณ์ทั้งหมด
    fun getAllIncidents(): LiveData<List<Incident>> {
        return incidentRepository.getAllIncidentsForCurrentUser()
    }

    // ฟังก์ชันกรองเหตุการณ์ตามประเภท
    fun filterIncidentsByType(incidents: List<Incident>, incidentType: String): List<Incident> {
        if (incidentType.isEmpty()) {
            return incidents
        }
        return incidents.filter { it.incidentType == incidentType }
    }

    // ฟังก์ชันเรียงลำดับเหตุการณ์ตามเวลาที่แจ้ง (ล่าสุดไปเก่าสุด)
    fun sortIncidentsByTime(incidents: List<Incident>): List<Incident> {
        return incidents.sortedByDescending { it.reportedAt }
    }

    // ฟังก์ชันเรียงลำดับเหตุการณ์ตามสถานะ
    fun sortIncidentsByStatus(incidents: List<Incident>): List<Incident> {
        // เรียงลำดับตามความสำคัญของสถานะ
        val statusOrder = mapOf(
            "รอรับเรื่อง" to 0,
            "เจ้าหน้าที่รับเรื่องแล้ว" to 1,
            "กำลังดำเนินการ" to 2,
            "เสร็จสิ้น" to 3
        )

        return incidents.sortedBy { statusOrder[it.status] ?: 4 }
    }
}