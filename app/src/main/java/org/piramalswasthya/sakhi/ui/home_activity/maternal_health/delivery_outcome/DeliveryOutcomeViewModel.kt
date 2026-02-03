package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.delivery_outcome

import android.content.Context
<<<<<<< Updated upstream
=======
import android.net.Uri
>>>>>>> Stashed changes
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
import org.piramalswasthya.sakhi.configuration.DeliveryOutcomeDataset
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.DeliveryOutcomeCache
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.PregnantWomanAncCache
import org.piramalswasthya.sakhi.model.PregnantWomanRegistrationCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.DeliveryOutcomeRepo
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.delivery_outcome.DeliveryOutcomeFragmentArgs
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DeliveryOutcomeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val deliveryOutcomeRepo: DeliveryOutcomeRepo,
    private val maternalHealthRepo: MaternalHealthRepo,
    private val benRepo: BenRepo
) : ViewModel() {
    val benId =
        DeliveryOutcomeFragmentArgs.fromSavedStateHandle(savedStateHandle).benId
<<<<<<< Updated upstream

    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
=======
    val hhId =
        DeliveryOutcomeFragmentArgs.fromSavedStateHandle(savedStateHandle).hhId

    sealed class State {
        object IDLE : State()
        object SAVING : State()
        data class SAVE_SUCCESS(val shouldNavigateToMdsr: Boolean) : State()
        object SAVE_FAILED : State()
        object DRAFT_SAVED : State()
>>>>>>> Stashed changes
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

    private val _hasUnsavedChanges = MutableLiveData(false)
    val hasUnsavedChanges: LiveData<Boolean>
        get() = _hasUnsavedChanges

    private val dataset =
        DeliveryOutcomeDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow

    private lateinit var deliveryOutcome: DeliveryOutcomeCache
    private var pwrCache: PregnantWomanRegistrationCache? = null
    private var ancCache: PregnantWomanAncCache? = null

<<<<<<< Updated upstream
=======
    fun getIndexOfMCP1() = dataset.getIndexOfMCP1()
    fun getIndexOfMCP2() = dataset.getIndexOfMCP2()
    fun getIndexOfIsjsyFileUpload() = dataset.getIndexOfIsjsyFileUpload()

    private var lastDocumentFormId: Int = 0
    fun setCurrentDocumentFormId(id: Int) {
        lastDocumentFormId = id
    }

    fun getDocumentFormId(): Int {
        return lastDocumentFormId
    }

    fun setImageUriToFormElement(dpUri: Uri) {
        _hasUnsavedChanges.value = true
        dataset.setImageUriToFormElement(lastDocumentFormId, dpUri)

    }

>>>>>>> Stashed changes
    init {
        viewModelScope.launch {
            val asha = preferenceDao.getLoggedInUser()!!
            val ben = maternalHealthRepo.getBenFromId(benId)?.also { ben ->
                _benName.value =
                    "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
                _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"
                deliveryOutcome = DeliveryOutcomeCache(
                    benId = ben.beneficiaryId,
                    isActive = true,
                    syncState = SyncState.UNSYNCED,
                    createdBy = asha.userName,
<<<<<<< Updated upstream
                    updatedBy = asha.userName,
                    isActive = true
=======
                    updatedBy = asha.userName
>>>>>>> Stashed changes
                )
            }

            deliveryOutcomeRepo.getDeliveryOutcome(benId)?.let {
                deliveryOutcome = it
                _recordExists.value = true
            } ?: run {
                _recordExists.value = false
            }

            pwrCache = maternalHealthRepo.getSavedRegistrationRecord(benId)
            ancCache = maternalHealthRepo.getSavedRecordANC(benId)

            if (pwrCache != null && ancCache != null) {
                dataset.setUpPage(
                    pwrCache!!,
                    ancCache!!,
                    if (recordExists.value == true) deliveryOutcome else null
                )
            }
        }
    }


    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch {
            _hasUnsavedChanges.postValue(true)
            dataset.updateList(formId, index)
        }

    }

    fun saveForm() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    _state.postValue(State.SAVING)
                    dataset.mapValues(deliveryOutcome, 1)
<<<<<<< Updated upstream
                    deliveryOutcomeRepo.saveDeliveryOutcome(deliveryOutcome)

                    val ecr = ecrRepo.getSavedRecord(deliveryOutcome.benId)
                    if (ecr != null) {
                        deliveryOutcome.liveBirth?.let {
                            ecr.noOfLiveChildren = ecr.noOfLiveChildren + it
                        }
                        deliveryOutcome.deliveryOutcome?.let {
                            ecr.noOfChildren = ecr.noOfChildren + it
                        }
                        if (ecr.processed != "N") ecr.processed = "U"
                        ecr.syncState = SyncState.UNSYNCED
                        ecrRepo.persistRecord(ecr)
                    }

                    _state.postValue(State.SAVE_SUCCESS)
=======
                    deliveryOutcome.syncState = SyncState.UNSYNCED
                    deliveryOutcomeRepo.saveDeliveryOutcome(deliveryOutcome)
                    if (deliveryOutcome.isDeath == true) {
                        maternalHealthRepo.getBenFromId(benId)?.let {
                            it.isDeath = true
                            it.isDeathValue = "Death"
                            // it.dateOfDeath = deliveryOutcome.deliveryDate // Need to format this
                            it.reasonOfDeath = "Maternal Death"
                            it.reasonOfDeathId = 0
                            it.placeOfDeath = deliveryOutcome.placeOfDeath
                            it.placeOfDeathId = deliveryOutcome.placeOfDeathId ?: 0

                            if (it.processed != "N") it.processed = "U"
                            it.syncState = SyncState.UNSYNCED
                            benRepo.updateRecord(it)
                        }
                    }
                    if (deliveryOutcome.id != 0L) {
                        maternalHealthRepo.getSavedRegistrationRecord(benId)?.let {
                            it.active = false
                            if (it.processed != "N") it.processed = "U"
                            it.syncState = SyncState.UNSYNCED
                            maternalHealthRepo.persistRegisterRecord(it)
                        }
                        val inactiveAnc = maternalHealthRepo.getAllInActiveAncRecords(benId)
                        inactiveAnc.forEach {
                            it.isActive = false
                            if (it.processed != "N") it.processed = "U"
                            it.syncState = SyncState.UNSYNCED
                        }
                        maternalHealthRepo.updateAncRecord(inactiveAnc.toTypedArray())
                    }
                    _hasUnsavedChanges.postValue(false)
                    _state.postValue(State.SAVE_SUCCESS(deliveryOutcome.isDeath == true))
>>>>>>> Stashed changes
                } catch (e: Exception) {
                    Timber.d("saving delivery outcome data failed!!")
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
    }
<<<<<<< Updated upstream
=======

    fun saveDraft() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    _state.postValue(State.SAVING)
                    dataset.mapValues(deliveryOutcome, 1)
                    deliveryOutcome.syncState = SyncState.DRAFT
                    deliveryOutcomeRepo.saveDeliveryOutcome(deliveryOutcome)
                    _hasUnsavedChanges.postValue(false)
                    _state.postValue(State.DRAFT_SAVED)
                } catch (e: Exception) {
                    Timber.d("saving delivery outcome draft failed!!")
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
    }
>>>>>>> Stashed changes

    fun resetState() {
        _state.value = State.IDLE
    }

}
