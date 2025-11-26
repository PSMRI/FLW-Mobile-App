package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_eligible_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.filterBenList
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import javax.inject.Inject

@HiltViewModel
class NcdEligibleListViewModel @Inject constructor(
    recordsRepo: RecordsRepo,
    preferenceDao: PreferenceDao
) : ViewModel(

) {

    private lateinit var asha: User
    var clickedPosition = 0

    private val selectedCategory = MutableStateFlow("ALL")

    private val allBenList = recordsRepo.getNcdEligibleList
    private val filter = MutableStateFlow("")
    private val selectedBenId = MutableStateFlow(0L)

    val benList = combine(allBenList, filter, selectedCategory) { cacheList, filterText, selectedCat ->
        val list = cacheList.map { it.asDomainModel() }
        val benBasicDomainList = list.map { it.ben }
        val filteredBenBasicDomainList = filterBenList(benBasicDomainList, filterText)

        val filteredIds = filteredBenBasicDomainList.map { it.benId }.toSet()

        when (selectedCat) {
            "Screened" -> list.filter { it.savedCbacRecords.isNotEmpty() && (it.ben.benId in filteredIds) }
            "Not Screened" -> list.filter { it.savedCbacRecords.isEmpty() && (it.ben.benId in filteredIds) }
            else -> list.filter { it.ben.benId in filteredIds } // "ALL" or any other text -> show filtered results
        }
    }

    fun setSelectedCategory(cat: String) {
        viewModelScope.launch {
            selectedCategory.emit(cat)
        }
    }


    val ncdDetails = allBenList.combineTransform(selectedBenId) { list, benId ->
        if (benId != 0L) {
            val emitList =
                list.firstOrNull { it.ben.benId == benId }?.savedCbacRecords?.map { it.asDomainModel() }
            if (!emitList.isNullOrEmpty()) emit(emitList.reversed())
        }
    }

    init {
        viewModelScope.launch {
            asha = preferenceDao.getLoggedInUser()!!
        }
    }

    fun filterText(text: String) {
        viewModelScope.launch {
            filter.emit(text)
        }

    }

    fun setSelectedBenId(benId: Long) {
        viewModelScope.launch {
            selectedBenId.emit(benId)
        }
    }

    fun getSelectedBenId(): Long = selectedBenId.value
    fun getAshaId(): Int = asha.userId


    private val catList = ArrayList<String>()

    fun categoryData() : ArrayList<String> {

        catList.clear()
        catList.add("ALL")
        catList.add("Screened")
        catList.add("Not Screened")
        return catList

    }

    private val yearsData = ArrayList<String>()

    fun yearsList() : ArrayList<String> {

        yearsData.clear()
        yearsData.add("Select Years")
        yearsData.add("35 YEARS")
        yearsData.add("40 YEARS")
        yearsData.add("45 YEARS")
        yearsData.add("50 YEARS")
        yearsData.add("55 YEARS")
        yearsData.add("60 YEARS")
        yearsData.add("65 YEARS")
        yearsData.add("70 YEARS")
        yearsData.add("75 YEARS")
        yearsData.add("80 YEARS")
        return yearsData

    }

}