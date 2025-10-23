package org.piramalswasthya.sakhi.ui.home_activity.child_care.children_under_five_years.children_forms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.helpers.filterBenList
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import javax.inject.Inject


@HiltViewModel
class CUFYFormCardViewModel @Inject constructor(
    recordsRepo: RecordsRepo
) : ViewModel() {

    private val allBenList = recordsRepo.childCard
    private val filter = MutableStateFlow("")
    private val kind = MutableStateFlow("false")

    val benList = allBenList.combine(kind) { list, kind ->
        if (kind.equals("false", true)) {
            filterBenList(list, false)
        } else {
            filterBenList(list, true)
        }
    }.combine(filter) { list, filter ->
        filterBenList(list, filter)
    }

    fun getDobByBenIdAsync(benId: Long, onResult: (Long?) -> Unit) {
        viewModelScope.launch {
            allBenList.collect { list ->
                val dob = list.find { it.benId == benId }?.dob
                onResult(dob)
                return@collect
            }
        }
    }
//    fun getBenById(benId: Long, onResult: (BenBasicDomain?) -> Unit) {
//        viewModelScope.launch {
//            allBenList.collect { list ->
//                val ben = list.find { it.benId == benId }
//                onResult(ben)
//                return@collect
//            }
//        }
//    }

    fun getBenById(benId: Long, onResult: (BenBasicDomain?) -> Unit) {
        viewModelScope.launch {
            val list = allBenList.firstOrNull() ?: emptyList()
            val ben = list.find { it.benId == benId }
            onResult(ben)
        }
    }


    fun filterText(text: String) {
        viewModelScope.launch {
            filter.emit(text)
        }

    }

    fun filterType(type: String) {
        viewModelScope.launch {
            kind.emit(type)
        }

    }

}