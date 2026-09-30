package org.piramalswasthya.sakhi.ui.home_activity.all_household.non_household

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.repositories.HouseholdRepo
import javax.inject.Inject

@AndroidEntryPoint
class NonHouseholdLivingFragment : Fragment() {
    @Inject lateinit var householdRepo: HouseholdRepo
    @Inject lateinit var preferenceDao: PreferenceDao

    private val places by lazy {
        resources.getStringArray(R.array.non_household_living_places).toList()
    }
    private val institutionPlaces by lazy {
        resources.getStringArray(R.array.non_household_institution_places).toSet()
    }
    private val otherLivingPlace by lazy {
        getString(R.string.non_household_living_other)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        inflater.inflate(R.layout.fragment_non_household_living, container, false)

    override fun onViewCreated(view: View, state: Bundle?) {
        val place = view.findViewById<AutoCompleteTextView>(R.id.act_place)
        val other = view.findViewById<View>(R.id.til_other)
        val institution = view.findViewById<View>(R.id.til_institution)
        val placeLayout = view.findViewById<TextInputLayout>(R.id.til_place)
        val otherLayout = view.findViewById<TextInputLayout>(R.id.til_other)
        val institutionLayout = view.findViewById<TextInputLayout>(R.id.til_institution)
        val otherText = view.findViewById<TextInputEditText>(R.id.et_other)
        val institutionText = view.findViewById<TextInputEditText>(R.id.et_institution)
        place.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, places))
        place.setOnItemClickListener { _, _, position, _ ->
            val selected = places[position]
            placeLayout.error = null
            otherLayout.error = null
            institutionLayout.error = null
            other.visibility = if (selected == otherLivingPlace) View.VISIBLE else View.GONE
            institution.visibility = if (selected in institutionPlaces) View.VISIBLE else View.GONE
        }
        view.findViewById<View>(R.id.btn_continue).setOnClickListener {
            val selected = place.text.toString()
            placeLayout.error = null
            otherLayout.error = null
            institutionLayout.error = null
            if (selected !in places) {
                placeLayout.error = getString(R.string.form_input_empty_error)
                return@setOnClickListener
            }
            if (selected == otherLivingPlace && otherText.text.isNullOrBlank()) {
                otherLayout.error = getString(R.string.form_input_empty_error)
                return@setOnClickListener
            }
            if (selected in institutionPlaces && institutionText.text.isNullOrBlank()) {
                institutionLayout.error = getString(R.string.form_input_empty_error)
                return@setOnClickListener
            }
            ensureStandaloneHousehold()
        }
    }

    private fun ensureStandaloneHousehold() {
        val place = view?.findViewById<AutoCompleteTextView>(R.id.act_place)?.text?.toString()
            ?: return
        val otherPlace = view?.findViewById<TextInputEditText>(R.id.et_other)?.text?.toString()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
        val institutionName = view?.findViewById<TextInputEditText>(R.id.et_institution)?.text
            ?.toString()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            if (householdRepo.getRecord(0L) == null) {
                val user = preferenceDao.getLoggedInUser() ?: return@launchWhenStarted
                val location = preferenceDao.getLocationRecord() ?: return@launchWhenStarted
                householdRepo.persistRecord(HouseholdCache(0L, user.userId, locationRecord = location, processed = "P", isDraft = true))
            }
            findNavController().navigate(R.id.action_nonHouseholdLivingFragment_to_newBenRegFragment,
                Bundle().apply {
                    putLong("hhId", 0L)
                    putInt("relToHeadId", 18)
                    putString("livingPlace", place)
                    putString("otherLivingPlace", otherPlace)
                    putString("institutionName", institutionName)
                })
        }
    }
}
