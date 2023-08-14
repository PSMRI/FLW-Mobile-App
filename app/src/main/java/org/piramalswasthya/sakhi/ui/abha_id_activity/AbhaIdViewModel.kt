package org.piramalswasthya.sakhi.ui.abha_id_activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.network.AbhaTokenResponse
import org.piramalswasthya.sakhi.network.NetworkResult
import org.piramalswasthya.sakhi.network.interceptors.TokenInsertAbhaInterceptor
import org.piramalswasthya.sakhi.repositories.AbhaIdRepo
import org.piramalswasthya.sakhi.repositories.UserRepo
import javax.inject.Inject

@HiltViewModel
class AbhaIdViewModel @Inject constructor(
    private val abhaIdRepo: AbhaIdRepo,
    private val prefDao: PreferenceDao,
    private val userRepo: UserRepo
) :
    ViewModel() {

    enum class State {
        LOADING_TOKEN,
        ERROR_SERVER,
        ERROR_NETWORK,
        SUCCESS
    }

    private val _state = MutableLiveData<State>()
    val state: LiveData<State>
        get() = _state

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    init {
        generateAmritToken()
        generateAccessToken()
        generatePublicKey()
    }

    private var _accessToken: AbhaTokenResponse? = null
    private val accessToken: AbhaTokenResponse
        get() = _accessToken!!

    private var _authCert: String? = null
    private val authCert: String
        get() = _authCert!!


    private fun generateAmritToken() {
        val user = prefDao.getLoggedInUser()
        viewModelScope.launch {
            user?.let {
                if (userRepo.refreshTokenTmc(user.userName, user.password)) {
                    _state.value = State.LOADING_TOKEN
                } else {
                    _state.value = State.ERROR_SERVER
                }
            }
        }
    }

    fun generateAccessToken() {
        _state.value = State.LOADING_TOKEN
        viewModelScope.launch {
            when (val result = abhaIdRepo.getAccessToken()) {
                is NetworkResult.Success -> {
                    _accessToken = result.data
                    _state.value = State.SUCCESS
                    TokenInsertAbhaInterceptor.setToken(accessToken.accessToken)
                }

                is NetworkResult.Error -> {
                    _state.value = State.ERROR_SERVER
                    _errorMessage.value = result.message
                }

                is NetworkResult.NetworkError -> {
                    _state.value = State.ERROR_NETWORK
                }
            }
        }
    }

    private fun generatePublicKey() {
        val publicKey = prefDao.getPublicKeyForAbha()
        if (publicKey == null) {
            viewModelScope.launch {
                when (val result = abhaIdRepo.getAuthCert()) {
                    is NetworkResult.Success -> {
                        prefDao.savePublicKeyForAbha(result.data)
                    }

                    is NetworkResult.Error -> {}
                    is NetworkResult.NetworkError -> {}
                }
            }
        }
    }
}