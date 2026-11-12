package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.RvItemBenPwRegistrationListWithFormBinding
import org.piramalswasthya.sakhi.helpers.getLocalizedAge
import org.piramalswasthya.sakhi.model.BenWithPwrDomain
import org.piramalswasthya.sakhi.utils.RoleConstants

class PwRegistrationListAdapter(
    private val clickListener: ClickListener? = null,
    private val pref: PreferenceDao? = null
) :
    ListAdapter<BenWithPwrDomain, PwRegistrationListAdapter.BenViewHolder>
        (BenDiffUtilCallBack) {
    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<BenWithPwrDomain>() {
        override fun areItemsTheSame(
            oldItem: BenWithPwrDomain,
            newItem: BenWithPwrDomain
        ) = oldItem.ben.benId == newItem.ben.benId

        override fun areContentsTheSame(
            oldItem: BenWithPwrDomain,
            newItem: BenWithPwrDomain
        ) = oldItem == newItem

    }

    class BenViewHolder private constructor(private val binding: RvItemBenPwRegistrationListWithFormBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): BenViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemBenPwRegistrationListWithFormBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
                return BenViewHolder(binding)
            }
        }

        fun bind(
            item: BenWithPwrDomain,
            clickListener: ClickListener?,
            pref: PreferenceDao?
        ) {

            if (pref?.getLoggedInUser()?.role.equals(RoleConstants.ROLE_ASHA_SUPERVISOR, true)) {
                binding.btnFormEc1.visibility = View.GONE
            } else {
                binding.btnFormEc1.visibility = View.VISIBLE
            }

            binding.benWithPwr = item
            binding.ivSyncState.visibility = if (item.pwr == null) View.INVISIBLE else View.VISIBLE
//binding.btnFormEc1.text = if (item.pwr == null) binding.root.resources.getString(R.string.register) else binding.root.resources.getString(R.string.view)
            binding.btnFormEc1.text = if (item.pwr == null) binding.root.resources.getString(R.string.register) else binding.root.resources.getString(R.string.ds_view) // Redesign: "View"
//binding.age.text = getLocalizedAge(binding.root.context, item.ben.dob)
            binding.age.text = dsWifeOfAndAge(binding.root.context, item.ben.spouseName, getLocalizedAge(binding.root.context, item.ben.dob)) // Redesign meta line
//binding.btnFormEc1.setBackgroundColor(binding.root.resources.getColor(if (item.pwr == null) android.R.color.holo_red_dark else android.R.color.holo_green_dark))
            // Redesign (Figma M2.2-02): "Register" = filled primary, "View" = tonal (was red / green)
            val ctx = binding.root.context
            val isView = item.pwr != null
            binding.btnFormEc1.backgroundTintList = androidx.core.content.ContextCompat.getColorStateList(
                ctx, if (isView) R.color.ds_tonal_bg else R.color.ds_filled_bg
            )
            binding.btnFormEc1.setTextColor(
                androidx.core.content.ContextCompat.getColorStateList(ctx, if (isView) R.color.ds_tonal_fg else R.color.ds_filled_fg)
            )
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
        holder.bind(getItem(position), clickListener, pref)
    }


    class ClickListener(
        private val clickedBen: (benId: Long) -> Unit,
        private val clickedForm: ((hhId: Long, benId: Long) -> Unit)? = null

    ) {
        fun onClickedBen(item: BenWithPwrDomain) = clickedBen(item.ben.benId)
        fun onClickForm(item: BenWithPwrDomain) =
            clickedForm?.let { it(item.ben.hhId, item.ben.benId) }
    }

}