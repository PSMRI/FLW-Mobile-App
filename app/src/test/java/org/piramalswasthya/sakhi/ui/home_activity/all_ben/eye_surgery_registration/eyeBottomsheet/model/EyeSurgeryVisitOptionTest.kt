package org.piramalswasthya.sakhi.ui.home_activity.all_ben.eye_surgery_registration.eyeBottomsheet.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class EyeSurgeryVisitOptionTest {

    @Test
    fun `constructor uses default values when omitted`() {
        val option = EyeSurgeryVisitOption(
            title = "Visit 1",
            visitDate = "2026-08-17",
            eyeSide = "Left",
            isAddNew = false,
            formDataJson = null,
            recordId = 1
        )

        assertNotNull(option)
        assertEquals("", option.benName)
        assertEquals("", option.gender)
        assertEquals("", option.age)
    }
}
