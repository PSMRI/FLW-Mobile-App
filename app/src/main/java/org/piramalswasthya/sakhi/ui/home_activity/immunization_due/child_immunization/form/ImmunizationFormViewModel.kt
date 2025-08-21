package org.piramalswasthya.sakhi.ui.home_activity.immunization_due.child_immunization.form

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.configuration.ImmunizationDataset
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.ImmunizationDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.ImmunizationCache
import org.piramalswasthya.sakhi.model.ImmunizationCategory
import org.piramalswasthya.sakhi.model.ImmunizationDetailsDomain
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.model.Vaccine
import org.piramalswasthya.sakhi.model.VaccineDomain
import org.piramalswasthya.sakhi.model.VaccineState
import timber.log.Timber
import java.util.ArrayList
import java.util.Arrays
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ImmunizationFormViewModel @Inject constructor(
    @ApplicationContext context: Context,
    preferenceDao: PreferenceDao,
    savedStateHandle: SavedStateHandle,
    private val vaccineDao: ImmunizationDao,
    benDao: BenDao,
) : ViewModel() {

    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }

    var vaccinationDoneList = arrayListOf<VaccineDomain>()
    lateinit var list: List<VaccineDomain>

    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State>
        get() = _state

    private val benId = ImmunizationFormFragmentArgs.fromSavedStateHandle(savedStateHandle).benId
    private val vaccineId =
        ImmunizationFormFragmentArgs.fromSavedStateHandle(savedStateHandle).vaccineId

    val vaccineCategory =
        ImmunizationFormFragmentArgs.fromSavedStateHandle(savedStateHandle).category ?: ""

    private val _recordExists = MutableLiveData<Boolean>()
    val recordExists: LiveData<Boolean>
        get() = _recordExists

    private val _benName = MutableLiveData<String>()
    val benName: LiveData<String>
        get() = _benName
    private val _benAgeGender = MutableLiveData<String>()
    val benAgeGender: LiveData<String>
        get() = _benAgeGender

    private val _benRegCache = MutableLiveData<BenRegCache>()
    val benRegCache: LiveData<BenRegCache>
        get() = _benRegCache

    private val dataset = ImmunizationDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow
    private lateinit var immCache: ImmunizationCache
    private  var immCacheArray = mutableListOf<ImmunizationCache>()
    private lateinit var asha: User

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                asha = preferenceDao.getLoggedInUser()!!
                val savedRecord = vaccineDao.getImmunizationRecord(benId, vaccineId)
                immCache = savedRecord?.also { _recordExists.postValue(true) } ?: run {
                    ImmunizationCache(
                        beneficiaryId = benId,
                        vaccineId = vaccineId,
                        createdBy = asha.userName,
                        updatedBy = asha.userName,
                        syncState = SyncState.UNSYNCED
                    )
                }.also { _recordExists.postValue(false) }
                val ben = benDao.getBen(benId)!!
                clickedBenId.emit(benId)
                _benRegCache.postValue(ben)
                _benName.postValue("${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}")
                _benAgeGender.postValue("${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}")
                val vaccine = vaccineDao.getVaccineById(vaccineId)
                    ?: throw IllegalStateException("Unknown Vaccine Injected, contact HAZMAT team!")
                dataset.setFirstPage(ben, vaccine, savedRecord)
            }
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                vaccinesList = vaccineDao.getVaccinesForCategory(ImmunizationCategory.CHILD)
            }
        }
    }

    fun saveForm() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    _state.postValue(State.SAVING)
                    dataset.mapValues(immCache, 1)
                    vaccineDao.addImmunizationRecord(immCache)
                    _state.postValue(State.SAVE_SUCCESS)
                } catch (e: Exception) {
                    Timber.d("saving PW-ANC data failed!!")
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
    }

    fun saveImmunization() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    _state.postValue(State.SAVING)
                    vaccinationDoneList.forEach { item->
                        val savedRecord = vaccineDao.getImmunizationRecord(benId, item.vaccineId)
                        immCache = savedRecord?.also { _recordExists.postValue(true) } ?: run {
                            ImmunizationCache(
                                beneficiaryId = benId,
                                vaccineId = item.vaccineId,
                                createdBy = asha.userName,
                                updatedBy = asha.userName,
                                syncState = SyncState.UNSYNCED
                            )
                        }.also { _recordExists.postValue(false) }

                        dataset.mapValues(immCache, 1)
                        immCacheArray.add(immCache)
                    }
                     vaccineDao.insertImmunizationRecord(immCacheArray)
                    _state.postValue(State.SAVE_SUCCESS)
                } catch (e: Exception) {
                    Timber.d("saving PW-ANC data failed!!")
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
    }

    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch {
            dataset.updateList(formId, index)
        }
    }

    fun updateRecordExists(b: Boolean) {
        _recordExists.value = b

    }

    //========================================================================================================================
    private val clickedBenId = MutableStateFlow(0L)

    private val pastRecords = vaccineDao.getBenWithImmunizationRecords(
        minDob = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.YEAR, -16)
        }.timeInMillis,
        maxDob = System.currentTimeMillis(),
    )
    lateinit var vaccinesList: List<Vaccine>

    val benWithVaccineDetails = pastRecords.map { vaccineIdList ->
        vaccineIdList.map { cache ->
            val ageMillis = System.currentTimeMillis() - cache.ben.dob
            ImmunizationDetailsDomain(ben = cache.ben.asBasicDomainModel(),
                vaccineStateList = vaccinesList.filter {
                    it.minAllowedAgeInMillis < ageMillis
                }.map { vaccine ->
                    VaccineDomain(
                        vaccine.vaccineId,
                        vaccine.vaccineName,
                        vaccine.immunizationService,
                        if (cache.givenVaccines.any { it.vaccineId == vaccine.vaccineId }) VaccineState.DONE
                        else if (ageMillis <= (vaccine.minAllowedAgeInMillis)) {
                            VaccineState.PENDING
                        } else if (ageMillis <= (vaccine.maxAllowedAgeInMillis)) {
                            VaccineState.OVERDUE
                        } else VaccineState.MISSED
                    )
                })
        }
    }
    val bottomSheetContent = clickedBenId.combine(benWithVaccineDetails) { a, b ->
        b.firstOrNull { it.ben.benId == a }
    }


}