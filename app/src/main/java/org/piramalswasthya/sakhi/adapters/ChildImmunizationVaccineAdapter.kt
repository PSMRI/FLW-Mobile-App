package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.ItemChildImmunizationDoseBinding
import org.piramalswasthya.sakhi.model.VaccineDomain
import org.piramalswasthya.sakhi.model.VaccineState


class ChildImmunizationVaccineAdapter (private val clickListener: ImmunizationClickListener? = null) :
    ListAdapter<VaccineDomain, ChildImmunizationVaccineAdapter.IconViewHolder>(
        ImmunizationIconDiffCallback
    ) {
    object ImmunizationIconDiffCallback : DiffUtil.ItemCallback<VaccineDomain>() {
        override fun areItemsTheSame(oldItem: VaccineDomain, newItem: VaccineDomain) =
            oldItem.vaccineId == newItem.vaccineId

        override fun areContentsTheSame(oldItem: VaccineDomain, newItem: VaccineDomain) =
            (oldItem == newItem)

    }


    class IconViewHolder private constructor(private val binding: ItemChildImmunizationDoseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): IconViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemChildImmunizationDoseBinding.inflate(layoutInflater, parent, false)
                return IconViewHolder(binding)
            }
        }

        fun bind(
            item: VaccineDomain,
            clickListener: ImmunizationClickListener?
        ) {
            binding.vaccine = item
            binding.clickListener = clickListener
            binding.executePendingBindings()

            if (item.state.name =="PENDING"||item.state.name =="OVERDUE"||item.state.name =="PENDING" ){
                binding.idSwitch.isEnabled = true

            }else if(item.state.name =="MISSED" ||item.state.name =="UNAVAILABLE"){
                binding.idSwitch.isEnabled = false

            }else{
                //If state is DONE
                binding.idSwitch.isEnabled = false
                binding.idSwitch.isChecked = true
            }

            binding.idSwitch.setOnCheckedChangeListener { compoundButton, b ->
                if (b){
                    clickListener?.onClicked(item.apply {
                        isSwitchChecked = true })
                }else{
                    clickListener?.onClicked(item.apply { isSwitchChecked = false })
                }

            }


        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        IconViewHolder.from(parent)

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class ImmunizationClickListener(val selectedListener: (vaccine: VaccineDomain) -> Unit) {
        fun onClicked(vaccine: VaccineDomain) = selectedListener(vaccine)

    }
}