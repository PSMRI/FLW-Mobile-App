package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.form

import android.content.Context
import android.net.Uri
import android.util.Log
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
import org.piramalswasthya.sakhi.configuration.PregnantWomanAncVisitDataset
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.PregnantWomanAncCache
import org.piramalswasthya.sakhi.model.PregnantWomanRegistrationCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PwAncFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val maternalHealthRepo: MaternalHealthRepo,
    private val benRepo: BenRepo
) : ViewModel() {

    sealed class State {
        object IDLE : State()
        object SAVING : State()
        data class SAVE_SUCCESS(val shouldNavigateToMdsr: Boolean) : State()
        object SAVE_FAILED : State()
    }

    private var lastDocumentFormId: Int = 0

    val benId = PwAncFormFragmentArgs.fromSavedStateHandle(savedStateHandle).benId
    val hhID = PwAncFormFragmentArgs.fromSavedStateHandle(savedStateHandle).hhId.toString()
    private val visitNumber =
        PwAncFormFragmentArgs.fromSavedStateHandle(savedStateHandle).visitNumber
    val lastItemClick =
        PwAncFormFragmentArgs.fromSavedStateHandle(savedStateHandle).lastItemClick


    private val _state = MutableLiveData<State>(State.IDLE)
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

    private val dataset =
        PregnantWomanAncVisitDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow

    private lateinit var ancCache: PregnantWomanAncCache
    private lateinit var registerRecord: PregnantWomanRegistrationCache

    fun getIndexOfMCPCardFront() = dataset.getIndexOfMCPCardFrontPath()
    fun getIndexOfMCPCardBack() = dataset.getIndexOfMCPCardBackPath()

    init {
        viewModelScope.launch {
            val asha = preferenceDao.getLoggedInUser()!!
            val ben = maternalHealthRepo.getBenFromId(benId)?.also { ben ->
                _benName.value =
                    "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
                _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"
                ancCache = PregnantWomanAncCache(
                    benId = ben.beneficiaryId,
                    visitNumber = visitNumber,
                    syncState = SyncState.UNSYNCED,
                    createdBy = asha.userName,
                    updatedBy = asha.userName,
                    frontFilePath = "",
                    backFilePath = ""
                )
            }
            registerRecord = maternalHealthRepo.getSavedRegistrationRecord(benId)!!
            maternalHealthRepo.getSavedAncRecord(benId, visitNumber)?.let {
                ancCache = it
                _recordExists.value = true
            } ?: run {
                _recordExists.value = false
            }
            val lastAnc = maternalHealthRepo.getSavedAncRecord(benId, visitNumber - 1)

            dataset.setUpPage(
                visitNumber,
                ben,
                registerRecord,
                lastAnc,
                if (recordExists.value == true) ancCache else null
            )


        }
    }

    fun setCurrentDocumentFormId(id: Int) {
        lastDocumentFormId = id
    }
    fun getDocumentFormId():Int {
        return lastDocumentFormId
    }

    fun setImageUriToFormElement(dpUri: Uri) {
        dataset.setImageUriToFormElement(lastDocumentFormId, dpUri)


    }
    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch {
            dataset.updateList(formId, index)
        }

    }

    companion object {
        private const val TAG = "ANCFormViewModel"
    }

    fun saveForm() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    _state.postValue(State.SAVING)

                    dataset.mapValues(ancCache, 1)

                    maternalHealthRepo.persistAncRecord(ancCache)

                    if (registerRecord.syncState == SyncState.UNSYNCED) {
                        maternalHealthRepo.persistRegisterRecord(registerRecord)
                    }

                    if (ancCache.pregnantWomanDelivered == true) {
                        maternalHealthRepo.getBenFromId(benId)?.let {
                            dataset.updateBenRecordToDelivered(it)
                            benRepo.updateRecord(it)
                        }
                    }

                    if (ancCache.isAborted) {
                        maternalHealthRepo.getSavedRegistrationRecord(benId)?.let {
                            it.active = false
                            if (it.processed != "N") it.processed = "U"
                            it.syncState = SyncState.UNSYNCED
                            maternalHealthRepo.persistRegisterRecord(it)

                            maternalHealthRepo.getBenFromId(benId)?.let {
                                it.genDetails?.reproductiveStatus = "Eligible Couple"
                                it.genDetails?.reproductiveStatusId =1
                                if (it.processed != "N") it.processed = "U"
                                it.syncState = SyncState.UNSYNCED
                                benRepo.updateRecord(it)
                            }
                        }

                        maternalHealthRepo.getAllActiveAncRecords(benId).apply {
                            forEach {
                                it.isActive = false
                                if (it.processed != "N") it.processed = "U"
                                it.syncState = SyncState.UNSYNCED
                            }
                            maternalHealthRepo.updateAncRecord(toTypedArray())
                        }

                        maternalHealthRepo.getBenFromId(benId)?.let {
                            dataset.updateBenRecordToEligibleCouple(it)
                            benRepo.updateRecord(it)
                        }
                    }

                    val shouldNavigateToMdsr = ancCache.maternalDeath ?: false

                    if (ancCache.maternalDeath == true) {
                        maternalHealthRepo.getSavedRegistrationRecord(benId)?.let {
                            it.active = false
                            if (it.processed != "N") it.processed = "U"
                            it.syncState = SyncState.UNSYNCED
                            maternalHealthRepo.persistRegisterRecord(it)
                        }

                        maternalHealthRepo.getBenFromId(benId)?.let {
                            it.isDeath = true
                            it.isDeathValue = "Death"
                            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                            val dateOfDeath = ancCache.deathDate?.let { d ->
                                dateFormat.format(Date(d))
                            } ?: ""
                            it.dateOfDeath = dateOfDeath
                            it.reasonOfDeath = ancCache.maternalDeathProbableCause
                            it.reasonOfDeathId = ancCache.maternalDeathProbableCauseId
                            it.placeOfDeath = ancCache.placeOfDeath
                            it.placeOfDeathId = ancCache.placeOfDeathId ?: -1
                            it.otherPlaceOfDeath = ancCache.otherPlaceOfDeath
                            if (it.processed != "N") it.processed = "U"
                            it.syncState = SyncState.UNSYNCED
                            benRepo.updateRecord(it)
                        }

                        maternalHealthRepo.getAllActiveAncRecords(benId).apply {
                            forEach {
                                it.isActive = false
                                if (it.processed != "N") it.processed = "U"
                                it.syncState = SyncState.UNSYNCED
                            }
                            maternalHealthRepo.updateAncRecord(toTypedArray())
                        }
                    }

                    _state.postValue(State.SAVE_SUCCESS(shouldNavigateToMdsr))
                } catch (e: Exception) {
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
    }


    fun setRecordExist(b: Boolean) {
        _recordExists.value = b

    }

    fun getIndexOfWeeksOfPregnancy(): Int = dataset.getWeeksOfPregnancy()
    fun getIndexOfTT1(): Int = dataset.getIndexOfTd1()
    fun getIndexOfTT2(): Int = dataset.getIndexOfTd2()
    fun getIndexOfTTBooster(): Int = dataset.getIndexOfTdBooster()


}