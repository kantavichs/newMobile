package com.example.sos.report

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import com.example.sos.R
import com.example.sos.guides.SurvivalGuidesActivity
import com.example.sos.home.HomeActivity
import com.example.sos.chat.ChatActivity
import com.example.sos.models.Incident
import com.example.sos.viewmodel.SummaryViewModel

class SummaryActivity : AppCompatActivity() {

    private lateinit var tvStatusTitle: TextView
    private lateinit var tvReporterName: TextView
    private lateinit var tvRelation: TextView
    private lateinit var tvIncidentType: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvAdditionalInfo: TextView
    private lateinit var chatButton: ImageView
    private lateinit var callButton: ImageView
    private lateinit var chatButtonContainer: CardView
    private lateinit var callButtonContainer: CardView
    private lateinit var backButton: TextView
    private lateinit var guideButton: Button
    private lateinit var guideCardView: CardView

    // เพิ่ม progress indicator
    private lateinit var progressLayout: View

    private lateinit var summaryViewModel: SummaryViewModel
    private var incidentId: String = ""

    // สำหรับเก็บข้อมูลเหตุการณ์ที่ดึงมา
    private var currentIncident: Incident? = null

    companion object {
        private const val TAG = "SummaryActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        // ผูกตัวแปรกับ views
        tvStatusTitle = findViewById(R.id.tvStatusTitle)
        tvReporterName = findViewById(R.id.tvReporterName)
        tvRelation = findViewById(R.id.tvRelation)
        tvIncidentType = findViewById(R.id.tvIncidentType)
        tvLocation = findViewById(R.id.tvLocation)
        tvAdditionalInfo = findViewById(R.id.tvAdditionalInfo)
        chatButton = findViewById(R.id.chatButton)
        callButton = findViewById(R.id.callButton)
        chatButtonContainer = findViewById(R.id.chatButtonContainer)
        callButtonContainer = findViewById(R.id.callButtonContainer)
        backButton = findViewById(R.id.backButton)
        guideButton = findViewById(R.id.guideButton)
        guideCardView = findViewById(R.id.guideCardView)

        // เพิ่ม ProgressBar (ต้องเพิ่ม View ในไฟล์ layout ด้วย)
        progressLayout = findViewById(R.id.progressLayout)

        // รับค่าจาก Intent
        incidentId = intent.getStringExtra("incidentId") ?: ""

        Log.d(TAG, "Received incidentId: $incidentId")

        if (incidentId.isEmpty()) {
            Toast.makeText(this, "ไม่พบข้อมูลการแจ้งเหตุ", Toast.LENGTH_SHORT).show()
            navigateToHome()
            return
        }

        summaryViewModel = ViewModelProvider(this).get(SummaryViewModel::class.java)

        // แสดง Progress ระหว่างโหลดข้อมูล
        showProgress(true)

        // รับข้อมูลเหตุการณ์และอัพเดทสถานะ
        summaryViewModel.getIncidentDetails(incidentId).observe(this) { incident ->
            // ซ่อน Progress เมื่อโหลดเสร็จ
            showProgress(false)

            if (incident != null && incident.id.isNotEmpty()) {
                currentIncident = incident
                updateUI(incident)
                Log.d(TAG, "Incident data loaded: ${incident.id}, status: ${incident.status}")
            } else {
                Log.e(TAG, "Failed to load incident data")
                Toast.makeText(this, "ไม่สามารถโหลดข้อมูลเหตุการณ์ได้", Toast.LENGTH_SHORT).show()
            }
        }

        // ตั้งค่าปุ่มแชท
        chatButtonContainer.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("chatId", incidentId)
            startActivity(intent)
        }

        chatButton.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("chatId", incidentId)
            startActivity(intent)
        }

        // ตั้งค่าปุ่มโทร
        callButtonContainer.setOnClickListener {
            handleCallButtonClick()
        }

        callButton.setOnClickListener {
            handleCallButtonClick()
        }

        // ตั้งค่าปุ่มย้อนกลับ
        backButton.setOnClickListener {
            navigateToHome()
        }

        // ตั้งค่าปุ่มดูคู่มือเอาตัวรอด
        guideButton.setOnClickListener {
            val intent = Intent(this, SurvivalGuidesActivity::class.java)
            // ส่งประเภทเหตุการณ์ไปให้หน้าคู่มือเพื่อกรองคู่มือที่เกี่ยวข้อง
            intent.putExtra("incidentType", currentIncident?.incidentType ?: "")
            startActivity(intent)
        }
    }

    // แสดงหรือซ่อน Progress
    private fun showProgress(show: Boolean) {
        progressLayout.visibility = if (show) View.VISIBLE else View.GONE
    }

    // อัพเดท UI ด้วยข้อมูลเหตุการณ์ที่ดึงมา
    private fun updateUI(incident: Incident) {
        // อัพเดทสถานะในหัวข้อ
        when (incident.status) {
            "รอรับเรื่อง" -> tvStatusTitle.text = "กำลังรอเจ้าหน้าที่รับเรื่อง"
            "เจ้าหน้าที่รับเรื่องแล้ว" -> tvStatusTitle.text = "เจ้าหน้าที่รับเรื่องแล้ว"
            "กำลังดำเนินการ" -> tvStatusTitle.text = "กำลังดำเนินการ"
            "เสร็จสิ้น" -> tvStatusTitle.text = "เสร็จสิ้น"
            else -> tvStatusTitle.text = incident.status
        }

        // อัพเดทข้อมูลรายละเอียด
        tvReporterName.text = incident.reporterName
        tvIncidentType.text = incident.incidentType
        tvLocation.text = incident.location
        tvRelation.text = incident.relationToVictim
        tvAdditionalInfo.text = incident.additionalInfo

        Log.d(TAG, "UI updated with incident data: ${incident.incidentType}, ${incident.location}")
    }

    private fun handleCallButtonClick() {
        if (currentIncident == null) {
            Toast.makeText(this, "กำลังโหลดข้อมูล โปรดลองอีกครั้ง", Toast.LENGTH_SHORT).show()
            return
        }

        // ตรวจสอบว่ามีเบอร์โทรเพิ่มเติมที่ระบุในข้อมูลเพิ่มเติมหรือไม่
        val additionalPhoneNumber = extractPhoneNumber(currentIncident?.additionalInfo ?: "")

        if (additionalPhoneNumber.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$additionalPhoneNumber")
            startActivity(intent)
        } else {
            // ถ้าไม่มีเบอร์โทรในข้อมูลเพิ่มเติม ให้ใช้เบอร์ของเจ้าหน้าที่
            showProgress(true)
            summaryViewModel.getStaffPhone(incidentId).observe(this) { phone ->
                showProgress(false)
                if (phone.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse("tel:$phone")
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "ไม่พบเบอร์โทรศัพท์สำหรับติดต่อ", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    /**
     * ดึงเบอร์โทรศัพท์จากข้อความที่กรอกในข้อมูลเพิ่มเติม
     */
    private fun extractPhoneNumber(text: String): String {
        // รูปแบบของเบอร์โทรศัพท์ในประเทศไทย เช่น 08x-xxx-xxxx, 08xxxxxxxx, 02-xxx-xxxx
        val phoneRegex = Regex("(0[689]\\d[\\s-]?\\d{3}[\\s-]?\\d{4})|(0\\d[\\s-]?\\d{3}[\\s-]?\\d{4})")
        val result = phoneRegex.find(text)

        return result?.value?.replace(Regex("[\\s-]"), "") ?: ""
    }
}