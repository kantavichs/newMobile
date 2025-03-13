package com.example.sos.message

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sos.adapter.ChatRoomAdapter
import com.example.sos.chat.ChatActivity
import com.example.sos.databinding.FragmentMessageBinding
import com.example.sos.viewmodel.MessageViewModel

class MessageFragment : Fragment() {

    private var _binding: FragmentMessageBinding? = null
    private val binding get() = _binding!!

    private lateinit var messageViewModel: MessageViewModel
    private lateinit var adapter: ChatRoomAdapter

    private val TAG = "MessageFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        messageViewModel = ViewModelProvider(this).get(MessageViewModel::class.java)

        // แสดงสถานะโหลด
        showLoading(true)

        // ตั้งค่า RecyclerView
        adapter = ChatRoomAdapter { chatId, isActive ->
            Log.d(TAG, "Chat room clicked: $chatId, active: $isActive")
            navigateToChatActivity(chatId, isActive)
        }

        binding.rvChatRooms.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChatRooms.adapter = adapter

        // ตั้งค่า TabLayout
        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> filterChatRooms("all")
                    1 -> filterChatRooms("active")
                    2 -> filterChatRooms("inactive")
                }
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })

        // ตั้งค่า SearchView
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchChatRooms(newText ?: "")
                return true
            }
        })

        // โหลดข้อมูลห้องแชท
        loadChatRooms()
    }

    private fun loadChatRooms() {
        messageViewModel.getChatRooms().observe(viewLifecycleOwner) { chatRooms ->
            // ซ่อนการโหลด
            showLoading(false)

            Log.d(TAG, "Chat rooms loaded: ${chatRooms.size}")

            if (chatRooms.isEmpty()) {
                showEmptyState(true)
            } else {
                showEmptyState(false)

                // บันทึกข้อมูลและอัปเดต adapter
                messageViewModel.setChatRoomsList(chatRooms)

                // ใช้ filter ตาม tab ที่เลือกอยู่
                val selectedTabPosition = binding.tabLayout.selectedTabPosition
                when (selectedTabPosition) {
                    0 -> adapter.submitList(chatRooms)
                    1 -> adapter.submitList(messageViewModel.getActiveChatRooms())
                    2 -> adapter.submitList(messageViewModel.getInactiveChatRooms())
                    else -> adapter.submitList(chatRooms)
                }
            }
        }
    }

    private fun filterChatRooms(filter: String) {
        when (filter) {
            "all" -> adapter.submitList(messageViewModel.getAllChatRooms())
            "active" -> adapter.submitList(messageViewModel.getActiveChatRooms())
            "inactive" -> adapter.submitList(messageViewModel.getInactiveChatRooms())
        }

        // ตรวจสอบว่ามีข้อมูลให้แสดงหรือไม่
        showEmptyState(adapter.itemCount == 0)
    }

    private fun searchChatRooms(query: String) {
        val filteredList = messageViewModel.searchChatRooms(query)
        adapter.submitList(filteredList)

        // ตรวจสอบว่ามีข้อมูลให้แสดงหรือไม่
        showEmptyState(filteredList.isEmpty())
    }

    private fun navigateToChatActivity(chatId: String, isActive: Boolean) {
        if (isActive) {
            val intent = Intent(requireContext(), ChatActivity::class.java)
            intent.putExtra("chatId", chatId)
            startActivity(intent)
        } else {
            // ถ้าแชทปิดแล้ว อาจจะไม่ต้องเปิดหรือแสดงข้อความเตือน
            Log.d(TAG, "Chat room is inactive, not opening")
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.rvChatRooms.visibility = View.GONE
            binding.tvEmpty.visibility = View.VISIBLE
        } else {
            binding.rvChatRooms.visibility = View.VISIBLE
            binding.tvEmpty.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}