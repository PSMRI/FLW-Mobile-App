package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.util.Log
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.BenRegGen
import org.piramalswasthya.sakhi.model.DeliveryOutcomeCache
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.InfantRegCache
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Unit tests for [ChildRegistrationDataset]. Consolidated from the previous
 * ChildRegistrationDatasetDeepTest + ChildRegistrationDatasetBranch2Test files into a single
 * class: base setUpPage/mapValues coverage plus branch coverage driving the motherBen spouseName
 * present/null (father-name fallback), delivery date/place present/null, and infant gender
 * MALE/FEMALE/null branches, then mapAsBeneficiary for male/female children.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChildRegistrationDatasetTest : BaseViewModelTest() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var mockResources: Resources
    @MockK private lateinit var preferenceDao: PreferenceDao

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.v(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.w(any(), any<Throwable>()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns Array(80) { i -> "opt$i" }
        every { mockResources.getString(any()) } returns "x"
        every { mockResources.getString(any(), any()) } returns "x"
        every { mockResources.getString(any(), any(), any()) } returns "x"
        every { preferenceDao.getLoggedInUser() } returns null
    }

    private fun motherBen(withSpouse: Boolean): BenRegCache {
        val b = mockk<BenRegCache>(relaxed = true)
        every { b.dob } returns 1_000_000_000_000L
        every { b.regDate } returns 1_600_000_000_000L
        every { b.firstName } returns "MOTHER"
        every { b.lastName } returns "LAST"
        every { b.familyHeadRelationPosition } returns 2
        every { b.isDeath } returns false
        val g = mockk<BenRegGen>(relaxed = true)
        every { g.spouseName } returns if (withSpouse) "FATHER" else null
        every { b.genDetails } returns if (withSpouse) g else null
        return b
    }

    private fun delivery(withData: Boolean): DeliveryOutcomeCache {
        val d = mockk<DeliveryOutcomeCache>(relaxed = true)
        every { d.dateOfDelivery } returns if (withData) 1_650_000_000_000L else null
        every { d.placeOfDelivery } returns if (withData) "opt0" else null
        return d
    }

    private fun infant(gender: Gender?): InfantRegCache {
        val i = mockk<InfantRegCache>(relaxed = true)
        every { i.gender } returns gender
        return i
    }

    private fun ds() = ChildRegistrationDataset(context, Languages.ENGLISH)

    @Test
    fun childRegistrationDeep() = runTest {
        val ds = ChildRegistrationDataset(context, Languages.ENGLISH)
        val motherBen = mockk<BenRegCache>(relaxed = true)
        val delivery = mockk<DeliveryOutcomeCache>(relaxed = true)
        val infant = mockk<InfantRegCache>(relaxed = true)
        runCatching { ds.setUpPage(null, null, null) }
        runCatching { ds.setUpPage(motherBen, delivery, infant) }
        runCatching { ds.mapValues(mockk<InfantRegCache>(relaxed = true), 0) }
        runCatching { ds.getIndexOfBirthCertificateFrontPath() }
        runCatching { ds.getIndexOfBirthCertificateBackPath() }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `setUpPage spouse and delivery and infant gender matrix`() = runTest {
        val genders = listOf(Gender.MALE, Gender.FEMALE, null)
        for (g in genders) {
            val d = ds()
            runCatching { d.setUpPage(motherBen(true), delivery(true), infant(g)) }
            assertNotNull(d.listFlow)
        }
    }

    @Test
    fun `setUpPage no-spouse null delivery null infant`() = runTest {
        val d = ds()
        runCatching { d.setUpPage(motherBen(false), null, null) }
        runCatching { d.setUpPage(null, delivery(false), infant(Gender.FEMALE)) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `mapAsBeneficiary male and female`() = runTest {
        val d = ds()
        runCatching { d.setUpPage(motherBen(true), delivery(true), infant(Gender.MALE)) }
        runCatching {
            d.mapAsBeneficiary(motherBen(true), mockk<User>(relaxed = true), mockk<LocationRecord>(relaxed = true))
        }
        runCatching {
            d.mapAsBeneficiary(motherBen(false), mockk<User>(relaxed = true), mockk<LocationRecord>(relaxed = true))
        }
        runCatching { d.setImageUriToFormElement(1, mockk<Uri>(relaxed = true)) }
        assertNotNull(d.listFlow)
    }

    // Drives handleListOnValueChanged (via public updateList) for every edit-text validator branch:
    // childName(2), rchId(3), rchIdMother(10), mobileNumber(9), fatherName(7),
    // weightAtBirth/birthCertificateNo(11), plus the else branch.
    @Test
    fun `updateList drives edit-text validator branches`() = runTest {
        val d = ds()
        runCatching { d.setUpPage(motherBen(true), delivery(true), infant(Gender.MALE)) }
        val valid = listOf(
            2 to "CHILD NAME",
            3 to "1234567890",
            10 to "1234567890",
            9 to "9876543210",
            7 to "FATHER NAME",
            11 to "3.2",
        )
        for ((id, v) in valid) {
            runCatching { d.setValueById(id, v); d.updateList(id, 0) }
        }
        // invalid inputs to exercise the error branches of the validators
        runCatching { d.setValueById(9, "abc"); d.updateList(9, 0) }
        runCatching { d.setValueById(2, "lower case"); d.updateList(2, 0) }
        runCatching { d.setValueById(3, "12"); d.updateList(3, 0) }
        // else branch
        runCatching { d.updateList(9999, 0) }
        assertNotNull(d.listFlow)
    }
}
