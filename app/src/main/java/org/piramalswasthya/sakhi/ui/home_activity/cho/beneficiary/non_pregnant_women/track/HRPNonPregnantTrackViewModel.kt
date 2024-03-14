package org.piramalswasthya.sakhi.ui.home_activity.cho.beneficiary.non_pregnant_women.track

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
import org.piramalswasthya.sakhi.configuration.HRPNonPregnantTrackDataset
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.HRPNonPregnantTrackCache
import org.piramalswasthya.sakhi.model.HRPPregnantAssessCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.HRPRepo
import timber.log.Timber

@HiltViewModel
class HRPNonPregnantTrackViewModel @javax.inject.Inject
constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val hrpReo: HRPRepo,
    private val benRepo: BenRepo
) : ViewModel() {
    val benId =
        HRPNonPregnantTrackFragmentArgs.fromSavedStateHandle(savedStateHandle).benId

    val trackId =
        HRPNonPregnantTrackFragmentArgs.fromSavedStateHandle(savedStateHandle).trackId

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

    //    private lateinit var user: UserDomain
    private val dataset =
        HRPNonPregnantTrackDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow

    var isHighRisk: Boolean = false

    private lateinit var hrpNonPregnantTrackCache: HRPNonPregnantTrackCache

    private var ben: BenRegCache? = null

    init {
        viewModelScope.launch {
            ben = benRepo.getBenFromId(benId)?.also { ben ->
                _benName.value =
                    "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
                _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"
                hrpNonPregnantTrackCache = HRPNonPregnantTrackCache(
                    benId = ben.beneficiaryId,
                )
            }

            hrpReo.getHRPNonTrack(trackId = trackId.toLong())?.let {
                if (trackId > 0) {
                    hrpNonPregnantTrackCache = it
                    _recordExists.value = true
                }

            } ?: run {
                _recordExists.value = false
            }

            val hist = hrpReo.getMaxLmp(benId)

            dataset.setUpPage(
                ben,
                if (recordExists.value == true) hrpNonPregnantTrackCache else null,
                hrpReo.getMaxLmp(benId),
                hrpReo.getMaxDoVNonHrp(benId)
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

                    dataset.mapValues(hrpNonPregnantTrackCache, 1)
                    Timber.d("non track data " + hrpNonPregnantTrackCache.severeAnemia)
                    hrpReo.saveRecord(hrpNonPregnantTrackCache)
                    if (hrpNonPregnantTrackCache.isPregnant.contentEquals("Yes")) {
                        ben?.let {
                            dataset.updateBen(it)
                            benRepo.updateRecord(it)
                        }


                        var pregAssessCache =
                            hrpReo.getPregnantAssess(hrpNonPregnantTrackCache.benId)
                        val nonPregAssessCache =
                            hrpReo.getNonPregnantAssess(hrpNonPregnantTrackCache.benId)
                        if (pregAssessCache == null) {
                            pregAssessCache = if (nonPregAssessCache == null) {
                                HRPPregnantAssessCache(
                                    benId = hrpNonPregnantTrackCache.benId
                                )
                            } else {
                                HRPPregnantAssessCache(
                                    benId = hrpNonPregnantTrackCache.benId,
                                    noOfDeliveries = nonPregAssessCache.noOfDeliveries,
                                    timeLessThan18m = nonPregAssessCache.timeLessThan18m,
                                    heightShort = nonPregAssessCache.heightShort,
                                    age = nonPregAssessCache.age,
                                    isHighRisk = (
                                            nonPregAssessCache.noOfDeliveries == "Yes" ||
                                                    nonPregAssessCache.timeLessThan18m == "Yes" ||
                                                    nonPregAssessCache.heightShort == "Yes" ||
                                                    nonPregAssessCache.age == "Yes"
                                            )
                                )
                            }
                        }

                        dataset.updateAssess(pregAssessCache)
                        hrpReo.saveRecord(pregAssessCache)
                    }

                    _state.postValue(State.SAVE_SUCCESS)
                } catch (e: Exception) {
                    Timber.d("saving hrp non tracking data failed!!")
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
    }

    fun resetState() {
        _state.value = State.IDLE
    }

    fun getIndexOfAncLabel() = dataset.getIndexOfAncLabel()

    fun getIndexOfAnemia() = dataset.getIndexOfAnemia()

    fun getIndexOfRisk() = dataset.getIndexOfRisk()

    fun getIndexOfLmp() = dataset.getIndexOfLmp()
    fun getIndexOfRbg() = dataset.getIndexOfRbg()
    fun getIndexOfFbg() = dataset.getIndexOfFbg()
    fun getIndexOfPpbg() = dataset.getIndexOfPpbg()
    fun getIndexOfIfaQuantity() = dataset.getIndexOfIfaQuantity()
}