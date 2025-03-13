package com.example.sos.report

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.sos.R
import com.example.sos.home.HomeActivity
import com.example.sos.viewmodel.ReportViewModel

class ReportActivity : AppCompatActivity() {

    private lateinit var tvReporterName: TextView
    private lateinit var tvIncidentTitle: TextView
    private lateinit var spinnerRelation: Spinner
    private lateinit var etLocation: EditText
    private lateinit var etAdditionalInfo: EditText
    private lateinit var btnSubmit: Button
    private lateinit var backButton: TextView

    private lateinit var reportViewModel: ReportViewModel
    private var incidentType: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        // ผูกตัวแปรกับ views
        tvReporterName = findViewById(R.id.tvReporterName)
        tvIncidentTitle = findViewById(R.id.tvIncidentTitle)
        spinnerRelation = findViewById(R.id.spinnerRelation)
        etLocation = findViewById(R.id.etLocation)
        etAdditionalInfo = findViewById(R.id.etAdditionalInfo)
        btnSubmit = findViewById(R.id.btnSubmit)
        backButton = findViewById(R.id.backButton)

        // รับค่า incidentType ที่ส่งมาจากปุ่มที่กดในหน้าแรก
        incidentType = intent.getStringExtra("incidentType") ?: ""

        // แสดงชื่อเหตุการณ์ในหน้า
        tvIncidentTitle.text = incidentType

        reportViewModel = ViewModelProvider(this).get(ReportViewModel::class.java)

        // แสดงข้อมูลผู้แจ้งเหตุ
        reportViewModel.getCurrentUser().observe(this) { user ->
            tvReporterName.text = "${user.firstName} ${user.lastName}"
        }

        // ตั้งค่า spinner สำหรับความเกี่ยวข้องกับผู้ประสบเหตุ
        val relationOptions = arrayOf("ผู้ประสบเหตุ", "ผู้เห็นเหตุการณ์", "เพื่อนผู้ประสบเหตุ")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, relationOptions)
        spinnerRelation.adapter = adapter

        // ตั้งค่าปุ่มย้อนกลับให้กลับไปหน้า Home
        backButton.setOnClickListener {
            // ใช้ Intent เพื่อกลับไปยังหน้า HomeActivity
            val intent = Intent(this, HomeActivity::class.java)
            // เพิ่ม FLAG_ACTIVITY_CLEAR_TOP เพื่อล้าง activity stack
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish() // ปิด ReportActivity นี้
        }

        // ตั้งค่าปุ่มยืนยัน
        btnSubmit.setOnClickListener {
            val location = etLocation.text.toString()
            val additionalInfo = etAdditionalInfo.text.toString()
            val relation = spinnerRelation.selectedItem.toString()

            if (location.isBlank()) {
                Toast.makeText(this, "กรุณาระบุสถานที่เกิดเหตุ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // แสดง loading หรือปิดปุ่มระหว่างรอการส่งข้อมูล
            btnSubmit.isEnabled = false
            btnSubmit.text = "กำลังส่งข้อมูล..."

            reportViewModel.reportIncident(incidentType, location, relation, additionalInfo)
                .observe(this) { result ->
                    if (result.success) {
                        // ไปยังหน้าสรุปการแจ้งเหตุพร้อมส่งข้อมูลที่กรอกไป
                        val intent = Intent(this, SummaryActivity::class.java)
                        intent.putExtra("incidentId", result.incidentId)
                        intent.putExtra("reporterName", tvReporterName.text.toString())
                        intent.putExtra("incidentType", incidentType)
                        intent.putExtra("relation", relation)
                        intent.putExtra("location", location)
                        intent.putExtra("additionalInfo", additionalInfo)
                        startActivity(intent)
                        finish()
                    } else {
                        // เปิดปุ่มอีกครั้งเมื่อเกิดข้อผิดพลาด
                        btnSubmit.isEnabled = true
                        btnSubmit.text = "ยืนยัน"
                        Toast.makeText(this, result.message ?: "เกิดข้อผิดพลาด", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}