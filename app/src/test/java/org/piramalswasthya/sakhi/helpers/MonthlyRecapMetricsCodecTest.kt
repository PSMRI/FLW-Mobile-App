package org.piramalswasthya.sakhi.helpers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.piramalswasthya.sakhi.model.MonthlyRecapMetricsContract
import org.piramalswasthya.sakhi.model.MonthlyRecapMetricsPayload
import org.piramalswasthya.sakhi.model.RecapActivityMetric
import org.piramalswasthya.sakhi.model.RecapCategoryMetric
import org.piramalswasthya.sakhi.model.RecapCountingUnit

class MonthlyRecapMetricsCodecTest {

    private fun payload(count: Int = 5) = MonthlyRecapMetricsPayload(
        payloadSchemaVersion = MonthlyRecapMetricsContract.PAYLOAD_SCHEMA_VERSION,
        calculationVersion = MonthlyRecapMetricsContract.CALCULATION_VERSION,
        recapYearMonth = 202606,
        generatedAt = 1_780_000_000_000L,
        windowStartMillis = 1_000L,
        windowEndMillisExclusive = 2_000L,
        categories = listOf(
            RecapCategoryMetric.from(
                MonthlyRecapMetricsContract.CATEGORY_NCD,
                listOf(
                    RecapActivityMetric(
                        MonthlyRecapMetricsContract.ACTIVITY_CBAC_SCREENINGS,
                        RecapCountingUnit.EVENT.name,
                        count,
                    )
                ),
            )
        ),
    )

    @Test
    fun `encode then decode round-trips to an equal payload`() {
        val original = payload(7)
        val decoded = MonthlyRecapMetricsCodec.decodeOrNull(MonthlyRecapMetricsCodec.encode(original))
        assertEquals(original, decoded)
    }

    @Test
    fun `null or blank json decodes to null`() {
        assertNull(MonthlyRecapMetricsCodec.decodeOrNull(null))
        assertNull(MonthlyRecapMetricsCodec.decodeOrNull(""))
        assertNull(MonthlyRecapMetricsCodec.decodeOrNull("   "))
    }

    @Test
    fun `corrupt json decodes to null without throwing`() {
        assertNull(MonthlyRecapMetricsCodec.decodeOrNull("{not valid json"))
        assertNull(MonthlyRecapMetricsCodec.decodeOrNull("[]garbage"))
    }

    @Test
    fun `unsupported schema version decodes to null (never reinterpreted)`() {
        val unsupported = """
            {"payloadSchemaVersion":999,"calculationVersion":2,"recapYearMonth":202606,
             "generatedAt":0,"windowStartMillis":0,"windowEndMillisExclusive":0,"categories":[]}
        """.trimIndent()
        assertNull(MonthlyRecapMetricsCodec.decodeOrNull(unsupported))
    }
}
