package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pnc.form

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri

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
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.configuration.PncFormDataset
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.DeliveryOutcomeCache
import org.piramalswasthya.sakhi.model.PNCVisitCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.DeliveryOutcomeRepo
import org.piramalswasthya.sakhi.repositories.PncRepo
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class PncFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val deliveryOutcomeRepo: DeliveryOutcomeRepo,
    private val pncRepo: PncRepo,
    private val benRepo: BenRepo
) : ViewModel() {


    sealed class State {
        object IDLE : State()
        object SAVING : State()
        data class SAVE_SUCCESS(val shouldNavigateToMdsr: Boolean) : State()
        object SAVE_FAILED : State()
    }


    val benId = PncFormFragmentArgs.fromSavedStateHandle(savedStateHandle).benId
    val hhId = PncFormFragmentArgs.fromSavedStateHandle(savedStateHandle).hhId?.toLong()
    private val visitNumber =
        PncFormFragmentArgs.fromSavedStateHandle(savedStateHandle).visitNumber


    @SuppressLint("StaticFieldLeak")
    private val _context = context
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

    //    private lateinit var user: UserDomain
    private val dataset =
        PncFormDataset(context, preferenceDao.getCurrentLanguage(), this)
    val formList = dataset.listFlow

    private lateinit var pncCache: PNCVisitCache
    var deliveryOutcome: DeliveryOutcomeCache? = null

    private val _showIncentiveAlert = MutableLiveData<Boolean>()
    val showIncentiveAlert: LiveData<Boolean> get() = _showIncentiveAlert

    private val _navigateToMdsr = MutableLiveData<Boolean>()
    val navigateToMdsr: LiveData<Boolean> get() = _navigateToMdsr

    fun triggerIncentiveAlert() {
        _showIncentiveAlert.value = true
    }

    fun incentiveAlertShown() {
        _showIncentiveAlert.value = false
    }

    fun getIndexDeliveryDischargeSummary1() = dataset.getIndexDeliveryDischargeSummary1()
    fun getIndexDeliveryDischargeSummary2() = dataset.getIndexDeliveryDischargeSummary2()
    fun getIndexDeliveryDischargeSummary3() = dataset.getIndexDeliveryDischargeSummary3()
    fun getIndexDeliveryDischargeSummary4() = dataset.getIndexDeliveryDischargeSummary4()

    private var lastDocumentFormId: Int = 0
    fun setCurrentDocumentFormId(id: Int) {
        lastDocumentFormId = id
    }

    fun getDocumentFormId(): Int {
        return lastDocumentFormId
    }

    fun setImageUriToFormElement(dpUri: Uri) {
        dataset.setImageUriToFormElement(lastDocumentFormId, dpUri)

    }

    suspend fun hasPreviousPermanentSterilization(): Boolean {
        return pncRepo.getAllPncVisitsForBeneficiary(benId)
            .filter { it.pncPeriod < visitNumber }
            .any { pncVisit ->
                pncVisit.contraceptionMethod?.let { method ->
                    isPermanentSterilizationMethod(method)
                } ?: false
            }
    }

    suspend fun getLastPermanentSterilizationVisit(
        benId: Long,
        currentVisitNumber: Int
    ): PNCVisitCache? {
        return pncRepo?.getAllPncVisitsForBeneficiary(benId)
            ?.filter { it.pncPeriod < currentVisitNumber }
            ?.filter { pncVisit ->
                pncVisit.contraceptionMethod?.let { method ->
                    isPermanentSterilizationMethod(method)
                } ?: false
            }
            ?.maxByOrNull { it.pncPeriod }
    }

    private fun isPermanentSterilizationMethod(method: String): Boolean {
        val permanentMethods = _context.resources.getStringArray(R.array.sterilization_methods_array).toList()
          /*  listOf(

            "MALE STERILIZATION",
            "FEMALE STERILIZATION",
            "POST PARTUM STERILIZATION (PPS)",
            "Minilap"
        )*/
        return permanentMethods.any { it.equals(method, ignoreCase = true) }
    }

    init {
        viewModelScope.launch {
            val asha = preferenceDao.getLoggedInUser()!!
            val ben = benRepo.getBenFromId(benId)!!.also { ben ->
                _benName.value =
                    "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
                _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"
                pncCache = PNCVisitCache(
                    benId = ben.beneficiaryId,
                    pncPeriod = visitNumber,
                    isActive = true,
                    syncState = SyncState.UNSYNCED,
                    createdBy = asha.userName,
                    updatedBy = asha.userName
                )
            }
            val outcomeRecord = deliveryOutcomeRepo.getDeliveryOutcome(benId)
//            if (outcomeRecord != null) {
            deliveryOutcome = outcomeRecord
            // }
            pncRepo.getSavedPncRecord(benId, visitNumber)?.let {
                pncCache = it
                _recordExists.value = true
            } ?: run {
                _recordExists.value = false
            }
            val lastPnc = pncRepo.getLastFilledPncRecord(benId)
            val hasPreviousSterilization = hasPreviousPermanentSterilization()

            dataset.setUpPage(
                visitNumber,
                ben,
                outcomeRecord,
                lastPnc,
                if (recordExists.value == true) pncCache else null,
                hasPreviousSterilization

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
                    dataset.mapValues(pncCache, 1)
                    pncRepo.persistPncRecord(pncCache)


                    if (deliveryOutcome?.dateOfDelivery == null || deliveryOutcome?.dateOfDelivery == 0L) {
                        val saveDeliveryOutcome = DeliveryOutcomeCache(
                            benId = pncCache.benId,
                            syncState = SyncState.UNSYNCED,
                            createdBy = pncCache.updatedBy,
                            updatedBy = pncCache.updatedBy,
                            dateOfDelivery = pncCache.dateOfDelivery,
                            isActive = true
                        )
                        deliveryOutcomeRepo.saveDeliveryOutcome(saveDeliveryOutcome)
                    }

                    updateWomanStatusAfterPnc(pncCache, benId)

                    val shouldNavigateToMdsr = pncCache.motherDeath

                    if (pncCache.motherDeath) {
                        benRepo.getBenFromId(benId)?.let {
                            it.isDeath = true
                            it.isDeathValue = "Death"
                            it.dateOfDeath = longToDateString(pncCache.deathDate)
                            it.reasonOfDeath = pncCache.causeOfDeath
                            it.placeOfDeath = pncCache.placeOfDeath
                            it.otherPlaceOfDeath = pncCache.otherPlaceOfDeath

                            if (it.processed != "N") it.processed = "U"
                            it.syncState = SyncState.UNSYNCED
                            benRepo.updateRecord(it)
                        }
                    }

                    _state.postValue(State.SAVE_SUCCESS(shouldNavigateToMdsr))
                } catch (e: Exception) {
                    Timber.d("saving PW-ANC data failed!! $e")
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
    }

    fun setRecordExist(b: Boolean) {
        _recordExists.value = b

    }

    fun longToDateString(dateMillis: Long?): String {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        return dateMillis?.let { millis ->
            dateFormat.format(Date(millis))
        } ?: ""
    }

    fun onNavigationComplete() {
        _navigateToMdsr.value = false
    }

    private suspend fun updateWomanStatusAfterPnc(pncCache: PNCVisitCache, benId: Long) {
        val ben = benRepo.getBenFromId(benId) ?: return

        val permanentSterilizationMethods = _context.resources.getStringArray(R.array.female_sterilization_methods_array).toList()


        val is42ndDayPnc = pncCache.pncPeriod == 42
        val daysSinceDelivery = if (pncCache.dateOfDelivery > 0L) {
            TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - pncCache.dateOfDelivery)
        } else 0L

        val isAfter60Days = daysSinceDelivery >= 60


        if (is42ndDayPnc || isAfter60Days) {

            val allPncVisits = pncRepo.getAllPncVisitsForBeneficiary(benId)
            val hasPermanentSterilization = allPncVisits.any { pncVisit ->
                pncVisit.contraceptionMethod?.let { method ->
                    permanentSterilizationMethods.any { sterilizationMethod ->
                        method.contains(sterilizationMethod, ignoreCase = true)
                    }
                } ?: false
            }

            if (hasPermanentSterilization) {
                updateBeneficiaryToPermanentlySterilised(ben)
            } else {
                updateBeneficiaryToEligibleCouple(ben)
            }
        }
    }

    private suspend fun updateBeneficiaryToPermanentlySterilised(ben: BenRegCache) {
        ben.genDetails?.reproductiveStatus = "Permanently Sterilised"
        ben.genDetails?.reproductiveStatusId = 5
        if (ben.processed != "N") ben.processed = "U"
        ben.syncState = SyncState.UNSYNCED
        benRepo.updateRecord(ben)
    }

    private suspend fun updateBeneficiaryToEligibleCouple(ben: BenRegCache) {
        ben.genDetails?.reproductiveStatus = "Eligible Couple"
        ben.genDetails?.reproductiveStatusId = 1
        if (ben.processed != "N") ben.processed = "U"
        ben.syncState = SyncState.UNSYNCED
        benRepo.updateRecord(ben)
    }

}