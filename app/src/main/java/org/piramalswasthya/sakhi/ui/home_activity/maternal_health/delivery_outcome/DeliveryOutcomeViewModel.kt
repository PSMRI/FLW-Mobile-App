package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.delivery_outcome

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
import org.piramalswasthya.sakhi.configuration.DeliveryOutcomeDataset
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.DeliveryOutcomeCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.DeliveryOutcomeRepo
import org.piramalswasthya.sakhi.repositories.EcrRepo
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DeliveryOutcomeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val deliveryOutcomeRepo: DeliveryOutcomeRepo,
    private val ecrRepo: EcrRepo,
    private val pwrRepo: MaternalHealthRepo,
    private val benRepo: BenRepo
) : ViewModel() {
    val benId =
        DeliveryOutcomeFragmentArgs.fromSavedStateHandle(savedStateHandle).benId
   val hhId = DeliveryOutcomeFragmentArgs.fromSavedStateHandle(savedStateHandle).hhId

    sealed class State {
        object IDLE : State()
        object SAVING : State()
        data class SAVE_SUCCESS(val shouldNavigateToMdsr: Boolean) : State()
        object SAVE_FAILED : State()
    }

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
        DeliveryOutcomeDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow

    private lateinit var deliveryOutcome: DeliveryOutcomeCache

    fun getIndexOfMCP1() = dataset.getIndexOfMCP1()

    fun getIndexOfMCP2() = dataset.getIndexOfMCP2()
    fun getIndexOfIsjsyFileUpload() = dataset.getIndexOfIsjsyFileUpload()

    private var lastDocumentFormId: Int = 0
    fun setCurrentDocumentFormId(id: Int) {
        lastDocumentFormId = id
    }
    fun getDocumentFormId():Int {
        return lastDocumentFormId
    }

    fun setImageUriToFormElement(dpUri: Uri) {
        dataset.setImageUriToFormElement(lastDocumentFormId, dpUri)

    }


    init {
        viewModelScope.launch {
            val asha = preferenceDao.getLoggedInUser()!!
            val pwr = pwrRepo.getLatestActiveRegistrationRecord(benId)!!
            val anc = pwrRepo.getLatestAncRecord(benId)!!
            benRepo.getBenFromId(benId)?.also { ben ->
                _benName.value =
                    "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
                _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"
                deliveryOutcome = DeliveryOutcomeCache(
                    benId = ben.beneficiaryId,
                    syncState = SyncState.UNSYNCED,
                    createdBy = asha.userName,
                    updatedBy = asha.userName,
                    isActive = true,



                )
            }

            deliveryOutcomeRepo.getDeliveryOutcome(benId)?.let {
                deliveryOutcome = it
                _recordExists.value = true
            } ?: run {
                _recordExists.value = false
            }

            dataset.setUpPage(
                pwr, anc,
                if (recordExists.value == true) deliveryOutcome else null
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

                dataset.mapValues(deliveryOutcome, 1)

                deliveryOutcomeRepo.saveDeliveryOutcome(deliveryOutcome)

                val ecr = ecrRepo.getSavedRecord(deliveryOutcome.benId)
                val shouldNavigateToMdsr = deliveryOutcome.complication?.equals("Death", ignoreCase = true) == true
                if (deliveryOutcome.complication.equals("Death", ignoreCase = true)) {
                    benRepo.getBenFromId(benId)?.let {
                        it.isDeath = true
                        it.isDeathValue = "Death"

                        it.dateOfDeath = deliveryOutcome.dateOfDeath
                        it.reasonOfDeath = deliveryOutcome.causeOfDeath
                        it.placeOfDeath = deliveryOutcome.placeOfDeath
                        it.placeOfDeathId = deliveryOutcome.placeOfDeathId ?: -1
                        it.otherPlaceOfDeath = deliveryOutcome.otherPlaceOfDeath

                        if (it.processed != "N") it.processed = "U"
                        it.syncState = SyncState.UNSYNCED

                        benRepo.updateRecord(it)
                    }
                }

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

                _state.postValue(State.SAVE_SUCCESS(shouldNavigateToMdsr))
            } catch (e: Exception) {
                _state.postValue(State.SAVE_FAILED)
            }
        }
    }
}



    fun resetState() {
        _state.value = State.IDLE
    }

}
