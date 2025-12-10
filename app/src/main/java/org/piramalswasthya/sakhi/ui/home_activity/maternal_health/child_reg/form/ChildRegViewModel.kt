package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.child_reg.form

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
import org.piramalswasthya.sakhi.configuration.ChildRegistrationDataset
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.EligibleCoupleRegCache
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.InfantRegCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.ChildRegRepo
import org.piramalswasthya.sakhi.repositories.EcrRepo
import org.piramalswasthya.sakhi.repositories.InfantRegRepo
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ChildRegViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val childRegRepo: ChildRegRepo,
    private val infantRegRepo: InfantRegRepo,
    private val benRepo: BenRepo,
    private val ecrRepo: EcrRepo
) : ViewModel() {
    val motherBenId =
        ChildRegFragmentArgs.fromSavedStateHandle(savedStateHandle).motherBenId
    val babyIndex =
        ChildRegFragmentArgs.fromSavedStateHandle(savedStateHandle).babyIndex
    private var lastDocumentFormId: Int = 0

    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }

    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State>
        get() = _state

    private val _recordExists = MutableLiveData(false)
    val recordExists: LiveData<Boolean>
        get() = _recordExists

    private lateinit var infantReg: InfantRegCache

    fun getIndexOfBirthCertificateFront() = dataset.getIndexOfBirthCertificateFrontPath()
    fun getIndexOfBirthCertificateBack() = dataset.getIndexOfBirthCertificateBackPath()

    private val dataset =
        ChildRegistrationDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow

    init {
        viewModelScope.launch {
            val ben = benRepo.getBenFromId(motherBenId)
            val deliveryOutcome =
                childRegRepo.getDeliveryOutcomeRepoFromMotherBenId(motherBenId = motherBenId)
            infantReg = childRegRepo.getInfantRegFromMotherBenId(
                motherBenId = motherBenId,
                babyIndex = babyIndex
            )!!
            dataset.setUpPage(
                motherBen = ben,
                deliveryOutcomeCache = deliveryOutcome,
                infantRegCache = infantReg,
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

    fun saveForm() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    _state.postValue(State.SAVING)
                    val motherBen = benRepo.getBenFromId(motherBenId)!!
                    val childBen = dataset.mapAsBeneficiary(
                        motherBen,
                        preferenceDao.getLoggedInUser()!!,
                        preferenceDao.getLocationRecord()!!
                    )
                    benRepo.substituteBenIdForDraft(childBen)
                    benRepo.persistRecord(childBen)
                    infantReg.childBenId = childBen.beneficiaryId
                    infantRegRepo.update(infantReg)

                    var existingEcr = ecrRepo.getSavedRecord(motherBenId)
                    val isNew = existingEcr == null

                    if (isNew) {
                        existingEcr = EligibleCoupleRegCache(
                            benId = motherBenId,
                            createdBy = preferenceDao.getLoggedInUser()!!.userName,
                            updatedBy = preferenceDao.getLoggedInUser()!!.userName,
                            syncState = SyncState.UNSYNCED,
                            lmp_date = Calendar.getInstance().timeInMillis,

                            )
                    }

                    val nextSlot = when {
                        existingEcr!!.dob1 == null -> 1
                        existingEcr.dob2 == null -> 2
                        existingEcr.dob3 == null -> 3
                        existingEcr.dob4 == null -> 4
                        existingEcr.dob5 == null -> 5
                        existingEcr.dob6 == null -> 6
                        existingEcr.dob7 == null -> 7
                        existingEcr.dob8 == null -> 8
                        existingEcr.dob9 == null -> 9
                        else -> 9 // Maximum 9 children allowed
                    }

                    // 5️⃣ Insert child into next available slot
                    when (nextSlot) {
                        1 -> { existingEcr.dob1 = childBen.dob; existingEcr.gender1 = childBen.gender; existingEcr.age1 = childBen.age }
                        2 -> { existingEcr.dob2 = childBen.dob; existingEcr.gender2 = childBen.gender; existingEcr.age2 = childBen.age }
                        3 -> { existingEcr.dob3 = childBen.dob; existingEcr.gender3 = childBen.gender; existingEcr.age3 = childBen.age }
                        4 -> { existingEcr.dob4 = childBen.dob; existingEcr.gender4 = childBen.gender; existingEcr.age4 = childBen.age }
                        5 -> { existingEcr.dob5 = childBen.dob; existingEcr.gender5 = childBen.gender; existingEcr.age5 = childBen.age }
                        6 -> { existingEcr.dob6 = childBen.dob; existingEcr.gender6 = childBen.gender; existingEcr.age6 = childBen.age }
                        7 -> { existingEcr.dob7 = childBen.dob; existingEcr.gender7 = childBen.gender; existingEcr.age7 = childBen.age }
                        8 -> { existingEcr.dob8 = childBen.dob; existingEcr.gender8 = childBen.gender; existingEcr.age8 = childBen.age }
                        9 -> { existingEcr.dob9 = childBen.dob; existingEcr.gender9 = childBen.gender; existingEcr.age9 = childBen.age }
                    }

                   /* existingEcr.noOfChildren = nextSlot
                    existingEcr.noOfLiveChildren++
                    if (ben.gender == Gender.MALE) existingEcr.noOfMaleChildren++
                    if (ben.gender == Gender.FEMALE) existingEcr.noOfFemaleChildren++*/

                    existingEcr.updatedBy = preferenceDao.getLoggedInUser()!!.userName
                    existingEcr.syncState = SyncState.UNSYNCED


                    ecrRepo.persistRecord(existingEcr)

                    _state.postValue(State.SAVE_SUCCESS)
                } catch (e: Exception) {
                    Timber.d("saving child registration data failed!!")
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
    }

    fun resetState() {
        _state.value = State.IDLE
    }

}
