package org.piramalswasthya.sakhi.ui.home_activity.child_care.child_list.hbyc

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.transform
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.HbycIcon
import org.piramalswasthya.sakhi.repositories.HbycRepo
import javax.inject.Inject

@HiltViewModel
class HbycMonthListViewModel @Inject constructor(
    hbycRepo: HbycRepo,
    state: SavedStateHandle,

    ) : ViewModel() {

    private val benId = HbycMonthListFragmentArgs.fromSavedStateHandle(state).benId
    private val hhId = HbycMonthListFragmentArgs.fromSavedStateHandle(state).hhId

    val dayList = hbycRepo.hbycList(benId, hhId).transform { dateList ->
        val daysList = dateList.map { it.month }
        val syncList = dateList.map { it.syncState }

        val headerList = mutableListOf(3, 6, 9, 12, 15).map { month ->
            val monthCache = dateList.filter { it.month == month.toString() }
            val syncState = if (monthCache.isNotEmpty()) monthCache[0].syncState else SyncState.UNSYNCED
            HbycIcon(
                hhId = hhId,
                benId = benId,
                count = month,
                isFilled = daysList.contains(month.toString()),
                syncState = syncState,
                destination = HbycMonthListFragmentDirections.actionHbycMonthListFragmentToHbycFragment(
                    hhId, benId, month
                )
            )
        }
        emit(headerList)
    }

}