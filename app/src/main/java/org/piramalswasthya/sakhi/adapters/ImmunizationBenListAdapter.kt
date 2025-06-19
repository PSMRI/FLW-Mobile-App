package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.RvItemImmunizationBenBinding
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.BenWithAncListDomain
import org.piramalswasthya.sakhi.model.ImmunizationDetailsDomain

class ImmunizationBenListAdapter(
    private val clickListener: VaccinesClickListener? = null,
    private val pref: PreferenceDao? = null
) : ListAdapter<ImmunizationDetailsDomain, ImmunizationBenListAdapter.BenVaccineViewHolder>(
    BenDiffUtilCallBack
) {
    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<ImmunizationDetailsDomain>() {
        override fun areItemsTheSame(
            oldItem: ImmunizationDetailsDomain, newItem: ImmunizationDetailsDomain
        ) = oldItem.ben.benId == newItem.ben.benId

        override fun areContentsTheSame(
            oldItem: ImmunizationDetailsDomain, newItem: ImmunizationDetailsDomain
        ) = oldItem == newItem

    }

    class BenVaccineViewHolder private constructor(private val binding: RvItemImmunizationBenBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): BenVaccineViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemImmunizationBenBinding.inflate(layoutInflater, parent, false)
                return BenVaccineViewHolder(binding)
            }
        }

        fun bind(
            item: ImmunizationDetailsDomain, clickListener: VaccinesClickListener?, pref: PreferenceDao?
        ) {

            binding.temp = item.ben
            binding.clickListener = clickListener
            binding.executePendingBindings()

        }
    }



    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): BenVaccineViewHolder = BenVaccineViewHolder.from(parent)

    override fun onBindViewHolder(holder: BenVaccineViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener, pref)

    }


    class VaccinesClickListener(
        private val clickedVaccine: (benId: Long) -> Unit,
        private val callBen: (ben: BenBasicDomain) -> Unit

        ) {
        fun onClickedBen(item: BenBasicDomain) = clickedVaccine(
            item.benId
        )
        fun onClickedForCall(item: BenBasicDomain) = callBen(item)

    }

}