package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.ItemChildImmunizationDoseBinding
import org.piramalswasthya.sakhi.model.VaccineDomain



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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        IconViewHolder.from(parent)

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
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

        /*fun bind(
            item: VaccineDomain,
            clickListener: ImmunizationClickListener?
        ) {
            binding.vaccine = item
            binding.clickListener = clickListener


            binding.idSwitch.setOnCheckedChangeListener(null)
            binding.idSwitch.isChecked = item.isSwitchChecked
            if (item.state.name =="PENDING"||item.state.name =="OVERDUE"){
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
                    clickListener?.onClicked(position,item.apply {
                        isSwitchChecked = true })
                }else{
                    clickListener?.onClicked(position,item.apply { isSwitchChecked = false })
                }

            }
            binding.executePendingBindings()


        }*/

        fun bind(
            item: VaccineDomain,
            clickListener: ImmunizationClickListener?
        ) {
            binding.vaccine = item
            binding.clickListener = clickListener

            // Reset listeners before re-binding
            binding.idSwitch.setOnCheckedChangeListener(null)
            binding.idSwitch.setOnClickListener(null)

            // Set switch state
            binding.idSwitch.isChecked = item.isSwitchChecked

            when (item.state.name) {
                "PENDING", "OVERDUE" -> {
                    binding.idSwitch.isEnabled = true
                    binding.idSwitch.setOnCheckedChangeListener { _, isChecked ->
                        clickListener?.onClicked(adapterPosition, item.apply { isSwitchChecked = isChecked })
                    }
                }

                "MISSED", "UNAVAILABLE" -> {
                    binding.idSwitch.isEnabled = true   // keep it tappable
                    binding.idSwitch.isChecked = false
                    binding.idSwitch.setOnClickListener {
                        Toast.makeText(
                            binding.root.context,
                            "Immunization cannot be done as it is missed",
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.idSwitch.isChecked = false // always force back to off
                    }
                }

                else -> { // DONE
                    binding.idSwitch.isEnabled = false
                    binding.idSwitch.isChecked = true
                }
            }

            binding.executePendingBindings()
        }

    }


    class ImmunizationClickListener(val selectedListener: (position:Int, vaccine: VaccineDomain) -> Unit) {
        fun onClicked(position:Int,vaccine: VaccineDomain) = selectedListener(position,vaccine)

    }
}