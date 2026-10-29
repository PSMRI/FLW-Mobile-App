package org.piramalswasthya.sakhi.repositories.dynamicRepo

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.JsonParser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.GCReferralFollowUpDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.model.dynamicEntity.GCReferralFollowUpEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.GCReferralFollowUpRequest
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response

/** generalCheckUpFollowUp/saveAll and /getAll contract (FLW-1229). */
class GCReferralFollowUpRepositoryTest : BaseRepositoryTest() {

    private val db = mockk<InAppDb>()
    private val dao = mockk<GCReferralFollowUpDao>(relaxed = true)
    private val api = mockk<AmritApiService>()

    init {
        every { db.gcReferralFollowUpDao() } returns dao
    }

    @Test
    fun push_sendsAllUnsyncedAsOneListInServerShape() = runTest {
        coEvery { dao.getUnsynced() } returns listOf(
            entity(7, 257306133079, """{"follow_up_date":"2026-10-28","followup_status":"referred_to_higher_facility","referred_to":"PHC"}"""),
            entity(8, 257306133080, """{"follow_up_date":"2026-10-28","followup_status":"medicines_given"}""")
        )
        val sent = slot<List<GCReferralFollowUpRequest>>()
        coEvery { api.saveGCReferralFollowUp(capture(sent)) } returns
                Response.success(ResponseBody.create(null, """{"statusCode":200}"""))

        assertThat(GCReferralFollowUpRepository(api, preferencesWithUser(), db).pushPending()).isTrue()

        // Same serializer as the gsonAmritApi Retrofit converter.
        val first = JsonParser.parseString(Gson().toJson(sent.captured)).asJsonArray[0].asJsonObject
        assertThat(sent.captured).hasSize(2)
        assertThat(first.get("id").asLong).isEqualTo(0)
        assertThat(first.get("beneficiaryId").asLong).isEqualTo(257306133079)
        assertThat(first.get("houseHoldId").asLong).isEqualTo(10537228)
        assertThat(first.get("visitDate").asString).isEqualTo("2026-10-28")
        assertThat(first.has("benId")).isFalse()
        val fields = first.getAsJsonObject("fields")
        assertThat(fields.get("followup_status").asString).isEqualTo("referred_to_higher_facility")
        assertThat(fields.get("referred_to").asString).isEqualTo("PHC")
        coVerify { dao.markSynced(7, any()) }
        coVerify { dao.markSynced(8, any()) }
    }

    @Test
    fun push_failureKeepsRecordsUnsynced() = runTest {
        coEvery { dao.getUnsynced() } returns listOf(entity(7, 257306133079, """{"follow_up_date":"2026-10-28"}"""))
        coEvery { api.saveGCReferralFollowUp(any()) } returns
                Response.success(ResponseBody.create(null, """{"statusCode":5000,"errorMessage":"x"}"""))

        assertThat(GCReferralFollowUpRepository(api, preferencesWithUser(), db).pushPending()).isFalse()
        coVerify(exactly = 0) { dao.markSynced(any(), any()) }
    }

    @Test
    fun pull_parsesBareArrayResponse() = runTest {
        val responseJson = """
            [{"id":44,"beneficiaryId":257306133079,"houseHoldId":10537228,"visitDate":"2026-10-28",
              "fields":{"follow_up_date":"2026-10-28","followup_status":"referred_to_higher_facility","referred_to":"PHC"}},
             {"id":45,"beneficiaryId":257306133080,"houseHoldId":10537229,"visitDate":"2026-10-28",
              "fields":{"follow_up_date":"2026-10-28","followup_status":"medicines_given","referred_to":null}}]
        """.trimIndent()
        coEvery { api.getGCReferralFollowUps(any()) } returns Response.success(ResponseBody.create(null, responseJson))
        coEvery { dao.getByBeneficiaryAndDate(any(), any()) } returns null
        val saved = mutableListOf<GCReferralFollowUpEntity>()
        coEvery { dao.insertOrUpdate(capture(saved)) } returns 1L

        assertThat(GCReferralFollowUpRepository(api, preferencesWithUser(), db).pullFromServer()).isTrue()

        assertThat(saved).hasSize(2)
        assertThat(saved[0].benId).isEqualTo(257306133079)
        assertThat(saved[0].houseHoldId).isEqualTo(10537228)
        assertThat(saved[0].followUpDate).isEqualTo("2026-10-28")
        assertThat(saved[0].followUpStatus).isEqualTo("referred_to_higher_facility")
        assertThat(saved[0].fieldsJson).contains("\"referred_to\":\"PHC\"")
        assertThat(saved[0].isSynced).isTrue()
        assertThat(saved[1].followUpStatus).isEqualTo("medicines_given")
    }

    @Test
    fun pull_keepsLocalUnsyncedEdit() = runTest {
        val responseJson = """[{"id":44,"beneficiaryId":257306133079,"houseHoldId":10537228,"visitDate":"2026-10-28",
            "fields":{"follow_up_date":"2026-10-28","followup_status":"treated_at_hwc"}}]"""
        coEvery { api.getGCReferralFollowUps(any()) } returns Response.success(ResponseBody.create(null, responseJson))
        coEvery { dao.getByBeneficiaryAndDate(257306133079, "2026-10-28") } returns
                entity(7, 257306133079, """{"follow_up_date":"2026-10-28"}""")

        assertThat(GCReferralFollowUpRepository(api, preferencesWithUser(), db).pullFromServer()).isTrue()
        coVerify(exactly = 0) { dao.insertOrUpdate(any()) }
    }

    private fun entity(id: Long, benId: Long, fieldsJson: String) = GCReferralFollowUpEntity(
        id = id,
        benId = benId,
        houseHoldId = if (benId == 257306133079) 10537228 else 10537229,
        referredOnDate = "2026-10-20",
        followUpDate = "2026-10-28",
        followUpStatus = null,
        fieldsJson = fieldsJson,
        isSynced = false
    )

    private fun preferencesWithUser(): PreferenceDao {
        val preferences = mockk<PreferenceDao>()
        val user = mockk<User>()
        every { user.userId } returns 4009
        every { preferences.getLoggedInUser() } returns user
        return preferences
    }
}
