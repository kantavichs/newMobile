package com.example.sos.guides

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

        // ตั้งค่า RecyclerView
        adapter = GuidesAdapter { guide ->
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
            allGuides = guides
            adapter.submitList(guides)
            binding.progressBar.visibility = android.view.View.GONE

            // แสดงข้อความว่างเมื่อไม่มีข้อมูล
            if (guides.isEmpty()) {
                binding.emptyView.visibility = android.view.View.VISIBLE
            } else {
                binding.emptyView.visibility = android.view.View.GONE
            }
        }

        // ปุ่มย้อนกลับ
        binding.btnBack.setOnClickListener {
            finish()
        }
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
        tvContent.text = guide.content

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}