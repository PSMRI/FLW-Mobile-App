package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_referred.followUp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.R

class FollowUpAdapter(private val followUps: List<String>) :
    RecyclerView.Adapter<FollowUpAdapter.FollowUpViewHolder>() {

    inner class FollowUpViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val tvFollowUp: TextView = view.findViewById(R.id.tv_followup)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowUpViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_followup, parent, false)
        return FollowUpViewHolder(view)
    }

    override fun onBindViewHolder(holder: FollowUpViewHolder, position: Int) {
        holder.tvFollowUp.text = followUps[position]
    }

    override fun getItemCount(): Int = followUps.size
}
