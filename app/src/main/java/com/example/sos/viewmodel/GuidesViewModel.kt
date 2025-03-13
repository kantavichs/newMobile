package com.example.sos.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.sos.models.SurvivalGuide
import com.example.sos.repository.GuidesRepository

class GuidesViewModel : ViewModel() {
    private val guidesRepository = GuidesRepository()

    // ฟังก์ชันดึงคู่มือเอาตัวรอดทั้งหมด
    fun getAllGuides(): LiveData<List<SurvivalGuide>> {
        return guidesRepository.getAllGuides()
    }

    // ฟังก์ชันดึงคู่มือเอาตัวรอดตามประเภทเหตุการณ์
    fun getGuidesByIncidentType(incidentType: String): LiveData<List<SurvivalGuide>> {
        return guidesRepository.getGuidesByIncidentType(incidentType)
    }

    // ฟังก์ชันดึงคู่มือเอาตัวรอดตาม ID
    fun getGuideById(guideId: String): LiveData<SurvivalGuide> {
        return guidesRepository.getGuideById(guideId)
    }

    // ฟังก์ชันเรียงลำดับคู่มือเอาตัวรอดตามชื่อ
    fun sortGuidesByTitle(guides: List<SurvivalGuide>): List<SurvivalGuide> {
        return guides.sortedBy { it.title }
    }

    // ฟังก์ชันกรองคู่มือเอาตัวรอดที่มีรูปภาพ
    fun filterGuidesByHasImage(guides: List<SurvivalGuide>): List<SurvivalGuide> {
        return guides.filter { it.hasImage() }
    }

    // ฟังก์ชันค้นหาคู่มือเอาตัวรอด
    fun searchGuides(guides: List<SurvivalGuide>, query: String): List<SurvivalGuide> {
        if (query.isEmpty()) {
            return guides
        }

        val lowerCaseQuery = query.lowercase()
        return guides.filter {
            it.title.lowercase().contains(lowerCaseQuery) ||
                    it.content.lowercase().contains(lowerCaseQuery) ||
                    it.incidentType.lowercase().contains(lowerCaseQuery)
        }
    }

    // ฟังก์ชันจัดกลุ่มคู่มือเอาตัวรอดตามประเภทเหตุการณ์
    fun groupGuidesByIncidentType(guides: List<SurvivalGuide>): Map<String, List<SurvivalGuide>> {
        return guides.groupBy { it.incidentType }
    }
}