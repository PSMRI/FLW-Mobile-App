package org.piramalswasthya.sakhi.repositories.dynamicRepo

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.TBReferralFollowUpDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.TPTFollowUpDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.model.dynamicEntity.TBReferralFollowUpEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.TPTFollowUpEntity
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response

class TBFollowUpRepositoryTest : BaseRepositoryTest() {

    @Test
    fun tbReferralPull_parsesNestedDataAndStoresFollowUp() = runTest {
        val responseJson = """
            {"data":{"data":[{"benId":474849020466,"userId":4009,
            "houseHoldId":12345,"fields":{"follow_up_date":"2026-09-29 00:00:00",
            "follow_up_status":"RECOMMEND_TPT"}}],"statusCode":200},"statusCode":200}
        """.trimIndent()
        val db = mockk<InAppDb>()
        val dao = mockk<TBReferralFollowUpDao>(relaxed = true)
        val api = mockk<AmritApiService>()
        val preferences = preferencesWithUser()
        val saved = slot<TBReferralFollowUpEntity>()

        every { db.tbReferralFollowUpDao() } returns dao
        every { dao.observeBeneficiariesByFollowUpStatus(any()) } returns emptyFlow()
        coEvery { api.getTBReferralFollowUps(any()) } returns Response.success(ResponseBody.create(null, responseJson))
        coEvery { dao.getByBeneficiaryAndDate(474849020466, "2026-09-29 00:00:00") } returns null
        coEvery { dao.insertOrUpdate(capture(saved)) } returns 1L

        val repository = TBReferralFollowUpRepository(api, preferences, db)

        assertThat(repository.pullFromServer()).isTrue()
        assertThat(saved.captured.benId).isEqualTo(474849020466)
        assertThat(saved.captured.houseHoldId).isEqualTo(12345)
        assertThat(saved.captured.followUpDate).isEqualTo("2026-09-29 00:00:00")
        assertThat(saved.captured.followUpStatus).isEqualTo("RECOMMEND_TPT")
        assertThat(saved.captured.isSynced).isTrue()
    }

    @Test
    fun tptPull_parsesNestedDataMapsDatesAndStoresSyncedVisit() = runTest {
        val responseJson = """
            {"data":{"data":[{"benId":474849020466,"userId":4009,"fields":{
            "regimen_type":"3RH","treatment_start_date":"2026-09-29 00:00:00",
            "expected_treatment_completion_date":"2026-12-29 00:00:00",
            "follow_up_date":"2026-09-30 00:00:00","monthly_follow_up":"Month-1",
            "medicine_adherence":"Regular","any_discomfort":"Yes",
            "treatment_completed":"No","tpt_outcome":""}}],"statusCode":200},"statusCode":200}
        """.trimIndent()
        val db = mockk<InAppDb>()
        val benDao = mockk<BenDao>(relaxed = true)
        val dao = mockk<TPTFollowUpDao>(relaxed = true)
        val api = mockk<AmritApiService>()
        val preferences = preferencesWithUser()
        val saved = slot<TPTFollowUpEntity>()

        every { db.benDao } returns benDao
        every { db.tptFollowUpDao() } returns dao
        coEvery { api.getTPTFollowUps(any()) } returns Response.success(ResponseBody.create(null, responseJson))
        coEvery { dao.getByFollowUpNo(474849020466, 1) } returns null
        coEvery { benDao.getBenById(474849020466) } returns null
        coEvery { dao.upsert(capture(saved)) } returns Unit

        val repository = TPTStartRepository(db, mockk(relaxed = true), api, preferences)

        assertThat(repository.pullFromServer()).isTrue()
        assertThat(saved.captured.benId).isEqualTo(474849020466)
        assertThat(saved.captured.followUpNo).isEqualTo(1)
        assertThat(saved.captured.treatmentType).isEqualTo("3RH")
        assertThat(saved.captured.treatmentStartDate).isEqualTo("29-09-2026")
        assertThat(saved.captured.followUpDate).isEqualTo("30-09-2026")
        assertThat(saved.captured.isSynced).isTrue()
        assertThat(saved.captured.fieldsJson).contains("\"monthly_follow_up\":\"Month-1\"")
    }

    private fun preferencesWithUser(): PreferenceDao {
        val preferences = mockk<PreferenceDao>()
        val user = mockk<User>()
        every { user.userId } returns 4009
        every { preferences.getLoggedInUser() } returns user
        return preferences
    }
}
