package org.piramalswasthya.sakhi.ui.home_activity.disease_control.general_checkup.form

import org.piramalswasthya.sakhi.model.HealthProblem
import org.piramalswasthya.sakhi.model.HealthProblemOwnerModule
import org.piramalswasthya.sakhi.model.HealthProblemType
import org.piramalswasthya.sakhi.model.dynamicEntity.FormFieldDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.OptionItem

/**
 * The server-driven Add Check-up form (`common-api/dynamicForm/form/general_check_up/fields`).
 *
 * Contract (sample in docs/general_check_up_form/):
 * - `checkup_date`: date.
 * - `problem_type`: dropdown; option value = type ID, extra option key `labelEn`.
 * - One multicheckbox per type, with conditional { dependsOn: "problem_type", expectedValue: <type ID> };
 *   option value = problem ID, extra option keys `labelEn`, `isEmergency`,
 *   `ownerModule` (MATERNAL | CHILD | ECL | CATARACT) and `requiresText`.
 * - Any type or problem option with `isActive: false` is hidden from new check-ups (FR12); saved
 *   check-ups keep their label snapshot.
 * - Other text box: a text field with conditional { dependsOn: <problem list fieldId>, expectedValue:
 *   <Other option value> }; shown and required while that option is ticked, and that option counts as
 *   "Other". Older schemas: `other_text` with no conditional, shown while a ticked problem has `requiresText`.
 *
 * The form engine keeps only label + value per option, so the extra keys are read here from the raw
 * options. Any other field the server adds is rendered as sent and its answer saved in formData.
 */
