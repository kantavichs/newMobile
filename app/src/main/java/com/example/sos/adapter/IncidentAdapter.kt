package com.example.sos.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sos.R
import com.example.sos.models.Incident

class IncidentAdapter(
    private val isActiveList: Boolean,
    private val onItemClick: (String) -> Unit
) : ListAdapter<Incident, IncidentAdapter.IncidentViewHolder>(IncidentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncidentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_incident, parent, false)
        return IncidentViewHolder(view)
    }

    override fun onBindViewHolder(holder: IncidentViewHolder, position: Int) {
        val incident = getItem(position)
        holder.bind(incident, isActiveList, onItemClick)
    }

    class IncidentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvIncidentType: TextView = itemView.findViewById(R.id.tvIncidentType)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)

        fun bind(incident: Incident, isActiveList: Boolean, onItemClick: (String) -> Unit) {
            tvIncidentType.text = incident.incidentType
            tvLocation.text = incident.location
            tvStatus.text = incident.status

            // Display different dates based on active or completed list
            if (isActiveList) {
                // For active incidents, show when they were reported
                val dateTime = incident.getFormattedReportTime().split(" ")
                if (dateTime.size >= 2) {
                    tvDate.text = dateTime[0] // Date part
                    tvTime.text = dateTime[1] // Time part
                }
            } else {
                // For completed incidents, show when they were completed
                incident.completedAt?.let { completedTime ->
                    val dateTimeFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm")
                    val dateTime = dateTimeFormat.format(java.util.Date(completedTime)).split(" ")
                    if (dateTime.size >= 2) {
                        tvDate.text = dateTime[0] // Date part
                        tvTime.text = dateTime[1] // Time part
                    }
                }
            }

            // Set status text color based on status
            when (incident.status) {
                "รอรับเรื่อง" -> tvStatus.setTextColor(itemView.context.getColor(R.color.status_waiting))
                "เจ้าหน้าที่รับเรื่องแล้ว" -> tvStatus.setTextColor(itemView.context.getColor(R.color.status_assigned))
                "กำลังดำเนินการ" -> tvStatus.setTextColor(itemView.context.getColor(R.color.status_in_progress))
                "เสร็จสิ้น" -> tvStatus.setTextColor(itemView.context.getColor(R.color.status_completed))
            }

            // Set click listener
            itemView.setOnClickListener {
                onItemClick(incident.id)
            }
        }
    }

    private class IncidentDiffCallback : DiffUtil.ItemCallback<Incident>() {
        override fun areItemsTheSame(oldItem: Incident, newItem: Incident): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Incident, newItem: Incident): Boolean {
            return oldItem == newItem
        }
    }
}