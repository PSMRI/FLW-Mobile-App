package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.tb_confirmed.from

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
import org.piramalswasthya.sakhi.configuration.SuspectedTBDataset
import org.piramalswasthya.sakhi.configuration.TBConfirmedDataset
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.LeprosyFollowUpCache
import org.piramalswasthya.sakhi.model.TBConfirmedTreatmentCache
import org.piramalswasthya.sakhi.model.TBSuspectedCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.TBRepo
import org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.tb_suspected.form.TBSuspectedFragmentArgs
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TBConfirmedViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val tbRepo: TBRepo,
    private val benRepo: BenRepo
) : ViewModel() {

    val benId =
        TBConfirmedFormFragmentArgs.fromSavedStateHandle(savedStateHandle).benId

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

    private val _followUpDates = MutableLiveData<List<TBConfirmedTreatmentCache>>()
    val followUpDates: LiveData<List<TBConfirmedTreatmentCache>>
        get() = _followUpDates

    private val dataset =
        TBConfirmedDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow

    private lateinit var tbSuspected: TBSuspectedCache
    private var tbConfirmedTreatmentCache: TBConfirmedTreatmentCache? = null

    init {
        viewModelScope.launch {
            val ben = benRepo.getBenFromId(benId)?.also { ben ->
                _benName.value =
                    "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
                _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"
                tbSuspected = TBSuspectedCache(
                    benId = ben.beneficiaryId
                )
                tbConfirmedTreatmentCache = TBConfirmedTreatmentCache(
                    benId = ben.beneficiaryId
                )
            }


            tbRepo.getTBSuspected(benId)?.let {
                tbSuspected = it
            }

            tbRepo.getTBConfirmed(benId)?.let {
                tbConfirmedTreatmentCache = it
                _recordExists.value = true
            }?: run{
                tbConfirmedTreatmentCache = null
                _recordExists.value = false


        }

            val allFollowUps = tbRepo.getAllFollowUpsForBeneficiary(benId)
            _followUpDates.value = allFollowUps
            dataset.setUpPage(
                ben,
                tbConfirmedTreatmentCache,tbSuspected
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

                    val isValid = validateForm()
                    if (!isValid) {
                        _state.postValue(State.SAVE_FAILED)
                        return@withContext
                    }

                        tbConfirmedTreatmentCache = TBConfirmedTreatmentCache(
                            benId = benId,
                        )

                    dataset.mapValues(tbConfirmedTreatmentCache!!, 1)
                    if (tbConfirmedTreatmentCache!!.treatmentOutcome.equals( "Death")) {
                        benRepo.getBenFromId(benId)?.let {
                            it.isDeath = true
                            it.isDeathValue = "Death"
                            it.dateOfDeath = longToDateString(tbConfirmedTreatmentCache!!.dateOfDeath)
                            it.reasonOfDeath = tbConfirmedTreatmentCache!!.reasonForDeath
                            it.placeOfDeath = tbConfirmedTreatmentCache!!.placeOfDeath


                            if (it.processed != "N") it.processed = "U"
                            it.syncState = SyncState.UNSYNCED
                            benRepo.updateRecord(it)
                        }
                    }
                    tbRepo.saveTBConfirmed(tbConfirmedTreatmentCache!!)
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

    fun longToDateString(dateMillis: Long?): String {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        return dateMillis?.let { millis ->
            dateFormat.format(Date(millis))
        } ?: ""
    }

    private suspend fun validateForm(): Boolean {
        return withContext(Dispatchers.Default) {
            dataset.validateAllFields()
        }
    }

}