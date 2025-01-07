package org.piramalswasthya.sakhi.ui.abha_id_activity.create_abha_id

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.BenHealthIdDetails
import org.piramalswasthya.sakhi.network.AbhaVerifyAadhaarOtpRequest
import org.piramalswasthya.sakhi.network.AuthData
import org.piramalswasthya.sakhi.network.Consent
import org.piramalswasthya.sakhi.network.CreateAbhaIdResponse
import org.piramalswasthya.sakhi.network.CreateHIDResponse
import org.piramalswasthya.sakhi.network.CreateHealthIdRequest
import org.piramalswasthya.sakhi.network.GenerateOtpHid
import org.piramalswasthya.sakhi.network.MapHIDtoBeneficiary
import org.piramalswasthya.sakhi.network.NetworkResult
import org.piramalswasthya.sakhi.network.Otp
import org.piramalswasthya.sakhi.network.ValidateOtpHid
import org.piramalswasthya.sakhi.repositories.AbhaIdRepo
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.ui.abha_id_activity.aadhaar_otp.AadhaarOtpViewModel.State
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CreateAbhaViewModel @Inject constructor(
    private val pref: PreferenceDao,
    private val abhaIdRepo: AbhaIdRepo,
    private val benRepo: BenRepo,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    enum class State {
        IDLE, LOADING, ERROR_NETWORK, ERROR_SERVER, ERROR_INTERNAL, DOWNLOAD_SUCCESS, ABHA_GENERATE_SUCCESS, OTP_GENERATE_SUCCESS, OTP_VERIFY_SUCCESS, DOWNLOAD_ERROR
    }

    private val _state = MutableLiveData<State>()
    val state: LiveData<State>
        get() = _state

    var abha = MutableLiveData<CreateAbhaIdResponse?>(null)

    var hidResponse = MutableLiveData<CreateHIDResponse?>(null)

    private val _benMapped = MutableLiveData<String?>(null)
    val benMapped: LiveData<String?>
        get() = _benMapped

    private val txnId =
        CreateAbhaFragmentArgs.fromSavedStateHandle(savedStateHandle).txnId

    private val hID =
        CreateAbhaFragmentArgs.fromSavedStateHandle(savedStateHandle).phrAddress

    private val healthIdNumber =
        CreateAbhaFragmentArgs.fromSavedStateHandle(savedStateHandle).abhaNumber

    val otpTxnID = MutableLiveData<String?>(null)

    val cardBase64 = MutableLiveData<String>(null)

    val byteImage = MutableLiveData<ResponseBody>(null)

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?>
        get() = _errorMessage

    init {
        _state.value = State.LOADING
    }

    fun printAbhaCard() {
        viewModelScope.launch {
            val result = abhaIdRepo.printAbhaCard()
            when (result) {
                is NetworkResult.Success -> {
                    byteImage.value = result.data
                    _state.value = State.ABHA_GENERATE_SUCCESS
                }

                is NetworkResult.Error -> {
                    _errorMessage.value = result.message
                    _state.value = State.ERROR_SERVER
                }

                is NetworkResult.NetworkError -> {
                    Timber.i(result.toString())
                    _state.value = State.ERROR_NETWORK
                }
            }
        }
    }

    fun mapBeneficiaryToHealthId(benId: Long, benRegId: Long) {
        viewModelScope.launch {
            if ((benId != 0L) or (benRegId != 0L)) {
                mapBeneficiary(
                    benId,
                    if (benRegId != 0L) benRegId else null,
                    hID,
                    healthIdNumber
                )
            } else {
                _state.value = State.ABHA_GENERATE_SUCCESS
            }
        }
    }

    fun createHID(benId: Long, benRegId: Long) {
        viewModelScope.launch {
            when (val result =
                abhaIdRepo.createHealthIdWithUid(
                    CreateHealthIdRequest(
                        "", txnId, "", "", "", "", "",
                        "", "", "", "", "", "",
                        "", "", 0, "", pref.getLoggedInUser()?.serviceMapId, ""
                    )
                )) {
                is NetworkResult.Success -> {
                    hidResponse.value = result.data
                    Timber.d("mapping abha to beneficiary with id $benId")
                    if ((benId != 0L) or (benRegId != 0L)) {
                        mapBeneficiary(
                            benId,
                            if (benRegId != 0L) benRegId else null,
                            result.data.hID.toString(),
                            result.data.healthIdNumber
                        )
                    } else {
                        _state.value = State.ABHA_GENERATE_SUCCESS
                    }
                }

                is NetworkResult.Error -> {
                    _errorMessage.value = result.message
                    _state.value = State.ERROR_SERVER
                }

                is NetworkResult.NetworkError -> {
                    Timber.i(result.toString())
                    _state.value = State.ERROR_NETWORK
                }
            }
        }
    }

    fun resetState() {
        _state.value = State.IDLE
    }

    fun resetErrorMessage() {
        _errorMessage.value = null
    }

    private suspend fun mapBeneficiary(
        benId: Long,
        benRegId: Long?,
        healthId: String,
        healthIdNumber: String?
    ) {
        val ben = benRepo.getBenFromId(benId)

        val req = MapHIDtoBeneficiary(benRegId, benId, healthId, healthIdNumber, 34, "")

        viewModelScope.launch {
            when (val result =
                abhaIdRepo.mapHealthIDToBeneficiary(req)) {
                is NetworkResult.Success -> {
                    ben?.let {
                        ben.firstName?.let { firstName ->
                            _benMapped.value = firstName
                        }
                        ben.lastName?.let { lastName ->
                            _benMapped.value = ben.firstName + " $lastName"
                        }
                        it.healthIdDetails = BenHealthIdDetails(healthId, healthIdNumber)
                        benRepo.updateRecord(ben)
                    }
                    _state.value = State.ABHA_GENERATE_SUCCESS
                }

                is NetworkResult.Error -> {
                    _errorMessage.value = result.message
                    _state.value = State.ERROR_SERVER
                }

                is NetworkResult.NetworkError -> {
                    Timber.i(result.toString())
                    _state.value = State.ERROR_NETWORK
                }
            }
        }
    }

    fun generateOtp() {
        viewModelScope.launch {
            when (val result =
                abhaIdRepo.generateOtpHid(
                    GenerateOtpHid(
                        "AADHAAR_OTP", hidResponse.value?.healthId,
                        hidResponse.value?.healthIdNumber
                    )
                )) {
                is NetworkResult.Success -> {
                    otpTxnID.value = result.data
                    _state.value = State.OTP_GENERATE_SUCCESS
                }

                is NetworkResult.Error -> {
                    if (result.code == 0) {
                        _errorMessage.value = result.message
                        _state.value = State.DOWNLOAD_ERROR
                    } else {
                        _errorMessage.value = result.message
                        _state.value = State.ERROR_SERVER
                    }
                }

                is NetworkResult.NetworkError -> {
                    Timber.i(result.toString())
                    _state.value = State.ERROR_NETWORK
                }
            }
        }
    }

    fun verifyOtp(otp: String?) {
        _state.value = State.LOADING
        viewModelScope.launch {
            when (val result =
                abhaIdRepo.verifyOtpAndGenerateHealthCard(
                    ValidateOtpHid(
                        otp,
                        otpTxnID.value,
                        "AADHAAR_OTP"
                    )
                )) {
                is NetworkResult.Success -> {
                    cardBase64.value = result.data
                    _state.value = State.OTP_VERIFY_SUCCESS
                }

                is NetworkResult.Error -> {
                    if (result.code == 0) {
                        _errorMessage.value = result.message
                        _state.value = State.DOWNLOAD_ERROR
                    } else {
                        _errorMessage.value = result.message
                        _state.value = State.ERROR_SERVER
                    }
                }

                is NetworkResult.NetworkError -> {
                    Timber.i(result.toString())
                    _state.value = State.ERROR_NETWORK
                }
            }
        }
    }
}