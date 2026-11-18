package org.piramalswasthya.sakhi.helpers

import com.squareup.moshi.Moshi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.model.UserNetworkResponse

/** FLW-1251: reading the `gamification` object of getUserDetail. */
class GamificationAccessParserTest {

    private val moshi = Moshi.Builder().build()

    /** Parses a full getUserDetail body the way Retrofit does, and returns `data.gamification`. */
    private fun rawFromResponse(gamificationJson: String?): Any? {
        val extra = gamificationJson?.let { ""","gamification":$it""" } ?: ""
        val body = """
            {"success":true,"message":null,"data":{
              "userId":7,"name":"A","userName":"a","stateId":1,"stateName":"S",
              "serviceProviderId":1,"roleId":1,"roleName":"ASHA","providerServiceMapId":1,
              "blockId":1,"blockName":"B","villageId":"1","villageName":"V"$extra}}
        """.trimIndent()
        return moshi.adapter(UserNetworkResponse::class.java).fromJson(body)!!.data.gamification
    }

    @Test
    fun `full object is read`() {
        val raw = rawFromResponse(
            """{"enabled":true,"mechanics":{"badges":true,"monthlyRecap":false,
               "eveningNotifications":true,"consistencyDashboard":false},
               "source":"BLOCK","updatedAt":1762156800000}"""
        )

        val access = GamificationAccessParser.parse(raw, 7)!!

        assertEquals(7, access.userId)
        assertTrue(access.enabled)
        assertTrue(access.badges)
        assertFalse(access.monthlyRecap)
        assertTrue(access.eveningNotifications)
        assertFalse(access.consistencyDashboard)
        assertEquals("BLOCK", access.source)
        assertEquals(1762156800000L, access.updatedAt)
    }

    @Test
    fun `missing object means no decision`() {
        assertNull(GamificationAccessParser.parse(rawFromResponse(null), 7))
    }

    @Test
    fun `wrong types never break the login response and give no decision`() {
        // Each of these must still parse as a getUserDetail response (login keeps working).
        listOf("\"yes\"", "42", "[]", "null", """{"enabled":"true"}""", "{}").forEach { json ->
            assertNull(json, GamificationAccessParser.parse(rawFromResponse(json), 7))
        }
    }

    @Test
    fun `missing or non boolean mechanic switches count as off`() {
        val raw = rawFromResponse("""{"enabled":true,"mechanics":{"badges":"true","monthlyRecap":1}}""")

        val access = GamificationAccessParser.parse(raw, 7)!!

        assertFalse(access.badges)
        assertFalse(access.monthlyRecap)
        assertFalse(access.eveningNotifications)
    }

    @Test
    fun `explicit off from server is kept as a decision`() {
        val access = GamificationAccessParser.parse(
            rawFromResponse("""{"enabled":false,"mechanics":{},"source":"DEFAULT"}"""), 7
        )

        assertNotNull(access)
        assertFalse(access!!.enabled)
        assertEquals("DEFAULT", access.source)
    }
}
