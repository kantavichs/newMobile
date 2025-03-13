package com.example.sos.guides

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sos.R
import com.example.sos.adapter.GuidesAdapter
import com.example.sos.databinding.ActivitySurvivalGuidesBinding
import com.example.sos.models.SurvivalGuide
import com.example.sos.viewmodel.GuidesViewModel
import com.google.android.material.chip.Chip
import android.view.View
import android.text.Html
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

// SurvivalGuidesActivity.kt
class SurvivalGuidesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySurvivalGuidesBinding
    private lateinit var guidesViewModel: GuidesViewModel
    private lateinit var adapter: GuidesAdapter
    private var allGuides: List<SurvivalGuide> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySurvivalGuidesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        guidesViewModel = ViewModelProvider(this).get(GuidesViewModel::class.java)

        // เพิ่ม logging เพื่อดีบัก
        Log.d("SurvivalGuidesActivity", "Activity created, setting up...")

        // แสดงสถานะ loading
        binding.progressBar.visibility = View.VISIBLE
        binding.emptyView.visibility = View.GONE

        // ตั้งค่า RecyclerView
        adapter = GuidesAdapter { guide ->
            Log.d("SurvivalGuidesActivity", "Guide selected: ${guide.id} - ${guide.title}")
            showGuideDialog(guide)
        }
        binding.rvGuides.layoutManager = LinearLayoutManager(this)
        binding.rvGuides.adapter = adapter

        // ตั้งค่าการค้นหา
        setupSearch()

        // ตั้งค่าตัวกรองหมวดหมู่
        setupCategoryFilter()

        // โหลดข้อมูลคู่มือ
        guidesViewModel.getAllGuides().observe(this) { guides ->
            binding.progressBar.visibility = View.GONE

            Log.d("SurvivalGuidesActivity", "Guides loaded: ${guides.size}")

            // เพื่อการดีบัก แสดงรายการคู่มือที่โหลดได้
            for (guide in guides) {
                Log.d("SurvivalGuidesActivity", "Guide: ${guide.id} - ${guide.title} (${guide.incidentType})")
            }

            // สร้างคู่มือพื้นฐานหากไม่พบข้อมูลใดๆ
            if (guides.isEmpty()) {
                Log.d("SurvivalGuidesActivity", "No guides found, creating basic guides")
                val basicGuides = createBasicGuides()
                allGuides = basicGuides
                adapter.submitList(basicGuides)
            } else {
                allGuides = guides
                adapter.submitList(guides)
            }

            // แสดงข้อความว่างเมื่อไม่มีข้อมูล
            if (allGuides.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.rvGuides.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.GONE
                binding.rvGuides.visibility = View.VISIBLE
            }
        }



        // ปุ่มย้อนกลับ
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    // ฟังก์ชันสร้างคู่มือพื้นฐานกรณีที่ไม่พบข้อมูลใน Firebase
    private fun createBasicGuides(): List<SurvivalGuide> {
        val guides = ArrayList<SurvivalGuide>()

        // คู่มืออุบัติเหตุบนถนน
        guides.add(SurvivalGuide(
            id = "basic001",
            title = "วิธีปฐมพยาบาลเบื้องต้นเมื่อเกิดอุบัติเหตุบนท้องถนน",
            content = "1. ตรวจสอบความปลอดภัยของพื้นที่เกิดเหตุ\n2. ประเมินอาการผู้บาดเจ็บเบื้องต้น\n3. โทรแจ้งเหตุที่ 1669 ให้ข้อมูลสถานที่ชัดเจน\n4. ถ้าผู้บาดเจ็บไม่รู้สึกตัว ให้จับชีพจรและตรวจดูการหายใจ\n5. ห้ามเคลื่อนย้ายผู้บาดเจ็บหากสงสัยว่ากระดูกสันหลังบาดเจ็บ",
            incidentType = "อุบัติเหตุบนถนน"
        ))

        // คู่มือเจอสัตว์มีพิษ
        guides.add(SurvivalGuide(
            id = "basic002",
            title = "วิธีรับมือเมื่อพบงูพิษในบริเวณมหาวิทยาลัย",
            content = "1. รักษาความสงบ ไม่ตื่นตระหนก\n2. ถอยห่างจากงูอย่างช้าๆ\n3. แจ้งเจ้าหน้าที่รักษาความปลอดภัย\n4. ไม่พยายามจับหรือไล่งูด้วยตนเอง\n5. หากถูกงูกัด ให้นั่งนิ่งๆ และรีบไปพบแพทย์ทันที",
            incidentType = "จับสัตว์"
        ))

        return guides
    }

    private fun setupSearch() {
        // ค้นหาเมื่อกดปุ่ม search บนคีย์บอร์ด
        binding.searchEditText.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(textView.text.toString())
                return@setOnEditorActionListener true
            }
            false
        }

        // ค้นหาแบบ real-time เมื่อพิมพ์
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString() ?: ""
                performSearch(query)
            }
        })
    }

    private fun setupCategoryFilter() {
        // กรองตามหมวดหมู่เมื่อเลือก Chip
        binding.chipAll.setOnClickListener { filterByCategory(null) }
        binding.chipAccident.setOnClickListener { filterByCategory("อุบัติเหตุบนถนน") }
        binding.chipAnimal.setOnClickListener { filterByCategory("จับสัตว์") }
        binding.chipFight.setOnClickListener { filterByCategory("ทะเลาะวิวาท") }
        binding.chipFire.setOnClickListener { filterByCategory("ไฟไหม้") }
        binding.chipHealth.setOnClickListener { filterByCategory("สุขภาพ") }
        binding.chipOther.setOnClickListener { filterByCategory("อื่นๆ") }
    }

    private fun performSearch(query: String) {
        if (query.isEmpty()) {
            // ถ้าไม่มีคำค้นหา ให้แสดงตามหมวดหมู่ที่เลือกอยู่
            val selectedChip = findSelectedCategoryChip()
            if (selectedChip == binding.chipAll) {
                adapter.submitList(allGuides)
            } else {
                val category = getCategoryFromChip(selectedChip)
                adapter.updateWithFilter(allGuides, category)
            }
        } else {
            // ค้นหาตามคำค้นหา
            adapter.searchGuides(allGuides, query)
        }

        // แสดงข้อความว่างถ้าไม่พบผลลัพธ์
        checkEmptyState()
    }

    private fun filterByCategory(category: String?) {
        val query = binding.searchEditText.text.toString()
        if (query.isEmpty()) {
            adapter.updateWithFilter(allGuides, category)
        } else {
            // กรองจากผลการค้นหาก่อนหน้า
            val filteredBySearch = allGuides.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.content.contains(query, ignoreCase = true) ||
                        it.incidentType.contains(query, ignoreCase = true)
            }
            adapter.updateWithFilter(filteredBySearch, category)
        }

        // แสดงข้อความว่างถ้าไม่พบผลลัพธ์
        checkEmptyState()
    }

    private fun findSelectedCategoryChip(): Chip {
        if (binding.chipAccident.isChecked) return binding.chipAccident
        if (binding.chipAnimal.isChecked) return binding.chipAnimal
        if (binding.chipFight.isChecked) return binding.chipFight
        if (binding.chipFire.isChecked) return binding.chipFire
        if (binding.chipHealth.isChecked) return binding.chipHealth
        if (binding.chipOther.isChecked) return binding.chipOther
        return binding.chipAll // ค่าเริ่มต้น
    }

    private fun getCategoryFromChip(chip: Chip): String? {
        return when (chip.id) {
            R.id.chipAccident -> "อุบัติเหตุบนถนน"
            R.id.chipAnimal -> "จับสัตว์"
            R.id.chipFight -> "ทะเลาะวิวาท"
            R.id.chipFire -> "ไฟไหม้"
            R.id.chipHealth -> "สุขภาพ"
            R.id.chipOther -> "อื่นๆ"
            else -> null
        }
    }

    private fun checkEmptyState() {
        val currentList = adapter.currentList
        if (currentList.isEmpty()) {
            binding.emptyView.visibility = android.view.View.VISIBLE
            binding.rvGuides.visibility = android.view.View.GONE
        } else {
            binding.emptyView.visibility = android.view.View.GONE
            binding.rvGuides.visibility = android.view.View.VISIBLE
        }
    }

    private fun showGuideDialog(guide: SurvivalGuide) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_guide)

        val tvTitle = dialog.findViewById<TextView>(R.id.tvGuideTitle)
        val tvContent = dialog.findViewById<TextView>(R.id.tvGuideContent)
        val btnClose = dialog.findViewById<Button>(R.id.btnCloseGuide)

        tvTitle.text = guide.title

        // แปลง HTML content เป็นข้อความที่แสดงผลได้
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            tvContent.text = Html.fromHtml(guide.content.replace("\n", "<br>"), Html.FROM_HTML_MODE_COMPACT)
        } else {
            @Suppress("DEPRECATION")
            tvContent.text = Html.fromHtml(guide.content.replace("\n", "<br>"))
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}