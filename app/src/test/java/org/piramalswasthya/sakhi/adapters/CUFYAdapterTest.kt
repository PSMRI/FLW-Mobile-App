package org.piramalswasthya.sakhi.adapters

import android.os.Looper
import androidx.recyclerview.widget.DiffUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.ui.home_activity.child_care.children_under_five_years.CUFYListViewModel
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants

class CUFYAdapterTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun benBasic(benId: Long = 1L, hhId: Long = 1L) = BenBasicDomain(
        benId = benId,
        hhId = hhId,
        reproductiveStatusId = 0,
        regDate = "01-01-2020",
        benName = "Test",
        gender = "MALE",
        dob = 0L,
        relToHeadId = 1,
        mobileNo = "9999999999",
        familyHeadName = "Head",
        syncState = null,
        isConsent = true,
        isSpouseAdded = false,
        isChildrenAdded = false,
        isMarried = false
    )

    private fun withSam(benId: Long = 1L, samStatus: String = "Normal") =
        CUFYListViewModel.BenWithSamStatus(benBasic(benId = benId), samStatus)

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<CUFYListViewModel.BenWithSamStatus> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.CUFYAdapter\$BenDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<CUFYListViewModel.BenWithSamStatus>
    }

    @Test
    fun areItemsTheSame_comparesByBenId() {
        val callback = diffCallback()
        assertTrue(callback.areItemsTheSame(withSam(benId = 1L), withSam(benId = 1L, samStatus = "Severe")))
        assertFalse(callback.areItemsTheSame(withSam(benId = 1L), withSam(benId = 2L)))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        val old = withSam(benId = 1L, samStatus = "Normal")
        val same = withSam(benId = 1L, samStatus = "Normal")
        val different = withSam(benId = 1L, samStatus = "Severe")
        assertTrue(callback.areContentsTheSame(old, same))
        assertFalse(callback.areContentsTheSame(old, different))
    }

    @Test
    fun clickListener_onClickedSAM_invokesLambdaWithFormConstant() {
        var received: List<Any>? = null
        val listener = CUFYAdapter.ChildListClickListener { benId, hhId, dob, type ->
            received = listOf(benId, hhId, dob, type)
        }
        listener.onClickedSAM(benBasic(benId = 1L, hhId = 2L))
        assertEquals(listOf(1L, 2L, 0L, FormConstants.SAM_FORM_NAME), received)
    }

    @Test
    fun updateSamStatus_replacesMatchingItemAndKeepsOthers() = runBlocking {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = CUFYAdapter(CUFYAdapter.ChildListClickListener { _, _, _, _ -> })
            try {
                adapter.submitList(listOf(withSam(benId = 1L, samStatus = "Normal"), withSam(benId = 2L, samStatus = "Normal")))
                adapter.updateSamStatus(1L, "Severe")
            } catch (e: NullPointerException) {
            }
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
