package org.piramalswasthya.sakhi.repositories

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.GeneralCheckupDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.GeneralCheckupCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.model.fromApiDate
import org.piramalswasthya.sakhi.model.checkupFromFormRecord
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitRequest
import org.piramalswasthya.sakhi.model.formDataFromJson
import org.piramalswasthya.sakhi.model.monthKey
import org.piramalswasthya.sakhi.model.toApiDate
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response

class GeneralCheckupRepoTest {

    private val dao = mockk<GeneralCheckupDao>()
    private val repo = GeneralCheckupRepo(mockk(), dao, mockk(), mockk(), mockk())

    private val date = 1_791_000_000_000L

    private fun saved(problemIds: List<String>, otherText: String? = null) = GeneralCheckupCache(
        checkupId = "c1", benId = 1L, hhId = 1L, ashaId = 1, checkupDate = date,
        problemTypeId = "g6", problemTypeLabel = "Fever", problemIds = problemIds,
        problemLabels = problemIds, otherText = otherText, priority = "ROUTINE", masterVersion = 1,
        referralReason = "General Check-up: Fever", referralDate = date, referralMonth = monthKey(date),
        createdBy = "asha"
    )

    @Test
    fun `same ben, problem and month is a duplicate`() = runTest {
        coEvery { dao.getCheckupsInMonth(1L, monthKey(date)) } returns listOf(saved(listOf("fever")))
        assertNotNull(repo.findDuplicate(1L, date, listOf("fever", "cough"), emptyList(), null))
    }

    @Test
    fun `different problem in the same month is allowed`() = runTest {
        coEvery { dao.getCheckupsInMonth(1L, monthKey(date)) } returns listOf(saved(listOf("fever")))
        assertNull(repo.findDuplicate(1L, date, listOf("cough"), emptyList(), null))
    }

    @Test
    fun `Other is a duplicate only when the typed text matches`() = runTest {
        coEvery { dao.getCheckupsInMonth(1L, monthKey(date)) } returns
                listOf(saved(listOf("g6_other"), otherText = "Rash"))
        assertNotNull(repo.findDuplicate(1L, date, listOf("g6_other"), listOf("g6_other"), " rash "))
        assertNull(repo.findDuplicate(1L, date, listOf("g6_other"), listOf("g6_other"), "Itching"))
    }

    // ── Sync ─────────────────────────────────────────────────────────────────

    private val syncDao = mockk<GeneralCheckupDao>(relaxed = true)
    private val api = mockk<AmritApiService>()
    private val preferences = mockk<PreferenceDao>().also { prefs ->
        val user = mockk<User>()
        every { user.userId } returns 9876
        every { user.userName } returns "asha"
        every { prefs.getLoggedInUser() } returns user
    }
    private val syncRepo = GeneralCheckupRepo(mockk(), syncDao, api, preferences, mockk())

    private fun ok(json: String) = Response.success(json.toResponseBody(null))

    private fun pending(vararg ids: String) {
        coEvery { syncDao.getCheckupsBySyncState(SyncState.UNSYNCED) } returns
                ids.map { saved(listOf("g6_fever")).copy(checkupId = it) }
    }

    @Test
    fun `push marks saved and rejected rows synced and returns the rest`() = runTest {
        pending("a", "b", "c")
        coEvery { api.saveGeneralCheckups(any()) } returns ok(
            """{"statusCode":200,"status":"Success","data":{
               "saved":[{"checkupId":"a","serverCheckupId":1001}],
               "rejected":[{"checkupId":"b","reason":"DUPLICATE"}]}}"""
        )

        assertFalse(syncRepo.pushUnsynced())
        coVerify { syncDao.moveCheckupSyncState(listOf("a", "b"), SyncState.SYNCING, SyncState.SYNCED) }
        coVerify { syncDao.moveCheckupSyncState(listOf("c"), SyncState.SYNCING, SyncState.UNSYNCED) }
    }

