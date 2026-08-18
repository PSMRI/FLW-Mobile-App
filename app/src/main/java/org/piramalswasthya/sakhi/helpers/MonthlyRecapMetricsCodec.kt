package org.piramalswasthya.sakhi.helpers

import com.google.gson.Gson
import org.piramalswasthya.sakhi.model.MonthlyRecapMetricsContract
import org.piramalswasthya.sakhi.model.MonthlyRecapMetricsPayload
import timber.log.Timber

/**
 * Encodes/decodes the aggregate Monthly Recap metrics payload stored in
 * MonthlyRecapCache.metricsJson (Gson — the project's existing JSON library).
 *
 * Decoding is defensive:
 * - corrupt/foreign JSON -> null (never crashes),
 * - an unsupported schema version -> null (never silently reinterpreted as the
 *   current schema),
 * - logs stay free of any record-level/health data (the payload is aggregates
 *   only, and we do not log its contents).
 *
 * A null decode is treated by the repository as "no valid metric yet" and leads
 * to a deterministic regeneration from local data.
 */
object MonthlyRecapMetricsCodec {

    private val gson = Gson()

    fun encode(payload: MonthlyRecapMetricsPayload): String = gson.toJson(payload)

    fun decodeOrNull(json: String?): MonthlyRecapMetricsPayload? {
        if (json.isNullOrBlank()) return null
        return try {
            val payload = gson.fromJson(json, MonthlyRecapMetricsPayload::class.java)
            when {
                payload == null -> null
                payload.payloadSchemaVersion != MonthlyRecapMetricsContract.PAYLOAD_SCHEMA_VERSION -> {
                    Timber.w(
                        "Monthly Recap: unsupported metrics schema version %d (expected %d); ignoring",
                        payload.payloadSchemaVersion,
                        MonthlyRecapMetricsContract.PAYLOAD_SCHEMA_VERSION,
                    )
                    null
                }

                else -> payload
            }
        } catch (e: Exception) {
            // Deliberately do NOT log the JSON body.
            Timber.w(e, "Monthly Recap: corrupt metrics payload; will regenerate")
            null
        }
    }
}
