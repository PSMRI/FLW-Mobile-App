package org.piramalswasthya.sakhi.utils

import android.view.View
import android.widget.TextView
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test
import org.piramalswasthya.sakhi.R

class TextViewBindingAdaptersTest {

    private fun textViewWithMeetingString(): TextView {
        val rootView = mockk<View>(relaxed = true)
        val view = mockk<TextView>(relaxed = true)
        every { view.rootView } returns rootView
        every { rootView.resources.getString(R.string.maa_meeting) } returns "MAA Meeting"
        return view
    }

    // =====================================================
    // setFormattedMeetingDate()
    // =====================================================

    @Test
    fun `setFormattedMeetingDate formats a valid date with the meeting label`() {
        val view = textViewWithMeetingString()

        TextViewBindingAdapters.setFormattedMeetingDate(view, "17-03-2026")

        verify { view.text = "MAA Meeting - (March-2026)" }
    }

    @Test
    fun `setFormattedMeetingDate clears the text for a null date`() {
        val view = mockk<TextView>(relaxed = true)

        TextViewBindingAdapters.setFormattedMeetingDate(view, null)

        verify { view.text = "" }
    }

    @Test
    fun `setFormattedMeetingDate clears the text for a blank date`() {
        val view = mockk<TextView>(relaxed = true)

        TextViewBindingAdapters.setFormattedMeetingDate(view, "   ")

        verify { view.text = "" }
    }

    @Test
    fun `setFormattedMeetingDate clears the text when parsing fails`() {
        val view = mockk<TextView>(relaxed = true)

        TextViewBindingAdapters.setFormattedMeetingDate(view, "not-a-date")

        verify { view.text = "" }
    }

    // =====================================================
    // setORSVisibility()
    // =====================================================

    @Test
    fun `setORSVisibility shows ORS for age within 0 to 5 years`() {
        val view = mockk<View>(relaxed = true)

        TextViewBindingAdapters.setORSVisibility(view, "3 YEARS")

        verify { view.visibility = View.VISIBLE }
    }

    @Test
    fun `setORSVisibility hides ORS for age above 5 years`() {
        val view = mockk<View>(relaxed = true)

        TextViewBindingAdapters.setORSVisibility(view, "10 YEARS")

        verify { view.visibility = View.GONE }
    }

    @Test
    fun `setORSVisibility shows ORS when age string is null`() {
        val view = mockk<View>(relaxed = true)

        TextViewBindingAdapters.setORSVisibility(view, null)

        verify { view.visibility = View.VISIBLE }
    }

    // =====================================================
    // setSAMVisibility() / setIFAVisibility()
    // =====================================================

    @Test
    fun `setSAMVisibility shows when at least six months old and under one year`() {
        val view = mockk<View>(relaxed = true)

        TextViewBindingAdapters.setSAMVisibility(view, "0 YEARS 6 MONTHS")

        verify { view.visibility = View.VISIBLE }
    }

    @Test
    fun `setSAMVisibility shows for one to five years`() {
        val view = mockk<View>(relaxed = true)

        TextViewBindingAdapters.setSAMVisibility(view, "2 YEARS")

        verify { view.visibility = View.VISIBLE }
    }

    @Test
    fun `setSAMVisibility hides for over five years`() {
        val view = mockk<View>(relaxed = true)

        TextViewBindingAdapters.setSAMVisibility(view, "10 YEARS")

        verify { view.visibility = View.GONE }
    }

    @Test
    fun `setSAMVisibility hides for a null age`() {
        val view = mockk<View>(relaxed = true)

        TextViewBindingAdapters.setSAMVisibility(view, null)

        verify { view.visibility = View.GONE }
    }

    @Test
    fun `setIFAVisibility mirrors the SAM visibility rule`() {
        val view = mockk<View>(relaxed = true)

        TextViewBindingAdapters.setIFAVisibility(view, "1 YEARS 2 MONTHS")

        verify { view.visibility = View.VISIBLE }
    }

    // =====================================================
    // setDobToDateText()
    // =====================================================

    @Test
    fun `setDobToDateText formats a positive dob into dd-MM-yyyy`() {
        val view = mockk<TextView>(relaxed = true)
        val cal = java.util.Calendar.getInstance().apply {
            set(2026, java.util.Calendar.MARCH, 17, 0, 0, 0)
        }

        TextViewBindingAdapters.setDobToDateText(view, cal.timeInMillis)

        verify { view.text = "17-03-2026" }
    }

    @Test
    fun `setDobToDateText clears the text for a null dob`() {
        val view = mockk<TextView>(relaxed = true)

        TextViewBindingAdapters.setDobToDateText(view, null)

        verify { view.text = "" }
    }

    @Test
    fun `setDobToDateText clears the text for a zero or negative dob`() {
        val view = mockk<TextView>(relaxed = true)

        TextViewBindingAdapters.setDobToDateText(view, 0L)

        verify { view.text = "" }
    }
}
