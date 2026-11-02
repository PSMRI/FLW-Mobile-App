package org.piramalswasthya.sakhi.adapters

import android.os.Looper
import androidx.recyclerview.widget.DiffUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.model.ChildImmunizationCategory
import org.piramalswasthya.sakhi.model.VaccineCategoryDomain
import org.piramalswasthya.sakhi.model.VaccineDomain
import org.piramalswasthya.sakhi.model.VaccineState

class ImmunizationCategoryAdapterTest {

    private fun vaccine(vaccineId: Int = 1) = VaccineDomain(
        vaccineId = vaccineId,
        vaccineName = "BCG",
        vaccineCategory = ChildImmunizationCategory.BIRTH,
        state = VaccineState.PENDING
    )

    private fun category(
        category: ChildImmunizationCategory = ChildImmunizationCategory.BIRTH,
        vaccineStateList: List<VaccineDomain> = emptyList(),
        isBenDeath: Boolean = false
    ) = VaccineCategoryDomain(category = category, vaccineStateList = vaccineStateList, isBenDeath = isBenDeath)

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<VaccineCategoryDomain> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.ImmunizationCategoryAdapter\$ImmunizationIconDiffCallback")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<VaccineCategoryDomain>
    }

    @Test
    fun areItemsTheSame_comparesByCategory() {
        val callback = diffCallback()
        assertTrue(callback.areItemsTheSame(category(category = ChildImmunizationCategory.BIRTH), category(category = ChildImmunizationCategory.BIRTH, isBenDeath = true)))
        assertFalse(callback.areItemsTheSame(category(category = ChildImmunizationCategory.BIRTH), category(category = ChildImmunizationCategory.WEEK_6)))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        assertTrue(callback.areContentsTheSame(category(isBenDeath = true), category(isBenDeath = true)))
        assertFalse(callback.areContentsTheSame(category(isBenDeath = false), category(isBenDeath = true)))
    }

    @Test
    fun clickListener_onClicked_invokesLambdaWithVaccine() {
        var captured: VaccineDomain? = null
        val listener = ImmunizationCategoryAdapter.ImmunizationIconClickListener { captured = it }
        val vaccine = vaccine(vaccineId = 3)
        listener.onClicked(vaccine)
        assertEquals(vaccine, captured)
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = ImmunizationCategoryAdapter()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
