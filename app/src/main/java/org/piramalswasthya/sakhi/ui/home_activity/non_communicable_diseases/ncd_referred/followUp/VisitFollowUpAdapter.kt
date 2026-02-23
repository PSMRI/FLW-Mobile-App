package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_referred.followUp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.R

class VisitFollowUpAdapter : RecyclerView.Adapter<VisitFollowUpAdapter.VisitViewHolder>() {

    private var visits: List<VisitItem> = emptyList()

    inner class VisitViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val tvVisitHeader: TextView = view.findViewById(R.id.tv_visit_header)
        val rvFollowUps: RecyclerView = view.findViewById(R.id.rv_followups)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_ncd_followup_table, parent, false)
        return VisitViewHolder(view)
    }

    override fun onBindViewHolder(holder: VisitViewHolder, position: Int) {
        val item = visits[position]

        if (item.visitHeader.isNullOrBlank()) {
            holder.tvVisitHeader.visibility = View.GONE
        } else {
            holder.tvVisitHeader.visibility = View.VISIBLE
            holder.tvVisitHeader.text = item.visitHeader
        }
        holder.rvFollowUps.layoutManager = LinearLayoutManager(holder.rvFollowUps.context)
        holder.rvFollowUps.adapter = FollowUpAdapter(item.followUps)
    }

    override fun getItemCount(): Int = visits.size

    fun submitList(list: List<VisitItem>) {
        visits = list
        notifyDataSetChanged()
    }
}
