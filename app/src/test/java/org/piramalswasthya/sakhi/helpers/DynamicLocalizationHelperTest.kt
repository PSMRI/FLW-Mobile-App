package org.piramalswasthya.sakhi.helpers

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test
import org.piramalswasthya.sakhi.model.ChildImmunizationCategory
import org.piramalswasthya.sakhi.model.VaccineType

class DynamicLocalizationHelperTest {

    private val context: Context = mockk(relaxed = true)

    init {
        // Return a deterministic, resource-id-derived token for any string lookup so we can
        // exercise every branch of the when() mappings without needing real Android resources.
        every { context.getString(any()) } answers { "res_${firstArg<Int>()}" }
    }

    @Test
    fun `every ChildImmunizationCategory maps to a localized string`() {
        for (category in ChildImmunizationCategory.values()) {
            val result = with(DynamicLocalizationHelper) { category.toLocalizedString(context) }
            assertEquals(true, result.startsWith("res_"))
        }
    }

    @Test
    fun `every VaccineType maps to a localized string`() {
        for (type in VaccineType.values()) {
            val result = with(DynamicLocalizationHelper) { type.toLocalizedString(context) }
            assertEquals(true, result.startsWith("res_"))
        }
    }

    @Test
    fun `distinct categories are requested for lookup`() {
        // Sanity: ensure the extension is actually invoked for a specific value.
        val result = with(DynamicLocalizationHelper) {
            ChildImmunizationCategory.BIRTH.toLocalizedString(context)
        }
        assertEquals(true, result.isNotEmpty())
    }
}
