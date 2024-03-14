package org.piramalswasthya.sakhi.adapters

//
//class VaccineListAdapter(private val clickListener: VaccineClickListener) :
//    ListAdapter<ImmunizationDetailsDomain, VaccineListAdapter.ImmViewHolder>(BenDiffUtilCallBack) {
//    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<ImmunizationDetailsDomain>() {
//        override fun areItemsTheSame(
//            oldItem: ImmunizationDetailsDomain, newItem: ImmunizationDetailsDomain
//        ) = oldItem.ben.benId == newItem.ben.benId
//
//        override fun areContentsTheSame(
//            oldItem: ImmunizationDetailsDomain, newItem: ImmunizationDetailsDomain
//        ) = oldItem == newItem
//
//    }
//
//    class ImmViewHolder private constructor(private val binding: RvItemImmunizationBenBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//        companion object {
//            fun from(parent: ViewGroup): ImmViewHolder {
//                val layoutInflater = LayoutInflater.from(parent.context)
//                val binding = RvItemImmunizationBenBinding.inflate(layoutInflater, parent, false)
//                return ImmViewHolder(binding)
//            }
//        }
//
//        fun bind(
//            item: ImmunizationDetailsDomain,
//            clickListener: VaccineClickListener,
////            clickListener: BenClickListener?
//        ) {
//            binding.temp = item.ben
//            binding.clickListener = clickListener
//            binding.executePendingBindings()
//
//        }
//    }
//
//    override fun onCreateViewHolder(
//        parent: ViewGroup, viewType: Int
//    ) = ImmViewHolder.from(parent)
//
//    override fun onBindViewHolder(holder: ImmViewHolder, position: Int) {
//        holder.bind(getItem(position), clickListener)
//    }
//
//
////    class BenClickListener(
////        private val clickedBen: (hhId: Long, benId : Long, isKid : Boolean) -> Unit,
////        private val clickedHousehold: (hhId: Long) -> Unit
////    ) {
////        fun onClickedVisit(item: BenBasicDomain) = clickedBen(item.hhId, item.benId, item.typeOfList== TypeOfList.CHILD.name || item.typeOfList == TypeOfList.INFANT.name || item.typeOfList== TypeOfList.ADOLESCENT.name)
////        fun onClickedHouseHold(item: BenBasicDomain) = clickedHousehold(item.hhId)
////    }
//
//}