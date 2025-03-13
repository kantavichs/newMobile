package com.example.sos.pending

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
                    }
                    R.id.btnCompleted -> {
                        binding.rvActiveIncidents.visibility = View.GONE
                        binding.rvCompletedIncidents.visibility = View.VISIBLE
                    }
                }
            }
        }

        // เลือก tab กำลังดำเนินการเป็นค่าเริ่มต้น
        binding.segmentButton.check(R.id.btnActive)

        // โหลดข้อมูลเหตุการณ์
        pendingViewModel.getActiveIncidents().observe(viewLifecycleOwner) { incidents ->
            activeAdapter.submitList(incidents)
        }

        pendingViewModel.getCompletedIncidents().observe(viewLifecycleOwner) { incidents ->
            completedAdapter.submitList(incidents)
        }
    }

    private fun navigateToSummary(incidentId: String) {
        val intent = Intent(requireContext(), SummaryActivity::class.java)
        intent.putExtra("incidentId", incidentId)
        startActivity(intent)
    }
}