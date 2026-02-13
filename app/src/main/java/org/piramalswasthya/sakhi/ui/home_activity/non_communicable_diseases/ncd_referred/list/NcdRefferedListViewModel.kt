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

    val benList = allBenList.combine(filter) { cacheList, filter ->
        val list = cacheList.map { it.asDomainModel() }
        val benBasicDomainList = list.map { it.ben }
        val filteredBenBasicDomainList = filterBenList(benBasicDomainList, filter)
        list.filter { it.ben.benId in filteredBenBasicDomainList.map { it.benId } }



    }
    var selectedPosition = 0
    val selectedFilter=MutableLiveData<String?>("ALL")

    private val clickedBenId = MutableStateFlow(0L)

    fun updateBottomSheetData(benId: Long) {
        viewModelScope.launch {
            clickedBenId.emit(benId)
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


        return catList

    }

}