package org.piramalswasthya.sakhi.adapters

import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.paging.PagingDataAdapter
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.BenBasicDomain

class BenPagingAdapter(
    private val clickListener: BenListAdapter.BenClickListener? = null,
    private val showBeneficiaries: Boolean = false,
    private val showRegistrationDate: Boolean = false,
    private val showSyncIcon: Boolean = false,
    private val showAbha: Boolean = false,
    private val showCall: Boolean = false,
    private val role: Int? = 0,
    private val pref: PreferenceDao? = null,
    var context: FragmentActivity,
    private val isSoftDeleteEnabled: Boolean = false,
) :
    PagingDataAdapter<BenBasicDomain, BenListAdapter.BenViewHolder>(BenListAdapter.BenDiffUtilCallBack) {

    private val benIds = mutableListOf<Long>()
    private val childCountMap = mutableMapOf<Long, Int>()

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = BenListAdapter.BenViewHolder.from(parent)

    override fun onBindViewHolder(holder: BenListAdapter.BenViewHolder, position: Int) {
        val item = getItem(position) ?: return
        holder.bind(
            item,
            clickListener,
            showAbha,
            showSyncIcon,
            showRegistrationDate,
            showBeneficiaries,
            role,
            showCall,
            isSoftDeleteEnabled,
            pref,
            context,
            benIds,
            childCountMap
        )
    }

    fun submitBenIds(list: List<Long>) {
        val oldIds = benIds.toSet()
        benIds.clear()
        benIds.addAll(list)
        val newIds = benIds.toSet()
        val changed = (oldIds - newIds) + (newIds - oldIds)
        if (changed.isNotEmpty()) {
            val items = snapshot()
            items.forEachIndexed { index, item ->
                if (item != null && item.benId in changed) {
                    notifyItemChanged(index)
                }
            }
        }
    }

    fun submitChildCounts(map: Map<Long, Int>) {
        val old = childCountMap.toMap()
        childCountMap.clear()
        childCountMap.putAll(map)
        val changed = map.entries.filter { (k, v) -> old[k] != v }.map { it.key }.toSet() +
            old.entries.filter { (k, v) -> map[k] != v }.map { it.key }.toSet()
        if (changed.isNotEmpty()) {
            val items = snapshot()
            items.forEachIndexed { index, item ->
                if (item != null && item.benId in changed) {
                    notifyItemChanged(index)
                }
            }
        }
    }
}