    @Test
    fun `push sends the generic dynamic-form submit for general_check_up`() = runTest {
        coEvery { syncDao.getCheckupsBySyncState(SyncState.UNSYNCED) } returns listOf(
            saved(listOf("g6_fever", "g6_other"), otherText = "Rash").copy(
                checkupId = "a", benId = 55L, hhId = 66L,
                formData = """{"checkup_date":"10-10-2026","problem_type":"g6","problems_g6":"g6_fever,g6_other","other_text":"Rash"}"""
            )
        )
        val sent = slot<List<org.piramalswasthya.sakhi.model.dynamicEntity.FormSubmitRequest>>()
        coEvery { api.saveGeneralCheckups(capture(sent)) } returns ok("""{"statusCode":200,"data":"saved"}""")

        assertTrue(syncRepo.pushUnsynced())

        val request = sent.captured.single()
        assertEquals("general_check_up", request.formId)
        assertEquals("asha", request.userName)
        assertEquals(55L, request.beneficiaryId)
        assertEquals(66L, request.houseHoldId)
        assertEquals(java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.ENGLISH).format(date), request.visitDate)
        assertNull(request.id)
        with(request.fields) {
            assertEquals("g6", this["problem_type"])
            assertEquals("g6_fever,g6_other", this["problems_g6"])
            assertEquals("Rash", this["other_text"])
            assertEquals("a", this["checkupId"])
            assertEquals("Rash", this["otherText"])
            assertEquals("ROUTINE", this["priority"])
            assertEquals("General Check-up: Fever", this["referralReason"])
            assertEquals(toApiDate(date), this["referralDate"])
        }
    }

    @Test
    fun `push without a per-record result marks the whole batch synced`() = runTest {
        pending("a", "b")
        coEvery { api.saveGeneralCheckups(any()) } returns ok("""{"status":"SUCCESS","data":null}""")

        assertTrue(syncRepo.pushUnsynced())
        coVerify { syncDao.moveCheckupSyncState(listOf("a", "b"), SyncState.SYNCING, SyncState.SYNCED) }
    }

    @Test
    fun `failed push returns every row to unsynced`() = runTest {
        pending("a")
        coEvery { api.saveGeneralCheckups(any()) } returns
                ok("""{"statusCode":5000,"status":"Failure","errorMessage":"boom"}""")

        assertFalse(syncRepo.pushUnsynced())
        coVerify { syncDao.moveCheckupSyncState(listOf("a"), SyncState.SYNCING, SyncState.UNSYNCED) }
    }

    @Test
    fun `pull stores rows for known beneficiaries as synced and skips broken rows`() = runTest {
        coEvery { api.getAllGeneralCheckups(any()) } returns ok(
            """{"statusCode":200,"status":"Success","data":[
               {"id":1,"beneficiaryId":1,"houseHoldId":10,"visitDate":"08-10-2026","fields":{
                "checkup_date":"08-10-2026","problem_type":"g6","problems_g6":"g6_fever",
                "checkupId":"a","ashaId":9876,"checkupDate":"2026-10-08",
                "problemTypeId":"g6","problemTypeLabel":"Fever, cough and common illness",
                "problems":[{"problemId":"g6_fever","label":"Fever"}],"otherText":null,
                "priority":"ROUTINE","masterVersion":2,"referralReason":"General Check-up: Fever",
                "referralDate":"2026-10-08","createdBy":"asha","createdDate":1,"updatedDate":1}},
               {"id":2,"beneficiaryId":2,"houseHoldId":20,"visitDate":"09-10-2026","fields":{
                "checkupId":"b","checkupDate":"2026-10-09","problemTypeId":"g6","problemTypeLabel":"x",
                "problems":[],"referralReason":"r","referralDate":"2026-10-09"}},
               {"id":3,"beneficiaryId":1,"houseHoldId":10,"visitDate":"x","fields":{
                "checkupId":"c","checkupDate":"not-a-date","problemTypeId":"g6","problemTypeLabel":"x",
                "referralReason":"r","referralDate":"x"}},
               {"id":4,"beneficiaryId":1,"houseHoldId":10,"visitDate":"x"}]}"""
        )
        coEvery { syncDao.getExistingBenIds(any()) } returns listOf(1L)
        val inserted = slot<List<GeneralCheckupCache>>()
        coEvery { syncDao.insertCheckupsIfAbsent(capture(inserted)) } returns Unit

        assertTrue(syncRepo.pullFromServer())
        val row = inserted.captured.single()
        assertEquals("a", row.checkupId)
        assertEquals(1L, row.benId)
        assertEquals(10L, row.hhId)
        assertEquals(listOf("g6_fever"), row.problemIds)
        assertEquals(2, row.masterVersion)
        assertEquals(fromApiDate("2026-10-08"), row.checkupDate)
        assertEquals(monthKey(fromApiDate("2026-10-08")!!), row.referralMonth)
        assertEquals(SyncState.SYNCED, row.syncState)
        assertEquals(
            mapOf("checkup_date" to "08-10-2026", "problem_type" to "g6", "problems_g6" to "g6_fever"),
            formDataFromJson(row.formData!!)
        )
    }

    @Test
    fun `pull sends the dynamic-form getAll request`() = runTest {
        val request = slot<HBNCVisitRequest>()
        coEvery { api.getAllGeneralCheckups(capture(request)) } returns ok("""{"statusCode":200,"data":[]}""")

        assertTrue(syncRepo.pullFromServer())
        assertEquals(9876, request.captured.ashaId)
        assertEquals("asha", request.captured.userName)
        assertEquals(0, request.captured.pageNo)
    }

    @Test
    fun `a pushed check-up reads back unchanged from getAll`() = runTest {
        val original = saved(listOf("g6_fever", "g6_other"), otherText = "Rash").copy(
            checkupId = "rt", benId = 1L, hhId = 10L, ashaId = 9876, masterVersion = 2,
            // The API carries dates without time; the form saves them at local midnight.
            checkupDate = fromApiDate("2026-10-08")!!, referralDate = fromApiDate("2026-10-08")!!,
            formData = """{"problem_type":"g6","problems_g6":"g6_fever,g6_other","other_text":"Rash"}"""
        )
        val record = com.google.gson.Gson().toJsonTree(original.asSubmitRequest("asha")).asJsonObject
        val back = checkupFromFormRecord(record)!!
        assertEquals(original.copy(id = 0, syncState = SyncState.SYNCED), back.copy(id = 0))
    }

    @Test
    fun `a server record with null label, priority and masterVersion is still read`() {
        val record = com.google.gson.JsonParser.parseString(
            """{"id":1,"beneficiaryId":701431633324,"houseHoldId":13102026132429977,"visitDate":"20-10-2026",
            "fields":{"checkupId":"1","ashaId":977,"checkupDate":"2026-10-20","problemTypeId":"g4",
            "problemTypeLabel":null,"problems":[{"problemId":"g12_mood","label":"Feeling sad or low for many days"},
            {"problemId":"g12_fits","label":"Fits"}],"otherText":null,"priority":null,"masterVersion":null,
            "referralReason":"General Check-up: Feeling sad or low for many days, Fits","referralDate":"2026-10-20",
            "createdBy":"Ritatanti","createdDate":1792500619000,"updatedDate":1792500619000,
            "check_up_date":"20-10-2026","problem_type":"g4","problems_g6":null,"other_text":null}}"""
        ).asJsonObject

        val back = checkupFromFormRecord(record)!!

        assertEquals("1", back.checkupId)
        assertEquals(701431633324L, back.benId)
        assertEquals("", back.problemTypeLabel)
        assertEquals("ROUTINE", back.priority)
        assertEquals(0, back.masterVersion)
        assertEquals(listOf("g12_mood", "g12_fits"), back.problemIds)
    }

    @Test
    fun `failed pull stores nothing`() = runTest {
        coEvery { api.getAllGeneralCheckups(any()) } returns
                ok("""{"statusCode":5000,"status":"Failure","errorMessage":"boom"}""")

        assertFalse(syncRepo.pullFromServer())
        coVerify(exactly = 0) { syncDao.insertCheckupsIfAbsent(any()) }
    }
}
