package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_referred.list

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.filterBenList
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.utils.HelperUtil.getLocalizedResources
import javax.inject.Inject

@HiltViewModel
class NcdRefferedListViewModel @Inject constructor(
    recordsRepo: RecordsRepo,
    private val preferenceDao: PreferenceDao,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private lateinit var asha: User

    private val resources get() = getLocalizedResources(context, preferenceDao.getCurrentLanguage())

    private val englishCategories = listOf("ALL", "NCD", "TB", "LEPROSY", "GERIATRIC", "HRP", "MATERNAL")

    private fun toEnglishCategory(localizedType: String?): String {
        val localized = listOf(
            resources.getString(R.string.all),
            resources.getString(R.string.cat_ncd),
            resources.getString(R.string.cat_tb),
            resources.getString(R.string.cat_leprosy),
            resources.getString(R.string.cat_geriatric),
            resources.getString(R.string.cat_hrp),
            resources.getString(R.string.cat_maternal)
        )
        val idx = localized.indexOf(localizedType)
        return if (idx >= 0) englishCategories[idx] else localizedType ?: "ALL"
    }

    private val allBenList = recordsRepo.getNcdrefferedList
    private val filter = MutableStateFlow("")
    private val selectedBenId = MutableStateFlow(0L)
    var userName = preferenceDao.getLoggedInUser()!!.name
    val selectedFilter = MutableStateFlow<String?>(resources.getString(R.string.all))

    val benList = combine(
        allBenList,
        filter,
        selectedFilter
    ) { cacheList, searchText, selectedType ->

        val englishType = toEnglishCategory(selectedType)
        val typeFiltered = if (englishType == "ALL") {
            cacheList
        } else {
            cacheList.filter { it.referral.type == englishType }
        }

        val domainList = typeFiltered.map { it.asDomainModel() }
        val benList = domainList.map { it.ben }
        val searchedBenList = filterBenList(benList, searchText)
        domainList.filter { domain ->
            searchedBenList.any { it.benId == domain.ben.benId }
        }
    }
    var selectedPosition = 0

    private val clickedBenId = MutableStateFlow(0L)

    fun updateBottomSheetData(benId: Long) {
        viewModelScope.launch {
            clickedBenId.emit(benId)
        }
    }

    fun setSelectedFilter(type: String) {
        viewModelScope.launch {
            selectedFilter.emit(type)
        }
    }

    init {
        viewModelScope.launch {
            asha = preferenceDao.getLoggedInUser()!!
        }
    }

    fun filterText(text: String) {
        viewModelScope.launch {
            filter.emit(text)
        }

    }

    fun setSelectedBenId(benId: Long) {
        viewModelScope.launch {
            selectedBenId.emit(benId)
        }
    }

    fun getSelectedBenId(): Long = selectedBenId.value
    fun getAshaId(): Int = asha.userId


    private val catList = ArrayList<String>()

    fun categoryData() : ArrayList<String> {

        catList.clear()
        catList.add(resources.getString(R.string.all))
        catList.add(resources.getString(R.string.cat_ncd))
        catList.add(resources.getString(R.string.cat_tb))
        catList.add(resources.getString(R.string.cat_leprosy))
        catList.add(resources.getString(R.string.cat_geriatric))
        catList.add(resources.getString(R.string.cat_hrp))
        catList.add(resources.getString(R.string.cat_maternal))


        return catList

    }

}