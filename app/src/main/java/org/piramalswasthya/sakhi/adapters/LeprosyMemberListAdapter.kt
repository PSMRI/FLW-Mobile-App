package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemLeprosyMemberListBinding
import org.piramalswasthya.sakhi.model.BenWithKALAZARScreeningDomain
import org.piramalswasthya.sakhi.model.BenWithLeprosyScreeningDomain

class LeprosyMemberListAdapter(
    private val clickListener: ClickListener? = null
) :
    ListAdapter<BenWithLeprosyScreeningDomain, LeprosyMemberListAdapter.BenViewHolder>
        (BenDiffUtilCallBack) {
    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<BenWithLeprosyScreeningDomain>() {
        override fun areItemsTheSame(
            oldItem: BenWithLeprosyScreeningDomain,
            newItem: BenWithLeprosyScreeningDomain
        ) = oldItem.ben.benId == newItem.ben.benId

        override fun areContentsTheSame(
            oldItem: BenWithLeprosyScreeningDomain,
            newItem: BenWithLeprosyScreeningDomain
        ) = oldItem == newItem

    }

    class BenViewHolder private constructor(private val binding: RvItemLeprosyMemberListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): BenViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemLeprosyMemberListBinding.inflate(layoutInflater, parent, false)
                return BenViewHolder(binding)
            }
        }

        fun bind(
            item: BenWithLeprosyScreeningDomain,
            clickListener: ClickListener?,
        ) {
            binding.benWithLeprosy = item

            /*if(item.tb?.historyOfTb == true){
                binding.cvContent.visibility = View.GONE
            }*/

            binding.ivSyncState.visibility = if (item.leprosy == null) View.GONE else View.VISIBLE

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
            if ( item.leprosy != null && item.ben.isDeath) {
                binding.btnFormTb.visibility = View.VISIBLE
            } else if (item.leprosy == null && !item.ben.isDeath){
                binding.btnFormTb.visibility = View.VISIBLE
            } else if (item.leprosy != null && !item.ben.isDeath){
                binding.btnFormTb.visibility = View.VISIBLE
            } else {
                binding.btnFormTb.visibility = View.INVISIBLE
            }
            val (text, colorRes) = when {

                item.leprosy == null -> {
                    "Screening" to android.R.color.holo_green_dark
                }
                item.leprosy.leprosySymptomsPosition == 1 -> {
                    "Screening" to android.R.color.holo_green_dark
                }
                item.leprosy.leprosySymptomsPosition == 0 && item.leprosy.isConfirmed -> {
                    "Follow Up" to android.R.color.holo_red_dark
                }
                item.leprosy.leprosySymptomsPosition == 0 -> {
                    "Suspected" to android.R.color.holo_red_dark
                }
                else -> {
                    "View" to android.R.color.holo_green_dark
                }
            }

            binding.btnFormTb.text = text
            binding.btnFormTb.setBackgroundColor(
                binding.root.resources.getColor(colorRes, binding.root.context.theme)
            )
           /* binding.btnFormTb.text = if (item.leprosy == null) "Register" else "View"
            item.leprosy.leprosySymptomsPosition
            binding.btnFormTb.setBackgroundColor(binding.root.resources.getColor(if (item.leprosy == null) android.R.color.holo_red_dark else android.R.color.holo_green_dark))*/
            binding.clickListener = clickListener

            binding.executePendingBindings()

        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) =
        BenViewHolder.from(parent)

    override fun onBindViewHolder(holder: BenViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }


    class ClickListener(
        private val clickedForm: ((hhId: Long, benId: Long) -> Unit)? = null,
        private val clickedVisits: ((benWithLeprosy: BenWithLeprosyScreeningDomain) -> Unit)? = null


    ) {
        fun onClickForm(item: BenWithLeprosyScreeningDomain) =
            clickedForm?.let { it(item.ben.hhId, item.ben.benId) }

        fun onClickVisits(item: BenWithLeprosyScreeningDomain) =
            clickedVisits?.let { it(item) }
    }

}