package org.piramalswasthya.sakhi.adapters.dynamicAdapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.model.dynamicModel.VisitCard

class VisitCardAdapter(
    private val visits: List<VisitCard>,
    private val onVisitClick: (VisitCard) -> Unit
) : RecyclerView.Adapter<VisitCardAdapter.VisitViewHolder>() {

    inner class VisitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvVisitDay: TextView = view.findViewById(R.id.tvVisitDay)
        val tvVisitDate: TextView = view.findViewById(R.id.tvVisitDate)
        val btnView: View = view.findViewById(R.id.btnView)
        val btnAddVisit: View = view.findViewById(R.id.btnAddVisit)

        init {
            btnView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onVisitClick(visits[position])
                }
            }
            btnAddVisit.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onVisitClick(visits[position])
                }
            }
        }

    }

    private val lastDeathIndex: Int
        get() = visits.indexOfLast { it.isCompleted && it.isBabyDeath }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_visit_card, parent, false)
        return VisitViewHolder(view)
    }

    override fun onBindViewHolder(holder: VisitViewHolder, position: Int) {
        val visit = visits[position]
        holder.tvVisitDay.text = visit.visitDay
        holder.tvVisitDate.text = visit.visitDate
        holder.btnView.visibility = View.GONE
        holder.btnAddVisit.visibility = View.GONE

        if (lastDeathIndex != -1 && position > lastDeathIndex) {
            holder.itemView.setBackgroundResource(R.color.read_only)
            holder.itemView.isEnabled = false
            holder.btnView.isEnabled = false
            holder.btnAddVisit.isEnabled = false
            return
        }

        when {
            visit.isCompleted -> {
                holder.btnView.visibility = View.VISIBLE
                holder.btnView.setBackgroundResource(R.color.Quartenary)
                holder.itemView.setBackgroundResource(R.color.md_theme_dark_inversePrimary)
                holder.itemView.isEnabled = true
                holder.btnView.isEnabled = true
            }

            visit.isEditable -> {
                holder.btnAddVisit.visibility = View.VISIBLE
                holder.btnAddVisit.setBackgroundResource(R.color.Quartenary)
                holder.itemView.setBackgroundResource(R.color.md_theme_dark_inversePrimary)
                holder.itemView.isEnabled = true
                holder.btnAddVisit.isEnabled = true
            }

            else -> {
                holder.itemView.setBackgroundResource(R.color.read_only)
                holder.itemView.isEnabled = false
                holder.btnView.isEnabled = false
                holder.btnAddVisit.isEnabled = false
            }
        }
    }

    override fun getItemCount(): Int = visits.size
}
