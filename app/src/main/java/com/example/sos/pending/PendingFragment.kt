package com.example.sos.pending

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sos.R
import com.example.sos.adapter.IncidentAdapter
import com.example.sos.databinding.FragmentPendingBinding
import com.example.sos.report.SummaryActivity
import com.example.sos.viewmodel.PendingViewModel

// PendingFragment.kt
class PendingFragment : Fragment() {

    private lateinit var binding: FragmentPendingBinding
    private lateinit var pendingViewModel: PendingViewModel
    private lateinit var activeAdapter: IncidentAdapter
    private lateinit var completedAdapter: IncidentAdapter
    private var activeLoaded = false
    private var completedLoaded = false
    private var handlerTimeout: Handler? = null
    private var timeoutRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPendingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pendingViewModel = ViewModelProvider(this).get(PendingViewModel::class.java)

        // ตั้งค่า RecyclerView สำหรับรายการที่กำลังดำเนินการ
        activeAdapter = IncidentAdapter(true) { incidentId ->
            navigateToSummary(incidentId)
        }
        binding.rvActiveIncidents.layoutManager = LinearLayoutManager(requireContext())
        binding.rvActiveIncidents.adapter = activeAdapter

        // ตั้งค่า RecyclerView สำหรับรายการที่เสร็จสิ้นแล้ว
        completedAdapter = IncidentAdapter(false) { incidentId ->
            navigateToSummary(incidentId)
        }
        binding.rvCompletedIncidents.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCompletedIncidents.adapter = completedAdapter

        // ตั้งค่า tab ส่วนแยกระหว่างกำลังดำเนินการและเสร็จสิ้น
        binding.segmentButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnActive -> {
                        binding.rvActiveIncidents.visibility = View.VISIBLE
                        binding.rvCompletedIncidents.visibility = View.GONE
                        binding.tvEmptyActive.visibility = if (activeAdapter.itemCount == 0) View.VISIBLE else View.GONE
                        binding.tvEmptyCompleted.visibility = View.GONE
                    }
                    R.id.btnCompleted -> {
                        binding.rvActiveIncidents.visibility = View.GONE
                        binding.rvCompletedIncidents.visibility = View.VISIBLE
                        binding.tvEmptyActive.visibility = View.GONE
                        binding.tvEmptyCompleted.visibility = if (completedAdapter.itemCount == 0) View.VISIBLE else View.GONE
                    }
                }
            }
        }

        // เลือก tab กำลังดำเนินการเป็นค่าเริ่มต้น
        binding.segmentButton.check(R.id.btnActive)

        // แสดง ProgressBar ก่อนเริ่มโหลดข้อมูล
        binding.progressBar.visibility = View.VISIBLE

        // ซ่อนข้อความ "ไม่มีรายการ" ในขณะที่กำลังโหลดข้อมูล
        binding.tvEmptyActive.visibility = View.GONE
        binding.tvEmptyCompleted.visibility = View.GONE

        // ตั้งเวลา timeout
        handlerTimeout = Handler(Looper.getMainLooper())
        timeoutRunnable = Runnable {
            if (!activeLoaded || !completedLoaded) {
                binding.progressBar.visibility = View.GONE
                val msg = "ไม่สามารถโหลดข้อมูลได้ โปรดตรวจสอบการเชื่อมต่อ"
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()

                // แสดงข้อความว่างเปล่าตามแท็บที่เลือก
                if (binding.segmentButton.checkedButtonId == R.id.btnActive) {
                    binding.tvEmptyActive.visibility = View.VISIBLE
                } else {
                    binding.tvEmptyCompleted.visibility = View.VISIBLE
                }
            }
        }

        handlerTimeout?.postDelayed(timeoutRunnable!!, 15000) // 15 วินาที

        pendingViewModel.loadActiveIncidents().observe(viewLifecycleOwner) { incidents ->
            activeLoaded = true

            // อัพเดทข้อมูลให้ adapter
            activeAdapter.submitList(incidents)

            // ถ้าทั้งสองรายการโหลดเสร็จแล้ว ให้ซ่อน ProgressBar
            if (activeLoaded && completedLoaded) {
                binding.progressBar.visibility = View.GONE
                handlerTimeout?.removeCallbacks(timeoutRunnable!!)
            }

            // แสดงข้อความเมื่อไม่มีรายการ (เฉพาะเมื่อแท็บ active เปิดอยู่)
            if (binding.segmentButton.checkedButtonId == R.id.btnActive) {
                binding.tvEmptyActive.visibility = if (incidents.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        pendingViewModel.loadCompletedIncidents().observe(viewLifecycleOwner) { incidents ->
            completedLoaded = true

            // อัพเดทข้อมูลให้ adapter
            completedAdapter.submitList(incidents)

            // ถ้าทั้งสองรายการโหลดเสร็จแล้ว ให้ซ่อน ProgressBar
            if (activeLoaded && completedLoaded) {
                binding.progressBar.visibility = View.GONE
                handlerTimeout?.removeCallbacks(timeoutRunnable!!)
            }

            // แสดงข้อความเมื่อไม่มีรายการ (เฉพาะเมื่อแท็บ completed เปิดอยู่)
            if (binding.segmentButton.checkedButtonId == R.id.btnCompleted) {
                binding.tvEmptyCompleted.visibility = if (incidents.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // ยกเลิก timeout เมื่อออกจากหน้าจอ
        handlerTimeout?.removeCallbacks(timeoutRunnable!!)
        handlerTimeout = null
        timeoutRunnable = null
    }

    private fun navigateToSummary(incidentId: String) {
        // เพิ่ม log เพื่อตรวจสอบ
        Log.d("PendingFragment", "Navigating to summary with incidentId: $incidentId")

        if (incidentId.isEmpty()) {
            Toast.makeText(requireContext(), "ไม่สามารถดูรายละเอียดได้เนื่องจากไม่มีรหัสเหตุการณ์", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(requireContext(), SummaryActivity::class.java)
        intent.putExtra("incidentId", incidentId)
        startActivity(intent)
    }
}