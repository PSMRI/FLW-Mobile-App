package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_eligible_list

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.filterBenList
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.utils.HelperUtil.getLocalizedResources
import javax.inject.Inject

@HiltViewModel
class NcdEligibleListViewModel @Inject constructor(
    recordsRepo: RecordsRepo,
    private val preferenceDao: PreferenceDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private lateinit var asha: User
    var clickedPosition = 0

    private val resources get() = getLocalizedResources(context, preferenceDao.getCurrentLanguage())

    private val selectedCategory = MutableStateFlow(resources.getString(R.string.all))

    private val allBenList = recordsRepo.getNcdEligibleList
    private val filter = MutableStateFlow("")
    private val selectedBenId = MutableStateFlow(0L)

    val benList = combine(allBenList, filter, selectedCategory) { cacheList, filterText, selectedCat ->
        val list = cacheList.map { it.asDomainModel() }
        val benBasicDomainList = list.map { it.ben }
        val filteredBenBasicDomainList = filterBenList(benBasicDomainList, filterText)

        val filteredIds = filteredBenBasicDomainList.map { it.benId }.toSet()

        when (selectedCat) {
            resources.getString(R.string.screened) -> list.filter { it.savedCbacRecords.isNotEmpty() && (it.ben.benId in filteredIds) }
            resources.getString(R.string.not_screened) -> list.filter { it.savedCbacRecords.isEmpty() && (it.ben.benId in filteredIds) }
            else -> list.filter { it.ben.benId in filteredIds }
        }
    }

    fun setSelectedCategory(cat: String) {
        viewModelScope.launch {
            selectedCategory.emit(cat)
        }
    }


    val ncdDetails = allBenList.combineTransform(selectedBenId) { list, benId ->
        if (benId != 0L) {
            val emitList =
                list.firstOrNull { it.ben.benId == benId }?.savedCbacRecords?.map { it.asDomainModel(resources) }
            if (!emitList.isNullOrEmpty()) emit(emitList.reversed())
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
        catList.add(resources.getString(R.string.screened))
        catList.add(resources.getString(R.string.not_screened))
        return catList

    }

    private val yearsData = ArrayList<String>()

    fun yearsList(context: Context) : ArrayList<String> {

        yearsData.clear()
        yearsData.add(context.getString(R.string.select_years))
        yearsData.add(context.getString(R.string.years_35))
        yearsData.add(context.getString(R.string.years_40))
        yearsData.add(context.getString(R.string.years_45))
        yearsData.add(context.getString(R.string.years_50))
        yearsData.add(context.getString(R.string.years_55))
        yearsData.add(context.getString(R.string.years_60))
        yearsData.add(context.getString(R.string.years_65))
        yearsData.add(context.getString(R.string.years_70))
        yearsData.add(context.getString(R.string.years_75))
        yearsData.add(context.getString(R.string.years_80))
        return yearsData

    }

}