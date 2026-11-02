package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.tb_suspected.form

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.piramalswasthya.sakhi.configuration.SuspectedTBDataset
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.TBSuspectedCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.TBRepo
import org.piramalswasthya.sakhi.repositories.dynamicRepo.TBReferralFollowUpRepository
import org.piramalswasthya.sakhi.model.dynamicEntity.TBReferralFollowUpEntity
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TBSuspectedViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val tbRepo: TBRepo,
    private val benRepo: BenRepo,
    private val tbReferralFollowUpRepository: TBReferralFollowUpRepository
) : ViewModel() {
    val benId =
        TBSuspectedFragmentArgs.fromSavedStateHandle(savedStateHandle).benId

    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }

    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State>
        get() = _state

    private val _benName = MutableLiveData<String>()
    val benName: LiveData<String>
        get() = _benName
    private val _benAgeGender = MutableLiveData<String>()
    val benAgeGender: LiveData<String>
        get() = _benAgeGender

    private val _recordExists = MutableLiveData<Boolean>()
    val recordExists: LiveData<Boolean>
        get() = _recordExists

    private val _visit2Complete = MutableLiveData(false)
    val visit2Complete: LiveData<Boolean>
        get() = _visit2Complete

    //    private lateinit var user: UserDomain
    private val dataset =
        SuspectedTBDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow

    private lateinit var tbSuspected: TBSuspectedCache
    private var householdId: Long = 0L

    init {
        viewModelScope.launch {
            val ben = benRepo.getBenFromId(benId)?.also { ben ->
                householdId = ben.householdId
                _benName.value =
                    "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
                _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"
                tbSuspected = TBSuspectedCache(
                    benId = ben.beneficiaryId,
                )
            }

            val savedSuspected = tbRepo.getTBSuspected(benId)
            savedSuspected?.let {
                tbSuspected = it
                _recordExists.value = true
                _visit2Complete.value = !it.sputumTestResult.isNullOrBlank() &&
                    it.isTBConfirmed != null &&
                    (it.isTBConfirmed == true || it.isRecommendedForTpt != null)
            } ?: run {
                _recordExists.value = false
                _visit2Complete.value = false
            }

            dataset.setUpPage(
                ben,
                if (recordExists.value == true) tbSuspected else null
            )

        }
    }

    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch {
            dataset.updateList(formId, index)
        }

    }

    fun saveForm() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    _state.postValue(State.SAVING)
                    appendVisitHistory()
                    dataset.mapValues(tbSuspected, 1)
                    tbSuspected.syncState = SyncState.UNSYNCED
                    tbRepo.saveTBSuspected(tbSuspected)
                    val status = when {
                        tbSuspected.isTBConfirmed == true -> "TB_CONFIRMED"
                        tbSuspected.isRecommendedForTpt == true -> "RECOMMEND_TPT"
                        else -> "NOT_RECOMMENDED"
                    }
                    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                        .format(java.util.Date())
                    tbReferralFollowUpRepository.save(
                        TBReferralFollowUpEntity(
                            benId = benId,
                            houseHoldId = householdId,
                            referredOnDate = today,
                            followUpDate = today,
                            followUpStatus = status,
                            fieldsJson = JSONObject().apply {
                                put("follow_up_status", status)
                                put("visit_label", tbSuspected.visitLabel)
                                put("is_tb_confirmed", tbSuspected.isTBConfirmed)
                                put("recommended_for_tpt", tbSuspected.isRecommendedForTpt)
                            }.toString()
                        )
                    )
                    _state.postValue(State.SAVE_SUCCESS)
                } catch (e: Exception) {
                    Timber.d("saving TB Suspected data failed due to $e")
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
    }

    fun resetState() {
        _state.value = State.IDLE
    }

    fun getAlerts(): String? {
        return dataset.isTestPositive()
    }

    private fun appendVisitHistory() {
        if (tbSuspected.id == 0) return
        val history = runCatching { JSONArray(tbSuspected.followUps) }.getOrElse {
            JSONArray().apply {
                tbSuspected.followUps?.takeIf { it.isNotBlank() }?.let { put(JSONObject().put("legacyFollowUps", it)) }
            }
        }
        history.put(JSONObject().apply {
            put("visitLabel", tbSuspected.visitLabel ?: "Visit 1")
            put("visitDate", tbSuspected.visitDate)
            put("typeOfTBCase", tbSuspected.typeOfTBCase)
            put("isSputumCollected", tbSuspected.isSputumCollected)
            put("sputumSubmittedAt", tbSuspected.sputumSubmittedAt)
            put("nikshayId", tbSuspected.nikshayId)
            put("sputumTestResult", tbSuspected.sputumTestResult)
            put("isTBConfirmed", tbSuspected.isTBConfirmed)
            put("isRecommendedForTpt", tbSuspected.isRecommendedForTpt)
        })
        tbSuspected.followUps = history.toString()
    }

}

