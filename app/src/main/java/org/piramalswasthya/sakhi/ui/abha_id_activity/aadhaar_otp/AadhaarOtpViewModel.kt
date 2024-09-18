package org.piramalswasthya.sakhi.ui.abha_id_activity.aadhaar_otp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.network.AbhaResendAadhaarOtpRequest
import org.piramalswasthya.sakhi.network.AbhaVerifyAadhaarOtpRequest
import org.piramalswasthya.sakhi.network.NetworkResult
import org.piramalswasthya.sakhi.repositories.AbhaIdRepo
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
        OTP_VERIFY_SUCCESS,
        OTP_GENERATED_SUCCESS
    }

    private var txnIdFromArgs = AadhaarOtpFragmentArgs.fromSavedStateHandle(savedStateHandle).txnId
    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State>
        get() = _state

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?>
        get() = _errorMessage

    private val _showExit = MutableLiveData(false)
    val showExit: LiveData<Boolean?>
        get() = _showExit

    private var _txnId: String? = null
    val txnId: String
        get() = _txnId!!

    fun verifyOtpClicked(otp: String) {
        _state.value = State.LOADING
        verifyAadhaarOtp(otp)
    }

    fun resetState() {
        _state.value = State.IDLE
    }

    fun resetErrorMessage() {
        _errorMessage.value = null
    }

    private fun verifyAadhaarOtp(otp: String) {
        viewModelScope.launch {
            val result = abhaIdRepo.verifyOtpForAadhaar(
                AbhaVerifyAadhaarOtpRequest(
                    otp,
                    txnIdFromArgs
                )
            )
            when (result) {
                is NetworkResult.Success -> {
                    _txnId = result.data.txnId
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