package org.piramalswasthya.sakhi.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSubmitRequest
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * A problem type of the Add Check-up form. Built from the server-driven `general_check_up` schema
 * (the `problem_type` dropdown and one multicheckbox per type); see GeneralCheckupFormSchema.
 */
data class HealthProblemType(
    val typeId: String,
    /** Label in the user's language, as sent by the server. */
    val label: String,
    /** English label: stored on the check-up and used in the referral reason. */
    val labelEn: String,
    /** fieldId of this type's multicheckbox in the schema. */
    val problemFieldId: String,
    val problems: List<HealthProblem>
)

data class HealthProblem(
    val problemId: String,
    val label: String,
    val labelEn: String,
    val emergency: Boolean = false,
    /** Null for a general problem; otherwise the module that owns it (FR6). */
    val ownerModule: HealthProblemOwnerModule? = null,
    /** True for "Other": opens the mandatory free-text box. */
    val needsText: Boolean = false
)

enum class HealthProblemOwnerModule {
    MATERNAL, CHILD, ECL, CATARACT
}

enum class CheckupPriority {
    ROUTINE, EMERGENCY
}

// ─────────────────────────────────────────────────────────────────────────────
// Room: one General Check-up episode (BRD §9 data fields)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * One check-up. A check-up is saved only together with its GC referral (one episode, one referral).
 *
 * The referral itself lives in NCD_REFER (type = GENERAL_CHECKUP). It is linked by
 * (benId, referralReason, referralMonth), which is the NCD_REFER unique key; the NCD_REFER row id
 * is not stable because the referral pull re-inserts rows.
 */
