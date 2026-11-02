package org.piramalswasthya.sakhi.adapters

import androidx.recyclerview.widget.DiffUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.BenWithAncListDomain
import org.piramalswasthya.sakhi.model.PregnantWomanAncCache

class AncAbortionListAdapterTest {

    private fun benBasic(benId: Long = 1L, hhId: Long = 1L) = BenBasicDomain(
        benId = benId,
        hhId = hhId,
        reproductiveStatusId = 0,
        regDate = "01-01-2020",
        benName = "Test",
        gender = "FEMALE",
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

    private fun benWithAnc(
        benId: Long = 1L,
        savedAncRecords: List<PregnantWomanAncCache> = emptyList()
    ) = BenWithAncListDomain(
        ben = benBasic(benId = benId),
        pwr = null,
        anc = emptyList(),
        pmsma = emptyList(),
        savedAncRecords = savedAncRecords,
        showAddAnc = false,
        pmsmaFillable = false,
        hasPmsma = false,
        syncState = null
    )

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<BenWithAncListDomain> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.AncAbortionListAdapter\$DiffCallback")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<BenWithAncListDomain>
    }

    @Test
    fun areItemsTheSame_comparesByBenId() {
        val callback = diffCallback()
        assertTrue(callback.areItemsTheSame(benWithAnc(benId = 1L), benWithAnc(benId = 1L)))
        assertFalse(callback.areItemsTheSame(benWithAnc(benId = 1L), benWithAnc(benId = 2L)))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        val old = benWithAnc(benId = 1L)
        val same = benWithAnc(benId = 1L)
        val different = benWithAnc(
            benId = 1L,
            savedAncRecords = listOf(
                PregnantWomanAncCache(
                    benId = 1L,
                    visitNumber = 1,
                    terminationDoneBy = "ANM",
                    createdBy = "test",
                    updatedBy = "test",
                    syncState = SyncState.SYNCED,
                    frontFilePath = null,
                    backFilePath = null
                )
            )
        )
        assertTrue(callback.areContentsTheSame(old, same))
        assertFalse(callback.areContentsTheSame(old, different))
    }

    @Test
    fun clickListener_showVisits_invokesLambdaWithBenId() {
        var captured: Long? = null
        val listener = AncAbortionListAdapter.AbortionListClickListener(
            showVisits = { benId -> captured = benId },
            addVisit = { }
        )
        listener.showVisits(benWithAnc(benId = 7L))
        assertEquals(7L, captured)
    }

    @Test
    fun clickListener_addVisit_invokesLambdaWithBenId() {
        var captured: Long? = null
        val listener = AncAbortionListAdapter.AbortionListClickListener(
            showVisits = { },
            addVisit = { benId -> captured = benId }
        )
        listener.addVisit(benWithAnc(benId = 8L))
        assertEquals(8L, captured)
    }
}
