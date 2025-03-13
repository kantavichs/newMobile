//java/com/example/sos/home/HomeFragment.kt
package com.example.sos.home

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.sos.R
import com.example.sos.guides.SurvivalGuidesActivity
import com.example.sos.report.ReportActivity

class HomeFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // ค้นหา ImageSlider จาก layout
        val imageSlider = view.findViewById<ImageSlider>(R.id.imageSlider)
        val slideModels = ArrayList<SlideModel>()

        // เพิ่มรูปภาพลงใน ImageSlider
        slideModels.add(SlideModel(R.drawable.b1, ScaleTypes.FIT))
        slideModels.add(SlideModel(R.drawable.b2, ScaleTypes.FIT))

        imageSlider.setImageList(slideModels, ScaleTypes.FIT)

        // ตั้งค่า listener สำหรับปุ่มแจ้งเหตุ
        val btnAccident = view.findViewById<Button>(R.id.btncar)
        val btnAnimal = view.findViewById<Button>(R.id.btnsnake)
        val btnFight = view.findViewById<Button>(R.id.btnbad)
        val btnSuspect = view.findViewById<Button>(R.id.btnaccountalert)
        val btnCarCrash = view.findViewById<Button>(R.id.btncarcrash)
        val btnHealth = view.findViewById<Button>(R.id.btnheart)
        val btnFire = view.findViewById<Button>(R.id.btnfire)
        val btnOther = view.findViewById<Button>(R.id.btnplus)

        btnAccident.setOnClickListener { navigateToReport("อุบัติเหตุบนถนน") }
        btnAnimal.setOnClickListener { navigateToReport("จับสัตว์") }
        btnFight.setOnClickListener { navigateToReport("ทะเลาะวิวาท") }
        btnSuspect.setOnClickListener { navigateToReport("บุคคลต้องสงสัย") }
        btnCarCrash.setOnClickListener { navigateToReport("รถเสีย") }
        btnHealth.setOnClickListener { navigateToReport("สุขภาพ") }
        btnFire.setOnClickListener { navigateToReport("ไฟไหม้") }
        btnOther.setOnClickListener { navigateToReport("อื่นๆ") }

        // ตั้งค่า listener สำหรับปุ่มคู่มือเอาตัวรอด
        val guidesBanner = view.findViewById<TextView>(R.id.tvGuideTitle) // เปลี่ยนจาก textView เป็น tvGuideTitle
        guidesBanner.setOnClickListener {
            val intent = Intent(requireContext(), SurvivalGuidesActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun navigateToReport(incidentType: String) {
        val intent = Intent(requireContext(), ReportActivity::class.java)
        intent.putExtra("incidentType", incidentType)
        startActivity(intent)
    }
}