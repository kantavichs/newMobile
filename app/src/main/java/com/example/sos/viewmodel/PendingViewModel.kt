package com.example.sos.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sos.models.Incident
import com.example.sos.repository.IncidentRepository

class PendingViewModel : ViewModel() {
    private val incidentRepository = IncidentRepository()

    // เพิ่มตัวแปรเพื่อเก็บข้อมูล incidents
    private val _activeIncidents = MutableLiveData<List<Incident>>()
    val activeIncidents: LiveData<List<Incident>> = _activeIncidents

    private val _completedIncidents = MutableLiveData<List<Incident>>()
    val completedIncidents: LiveData<List<Incident>> = _completedIncidents

    // เมธอดสำหรับโหลดข้อมูลเหตุการณ์ที่กำลังดำเนินการ
    fun loadActiveIncidents(): LiveData<List<Incident>> {
        return incidentRepository.getActiveIncidentsForCurrentUser()
    }

    // เมธอดสำหรับโหลดข้อมูลเหตุการณ์ที่เสร็จสิ้นแล้ว
    fun loadCompletedIncidents(): LiveData<List<Incident>> {
        return incidentRepository.getCompletedIncidentsForCurrentUser()
    }

    // เมธอดสำหรับกรองเหตุการณ์ตามประเภท
    fun filterIncidentsByType(incidents: List<Incident>, incidentType: String): List<Incident> {
        if (incidentType.isEmpty()) {
            return incidents
        }
        return incidents.filter { it.incidentType == incidentType }
    }

    // เมธอดสำหรับเรียงลำดับเหตุการณ์ตามเวลาที่แจ้ง (ล่าสุดไปเก่าสุด)
    fun sortIncidentsByTime(incidents: List<Incident>): List<Incident> {
        return incidents.sortedByDescending { it.reportedAt }
    }

    // เมธอดสำหรับเรียงลำดับเหตุการณ์ตามสถานะ
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