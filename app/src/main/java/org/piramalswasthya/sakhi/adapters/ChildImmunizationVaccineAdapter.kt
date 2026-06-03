package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.ItemChildImmunizationDoseBinding
import org.piramalswasthya.sakhi.helpers.DynamicLocalizationHelper.toLocalizedString
import org.piramalswasthya.sakhi.model.VaccineDomain
import org.piramalswasthya.sakhi.model.toVaccineType


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

            binding.tvVaccineName.text = item.vaccineName.toVaccineType()
                .toLocalizedString(binding.root.context)
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
                            binding.root.context.getString(R.string.immunization_cant_done_as_missed),
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