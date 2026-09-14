package org.piramalswasthya.sakhi.ui.abha_id_activity.aadhaar_id

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.network.CapturePIDRequest
import org.piramalswasthya.sakhi.network.FaceAuthData
import org.piramalswasthya.sakhi.network.FaceBlock
import org.piramalswasthya.sakhi.network.FaceEnrollmentRequest
import org.piramalswasthya.sakhi.network.NetworkResult
import org.piramalswasthya.sakhi.network.interceptors.TokenInsertAbhaInterceptor
import org.piramalswasthya.sakhi.repositories.AbhaIdRepo
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class
AadhaarIdViewModel @Inject constructor(
) : ViewModel() {
    enum class State {
        IDLE,
        LOADING,
        ERROR_SERVER,
        ERROR_NETWORK,
        SUCCESS,
        STATE_DETAILS_SUCCESS,
        ABHA_GENERATED_SUCCESS,
        ABHA_GENERATION_PENDING,
        ABHA_GENERATION_FAILED,
        ABHA_GENERATION_SUCCESS,
        FACE_AUTH_PENDING,
        FACE_AUTH_VERIFIED,
        FACE_AUTH_FAILURE,
        FACE_AUTH_SUCCESS
    }

    enum class Abha {
        NONE,
        CREATE,
        SEARCH
    }

    //    val aadhaarVerificationTypeValues = arrayOf("Aadhaar ID", "Fingerprint")
    val aadhaarVerificationTypeValues = arrayOf("Aadhaar No","Face Authentication")
    private val _aadhaarVerificationTypes = MutableLiveData(aadhaarVerificationTypeValues[0],)
    val aadhaarVerificationTypes: LiveData<String>
        get() = _aadhaarVerificationTypes

    init {
        Timber.d("initialised at ${Date().time}")
    }

    @Inject
    lateinit var abhaIdRepo: AbhaIdRepo


    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State>
        get() = _state

    private var _userType = MutableLiveData("ASHA")
    val userType: LiveData<String>
        get() = _userType

    private var _abhaMode = MutableLiveData(Abha.NONE)
    val abhaMode: LiveData<Abha>
        get() = _abhaMode

    private var _verificationType = MutableLiveData("OTP")
    val verificationType: LiveData<String>
        get() = _verificationType

    private var _name: String? = null
    val name: String
        get() = _name!!

    private var _abhaNumber: String? = null
    val abhaNumber: String
        get() = _abhaNumber!!

    private var _abhaResponse: String = ""
    val abhaResponse: String
        get() = _abhaResponse

    private var _phrAddress: String = ""
    val phrAddress: String
        get() = _phrAddress

    private var _txnId: String? = null
    val txnId: String
        get() = _txnId ?: ""

    private var _otpTxnId: String? = null
    val otpTxnId: String
        get() = _otpTxnId ?: ""

    private var _mobileNumber: String? = null
    val mobileNumber: String
        get() = _mobileNumber ?: ""

    private var _selectedAbhaIndex: String? = null
    val selectedAbhaIndex: String
        get() = _selectedAbhaIndex ?: ""

    private var _aadhaarNumber: String? = null
    val aadhaarNumber: String
        get() = _aadhaarNumber ?: ""

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?>
        get() = _errorMessage

    private val _beneficiaryName = MutableLiveData<String?>(null)
    val beneficiaryName: LiveData<String?>
        get() = _beneficiaryName

    private val _navigateToAadhaarConsent = MutableLiveData<Boolean>(false)
    val navigateToAadhaarConsent: LiveData<Boolean>
        get() = _navigateToAadhaarConsent


    private val _consentChecked = MutableLiveData<Boolean>(false)
    val consentChecked: LiveData<Boolean>
        get() = _consentChecked

    private var _otpMobileNumberMessage: String? = null
    val otpMobileNumberMessage: String
        get() = _otpMobileNumberMessage?:""

    var selectedNavToggle:String = "navHostFragmentAadhaarId"

    fun resetState() {
        _state.value = State.IDLE
    }

    fun resetErrorMessage() {
        _errorMessage.value = null
    }

    fun setAbha(abha: String) {
        _abhaResponse = abha
    }

    fun setBeneficiaryName(name: String) {
        _beneficiaryName.value = name
    }

    fun setConsentChecked(value: Boolean) {
        _consentChecked.value = value
    }

    fun setState(state: State) {
        _state.value = state
    }

    fun setAbhaMode(abha: Abha) {
        _abhaMode.value = abha
    }

    fun navigateToAadhaarConsent(value: Boolean) {
        _navigateToAadhaarConsent.value = value
    }

    fun setMobileNumber(mobileNumber: String) {
        _mobileNumber = mobileNumber
    }

    fun setAadhaarNumber(aadhaarNumber: String) {
        _aadhaarNumber = aadhaarNumber
    }

    fun setSelectedAbhaIndex(abhaIndex: String) {
        _selectedAbhaIndex = abhaIndex
    }

    fun setOtpTxnId(txnId: String) {
        _otpTxnId = txnId
    }

    fun setTxnId(txnId: String) {
        _txnId = txnId
    }
    fun setOTPMsg(msg: String) {
        _otpMobileNumberMessage = msg
    }


    fun setUserType(userType: String) {
        _userType.value = userType
    }

    fun setVerificationType(verificationType: String) {
        _verificationType.value = verificationType
    }



    fun startFaceAuthEnrollment() {
        viewModelScope.launch {
            _state.value = State.LOADING
            when (val result = abhaIdRepo.generateFaceAuthTxn()) {
                is NetworkResult.Success -> {
                    _txnId = result.data.txnId
                    _state.value = State.SUCCESS
                }
                is NetworkResult.Error -> {
                    _errorMessage.value = result.message
                    _state.value = State.ERROR_SERVER
                }
                NetworkResult.NetworkError -> _state.value = State.ERROR_NETWORK
            }
        }
    }

    fun submitCapturedPid() {
        viewModelScope.launch {
            while(isActive) {
                _state.value = State.FACE_AUTH_PENDING
                val request = CapturePIDRequest(txnId = txnId)
                when (val result = abhaIdRepo.submitCapturePID(request)) {
                    is NetworkResult.Success -> {
                        when (result.data.status) {
                            "PENDING" -> {
                                _state.value = State.FACE_AUTH_PENDING
                            }

                            "VERIFIED" -> {
                                _state.value = State.FACE_AUTH_VERIFIED
                            }

                            "COMPLETE" -> {
                                _state.value = State.FACE_AUTH_SUCCESS
                                break
                            }

                            "FAILED" -> {
                                _errorMessage.value =
                                    "Face authentication failed. Please try again."
                                _state.value = State.FACE_AUTH_FAILURE
                                break
                            }
                            else -> _state.value = State.FACE_AUTH_PENDING
                        }
                    }

                    is NetworkResult.Error -> {
                        _errorMessage.value = result.message
                        _state.value = State.FACE_AUTH_FAILURE
                        break
                    }

                    NetworkResult.NetworkError -> {
                        _state.value = State.FACE_AUTH_FAILURE
                        break
                    }
                }
                delay(5000)
            }
        }
    }

    fun completeFaceEnrollment() {
        viewModelScope.launch {
            _state.value = State.ABHA_GENERATION_PENDING
            val request = FaceEnrollmentRequest(
                authData = FaceAuthData(
                    face = FaceBlock(
                        txnId = txnId,
                        aadhaar = aadhaarNumber,
                        mobile = mobileNumber
                    )
                )
            )
            when (val result = abhaIdRepo.enrollByFace(request)) {
                is NetworkResult.Success -> {
                    if (result.data.tokens.token.isNullOrEmpty() == false){
                        TokenInsertAbhaInterceptor.setXToken(result.data.tokens.token)
                        _txnId = result.data.txnId
                        _abhaResponse = com.google.gson.Gson().toJson(result.data)
                        if (result.data.ABHAProfile.middleName.isNotEmpty()) {
                            _name =
                                result.data.ABHAProfile.firstName + " " + result.data.ABHAProfile.middleName + " " + result.data.ABHAProfile.lastName
                        } else {
                            _name =
                                result.data.ABHAProfile.firstName + " " + result.data.ABHAProfile.lastName
                        }
                        _abhaNumber = result.data.ABHAProfile.ABHANumber
                        _phrAddress = result.data.ABHAProfile.phrAddress?.get(0) ?: ""
                        _state.value = State.ABHA_GENERATION_SUCCESS
                    } else {
                        _errorMessage.value = result.data.message
                        _state.value = State.ABHA_GENERATION_FAILED
                    }
                }
                is NetworkResult.Error -> {
                    _errorMessage.value = result.message
                    _state.value = State.ABHA_GENERATION_FAILED
                }
                NetworkResult.NetworkError -> _state.value = State.ABHA_GENERATION_FAILED
            }
        }
    }
}
