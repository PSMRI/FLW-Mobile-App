package org.piramalswasthya.sakhi.adapters

import androidx.recyclerview.widget.DiffUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.BenWithEcrDomain
import org.piramalswasthya.sakhi.model.EligibleCoupleRegCache

class ECRegistrationAdapterTest {

    private fun ben(benId: Long = 1L) = BenBasicDomain(
        benId = benId,
        hhId = 100L,
        reproductiveStatusId = 1,
        regDate = "01-01-2024",
        benName = "Test",
        gender = "FEMALE",
        dob = 0L,
        relToHeadId = 1,
        mobileNo = "9999999999",
        familyHeadName = "Head",
        syncState = SyncState.SYNCED,
        isConsent = true,
        isSpouseAdded = false,
        isChildrenAdded = false,
        isMarried = false
    )

    private fun ecr(benId: Long = 1L, childCount: Int = 0, ecrCache: EligibleCoupleRegCache? = null) =
        BenWithEcrDomain(ben = ben(benId), ecr = ecrCache, childCount = childCount)

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<BenWithEcrDomain> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.ECRegistrationAdapter\$BenDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<BenWithEcrDomain>
    }

    @Test
    fun areItemsTheSame_trueForSameBenId() {
        val cb = diffCallback()
        assertTrue(cb.areItemsTheSame(ecr(benId = 5L), ecr(benId = 5L, childCount = 3)))
    }

    @Test
    fun areItemsTheSame_falseForDifferentBenId() {
        val cb = diffCallback()
        assertFalse(cb.areItemsTheSame(ecr(benId = 5L), ecr(benId = 6L)))
    }

    @Test
    fun areContentsTheSame_trueForEqualObjects() {
        val cb = diffCallback()
        assertTrue(cb.areContentsTheSame(ecr(benId = 5L, childCount = 2), ecr(benId = 5L, childCount = 2)))
    }

    @Test
    fun areContentsTheSame_falseWhenChildCountDiffers() {
        val cb = diffCallback()
        assertFalse(cb.areContentsTheSame(ecr(benId = 5L, childCount = 1), ecr(benId = 5L, childCount = 2)))
    }

    @Test
    fun clickListener_clickedAddAllBenBtn_passesDerivedIds() {
        var received: Triple<Long, Long, Boolean>? = null
        var isIfaReceived = false
        val listener = ECRegistrationAdapter.ClickListener(
            clickedAddAllBenBtn = { _, benId, hhId, isMatched, isIfa ->
                received = Triple(benId, hhId, isMatched)
                isIfaReceived = isIfa
            }
        )
        val item = ecr(benId = 9L)
        listener.clickedAddAllBenBtn(item, isMatched = true, isIFA = true)
        assertEquals(Triple(9L, 100L, true), received)
        assertTrue(isIfaReceived)
    }

    @Test
    fun clickListener_onClickForm_invokesLambdaWhenPresent() {
        var received: Pair<Long, Long>? = null
        val listener = ECRegistrationAdapter.ClickListener(
            clickedForm = { hhId, benId -> received = hhId to benId },
            clickedAddAllBenBtn = { _, _, _, _, _ -> }
        )
        listener.onClickForm(ecr(benId = 3L))
        assertEquals(100L to 3L, received)
    }

    @Test
    fun clickListener_onClickForm_doesNotThrowWhenNull() {
        val listener = ECRegistrationAdapter.ClickListener(
            clickedForm = null,
            clickedAddAllBenBtn = { _, _, _, _, _ -> }
        )
        listener.onClickForm(ecr(benId = 3L))
    }
}
