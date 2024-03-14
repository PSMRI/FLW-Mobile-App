package org.piramalswasthya.sakhi.ui.home_activity.all_ben.new_ben_registration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.repositories.BenRepo
import javax.inject.Inject

@HiltViewModel
class NewBenRegTypeViewModel @Inject constructor(
    private val benRepo: BenRepo
) : ViewModel() {

    private var _isConsentAgreed = false
    val isConsentAgreed: Boolean
        get() = _isConsentAgreed

    fun setConsentAgreed() {
        _isConsentAgreed = true
    }

    private val _hasDraftForKid = MutableLiveData(false)
    val hasDraftForKid: LiveData<Boolean>
        get() = _hasDraftForKid

    private val _hasDraftForGen = MutableLiveData(false)
    val hasDraftForGen: LiveData<Boolean>
        get() = _hasDraftForGen

    private val _navigateToNewBenKidRegistration = MutableLiveData(false)
    val navigateToNewBenKidRegistration: LiveData<Boolean>
        get() = _navigateToNewBenKidRegistration

    private val _navigateToNewBenGenRegistration = MutableLiveData(false)
    val navigateToNewBenGenRegistration: LiveData<Boolean>
        get() = _navigateToNewBenGenRegistration

    fun checkDraft(hhId: Long) {
        viewModelScope.launch {
            _hasDraftForKid.value = false
            _hasDraftForGen.value = false
        }
    }


    fun navigateToNewBenRegistration(hhId: Long, delete: Boolean, isKid: Boolean) {
        viewModelScope.launch {
            if (isKid)
                _navigateToNewBenKidRegistration.value = true
            else
                _navigateToNewBenGenRegistration.value = true
        }
    }

    fun navigateToBenKidRegistrationCompleted() {
        _navigateToNewBenKidRegistration.value = false
    }

    fun navigateToBenGenRegistrationCompleted() {
        _navigateToNewBenGenRegistration.value = false
    }


}