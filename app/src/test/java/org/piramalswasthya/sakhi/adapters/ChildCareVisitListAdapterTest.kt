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
import org.piramalswasthya.sakhi.model.ChildOption

class ChildCareVisitListAdapterTest {

    private fun option(
        formType: String = "sam",
        visitDay: String? = null,
        formDataJson: String? = null,
        recordId: Int? = null
    ) = ChildOption(
        formType = formType,
        title = "Title",
        description = "Description",
        visitDay = visitDay,
        formDataJson = formDataJson,
        recordId = recordId
    )

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<ChildOption> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.ChildCareVisitListAdapter\$MyDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<ChildOption>
    }

    @Test
    fun areItemsTheSame_comparesByFormType() {
        val callback = diffCallback()
        assertTrue(callback.areItemsTheSame(option(formType = "sam"), option(formType = "sam", visitDay = "1")))
        assertFalse(callback.areItemsTheSame(option(formType = "sam"), option(formType = "ors")))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        val old = option(formType = "sam")
        val same = option(formType = "sam")
        val different = option(formType = "sam", visitDay = "1")
        assertTrue(callback.areContentsTheSame(old, same))
        assertFalse(callback.areContentsTheSame(old, different))
    }

    @Test
    fun clickListener_onAddClicked_invokesLambdaWithDefaultsForAdd() {
        var received: List<Any?>? = null
        val listener = ChildCareVisitListAdapter.ChildOptionsClickListener { formType, visitDay, isViewMode, formDataJson, recordId ->
            received = listOf(formType, visitDay, isViewMode, formDataJson, recordId)
        }
        listener.onAddClicked(option(formType = "sam", visitDay = "3", formDataJson = "{}", recordId = 5))
        assertEquals(listOf("sam", null, false, null, null), received)
    }

    @Test
    fun clickListener_onViewClicked_invokesLambdaWithItemFieldsAndViewModeTrue() {
        var received: List<Any?>? = null
        val listener = ChildCareVisitListAdapter.ChildOptionsClickListener { formType, visitDay, isViewMode, formDataJson, recordId ->
            received = listOf(formType, visitDay, isViewMode, formDataJson, recordId)
        }
        listener.onViewClicked(option(formType = "sam", visitDay = "3", formDataJson = "{}", recordId = 5))
        assertEquals(listOf("sam", "3", true, "{}", 5), received)
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = ChildCareVisitListAdapter(ChildCareVisitListAdapter.ChildOptionsClickListener { _, _, _, _, _ -> })
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
