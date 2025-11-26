package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemMalariaCasesConfirmedListBinding
import org.piramalswasthya.sakhi.model.BenWithMalariaConfirmedDomain

class MalariaConfirmedCasesListAdapter(
    private val clickListener: ClickListener? = null
) :
    ListAdapter<BenWithMalariaConfirmedDomain, MalariaConfirmedCasesListAdapter.BenViewHolder>
        (BenDiffUtilCallBack) {
    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<BenWithMalariaConfirmedDomain>() {
        override fun areItemsTheSame(
            oldItem: BenWithMalariaConfirmedDomain,
            newItem: BenWithMalariaConfirmedDomain
        ) = oldItem.ben.benId == newItem.ben.benId

        override fun areContentsTheSame(
            oldItem: BenWithMalariaConfirmedDomain,
            newItem: BenWithMalariaConfirmedDomain
        ) = oldItem == newItem

    }

    class BenViewHolder private constructor(private val binding: RvItemMalariaCasesConfirmedListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): BenViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemMalariaCasesConfirmedListBinding.inflate(layoutInflater, parent, false)
                return BenViewHolder(binding)
            }
        }

        fun bind(
            item: BenWithMalariaConfirmedDomain,
            clickListener: ClickListener?,
        ) {
            binding.benWithMalariaConfirmed = item


            binding.ivSyncState.visibility = if (item.malariaConfirmed == null) View.GONE else View.VISIBLE

/*            try {
                if(item.malariaConfirmed!!.caseStatus != null) {
                    binding.ivMalariaStatus.visibility = View.VISIBLE
                    if (item.malariaConfirmed.caseStatus == "Confirmed") {
                        Glide.with(binding.ivSyncState).load(R.drawable.mosquito).into(binding.ivMalariaStatus)
                    } else if (item.malariaConfirmed.caseStatus == "Suspected") {
                        Glide.with(binding.ivSyncState).load(R.drawable.warning).into(binding.ivMalariaStatus)
                    }else if (item.malariaConfirmed.caseStatus == "Not Confirmed") {
                        Glide.with(binding.ivSyncState).load(R.drawable.ic_check_circle).into(binding.ivMalariaStatus)
                    } else if (item.malariaConfirmed.caseStatus == "Treatment Started"){
                        Glide.with(binding.ivSyncState).load(R.drawable.pill).into(binding.ivMalariaStatus)
                    } else {
                        Glide.with(binding.ivSyncState).load(R.drawable.warning).into(binding.ivMalariaStatus)

                    }

                } else {
                    binding.ivMalariaStatus.visibility =  View.INVISIBLE
                }
            } catch (e:Exception) {
                Timber.d("Exception at case status : $e collected")

            }*/



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
            if ( item.malariaConfirmed != null && item.ben.isDeath) {
                binding.btnFormTb.visibility = View.VISIBLE
            } else if (item.malariaConfirmed == null && !item.ben.isDeath){
                binding.btnFormTb.visibility = View.VISIBLE
            } else if (item.malariaConfirmed != null && !item.ben.isDeath){
                binding.btnFormTb.visibility = View.VISIBLE
            } else {
                binding.btnFormTb.visibility = View.INVISIBLE
            }

            binding.btnFormTb.text = if (item.malariaConfirmed == null) "Follow Up" else item.malariaConfirmed.day
            binding.btnFormTb.setBackgroundColor(binding.root.resources.getColor(if (item.malariaConfirmed == null) android.R.color.holo_red_dark else android.R.color.holo_green_dark))
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
        private val clickedForm: ((hhId: Long, benId: Long) -> Unit)? = null

    ) {
        fun onClickForm(item: BenWithMalariaConfirmedDomain) =
            clickedForm?.let { it(item.ben.hhId, item.ben.benId) }
    }

}