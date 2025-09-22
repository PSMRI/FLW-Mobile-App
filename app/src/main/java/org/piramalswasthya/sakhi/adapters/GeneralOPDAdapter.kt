package org.piramalswasthya.sakhi.adapters
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvGeneralOpdBenBinding
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.GeneralOPEDBeneficiary

class GeneralOPDAdapter(
    private val clickListener: CallClickListener? = null,
    private val showBeneficiaries: Boolean = false,
    private val showRegistrationDate: Boolean = false,
    private val showSyncIcon: Boolean = false,
    private val showAbha: Boolean = false,
    private val role: Int? = 0
) :
    ListAdapter<GeneralOPEDBeneficiary, GeneralOPDAdapter.BenViewHolder>(BenDiffUtilCallBack) {
    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<GeneralOPEDBeneficiary>() {
        override fun areItemsTheSame(
            oldItem: GeneralOPEDBeneficiary, newItem: GeneralOPEDBeneficiary
        ) = oldItem.beneficiaryId == newItem.beneficiaryId

        override fun areContentsTheSame(
            oldItem: GeneralOPEDBeneficiary, newItem: GeneralOPEDBeneficiary
        ) = oldItem == newItem

    }

    class BenViewHolder private constructor(private val binding: RvGeneralOpdBenBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): BenViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvGeneralOpdBenBinding.inflate(layoutInflater, parent, false)
                return BenViewHolder(binding)
            }
        }

        fun bind(
            item: GeneralOPEDBeneficiary,
            clickListener: CallClickListener?,
            showAbha: Boolean,
            showSyncIcon: Boolean,
            showRegistrationDate: Boolean,
            showBeneficiaries: Boolean, role: Int?
        ) {
//            if (!showSyncIcon) item.syncState = null
            binding.ben = item
            binding.callclickListener = clickListener
            binding.showAbha = showAbha
            binding.showRegistrationDate = showRegistrationDate
            binding.registrationDate.visibility =
                if (showRegistrationDate) View.VISIBLE else View.INVISIBLE
//            binding.hasAbha = !item.abhaId.isNullOrEmpty()
            binding.role = role

            if (showBeneficiaries) {
                if (item.spouseName == "Not Available" && item.fatherName == "Not Available") {
                    binding.father = true
                    binding.husband = false
                    binding.spouse = false
                } else {
                    if (item.genderName == "MALE") {
                        binding.father = true
                        binding.husband = false
                        binding.spouse = false
                    } else if (item.genderName == "FEMALE") {
                        if (item.ben_age_val!! > 15) {
                            binding.father =
                                item.fatherName != "Not Available" && item.spouseName == "Not Available"
                            binding.husband = item.spouseName != "Not Available"
                            binding.spouse = false
                        } else {
                            binding.father = true
                            binding.husband = false
                            binding.spouse = false
                        }
                    } else {
                        binding.father =
                            item.fatherName != "Not Available" && item.spouseName == "Not Available"
                        binding.spouse = item.spouseName != "Not Available"
                        binding.husband = false
                    }
                }
            } else {
                binding.father = false
                binding.husband = false
                binding.spouse = false
            }
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


    class CallClickListener(
        private val clickedCall: (benId: Long, mobileno: String) -> Unit,
    ) {

        fun onClickCall(item: GeneralOPEDBeneficiary) = clickedCall(item.beneficiaryId,
            item.preferredPhoneNum.toString()
        )
    }

}