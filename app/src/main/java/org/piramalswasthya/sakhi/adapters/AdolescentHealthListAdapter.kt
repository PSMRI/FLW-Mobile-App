package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemAdolscentHealthListBinding
import org.piramalswasthya.sakhi.model.BenWithAdolescentDomain


class AdolescentHealthListAdapter(
    private val clickListener: ClickListener? = null,
    private val showBeneficiaries: Boolean = false,
    private val showRegistrationDate: Boolean = false,
    private val showSyncIcon: Boolean = false,
    private val showAbha: Boolean = false,
    private val role: Int? = 0
) :
    ListAdapter<BenWithAdolescentDomain, AdolescentHealthListAdapter.BenViewHolder>(BenDiffUtilCallBack) {
    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<BenWithAdolescentDomain>() {
        override fun areItemsTheSame(
            oldItem: BenWithAdolescentDomain, newItem: BenWithAdolescentDomain
        ) = oldItem.ben.benId == newItem.ben.benId

        override fun areContentsTheSame(
            oldItem: BenWithAdolescentDomain, newItem: BenWithAdolescentDomain
        ) = oldItem == newItem

    }

    class BenViewHolder private constructor(private val binding: RvItemAdolscentHealthListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): BenViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemAdolscentHealthListBinding.inflate(layoutInflater, parent, false)
                return BenViewHolder(binding)
            }
        }

        fun bind(
            item: BenWithAdolescentDomain,
            clickListener: ClickListener?,
            showAbha: Boolean,
            showSyncIcon: Boolean,
            showRegistrationDate: Boolean,
            showBeneficiaries: Boolean, role: Int?
        ) {
            binding.ivSyncState.visibility = if (item.adolescent == null) View.INVISIBLE else View.VISIBLE

            binding.ben = item
            binding.clickListener = clickListener
            binding.showAbha = showAbha
            binding.showRegistrationDate = showRegistrationDate
            binding.registrationDate.visibility =
                if (showRegistrationDate) View.VISIBLE else View.INVISIBLE
            binding.blankSpace.visibility =
                if (showRegistrationDate) View.VISIBLE else View.INVISIBLE
            binding.hasAbha = !item.ben.abhaId.isNullOrEmpty()
            binding.role = role

            if (showBeneficiaries) {
                if (item.ben.spouseName == "Not Available" && item.ben.fatherName == "Not Available") {
                    binding.father = true
                    binding.husband = false
                    binding.spouse = false
                } else {
                    if (item.ben.gender == "MALE") {
                        binding.father = true
                        binding.husband = false
                        binding.spouse = false
                        binding.btnFormTb.visibility = View.GONE
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
                        binding.btnFormTb.visibility = View.VISIBLE

                    } else {
                        binding.father =
                            item.ben.fatherName != "Not Available" && item.ben.spouseName == "Not Available"
                        binding.spouse = item.ben.spouseName != "Not Available"
                        binding.husband = false
                    }
                }
            } else {
                binding.father = false
                binding.husband = false
                binding.spouse = false
            }
            binding.btnFormTb.text = if (item.adolescent == null) "Register" else "View"
            binding.btnFormTb.setBackgroundColor(binding.root.resources.getColor(if (item.adolescent == null) android.R.color.holo_red_dark else android.R.color.holo_green_dark))
            binding.clickListener = clickListener

            binding.executePendingBindings()

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = BenViewHolder.from(parent)

    override fun onBindViewHolder(holder: BenViewHolder, position: Int) {
        holder.bind(
            getItem(position),
            clickListener,
            showAbha,
            showSyncIcon,
            showRegistrationDate,
            showBeneficiaries,
            role
        )
    }


    class ClickListener(
        private val clickedForm: ((hhId: Long, benId: Long) -> Unit)? = null

    ) {
        fun onClickForm(item: BenWithAdolescentDomain) =
            clickedForm?.let { it(item.ben.hhId, item.ben.benId) }
    }

}