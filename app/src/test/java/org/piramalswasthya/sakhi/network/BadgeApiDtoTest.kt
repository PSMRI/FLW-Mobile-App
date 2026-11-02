package org.piramalswasthya.sakhi.network

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Test

/**
 * The restore path is the one that makes badges appear on a second device, and it is
 * all-or-nothing: Moshi throws on the first bad row, so one unexpected field shape
 * loses the whole award log rather than one award.
 *
 * The server in FLW-API #341 declares award_key as NOT NULL with a "" default, so it
 * sends a string today. These cases cover what it would take for that to change —
 * an older deployment without the column, or one that starts allowing nulls — because
 * the app cannot pick which server build it meets.
 */
class BadgeApiDtoTest {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(BadgeEarnedResponse::class.java)

    @Test
    fun `earned list parses the award key the server sends today`() {
        val json = """
            {"earned":[
              {"badgeId":"timely_reporter","level":1,"earnedAt":1790000000000,"awardKey":""},
              {"badgeId":"vulnerable_baby","level":1,"earnedAt":1790000000001,"awardKey":"abc123digest"}
            ],"statusCode":200,"status":"Success"}
        """.trimIndent()

        val earned = adapter.fromJson(json)?.earned

        assertThat(earned).hasSize(2)
        assertThat(earned!![0].awardKey).isEmpty()
        assertThat(earned[1].awardKey).isEqualTo("abc123digest")
    }

    @Test
    fun `an explicit null award key does not fail the whole restore`() {
        val json = """
            {"earned":[
              {"badgeId":"steady_syncer","level":2,"earnedAt":1790000000002,"awardKey":null},
              {"badgeId":"maternal_journey","level":1,"earnedAt":1790000000003,"awardKey":"q3"}
            ],"statusCode":200,"status":"Success"}
        """.trimIndent()

        val earned = adapter.fromJson(json)?.earned

        // both rows survive: the null one is the award it would previously have thrown on
        assertThat(earned).hasSize(2)
        assertThat(earned!![0].awardKey).isNull()
        assertThat(earned[1].awardKey).isEqualTo("q3")
    }

    @Test
    fun `a server predating the award key field still deserialises`() {
        val json = """
            {"earned":[{"badgeId":"complete_worker","level":1,"earnedAt":1790000000004}],
             "statusCode":200,"status":"Success"}
        """.trimIndent()

        val earned = adapter.fromJson(json)?.earned

        assertThat(earned).hasSize(1)
        assertThat(earned!![0].awardKey).isNull()
    }

    @Test
    fun `push payload keeps the award key that separates two awards of one level`() {
        val push = BadgeEarnedPush(
            userId = 4321,
            badges = listOf(
                BadgeEarnedDTO("community_voice", 1, 1790000000005, "2026-Q2"),
                BadgeEarnedDTO("community_voice", 1, 1790000000006, "2026-Q3"),
            )
        )

        val json = moshi.adapter(BadgeEarnedPush::class.java).toJson(push)

        assertThat(json).contains("2026-Q2")
        assertThat(json).contains("2026-Q3")
    }
}