@Entity(
    tableName = "GENERAL_CHECKUP",
    foreignKeys = [ForeignKey(
        entity = BenRegCache::class,
        parentColumns = arrayOf("beneficiaryId"),
        childColumns = arrayOf("benId"),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index(name = "ind_gc_checkup_id", value = ["checkupId"], unique = true),
        Index(name = "ind_gc_ben", value = ["benId"]),
    ]
)
data class GeneralCheckupCache(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** Client-generated UUID, so a re-push or a pull never duplicates the record. */
    val checkupId: String,
    val benId: Long,
    val hhId: Long,
    val ashaId: Int,
    val checkupDate: Long,
    val problemTypeId: String,
    /** Label shown to the ASHA at the time of the check-up (snapshot, English). */
    val problemTypeLabel: String,
    val problemIds: List<String>,
    /** Labels shown at the time of the check-up (snapshot, English), same order as [problemIds]. */
    val problemLabels: List<String>,
    val otherText: String?,
    /** [CheckupPriority] name. */
    val priority: String,
    /** Version of the `general_check_up` form schema the check-up was filled with. */
    val masterVersion: Int,
    val referralReason: String,
    val referralDate: Long,
    /** yyyyMM of [referralDate]; part of the link to NCD_REFER and the dedup key. */
    val referralMonth: Int,
    /**
     * JSON object of every answered form field (fieldId → value), so fields the server adds to
     * the schema later are kept and synced without an app change. Null for pulled old records.
     */
    val formData: String? = null,
    val createdBy: String,
    val createdDate: Long = System.currentTimeMillis(),
    var updatedDate: Long = System.currentTimeMillis(),
    var syncState: SyncState = SyncState.UNSYNCED
) {
    /**
     * One item of flw-api/disease/generalCheckUp/saveAll: the generic dynamic-form submit.
     * `fields` = the form answers (formData, keyed by schema fieldId) plus the check-up values the app
     * derives; those use the same names and formats as [GeneralCheckupDTO], read back by [checkupFromFormRecord].
     */
    fun asSubmitRequest(userName: String) = FormSubmitRequest(
        userName = userName,
        formId = FormConstants.GENERAL_CHECKUP_FORM_ID,
        beneficiaryId = benId,
        houseHoldId = hhId,
        visitDate = SimpleDateFormat(FORM_DATE, Locale.ENGLISH).format(checkupDate),
        fields = formData?.let { formDataFromJson(it) }.orEmpty() + mapOf(
            // Keys must stay in CHECKUP_DETAIL_KEYS, which the download uses to split them from the answers.
            "checkupId" to checkupId,
            "ashaId" to ashaId,
            "checkupDate" to toApiDate(checkupDate),
            "problemTypeId" to problemTypeId,
            "problemTypeLabel" to problemTypeLabel,
            "problems" to problemIds.mapIndexed { i, id ->
                GeneralCheckupProblemDTO(problemId = id, label = problemLabels.getOrNull(i).orEmpty())
            },
            "otherText" to otherText,
            "priority" to priority,
            "masterVersion" to masterVersion,
            "referralReason" to referralReason,
            "referralDate" to toApiDate(referralDate),
            "createdBy" to createdBy,
            "createdDate" to createdDate,
            "updatedDate" to updatedDate
        )
    )
}

data class BenCheckupCount(
    @ColumnInfo(name = "benId") val benId: Long,
    @ColumnInfo(name = "count") val count: Int
)

// ─────────────────────────────────────────────────────────────────────────────
// Network (FLW-1228), same pattern as the other dynamic forms:
// upload   = flw-api/disease/generalCheckUp/saveAll, list of FormSubmitRequest (asSubmitRequest);
// download = flw-api/disease/generalCheckUp/getAll, request HBNCVisitRequest, data = list of
//            { id, beneficiaryId, houseHoldId, visitDate, fields } (checkupFromFormRecord).
// GeneralCheckupDTO is the check-up details part of `fields`.
// Dates are plain local dates "yyyy-MM-dd" to avoid the UTC shift seen in FLW-1203.
// ─────────────────────────────────────────────────────────────────────────────

data class GeneralCheckupProblemDTO(
    val problemId: String,
    val label: String
)

data class GeneralCheckupDTO(
    val checkupId: String,
    val benId: Long,
    val hhId: Long,
    val ashaId: Int,
    val checkupDate: String,
    val problemTypeId: String,
    val problemTypeLabel: String?,
    val problems: List<GeneralCheckupProblemDTO>?,
    val otherText: String?,
    val priority: String?,
    val masterVersion: Int?,
    val referralReason: String,
    val referralDate: String,
    /** All answered form fields, fieldId → value (multi-select values comma-separated). */
    val formData: Map<String, String>?,
    val createdBy: String?,
    val createdDate: Long?,
    val updatedDate: Long?
) {
    /** A pulled check-up; null when a required field is missing. Problems are nullable for Gson. */
    fun asCacheModel(): GeneralCheckupCache? {
        val checkupMillis = fromApiDate(checkupDate) ?: return null
        val referralMillis = fromApiDate(referralDate) ?: checkupMillis
        val problemList = problems.orEmpty()
        return GeneralCheckupCache(
            checkupId = checkupId,
            benId = benId,
            hhId = hhId,
            ashaId = ashaId,
            checkupDate = checkupMillis,
            problemTypeId = problemTypeId,
            problemTypeLabel = problemTypeLabel.orEmpty(),
            problemIds = problemList.map { it.problemId },
            problemLabels = problemList.map { it.label },
            otherText = otherText,
            priority = priority ?: CheckupPriority.ROUTINE.name,
            masterVersion = masterVersion ?: 0,
            referralReason = referralReason,
            referralDate = referralMillis,
            referralMonth = monthKey(referralMillis),
            formData = formData?.let { formDataToJson(it) },
            createdBy = createdBy.orEmpty(),
            createdDate = createdDate ?: checkupMillis,
            updatedDate = updatedDate ?: checkupMillis,
            syncState = SyncState.SYNCED
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────

/** Keys of [GeneralCheckupCache.asSubmitRequest] `fields` that are check-up details, not form answers. */
val CHECKUP_DETAIL_KEYS = setOf(
    "checkupId", "ashaId", "checkupDate", "problemTypeId", "problemTypeLabel", "problems", "otherText",
    "priority", "masterVersion", "referralReason", "referralDate", "createdBy", "createdDate", "updatedDate"
)

/**
 * One record of generalCheckUp/getAll ({ beneficiaryId, houseHoldId, visitDate, fields }) as a synced
 * check-up; null when a required detail is missing or unreadable. The remaining `fields` keys are the
 * form answers and go back into formData.
 */
fun checkupFromFormRecord(record: JsonObject): GeneralCheckupCache? = runCatching {
    val fields = record.getAsJsonObject("fields") ?: return null
    val details = JsonObject().apply {
        CHECKUP_DETAIL_KEYS.forEach { key -> fields.get(key)?.takeUnless { it.isJsonNull }?.let { add(key, it) } }
        addProperty("benId", record.get("beneficiaryId")?.asLong ?: return null)
        addProperty("hhId", record.get("houseHoldId")?.takeUnless { it.isJsonNull }?.asLong ?: 0L)
    }
    val answers = fields.entrySet()
        .filter { (key, value) -> key !in CHECKUP_DETAIL_KEYS && value.isJsonPrimitive }
        .associate { (key, value) -> key to value.asString }
    Gson().fromJson(details, GeneralCheckupDTO::class.java)
        .copy(formData = answers.takeIf { it.isNotEmpty() })
        .asCacheModel()
}.getOrNull()

fun formDataToJson(values: Map<String, String>): String = Gson().toJson(values)

fun formDataFromJson(json: String): Map<String, String>? = runCatching {
    Gson().fromJson<Map<String, String>>(json, object : TypeToken<Map<String, String>>() {}.type)
}.getOrNull()

/** yyyyMM of a date in the device time zone, e.g. 202610. */
fun monthKey(millis: Long): Int {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    return cal.get(Calendar.YEAR) * 100 + cal.get(Calendar.MONTH) + 1
}

private const val API_DATE = "yyyy-MM-dd"

/** visitDate format of the generic dynamic-form submit (same as the form engine). */
private const val FORM_DATE = "dd-MM-yyyy"

fun toApiDate(millis: Long): String = SimpleDateFormat(API_DATE, Locale.ENGLISH).format(millis)

/** Parses "yyyy-MM-dd" (leading part only) as local midnight, so the day never shifts (FLW-1203). */
fun fromApiDate(date: String?): Long? =
    date?.take(API_DATE.length)?.let {
        runCatching { SimpleDateFormat(API_DATE, Locale.ENGLISH).parse(it)?.time }.getOrNull()
    }

