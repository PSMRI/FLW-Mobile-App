package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.hwc.form

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
import org.piramalswasthya.sakhi.configuration.dynamicDataSet.ReferalFormDataset
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.ReferalCache
import org.piramalswasthya.sakhi.repositories.CbacRepo
import org.piramalswasthya.sakhi.repositories.NcdReferalRepo
import org.piramalswasthya.sakhi.repositories.SaasBahuSammelanRepo
import org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_referred.form.NCDReferalFormFragmentArgs
import javax.inject.Inject

@HiltViewModel
class HwcReferredFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    var cbacRepo: CbacRepo,
    var benDao: BenDao,
    var referalRepo: NcdReferalRepo,
    var saasBahuSammelanRepo: SaasBahuSammelanRepo,
    @ApplicationContext context: Context,
) : ViewModel() {


    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }



    private lateinit var ben: BenRegCache



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

    private val dataset =
        ReferalFormDataset(context, preferenceDao.getCurrentLanguage(),preferenceDao)
    val formList = dataset.listFlow

    lateinit var referalCache: ReferalCache
    private val _gender = MutableLiveData<Gender>()
    val gender: LiveData<Gender>
        get() = _gender
    private val _age = MutableLiveData<Int>()
    val age: LiveData<Int>
        get() = _age

    val benId = NCDReferalFormFragmentArgs.fromSavedStateHandle(savedStateHandle).benId
    val cbacId = NCDReferalFormFragmentArgs.fromSavedStateHandle(savedStateHandle).cbacId
    init {
        viewModelScope.launch {
            ben = benDao.getBen(benId)!!
            ben.gender?.let { _gender.value = it }
            _age.value = ben.age
            _benName.value = "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
            _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"
            _recordExists.value = false
            referalCache = ReferalCache(
                benId = benId,
                id = 0,
                syncState = SyncState.UNSYNCED,
                createdBy = preferenceDao.getLoggedInUser()?.userName
            )

          //  dataset.setUpPage()
            /*
                        cbac = if (cbacId > 0)
                            cbacRepo.getCbacCacheFromId(cbacId).also { _filledCbac.postValue(it) }
                        else
                            CbacCache(
                                benId = benId, ashaId = ashaId,
                                syncState = SyncState.UNSYNCED,
                                createdDate = System.currentTimeMillis()
                            )
            */


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

                    dataset.mapValues(referalCache)
//                    referalRepo.saveReferedNCD(referalCache)
                    _state.postValue(State.SAVE_SUCCESS)
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