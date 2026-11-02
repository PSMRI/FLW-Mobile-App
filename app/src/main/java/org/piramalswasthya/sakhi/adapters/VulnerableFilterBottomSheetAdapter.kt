package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.RvRiskFactorFilterBinding
import org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.vulnerable_population.list.VulnerablePopulationViewModel

class VulnerableFilterBottomSheetAdapter (
    private val catDataList: ArrayList<String>,
    private val clickListener: CategoryClickListener? = null,
    var viewModel: VulnerablePopulationViewModel
) : RecyclerView.Adapter<VulnerableFilterBottomSheetAdapter.CategoryViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CategoryViewHolder.from(parent)


    override fun onBindViewHolder(
        holder: CategoryViewHolder,
        position: Int,
    ) {

        holder.bind(catDataList[position])
        holder.binding.cMainLayout.setOnClickListener {
            viewModel.selectedPosition = position
            clickListener?.onClicked(catDataList[position])
            notifyDataSetChanged()

        }
        if (viewModel.selectedPosition == position) {
            holder.binding.cMainLayout.setBackgroundResource(R.drawable.tab_selection)
        } else {
            holder.binding.cMainLayout.setBackgroundResource(R.drawable.tab_unselection)
        }

    }


    class CategoryViewHolder private constructor(val binding: RvRiskFactorFilterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): CategoryViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvRiskFactorFilterBinding.inflate(layoutInflater, parent, false)
                return CategoryViewHolder(binding)
            }
        }

        fun bind(
            catDataList: String,
        ) {

            binding.monthText.text = catDataList

        }

    }
    override fun getItemCount(): Int {
        return catDataList.size
    }

    fun interface CategoryClickListener
    {
        fun onClicked(catDataList: String)
    }
}