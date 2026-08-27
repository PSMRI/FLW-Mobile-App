package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.cbac

import android.app.Application
import android.content.res.Configuration
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.piramalswasthya.sakhi.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.CbacDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.AgeUnit
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.BenRegGen
import org.piramalswasthya.sakhi.model.CbacCache
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.ReferalCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.CbacRepo
import org.piramalswasthya.sakhi.repositories.NcdReferalRepo
import java.util.concurrent.TimeUnit

/**
 * Unit tests for [CbacViewModel].
 *
 * Notes on the fixture:
 *  - `Dispatchers.IO` is redirected onto the test scheduler so the `init` block's
 *    `withContext(Dispatchers.IO) { ... }` completes deterministically under `advanceUntilIdle()`
 *    instead of racing on a real IO thread.
 *  - The view model's private `resources` lazy builds a localized `Resources` through
 *    `Configuration.setLocale`, which is not available in a JVM unit test. Everything that reads it
 *    (`raTotalScore`, `phq2TotalScore`, `raAgeText` and the *failing* arms of `dataValid()`) is
 *    therefore deliberately never observed / only reached inside `runCatching`.
 *  - The "edit" path (`cbacId > 0`) hands the view model the very same [CbacCache] instance the repo
 *    mock returns, so setter effects can be asserted directly on that object.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CbacViewModelTest : BaseViewModelTest() {

    @MockK
    private lateinit var context: Application

    @MockK
    private lateinit var benDao: BenDao

    @MockK
    private lateinit var preferenceDao: PreferenceDao

    @MockK
    private lateinit var cbacRepo: CbacRepo

    @MockK
    private lateinit var cbacDao: CbacDao

    @MockK
    private lateinit var referalRepo: NcdReferalRepo

    private lateinit var user: User

    @After
    fun releaseStaticMocks() {
        unmockkStatic(Dispatchers::class)
    }

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false

        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns testDispatcher

        user = mockk(relaxed = true)
        every { user.userId } returns 11
        every { user.userName } returns "asha"
        every { user.serviceMapId } returns 99

        every { preferenceDao.getLoggedInUser() } returns user
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH

        coEvery { benDao.getBen(any<Long>()) } returns benMock()
        coEvery { cbacRepo.getLastFilledCbac(any()) } returns null
        coEvery { cbacRepo.getCbacCacheFromId(any()) } returns emptyCbac()
        coEvery { cbacRepo.saveCbacData(any(), any()) } returns true
        coEvery { referalRepo.saveReferedNCD(any()) } just Runs
    }

    // ----------------------------------------------------------------------------------------
    // fixtures
    // ----------------------------------------------------------------------------------------

    private fun benMock(
        gender: Gender = Gender.FEMALE,
        age: Int = 65,
        reproductiveStatusId: Int = 1,
        regDate: Long = 1_600_000_000_000L,
        dob: Long = 1_000_000_000_000L
    ): BenRegCache {
        val b = mockk<BenRegCache>(relaxed = true)
        every { b.ageUnit } returns AgeUnit.YEARS
        every { b.age } returns age
        every { b.dob } returns dob
        every { b.regDate } returns regDate
        every { b.gender } returns gender
        every { b.firstName } returns "RITA"
        every { b.lastName } returns "DEVI"
        every { b.benRegId } returns 555L
        every { b.processed } returns "N"
        val gen = mockk<BenRegGen>(relaxed = true)
        every { gen.reproductiveStatusId } returns reproductiveStatusId
        every { b.genDetails } returns gen
        return b
    }

    private fun emptyCbac(): CbacCache =
        CbacCache(id = 9, benId = BEN_ID, ashaId = ASHA_ID, syncState = SyncState.UNSYNCED)

    /** A cache where every field checked by `dataValid()` is answered, so validation passes. */
    private fun fullCbac(): CbacCache = emptyCbac().apply {
        cbac_age_posi = 1
        cbac_smoke_posi = 1
        cbac_alcohol_posi = 1
        cbac_waist_posi = 1
        cbac_pa_posi = 1
        cbac_familyhistory_posi = 1
        cbac_sufferingtb_pos = 1
        cbac_antitbdrugs_pos = 1
        cbac_tbhistory_pos = 1
        cbac_coughing_pos = 1
        cbac_bloodsputum_pos = 1
        cbac_fivermore_pos = 1
        cbac_loseofweight_pos = 1
        cbac_nightsweats_pos = 1
        cbac_uicers_pos = 1
        cbac_tingling_palm_posi = 1
        cbac_cloudy_posi = 1
        cbac_diffreading_posi = 1
        cbac_pain_ineyes_posi = 1
        cbac_redness_ineyes_posi = 1
        cbac_diff_inhearing_posi = 1
        cbac_sortnesofbirth_pos = 1
        cbac_historyoffits_pos = 1
        cbac_difficultyinmouth_pos = 1
        cbac_growth_in_mouth_posi = 1
        cbac_toneofvoice_pos = 1
        cbac_white_or_red_patch_posi = 1
        cbac_Pain_while_chewing_posi = 1
        cbac_hyper_pigmented_patch_posi = 1
        cbac_any_thickend_skin_posi = 1
        cbac_nodules_on_skin_posi = 1
        cbac_numbness_on_palm_posi = 1
        cbac_clawing_of_fingers_posi = 1
        cbac_tingling_or_numbness_posi = 1
        cbac_inability_close_eyelid_posi = 1
        cbac_diff_holding_obj_posi = 1
        cbac_weekness_in_feet_posi = 1
        cbac_lumpinbreast_pos = 1
        cbac_blooddischage_pos = 1
        cbac_changeinbreast_pos = 1
        cbac_bleedingbtwnperiods_pos = 1
        cbac_bleedingaftermenopause_pos = 1
        cbac_bleedingafterintercourse_pos = 1
        cbac_foulveginaldischarge_pos = 1
        cbac_feeling_unsteady_posi = 1
        cbac_suffer_physical_disability_posi = 1
        cbac_needing_help_posi = 1
        cbac_forgetting_names_posi = 1
    }

    private fun buildVm(cbacId: Int = 0, benId: Long = BEN_ID, ashaId: Int = ASHA_ID): CbacViewModel =
        CbacViewModel(
            context,
            SavedStateHandle(
                mapOf("benId" to benId, "cbacId" to cbacId, "ashaId" to ashaId)
            ),
            benDao,
            preferenceDao,
            cbacRepo,
            cbacDao,
            referalRepo
        )

    /** Reads the current value of a mapped LiveData (mapping is lazy until observed). */
    private fun <T> LiveData<T>.latest(): T? {
        var captured: T? = null
        val observer = Observer<T> { captured = it }
        observeForever(observer)
        removeObserver(observer)
        return captured
    }

    private fun referral(reason: String): ReferalCache =
        ReferalCache(benId = BEN_ID, referralReason = reason, syncState = SyncState.UNSYNCED)

    // ----------------------------------------------------------------------------------------
    // construction / args
    // ----------------------------------------------------------------------------------------

    @Test
    fun `viewModel initializes successfully`() {
        val vm = buildVm()
        assertNotNull(vm)
        assertEquals(CbacViewModel.State.LOADING, vm.state.value)
    }

    @Test
    fun `benId and cbacId are read from SavedStateHandle`() {
        val vm = buildVm(cbacId = 17, benId = 88L)
        assertEquals(88L, vm.benId)
        assertEquals(17, vm.cbacId)
    }

    @Test
    fun `setHisTb is a no-op before the cbac cache is initialized`() {
        val vm = buildVm()
        vm.setHisTb(1)
        assertEquals(CbacViewModel.State.LOADING, vm.state.value)
    }

    @Test
    fun `logged in user is exposed`() {
        val vm = buildVm()
        assertEquals(user, vm.user)
    }

    // ----------------------------------------------------------------------------------------
    // init
    // ----------------------------------------------------------------------------------------

    @Test
    fun `init on create path publishes ben details and IDLE state`() = runTest {
        val vm = buildVm(cbacId = 0)
        advanceUntilIdle()

        assertEquals(CbacViewModel.State.IDLE, vm.state.value)
        assertEquals("RITA DEVI", vm.benName.value)
        assertEquals("65 YEARS | FEMALE", vm.benAgeGender.value)
        assertEquals(Gender.FEMALE, vm.gender.value)
        assertEquals(65, vm.age.value)
        assertEquals(1_600_000_000_000L, vm.minDate.value)
        assertNull(vm.filledCbac.value)
    }

    @Test
    fun `init with a null last name renders only the first name`() = runTest {
        val b = benMock()
        every { b.lastName } returns null
        coEvery { benDao.getBen(any<Long>()) } returns b

        val vm = buildVm(cbacId = 0)
        advanceUntilIdle()

        assertEquals("RITA ", vm.benName.value)
    }

    @Test
    fun `init uses last filled cbac plus one year as min date`() = runTest {
        val previous = emptyCbac().apply { fillDate = 1_700_000_000_000L }
        coEvery { cbacRepo.getLastFilledCbac(any()) } returns previous

        val vm = buildVm(cbacId = 0)
        advanceUntilIdle()

        assertEquals(1_700_000_000_000L + TimeUnit.DAYS.toMillis(365), vm.minDate.value)
    }

    @Test
    fun `init on edit path publishes the stored cbac`() = runTest {
        val stored = fullCbac()
        coEvery { cbacRepo.getCbacCacheFromId(4) } returns stored

        val vm = buildVm(cbacId = 4)
        advanceUntilIdle()

        assertEquals(stored, vm.filledCbac.value)
        assertEquals(CbacViewModel.State.IDLE, vm.state.value)
    }

    @Test
    fun `init maps every age bucket to a risk-assessment score`() = runTest {
        val expectations = listOf(20 to "0", 35 to "1", 45 to "2", 55 to "3", 70 to "4")
        for ((age, expected) in expectations) {
            coEvery { benDao.getBen(any<Long>()) } returns benMock(age = age)
            val vm = buildVm(cbacId = 0)
            advanceUntilIdle()
            assertEquals("age=$age", expected, vm.raAgeScore.latest())
        }
    }

    // ----------------------------------------------------------------------------------------
    // scoring setters
    // ----------------------------------------------------------------------------------------

    private suspend fun editVm(cache: CbacCache): CbacViewModel {
        coEvery { cbacRepo.getCbacCacheFromId(any()) } returns cache
        return buildVm(cbacId = 4)
    }

    @Test
    fun `setSmoke stores the position and maps the score`() = runTest {
        val cache = emptyCbac()
        val vm = editVm(cache)
        advanceUntilIdle()

        val expected = listOf("0", "1", "2", "0")
        for (i in 0..3) {
            vm.setSmoke(i)
            assertEquals("i=$i", expected[i], vm.raSmokeScore.latest())
            assertEquals(i + 1, cache.cbac_smoke_posi)
        }
    }

    @Test
    fun `setAlcohol stores the position and maps the score`() = runTest {
        val cache = emptyCbac()
        val vm = editVm(cache)
        advanceUntilIdle()

        val expected = listOf("0", "1", "0")
        for (i in 0..2) {
            vm.setAlcohol(i)
            assertEquals("i=$i", expected[i], vm.raAlcoholScore.latest())
            assertEquals(i + 1, cache.cbac_alcohol_posi)
        }
    }

    @Test
    fun `setWaist stores the position and maps the score`() = runTest {
        val cache = emptyCbac()
        val vm = editVm(cache)
        advanceUntilIdle()

        val expected = listOf("0", "1", "2", "0")
        for (i in 0..3) {
            vm.setWaist(i)
            assertEquals("i=$i", expected[i], vm.raWaistScore.latest())
            assertEquals(i + 1, cache.cbac_waist_posi)
        }
    }

    @Test
    fun `setPa stores the position and maps the score`() = runTest {
        val cache = emptyCbac()
        val vm = editVm(cache)
        advanceUntilIdle()

        val expected = listOf("0", "1", "0")
        for (i in 0..2) {
            vm.setPa(i)
            assertEquals("i=$i", expected[i], vm.raPaScore.latest())
            assertEquals(i + 1, cache.cbac_pa_posi)
        }
    }

    @Test
    fun `setFh scores family history of two for a yes`() = runTest {
        val cache = emptyCbac()
        val vm = editVm(cache)
        advanceUntilIdle()

        val expected = listOf("0", "2", "0")
        for (i in 0..2) {
            vm.setFh(i)
            assertEquals("i=$i", expected[i], vm.raFhScore.latest())
            assertEquals(i + 1, cache.cbac_familyhistory_posi)
        }
    }

    @Test
    fun `phq2 setters accumulate the depression scores`() = runTest {
        val cache = emptyCbac()
        val vm = editVm(cache)
        advanceUntilIdle()

        vm.setLi(2)
        assertEquals("2", vm.phq2LittleInterestScore.latest())
        assertEquals(3, cache.cbac_little_interest_posi)
        assertEquals(2, cache.cbac_little_interest_score)

        vm.setFd(1)
        assertEquals("1", vm.phq2FeelDownDepScore.latest())
        assertEquals(2, cache.cbac_feeling_down_posi)
        assertEquals(1, cache.cbac_feeling_down_score)
    }

    // ----------------------------------------------------------------------------------------
    // symptom counters
    // ----------------------------------------------------------------------------------------

    @Test
    fun `tb symptom setters raise and lower the ast1 counter`() = runTest {
        val cache = emptyCbac()
        val vm = editVm(cache)
        advanceUntilIdle()

        vm.setCoughing(1)
        vm.setBloodSputum(1)
        vm.setFeverWks(1)
        vm.setLsWt(1)
        vm.setNtSwets(1)
        assertEquals(5, vm.ast1.value)
        assertEquals(1, cache.cbac_coughing_pos)
        assertEquals(1, cache.cbac_nightsweats_pos)

        vm.setCoughing(2)
        vm.setBloodSputum(2)
        vm.setFeverWks(2)
        vm.setLsWt(2)
        vm.setNtSwets(2)
        assertEquals(0, vm.ast1.value)
        assertEquals(2, cache.cbac_coughing_pos)

        // already at zero, the guard keeps it there
        vm.setCoughing(2)
        assertEquals(0, vm.ast1.value)
    }

    @Test
    fun `tb history setters raise and lower the ast2 counter`() = runTest {
        val cache = emptyCbac()
        val vm = editVm(cache)
        advanceUntilIdle()

        vm.setFhTb(1)
        vm.setTakingTbDrug(1)
        assertEquals(2, vm.ast2.value)

        vm.setFhTb(2)
        vm.setTakingTbDrug(2)
        assertEquals(0, vm.ast2.value)

        vm.setFhTb(2)
        vm.setTakingTbDrug(2)
        assertEquals(0, vm.ast2.value)

        vm.setHisTb(1)
        assertEquals(1, cache.cbac_tbhistory_pos)
    }

    @Test
    fun `geriatric setters raise and lower the astMoic counter`() = runTest {
        val cache = emptyCbac()
        val vm = editVm(cache)
        advanceUntilIdle()

        vm.setUnsteady(1)
        vm.setPdRm(1)
        vm.setNhop(1)
        vm.setForgetNames(1)
        assertEquals(4, vm.astMoic.value)
        assertEquals(1, cache.cbac_feeling_unsteady_posi)
        assertEquals(1, cache.cbac_forgetting_names_posi)

        vm.setUnsteady(2)
        vm.setPdRm(2)
        vm.setNhop(2)
        vm.setForgetNames(2)
        assertEquals(0, vm.astMoic.value)

        vm.setForgetNames(2)
        assertEquals(0, vm.astMoic.value)
    }

    // ----------------------------------------------------------------------------------------
    // plain field setters
    // ----------------------------------------------------------------------------------------

    @Test
    fun `plain symptom setters write straight through to the cbac cache`() = runTest {
        val cache = emptyCbac()
        val vm = editVm(cache)
        advanceUntilIdle()

        vm.setRecurrentCloudy(1)
        vm.setRecurrentUlceration(2)
        vm.setDiffReading(1)
        vm.setPainEyes(2)
        vm.setDiffHearing(1)
        vm.setRedEyes(2)
        vm.setBreathe(1)
        vm.setHisFits(2)
        vm.setDiffMouth(1)
        vm.setHealed(2)
        vm.setVoice(1)
        vm.setAnyGrowth(2)
        vm.setAnyWhite(1)
        vm.setPainChew(2)
        vm.setHyperPig(1)
        vm.setThickSkin(2)
        vm.setNoduleSkin(1)
        vm.setTing(2)
        vm.setNumb(1)
        vm.setClaw(2)
        vm.setTingNumb(1)
        vm.setCloseEyelid(2)
        vm.setHoldObj(1)
        vm.setWeakFeet(2)
        vm.setBreast(1)
        vm.setBlP(2)
        vm.setFoulD(1)
        vm.setFuelType(2)
        vm.setOccExposure(3)

        assertEquals(1, cache.cbac_cloudy_posi)
        assertEquals(2, cache.cbac_uicers_pos)
        assertEquals(1, cache.cbac_diffreading_posi)
        assertEquals(2, cache.cbac_pain_ineyes_posi)
        assertEquals(1, cache.cbac_diff_inhearing_posi)
        assertEquals(2, cache.cbac_redness_ineyes_posi)
        assertEquals(1, cache.cbac_sortnesofbirth_pos)
        assertEquals(2, cache.cbac_historyoffits_pos)
        assertEquals(1, cache.cbac_difficultyinmouth_pos)
        // setHealed and setAnyGrowth intentionally share the same backing field
        assertEquals(2, cache.cbac_growth_in_mouth_posi)
        assertEquals(1, cache.cbac_toneofvoice_pos)
        assertEquals(1, cache.cbac_white_or_red_patch_posi)
        assertEquals(2, cache.cbac_Pain_while_chewing_posi)
        assertEquals(1, cache.cbac_hyper_pigmented_patch_posi)
        assertEquals(2, cache.cbac_any_thickend_skin_posi)
        assertEquals(1, cache.cbac_nodules_on_skin_posi)
        assertEquals(2, cache.cbac_tingling_palm_posi)
        assertEquals(1, cache.cbac_numbness_on_palm_posi)
        assertEquals(2, cache.cbac_clawing_of_fingers_posi)
        assertEquals(1, cache.cbac_tingling_or_numbness_posi)
        assertEquals(2, cache.cbac_inability_close_eyelid_posi)
        assertEquals(1, cache.cbac_diff_holding_obj_posi)
        assertEquals(2, cache.cbac_weekness_in_feet_posi)
        assertEquals(1, cache.cbac_changeinbreast_pos)
        assertEquals(2, cache.cbac_bleedingbtwnperiods_pos)
        assertEquals(1, cache.cbac_foulveginaldischarge_pos)
        assertEquals(3, cache.cbac_fuel_used_posi)
        assertEquals(4, cache.cbac_occupational_exposure_posi)
    }

    @Test
    fun `sputum tracing and referral text setters round trip`() = runTest {
        val cache = emptyCbac()
        val vm = editVm(cache)
        advanceUntilIdle()

        assertEquals("0", vm.getCollectSputum())
        vm.setCollectSputum(1)
        assertEquals("1", vm.getCollectSputum())
        assertEquals("1", cache.cbac_sputemcollection)

        vm.setTraceAllMembers(2)
        assertEquals("2", cache.cbac_tracing_all_fm)

        vm.setReferMoic(3)
        assertEquals("3", cache.cbac_referpatient_mo)

        vm.setFillDate(1_650_000_000_000L)
        assertEquals(1_650_000_000_000L, cache.fillDate)
    }

    @Test
    fun `resetState moves the form back to IDLE`() = runTest {
        val vm = editVm(emptyCbac())
        advanceUntilIdle()
        vm.resetState()
        assertEquals(CbacViewModel.State.IDLE, vm.state.value)
    }

    // ----------------------------------------------------------------------------------------
    // referral dialog / leprosy
    // ----------------------------------------------------------------------------------------

    @Test
    fun `a breast lump raises the referral dialog`() = runTest {
        val cache = emptyCbac()
        val vm = editVm(cache)
        advanceUntilIdle()

        vm.setLumpB(1)
        vm.setBlI(0)
        assertEquals(true, vm.showReferralDialog.value)
        assertEquals(1, cache.cbac_lumpinbreast_pos)
    }

    @Test
    fun `bleeding after intercourse raises the referral dialog`() = runTest {
        val cache = emptyCbac()
        val vm = editVm(cache)
        advanceUntilIdle()

        vm.setBlI(1)
        assertEquals(true, vm.showReferralDialog.value)
        assertEquals(1, cache.cbac_bleedingafterintercourse_pos)
    }

    @Test
    fun `no gynaecological red flag keeps the referral dialog hidden`() = runTest {
        val cache = emptyCbac()
        val vm = editVm(cache)
        advanceUntilIdle()

        vm.setLumpB(2)
        vm.setNipple(2)
        vm.setBlM(2)
        vm.setBlI(2)
        assertEquals(false, vm.showReferralDialog.value)
        assertEquals(2, cache.cbac_blooddischage_pos)
        assertEquals(2, cache.cbac_bleedingaftermenopause_pos)
    }

    @Test
    fun `a lone nipple discharge raises the referral dialog`() = runTest {
        val cache = emptyCbac()
        val vm = editVm(cache)
        advanceUntilIdle()

        vm.setNipple(1)
        vm.setBlI(2)

        assertEquals(true, vm.showReferralDialog.value)
        assertEquals(1, cache.cbac_blooddischage_pos)
    }

    @Test
    fun `a lone bleeding after menopause raises the referral dialog`() = runTest {
        val cache = emptyCbac()
        val vm = editVm(cache)
        advanceUntilIdle()

        vm.setBlM(1)
        vm.setBlI(2)

        assertEquals(true, vm.showReferralDialog.value)
        assertEquals(1, cache.cbac_bleedingaftermenopause_pos)
    }

    @Test
    fun `checkLeprosySymptoms flags a fully symptomatic cache`() = runTest {
        val vm = editVm(fullCbac())
        advanceUntilIdle()

        vm.checkLeprosySymptoms()
        assertEquals(true, vm.isLeprosySuspected.value)
    }

    @Test
    fun `checkLeprosySymptoms clears when nothing is reported`() = runTest {
        val vm = editVm(emptyCbac())
        advanceUntilIdle()

        assertEquals(false, vm.isLeprosySuspected.value)
        vm.checkLeprosySymptoms()
        assertEquals(false, vm.isLeprosySuspected.value)
    }

    // ----------------------------------------------------------------------------------------
    // referral list bookkeeping
    // ----------------------------------------------------------------------------------------

    @Test
    fun `addReferral ignores duplicates by reason`() {
        val vm = buildVm()
        vm.addReferral(referral("TB"))
        vm.addReferral(referral("TB"))
        vm.addReferral(referral("NCD"))

        assertEquals(2, vm.referralList.value?.size)
    }

    @Test
    fun `setReferral keeps the most recent referral`() {
        val vm = buildVm()
        assertNull(vm.referralCache)
        val r = referral("LEPROSY")
        vm.setReferral(r)
        assertEquals(r, vm.referralCache)
    }

    @Test
    fun `completed referrals are tracked per type`() {
        val vm = buildVm()
        assertFalse(vm.isReferralAlreadyDone(CbacViewModel.ReferralType.TB))

        vm.markReferralCompleted(CbacViewModel.ReferralType.TB)
        vm.markReferralCompleted(CbacViewModel.ReferralType.TB)
        vm.markReferralCompleted(CbacViewModel.ReferralType.CANCER)

        assertTrue(vm.isReferralAlreadyDone(CbacViewModel.ReferralType.TB))
        assertTrue(vm.isReferralAlreadyDone(CbacViewModel.ReferralType.CANCER))
        assertFalse(vm.isReferralAlreadyDone(CbacViewModel.ReferralType.COPD))
        assertEquals(2, vm.completedReferrals.value?.size)
    }

    // ----------------------------------------------------------------------------------------
    // submitForm
    // ----------------------------------------------------------------------------------------

    @Test
    fun `submitForm saves a complete form and reports success`() = runTest {
        val cache = fullCbac()
        val vm = editVm(cache)
        advanceUntilIdle()

        vm.setFlagForNcd(true)
        vm.addReferral(referral("NCD"))
        vm.submitForm()
        advanceUntilIdle()

        assertEquals(CbacViewModel.State.SAVE_SUCCESS, vm.state.value)
        assertEquals("Yes", cache.suspected_hrp)
        assertEquals(true, cache.hrp_suspected)
        assertEquals("Yes", cache.suspected_tb)
        assertEquals("Yes", cache.suspected_ncd)
        assertEquals(true, cache.ncd_confirmed)
        assertEquals(true, cache.isReffered)
        assertEquals(555L, cache.cbac_reg_id)
        assertEquals(99, cache.ProviderServiceMapID)
        coVerify { cbacRepo.saveCbacData(cache, any()) }
        coVerify { referalRepo.saveReferedNCD(any()) }
    }

    @Test
    fun `submitForm reports failure when the repository rejects the save`() = runTest {
        coEvery { cbacRepo.saveCbacData(any(), any()) } returns false
        val vm = editVm(fullCbac())
        advanceUntilIdle()

        vm.submitForm()
        advanceUntilIdle()

        assertEquals(CbacViewModel.State.SAVE_FAIL, vm.state.value)
    }

    @Test
    fun `submitForm marks ncd as not suspected when no flag was raised`() = runTest {
        val cache = fullCbac()
        val vm = editVm(cache)
        advanceUntilIdle()

        vm.setFlagForNcd(false)
        vm.setFlagForPhQ2(false)
        vm.submitForm()
        advanceUntilIdle()

        assertEquals(CbacViewModel.State.SAVE_SUCCESS, vm.state.value)
        assertEquals("No", cache.suspected_ncd)
        assertEquals("No", cache.ncd_suspected)
        assertEquals(false, cache.ncd_confirmed)
        assertEquals(false, cache.isReffered)
    }

    @Test
    fun `submitForm marks ncd suspected when only the phq2 flag is raised`() = runTest {
        val cache = fullCbac()
        val vm = editVm(cache)
        advanceUntilIdle()

        vm.setFlagForPhQ2(true)
        vm.submitForm()
        advanceUntilIdle()

        assertEquals("Yes", cache.ncd_suspected)
        assertEquals(true, cache.ncd_confirmed)
    }

    @Test
    fun `submitForm does not suspect hrp for a male beneficiary`() = runTest {
        coEvery { benDao.getBen(any<Long>()) } returns benMock(gender = Gender.MALE)
        val cache = fullCbac()
        val vm = editVm(cache)
        advanceUntilIdle()

        vm.submitForm()
        advanceUntilIdle()

        assertEquals("No", cache.suspected_hrp)
        assertEquals(false, cache.hrp_suspected)
        assertEquals("Yes", cache.suspected_tb)
    }

    @Test
    fun `submitForm does not suspect hrp for reproductive status four`() = runTest {
        coEvery { benDao.getBen(any<Long>()) } returns benMock(reproductiveStatusId = 4)
        val cache = fullCbac()
        val vm = editVm(cache)
        advanceUntilIdle()

        vm.submitForm()
        advanceUntilIdle()

        assertEquals("No", cache.suspected_hrp)
    }

    @Test
    fun `submitForm skips the hrp block for a non reproductive status`() = runTest {
        coEvery { benDao.getBen(any<Long>()) } returns benMock(reproductiveStatusId = 9)
        val cache = fullCbac()
        val vm = editVm(cache)
        advanceUntilIdle()

        vm.submitForm()
        advanceUntilIdle()

        assertEquals("No", cache.suspected_hrp)
        assertEquals(CbacViewModel.State.SAVE_SUCCESS, vm.state.value)
    }

    @Test
    fun `submitForm treats reproductive status two as hrp eligible`() = runTest {
        coEvery { benDao.getBen(any<Long>()) } returns benMock(reproductiveStatusId = 2)
        val cache = fullCbac()
        val vm = editVm(cache)
        advanceUntilIdle()

        vm.submitForm()
        advanceUntilIdle()

        assertEquals(true, cache.hrp_suspected)
        assertEquals("Yes", cache.suspected_hrp)
    }

    @Test
    fun `submitForm treats reproductive status three as hrp eligible`() = runTest {
        coEvery { benDao.getBen(any<Long>()) } returns benMock(reproductiveStatusId = 3)
        val cache = fullCbac()
        val vm = editVm(cache)
        advanceUntilIdle()

        vm.submitForm()
        advanceUntilIdle()

        assertEquals(true, cache.hrp_suspected)
        assertEquals("Yes", cache.suspected_hrp)
    }

    @Test
    fun `submitForm skips the hrp block when reproductive details are absent`() = runTest {
        val b = benMock()
        every { b.genDetails } returns null
        coEvery { benDao.getBen(any<Long>()) } returns b
        val cache = fullCbac()
        val vm = editVm(cache)
        advanceUntilIdle()

        vm.submitForm()
        advanceUntilIdle()

        assertEquals(false, cache.hrp_suspected)
        assertEquals("No", cache.suspected_hrp)
    }

    @Test
    fun `submitForm reports no tb when no tb symptom was recorded`() = runTest {
        val cache = fullCbac().apply {
            cbac_coughing_pos = 2
            cbac_familyhistory_posi = 2
            cbac_tbhistory_pos = 2
            cbac_bloodsputum_pos = 2
            cbac_fivermore_pos = 2
            cbac_loseofweight_pos = 2
            cbac_nightsweats_pos = 2
            cbac_antitbdrugs_pos = 2
            cbac_growth_in_mouth_posi = 2
        }
        val vm = editVm(cache)
        advanceUntilIdle()

        vm.submitForm()
        advanceUntilIdle()

        assertEquals("No", cache.suspected_tb)
    }

    @Test
    fun `submitForm on an unanswered form never reaches the repository`() = runTest {
        val vm = editVm(emptyCbac())
        advanceUntilIdle()

        // dataValid() resolves its message through the localized Resources, which is not available
        // to a JVM unit test; the validation branch itself still runs.
        runCatching { vm.submitForm() }
        advanceUntilIdle()

        assertNotNull(vm.state.value)
    }

    @Test
    fun `submitForm flags every mandatory field individually as the first missing field`() = runTest {
        val fieldMutators = listOf<CbacCache.() -> Unit>(
            { cbac_smoke_posi = 0 },
            { cbac_alcohol_posi = 0 },
            { cbac_waist_posi = 0 },
            { cbac_pa_posi = 0 },
            { cbac_familyhistory_posi = 0 },
            { cbac_sufferingtb_pos = 0 },
            { cbac_antitbdrugs_pos = 0 },
            { cbac_tbhistory_pos = 0 },
            { cbac_coughing_pos = 0 },
            { cbac_bloodsputum_pos = 0 },
            { cbac_fivermore_pos = 0 },
            { cbac_loseofweight_pos = 0 },
            { cbac_nightsweats_pos = 0 },
            { cbac_uicers_pos = 0 },
            { cbac_tingling_palm_posi = 0 },
            { cbac_cloudy_posi = 0 },
            { cbac_diffreading_posi = 0 },
            { cbac_pain_ineyes_posi = 0 },
            { cbac_redness_ineyes_posi = 0 },
            { cbac_diff_inhearing_posi = 0 },
            { cbac_sortnesofbirth_pos = 0 },
            { cbac_historyoffits_pos = 0 },
            { cbac_difficultyinmouth_pos = 0 },
            { cbac_growth_in_mouth_posi = 0 },
            { cbac_toneofvoice_pos = 0 },
            { cbac_white_or_red_patch_posi = 0 },
            { cbac_Pain_while_chewing_posi = 0 },
            { cbac_hyper_pigmented_patch_posi = 0 },
            { cbac_any_thickend_skin_posi = 0 },
            { cbac_nodules_on_skin_posi = 0 },
            { cbac_numbness_on_palm_posi = 0 },
            { cbac_clawing_of_fingers_posi = 0 },
            { cbac_tingling_or_numbness_posi = 0 },
            { cbac_inability_close_eyelid_posi = 0 },
            { cbac_diff_holding_obj_posi = 0 },
            { cbac_weekness_in_feet_posi = 0 },
            { cbac_lumpinbreast_pos = 0 },
            { cbac_blooddischage_pos = 0 },
            { cbac_changeinbreast_pos = 0 },
            { cbac_bleedingbtwnperiods_pos = 0 },
            { cbac_bleedingaftermenopause_pos = 0 },
            { cbac_bleedingafterintercourse_pos = 0 },
            { cbac_foulveginaldischarge_pos = 0 },
            { cbac_feeling_unsteady_posi = 0 },
            { cbac_suffer_physical_disability_posi = 0 },
            { cbac_needing_help_posi = 0 },
            { cbac_forgetting_names_posi = 0 }
        )

        for ((index, mutate) in fieldMutators.withIndex()) {
            val cache = fullCbac().apply(mutate)
            val vm = editVm(cache)
            advanceUntilIdle()

            runCatching { vm.submitForm() }
            advanceUntilIdle()

            assertNotNull("field index $index", vm.state.value)
        }
    }

    @Test
    fun `submitForm hrp check walks every clause of the female symptom or-chain`() = runTest {
        val orderedFields = listOf<(CbacCache, Int) -> Unit>(
            { c, v -> c.cbac_foulveginaldischarge_pos = v },
            { c, v -> c.cbac_sufferingtb_pos = v },
            { c, v -> c.cbac_bleedingafterintercourse_pos = v },
            { c, v -> c.cbac_antitbdrugs_pos = v },
            { c, v -> c.cbac_tbhistory_pos = v },
            { c, v -> c.cbac_historyoffits_pos = v },
            { c, v -> c.cbac_growth_in_mouth_posi = v },
            { c, v -> c.cbac_numbness_on_palm_posi = v },
            { c, v -> c.cbac_clawing_of_fingers_posi = v },
            { c, v -> c.cbac_tingling_or_numbness_posi = v },
            { c, v -> c.cbac_inability_close_eyelid_posi = v },
            { c, v -> c.cbac_diff_holding_obj_posi = v },
            { c, v -> c.cbac_blooddischage_pos = v },
            { c, v -> c.cbac_weekness_in_feet_posi = v },
            { c, v -> c.cbac_sortnesofbirth_pos = v },
            { c, v -> c.cbac_coughing_pos = v },
            { c, v -> c.cbac_bloodsputum_pos = v },
            { c, v -> c.cbac_fivermore_pos = v },
            { c, v -> c.cbac_loseofweight_pos = v },
            { c, v -> c.cbac_nightsweats_pos = v }
        )

        for (triggerIndex in orderedFields.indices) {
            val cache = fullCbac()
            orderedFields.forEachIndexed { i, setter ->
                setter(cache, if (i == triggerIndex) 1 else 2)
            }
            val vm = editVm(cache)
            advanceUntilIdle()

            vm.submitForm()
            advanceUntilIdle()

            assertEquals("trigger=$triggerIndex", true, cache.hrp_suspected)
            assertEquals("trigger=$triggerIndex", CbacViewModel.State.SAVE_SUCCESS, vm.state.value)
        }
    }

    @Test
    fun `submitForm tb suspicion walks every remaining clause of the tb or-chain`() = runTest {
        val orderedFields = listOf<(CbacCache, Int) -> Unit>(
            { c, v -> c.cbac_coughing_pos = v },
            { c, v -> c.cbac_familyhistory_posi = v },
            { c, v -> c.cbac_tbhistory_pos = v },
            { c, v -> c.cbac_bloodsputum_pos = v },
            { c, v -> c.cbac_fivermore_pos = v },
            { c, v -> c.cbac_loseofweight_pos = v },
            { c, v -> c.cbac_nightsweats_pos = v },
            { c, v -> c.cbac_antitbdrugs_pos = v },
            { c, v -> c.cbac_growth_in_mouth_posi = v }
        )

        for (triggerIndex in 1 until orderedFields.size) {
            val cache = fullCbac()
            orderedFields.forEachIndexed { i, setter ->
                setter(cache, if (i == triggerIndex) 1 else 2)
            }
            val vm = editVm(cache)
            advanceUntilIdle()

            vm.submitForm()
            advanceUntilIdle()

            assertEquals("trigger=$triggerIndex", "Yes", cache.suspected_tb)
            assertEquals("trigger=$triggerIndex", CbacViewModel.State.SAVE_SUCCESS, vm.state.value)
        }
    }

    @Test
    fun `submitForm skips the geriatric validation block for a beneficiary under sixty`() = runTest {
        coEvery { benDao.getBen(any<Long>()) } returns benMock(age = 45)
        val cache = fullCbac().apply {
            cbac_feeling_unsteady_posi = 0
            cbac_suffer_physical_disability_posi = 0
            cbac_needing_help_posi = 0
            cbac_forgetting_names_posi = 0
        }
        val vm = editVm(cache)
        advanceUntilIdle()

        vm.submitForm()
        advanceUntilIdle()

        assertEquals(CbacViewModel.State.SAVE_SUCCESS, vm.state.value)
    }

    @Test
    fun `submitForm marks an already processed beneficiary for update`() = runTest {
        val b = benMock()
        every { b.processed } returns "P"
        coEvery { benDao.getBen(any<Long>()) } returns b
        val cache = fullCbac()
        val vm = editVm(cache)
        advanceUntilIdle()

        vm.submitForm()
        advanceUntilIdle()

        verify { b.processed = "U" }
    }

    @Test
    fun `init leaves gender unset when the beneficiary has no recorded gender`() = runTest {
        val b = benMock()
        every { b.gender } returns null
        coEvery { benDao.getBen(any<Long>()) } returns b

        val vm = buildVm(cbacId = 0)
        advanceUntilIdle()

        assertNull(vm.gender.value)
        assertEquals("65 YEARS | null", vm.benAgeGender.value)
    }

    @Test
    fun `init maps age bucket boundaries and out-of-range ages`() = runTest {
        val expectations = listOf(-5 to "4", 0 to "0", 29 to "0", 30 to "1", 39 to "1", 40 to "2", 49 to "2", 50 to "3", 59 to "3", 60 to "4")
        for ((age, expected) in expectations) {
            coEvery { benDao.getBen(any<Long>()) } returns benMock(age = age)
            val vm = buildVm(cbacId = 0)
            advanceUntilIdle()
            assertEquals("age=$age", expected, vm.raAgeScore.latest())
        }
    }

    @Test
    fun `symptom counters ignore an unanswered response`() = runTest {
        val cache = emptyCbac()
        val vm = editVm(cache)
        advanceUntilIdle()

        vm.setCoughing(0)
        vm.setBloodSputum(0)
        vm.setFeverWks(0)
        vm.setLsWt(0)
        vm.setNtSwets(0)
        vm.setFhTb(0)
        vm.setTakingTbDrug(0)
        vm.setUnsteady(0)
        vm.setPdRm(0)
        vm.setNhop(0)
        vm.setForgetNames(0)

        assertEquals(0, vm.ast1.value)
        assertEquals(0, vm.ast2.value)
        assertEquals(0, vm.astMoic.value)
        assertEquals(0, cache.cbac_coughing_pos)
        assertEquals(0, cache.cbac_sufferingtb_pos)
        assertEquals(0, cache.cbac_feeling_unsteady_posi)
    }

    // ----------------------------------------------------------------------------------------
    // localized resources (dataValid messages / score labels)
    // ----------------------------------------------------------------------------------------

    private fun mockLocalizedResources(): Resources {
        val mockResources = mockk<Resources>(relaxed = true)
        mockkConstructor(Configuration::class)
        every { anyConstructed<Configuration>().setLocale(any()) } just Runs
        every { context.resources } returns mockResources
        every { context.createConfigurationContext(any()) } returns context
        return mockResources
    }

    @Test
    fun `raAgeText resolves the localized age bucket label`() = runTest {
        val mockResources = mockLocalizedResources()
        every { mockResources.getStringArray(R.array.cbac_age) } returns
            arrayOf("Below 30", "30-39", "40-49", "50-59", "60 and above")

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals("60 and above", vm.raAgeText.latest())
    }

    @Test
    fun `raTotalScore renders the localized total score label`() = runTest {
        val mockResources = mockLocalizedResources()
        every { mockResources.getString(R.string.total_score_wihout_semi_colon) } returns "Total Score"

        val cache = emptyCbac()
        val vm = editVm(cache)
        advanceUntilIdle()

        vm.setSmoke(1)
        assertEquals("Total Score: 1", vm.raTotalScore.latest())
    }

    @Test
    fun `phq2TotalScore renders the localized total score label`() = runTest {
        val mockResources = mockLocalizedResources()
        every { mockResources.getString(R.string.total_score_wihout_semi_colon) } returns "Total Score"

        val cache = emptyCbac()
        val vm = editVm(cache)
        advanceUntilIdle()

        vm.setLi(1)
        vm.setFd(1)
        assertEquals("Total Score: 2", vm.phq2TotalScore.latest())
    }

    @Test
    fun `dataValid resolves a localized message for every mandatory field in turn`() = runTest {
        val mockResources = mockLocalizedResources()
        every { mockResources.getString(any<Int>()) } returns "required"

        val fieldMutators = listOf<CbacCache.() -> Unit>(
            { cbac_age_posi = 0 },
            { cbac_smoke_posi = 0 },
            { cbac_alcohol_posi = 0 },
            { cbac_waist_posi = 0 },
            { cbac_pa_posi = 0 },
            { cbac_familyhistory_posi = 0 },
            { cbac_sufferingtb_pos = 0 },
            { cbac_antitbdrugs_pos = 0 },
            { cbac_tbhistory_pos = 0 },
            { cbac_coughing_pos = 0 },
            { cbac_bloodsputum_pos = 0 },
            { cbac_fivermore_pos = 0 },
            { cbac_loseofweight_pos = 0 },
            { cbac_nightsweats_pos = 0 },
            { cbac_uicers_pos = 0 },
            { cbac_tingling_palm_posi = 0 },
            { cbac_cloudy_posi = 0 },
            { cbac_diffreading_posi = 0 },
            { cbac_pain_ineyes_posi = 0 },
            { cbac_redness_ineyes_posi = 0 },
            { cbac_diff_inhearing_posi = 0 },
            { cbac_sortnesofbirth_pos = 0 },
            { cbac_historyoffits_pos = 0 },
            { cbac_difficultyinmouth_pos = 0 },
            { cbac_growth_in_mouth_posi = 0 },
            { cbac_toneofvoice_pos = 0 },
            { cbac_white_or_red_patch_posi = 0 },
            { cbac_Pain_while_chewing_posi = 0 },
            { cbac_hyper_pigmented_patch_posi = 0 },
            { cbac_any_thickend_skin_posi = 0 },
            { cbac_nodules_on_skin_posi = 0 },
            { cbac_numbness_on_palm_posi = 0 },
            { cbac_clawing_of_fingers_posi = 0 },
            { cbac_tingling_or_numbness_posi = 0 },
            { cbac_inability_close_eyelid_posi = 0 },
            { cbac_diff_holding_obj_posi = 0 },
            { cbac_weekness_in_feet_posi = 0 },
            { cbac_lumpinbreast_pos = 0 },
            { cbac_blooddischage_pos = 0 },
            { cbac_changeinbreast_pos = 0 },
            { cbac_bleedingbtwnperiods_pos = 0 },
            { cbac_bleedingaftermenopause_pos = 0 },
            { cbac_bleedingafterintercourse_pos = 0 },
            { cbac_foulveginaldischarge_pos = 0 },
            { cbac_feeling_unsteady_posi = 0 },
            { cbac_suffer_physical_disability_posi = 0 },
            { cbac_needing_help_posi = 0 },
            { cbac_forgetting_names_posi = 0 }
        )

        for ((index, mutate) in fieldMutators.withIndex()) {
            val cache = fullCbac().apply(mutate)
            val vm = editVm(cache)
            advanceUntilIdle()

            vm.submitForm()
            advanceUntilIdle()

            assertEquals("field index $index", CbacViewModel.State.MISSING_FIELD, vm.state.value)
            assertEquals("field index $index", "required", vm.missingFieldString)
        }
    }

    private companion object {
        const val BEN_ID = 42L
        const val ASHA_ID = 7
    }
}
