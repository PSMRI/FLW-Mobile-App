package org.piramalswasthya.sakhi.ui.home_activity.disease_control.general_checkup.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.RvItemGeneralCheckupBinding
import org.piramalswasthya.sakhi.helpers.getLocalizedAge
import org.piramalswasthya.sakhi.model.BenBasicDomain

class GeneralCheckupListAdapter(
    private val onAddCheckup: (BenBasicDomain) -> Unit
) : ListAdapter<GeneralCheckupListAdapter.Item, GeneralCheckupListAdapter.ViewHolder>(DiffCallback) {

    data class Item(val ben: BenBasicDomain, val checkupCount: Int)

    private object DiffCallback : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item) = oldItem.ben.benId == newItem.ben.benId
        override fun areContentsTheSame(oldItem: Item, newItem: Item) = oldItem == newItem
    }

    class ViewHolder(private val binding: RvItemGeneralCheckupBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Item, onAddCheckup: (BenBasicDomain) -> Unit) {
            val ben = item.ben
            val context = binding.root.context
            binding.tvBenName.text = "${ben.benName} ${ben.benSurname.orEmpty()}".trim()
            val showHusband = ben.gender == "FEMALE" && ben.ageInt > 15 &&
                    !ben.spouseName.isNullOrBlank() && ben.spouseName != NOT_AVAILABLE
            binding.tvRelationLabel.setText(
                if (showHusband) R.string.tv_husband_name_ph else R.string.tv_father_name_ph
            )
            binding.tvRelationName.text =
                (if (showHusband) ben.spouseName else ben.fatherName)?.uppercase()
            binding.tvAge.text = getLocalizedAge(context, ben.dob)
            binding.tvGender.text = ben.gender
            binding.tvBenId.text = ben.benId.toString()
            binding.tvMobile.text = ben.mobileNo
            binding.tvCheckupCount.text = item.checkupCount.toString()
            binding.btnAddCheckup.setOnClickListener { onAddCheckup(ben) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        RvItemGeneralCheckupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onAddCheckup)
    }

    companion object {
        private const val NOT_AVAILABLE = "Not Available"
    }
}
