package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.RvItemVelunaribilityListBinding
import org.piramalswasthya.sakhi.helpers.getLocalizedAge
import org.piramalswasthya.sakhi.model.BenWithTbScreeningDomain
import org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.vulnerable_population.list.VulnerablePopulationViewModel

class VulnerableListAdapter(
    private val clickListener: ClickListener? = null,
    var viewModel: VulnerablePopulationViewModel
) :
    ListAdapter<BenWithTbScreeningDomain, VulnerableListAdapter.BenViewHolder>
        (BenDiffUtilCallBack) {
    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<BenWithTbScreeningDomain>() {
        override fun areItemsTheSame(
            oldItem: BenWithTbScreeningDomain,
            newItem: BenWithTbScreeningDomain
        ) = oldItem.ben.benId == newItem.ben.benId

        override fun areContentsTheSame(
            oldItem: BenWithTbScreeningDomain,
            newItem: BenWithTbScreeningDomain
        ) = oldItem == newItem

    }

    class BenViewHolder private constructor(private val binding: RvItemVelunaribilityListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): BenViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemVelunaribilityListBinding.inflate(layoutInflater, parent, false)
                return BenViewHolder(binding)
            }
        }

        fun bind(
            item: BenWithTbScreeningDomain,
            clickListener: ClickListener?,
            viewModel: VulnerablePopulationViewModel
        ) {
            binding.benWithTb = item
            binding.clickListener = clickListener
            val riskFactorAdapter = RiskFactorAdapter()
            binding.riskfactRV.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                adapter = riskFactorAdapter
            }

            riskFactorAdapter.submitList(getRiskFactor(item.tb?.keyPopulationRiskFactors?:emptyList(), viewModel))
            val params = binding.btnFormTb.layoutParams as ConstraintLayout.LayoutParams

            params.topToBottom = ConstraintLayout.LayoutParams.UNSET

            binding.btnFormTb.layoutParams = params

            binding.nikshayLayout.visibility = View.VISIBLE
            binding.RCHlayout.visibility = View.GONE
            /*  if(item.tb?.historyOfTb == true){
                  binding.cvContent.visibility = View.GONE
              } */

            binding.ivSyncState.visibility = if (item.tb == null) View.INVISIBLE else View.VISIBLE

            if (item.ben.spouseName == "Not Available" && item.ben.fatherName == "Not Available") {
                binding.father = true
                binding.husband = false
                binding.spouse = false
            } else {
                if (item.ben.gender == "MALE") {
                    binding.father = true
                    binding.husband = false
                    binding.spouse = false
                } else if (item.ben.gender == "FEMALE") {
                    if (item.ben.ageInt > 15) {
                        binding.father =
                            item.ben.fatherName != "Not Available" && item.ben.spouseName == "Not Available"
                        binding.husband = item.ben.spouseName != "Not Available"
                        binding.spouse = false
                    } else {
                        binding.father = true
                        binding.husband = false
                        binding.spouse = false
                    }
                } else {
                    binding.father =
                        item.ben.fatherName != "Not Available" && item.ben.spouseName == "Not Available"
                    binding.spouse = item.ben.spouseName != "Not Available"
                    binding.husband = false
                }
            }


            binding.status.setImageResource(
                if (item.tb == null) R.drawable.ic_crossed_circle
                else R.drawable.ic_verified
            )

            binding.btnFormTb.text = if (item.tb == null) binding.root.context.getString(R.string.screen) else binding.root.context.getString(R.string.view_screen)

            binding.btnFormTb.setBackgroundColor(binding.root.resources.getColor(if (item.tb == null) android.R.color.holo_red_dark else android.R.color.holo_green_dark))
            binding.age.text = getLocalizedAge(binding.root.context, item.ben.dob)


            if (item.ben.isDeath) {
                binding.linearTbScreeningListLayout.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.linearTbScreeningListLayout.context,
                        R.color.md_theme_dark_outline
                    )
                )
                binding.ivSyncState.visibility = View.GONE
                binding.btnFormTb.visibility = View.GONE
            } else {
                binding.linearTbScreeningListLayout.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.linearTbScreeningListLayout.context,
                        R.color.md_theme_light_primary
                    )
                )
            }
            binding.executePendingBindings()

        }

        private fun getRiskFactor(savedCodes: List<String>,viewModel: VulnerablePopulationViewModel)  : List<String>
            {

                val options = viewModel.masterRiskFactorOptions()

                return savedCodes.mapNotNull { savedCode ->
                    options.firstOrNull { option ->
                        option.code == savedCode
                    }?.label
                }
            }


    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) =
        BenViewHolder.from(parent)

    override fun onBindViewHolder(holder: BenViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener,viewModel)
    }


    class ClickListener(
        private val clickedForm: ((hhId: Long, benId: Long) -> Unit)? = null

    ) {
        fun onClickForm(item: BenWithTbScreeningDomain) =
            clickedForm?.let { it(item.ben.hhId, item.ben.benId) }
    }




}