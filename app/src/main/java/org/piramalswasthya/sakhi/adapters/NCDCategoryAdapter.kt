package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.RvTabBinding
import org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_eligible_list.NcdCategory
import org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_eligible_list.NcdEligibleListViewModel

class NCDCategoryAdapter(
    private val dataList: ArrayList<Pair<NcdCategory, Int>>,
    private val listener: ClickListener? = null,
    var viewModel: NcdEligibleListViewModel
) : RecyclerView.Adapter<NCDCategoryAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder.from(parent)


    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val item = dataList[position]
        holder.bind(item)
        holder.binding.cMainLayout.setOnClickListener {
            viewModel.clickedPosition = position
            listener?.onClicked(item.first)
            notifyDataSetChanged()
        }
        if (viewModel.clickedPosition == position) {
            holder.binding.cMainLayout.setBackgroundResource(R.drawable.tab_selection)
        } else {
            holder.binding.cMainLayout.setBackgroundResource(R.drawable.tab_unselection)
        }
    }


    class ViewHolder private constructor(val binding: RvTabBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvTabBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }

        fun bind(item: Pair<NcdCategory, Int>) {
            binding.monthText.text =
                binding.root.context.getString(item.first.labelResId) + " (${item.second})"
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun updateData(newList: ArrayList<Pair<NcdCategory, Int>>) {
        dataList.clear()
        dataList.addAll(newList)
        notifyDataSetChanged()
    }

    fun interface ClickListener {
        fun onClicked(category: NcdCategory)
    }
}
