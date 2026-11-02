package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.tpt.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import org.json.JSONObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.helpers.filterBeneficiaries
import org.piramalswasthya.sakhi.model.dynamicEntity.BenWithTbReferralFollowUpDomain
import org.piramalswasthya.sakhi.model.dynamicEntity.TPTFollowUpEntity
import org.piramalswasthya.sakhi.repositories.dynamicRepo.TBReferralFollowUpRepository
import org.piramalswasthya.sakhi.repositories.dynamicRepo.TPTStartRepository
import javax.inject.Inject

@HiltViewModel
class TPTListViewModel @Inject constructor(
    repository: TBReferralFollowUpRepository,
    tptRepository: TPTStartRepository
) : ViewModel() {
    private val filter = MutableStateFlow("")

    private val benTptList = repository.observeRecommendedTpt


    val benList = combine(benTptList, tptRepository.observeAllFollowUps(), filter) { list, histories, filterText ->
        filterBeneficiaries(list, filterText).map { beneficiary ->
            TPTListItem(
                beneficiary = beneficiary,
                followUpHistory = histories.filter {
                    it.benId == beneficiary.ben.benId && effectiveFollowUpNo(it) > 0
                }.sortedBy(::effectiveFollowUpNo)
            )
        }
    }



    fun filterText(text: String) {
        viewModelScope.launch {
            filter.emit(text)
        }
    }
}

private fun effectiveFollowUpNo(visit: TPTFollowUpEntity): Int {
    if (visit.followUpNo > 0) return visit.followUpNo
    val month = runCatching {
        JSONObject(visit.fieldsJson).optString("monthly_follow_up")
    }.getOrDefault("")
    return Regex("Month-(\\d+)", RegexOption.IGNORE_CASE)
        .find(month)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0
}

data class TPTListItem(
    val beneficiary: BenWithTbReferralFollowUpDomain,
    val followUpHistory: List<TPTFollowUpEntity>
) {
    val ben get() = beneficiary.ben
    val tbSuspected get() = beneficiary.tbSuspected
    val referralFollowUps get() = beneficiary.referralFollowUps
}
