package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.RvItemMalariaMembersListBinding
import org.piramalswasthya.sakhi.model.BenWithMalariaScreeningDomain
import timber.log.Timber

class MalariaMemberListAdapter(
    private val clickListener: ClickListener? = null
) :
    ListAdapter<BenWithMalariaScreeningDomain, MalariaMemberListAdapter.BenViewHolder>
        (BenDiffUtilCallBack) {
    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<BenWithMalariaScreeningDomain>() {
        override fun areItemsTheSame(
            oldItem: BenWithMalariaScreeningDomain,
            newItem: BenWithMalariaScreeningDomain
        ) = oldItem.ben.benId == newItem.ben.benId

        override fun areContentsTheSame(
            oldItem: BenWithMalariaScreeningDomain,
            newItem: BenWithMalariaScreeningDomain
        ) = oldItem == newItem

    }

    class BenViewHolder private constructor(private val binding: RvItemMalariaMembersListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): BenViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemMalariaMembersListBinding.inflate(layoutInflater, parent, false)
                return BenViewHolder(binding)
            }
        }

        fun bind(
            item: BenWithMalariaScreeningDomain,
            clickListener: ClickListener?,
        ) {
            binding.benWithMalaria = item

            /*if(item.tb?.historyOfTb == true){
                binding.cvContent.visibility = View.GONE
            }*/

            binding.ivSyncState.visibility = if (item.tb == null) View.INVISIBLE else View.VISIBLE

            try {
                if(item.tb!!.caseStatus != null) {
                    binding.ivMalariaStatus.visibility = View.VISIBLE
                    if (item.tb.caseStatus == "Confirmed") {
                        Glide.with(binding.ivSyncState).load(R.drawable.mosquito).into(binding.ivMalariaStatus)
                    } else if (item.tb.caseStatus == "Suspected") {
                        Glide.with(binding.ivSyncState).load(R.drawable.warning).into(binding.ivMalariaStatus)
                    }else if (item.tb.caseStatus == "Not Confirmed") {
                        Glide.with(binding.ivSyncState).load(R.drawable.ic_check_circle).into(binding.ivMalariaStatus)
                    } else if (item.tb.caseStatus == "Treatment Started"){
                        Glide.with(binding.ivSyncState).load(R.drawable.pill).into(binding.ivMalariaStatus)
                    } else {
                        Glide.with(binding.ivSyncState).load(R.drawable.warning).into(binding.ivMalariaStatus)

                    }

                } else {
                    binding.ivMalariaStatus.visibility =  View.INVISIBLE
                }
            } catch (e:Exception) {
                Timber.d("Exception at case status : $e collected")

            }



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

            binding.btnFormTb.text = if (item.tb == null) "Register" else "View"
            binding.btnFormTb.setBackgroundColor(binding.root.resources.getColor(if (item.tb == null) android.R.color.holo_red_dark else android.R.color.holo_green_dark))
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
        fun onClickForm(item: BenWithMalariaScreeningDomain) =
            clickedForm?.let { it(item.ben.hhId, item.ben.benId) }
    }

}