package org.piramalswasthya.sakhi.adapters.dynamicAdapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.model.BottleItem

class BottleAdapter(private val items: List<BottleItem>) :
    RecyclerView.Adapter<BottleAdapter.BottleViewHolder>() {

    inner class BottleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSrNo = itemView.findViewById<TextView>(R.id.tvSrNo)
        val tvBottleNumber = itemView.findViewById<TextView>(R.id.tvBottleNumber)
        val tvDate = itemView.findViewById<TextView>(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bottle_row, parent, false)
        return BottleViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: BottleViewHolder, position: Int) {
        val item = items[position]
        holder.tvSrNo.text = (position+1).toString()
        holder.tvBottleNumber.text = item.bottleNumber
        holder.tvDate.text = item.dateOfProvision
    }

    override fun getItemCount(): Int = items.size
}
