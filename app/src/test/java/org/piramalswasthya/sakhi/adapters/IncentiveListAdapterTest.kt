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
import org.piramalswasthya.sakhi.model.IncentiveActivityCache
import org.piramalswasthya.sakhi.model.IncentiveDomain
import org.piramalswasthya.sakhi.model.IncentiveRecordCache

class IncentiveListAdapterTest {

    private fun record(id: Long = 1L) = IncentiveRecordCache(
        id = id,
        activityId = 1L,
        ashaId = 1,
        benId = 1L,
        amount = 100L,
        name = "name",
        startDate = 0L,
        endDate = 0L,
        createdDate = 0L,
        createdBy = "creator",
        updatedDate = 0L,
        updatedBy = "updater",
        isEligible = true,
        verifiedByUserName = "verifier",
        reason = "reason",
        otherReason = "other",
        approvalStatus = 1,
        verifiedByUserId = 1,
        isClaimed = false,
        approvalDate = "",
        calimedDate = "",
        supervisorRole = "role"
    )

    private fun activity(id: Long = 1L) = IncentiveActivityCache(
        id = id,
        name = "Activity",
        description = "Desc",
        paymentParam = "param",
        rate = 10,
        state = 1,
        district = 1,
        group = "group",
        groupName = "groupName",
        fmrCode = null,
        fmrCodeOld = null
    )

    private fun incentive(recordId: Long = 1L) = IncentiveDomain(
        record = record(recordId),
        activity = activity(),
        ben = null
    )

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<IncentiveDomain> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.IncentiveListAdapter\$IncentiveDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<IncentiveDomain>
    }

    @Test
    fun areItemsTheSame_comparesByRecordId() {
        val callback = diffCallback()
        assertTrue(callback.areItemsTheSame(incentive(recordId = 1L), incentive(recordId = 1L)))
        assertFalse(callback.areItemsTheSame(incentive(recordId = 1L), incentive(recordId = 2L)))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        assertTrue(callback.areContentsTheSame(incentive(recordId = 1L), incentive(recordId = 1L)))
        val differentFileCount = incentive(recordId = 1L).copy(fileCount = 5)
        assertFalse(callback.areContentsTheSame(incentive(recordId = 1L), differentFileCount))
    }

    @Test
    fun clickListener_invokesRespectiveLambdas() {
        var uploadCaptured: IncentiveDomain? = null
        var viewCaptured: IncentiveDomain? = null
        var submitCaptured: IncentiveDomain? = null
        val listener = IncentiveListAdapter.FileClickListener(
            onUpload = { uploadCaptured = it },
            onView = { viewCaptured = it },
            onSubmit = { submitCaptured = it }
        )
        val item = incentive(recordId = 9L)
        listener.onUploadClick(item)
        listener.onViewClick(item)
        listener.onSubmitClick(item)
        assertEquals(item, uploadCaptured)
        assertEquals(item, viewCaptured)
        assertEquals(item, submitCaptured)
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val listener = IncentiveListAdapter.FileClickListener({}, {}, {})
            val adapter = IncentiveListAdapter(listener)
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
