package com.example.sos.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.sos.models.Incident
import com.example.sos.repository.IncidentRepository
import com.example.sos.repository.ReportRepository

class SummaryViewModel : ViewModel() {
    private val incidentRepository = IncidentRepository()
    private val reportRepository = ReportRepository()

    // ฟังก์ชันดึงข้อมูลเหตุการณ์
    fun getIncidentDetails(incidentId: String): LiveData<Incident> {
        return incidentRepository.getIncidentLiveUpdates(incidentId)
    }

    // ฟังก์ชันดึงข้อมูลเหตุการณ์แบบครั้งเดียว (non-live)
    fun getIncidentById(incidentId: String): LiveData<Incident> {
        return reportRepository.getIncidentById(incidentId)
    }

    // ฟังก์ชันดึงเบอร์โทรของเจ้าหน้าที่ที่รับเรื่อง
    fun getStaffPhone(incidentId: String): LiveData<String> {
        return incidentRepository.getAssignedStaffPhone(incidentId)
    }

    // ฟังก์ชันตรวจสอบสถานะการรับเรื่อง
    fun isIncidentAssigned(incident: Incident): Boolean {
        return incident.assignedStaffId.isNotEmpty()
    }

    // ฟังก์ชันตรวจสอบว่าเหตุการณ์ยังดำเนินการอยู่หรือไม่
    fun isIncidentActive(incident: Incident): Boolean {
        return incident.isActive()
    }
}