package com.example.sos.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sos.R
import com.example.sos.models.SurvivalGuide

class GuidesAdapter(
    private val onItemClick: (SurvivalGuide) -> Unit
) : ListAdapter<SurvivalGuide, GuidesAdapter.GuideViewHolder>(GuideDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuideViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_guide, parent, false)
        return GuideViewHolder(view)
    }

    override fun onBindViewHolder(holder: GuideViewHolder, position: Int) {
        val guide = getItem(position)
        holder.bind(guide, onItemClick)
    }

    class GuideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvGuideTitle)
        private val tvIncidentType: TextView = itemView.findViewById(R.id.tvGuideIncidentType)
        private val ivGuideImage: ImageView = itemView.findViewById(R.id.ivGuideImage)

        fun bind(guide: SurvivalGuide, onItemClick: (SurvivalGuide) -> Unit) {
            tvTitle.text = guide.title
            tvIncidentType.text = guide.incidentType

            // Load image if available
            if (guide.hasImage()) {
                ivGuideImage.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(guide.imageUrl)
                    .placeholder(R.drawable.placeholder_guide)
                    .error(R.drawable.error_guide)
                    .centerCrop()
                    .into(ivGuideImage)
            } else {
                ivGuideImage.visibility = View.GONE
            }

            // Set click listener to open guide details
            itemView.setOnClickListener {
                onItemClick(guide)
            }
        }
    }

    private class GuideDiffCallback : DiffUtil.ItemCallback<SurvivalGuide>() {
        override fun areItemsTheSame(oldItem: SurvivalGuide, newItem: SurvivalGuide): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SurvivalGuide, newItem: SurvivalGuide): Boolean {
            return oldItem == newItem
        }
    }

    // Function to update the list with a specific incident type filter
    fun updateWithFilter(guides: List<SurvivalGuide>, incidentType: String?) {
        val filteredGuides = if (incidentType.isNullOrEmpty()) {
            guides
        } else {
            guides.filter { it.incidentType == incidentType }
        }
        submitList(filteredGuides)
    }

    // Function to search through guides
    fun searchGuides(guides: List<SurvivalGuide>, query: String) {
        if (query.isEmpty()) {
            submitList(guides)
            return
        }

        val searchQuery = query.lowercase()
        val filteredGuides = guides.filter {
            it.title.lowercase().contains(searchQuery) ||
                    it.content.lowercase().contains(searchQuery) ||
                    it.incidentType.lowercase().contains(searchQuery)
        }
        submitList(filteredGuides)
    }
}