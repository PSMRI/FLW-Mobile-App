package org.piramalswasthya.sakhi.helpers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.BenBasicDomain
import java.util.Calendar

class CommonUtilsTest {

    // --- getAgeFromDob ---

    @Test
    fun `getAgeFromDob returns correct age for adult`() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, -25)
        val age = getAgeFromDob(cal.timeInMillis)
        assertEquals(25, age)
    }

    @Test
    fun `getAgeFromDob returns 0 for newborn`() {
        val age = getAgeFromDob(System.currentTimeMillis())
        assertEquals(0, age)
    }

    @Test
    fun `getAgeFromDob returns 0 for null`() {
        val age = getAgeFromDob(null)
        assertEquals(0, age)
    }

    @Test
    fun `getAgeFromDob returns correct age for elderly`() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, -80)
        val age = getAgeFromDob(cal.timeInMillis)
        assertEquals(80, age)
    }

    @Test
    fun `getAgeFromDob returns correct age for child`() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, -5)
        val age = getAgeFromDob(cal.timeInMillis)
        assertEquals(5, age)
    }

    // --- filterBenList by text ---

    @Test
    fun `filterBenList with empty query returns full list`() {
        val list = listOf(createBen(name = "Asha"), createBen(name = "Priya"))
        val result = filterBenList(list, "")
        assertEquals(2, result.size)
    }

    @Test
    fun `filterBenList matches by name`() {
        val list = listOf(
            createBen(name = "Asha Devi"),
            createBen(name = "Priya Sharma"),
            createBen(name = "Asha Kumari")
        )
        val result = filterBenList(list, "Asha")
        assertEquals(2, result.size)
    }

    @Test
    fun `filterBenList matches by partial name case insensitive`() {
        val list = listOf(createBen(name = "ASHA DEVI"), createBen(name = "Priya"))
        val result = filterBenList(list, "asha")
        assertEquals(1, result.size)
    }

    @Test
    fun `filterBenList matches by mobile number`() {
        val list = listOf(
            createBen(name = "Asha", mobileNo = "9876543210"),
            createBen(name = "Priya", mobileNo = "1234567890")
        )
        val result = filterBenList(list, "9876")
        assertEquals(1, result.size)
    }

    @Test
    fun `filterBenList with no match returns empty`() {
        val list = listOf(createBen(name = "Asha"), createBen(name = "Priya"))
        val result = filterBenList(list, "Nonexistent")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `filterBenList on empty list returns empty`() {
        val result = filterBenList(emptyList(), "Asha")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `filterBenList matches by benId`() {
        val list = listOf(createBen(benId = 12345, name = "Asha"))
        val result = filterBenList(list, "12345")
        assertEquals(1, result.size)
    }

    @Test
    fun `filterBenList matches by family head name`() {
        val list = listOf(
            createBen(name = "Asha", familyHeadName = "Raman Kumar"),
            createBen(name = "Priya", familyHeadName = "Suresh")
        )
        val result = filterBenList(list, "raman")
        assertEquals(1, result.size)
    }

    // --- filterBenList by type ---

    @Test
    fun `filterBenList type 1 returns only ABHA present`() {
        val list = listOf(
            createBen(name = "WithAbha", abhaId = "ABHA123"),
            createBen(name = "WithoutAbha", abhaId = null)
        )
        val result = filterBenList(list, 1)
        assertEquals(1, result.size)
        assertEquals("WithAbha", result[0].benName)
    }

    @Test
    fun `filterBenList type 2 returns only ABHA absent`() {
        val list = listOf(
            createBen(name = "WithAbha", abhaId = "ABHA123"),
            createBen(name = "WithoutAbha", abhaId = null)
        )
        val result = filterBenList(list, 2)
        assertEquals(1, result.size)
        assertEquals("WithoutAbha", result[0].benName)
    }

    @Test
    fun `filterBenList type 3 returns age 30 and above alive`() {
        val cal30 = Calendar.getInstance().apply { add(Calendar.YEAR, -30) }
        val cal25 = Calendar.getInstance().apply { add(Calendar.YEAR, -25) }
        val list = listOf(
            createBen(name = "Above30", dob = cal30.timeInMillis, isDeathValue = "false"),
            createBen(name = "Below30", dob = cal25.timeInMillis, isDeathValue = "false")
        )
        val result = filterBenList(list, 3)
        assertEquals(1, result.size)
        assertEquals("Above30", result[0].benName)
    }

    @Test
    fun `filterBenList type 0 returns all`() {
        val list = listOf(createBen(name = "A"), createBen(name = "B"))
        val result = filterBenList(list, 0)
        assertEquals(2, result.size)
    }

    @Test
    fun `filterBenList type 4 WARA returns eligible women`() {
        val cal25 = Calendar.getInstance().apply { add(Calendar.YEAR, -25) }
        val list = listOf(
            createBen(
                name = "EligibleWoman",
                gender = "Female",
                dob = cal25.timeInMillis,
                isDeathValue = "false",
                reproductiveStatusId = 1
            ),
            createBen(
                name = "Male",
                gender = "Male",
                dob = cal25.timeInMillis,
                isDeathValue = "false",
                reproductiveStatusId = 1
            )
        )
        val result = filterBenList(list, 4)
        assertEquals(1, result.size)
        assertEquals("EligibleWoman", result[0].benName)
    }

    @Test
    fun `filterBenList WARA rejects age below 20`() {
        val cal18 = Calendar.getInstance().apply { add(Calendar.YEAR, -18) }
        val list = listOf(
            createBen(
                name = "YoungWoman",
                gender = "Female",
                dob = cal18.timeInMillis,
                isDeathValue = "false",
                reproductiveStatusId = 1
            )
        )
        val result = filterBenList(list, 4)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `filterBenList WARA rejects age above 49`() {
        val cal50 = Calendar.getInstance().apply { add(Calendar.YEAR, -50) }
        val list = listOf(
            createBen(
                name = "OlderWoman",
                gender = "Female",
                dob = cal50.timeInMillis,
                isDeathValue = "false",
                reproductiveStatusId = 1
            )
        )
        val result = filterBenList(list, 4)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `filterBenList WARA rejects dead beneficiary`() {
        val cal25 = Calendar.getInstance().apply { add(Calendar.YEAR, -25) }
        val list = listOf(
            createBen(
                name = "DeadWoman",
                gender = "Female",
                dob = cal25.timeInMillis,
                isDeathValue = "true",
                reproductiveStatusId = 1
            )
        )
        val result = filterBenList(list, 4)
        assertTrue(result.isEmpty())
    }

    // --- Helper: Create BenBasicDomain for tests ---

    private fun createBen(
        benId: Long = 1L,
        hhId: Long = 100L,
        name: String = "Test",
        gender: String = "Female",
        dob: Long = Calendar.getInstance().apply { add(Calendar.YEAR, -25) }.timeInMillis,
        mobileNo: String = "9999999999",
        familyHeadName: String = "Head",
        abhaId: String? = null,
        isDeathValue: String? = "false",
        reproductiveStatusId: Int = 1
    ): BenBasicDomain {
        return BenBasicDomain(
            benId = benId,
            hhId = hhId,
            reproductiveStatusId = reproductiveStatusId,
            regDate = "17-03-2026",
            benName = name,
            gender = gender,
            dob = dob,
            relToHeadId = 1,
            mobileNo = mobileNo,
            familyHeadName = familyHeadName,
            abhaId = abhaId,
            syncState = SyncState.SYNCED,
            isConsent = true,
            isSpouseAdded = false,
            isChildrenAdded = false,
            isMarried = false,
            isDeathValue = isDeathValue
        )
    }
}
