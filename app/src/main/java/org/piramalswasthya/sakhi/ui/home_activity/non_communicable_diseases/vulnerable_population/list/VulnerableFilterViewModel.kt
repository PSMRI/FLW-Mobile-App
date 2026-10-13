package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.vulnerable_population.list

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.room.dao.ImmunizationDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.utils.HelperUtil.getLocalizedResources
import javax.inject.Inject

@HiltViewModel
class VulnerableFilterViewModel @Inject constructor(
    vaccineDao: ImmunizationDao,
    private val preferenceDao: PreferenceDao,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val resources get() = getLocalizedResources(context, preferenceDao.getCurrentLanguage())
    private val englishCategories = listOf(
        "ALL", "Birth Dose", "6 WEEKS", "10 WEEKS", "14 WEEKS",
        "9-12 MONTHS", "16-24 MONTHS", "5-6 YEARS", "10 YEARS", "16 YEARS"
    )

    fun toEnglishCategory(localized: String): String {
        val localizedList = listOf(
            resources.getString(R.string.all),
            resources.getString(R.string.imm_cat_birth_dose),
            resources.getString(R.string.imm_cat_6_weeks),
            resources.getString(R.string.imm_cat_10_weeks),
            resources.getString(R.string.imm_cat_14_weeks),
            resources.getString(R.string.imm_cat_9_12_months),
            resources.getString(R.string.imm_cat_16_24_months),
            resources.getString(R.string.imm_cat_5_6_years),
            resources.getString(R.string.imm_cat_10_years),
            resources.getString(R.string.imm_cat_16_years)
        )
        val idx = localizedList.indexOf(localized)
        return if (idx > 0) englishCategories[idx] else ""
    }
    private val filter = MutableStateFlow("")
    val selectedFilter = MutableLiveData<String?>(resources.getString(R.string.all))
    var selectedPosition = 0

    fun filterText(text: String) {
        viewModelScope.launch {
            filter.emit(text.trim().lowercase())
        }

    }

    private val clickedBenId = MutableStateFlow(0L)

    fun updateBottomSheetData(benId: Long) {
        viewModelScope.launch {
            clickedBenId.emit(benId)
        }
    }
    private val catList = ArrayList<String>()

    fun categoryData() : ArrayList<String> {

        catList.clear()
        catList.add(resources.getString(R.string.all))
        catList.add(resources.getString(R.string.imm_cat_birth_dose))
        catList.add(resources.getString(R.string.imm_cat_6_weeks))
        catList.add(resources.getString(R.string.imm_cat_10_weeks))
        catList.add(resources.getString(R.string.imm_cat_14_weeks))
        catList.add(resources.getString(R.string.imm_cat_9_12_months))
        catList.add(resources.getString(R.string.imm_cat_16_24_months))
        catList.add(resources.getString(R.string.imm_cat_5_6_years))
        catList.add(resources.getString(R.string.imm_cat_10_years))
        catList.add(resources.getString(R.string.imm_cat_16_years))

        return catList

    }
}