class GeneralCheckupFormSchema private constructor(
    val schema: FormSchemaDto,
    val problemTypes: List<HealthProblemType>,
    private val otherFieldIds: Set<String>,
) {
    val version: Int get() = schema.version

    val fields: List<FormFieldDto> = schema.sections.flatMap { it.fields }

    fun problemType(typeId: String?): HealthProblemType? = problemTypes.firstOrNull { it.typeId == typeId }

    /** The type whose problem list is [fieldId], or null if [fieldId] is not a problem list. */
    fun typeOfProblemField(fieldId: String): HealthProblemType? =
        problemTypes.firstOrNull { it.problemFieldId == fieldId }

    /** True for an Other text box: conditional on a problem list, or the older `other_text`. */
    fun isOtherField(fieldId: String): Boolean = fieldId == FIELD_OTHER || fieldId in otherFieldIds

    companion object {
        const val FIELD_DATE = "checkup_date"
        const val FIELD_TYPE = "problem_type"
        const val FIELD_OTHER = "other_text"
        /** Server label showing the priority for a type (v24: "Emergency" while type g11 is chosen). */
        const val FIELD_PRIORITY = "priority_text"
        const val OTHER_MAX_LENGTH = 250

        private const val TYPE_MULTI = "multicheckbox"
        private const val TYPE_TEXT = "text"
        private const val OTHER_SUFFIX = "_other"

        /** Null when the schema has no `problem_type` dropdown or no type has a problem list. */
        fun parse(schema: FormSchemaDto): GeneralCheckupFormSchema? {
            val fields = schema.sections.flatMap { it.fields }
            val typeField = fields.firstOrNull { it.fieldId == FIELD_TYPE } ?: return null
            val problemFieldByType = fields
                .filter { it.type == TYPE_MULTI && it.conditional?.dependsOn == FIELD_TYPE }
                .mapNotNull { field -> field.conditional?.expectedValue?.trim()?.takeIf { it.isNotEmpty() }?.let { it to field } }
                .toMap()
            val otherFields = otherTextFields(fields)
            val types = rawOptions(typeField.options).filterNot { it.hidden() }.mapNotNull { option ->
                val typeId = option.text("value") ?: return@mapNotNull null
                val problemField = problemFieldByType[typeId] ?: return@mapNotNull null
                val label = option.text("label") ?: typeId
                HealthProblemType(
                    typeId = typeId,
                    label = label,
                    labelEn = option.text("labelEn") ?: label,
                    problemFieldId = problemField.fieldId,
                    problems = rawOptions(problemField.options).filterNot { it.hidden() }
                        .mapNotNull { it.toProblem(otherValues(otherFields, problemField.fieldId)) }
                )
            }.filter { it.problems.isNotEmpty() }
            return if (types.isEmpty()) null
            else GeneralCheckupFormSchema(schema, types, otherFields.map { it.fieldId }.toSet())
        }

        /** Text fields whose conditional names a problem list: the Other boxes. */
        private fun otherTextFields(fields: List<FormFieldDto>): List<FormFieldDto> {
            val listIds = fields.filter { it.type == TYPE_MULTI && it.conditional?.dependsOn == FIELD_TYPE }
                .map { it.fieldId }.toSet()
            return fields.filter { it.type == TYPE_TEXT && it.conditional?.dependsOn?.trim() in listIds }
        }

        /** Option values of [listFieldId] that open an Other box. */
        private fun otherValues(otherFields: List<FormFieldDto>, listFieldId: String): Set<String> =
            otherFields.filter { it.conditional?.dependsOn?.trim() == listFieldId }
                .mapNotNull { it.conditional?.expectedValue?.trim()?.takeIf { v -> v.isNotEmpty() } }
                .toSet()

        /** Field types FormRendererAdapter can draw. */
        private val RENDERABLE_TYPES = setOf("label", "text", "number", "dropdown", "date", "radio", TYPE_MULTI, "image")

        /**
         * Every way [schema] breaks the contract above, one plain sentence each saying what to change.
         * Shown on screen in debug builds and logged, so the backend developer sees the exact fix.
         */
        fun issues(schema: FormSchemaDto): List<String> {
            val fields = schema.sections.flatMap { it.fields }
            val ids = fields.map { it.fieldId }.toSet()
            val issues = mutableListOf<String>()

            if (FIELD_DATE !in ids) issues += "Field \"$FIELD_DATE\" is missing. Add a field with fieldId \"$FIELD_DATE\" and type \"date\"."
            val typeField = fields.firstOrNull { it.fieldId == FIELD_TYPE }
            val typeIds = rawOptions(typeField?.options).mapNotNull { it.text("value") }
            when {
                typeField == null -> issues += "Field \"$FIELD_TYPE\" is missing. Add a field with fieldId \"$FIELD_TYPE\", " +
                        "type \"dropdown\", and the problem types as options (value = type ID, label, labelEn)."
                typeIds.isEmpty() -> issues += "Field \"$FIELD_TYPE\" has no options. Add the problem types as options " +
                        "(value = type ID, label, labelEn)."
            }
            val otherFields = otherTextFields(fields)
            if (FIELD_OTHER !in ids && otherFields.isEmpty()) issues += "No Other text box found. Add a field of type " +
                    "\"text\" with validation.maxLength 250 and conditional {\"dependsOn\": \"<problem list fieldId>\", " +
                    "\"expectedValue\": \"<Other option value>\"}."

            val lists = fields.filter { it.type == TYPE_MULTI && it.conditional?.dependsOn == FIELD_TYPE }
            if (lists.isEmpty()) {
                issues += "No problem lists found. Add one field of type \"$TYPE_MULTI\" per problem type, with fieldId " +
                        "\"problems_<typeId>\" and conditional {\"dependsOn\": \"$FIELD_TYPE\", \"expectedValue\": \"<typeId>\"}."
            } else {
                val listedTypes = lists.mapNotNull { it.conditional?.expectedValue?.trim() }.toSet()
                val withoutList = typeIds.filter { it !in listedTypes }
                if (withoutList.isNotEmpty()) issues += "Problem types ${withoutList.joinToString { "\"$it\"" }} have no problem list. " +
                        "Add a \"$TYPE_MULTI\" field with conditional.expectedValue = that type ID."
                lists.forEach { list ->
                    val options = rawOptions(list.options)
                    val otherValues = otherValues(otherFields, list.fieldId)
                    when {
                        options.isEmpty() -> issues += "Field \"${list.fieldId}\" has no options. Add the problems of that type."
                        options.none { it.flag("requiresText") || it.text("value") in otherValues } ->
                            issues += "Field \"${list.fieldId}\" has no \"Other\" option. Add {\"value\": \"<typeId>_other\", " +
                                    "\"label\": \"Other\", \"labelEn\": \"Other\"} and a text field with conditional " +
                                    "{\"dependsOn\": \"${list.fieldId}\", \"expectedValue\": \"<typeId>_other\"}."
                    }
                    otherValues.filter { v -> options.none { it.text("value") == v } }.forEach { v ->
                        issues += "An Other text box expects \"$v\" in \"${list.fieldId}\", but no option has that value. " +
                                "Make conditional.expectedValue equal to the Other option's value."
                    }
                    if (options.isNotEmpty() && options.none { it.text("labelEn") != null }) issues += "Field \"${list.fieldId}\" " +
                            "options have no \"labelEn\". Add the English name to each option."
                }
            }

            fields.filter { it.type !in RENDERABLE_TYPES }.forEach {
                issues += "Field \"${it.fieldId}\" has type \"${it.type}\", which the app cannot show. " +
                        if (it.type == "multi-select") "Use \"$TYPE_MULTI\" (one per problem type)." else "Remove it."
            }
            return issues
        }

        /** [issues] as one text, with the fields received, for the log and the debug screen. */
        fun issueReport(schema: FormSchemaDto): String {
            val received = schema.sections.flatMap { it.fields }.joinToString { "${it.fieldId} (${it.type})" }
            val issues = issues(schema).ifEmpty {
                listOf("No problem type has a problem list with options. Check that each conditional.expectedValue matches a \"$FIELD_TYPE\" option value.")
            }
            return "Form \"${schema.formId}\" version ${schema.version}: ${issues.size} problem(s)\n" +
                    issues.mapIndexed { i, issue -> "${i + 1}. $issue" }.joinToString("\n") +
                    "\nFields received: $received"
        }

        /** Options as raw maps: Gson gives maps, so keys beyond label/value survive. */
        private fun rawOptions(raw: Any?): List<Map<*, *>> = (raw as? List<*>).orEmpty().mapNotNull { item ->
            when (item) {
                is Map<*, *> -> item
                is String -> mapOf("value" to item, "label" to item)
                is OptionItem -> mapOf("value" to item.value, "label" to item.label)
                else -> null
            }
        }

        private fun Map<*, *>.toProblem(otherValues: Set<String>): HealthProblem? {
            val problemId = text("value") ?: return null
            val label = text("label") ?: problemId
            return HealthProblem(
                problemId = problemId,
                label = label,
                labelEn = text("labelEn") ?: label,
                emergency = flag("isEmergency"),
                ownerModule = text("ownerModule")?.let { name ->
                    HealthProblemOwnerModule.entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
                },
                // The server's expectedValue does not always match its Other option (v24: the Emergency
                // list's Other is "g12_other", its box expects "g11_other"), so "<x>_other" also counts.
                needsText = flag("requiresText") || problemId in otherValues ||
                        problemId.endsWith(OTHER_SUFFIX, ignoreCase = true)
            )
        }

        /** Gson reads every JSON number as Double, so 5 arrives as 5.0; whole numbers lose the ".0". */
        private fun Map<*, *>.text(key: String): String? = when (val v = this[key]) {
            null -> null
            is Number -> if (v.toDouble() % 1.0 == 0.0) v.toLong().toString() else v.toString()
            else -> v.toString()
        }?.trim()?.takeIf { it.isNotEmpty() }

        /** Only an explicit `isActive: false` hides an option; a missing key means active. */
        private fun Map<*, *>.hidden(): Boolean = containsKey("isActive") && this["isActive"] != null && !flag("isActive")

        private fun Map<*, *>.flag(key: String): Boolean = when (val v = this[key]) {
            is Boolean -> v
            is Number -> v.toInt() != 0
            is String -> v.equals("true", ignoreCase = true) || v == "1"
            else -> false
        }
    }
}
