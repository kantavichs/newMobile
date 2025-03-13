package com.example.sos.message

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sos.R
import com.example.sos.adapter.ChatRoomAdapter
import com.example.sos.chat.ChatActivity
import com.example.sos.databinding.FragmentMessageBinding
import com.example.sos.viewmodel.MessageViewModel

// MessageFragment.kt
class MessageFragment : Fragment() {

    private lateinit var binding: FragmentMessageBinding
    private lateinit var messageViewModel: MessageViewModel
    private lateinit var adapter: ChatRoomAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        messageViewModel = ViewModelProvider(this).get(MessageViewModel::class.java)

        // ตั้งค่า RecyclerView
        adapter = ChatRoomAdapter { chatId, isActive ->
            if (isActive) {
                val intent = Intent(requireContext(), ChatActivity::class.java)
                intent.putExtra("chatId", chatId)
                startActivity(intent)
            }
        }
        binding.rvChatRooms.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChatRooms.adapter = adapter

        // โหลดข้อมูลห้องแชท
        messageViewModel.getChatRooms().observe(viewLifecycleOwner) { chatRooms ->
            adapter.submitList(chatRooms)
        }
    }
}