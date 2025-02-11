package org.piramalswasthya.sakhi.ui.abha_id_activity.aadhaar_otp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.network.AbhaGenerateAadhaarOtpRequest
import org.piramalswasthya.sakhi.network.AbhaResendAadhaarOtpRequest
import org.piramalswasthya.sakhi.network.AbhaVerifyAadhaarOtpRequest
import org.piramalswasthya.sakhi.network.AuthData
import org.piramalswasthya.sakhi.network.AuthData3
import org.piramalswasthya.sakhi.network.Consent
import org.piramalswasthya.sakhi.network.LoginVerifyOtpRequest
import org.piramalswasthya.sakhi.network.LoginVerifyOtpResponse
import org.piramalswasthya.sakhi.network.NetworkResult
import org.piramalswasthya.sakhi.network.Otp
import org.piramalswasthya.sakhi.network.Otp3
import org.piramalswasthya.sakhi.network.interceptors.TokenInsertAbhaInterceptor
import org.piramalswasthya.sakhi.repositories.AbhaIdRepo
import org.piramalswasthya.sakhi.ui.abha_id_activity.aadhaar_id.AadhaarIdViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AadhaarOtpViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val abhaIdRepo: AbhaIdRepo
) : ViewModel() {

    enum class State {
        IDLE,
        LOADING,
        ERROR_SERVER,
        ERROR_NETWORK,
        SUCCESS,
        OTP_VERIFY_SUCCESS,
        OTP_GENERATED_SUCCESS
    }

    private var txnIdFromArgs = AadhaarOtpFragmentArgs.fromSavedStateHandle(savedStateHandle).txnId
    private var mobileFromArgs = AadhaarOtpFragmentArgs.fromSavedStateHandle(savedStateHandle).mobileNumber
    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State>
        get() = _state

    private val _state2 = MutableLiveData(AadhaarIdViewModel.State.IDLE)
    val state2: LiveData<AadhaarIdViewModel.State>
        get() = _state2

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?>
        get() = _errorMessage

    private val _showExit = MutableLiveData(false)
    val showExit: LiveData<Boolean?>
        get() = _showExit

    private var _txnId: String? = null
    val txnId: String
        get() = _txnId!!

    private var _name: String? = null
    val name: String
        get() = _name!!

    private var _abhaNumber: String? = null
    val abhaNumber: String
        get() = _abhaNumber!!

    private var _phrAddress: String = ""
    val phrAddress: String
        get() = _phrAddress

    private var _mobileNumber: String? = null
    val mobileNumber: String
        get() = _mobileNumber!!

    fun verifyOtpClicked(otp: String, mobile: String) {
        _state.value = State.LOADING
        verifyAadhaarOtp(otp, mobile)
    }

    fun resetState() {
        _state.value = State.IDLE
    }

    fun resetErrorMessage() {
        _errorMessage.value = null
    }

    private fun verifyAadhaarOtp(otp: String, mobile: String) {
        viewModelScope.launch {
            val result = abhaIdRepo.verifyOtpForAadhaar(
                AbhaVerifyAadhaarOtpRequest(
                    AuthData(
                        listOf<String>("otp"),
                        Otp(
                            "",
                            txnIdFromArgs,
                            otp,
                            mobile
                        )
                    ),
                    Consent(
                        "abha-enrollment",
                        "1.4"
                    )
                )
            )
            when (result) {
                is NetworkResult.Success -> {
                    TokenInsertAbhaInterceptor.setXToken(result.data.tokens.token)
                    _txnId = result.data.txnId
                    _mobileNumber = result.data.ABHAProfile.mobile
                    if (result.data.ABHAProfile.middleName.isNotEmpty()) {
                        _name = result.data.ABHAProfile.firstName + " " + result.data.ABHAProfile.middleName + " " + result.data.ABHAProfile.lastName
                    } else {
                        _name = result.data.ABHAProfile.firstName + " " + result.data.ABHAProfile.lastName
                    }
                    _phrAddress = result.data.ABHAProfile.phrAddress[0]
                    _abhaNumber = result.data.ABHAProfile.ABHANumber
                    _state.value = State.OTP_VERIFY_SUCCESS
                }

                is NetworkResult.Error -> {
                    _errorMessage.value = result.message
                    if (result.message.contains("exit your browser", true)) {
                        _showExit.value = true
                    }
                    _state.value = State.ERROR_SERVER
                }

                is NetworkResult.NetworkError -> {
                    _showExit.value = true
                    _state.value = State.ERROR_NETWORK
                }
            }
        }
    }

    fun verifyLoginOtpClicked(otp: String) {
        _state.value = State.LOADING
        verifyLoginOtp(otp)
    }

    private fun verifyLoginOtp(otp: String) {
        viewModelScope.launch {
            val result = abhaIdRepo.verifyAbhaOtp(
                LoginVerifyOtpRequest(
                    listOf<String>("abha-login", "mobile-verify"),
                    AuthData3(
                        listOf<String>("otp"),
                        Otp3(
                            txnIdFromArgs,
                            otp
                        )
                    )
                )
            )
            when (result) {
                is NetworkResult.Success -> {
                    TokenInsertAbhaInterceptor.setXToken(result.data.token)
                    _txnId = result.data.txnId
                    _name = result.data.accounts[0].name
                    _abhaNumber = result.data.accounts[0].ABHANumber
                    _state.value = State.OTP_VERIFY_SUCCESS
                    _phrAddress = result.data.accounts[0].preferredAbhaAddress
                    _mobileNumber = ""
                }

                is NetworkResult.Error -> {
                    _errorMessage.value = result.message
                    if (result.message.contains("exit your browser", true)) {
                        _showExit.value = true
                    }
                    _state.value = State.ERROR_SERVER
                }

                is NetworkResult.NetworkError -> {
                    _showExit.value = true
                    _state.value = State.ERROR_NETWORK
                }
            }
        }
    }

    fun generateOtpClicked() {
        _state.value = State.LOADING
        generateAadhaarOtp()
    }

    private fun generateAadhaarOtp() {
        viewModelScope.launch {
            when (val result =
//                abhaIdRepo.generateOtpForAadhaarV2(AbhaGenerateAadhaarOtpRequest(aadhaarNo))) {
                abhaIdRepo.generateOtpForAadhaarV2(
                    AbhaGenerateAadhaarOtpRequest(
                        txnId,
                        listOf<String>("abha-enrol", "mobile-verify"),
                        "mobile",
                        mobileFromArgs,
                        "abdm"
                    )
                )) {
                is NetworkResult.Success -> {
                    _txnId = result.data.txnId
                    _state.value = State.SUCCESS
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

    fun generateOtpClicked(aadhaarNo: String) {
        _state2.value = AadhaarIdViewModel.State.LOADING
    }

    fun resendOtp() {
        _state.value = State.LOADING
        viewModelScope.launch {
            when (val result =
                abhaIdRepo.resendOtpForAadhaar(AbhaResendAadhaarOtpRequest(txnIdFromArgs))) {
                is NetworkResult.Success -> {
                    txnIdFromArgs = result.data.txnId
                    _state.value = State.OTP_GENERATED_SUCCESS
                }

                is NetworkResult.Error -> {
                    _errorMessage.value = result.message
                    if (result.message.contains("exit", true)) {
                        _showExit.value = true
                    }
                    _state.value = State.ERROR_SERVER
                }

                is NetworkResult.NetworkError -> {
                    _state.value = State.ERROR_NETWORK
                }
            }
        }
    }

}