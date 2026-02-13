package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormResponseANCJsonDao
import org.piramalswasthya.sakhi.model.HomeVisitDomain
import org.piramalswasthya.sakhi.utils.HomeVisitHelper
import javax.inject.Inject

@HiltViewModel
class AncHomeVisitViewModel @Inject constructor(
    private val formResponseDao: FormResponseANCJsonDao
) : ViewModel() {

    private val _homeVisits = MutableLiveData<List<HomeVisitDomain>>()
    val homeVisits: LiveData<List<HomeVisitDomain>> = _homeVisits

    fun loadHomeVisits(benId: Long) {
        viewModelScope.launch {
            try {
                val formResponses = formResponseDao.getSyncedVisitsByRchId(benId)

                val visits = HomeVisitHelper.getSortedHomeVisits(formResponses)

                _homeVisits.value = visits
            } catch (e: Exception) {
                _homeVisits.value = emptyList()
            }
        }
    }

    suspend fun getNextVisitNumber(benId: Long): Int {
        return viewModelScope.run {
            val formResponses = formResponseDao.getSyncedVisitsByRchId(benId)
            HomeVisitHelper.getNextVisitNumber(formResponses)
        }
    }
}
