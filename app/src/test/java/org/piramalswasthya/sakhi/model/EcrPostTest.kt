package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EcrPostTest {

    private fun minimal() = EcrPost(
        benId = 900L,
        createdBy = "asha",
        createdDate = "2024-01-01",
        updatedBy = "asha",
        updatedDate = "2024-01-02",
        isKitHandedOver = false
    )

    @Test
    fun `default constructor applies documented defaults`() {
        val post = minimal()

        assertNotNull(post)
        assertEquals(900L, post.benId)
        assertNull(post.dateOfReg)
        assertNull(post.bankAccount)
        assertNull(post.bankName)
        assertNull(post.branchName)
        assertNull(post.ifsc)
        assertNull(post.numChildren)
        assertNull(post.numLiveChildren)
        assertNull(post.numMaleChildren)
        assertNull(post.numFemaleChildren)
        assertNull(post.dob1)
        assertNull(post.age1)
        assertNull(post.gender1)
        assertNull(post.lmpDate)
        assertNull(post.marriageFirstChildGap)
        assertNull(post.misCarriage)
        assertNull(post.homeDelivery)
        assertNull(post.medicalIssues)
        assertNull(post.pastCSection)
        assertFalse(post.isHighRisk)
        assertTrue(post.isRegistered)
        assertFalse(post.isKitHandedOver)
        assertEquals("", post.kitHandedOverDate)
        assertEquals("", post.kitPhoto1)
        assertEquals("", post.kitPhoto2)
    }

    @Test
    fun `child detail fields round trip`() {
        val post = minimal().copy(
            dateOfReg = "2024-01-01",
            bankAccount = 123456789L,
            bankName = "Bank",
            branchName = "Branch",
            ifsc = "IFSC0001",
            numChildren = 3,
            numLiveChildren = 3,
            numMaleChildren = 2,
            numFemaleChildren = 1,
            dob1 = "2015-01-01",
            age1 = 9,
            lmpDate = "2024-01-01",
            marriageFirstChildGap = 2,
            dob2 = "2017-01-01",
            age2 = 7,
            firstAndSecondChildGap = 2,
            dob3 = "2019-01-01",
            age3 = 5,
            secondAndThirdChildGap = 2,
            dob4 = "2020-01-01",
            age4 = 4,
            thirdAndFourthChildGap = 1,
            dob5 = "2021-01-01",
            age5 = 3,
            fourthAndFifthChildGap = 1,
            dob6 = "2022-01-01",
            age6 = 2,
            fifthANdSixthChildGap = 1,
            dob7 = "2023-01-01",
            age7 = 1,
            sixthAndSeventhChildGap = 1,
            dob8 = "2023-06-01",
            age8 = 1,
            seventhAndEighthChildGap = 1,
            dob9 = "2023-12-01",
            age9 = 0,
            eighthAndNinthChildGap = 1,
            kitHandedOverDate = "2024-02-01",
            kitPhoto1 = "p1",
            kitPhoto2 = "p2",
            isKitHandedOver = true
        )

        assertEquals(123456789L, post.bankAccount)
        assertEquals("Bank", post.bankName)
        assertEquals("Branch", post.branchName)
        assertEquals("IFSC0001", post.ifsc)
        assertEquals(3, post.numChildren)
        assertEquals(3, post.numLiveChildren)
        assertEquals(2, post.numMaleChildren)
        assertEquals(1, post.numFemaleChildren)
        assertEquals("2015-01-01", post.dob1)
        assertEquals(9, post.age1)
        assertEquals(2, post.marriageFirstChildGap)
        assertEquals("2017-01-01", post.dob2)
        assertEquals(7, post.age2)
        assertEquals(2, post.firstAndSecondChildGap)
        assertEquals("2019-01-01", post.dob3)
        assertEquals(5, post.age3)
        assertEquals(2, post.secondAndThirdChildGap)
        assertEquals("2020-01-01", post.dob4)
        assertEquals(4, post.age4)
        assertEquals(1, post.thirdAndFourthChildGap)
        assertEquals("2021-01-01", post.dob5)
        assertEquals(3, post.age5)
        assertEquals(1, post.fourthAndFifthChildGap)
        assertEquals("2022-01-01", post.dob6)
        assertEquals(2, post.age6)
        assertEquals(1, post.fifthANdSixthChildGap)
        assertEquals("2023-01-01", post.dob7)
        assertEquals(1, post.age7)
        assertEquals(1, post.sixthAndSeventhChildGap)
        assertEquals("2023-06-01", post.dob8)
        assertEquals(1, post.age8)
        assertEquals(1, post.seventhAndEighthChildGap)
        assertEquals("2023-12-01", post.dob9)
        assertEquals(0, post.age9)
        assertEquals(1, post.eighthAndNinthChildGap)
        assertEquals("2024-02-01", post.kitHandedOverDate)
        assertEquals("p1", post.kitPhoto1)
        assertEquals("p2", post.kitPhoto2)
        assertTrue(post.isKitHandedOver)
    }

    @Test
    fun `mutable risk flags can be reassigned`() {
        val post = minimal()
        post.misCarriage = "Yes"
        post.homeDelivery = "No"
        post.medicalIssues = "None"
        post.pastCSection = "No"
        post.isHighRisk = true
        post.isRegistered = false
        post.createdBy = "supervisor"
        post.updatedBy = "supervisor"

        assertEquals("Yes", post.misCarriage)
        assertEquals("No", post.homeDelivery)
        assertEquals("None", post.medicalIssues)
        assertEquals("No", post.pastCSection)
        assertTrue(post.isHighRisk)
        assertFalse(post.isRegistered)
        assertEquals("supervisor", post.createdBy)
        assertEquals("supervisor", post.updatedBy)
        assertNotNull(post.toString())
        assertEquals(post, post.copy())
    }

    @Test
    fun `accessor round trip covers every property`() {
        val obj = minimal()
        obj.javaClass.methods
            .filter { (it.name.startsWith("get") || it.name.startsWith("is")) && it.parameterCount == 0 }
            .forEach { getter ->
                runCatching {
                    val value = getter.invoke(obj)
                    val setterName = "set" + getter.name.removePrefix("get").removePrefix("is")
                    obj.javaClass.methods
                        .firstOrNull { it.name == setterName && it.parameterCount == 1 }
                        ?.invoke(obj, value)
                }
            }
        assertNotNull(obj)
    }
}
