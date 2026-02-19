package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_referred.form

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import javax.inject.Inject

@HiltViewModel
class NCDReferDialogViewModel @Inject constructor(
    var preferenceDao: PreferenceDao,
    var cbacRepo: CbacRepo,
    var benDao: BenDao,
    var referalRepo: NcdReferalRepo,
     @ApplicationContext var context: Context,
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

    lateinit var dataset : ReferalFormDataset
//    val formList = dataset.listFlow
    val formList get() = dataset.listFlow


    lateinit var referalCache: ReferalCache
    private val _gender = MutableLiveData<Gender>()
    val gender: LiveData<Gender>
        get() = _gender
    private val _age = MutableLiveData<Int>()
    val age: LiveData<Int>
        get() = _age

     var benId: Long = 0
     var referralReason = ""
    var cbacId: Long = 0
     var referralType = ""


    fun initFromArgs(
        benId: Long,
        referralReason: String,
        cbacId: Long,
        referralType: String
    ) {

        this.benId = benId
        this.referralReason = referralReason
        this.cbacId = cbacId
        this.referralType = referralType
        dataset = ReferalFormDataset(
            context,
            preferenceDao.getCurrentLanguage(),
            preferenceDao
        )

        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val fetchedBen = benDao.getBen(benId)
               if (fetchedBen == null) {
                   _state.postValue(State.SAVE_FAILED)
                   return@launch
               }
            ben = fetchedBen
            _gender.value = ben.gender!!
            _age.value = ben.age
            _benName.value = "${ben.firstName} ${ben.lastName.orEmpty()}"
            _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"

            referalCache = ReferalCache(
                benId = benId,
                id = 0,
                syncState = SyncState.UNSYNCED,
                createdBy = preferenceDao.getLoggedInUser()?.userName
            )
            dataset.setUpPage(referralReason, referralType)

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