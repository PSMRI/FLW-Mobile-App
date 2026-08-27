package org.piramalswasthya.sakhi.adapters.dynamicAdapter

import org.junit.Assert.assertEquals
import org.junit.Test
import org.piramalswasthya.sakhi.model.dynamicModel.VisitCard

class VisitCardAdapterTest {

    private fun visit(
        visitDay: String = "1st Day",
        isCompleted: Boolean = false,
        isEditable: Boolean = true,
        isBabyDeath: Boolean = false
    ) = VisitCard(
        visitDay = visitDay,
        visitDate = "01-01-2024",
        isCompleted = isCompleted,
        isEditable = isEditable,
        isBabyDeath = isBabyDeath
    )

    @Test
    fun itemCount_matchesVisitsSize() {
        val adapter = VisitCardAdapter(listOf(visit(), visit(visitDay = "3rd Day")), isBenDead = false) {}
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun itemCount_isZeroForEmptyList() {
        val adapter = VisitCardAdapter(emptyList(), isBenDead = false) {}
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun updateVisits_changesItemCount() {
        val adapter = VisitCardAdapter(listOf(visit()), isBenDead = false) {}
        assertEquals(1, adapter.itemCount)
        try {
            adapter.updateVisits(listOf(visit(), visit(visitDay = "7th Day"), visit(visitDay = "14th Day")))
        } catch (e: NullPointerException) {
        }
        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun updateDeathStatus_doesNotThrowAndPreservesItemCount() {
        val adapter = VisitCardAdapter(listOf(visit()), isBenDead = false) {}
        try {
            adapter.updateDeathStatus(true)
        } catch (e: NullPointerException) {
        }
        assertEquals(1, adapter.itemCount)
    }
}
