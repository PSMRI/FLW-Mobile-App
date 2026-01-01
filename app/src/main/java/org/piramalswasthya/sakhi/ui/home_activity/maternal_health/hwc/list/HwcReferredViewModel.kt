package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.hwc.list

import android.content.Context
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
class HwcReferredViewModel @Inject constructor(
    recordsRepo: RecordsRepo,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
) : ViewModel(){

    private lateinit var asha: User

    private val allBenList = recordsRepo.getHwcRefferedList
    private val filter = MutableStateFlow("")
    private val selectedBenId = MutableStateFlow(0L)
    var userName = preferenceDao.getLoggedInUser()!!.name

    val benList = allBenList.combine(filter) { cacheList, filter ->
        val maternalList = cacheList
        .filter { it.referral.type == "MATERNAL" }
            .map { it.asDomainModel() }

        val benBasicDomainList = maternalList.map { it.ben }
        val filteredBenBasicDomainList = filterBenList(benBasicDomainList, filter)

        maternalList.filter { it.ben.benId in filteredBenBasicDomainList.map { it.benId } }
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

}