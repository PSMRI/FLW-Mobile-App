package org.piramalswasthya.sakhi.ui.home_activity.immunization_due

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ImmunizationDueTypeViewModel @Inject constructor(

) : ViewModel() {
    private val _navigateToChildrenImmunization = MutableLiveData(false)
    val navigateToChildrenImmunization: LiveData<Boolean>
        get() = _navigateToChildrenImmunization

    private val _navigateToMotherImmunization = MutableLiveData(false)
    val navigateToMotherImmunization: LiveData<Boolean>
        get() = _navigateToMotherImmunization

    fun resetNavigation() {
        _navigateToChildrenImmunization.value = false
        _navigateToMotherImmunization.value = false
    }

    fun navToChildren() {
        _navigateToChildrenImmunization.value = true
    }

    fun navToMother() {
        _navigateToMotherImmunization.value = true

    }
}