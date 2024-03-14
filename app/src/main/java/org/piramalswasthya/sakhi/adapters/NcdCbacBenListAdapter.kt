package org.piramalswasthya.sakhi.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemNcdCbacBinding
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.BenWithCbacDomain

class NcdCbacBenListAdapter(
    private val clickListener: CbacFormClickListener
) : ListAdapter<BenWithCbacDomain, NcdCbacBenListAdapter.BenCbacViewHolder>(
    BenDiffUtilCallBack
) {
    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<BenWithCbacDomain>() {
        override fun areItemsTheSame(
            oldItem: BenWithCbacDomain, newItem: BenWithCbacDomain
        ) = oldItem.ben.benId == newItem.ben.benId

        override fun areContentsTheSame(
            oldItem: BenWithCbacDomain, newItem: BenWithCbacDomain
        ) = oldItem == newItem

    }

    class BenCbacViewHolder private constructor(private val binding: RvItemNcdCbacBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): BenCbacViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemNcdCbacBinding.inflate(layoutInflater, parent, false)
                return BenCbacViewHolder(binding)
            }
        }

        fun bind(
            item: BenWithCbacDomain, clickListener: CbacFormClickListener?
        ) {
            binding.benWithCbac = item
            binding.clickListener = clickListener

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

            binding.executePendingBindings()

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): BenCbacViewHolder = BenCbacViewHolder.from(parent)

    override fun onBindViewHolder(holder: BenCbacViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }


    class CbacFormClickListener(
        private val clickedView: (benId: Long) -> Unit,
        private val clickedNew: (benId: Long, dueDate: Long?) -> Unit,

        ) {
        fun onClickedView(item: BenWithCbacDomain) = clickedView(
            item.ben.benId
        )

        fun onClickedNew(item: BenWithCbacDomain) {

            clickedNew(
                item.ben.benId,
                if (item.savedCbacRecords.isEmpty()) null else {
                    val lastFillDate = item.savedCbacRecords.maxBy { it.fillDate }.fillDate
                    val nextAvailFilDate = lastFillDate + Konstants.minMillisBwtweenCbacFiling
                    if (System.currentTimeMillis() > nextAvailFilDate)
                        null
                    else
                        nextAvailFilDate
                }
            )

        }

    }

}