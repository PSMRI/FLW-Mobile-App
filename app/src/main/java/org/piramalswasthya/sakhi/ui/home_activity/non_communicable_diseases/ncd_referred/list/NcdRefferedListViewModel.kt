package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_referred.list

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.filterBenList
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import javax.inject.Inject

@HiltViewModel
class NcdRefferedListViewModel @Inject constructor(
    recordsRepo: RecordsRepo,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
) : ViewModel() {

    private lateinit var asha: User

    private val allBenList = recordsRepo.getNcdrefferedList
    private val filter = MutableStateFlow("")
    private val selectedBenId = MutableStateFlow(0L)
    var userName = preferenceDao.getLoggedInUser()!!.name
    val selectedFilter = MutableStateFlow<String?>("ALL")

    val benList = combine(
        allBenList,
        filter,
        selectedFilter
    ) { cacheList, searchText, selectedType ->

        val typeFiltered = if (selectedType == "ALL") {
            cacheList
        } else {
            cacheList.filter { it.referral.type == selectedType }
        }

        val domainList = typeFiltered.map { it.asDomainModel() }
        val benList = domainList.map { it.ben }
        val searchedBenList = filterBenList(benList, searchText)
        domainList.filter { domain ->
            searchedBenList.any { it.benId == domain.ben.benId }
        }
    }
    var selectedPosition = 0

    private val clickedBenId = MutableStateFlow(0L)

    fun updateBottomSheetData(benId: Long) {
        viewModelScope.launch {
            clickedBenId.emit(benId)
        }
    }

    fun setSelectedFilter(type: String) {
        viewModelScope.launch {
            selectedFilter.emit(type)
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
        catList.add("NCD")
        catList.add("TB")
        catList.add("LEPROSY")
        catList.add("GERIATRIC")
        catList.add("HRP")
        catList.add("MATERNAL")


        return catList

    }

}