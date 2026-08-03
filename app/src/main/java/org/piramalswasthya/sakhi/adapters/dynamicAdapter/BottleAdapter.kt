package org.piramalswasthya.sakhi.adapters.dynamicAdapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.model.BottleItem

/**
 * @param showAsVisitNumber FLW-1129 — on the Mitanin build the raw bottle count is not shown at
 * all. The table collapses to "IFA Visit N | Date of Provision", so the serial column is dropped
 * and the middle column carries the visit number instead of the stored bottle tally.
 */
class BottleAdapter(
    private val items: List<BottleItem>,
    private val showAsVisitNumber: Boolean = false
) : RecyclerView.Adapter<BottleAdapter.BottleViewHolder>() {

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
        val visitNumber = (position + 1).toString()

        if (showAsVisitNumber) {
            holder.tvSrNo.visibility = View.GONE
            holder.tvBottleNumber.text = visitNumber
            (holder.itemView as? LinearLayout)?.weightSum = TWO_COLUMN_WEIGHT_SUM
        } else {
            holder.tvSrNo.visibility = View.VISIBLE
            holder.tvSrNo.text = visitNumber
            holder.tvBottleNumber.text = item.bottleNumber
        }

        holder.tvDate.text = item.dateOfProvision
    }

    override fun getItemCount(): Int = items.size

    private companion object {
        /** item_bottle_row declares weightSum 3; with the serial column gone only two remain. */
        const val TWO_COLUMN_WEIGHT_SUM = 2f
    }
}
