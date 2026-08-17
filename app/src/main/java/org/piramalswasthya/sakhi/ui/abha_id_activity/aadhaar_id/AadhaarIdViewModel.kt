package org.piramalswasthya.sakhi.ui.abha_id_activity.aadhaar_id

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.network.CapturePIDRequest
import org.piramalswasthya.sakhi.network.FaceAuthData
import org.piramalswasthya.sakhi.network.FaceBlock
import org.piramalswasthya.sakhi.network.FaceEnrollmentRequest
import org.piramalswasthya.sakhi.network.NetworkResult
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
        RD_APP_NOT_INSTALLED,
        FACE_TXN_GENERATED,
        FACE_CAPTURE_PENDING,
        FACE_ENROLL_SUCCESS
    }

    enum class Abha {
        NONE,
        CREATE,
        SEARCH
    }


    private var _faceAuthTxnId: String? = null
    val faceAuthTxnId: String
        get() = _faceAuthTxnId ?: ""

    private var _pidData: String? = null
    val pidData: String
        get() = _pidData ?: ""
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

    private var _abhaResponse: String? = null
    val abhaResponse: String
        get() = _abhaResponse!!

    private var _txnId: String? = null
    val txnId: String
        get() = _txnId?:""

    private var _otpTxnId: String? = null
    val otpTxnId: String
        get() = _otpTxnId?:""

    private var _mobileNumber: String? = null
    val mobileNumber: String
        get() = _mobileNumber?:""

    private var _selectedAbhaIndex: String? = null
    val selectedAbhaIndex: String
        get() = _selectedAbhaIndex?:""

    private var _aadhaarNumber: String? = null
    val aadhaarNumber: String
        get() = _aadhaarNumber?:""

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
                    _faceAuthTxnId = result.data.txnId
                    _state.value = State.FACE_TXN_GENERATED
                }
                is NetworkResult.Error -> {
                    _errorMessage.value = result.message
                    _state.value = State.ERROR_SERVER
                }
                NetworkResult.NetworkError -> _state.value = State.ERROR_NETWORK
            }
        }
    }

    // Called from the Fragment once RD Service returns PID_DATA in onActivityResult
    fun onFaceCaptured(pidData: String) {
        _pidData = pidData
        submitCapturedPid()
    }

    private fun submitCapturedPid() {
        viewModelScope.launch {
            _state.value = State.LOADING
            val request = CapturePIDRequest(txnId = faceAuthTxnId, pid = pidData)
            when (val result = abhaIdRepo.submitCapturePID(request)) {
                is NetworkResult.Success -> {
                    when (result.data.status) {
                        "COMPLETE" -> completeFaceEnrollment()
                        "FAILED" -> {
                            _errorMessage.value = "Face authentication failed. Please try again."
                            _state.value = State.ERROR_SERVER
                        }
                        // PENDING / VERIFIED — if you want to poll every 5-10s per the PDF,
                        // have the Fragment re-call submitCapturedPid() on a timer while in this state
                        else -> _state.value = State.FACE_CAPTURE_PENDING
                    }
                }
                is NetworkResult.Error -> {
                    _errorMessage.value = result.message
                    _state.value = State.ERROR_SERVER
                }
                NetworkResult.NetworkError -> _state.value = State.ERROR_NETWORK
            }
        }
    }

    private fun completeFaceEnrollment() {
        viewModelScope.launch {
            val request = FaceEnrollmentRequest(
                authData = FaceAuthData(
                    face = FaceBlock(
                        txnId = faceAuthTxnId,
                        aadhaar = aadhaarNumber,
                        mobile = mobileNumber
                    )
                )
            )
            when (val result = abhaIdRepo.enrollByFace(request)) {
                is NetworkResult.Success -> {
                    _abhaResponse = com.google.gson.Gson().toJson(result.data)
                    _state.value = State.FACE_ENROLL_SUCCESS
                }
                is NetworkResult.Error -> {
                    _errorMessage.value = result.message
                    _state.value = State.ERROR_SERVER
                }
                NetworkResult.NetworkError -> _state.value = State.ERROR_NETWORK
            }
        }
    }
}
