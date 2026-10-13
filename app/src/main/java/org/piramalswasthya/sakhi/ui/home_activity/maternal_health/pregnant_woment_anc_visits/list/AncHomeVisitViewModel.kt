package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
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
            formResponseDao.getVisitsByBenFlow(benId)
                .map { HomeVisitHelper.getSortedHomeVisits(it) }
                .catch { _homeVisits.postValue(emptyList()) }
                .collect { _homeVisits.postValue(it) }
        }
    }

    suspend fun getNextVisitNumber(benId: Long): Int {
        return viewModelScope.run {
            val formResponses = formResponseDao.getSyncedVisitsByRchId(benId)
            HomeVisitHelper.getNextVisitNumber(formResponses)
        }
    }
}
