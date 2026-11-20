package org.piramalswasthya.sakhi.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.di.AppModule
import org.piramalswasthya.sakhi.model.HighRiskAssessDTO
import java.lang.reflect.ParameterizedType

class HRPSyncSerializationTest {

    private val moshi: Moshi = AppModule.provideMoshiInstance()

    private fun <T : Any> serializeRequest(entryType: Class<T>, entry: T): String {
        val requestType = Types.newParameterizedType(UserDataDTO::class.java, entryType)
        val request = UserDataDTO(userId = 17, entries = listOf(entry))
        return moshi.adapter<UserDataDTO<T>>(requestType).toJson(request)
    }

    private fun assertEndpointEntryType(methodName: String, expectedType: Class<*>) {
        val method = AmritApiService::class.java.declaredMethods
            .single { it.name == methodName }
        val requestType = method.genericParameterTypes
            .filterIsInstance<ParameterizedType>()
            .single { it.rawType == UserDataDTO::class.java }
        assertEquals(expectedType, (requestType.actualTypeArguments.single() as Class<*>))
    }

    @Test
    fun `high risk assessment payload serializes with generated adapter`() {
        val json = serializeRequest(
            HighRiskAssessDTO::class.java,
            HighRiskAssessDTO(benId = 101L, noOfDeliveries = "Yes")
        )

        assertTrue(json.contains("\"userId\":17"))
        assertTrue(json.contains("\"benId\":101"))
        assertTrue(json.contains("\"noOfDeliveries\":\"Yes\""))
        assertEndpointEntryType("saveHighRiskAssessData", HighRiskAssessDTO::class.java)
    }

    @Test
    fun `pregnant follow up payload serializes with generated adapter`() {
        val json = serializeRequest(
            HRPPregnantTrackDTO::class.java,
            HRPPregnantTrackDTO(benId = 102L, visitDate = "2026-11-20T00:00:00.000Z", visit = "1st ANC")
        )

        assertTrue(json.contains("\"benId\":102"))
        assertTrue(json.contains("\"visit\":\"1st ANC\""))
        assertEndpointEntryType("saveHRPTrackData", HRPPregnantTrackDTO::class.java)
    }

    @Test
    fun `non pregnant follow up payload serializes with generated adapter`() {
        val json = serializeRequest(
            HRPNonPregnantTrackDTO::class.java,
            HRPNonPregnantTrackDTO(
                benId = 103L,
                visitDate = "2026-11-20T00:00:00.000Z",
                lmp = null
            )
        )

        assertTrue(json.contains("\"benId\":103"))
        assertEndpointEntryType("saveHRNonPTrackData", HRPNonPregnantTrackDTO::class.java)
    }
}
