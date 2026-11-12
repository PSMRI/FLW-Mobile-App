package org.piramalswasthya.sakhi.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.ReferalCache
import org.piramalswasthya.sakhi.model.ReferralRequest

/** The referral push body sends benVisitID / visitCode as null (not 0, not missing) when there is no visit. */
class SerializeNullTest {

    // Same setup as AppModule.provideMoshiInstance().
    private val moshi = Moshi.Builder()
        .add(SerializeNullAdapterFactory)
        .add(KotlinJsonAdapterFactory())
        .build()

    private fun toJson(referral: ReferalCache): String =
        moshi.adapter(ReferralRequest::class.java).toJson(ReferralRequest(referral.toDTO()))

    @Test
    fun `new referral defaults to no visit`() {
        val referral = ReferalCache(benId = 1L, syncState = SyncState.UNSYNCED)
        assertEquals(null, referral.benVisitID)
        assertEquals(null, referral.visitCode)
    }

    @Test
    fun `missing visit is sent as explicit null`() {
        val json = toJson(ReferalCache(benId = 1L, syncState = SyncState.UNSYNCED))
        assertTrue(json, json.contains("\"benVisitID\":null"))
        assertTrue(json, json.contains("\"visitCode\":null"))
    }

    @Test
    fun `real visit is sent as number`() {
        val json = toJson(
            ReferalCache(benId = 1L, syncState = SyncState.UNSYNCED, benVisitID = 55L, visitCode = 3001L)
        )
        assertTrue(json, json.contains("\"benVisitID\":55"))
        assertTrue(json, json.contains("\"visitCode\":3001"))
    }

    @Test
    fun `other null fields are still left out`() {
        val json = toJson(ReferalCache(benId = 1L, syncState = SyncState.UNSYNCED, referredToInstituteName = null))
        assertFalse(json, json.contains("referredToInstituteName"))
    }
}
