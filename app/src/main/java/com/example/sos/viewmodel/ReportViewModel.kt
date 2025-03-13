package com.example.sos.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.sos.models.User
import com.example.sos.repository.ReportRepository
import com.example.sos.repository.UserRepository

class ReportViewModel : ViewModel() {
    private val reportRepository = ReportRepository()
    private val userRepository = UserRepository()

    // ฟังก์ชันรายงานเหตุการณ์ใหม่
    fun reportIncident(
        incidentType: String,
        location: String,
        relationToVictim: String,
        additionalInfo: String
    ): LiveData<ReportRepository.ReportResult> {
        return reportRepository.reportIncident(
            incidentType,
            location,
            relationToVictim,
            additionalInfo
        )
    }

    // ฟังก์ชันดึงข้อมูลผู้ใช้ปัจจุบัน
    fun getCurrentUser(): LiveData<User> {
        return userRepository.getCurrentUser()
    }

    // ฟังก์ชันตรวจสอบความถูกต้องของข้อมูลรายงาน
    fun isValidReportData(location: String): Boolean {
        // ตรวจสอบเบื้องต้นว่าระบุตำแหน่งเหตุการณ์หรือไม่
        return location.isNotBlank()
    }

    // ฟังก์ชันดึงข้อมูลเหตุการณ์ตาม ID
    fun getIncidentById(incidentId: String): LiveData<com.example.sos.models.Incident> {
        return reportRepository.getIncidentById(incidentId)
    }
}